package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import net.masonapps.vrsolidmodeling.bvh.BVH;
import net.masonapps.vrsolidmodeling.mesh.MeshInfo;

import java.io.InputStream;

/**
 * Created by Bob Mason on 1/2/2018.
 */

public abstract class Primitive implements Disposable {

    public Primitive() {
    }

    public abstract void initialize(@NonNull InputStream meshStream, @Nullable InputStream hullStream);

    public abstract MeshInfo getMeshInfo();

    public abstract BVH getBVH();

    public abstract String getName();

    public abstract BoundingBox createBounds();

    public abstract boolean rayTest(Ray ray, @Nullable Vector3 hitPoint);

    @Override
    public void dispose() {
    }
}
