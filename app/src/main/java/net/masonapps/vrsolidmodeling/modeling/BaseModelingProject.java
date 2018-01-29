package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import org.masonapps.libgdxgooglevr.gfx.Transformable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public abstract class BaseModelingProject extends Transformable implements Disposable {
    protected List<ModelingEntity> entities = new ArrayList<>();
    private ModelCache modelCache;

    public BaseModelingProject() {
        super();
        modelCache = new ModelCache();
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        entities.forEach(entity -> entity.setParentTransform(this.transform));
    }

    @Override
    public Transformable setTransform(Matrix4 transform) {
        super.setTransform(transform);
        entities.forEach(entity -> entity.setParentTransform(this.transform));
        return this;
    }

    public void add(ModelingEntity entity) {
        entities.add(entity);
        entity.setParentTransform(this.transform);
    }

    public void update() {
        if (!updated)
            recalculateTransform();
        modelCache.begin();
        for (ModelingEntity entity : entities) {
            entity.update();
            modelCache.add(entity.modelInstance);
        }
        modelCache.end();
    }

    public void render(ModelBatch batch, Environment environment) {
        if (!updated)
            recalculateTransform();
        batch.render(modelCache, environment);
    }

    @Nullable
    public abstract ModelingEntity rayTest(Ray ray, @Nullable Vector3 hitPoint);

    @Override
    public void dispose() {
        if (modelCache != null) {
            modelCache.dispose();
            modelCache = null;
        }
    }

    public List<ModelingObject> getModelingObjectList() {
        final ArrayList<ModelingObject> list = new ArrayList<>();
        for (ModelingEntity entity : entities) {
            list.add(entity.modelingObject);
        }
        return list;
    }
}
