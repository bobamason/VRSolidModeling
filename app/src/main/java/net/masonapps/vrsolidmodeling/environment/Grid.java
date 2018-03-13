package net.masonapps.vrsolidmodeling.environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.masonapps.libgdxgooglevr.gfx.PhongShader;
import org.masonapps.libgdxgooglevr.gfx.Transformable;

/**
 * Created by Bob on 6/6/2017.
 */

public class Grid extends Transformable implements Disposable {

    private final GridShader gridShader;
    private final float sectionSize;
    private final Model rect;
    private Array<ModelInstance> modelInstances = new Array<>();

    public Grid(float sectionSize, TextureRegion textureRegion, Color color) {
        this.sectionSize = sectionSize;
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Material material = new Material(TextureAttribute.createDiffuse(textureRegion), ColorAttribute.createDiffuse(color));
        final float radius = sectionSize / 2f;
        rect = modelBuilder.createRect(
                -radius, 0f, radius,
                radius, 0f, radius,
                radius, 0f, -radius,
                -radius, 0f, -radius,
                0f, 0f, 1f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
        final ModelInstance modelInstance = new ModelInstance(rect);
        gridShader = new GridShader(modelInstance.getRenderable(new Renderable()));
    }

    public void update(ModelBatch batch) {
        batch.render(modelInstances, gridShader);
    }

    @Override
    public void dispose() {
        if (rect != null) {

        }
    }

    private static class GridShader extends PhongShader {

        private static String vertexShader = null;
        private static String fragmentShader = null;
        private final int u_projTrans = register(new Uniform("u_projTrans"));
        private final int u_worldTrans = register(new Uniform("u_worldTrans"));
        private final int u_color1 = register(new Uniform("u_color1"));
        private final int u_color2 = register(new Uniform("u_color2"));
        private final int u_spacing = register(new Uniform("u_spacing"));
        private final int u_thickness = register(new Uniform("u_thickness"));
        private final Color color1 = new Color();
        private final Color color2 = new Color(Color.CLEAR);
        private final ShaderProgram program;
        private float spacing = 1f;
        private float thickness = 0.025f;

        public GridShader(Renderable renderable) {
            super(renderable);
            program = new ShaderProgram(getVertexShader(), getFragmentShader());

            if (!program.isCompiled())
                throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
            String log = program.getLog();
            if (log.length() > 0)
                Gdx.app.error(Grid.class.getSimpleName(), "Shader compilation log: " + log);
            init();
        }

        public static String getVertexShader() {
            if (vertexShader == null)
                vertexShader = Gdx.files.internal("shaders/grid.vertex.glsl").readString();
            return vertexShader;
        }

        public static String getFragmentShader() {
            if (fragmentShader == null)
                fragmentShader = Gdx.files.internal("shaders/grid.fragment.glsl").readString();
            return fragmentShader;
        }

        @Override
        public void init() {
            super.init(program, null);
        }

        @Override
        public int compareTo(Shader other) {
            return 0;
        }

        @Override
        public boolean canRender(Renderable instance) {
            return true;
        }

        @Override
        public void begin(Camera camera, RenderContext context) {
            program.begin();
            context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            context.setDepthTest(GL20.GL_LEQUAL, 0f, 1f);
            context.setCullFace(0);
            set(u_projTrans, camera.combined);
        }

        @Override
        public void render(Renderable renderable) {
            set(u_worldTrans, renderable.worldTransform);
            set(u_color1, color1);
            renderable.meshPart.render(program);
        }

        @Override
        public void end() {
            program.end();
        }

        @Override
        public void dispose() {
            super.dispose();
            program.dispose();
        }
    }
}
