package net.masonapps.vrsolidmodeling.ui;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pool;

import org.json.JSONException;
import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Bob Mason on 2/8/2018.
 */

public abstract class ProjectPreviewList<T> implements VrInputProcessor {

    public static final float ITEM_WIDTH = 1f;
    public static final float TOUCHPAD_SCALE = 150f;
    private final List<T> list;
    private final GestureDetector gestureDetector;
    private final AABBTree aabbTree = new AABBTree();
    private final Pool<ProjectItem> itemPool = new Pool<ProjectItem>() {
        @Override
        protected ProjectItem newObject() {
            return new ProjectItem();
        }
    };
    private final Vector3 hitPoint = new Vector3();
    private final OnProjectSelectedListener listener;
    private AABBTree.IntersectionInfo intersetionInfo = new AABBTree.IntersectionInfo();
    @Nullable
    private ProjectItem focusedItem = null;
    private boolean scrolling = false;
    private float scrollX = 0f;
    private boolean isCursorOver = false;
    private SparseArray<ProjectItem> visibleItems = new SparseArray<>();
    private float maxScroll = 0f;
    private boolean needsLayout = true;
    private float SPEED = 2f;

    public ProjectPreviewList(List<T> list, OnProjectSelectedListener listener) {
        this.list = list;
        maxScroll = list.size() * ITEM_WIDTH;
        this.listener = listener;
        final GestureDetector.GestureAdapter gestureAdapter = new GestureDetector.GestureAdapter() {

            public float startX = 0f;

            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                startX = x;
                return super.touchDown(x, y, pointer, button);
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                return false;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
//                if (animating) return false;
                scrolling = true;
                scrollX += (x - startX) / TOUCHPAD_SCALE * SPEED;
                scrollX = MathUtils.clamp(scrollX, 0, maxScroll);
                invalidate();
                return true;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
//                if (animating) return false;
                scrolling = false;
                invalidate();
                return true;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
//                if (animating) return false;
                final float vX = velocityX / TOUCHPAD_SCALE;
//                if (vX < -0.85f && currentIndex < itemCount - 1) {
//                    
//                } else if (vX > 0.85f && currentIndex > 0) {
//                    
//                }
                return true;
            }
        };
        gestureDetector = new GestureDetector(gestureAdapter);
    }

    private void validate() {
        if (needsLayout) {
            final int startIndex = getStartIndex();
            final int endIndex = getEndIndex();
            for (int i = 0; i < visibleItems.size(); i++) {
                final int key = visibleItems.keyAt(i);
                if (key < 0) continue;
                if (key < startIndex || key >= endIndex)
                    recycle(visibleItems.get(key));
            }
            for (int i = startIndex; i < endIndex; i++) {
                ProjectItem projectItem = visibleItems.get(i);
                if (projectItem == null) {
                    projectItem = init(list.get(i), i);
                }
                projectItem.position.set((i - startIndex) * ITEM_WIDTH - scrollX * (startIndex * ITEM_WIDTH), 0f, -3f);
                projectItem.updateTransform();
            }
            needsLayout = false;
        }
    }

    private int getStartIndex() {
        return Math.round(scrollX / ITEM_WIDTH);
    }

    private int getEndIndex() {
        return Math.min(getStartIndex() + 6, list.size());
    }

    private void invalidate() {
        needsLayout = true;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        isCursorOver = false;
        final boolean rayTest = aabbTree.rayTest(ray, intersetionInfo);
        if (rayTest) {
            hitPoint.set(intersetionInfo.hitPoint);
            focusedItem = (ProjectItem) intersetionInfo.object;
            isCursorOver = true;
        }
        return rayTest;
    }

    public void update() {
        validate();
        for (int i = 0; i < visibleItems.size(); i++) {
            final int key = visibleItems.keyAt(i);
            final Entity project = visibleItems.get(key).project;
            if (project != null) {
                Logger.d("updating project");
                project.validate();
            }
        }
    }

    public void render(ModelBatch batch, Environment environment) {
        for (int i = 0; i < visibleItems.size(); i++) {
            final int key = visibleItems.keyAt(i);
            final Entity project = visibleItems.get(key).project;
            if (project != null && project.modelInstance != null)
                batch.render(project.modelInstance, environment);
        }
    }

    private ProjectItem init(final T t, final int index) {
        Logger.d("init " + index);
        final ProjectItem projectItem = itemPool.obtain();
        visibleItems.put(projectItem.key, projectItem);
        projectItem.key = index;
        projectItem.loadModelFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return loadProject(t);
            } catch (IOException | JSONException e) {
                throw new RuntimeException("failed to load project #" + index, e);
            }
        });
        projectItem.loadModelFuture.exceptionally(e -> {
            GdxVr.app.postRunnable(() -> onLoadFailed(t, e));
            return null;
        }).thenAccept(modelData -> {
            if (modelData != null) {
                GdxVr.app.postRunnable(() -> {
                    projectItem.project = new Entity(new ModelInstance(new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal)));
                        projectItem.updateTransform();
                        projectItem.loadModelFuture = null;
                        aabbTree.insert(projectItem);
                        invalidate();
                });
            }
        });
        return projectItem;
    }

    protected abstract ModelData loadProject(T t) throws IOException, JSONException;

    protected abstract void onLoadFailed(T t, Throwable e);

    private void recycle(ProjectItem projectItem) {
        Logger.d("recycle " + projectItem.key);
        visibleItems.remove(projectItem.key);
        aabbTree.remove(projectItem);
        itemPool.free(projectItem);
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    @Nullable
    @Override
    public Vector2 getHitPoint2D() {
        return null;
    }

    @Nullable
    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver) {
            listener.onProjectSelected(focusedItem);
        }
        return isCursorOver;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void onControllerTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                gestureDetector.touchDown(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0, 0);
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                gestureDetector.touchDragged(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0);
                break;
            case DaydreamTouchEvent.ACTION_UP:
                gestureDetector.touchUp(event.x * TOUCHPAD_SCALE, event.y * TOUCHPAD_SCALE, 0, 0);
                break;
        }
    }


    public interface OnProjectSelectedListener {
        void onProjectSelected(ProjectItem item);
    }

    public static class ProjectItem implements Pool.Poolable, AABBTree.AABBObject {

        public int key = -1;
        @Nullable
        public CompletableFuture<ModelData> loadModelFuture = null;
        @Nullable
        public Entity project = null;
        @Nullable
        private AABBTree.LeafNode node = null;
        private BoundingBox aabb = new BoundingBox();
        private Vector3 position = new Vector3();
//        public Animator animator = new Animator(new Animator.AnimationListener() {
//            @Override
//            public void apply(float value) {
//                if(project != null){
//                    project.getPosition().set(position).lerp(targetPosition, value);
//                    project.getRotation().set(rotation).slerp(targetRotation, value);
//                }
//            }
//
//            @Override
//            public void finished() {
//                position.set(targetPosition);
//                rotation.set(targetRotation);
//            }
//        });
//        public Vector3 position = new Vector3();
//        public Vector3 targetPosition = new Vector3();
//        public Quaternion rotation = new Quaternion();
//        public Quaternion targetRotation = new Quaternion();

        @Override
        public void reset() {
            position.set(0, 0, 0);
            key = -1;
            if (loadModelFuture != null) {
                loadModelFuture.cancel(true);
                loadModelFuture = null;
            }
            if (project != null) {
                project.dispose();
                project = null;
            }
        }

        @Nullable
        @Override
        public AABBTree.LeafNode getNode() {
            return node;
        }

        @Override
        public void setNode(@Nullable AABBTree.LeafNode node) {
            this.node = node;
        }

        @Override
        public BoundingBox getAABB() {
            return aabb;
        }

        @Override
        public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
            if (project == null) return false;
            intersection.object = null;
            final boolean rayTest = project.intersectsRaySphere(ray, intersection.hitPoint);
            if (rayTest)
                intersection.object = this;
            return rayTest;
        }

        public void updateTransform() {
            if (project != null) {
                project.setPosition(position);
                project.setScale(ITEM_WIDTH / project.getRadius());
                project.validate();
                aabb.set(project.getBounds()).mul(project.getTransform());
                Logger.d("project " + key + " position = " + project.getPosition());
                Logger.d("project " + key + " scale = " + project.getScale().x);
            } else {
                Logger.d("project " + key + " is null");
            }
        }
    }
}
