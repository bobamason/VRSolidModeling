package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;

/**
 * Created by Bob Mason on 1/2/2018.
 */

public abstract class Primitive implements Disposable {

    public Primitive() {
    }

    public abstract void initialize();

    public abstract Model createModel();

    public abstract String getName();

    public abstract BoundingBox createBounds();

    public abstract PolyhedronsSet toPolyhedronsSet(Matrix4 transform);

    public abstract boolean rayTest(Ray ray, @Nullable Vector3 hitPoint);

    @Override
    public void dispose() {
    }
}
