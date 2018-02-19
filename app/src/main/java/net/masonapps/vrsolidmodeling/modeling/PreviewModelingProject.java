package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;

import java.util.List;

/**
 * Created by Bob Mason on 1/16/2018.
 */

public class PreviewModelingProject extends ModelingProject2 {

    private Ray tmpRay = new Ray();

    public PreviewModelingProject(List<EditableNode> nodes) {
        super(nodes);
    }

    @Nullable
    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        validate();
        tmpRay.set(ray).mul(inverseTransform);
        ray.direction.nor();
        final boolean intersects = Intersector.intersectRaySphere(ray, Vector3.Zero, getRadius(), intersection.hitPoint);
        if (intersects) {
            intersection.hitPoint.mul(transform);
            intersection.t = ray.origin.dst2(intersection.hitPoint);
        }
        return intersects;
    }
}
