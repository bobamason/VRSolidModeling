package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.Assets;

import java.io.InputStream;

/**
 * Created by Bob on 1/2/2018.
 */
public class Cube extends AssetPrimitive {

    private final BoundingBox boundingBox = new BoundingBox();

    public Cube() {
        super(Primitives.KEY_CUBE, Assets.SHAPE_CUBE, null);
    }

    @Override
    public void initialize(@NonNull InputStream meshStream, @Nullable InputStream hullStream) {
        super.initialize(meshStream, null);
        boundingBox.set(createBounds());
    }

    @Override
    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        return Intersector.intersectRayBounds(ray, boundingBox, hitPoint);
    }
}