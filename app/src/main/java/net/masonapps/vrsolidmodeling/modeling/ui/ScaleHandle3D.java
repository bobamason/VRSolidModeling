package net.masonapps.vrsolidmodeling.modeling.ui;

import android.opengl.GLES20;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class ScaleHandle3D extends DragHandle3D {

    private static final float len = 0.5f;
    private final Plane plane = new Plane();
    private Vector3 startHitPoint = new Vector3();
    private boolean shouldSetPlane = true;
    private float scaleValue = 1f;
    private float lastDst = 0f;

    public ScaleHandle3D(ModelBuilder builder, Axis axis) {
        super(createModelInstance(builder, axis), axis);
        setLightingEnabled(false);
        switch (axis) {
            case AXIS_X:
                plane.set(1f, 0f, 0f, 0f);
                break;
            case AXIS_Y:
                plane.set(0f, 1f, 0f, 0f);
                break;
            case AXIS_Z:
                plane.set(0f, 0f, 1f, 0f);
                break;
        }
    }

    private static ModelInstance createModelInstance(ModelBuilder builder, Axis axis) {
        final Color color = new Color();
        final float s = 0.25f;
        builder.begin();
        final MeshPartBuilder part = builder.part("t" + axis.name(), GLES20.GL_TRIANGLES, VertexAttributes.Usage.Position, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)));
        final Matrix4 matrix = new Matrix4();
        switch (axis) {
            case AXIS_X:
                matrix.setToTranslation(len, 0f, 0f);
                break;
            case AXIS_Y:
                matrix.setToTranslation(0f, len, 0f);
                break;
            case AXIS_Z:
                matrix.setToTranslation(0f, 0f, len);
                break;
        }
        matrix.scale(s, s, s);
        BoxShapeBuilder.build(part, matrix);
        return new ModelInstance(builder.end());
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!updated) recalculateTransform();
        if (isDragging() && Intersector.intersectRayPlane(ray, plane, getHitPoint3D())) {
            handleDrag();
            return true;
        }
        return super.intersectsRayBounds(ray, getHitPoint3D());
    }

    private void handleDrag() {
        if (transformable == null) return;
        final float dst = getHitPoint3D().dst(transformable.getPosition());
        float value = dst - lastDst + 1f;
        switch (axis) {
            case AXIS_X:
                transformable.scaleX(value);
                break;
            case AXIS_Y:
                transformable.scaleY(value);
                break;
            case AXIS_Z:
                transformable.scaleZ(value);
                break;
        }
        lastDst = dst;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (transformable != null) {
            lastDst = getHitPoint3D().dst(transformable.getPosition());
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        shouldSetPlane = true;
        return super.touchUp(screenX, screenY, pointer, button);

    }

    @Override
    public void update() {
        if (transformable != null) {
            setPosition(transformable.getPosition());
            setRotation(transformable.getRotation());
        }
    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        switch (axis) {
            case AXIS_X:
                renderer.setColor(Color.RED);
                renderer.line(Vector3.Zero, tmp.set(len * scaleValue, 0, 0).mul(rotation).add(position));
                break;
            case AXIS_Y:
                renderer.setColor(Color.BLUE);
                renderer.line(Vector3.Zero, tmp.set(0, len * scaleValue, 0).mul(rotation).add(position));
                break;
            case AXIS_Z:
                renderer.setColor(Color.GREEN);
                renderer.line(Vector3.Zero, tmp.set(0, 0, len * scaleValue).mul(rotation).add(position));
                break;
        }
        Pools.free(tmp);
    }
}
