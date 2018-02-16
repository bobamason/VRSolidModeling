package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.List;

/**
 * Created by Bob Mason on 1/16/2018.
 */

public class PreviewModelingProject extends ModelingProject2 {

    private final float radius;
    private Ray tmpRay = new Ray();

    public PreviewModelingProject(List<EditableNode> nodes) {
        super(nodes);
        Vector3 dimens = new Vector3();
        radius = getBounds().getDimensions(dimens).len() / 2f;
    }

    @Nullable
    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        validate();
        tmpRay.set(ray).mul(inverseTransform);
        ray.direction.nor();
        final boolean intersects = Intersector.intersectRaySphere(ray, Vector3.Zero, radius, intersection.hitPoint);
        if (intersects) {
            intersection.hitPoint.mul(transform);
            intersection.t = ray.origin.dst2(intersection.hitPoint);
        }
        return intersects;
    }
}
