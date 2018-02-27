package net.masonapps.vrsolidmodeling.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.SolidModelingGame;
import net.masonapps.vrsolidmodeling.io.ProjectFileIO;
import net.masonapps.vrsolidmodeling.ui.ProjectPreviewTestList;

import org.json.JSONException;
import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Bob on 8/30/2017.
 */

public class OpenProjectListScreen extends RoomScreen implements ProjectPreviewTestList.OnProjectSelectedListener {

    protected final ProjectPreviewTestList<File> ui;
    private final List<File> list;
    private final Consumer<File> consumer;


    public OpenProjectListScreen(SolidModelingGame game, List<File> list, Consumer<File> consumer) {
        super(game);
        this.list = list;
        this.consumer = consumer;
//        getWorld().add(Style.newGradientBackground(getVrCamera().far - 1f));
//        getWorld().add(Grid.newInstance(20f, 0.5f, 0.02f, Color.WHITE, Color.DARK_GRAY)).setPosition(0, -1.3f, 0);

        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        ui = new ProjectPreviewTestList<File>(list, this) {
            @Override
            protected ModelData loadProject(File file, BoundingBox bounds) throws IOException, JSONException {
                return ProjectFileIO.loadModelData(file, bounds);
            }

            @Override
            protected void onLoadFailed(File file, Throwable e) {
                Logger.e(file.getName(), e);
            }
        };

//        final MeshInfo meshInfo = Primitives.getPrimitiveMeshInfo(Primitives.KEY_TORUS);
//        try {
//            final Model model = new Model(ProjectFileIO.loadModelData(list.get(0)));
//            getWorld().add(new Entity(new ModelInstance(model))).setPosition(0, 0, -2);
//        } catch (Exception e){
//            Logger.e("failed to create test model", e);
//        }
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
            public void update() {
                super.update();
                ui.update();
            }

            @Override
            public void render(ModelBatch batch, Environment environment) {
                final ModelInstance roomInstance = getSolidModelingGame().getRoomInstance();
                if (roomInstance != null)
                    batch.render(roomInstance);
                super.render(batch, environment);
                ui.render(batch, environment);
            }
        };
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
    }

    @Override
    public void onControllerBackButtonClicked() {
        getSolidModelingGame().switchToStartupScreen();
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        ui.onControllerTouchPadEvent(event);
    }

    @Override
    public void onProjectSelected(ProjectPreviewTestList.ProjectItem item) {
        consumer.accept(list.get(item.index));
    }
}
