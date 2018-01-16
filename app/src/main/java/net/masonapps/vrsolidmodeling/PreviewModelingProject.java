package net.masonapps.vrsolidmodeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.vrsolidmodeling.modeling.BaseModelingProject;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.ModelingObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Bob Mason on 1/16/2018.
 */

public class PreviewModelingProject extends BaseModelingProject {

    private BoundingBox bounds = new BoundingBox();
    private float radius;

    public PreviewModelingProject(List<ModelingObject> modelingObjects, HashMap<String, Model> modelMap) {
        super();
        bounds.inf();
        final BoundingBox tmpBB = new BoundingBox();
        for (ModelingObject modelingObject : modelingObjects) {
            final ModelingEntity modelingEntity = new ModelingEntity(modelingObject, modelingObject.createModelInstance(modelMap));
            add(modelingEntity);
            bounds.ext(modelingEntity.getTransformedBounds(tmpBB));
        }
        final Vector3 dimens = new Vector3();
        bounds.getDimensions(dimens);
        radius = dimens.len() / 2f;
    }

    @Nullable
    @Override
    public ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final Ray tmpRay = Pools.obtain(Ray.class);
        final Matrix4 tmpMat = Pools.obtain(Matrix4.class);
        final BoundingBox tmpBounds = Pools.obtain(BoundingBox.class);

        tmpRay.set(ray).mul(inverseTransform);
        bounds.inf();
        for (ModelingEntity modelingEntity : entities) {
            bounds.ext(modelingEntity.getTransformedBounds(tmpBounds));
        }
        ModelingEntity result = null;
        if (Intersector.intersectRayBounds(ray, bounds, hitPoint)) {
            result = entities.get(0);
        }
        Pools.free(tmpRay);
        Pools.free(tmpMat);
        Pools.free(tmpBounds);
        return result;
    }

    public float getRadius() {
        return radius;
    }
}
