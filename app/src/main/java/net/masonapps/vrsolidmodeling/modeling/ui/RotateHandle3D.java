package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.math.PlaneUtils;
import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class RotateHandle3D extends Input3D {


    private static final float HANDLE_RADIUS = 0.075f;
    private final float circleRadius = 1f;
    private float angleDeg = 0f;
    private int numCircleSegments = 64;
    private Plane plane = new Plane();
    private Vector2 vec2 = new Vector2();
    private Vector3 tmpV = new Vector3();
    @Nullable
    private RotationListener listener = null;

    public RotateHandle3D(ModelBuilder builder, Axis axis) {
        super(createModelInstance(builder, axis), createBounds(), axis);
        setLightingEnabled(false);
        switch (axis) {
            case AXIS_X:
                setPosition(tmpV.set(0, 0, circleRadius).rotate(Vector3.X, angleDeg));
                plane.set(0f, 0f, 0f, 1f, 0f, 0f);
                break;
            case AXIS_Y:
                setPosition(tmpV.set(circleRadius, 0, 0).rotate(Vector3.Y, angleDeg));
                plane.set(0f, 0f, 0f, 0f, 1f, 0f);
                break;
            case AXIS_Z:
                setPosition(tmpV.set(0, circleRadius, 0).rotate(Vector3.Z, angleDeg));
                plane.set(0f, 0f, 0f, 0f, 0f, 1f);
                break;
        }
    }

    private static ModelInstance createModelInstance(ModelBuilder builder, Axis axis) {
        final Color color = new Color();
        switch (axis) {
            case AXIS_X:
                color.set(Color.RED);
                break;
            case AXIS_Y:
                color.set(Color.BLUE);
                break;
            case AXIS_Z:
                color.set(Color.GREEN);
                break;
        }
        final Model model = builder.createSphere(HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, 12, 6, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)), VertexAttributes.Usage.Position);
        return new ModelInstance(model);
    }

    private static BoundingBox createBounds() {
        final float r = HANDLE_RADIUS / (float) Math.sqrt(3);
        return new BoundingBox(new Vector3(-r, -r, -r), new Vector3(r, r, r));
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!updated) recalculateTransform();
        if (dragging && Intersector.intersectRayPlane(ray, plane, getHitPoint3D())) {
            angleChanged();
            return true;
        }
        return super.intersectsRaySphere(ray, getHitPoint3D());
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void angleChanged() {
        PlaneUtils.toSubSpace(plane, getHitPoint3D(), vec2);
        Logger.d("point in plane " + vec2 + " axis " + axis.name());
        switch (axis) {
            case AXIS_X:
                angleDeg = -MathUtils.atan2(vec2.y, -vec2.x) * MathUtils.radiansToDegrees;
                setPosition(tmpV.set(0, 0, circleRadius).rotate(Vector3.X, angleDeg));
                break;
            case AXIS_Y:
                angleDeg = MathUtils.atan2(vec2.y, vec2.x) * MathUtils.radiansToDegrees;
                setPosition(tmpV.set(circleRadius, 0, 0).rotate(Vector3.Y, angleDeg));
                break;
            case AXIS_Z:
                angleDeg = -MathUtils.atan2(vec2.x, vec2.y) * MathUtils.radiansToDegrees;
                setPosition(tmpV.set(0, circleRadius, 0).rotate(Vector3.Z, angleDeg));
                break;
        }
        if (listener != null)
            listener.dragged(axis, angleDeg);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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

    public void setListener(@Nullable RotationListener listener) {
        this.listener = listener;
    }

    public void drawCircle(ShapeRenderer renderer) {
        for (int i = 0; i < numCircleSegments; i++) {
            final float a = MathUtils.PI2 / (float) numCircleSegments * i;
            final float a2 = MathUtils.PI2 / (float) numCircleSegments * (i + 1);
            final float r = circleRadius;
            switch (axis) {
                case AXIS_X:
                    renderer.setColor(Color.RED);
                    renderer.line(0, MathUtils.sin(a) * r, -MathUtils.cos(a) * r, 0, MathUtils.sin(a2) * r, -MathUtils.cos(a2) * r);
                    break;
                case AXIS_Y:
                    renderer.setColor(Color.BLUE);
                    renderer.line(MathUtils.cos(a) * r, 0, -MathUtils.sin(a) * r, MathUtils.cos(a2) * r, 0, -MathUtils.sin(a2) * r);
                    break;
                case AXIS_Z:
                    renderer.setColor(Color.GREEN);
                    renderer.line(MathUtils.cos(a) * r, MathUtils.sin(a) * r, 0, MathUtils.cos(a2) * r, MathUtils.sin(a2) * r, 0);
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (modelInstance != null) {
            modelInstance.model.dispose();
            modelInstance = null;
        }
    }

    public void setNumCircleSegments(int numCircleSegments) {
        this.numCircleSegments = numCircleSegments;
    }

    public float getAngleDeg() {
        return angleDeg;
    }

    public void setAngleDeg(float angleDeg) {
        this.angleDeg = angleDeg;
        switch (axis) {
            case AXIS_X:
                setPosition(tmpV.set(0, 0, circleRadius).rotate(Vector3.X, angleDeg));
                break;
            case AXIS_Y:
                setPosition(tmpV.set(circleRadius, 0, 0).rotate(Vector3.Y, angleDeg));
                break;
            case AXIS_Z:
                setPosition(tmpV.set(0, circleRadius, 0).rotate(Vector3.Z, angleDeg));
                break;
        }
    }

    public interface RotationListener {
        void touchDown(Axis axis);

        void dragged(Axis axis, float angleDeg);

        void touchUp(Axis axis);
    }
}
