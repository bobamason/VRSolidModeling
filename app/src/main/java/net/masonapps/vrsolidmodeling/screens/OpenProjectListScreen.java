package net.masonapps.vrsolidmodeling.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.SolidModelingGame;
import net.masonapps.vrsolidmodeling.mesh.MeshUtils;
import net.masonapps.vrsolidmodeling.ui.ProjectPreviewList;

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

public class OpenProjectListScreen extends RoomScreen implements ProjectPreviewList.OnProjectSelectedListener {

    protected final ProjectPreviewList<File> ui;
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
        ui = new ProjectPreviewList<File>(list, this) {
            @Override
            protected ModelData loadProject(File file) throws IOException, JSONException {
//                return ProjectFileIO.loadModelData(file);
                final Mesh mesh = new ModelBuilder().createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal).meshes.get(0);
                final float[] vertices = new float[mesh.getNumVertices()];
                mesh.getVertices(vertices);
                final short[] indices = new short[mesh.getNumIndices()];
                mesh.getIndices(indices);
                final VertexAttribute[] attrArray = new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.Normal()};
                return MeshUtils.createModelData(vertices, indices, attrArray);
            }

            @Override
            protected void onLoadFailed(File file, Throwable e) {
                Logger.e(file.getName(), e);
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
    public void onProjectSelected(ProjectPreviewList.ProjectItem item) {
        consumer.accept(list.get(item.key));
    }
}
