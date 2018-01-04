package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.World;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public class ModelingWorld extends World {

    public Matrix4 transform = new Matrix4();

    public ModelingWorld() {
        super();
    }

    @Override
    public void render(ModelBatch batch, Environment lights, Entity entity) {
        if (entity instanceof ModelingEntity)
            ((ModelingEntity) entity).setParentTransform(transform);
        super.render(batch, lights, entity);
    }

    @Nullable
    public ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        for (Entity entity : entities) {
            if (entity instanceof ModelingEntity) {
                final ModelingEntity modelingEntity = (ModelingEntity) entity;
                if (modelingEntity.rayTest(ray, hitPoint))
                    return modelingEntity;
            }
        }
        return null;
    }
}
