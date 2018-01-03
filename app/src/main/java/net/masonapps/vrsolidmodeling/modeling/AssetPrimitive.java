package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.bvh.BVH;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 1/3/2018.
 */

public class AssetPrimitive extends Primitive {
    private BoundingBox boundingBox;
    private com.badlogic.gdx.graphics.g3d.model.data.ModelData modelData;
    private BVH bvh;
    private BVH.IntersectionInfo intersectionInfo;

    public AssetPrimitive() {
        intersectionInfo = new BVH.IntersectionInfo();
    }

    @Override
    public void initialize() {

    }

    @Override
    public Model createModel() {
        return new Model(modelData);
    }

    @Override
    public BoundingBox createBounds() {
        return new BoundingBox(boundingBox);
    }

    @Override
    public PolyhedronsSet toPolyhedronsSet(Matrix4 transform) {
        final List<Vector3D> vertices = new ArrayList<>();
        final List<int[]> facets = new ArrayList<>();
        return new PolyhedronsSet(vertices, facets, 1e-10);
    }

    @Override
    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final boolean intersection = bvh.closestIntersection(ray, intersectionInfo);
        if (intersection && hitPoint != null)
            hitPoint.set(intersectionInfo.hitPoint);
        return intersection;
    }
}
