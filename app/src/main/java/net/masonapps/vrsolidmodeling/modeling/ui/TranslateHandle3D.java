package net.masonapps.vrsolidmodeling.modeling.ui;

import android.opengl.GLES20;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class TranslateHandle3D extends Input3D {

    private final Plane plane = new Plane();
    private Vector3 normal = new Vector3();
    private Vector3 startHitPoint = new Vector3();
    private boolean shouldSetPlane = true;
    @Nullable
    private TranslationListener listener = null;

    public TranslateHandle3D(ModelBuilder builder, Axis axis) {
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
        final Vector3 from = new Vector3();
        final Vector3 to = new Vector3();
        final float len = 0.65f;
        switch (axis) {
            case AXIS_X:
                from.set(len * 0.5f, 0, 0);
                to.set(len, 0, 0);
                color.set(Color.RED);
                break;
            case AXIS_Y:
                from.set(0, len * 0.5f, 0);
                to.set(0, len, 0);
                color.set(Color.BLUE);
                break;
            case AXIS_Z:
                from.set(0, 0, len * 0.5f);
                to.set(0, 0, len);
                color.set(Color.GREEN);
                break;
        }
        builder.begin();
        final MeshPartBuilder part = builder.part("t" + axis.name(), GLES20.GL_TRIANGLES, VertexAttributes.Usage.Position, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)));
        ArrowShapeBuilder.build(part,
                from.x, from.y, from.z,
                to.x, to.y, to.z,
                Vector3.dst(from.x, from.y, from.z, to.x, to.y, to.z) * 0.45f,
                0.35f, 8);
        return new ModelInstance(builder.end());
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!updated) recalculateTransform();
        if (isDragging()) {
            if (shouldSetPlane) {
                normal.set(ray.origin).sub(position);
                setToClosestUnitVector(normal);
                plane.set(position, normal);
                shouldSetPlane = false;
            }
            if (Intersector.intersectRayPlane(ray, plane, getHitPoint3D())) {
                handleDrag();
                return true;
            }
        }
        return super.intersectsRayBounds(ray, getHitPoint3D());
    }

    private void handleDrag() {
        final Vector3 hitPoint = getHitPoint3D();
        switch (axis) {
            case AXIS_X:
                if (listener != null)
                    listener.dragged(axis, MathUtils.clamp(hitPoint.x - startHitPoint.x, -10f, 10f));
                break;
            case AXIS_Y:
                if (listener != null)
                    listener.dragged(axis, MathUtils.clamp(hitPoint.y - startHitPoint.y, -10f, 10f));
                break;
            case AXIS_Z:
                if (listener != null)
                    listener.dragged(axis, MathUtils.clamp(hitPoint.z - startHitPoint.z, -10f, 10f));
                break;
        }
    }


    private void setToClosestUnitVector(Vector3 v) {
        switch (axis) {
            case AXIS_X:
                if (Math.abs(v.y) > Math.abs(v.z)) {
                    v.x = 0f;
                    v.y = Math.signum(v.y);
                    v.z = 0f;
                } else {
                    v.x = 0f;
                    v.y = 0f;
                    v.z = Math.signum(v.z);
                }
                break;
            case AXIS_Y:
                if (Math.abs(v.x) > Math.abs(v.z)) {
                    v.x = Math.signum(v.x);
                    v.y = 0f;
                    v.z = 0f;
                } else {
                    v.x = 0f;
                    v.y = 0f;
                    v.z = Math.signum(v.z);
                }
                break;
            case AXIS_Z:
                if (Math.abs(v.x) > Math.abs(v.y)) {
                    v.x = Math.signum(v.x);
                    v.y = 0f;
                    v.z = 0f;
                } else {
                    v.x = 0f;
                    v.y = Math.signum(v.y);
                    v.z = 0f;
                }
                break;
        }
    }

    public void drawLines(ShapeRenderer renderer) {
        if (!isDragging()) return;
        switch (axis) {
            case AXIS_X:
                renderer.setColor(Color.RED);
                renderer.line(-20, 0, 0, 20, 0, 0);
                break;
            case AXIS_Y:
                renderer.setColor(Color.BLUE);
                renderer.line(0, -20, 0, 0, 20, 0);
                break;
            case AXIS_Z:
                renderer.setColor(Color.GREEN);
                renderer.line(0, 0, -20, 0, 0, 20);
                break;
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        startHitPoint.set(getHitPoint3D());
        if (listener != null)
            listener.touchDown(axis);
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (listener != null)
            listener.touchUp(axis);
        shouldSetPlane = true;
        return super.touchUp(screenX, screenY, pointer, button);
    }

    public void setListener(@Nullable TranslationListener listener) {
        this.listener = listener;
    }

    public interface TranslationListener {
        void touchDown(Axis axis);

        void dragged(Axis axis, float value);

        void touchUp(Axis axis);
    }
}
