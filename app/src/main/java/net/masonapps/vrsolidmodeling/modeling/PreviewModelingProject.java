package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

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
        if (modelingObjects.isEmpty()) {
            bounds.set(new Vector3(-1f, -1f, -1f), new Vector3(1f, 1f, 1f));
        } else {
            bounds.inf();
            final BoundingBox tmpBB = new BoundingBox();
            for (ModelingObject modelingObject : modelingObjects) {
                final ModelingEntity modelingEntity = new ModelingEntity(modelingObject, modelingObject.createModelInstance(modelMap));
                add(modelingEntity);
                bounds.ext(modelingEntity.getTransformedBounds(tmpBB));
            }
        }
        final Vector3 dimens = new Vector3();
        bounds.getDimensions(dimens);
        radius = dimens.len() / 2f;
    }

    @Nullable
    @Override
    public ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final Vector3 tmpV = Pools.obtain(Vector3.class);
        ModelingEntity result = null;
        if (Intersector.intersectRaySphere(ray, bounds.getCenter(tmpV), radius, hitPoint)) {
            result = entities.get(0);
        }
        Pools.free(tmpV);
        return result;
    }

    public float getRadius() {
        return radius;
    }
}
