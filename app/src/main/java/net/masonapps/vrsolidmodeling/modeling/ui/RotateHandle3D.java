package net.masonapps.vrsolidmodeling.modeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class RotateHandle3D extends Input3D {


    private static final float HANDLE_RADIUS = 0.05f;
    private final float circleRadius = 1f;
    private final Vector3 center = new Vector3();
    private final float angleDeg = 0f;
    private int numCircleSegments = 64;
    private boolean dragging = false;
    private Plane plane = new Plane();

    public RotateHandle3D(Axis axis) {
        super(createModelInstance(axis), createBounds(), axis);
        setLightingEnabled(false);
    }

    private static ModelInstance createModelInstance(Axis axis) {
        final ModelBuilder builder = new ModelBuilder();
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
        final Model model = builder.createSphere(HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, HANDLE_RADIUS * 2f, 12, 6, new Material(ColorAttribute.createDiffuse(color)), VertexAttributes.Usage.Position);
        return new ModelInstance(model);
    }

    private static BoundingBox createBounds() {
        final float r = HANDLE_RADIUS * (float) Math.sqrt(2);
        return new BoundingBox(new Vector3(-r, -r, -r), new Vector3(r, r, r));
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        switch (axis) {
            case AXIS_X:
                plane.set(position.x, position.y, position.z, 1f, 0f, 0f);
                break;
            case AXIS_Y:
                plane.set(position.x, position.y, position.z, 0f, 1f, 0f);
                break;
            case AXIS_Z:
                plane.set(position.x, position.y, position.z, 0f, 0f, 1f);
                break;
        }
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

    private void angleChanged() {
        
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        dragging = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        dragging = false;
        return true;
    }

    public void drawCircle(ShapeRenderer renderer) {
        for (int i = 0; i < numCircleSegments; i++) {
            final float a = MathUtils.PI2 / (float) numCircleSegments * i;
            final float a2 = MathUtils.PI2 / (float) numCircleSegments * (i + 1);
            final float r = circleRadius;
            switch (axis) {
                case AXIS_X:
                    renderer.line(center.x * r, center.y + MathUtils.sin(a), center.z - MathUtils.cos(a) * r, center.x, center.y + MathUtils.sin(a2) * r, center.z - MathUtils.cos(a2) * r);
                    break;
                case AXIS_Y:
                    renderer.line(center.x + MathUtils.cos(a) * r, center.y, center.z - MathUtils.sin(a) * r, center.x + MathUtils.cos(a2) * r, center.y, center.z - MathUtils.sin(a2) * r);
                    break;
                case AXIS_Z:
                    renderer.line(center.x + MathUtils.cos(a) * r, center.y + MathUtils.sin(a) * r, center.z, center.x + MathUtils.cos(a2) * r, center.y + MathUtils.sin(a2) * r, center.z);
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
}
