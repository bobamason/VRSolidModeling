package net.masonapps.vrsolidmodeling.screens;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.SolidModelingGame;
import net.masonapps.vrsolidmodeling.math.RotationUtil;
import net.masonapps.vrsolidmodeling.math.Side;
import net.masonapps.vrsolidmodeling.modeling.ModelingWorld;
import net.masonapps.vrsolidmodeling.ui.MainInterface;
import net.masonapps.vrsolidmodeling.ui.UndoRedoCache;
import net.masonapps.vrsolidmodeling.ui.ViewControls;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;

import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.State.STATE_NONE;
import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.State.STATE_VIEW_TRANSFORM;
import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.TransformAction.ACTION_NONE;
import static net.masonapps.vrsolidmodeling.screens.ModelingScreen.TransformAction.ROTATE;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class ModelingScreen extends VrWorldScreen implements SolidModelingGame.OnControllerBackPressedListener {

    private static final String TAG = ModelingScreen.class.getSimpleName();
    private static final float UI_ALPHA = 0.25f;
    private final MainInterface mainInterface;
    private final UndoRedoCache undoRedoCache;
    private boolean isTouchPadClicked = false;
    private Quaternion rotation = new Quaternion();
    private Quaternion lastRotation = new Quaternion();
    private Quaternion startRotation = new Quaternion();
    private String projectName;
    private TransformAction transformAction = ACTION_NONE;
    private InputMode currentInputMode = InputMode.VIEW;
    private State currentState = STATE_NONE;

    public ModelingScreen(VrGame game, String projectName) {
        super(game);
        this.projectName = projectName;

        final ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);

        final MainInterface.UiEventListener uiEventListener = new MainInterface.UiEventListener() {

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
        mainInterface = new MainInterface(spriteBatch, getSolidModelingGame().getSkin(), uiEventListener);
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
    protected World createWorld() {
        return new ModelingWorld();
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
        getVrCamera().position.set(0, 0, 2);
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
        mainInterface.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        mainInterface.draw(camera);
    }

    private void rotate() {
        final Quaternion rotDiff = Pools.obtain(Quaternion.class);
        final Vector3 axis = Pools.obtain(Vector3.class);
        rotDiff.set(lastRotation).conjugate().mulLeft(GdxVr.input.getControllerOrientation());
        rotation.mulLeft(rotDiff);
        // TODO: 12/20/2017 rotate camera
        getVrCamera().position.mul(rotDiff);
        getVrCamera().lookAt(Vector3.Zero);
        lastRotation.set(GdxVr.input.getControllerOrientation());
        Pools.free(rotDiff);
        Pools.free(axis);
    }

    @Override
    public void onControllerBackButtonClicked() {
        if (!mainInterface.onControllerBackButtonClicked()) {
            getSolidModelingGame().closeModelingScreen();
            getSolidModelingGame().switchToStartupScreen();
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
            if (transformAction == ROTATE)
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
            case VIEW:
                startRotation.set(GdxVr.input.getControllerOrientation());
                lastRotation.set(GdxVr.input.getControllerOrientation());
                transformAction = ROTATE;
                currentState = STATE_VIEW_TRANSFORM;
                break;
        }
    }

    private void onTouchPadButtonUp() {
        switch (currentState) {
            case STATE_NONE:
                break;
            case STATE_VIEW_TRANSFORM:
                transformAction = ACTION_NONE;
                currentState = STATE_NONE;
                break;
        }
        currentState = State.STATE_NONE;
        mainInterface.setAlpha(1f);
        mainInterface.setVisible(true);
    }

    private void updateCurrentInputMode() {
        switch (currentState) {
            case STATE_NONE:
                if (mainInterface.isCursorOver())
                    currentInputMode = InputMode.UI;
                else
                    currentInputMode = InputMode.VIEW;
                break;
            case STATE_VIEW_TRANSFORM:
                currentInputMode = InputMode.VIEW;
                break;
        }
    }

    enum TransformAction {
        ACTION_NONE, ROTATE, PAN, ZOOM
    }

    enum InputMode {
        UI, VIEW
    }

    enum State {
        STATE_VIEW_TRANSFORM, STATE_NONE
    }
}
