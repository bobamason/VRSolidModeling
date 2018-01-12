package net.masonapps.vrsolidmodeling.screens;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.SolidModelingGame;
import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.math.Animator;
import net.masonapps.vrsolidmodeling.math.RotationUtil;
import net.masonapps.vrsolidmodeling.math.Side;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.ModelingWorld;
import net.masonapps.vrsolidmodeling.modeling.UndoRedoCache;
import net.masonapps.vrsolidmodeling.modeling.ui.MainInterface;
import net.masonapps.vrsolidmodeling.modeling.ui.TransformUI;
import net.masonapps.vrsolidmodeling.modeling.ui.ViewControls;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;

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
    private final Animator snapAnimator;
    private final TransformUI transformUI;
    private final Entity gridEntity;
    private boolean isTouchPadClicked = false;
    private Quaternion rotation = new Quaternion();
    private Quaternion lastRotation = new Quaternion();
    private Quaternion startRotation = new Quaternion();
    private Quaternion snappedRotation = new Quaternion();
    private String projectName;
    private ViewAction viewAction = ACTION_NONE;
    private InputMode currentInputMode = InputMode.VIEW;
    private State currentState = STATE_NONE;
    @Nullable
    private ModelingEntity focusedEntity = null;
    @Nullable
    private ModelingEntity selectedEntity = null;
    private ModelingWorld modelingWorld;
    private Vector3 hitPoint = new Vector3();

    public ModelingScreen(VrGame game, String projectName) {
        super(game);
        this.projectName = projectName;

        setBackgroundColor(Color.SKY);
        modelingWorld = new ModelingWorld();

        snapAnimator = new Animator(new Animator.AnimationListener() {
            @Override
            public void apply(float value) {
                modelingWorld.transformable.getRotation().set(rotation).slerp(snappedRotation, value);
                lastRotation.set(modelingWorld.transformable.getRotation());
                modelingWorld.transformable.recalculateTransform();
                gridEntity.setPosition(modelingWorld.transformable.getPosition());
                gridEntity.setRotation(modelingWorld.transformable.getRotation());

//                getVrCamera().position.set(0, 0, 4).mul(snappedRotation);
//                getVrCamera().up.set(0, 1, 0).mul(snappedRotation);
//                getVrCamera().lookAt(Vector3.Zero);
            }

            @Override
            public void finished() {
                transformUI.setEntity(selectedEntity);
                rotation.set(snappedRotation);
                lastRotation.set(rotation);
            }
        });
        snapAnimator.setInterpolation(Interpolation.linear);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(shapeRenderer, spriteBatch);

        modelingWorld.transformable.setPosition(0, 0, -4);

        final MainInterface.UiEventListener uiEventListener = new MainInterface.UiEventListener() {

            @Override
            public void onAddClicked() {
                final Color color = new Color(Color.LIGHT_GRAY);
                final Vector3 pos = new Vector3(MathUtils.random(-2f, 2f), MathUtils.random(-2f, 2f), MathUtils.random(-2f, 2f));
                modelingWorld.add(new ModelingEntity(getSolidModelingGame().getPrimitive("cube"), new Material(ColorAttribute.createDiffuse(color)))).setPosition(pos).scale(0.25f);
            }

            @Override
            public void onColorChanged(Color color) {
                if (selectedEntity != null)
                    selectedEntity.setDiffuseColor(color);
            }

            @Override
            public void onUndoClicked() {
                UndoRedoCache.applySaveData();
            }

            @Override
            public void onRedoClicked() {
                UndoRedoCache.applySaveData();
            }

            @Override
            public void onExportClicked() {
                getSolidModelingGame().switchToExportScreen();
            }
        };
        final Skin skin = getSolidModelingGame().getSkin();
        mainInterface = new MainInterface(spriteBatch, skin, uiEventListener);
        mainInterface.loadWindowPositions(PreferenceManager.getDefaultSharedPreferences(GdxVr.app.getContext()));
        undoRedoCache = new UndoRedoCache();


        mainInterface.setViewControlsListener(new ViewControls.ViewControlListener() {
            @Override
            public void onViewSelected(Side side) {
                final Quaternion tmpQ = Pools.obtain(Quaternion.class);
                RotationUtil.rotateToViewSide(tmpQ, side);
                rotation.set(tmpQ);
                lastRotation.set(tmpQ);
                // TODO: 12/20/2017 update camera 
                Pools.free(tmpQ);
            }
        });

//        brush.setUseSymmetry(false);
        undoRedoCache.save(null);
        transformUI = new TransformUI(spriteBatch, skin);
        transformUI.setVisible(false);
        mainInterface.addProcessor(transformUI);

        final ModelBuilder modelBuilder = new ModelBuilder();
        gridEntity = new Entity(new ModelInstance(createGrid(modelBuilder, skin, 1f)));
        gridEntity.setLightingEnabled(false);
        getWorld().add(gridEntity);
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
        shapeRenderer.box(bounds.min.x, bounds.min.y, bounds.max.z,
                bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    private SolidModelingGame getSolidModelingGame() {
        return (SolidModelingGame) game;
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
        modelingWorld.update();
        mainInterface.act();
        snapAnimator.update(GdxVr.graphics.getDeltaTime());
//        Logger.d(GdxVr.graphics.getFramesPerSecond() + "fps");
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        getModelBatch().begin(camera);
        modelingWorld.render(getModelBatch(), getEnvironment());
        getModelBatch().end();
        shapeRenderer.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
        if (focusedEntity != null) {
            drawEntityBounds(shapeRenderer, focusedEntity, Color.BLACK);
        }
        if (selectedEntity != null) {
            drawEntityBounds(shapeRenderer, selectedEntity, Color.WHITE);
        }
        shapeRenderer.setTransformMatrix(modelingWorld.transformable.getTransform());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(0, 0, 0, 1, 0, 0);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, 0, 0, 0, 0, 1);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.line(0, 0, 0, 0, 1, 0);
        shapeRenderer.end();
        mainInterface.draw(camera);
    }

    private void rotate() {
        final Quaternion rotDiff = Pools.obtain(Quaternion.class);
        rotDiff.set(lastRotation).conjugate().mulLeft(GdxVr.input.getControllerOrientation());
//        rotDiff.conjugate();
        rotation.mulLeft(rotDiff);
//        getVrCamera().position.set(0, 0, 4).mul(rotation);
//        getVrCamera().up.set(0, 1, 0).mul(rotation);
//        getVrCamera().lookAt(Vector3.Zero);
        modelingWorld.transformable.setRotation(rotation);
        gridEntity.setPosition(modelingWorld.transformable.getPosition());
        gridEntity.setRotation(modelingWorld.transformable.getRotation());
        lastRotation.set(GdxVr.input.getControllerOrientation());
        Pools.free(rotDiff);
    }

    @Override
    public void onControllerBackButtonClicked() {
        if (!mainInterface.onControllerBackButtonClicked()) {
            if (selectedEntity != null) {
                selectedEntity = null;
                transformUI.setEntity(selectedEntity);
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
            case SELECT:
                if (focusedEntity != null) {
                    selectedEntity = focusedEntity;
                    transformUI.setEntity(selectedEntity);
                    final Color diffuseColor = selectedEntity.getDiffuseColor();
                    if (diffuseColor != null)
                        mainInterface.getColorPicker().setColor(diffuseColor);
                }
                break;
            case VIEW:
                startRotation.set(GdxVr.input.getControllerOrientation());
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
            case STATE_VIEW_TRANSFORM:
                RotationUtil.snap(rotation, snappedRotation);

                final Quaternion rotDiff = Pools.obtain(Quaternion.class);
                rotDiff.set(rotation).conjugate().mulLeft(snappedRotation);
                final float angleRad = rotDiff.getAngleRad();
                final float duration = Math.abs(angleRad < MathUtils.PI ? angleRad : MathUtils.PI2 - angleRad) / MathUtils.PI;
                Pools.free(rotDiff);
                snapAnimator.setDuration(duration);
                snapAnimator.start();
                transformUI.setVisible(false);
//                final float len = getVrCamera().position.len();
//                RotationUtil.setToClosestUnitVector(getVrCamera().position).scl(len);
//                RotationUtil.setToClosestUnitVector(getVrCamera().up);
//                getVrCamera().lookAt(Vector3.Zero);
                viewAction = ACTION_NONE;
                currentState = STATE_NONE;
                break;
            default:
                break;
        }
        mainInterface.setAlpha(1f);
        mainInterface.setVisible(true);
    }

    private void updateCurrentInputMode() {
        switch (currentState) {
            case STATE_NONE:
                if (mainInterface.isCursorOver())
                    currentInputMode = InputMode.UI;
                else if ((focusedEntity = modelingWorld.rayTest(GdxVr.input.getInputRay(), hitPoint)) != null)
                    currentInputMode = InputMode.SELECT;
                else
                    currentInputMode = InputMode.VIEW;
                break;
            case STATE_VIEW_TRANSFORM:
                currentInputMode = InputMode.VIEW;
                break;
        }
    }

    enum ViewAction {
        ACTION_NONE, ROTATE, PAN, ZOOM
    }

    enum InputMode {
        UI, SELECT, VIEW
    }

    enum State {
        STATE_VIEW_TRANSFORM, STATE_NONE
    }
}
