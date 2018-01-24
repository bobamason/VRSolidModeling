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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class TranslateHandle3D extends Input3D {

    private final Plane plane = new Plane();
    private Vector3 normal = new Vector3();
    private Vector3 startHitPoint = new Vector3();
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
        final Vector3 from = new Vector3(0, 0, 0);
        final Vector3 to = new Vector3();
        final float len = 0.5f;
        switch (axis) {
            case AXIS_X:
                to.set(len, 0, 0);
                color.set(Color.RED);
                break;
            case AXIS_Y:
                to.set(0, len, 0);
                color.set(Color.BLUE);
                break;
            case AXIS_Z:
                to.set(0, 0, len);
                color.set(Color.GREEN);
                break;
        }
        builder.begin();
        final MeshPartBuilder part = builder.part("t" + axis.name(), GLES20.GL_TRIANGLES, VertexAttributes.Usage.Position, new Material(new BlendingAttribute(true, 0.5f), new DepthTestAttribute(false), ColorAttribute.createDiffuse(color)));
        ArrowShapeBuilder.build(part,
                from.x, from.y, from.z,
                to.x, to.y, to.z,
                Vector3.dst(from.x, from.y, from.z, to.x, to.y, to.z) * 0.33f,
                0.2f, 8);
        return new ModelInstance(builder.end());
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!updated) recalculateTransform();
        if (isDragging() && Intersector.intersectRayPlane(ray, plane, getHitPoint3D())) {
            Logger.d("dragging " + getHitPoint3D());
            handleDrag();
            return true;
        }
        final boolean intersectsRayBounds = super.intersectsRayBounds(ray, getHitPoint3D());
        if (intersectsRayBounds) {
            normal.set(ray.origin).sub(position);
            setToClosestUnitVector(normal);
            plane.set(position, normal);
        }
        return intersectsRayBounds;
    }

    private void handleDrag() {
        final Vector3 hitPoint = getHitPoint3D();
        switch (axis) {
            case AXIS_X:
                if (listener != null)
                    listener.dragged(axis, hitPoint.x - startHitPoint.x);
                break;
            case AXIS_Y:
                if (listener != null)
                    listener.dragged(axis, hitPoint.y - startHitPoint.y);
                break;
            case AXIS_Z:
                if (listener != null)
                    listener.dragged(axis, hitPoint.z - startHitPoint.z);
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
