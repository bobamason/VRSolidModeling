package net.masonapps.vrsolidmodeling.screens;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.SolidModelingGame;
import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.actions.AddAction;
import net.masonapps.vrsolidmodeling.actions.ColorAction;
import net.masonapps.vrsolidmodeling.actions.TransformAction;
import net.masonapps.vrsolidmodeling.actions.UndoRedoCache;
import net.masonapps.vrsolidmodeling.math.Animator;
import net.masonapps.vrsolidmodeling.math.RotationUtil;
import net.masonapps.vrsolidmodeling.math.Side;
import net.masonapps.vrsolidmodeling.modeling.AABBTree;
import net.masonapps.vrsolidmodeling.modeling.BaseModelingProject;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.ModelingObject;
import net.masonapps.vrsolidmodeling.modeling.ModelingProject;
import net.masonapps.vrsolidmodeling.modeling.primitives.Primitives;
import net.masonapps.vrsolidmodeling.modeling.transform.RotateWidget;
import net.masonapps.vrsolidmodeling.modeling.transform.ScaleWidget;
import net.masonapps.vrsolidmodeling.modeling.transform.TransformWidget3D;
import net.masonapps.vrsolidmodeling.modeling.transform.TranslateWidget;
import net.masonapps.vrsolidmodeling.modeling.ui.EditModeTable;
import net.masonapps.vrsolidmodeling.modeling.ui.MainInterface;
import net.masonapps.vrsolidmodeling.modeling.ui.ViewControls;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.State.STATE_EDITING;
import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.State.STATE_NONE;
import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.State.STATE_VIEW_TRANSFORM;
import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.ViewAction.ACTION_NONE;
import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.ViewAction.ROTATE;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class ModelingScreen extends VrWorldScreen implements SolidModelingGame.OnControllerBackPressedListener {

    private static final String TAG = ModelingScreen.class.getSimpleName();
    private static final float UI_ALPHA = 0.25f;
    private final MainInterface mainInterface;
    private final UndoRedoCache undoRedoCache;
    private final ShapeRenderer shapeRenderer;
    private final Animator rotationAnimator;
    private final Animator positionAnimator;
    private final Entity gridEntity;
    private TransformWidget3D transformUI;
    private boolean isTouchPadClicked = false;
    private Quaternion rotation = new Quaternion();
    private Quaternion lastRotation = new Quaternion();
    private Quaternion snappedRotation = new Quaternion();
    private Vector3 projectPosition = new Vector3(0, 0, -3);
    private Vector3 position = new Vector3(projectPosition);
    private Vector3 snappedPosition = new Vector3(projectPosition);
    private Vector3 center = new Vector3();
    private String projectName;
    private ViewAction viewAction = ACTION_NONE;
    private InputMode currentInputMode = InputMode.VIEW;
    private State currentState = STATE_NONE;
    @Nullable
    private ModelingEntity focusedEntity = null;
    @Nullable
    private ModelingEntity selectedEntity = null;
    private ModelingProject modelingProject;
    private Vector3 hitPoint = new Vector3();

    public ModelingScreen(VrGame game, String projectName) {
        this(game, projectName, new ArrayList<>());
    }

    public ModelingScreen(VrGame game, String projectName, List<ModelingObject> objects) {
        super(game);
        this.projectName = projectName;

        setBackgroundColor(Color.SKY);
        modelingProject = new ModelingProject();
        undoRedoCache = new UndoRedoCache();

        final ModelBuilder modelBuilder = new ModelBuilder();

        final TranslateWidget translateWidget = new TranslateWidget(modelBuilder);
        translateWidget.setVisible(false);
        final RotateWidget rotateWidget = new RotateWidget(modelBuilder);
        rotateWidget.setVisible(false);
        final ScaleWidget scaleWidget = new ScaleWidget(modelBuilder);
        scaleWidget.setVisible(false);

        final TransformWidget3D.OnTransformActionListener transformActionListener = new TransformWidget3D.OnTransformActionListener() {

            Matrix4 oldTransform;

            @Override
            public void onTransformStarted(@NonNull ModelingEntity entity) {
                oldTransform = entity.modelingObject.getTransform(new Matrix4());
            }

            @Override
            public void onTransformFinished(@NonNull ModelingEntity entity) {
                undoRedoCache.save(new TransformAction(entity, oldTransform, entity.modelingObject.getTransform(new Matrix4())));
            }
        };
        translateWidget.setListener(transformActionListener);
        rotateWidget.setListener(transformActionListener);
        scaleWidget.setListener(transformActionListener);

        transformUI = translateWidget;

        rotationAnimator = new Animator(new Animator.AnimationListener() {
            @Override
            public void apply(float value) {
                final Quaternion rot = modelingProject.getRotation();
                rot.set(rotation).slerp(snappedRotation, value);
                lastRotation.set(rot);
                modelingProject.invalidate();
                transformUI.setTransform(modelingProject.getTransform());
            }

            @Override
            public void finished() {
                rotation.set(snappedRotation);
                lastRotation.set(rotation);
            }
        });
        rotationAnimator.setInterpolation(Interpolation.linear);

        positionAnimator = new Animator(new Animator.AnimationListener() {
            @Override
            public void apply(float value) {
                modelingProject.getPosition().set(position).slerp(snappedPosition, value);
                modelingProject.invalidate();
                transformUI.setTransform(modelingProject.getTransform());
            }

            @Override
            public void finished() {
                position.set(snappedPosition);
            }
        });
        positionAnimator.setInterpolation(Interpolation.linear);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(shapeRenderer, spriteBatch);

        modelingProject.setPosition(projectPosition);

        final MainInterface.UiEventListener uiEventListener = new MainInterface.UiEventListener() {

            @Override
            public void onAddClicked(String key) {
                final ModelingObject modelingObject = new ModelingObject(Primitives.getPrimitive(key));
                final ModelingEntity entity = new ModelingEntity(modelingObject, modelingObject.createModelInstance(getSolidModelingGame().getPrimitiveModelMap()));
                modelingProject.add(entity);
                setSelectedEntity(entity);
                undoRedoCache.save(new AddAction(entity, modelingProject));
            }

            @Override
            public void onColorChanged(Color color) {
                if (selectedEntity != null) {
                    final ColorAction colorAction = new ColorAction(selectedEntity, selectedEntity.getDiffuseColor().cpy(), color.cpy(), c -> mainInterface.getColorPicker().setColor(c));
                    undoRedoCache.save(colorAction);
                    selectedEntity.setDiffuseColor(color);
                }
            }

            @Override
            public void onEditModeChanged(EditModeTable.EditMode mode) {
                switch (mode) {
                    case TRANSLATE:
                        transformUI = translateWidget;
                        rotateWidget.setVisible(false);
                        scaleWidget.setVisible(false);
                        transformUI.setVisible(true);
                        break;
                    case ROTATE:
                        transformUI = rotateWidget;
                        translateWidget.setVisible(false);
                        scaleWidget.setVisible(false);
                        transformUI.setVisible(true);
                        break;
                    case SCALE:
                        transformUI = scaleWidget;
                        translateWidget.setVisible(false);
                        rotateWidget.setVisible(false);
                        transformUI.setVisible(true);
                        break;
                    default:
                        transformUI.setVisible(false);
                        break;
                }
                transformUI.setEntity(selectedEntity);
            }

            @Override
            public void onUndoClicked() {
                undoRedoCache.undo();
            }

            @Override
            public void onRedoClicked() {
                undoRedoCache.redo();
            }

            @Override
            public void onExportClicked() {
                getSolidModelingGame().switchToExportScreen();
            }
        };
        final Skin skin = getSolidModelingGame().getSkin();
        mainInterface = new MainInterface(spriteBatch, skin, uiEventListener);
        mainInterface.loadWindowPositions(PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()));


        mainInterface.setViewControlsListener(new ViewControls.ViewControlListener() {
            @Override
            public void onViewSelected(Side side) {
                RotationUtil.rotateToViewSide(snappedRotation, side);
                final Quaternion rotDiff = Pools.obtain(Quaternion.class);
                rotDiff.set(rotation).conjugate().mulLeft(snappedRotation);
                final float angleRad = rotDiff.getAngleRad();
                final float duration = Math.abs(angleRad < MathUtils.PI ? angleRad : MathUtils.PI2 - angleRad) / MathUtils.PI;
                Pools.free(rotDiff);
                rotationAnimator.setDuration(duration);
                rotationAnimator.start();
            }
        });

        getWorld().add(Style.newGradientBackground(getVrCamera().far - 1f));

        gridEntity = new Entity(new ModelInstance(createGrid(modelBuilder, skin, 1f)));
        gridEntity.setLightingEnabled(false);
        getWorld().add(gridEntity).setTransform(modelingProject.getTransform());

        for (ModelingObject object : objects) {
            modelingProject.add(new ModelingEntity(object, object.createModelInstance(getSolidModelingGame().getPrimitiveModelMap())));
        }
    }

    private static Model createGrid(ModelBuilder builder, Skin skin, float radius) {
        final Material material = new Material(TextureAttribute.createDiffuse(skin.getRegion(Style.Drawables.grid)), FloatAttribute.createAlphaTest(0.5f), IntAttribute.createCullFace(0), new BlendingAttribute(true, 1f));
        return builder.createRect(
                -radius, 0f, radius,
                radius, 0f, radius,
                radius, 0f, -radius,
                -radius, 0f, -radius,
                0f, 1f, 0f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
    }

    private static Model createGridBox(ModelBuilder builder, Skin skin, float radius) {
        final Material material = new Material(TextureAttribute.createDiffuse(skin.getRegion(Style.Drawables.grid)), ColorAttribute.createDiffuse(Color.GRAY), FloatAttribute.createAlphaTest(0.5f), IntAttribute.createCullFace(GL20.GL_FRONT), new BlendingAttribute(true, 1f));
        return builder.createBox(radius * 2f, radius * 2f, radius * 2f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
    }

    private static Model createBoxModel(ModelBuilder builder, Color color, BoundingBox bounds) {
        builder.begin();
        final MeshPartBuilder part = builder.part("s", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(color), new BlendingAttribute(true, 0.5f)));
        BoxShapeBuilder.build(part, bounds);
        return builder.end();
    }

    protected static void drawEntityBounds(ShapeRenderer shapeRenderer, ModelingEntity entity, Color color) {
        shapeRenderer.setColor(color);
        shapeRenderer.setTransformMatrix(entity.modelInstance.transform);
        final BoundingBox bounds = entity.getBounds();
        drawBounds(shapeRenderer, bounds);
    }

    protected static void debugAABBTree(ShapeRenderer shapeRenderer, ModelingProject modelingProject, Color color) {
        shapeRenderer.setColor(color);
        shapeRenderer.setTransformMatrix(modelingProject.getTransform());
        Queue<AABBTree.Node> queue = new LinkedList<>();
        queue.offer(modelingProject.getAABBTree().root);
        while (!queue.isEmpty()) {
            AABBTree.Node node = queue.poll();
            if (node.bb.isValid())
                drawBounds(shapeRenderer, node.bb);
            if (node instanceof AABBTree.InnerNode) {
                final AABBTree.InnerNode innerNode = (AABBTree.InnerNode) node;
                if (innerNode.child1 != null)
                    queue.offer(innerNode.child1);
                if (innerNode.child2 != null)
                    queue.offer(innerNode.child2);
            }
        }
    }

    private static void drawBounds(ShapeRenderer shapeRenderer, BoundingBox bounds) {
        shapeRenderer.box(bounds.min.x, bounds.min.y, bounds.max.z,
                bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    private SolidModelingGame getSolidModelingGame() {
        return (SolidModelingGame) game;
    }

    @Override
    protected World createWorld() {
        return new World() {

            @Override
            public void update() {
                super.update();
                modelingProject.update();
                transformUI.update();
            }

            @Override
            public void render(ModelBatch batch, Environment environment) {
                super.render(batch, environment);
                modelingProject.render(batch, environment);
                transformUI.render(batch);
            }
        };
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        lights.add(light);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()).edit();
        mainInterface.saveWindowPositions(editor);
        editor.apply();
    }

    @Override
    public void show() {
        super.show();
        GdxVr.input.setInputProcessor(mainInterface);
//        buttonControls.attachListener();
    }

    @Override
    public void hide() {
        super.hide();
        GdxVr.input.setInputProcessor(null);
//        buttonControls.detachListener();
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()).edit();
        mainInterface.saveWindowPositions(editor);
        editor.apply();
    }

    @Override
    public void update() {
        super.update();
        if (currentInputMode == InputMode.SELECT)
            getSolidModelingGame().getCursor().position.set(hitPoint);
        mainInterface.act();
        rotationAnimator.update(GdxVr.graphics.getDeltaTime());
        positionAnimator.update(GdxVr.graphics.getDeltaTime());
        gridEntity.setPosition(modelingProject.getPosition());
        gridEntity.setRotation(modelingProject.getRotation());
//        Logger.d(GdxVr.graphics.getFramesPerSecond() + "fps");
    }

    @Nullable
    public ModelingEntity getSelectedEntity() {
        return selectedEntity;
    }

    private void setSelectedEntity(@Nullable ModelingEntity entity) {
        selectedEntity = entity;
        mainInterface.setEntity(selectedEntity);

        if (selectedEntity != null) {
            center.set(selectedEntity.modelingObject.getPosition());
            snappedPosition.set(center).scl(-1).mul(rotation).add(projectPosition);
            positionAnimator.setDuration(0.5f);
            positionAnimator.start();

            final Color diffuseColor = selectedEntity.getDiffuseColor();
            if (diffuseColor != null)
                mainInterface.getColorPicker().setColor(diffuseColor);
        } else {
            transformUI.setVisible(false);
            center.set(0, 0, 0);
            snappedPosition.set(center).scl(-1).mul(rotation).add(projectPosition);
            positionAnimator.setDuration(0.5f);
            positionAnimator.start();
        }
    }

    @Override
    public void render(Camera camera, int whichEye) {
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setTransformMatrix(modelingProject.getTransform());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 0, 1, 0, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 0, 0, 1);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.line(0, 0, 0, 0, 1, 0);
        shapeRenderer.end();

        super.render(camera, whichEye);

        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
//        debugAABBTree(shapeRenderer, modelingProject, Color.YELLOW);
        transformUI.drawShapes(shapeRenderer);
        if (focusedEntity != null) {
            drawEntityBounds(shapeRenderer, focusedEntity, Color.BLACK);
        }
        if (selectedEntity != null) {
            drawEntityBounds(shapeRenderer, selectedEntity, Color.WHITE);
        }
        shapeRenderer.end();
        
        mainInterface.draw(camera);
    }

    private void rotate() {
        final Quaternion rotDiff = Pools.obtain(Quaternion.class);
        rotDiff.set(lastRotation).conjugate().mulLeft(GdxVr.input.getControllerOrientation());
//        RotationUtil.snapAxisAngle(rotDiff);
//        Logger.d("rotDiff " + rotDiff);

        rotation.mulLeft(rotDiff);
        modelingProject.setRotation(rotation);
        position.set(center).scl(-1).mul(rotation).add(projectPosition);
        modelingProject.setPosition(position);
//        gridEntity.setPosition(modelingProject.getPosition());
//        gridEntity.setRotation(modelingProject.getRotation());
        lastRotation.set(GdxVr.input.getControllerOrientation());
        Pools.free(rotDiff);
        transformUI.setTransform(modelingProject.getTransform());
    }

    @Override
    public void onControllerBackButtonClicked() {
        if (!mainInterface.onControllerBackButtonClicked()) {
            if (selectedEntity != null) {
                setSelectedEntity(null);
            } else {
                getSolidModelingGame().closeModelingScreen();
                getSolidModelingGame().switchToStartupScreen();
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        undoRedoCache.clear();
    }

    public String getProjectName() {
        return projectName;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        updateCurrentInputMode();
        if (currentState == STATE_VIEW_TRANSFORM) {
            getSolidModelingGame().setCursorVisible(false);
            mainInterface.setVisible(false);
            if (viewAction == ROTATE)
                rotate();
//            else if (transformAction == PAN)
//                pan();
//            else if (transformAction == ZOOM)
//                zoom();
        } else {
            getSolidModelingGame().setCursorVisible(true);
        }

        if (controller.clickButtonState) {
            if (!isTouchPadClicked) {
                onTouchPadButtonDown();
                isTouchPadClicked = true;
            }
        } else {
            if (isTouchPadClicked) {
                onTouchPadButtonUp();
                isTouchPadClicked = false;
            }
        }
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
    }

    private void onTouchPadButtonDown() {
        switch (currentInputMode) {
            case UI:
                currentState = STATE_NONE;
                break;
            case EDIT:
                currentState = STATE_EDITING;
                transformUI.touchDown();
                break;
            case SELECT:
                if (focusedEntity != null) {
                    setSelectedEntity(focusedEntity);
                }
                break;
            case VIEW:
                lastRotation.set(GdxVr.input.getControllerOrientation());
                viewAction = ROTATE;
                currentState = STATE_VIEW_TRANSFORM;
                break;
            default:
                break;
        }
    }

    private void onTouchPadButtonUp() {
        switch (currentState) {
            case STATE_EDITING:
                transformUI.touchUp();
                break;
            case STATE_VIEW_TRANSFORM:
                if (RotationUtil.snap(rotation, snappedRotation, 0.1f)) {
                    final Quaternion rotDiff = Pools.obtain(Quaternion.class);
                    rotDiff.set(rotation).conjugate().mulLeft(snappedRotation);
                    final float angleRad = rotDiff.getAngleRad();
                    final float duration = Math.abs(angleRad < MathUtils.PI ? angleRad : MathUtils.PI2 - angleRad) / MathUtils.PI;
                    Pools.free(rotDiff);
                    rotationAnimator.setDuration(duration);
                    rotationAnimator.start();

                    snappedPosition.set(center).scl(-1).mul(snappedRotation).add(projectPosition);
                    positionAnimator.setDuration(duration);
                    positionAnimator.start();
                }
//                final float len = getVrCamera().position.len();
//                RotationUtil.setToClosestUnitVector(getVrCamera().position).scl(len);
//                RotationUtil.setToClosestUnitVector(getVrCamera().up);
//                getVrCamera().lookAt(Vector3.Zero);
                viewAction = ACTION_NONE;
                break;
            default:
                break;
        }
        currentState = STATE_NONE;
        mainInterface.setAlpha(1f);
        mainInterface.setVisible(true);
    }

    private void updateCurrentInputMode() {
        switch (currentState) {
            case STATE_EDITING:
                transformUI.performRayTest(getControllerRay());
                currentInputMode = InputMode.EDIT;
                break;
            case STATE_NONE:
                if (mainInterface.isCursorOver())
                    currentInputMode = InputMode.UI;
                else if (transformUI.performRayTest(getControllerRay()))
                    currentInputMode = InputMode.EDIT;
                else if ((focusedEntity = modelingProject.rayTest(getControllerRay(), hitPoint)) != null)
                    currentInputMode = InputMode.SELECT;
                else
                    currentInputMode = InputMode.VIEW;
                break;
            case STATE_VIEW_TRANSFORM:
                currentInputMode = InputMode.VIEW;
                break;
        }
    }

    public BaseModelingProject getModelingProject() {
        return modelingProject;
    }

    enum ViewAction {
        ACTION_NONE, ROTATE, PAN, ZOOM
    }

    enum InputMode {
        UI, EDIT, SELECT, VIEW
    }

    enum State {
        STATE_VIEW_TRANSFORM, STATE_EDITING, STATE_NONE
    }
}
