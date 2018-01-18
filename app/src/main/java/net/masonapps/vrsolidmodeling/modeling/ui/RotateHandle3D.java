package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class RotateHandle3D extends Input3D {
    public RotateHandle3D(@Nullable ModelInstance modelInstance, BoundingBox bounds) {
        super(modelInstance, bounds);
    }
}
