package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;

/**
 * Created by Bob on 1/2/2018.
 */
public class Cube extends Primitive {
    @Override
    public void initialize() {

    }

    @Override
    public Model createModel() {
        return null;
    }

    @Override
    public BoundingBox createBounds() {
        return null;
    }

    @Override
    public PolyhedronsSet toPolyhedronsSet(Matrix4 transform) {
        return null;
    }

    @Override
    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        return false;
    }
}