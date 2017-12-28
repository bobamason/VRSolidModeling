package net.masonapps.vrsolidmodeling.modeling;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;

import org.masonapps.libgdxgooglevr.gfx.Entity;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public abstract class ModelingEntity extends Entity {

    public ModelingEntity() {
        super(null);
        modelInstance = createModelInstance();
        bounds.set(createBounds());
        updateDimensions();
    }

    protected abstract ModelInstance createModelInstance();

    protected abstract BoundingBox createBounds();
}
