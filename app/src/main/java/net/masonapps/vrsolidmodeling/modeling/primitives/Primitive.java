package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import java.io.InputStream;

/**
 * Created by Bob Mason on 1/2/2018.
 */

public abstract class Primitive implements Disposable {

    public Primitive() {
    }

    public abstract void initialize(InputStream inputStream);

    public abstract Mesh createMesh();

    public abstract String getName();

    public abstract BoundingBox createBounds();

    public abstract boolean rayTest(Ray ray, @Nullable Vector3 hitPoint);

    @Override
    public void dispose() {
    }
}
