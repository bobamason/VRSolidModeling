package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.Transformable;
import org.masonapps.libgdxgooglevr.gfx.World;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public class ModelingWorld extends World {

    public Transformable transformable;

    public ModelingWorld() {
        super();
        transformable = new Transformable() {
            @Override
            public void recalculateTransform() {
                super.recalculateTransform();
//                Logger.d(transform.toString());
                for (Entity entity : entities) {
                    if (entity instanceof ModelingEntity)
                        ((ModelingEntity) entity).setParentTransform(this.transform);
                }
            }

            @Override
            public Transformable setTransform(Matrix4 transform) {
                final Transformable transformable = super.setTransform(transform);
                for (Entity entity : entities) {
                    if (entity instanceof ModelingEntity)
                        ((ModelingEntity) entity).setParentTransform(this.transform);
                }
                return transformable;
            }
        };
    }

    @Override
    public Entity add(Entity entity) {
        if (entity instanceof ModelingEntity)
            ((ModelingEntity) entity).setParentTransform(transformable.getTransform(new Matrix4()));
        return super.add(entity);
    }

    @Override
    public void update() {
        if (!transformable.isUpdated())
            transformable.recalculateTransform();
        super.update();
    }

    @Nullable
    public ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final Ray tmpRay = Pools.obtain(Ray.class);
        final Matrix4 tmpMat = Pools.obtain(Matrix4.class);

        tmpRay.set(ray).mul(transformable.getInverseTransform(tmpMat));
        ModelingEntity hitEntity = null;
        for (Entity entity : entities) {
            if (entity instanceof ModelingEntity) {
                final ModelingEntity modelingEntity = (ModelingEntity) entity;
                if (modelingEntity.rayTest(tmpRay, hitPoint)) {
                    if (hitPoint != null) hitPoint.mul(transformable.getTransform(tmpMat));
                    hitEntity = modelingEntity;
//                    Logger.d("ray hit! " + hitPoint);
                    break;
                }
            }
        }
        Pools.free(tmpRay);
        Pools.free(tmpMat);
        return hitEntity;
    }
}
