package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.Assets;

/**
 * Created by Bob on 1/2/2018.
 */

public class Sphere extends AssetPrimitive {

    private final float radius;

    public Sphere() {
        super(Primitives.KEY_SPHERE, Assets.SHAPE_SPHERE);
        final Vector3 dimens = new Vector3();
        bvh.root.bb.getDimensions(dimens);
        radius = Math.min(dimens.x, Math.min(dimens.y, dimens.z));
    }

    @Override
    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        return Intersector.intersectRaySphere(ray, Vector3.Zero, radius, hitPoint);
    }
}
