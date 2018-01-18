package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by Bob Mason on 1/18/2018.
 */

public class DragHandle3D extends Input3D {

    public DragHandle3D(@Nullable ModelInstance modelInstance, BoundingBox bounds, Axis axis) {
        super(modelInstance, bounds, axis);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }
}
