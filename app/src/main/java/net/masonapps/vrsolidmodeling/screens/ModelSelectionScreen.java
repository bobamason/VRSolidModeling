package net.masonapps.vrsolidmodeling.screens;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.SolidModelingGame;
import net.masonapps.vrsolidmodeling.ui.FileButtonBar;
import net.masonapps.vrsolidmodeling.ui.ModelSelectionUI;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.vr.VrGraphics;

import java.util.List;

/**
 * Created by Bob on 8/30/2017.
 */

public abstract class ModelSelectionScreen<T> extends RoomScreen implements FileButtonBar.OnFileButtonClicked<T>, ModelSelectionUI.ModelAdapter<T> {

    protected final ModelSelectionUI<T> ui;


    public ModelSelectionScreen(SolidModelingGame game, Skin skin, List<T> list) {
        super(game);
//        getWorld().add(Style.newGradientBackground(getVrCamera().far - 1f));
//        getWorld().add(Grid.newInstance(20f, 0.5f, 0.02f, Color.WHITE, Color.DARK_GRAY)).setPosition(0, -1.3f, 0);

        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        ui = new ModelSelectionUI<>(game, spriteBatch, skin, list, this, this);
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
        return new World() {
            @Override
            public void render(ModelBatch batch, Environment environment) {
                super.render(batch, environment);
                ui.renderProjects(batch, environment);
            }
        };
    }

    @Override
    public void update() {
        super.update();
        ui.act();
//        final boolean isLoadingModel = currentModel == null && list.size() > 0;
//        loadingSpinner.setVisible(isLoadingModel);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        VrGraphics.checkGlError("rendered scene");
        ui.draw(camera);
        VrGraphics.checkGlError("rendered ui");
    }

    @Override
    public void show() {
        super.show();
        GdxVr.input.setInputProcessor(ui);
    }

    @Override
    public void hide() {
        super.hide();
        GdxVr.input.setInputProcessor(null);
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
        ui.onControllerButtonEvent(controller, event);
    }

    @Override
    public void onControllerBackButtonClicked() {
        getSolidModelingGame().switchToStartupScreen();
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        ui.onControllerTouchPadEvent(controller, event);
    }
}
