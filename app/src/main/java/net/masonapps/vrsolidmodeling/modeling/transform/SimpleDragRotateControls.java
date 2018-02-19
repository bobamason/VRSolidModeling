package net.masonapps.vrsolidmodeling.modeling.transform;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.actions.TransformAction;
import net.masonapps.vrsolidmodeling.math.RotationUtil;
import net.masonapps.vrsolidmodeling.modeling.EditableNode;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.utils.Logger;
import org.masonapps.libgdxgooglevr.vr.ArmModel;

/**
 * Created by Bob Mason on 2/19/2018.
 */

public class SimpleDragRotateControls {

    @Nullable
    private EditableNode node = null;
    private Ray ray = new Ray();
    private Plane plane = new Plane();
    private TransformAction.Transform startTransform = new TransformAction.Transform();
    ;
    private Vector3 hitPoint1 = new Vector3();
    private Vector3 hitPoint2 = new Vector3();
    private Vector3 tmp = new Vector3();
    private Vector3 offset = new Vector3();

    public void begin(EditableNode node, Vector3 hitPoint) {
        this.node = node;
        final ArmModel armModel = GdxVr.input.getArmModel();
        node.getTransform(startTransform);
        ray.set(GdxVr.input.getInputRay());
        final Vector3 point = node.getPosition();
        offset.set(hitPoint).sub(point);
        plane.set(point.x, point.y, point.z, -ray.direction.x, -ray.direction.y, -ray.direction.z);
        RotationUtil.setToClosestUnitVector(plane.normal);

        Intersector.intersectRayPlane(ray, plane, hitPoint1);
    }
    
    public boolean update(Vector3 outHitPoint) {
        if (node != null) {
            final ArmModel armModel = GdxVr.input.getArmModel();
            ray.set(GdxVr.input.getInputRay());
            float angle = armModel.pointerRotation.getAngleAround(plane.normal);
            Logger.d("angle = " + angle);
            node.calculateTransforms(false);
            return true;
        }
        return false;
    }

    public void end() {
        node = null;
    }
}
