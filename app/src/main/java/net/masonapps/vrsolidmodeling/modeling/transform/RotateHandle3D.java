package net.masonapps.vrsolidmodeling.modeling.transform;

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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.vrsolidmodeling.math.SnapUtil;

import org.masonapps.libgdxgooglevr.gfx.Transformable;
import org.masonapps.libgdxgooglevr.math.PlaneUtils;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class RotateHandle3D extends DragHandle3D {


    private static final float HANDLE_RADIUS = 0.075f;
    private final float circleRadius = 1f;
    private int numCircleSegments = 64;
    private Plane plane = new Plane();
    private Vector2 vec2 = new Vector2();
    private Quaternion tmpQ = new Quaternion();
    private float angle = 0f;
    private Vector3 tmpV = new Vector3();
    private boolean shouldSetPlane = true;

    public RotateHandle3D(ModelBuilder builder, Axis axis) {
        super(createModelInstance(builder, axis), createBounds(axis), axis);
        setLightingEnabled(false);
        switch (axis) {
            case AXIS_X:
                plane.set(0f, 0f, 0f, 1f, 0f, 0f);
                break;
            case AXIS_Y:
                plane.set(0f, 0f, 0f, 0f, 1f, 0f);
                break;
            case AXIS_Z:
                plane.set(0f, 0f, 0f, 0f, 0f, 1f);
                break;
        }
    }

    private static ModelInstance createModelInstance(ModelBuilder builder, Axis axis) {
        final Color color = new Color();
        final Matrix4 matrix = new Matrix4();
        switch (axis) {
            case AXIS_X:
                color.set(Color.RED);
                matrix.setToTranslation(0, 0, 1f);
                break;
            case AXIS_Y:
                matrix.setToTranslation(1f, 0, 0);
                color.set(Color.BLUE);
                break;
            case AXIS_Z:
                matrix.setToTranslation(0, 1f, 0);
                color.set(Color.GREEN);
                break;
        }
        final Model model = builder.createSphere(HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, 12, 6, new Material(new BlendingAttribute(true, 1f), new DepthTestAttribute(0), ColorAttribute.createDiffuse(color)), VertexAttributes.Usage.Position);
        model.meshes.get(0).transform(matrix);
        return new ModelInstance(model);
    }

    private static BoundingBox createBounds(Axis axis) {
        final float r = HANDLE_RADIUS / (float) Math.sqrt(3);
        final Matrix4 matrix = new Matrix4();
        switch (axis) {
            case AXIS_X:
                matrix.setToTranslation(0, 0, 1f);
                break;
            case AXIS_Y:
                matrix.setToTranslation(1f, 0, 0);
                break;
            case AXIS_Z:
                matrix.setToTranslation(0, 1f, 0);
                break;
        }
        return new BoundingBox(new Vector3(-r, -r, -r), new Vector3(r, r, r)).mul(matrix);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (transformable == null) return false;
        if (!updated) recalculateTransform();
        if (isDragging()) {
            if (shouldSetPlane) {
                switch (axis) {
                    case AXIS_X:
                        plane.set(transformable.getPosition(), tmpV.set(1, 0, 0));
                        break;
                    case AXIS_Y:
                        plane.set(transformable.getPosition(), tmpV.set(0, 1, 0));
                        break;
                    case AXIS_Z:
                        plane.set(transformable.getPosition(), tmpV.set(0, 0, 1));
                        break;
                }
                shouldSetPlane = false;
            }
            if (Intersector.intersectRayPlane(ray, plane, getHitPoint3D())) {
                angleChanged();
                return true;
            }
        }
        return super.performRayTest(ray);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void angleChanged() {
        if (transformable == null) return;
        PlaneUtils.toSubSpace(plane, getHitPoint3D(), vec2);
        final Quaternion rotation = transformable.getRotation();
        switch (axis) {
            case AXIS_X:
                angle = -MathUtils.atan2(vec2.y, -vec2.x) * MathUtils.radiansToDegrees;
                angle = SnapUtil.snap(angle, 5);
                rotation.setEulerAngles(rotation.getYaw(), angle, rotation.getRoll());
                setRotation(0, angle, 0);
                break;
            case AXIS_Y:
                angle = MathUtils.atan2(vec2.y, vec2.x) * MathUtils.radiansToDegrees;
                angle = SnapUtil.snap(angle, 5);
                rotation.setEulerAngles(angle, rotation.getPitch(), rotation.getRoll());
                setRotation(angle, 0, 0);
                break;
            case AXIS_Z:
                angle = -MathUtils.atan2(vec2.x, vec2.y) * MathUtils.radiansToDegrees;
                angle = SnapUtil.snap(angle, 5);
                rotation.setEulerAngles(rotation.getYaw(), rotation.getPitch(), angle);
                setRotation(0, 0, angle);
                break;
        }
        transformable.invalidate();
    }

    @Override
    public void update() {
        if (transformable != null) {
            setPosition(transformable.getPosition());
            switch (axis) {
                case AXIS_X:
                    setRotation(0, angle, 0);
                    break;
                case AXIS_Y:
                    setRotation(angle, 0, 0);
                    break;
                case AXIS_Z:
                    setRotation(0, 0, angle);
                    break;
            }
        }
    }

    @Override
    public void setTransformable(@Nullable Transformable transformable) {
        super.setTransformable(transformable);
        if (transformable != null) {
            setPosition(transformable.getPosition());
            final Quaternion rotation = transformable.getRotation();
            switch (axis) {
                case AXIS_X:
                    angle = rotation.getPitch();
                    setRotation(0, angle, 0);
                    break;
                case AXIS_Y:
                    angle = rotation.getYaw();
                    setRotation(angle, 0, 0);
                    break;
                case AXIS_Z:
                    angle = rotation.getRoll();
                    setRotation(0, 0, angle);
                    break;
            }
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        shouldSetPlane = true;
        return super.touchUp(screenX, screenY, pointer, button);

    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        for (int i = 0; i < numCircleSegments; i++) {
            final float a = MathUtils.PI2 / (float) numCircleSegments * i;
            final float a2 = MathUtils.PI2 / (float) numCircleSegments * (i + 1);
            final float r = circleRadius;
            switch (axis) {
                case AXIS_X:
                    renderer.setColor(Color.RED);
                    renderer.line(tmp.set(0, MathUtils.sin(a) * r, -MathUtils.cos(a) * r).add(position), tmp2.set(0, MathUtils.sin(a2) * r, -MathUtils.cos(a2) * r).add(position));
                    break;
                case AXIS_Y:
                    renderer.setColor(Color.BLUE);
                    renderer.line(tmp.set(MathUtils.cos(a) * r, 0, -MathUtils.sin(a) * r).add(position), tmp2.set(MathUtils.cos(a2) * r, 0, -MathUtils.sin(a2) * r).add(position));
                    break;
                case AXIS_Z:
                    renderer.setColor(Color.GREEN);
                    renderer.line(tmp.set(MathUtils.cos(a) * r, MathUtils.sin(a) * r, 0).add(position), tmp2.set(MathUtils.cos(a2) * r, MathUtils.sin(a2) * r, 0).add(position));
                    break;
            }
        }
        Pools.free(tmp);
        Pools.free(tmp2);
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
}
