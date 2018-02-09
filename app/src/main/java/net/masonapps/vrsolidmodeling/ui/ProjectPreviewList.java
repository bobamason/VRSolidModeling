package net.masonapps.vrsolidmodeling.ui;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pool;

import net.masonapps.vrsolidmodeling.modeling.AABBTree;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.ModelingObject;
import net.masonapps.vrsolidmodeling.modeling.PreviewModelingProject;

import org.json.JSONException;
import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.io.IOException;
import java.util.HashMap;
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
    private HashMap<String, Model> primitiveModelMap;
    private float maxScroll = 0f;
    private boolean needsLayout = true;
    private float SPEED = 2f;

    public ProjectPreviewList(List<T> list, HashMap<String, Model> primitiveModelMap, OnProjectSelectedListener listener) {
        this.list = list;
        maxScroll = list.size() * ITEM_WIDTH;
        this.listener = listener;
        this.primitiveModelMap = primitiveModelMap;
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
                final ProjectItem projectItem = visibleItems.get(i);
                if (projectItem == null) {
                    init(list.get(i), i);
                } else {
                    final PreviewModelingProject project = projectItem.project;
                    if (project != null) {
                        project.setPosition((i - startIndex) * ITEM_WIDTH - scrollX * (startIndex * ITEM_WIDTH), 0f, -3f);
                        project.setScale(ITEM_WIDTH / project.getRadius());
                        Logger.d("project " + i + " position = " + project.getPosition());
                    }
                }
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
            final PreviewModelingProject project = visibleItems.get(key).project;
            if (project != null) {
                project.update();
            }
        }
    }

    public void render(ModelBatch batch, Environment environment) {
        for (int i = 0; i < visibleItems.size(); i++) {
            final int key = visibleItems.keyAt(i);
            final PreviewModelingProject project = visibleItems.get(key).project;
            if (project != null)
                project.render(batch, environment);
        }
    }

    private void init(final T t, final int index) {
        Logger.d("init " + index);
        final ProjectItem projectItem = itemPool.obtain();
        projectItem.key = index;
        projectItem.isRecycled = false;
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
        }).thenAccept(modelingObjects -> {
            if (modelingObjects != null) {
                GdxVr.app.postRunnable(() -> {
                    if (!projectItem.isRecycled) {
                        projectItem.project = new PreviewModelingProject(modelingObjects, primitiveModelMap);
                        projectItem.project.update();
                        projectItem.loadModelFuture = null;
                        aabbTree.insert(projectItem);
                    }
                });
            }
        });
        visibleItems.put(projectItem.key, projectItem);
        itemPool.free(projectItem);
    }

    protected abstract List<ModelingObject> loadProject(T t) throws IOException, JSONException;

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
        public CompletableFuture<List<ModelingObject>> loadModelFuture = null;
        @Nullable
        public PreviewModelingProject project = null;
        public boolean isRecycled = false;
        @Nullable
        private AABBTree.LeafNode node = null;
        private BoundingBox aabb = new BoundingBox();
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
            isRecycled = true;
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
            if (project != null)
                aabb.set(project.getBounds()).mul(project.getTransform());
            return aabb;
        }

        @Override
        public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
            if (project == null) return false;
            final ModelingEntity entity = project.rayTest(ray, intersection.hitPoint);
            final boolean rayTest = entity != null;
            if (rayTest)
                intersection.object = this;
            return rayTest;
        }
    }
}
