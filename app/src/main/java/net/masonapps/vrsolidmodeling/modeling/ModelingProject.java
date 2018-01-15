package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.Transformable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public class ModelingProject extends Transformable implements Disposable {
    private ModelCache modelCache;
    private List<ModelingEntity> entities = new ArrayList<>();

    public ModelingProject() {
        super();
        modelCache = new ModelCache();
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        for (Entity entity : entities) {
            if (entity instanceof ModelingEntity)
                ((ModelingEntity) entity).setParentTransform(this.transform);
        }
    }

    @Override
    public Transformable setTransform(Matrix4 transform) {
        super.setTransform(transform);
        for (Entity entity : entities) {
            if (entity instanceof ModelingEntity)
                ((ModelingEntity) entity).setParentTransform(this.transform);
        }
        return this;
    }

    public void add(ModelingEntity entity) {
        entities.add(entity);
        entity.setParentTransform(getTransform(new Matrix4()));
    }

    public void update() {
        if (!isUpdated())
            recalculateTransform();
        modelCache.begin();
        for (Entity entity : entities) {
            if (!entity.isUpdated())
                entity.recalculateTransform();
            modelCache.add(entity.modelInstance);
        }
        modelCache.end();
    }

    public void render(ModelBatch batch, Environment environment) {
        batch.render(modelCache, environment);
    }

    @Nullable
    public ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final Ray tmpRay = Pools.obtain(Ray.class);
        final Matrix4 tmpMat = Pools.obtain(Matrix4.class);

        tmpRay.set(ray).mul(inverseTransform);
        ModelingEntity hitEntity = null;
        for (Entity entity : entities) {
            if (entity instanceof ModelingEntity) {
                final ModelingEntity modelingEntity = (ModelingEntity) entity;
                if (modelingEntity.rayTest(tmpRay, hitPoint)) {
                    if (hitPoint != null) hitPoint.mul(transform);
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

    @Override
    public void dispose() {
        if (modelCache != null) {
            modelCache.dispose();
            modelCache = null;
        }
    }

    public List<ModelingEntity> getModelingEntityList() {
        final ArrayList<ModelingEntity> list = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof ModelingEntity)
                list.add((ModelingEntity) entity);
        }
        return list;
    }
}
