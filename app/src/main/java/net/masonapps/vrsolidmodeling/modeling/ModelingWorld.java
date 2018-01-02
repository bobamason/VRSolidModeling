package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.World;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public class ModelingWorld extends World {

    public ModelingWorld() {
        super();
    }

    @Nullable
    public Primitive rayTest(Ray ray, Vector3 hitPoint) {
        return null;
    }
}
