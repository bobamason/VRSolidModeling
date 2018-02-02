package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob Mason on 1/16/2018.
 */

public class ModelingProject extends BaseModelingProject {
    private AABBTree.IntersectionInfo intersection = new AABBTree.IntersectionInfo();
    private AABBTree aabbTree;

    public ModelingProject() {
        aabbTree = new AABBTree();
    }

    @Override
    public void add(ModelingEntity entity) {
        super.add(entity);
        aabbTree.insert(entity);
    }

    @Override
    public void remove(ModelingEntity entity) {
        super.remove(entity);
        aabbTree.remove(entity);
    }

    @Nullable
    @Override
    public ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final Ray tmpRay = Pools.obtain(Ray.class);
        final Matrix4 tmpMat = Pools.obtain(Matrix4.class);

        tmpRay.set(ray).mul(inverseTransform);
        ray.direction.nor();
        intersection.object = null;
        if (aabbTree.rayTest(tmpRay, intersection)) {
                if (hitPoint != null) hitPoint.set(intersection.hitPoint).mul(transform);
            }
        Pools.free(tmpRay);
        Pools.free(tmpMat);
        return (ModelingEntity) intersection.object;
    }

    public AABBTree getAABBTree() {
        return aabbTree;
    }
}
