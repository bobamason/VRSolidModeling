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
    @Nullable
    @Override
    public ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final Ray tmpRay = Pools.obtain(Ray.class);
        final Matrix4 tmpMat = Pools.obtain(Matrix4.class);

        tmpRay.set(ray).mul(inverseTransform);
        ModelingEntity hitEntity = null;
        for (ModelingEntity modelingEntity : entities) {
            if (modelingEntity.rayTest(tmpRay, hitPoint)) {
                if (hitPoint != null) hitPoint.mul(transform);
                hitEntity = modelingEntity;
                break;
            }
        }
        Pools.free(tmpRay);
        Pools.free(tmpMat);
        return hitEntity;
    }
}
