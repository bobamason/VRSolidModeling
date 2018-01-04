package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.masonapps.libgdxgooglevr.gfx.Entity;

/**
 * Created by Bob Mason on 1/2/2018.
 */

public class ModelingEntity extends Entity {

    private final Primitive primitive;
    @Nullable
    private AABBTree.Node node = null;
    private Matrix4 parentTransform = new Matrix4();

    public ModelingEntity(Primitive primitive, Material material) {
        this(primitive, material, new Matrix4());
    }

    public ModelingEntity(Primitive primitive, Material material, Vector3 position) {
        this(primitive, material, new Matrix4().setToTranslation(position));
    }

    public ModelingEntity(Primitive primitive, Material material, float x, float y, float z) {
        this(primitive, material, new Matrix4().setToTranslation(x, y, z));
    }

    public ModelingEntity(Primitive primitive, Material material, Matrix4 transform) {
        super(primitive.createModelInstance(transform, material), primitive.createBounds());
        this.primitive = primitive;
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        if (modelInstance != null)
            modelInstance.transform.mulLeft(parentTransform);
    }

    @Override
    public Entity setTransform(Matrix4 transform) {
        super.setTransform(transform);
        if (modelInstance != null)
            modelInstance.transform.mulLeft(parentTransform);
        return this;
    }

    @Nullable
    public AABBTree.Node getNode() {
        return node;
    }

    public void setNode(@Nullable AABBTree.Node node) {
        this.node = node;
    }

    public BoundingBox getTransformedBounds(BoundingBox out) {
        return out.set(getBounds()).mul(transform);
    }

    public PolyhedronsSet toPolyhedronsSet() {
        return primitive.toPolyhedronsSet(getTransform(new Matrix4()));
    }

    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRayBounds = primitive.rayTest(tmpRay, hitPoint);
        if (intersectRayBounds && hitPoint != null) hitPoint.mul(transform);

        Pools.free(tmpRay);
        return intersectRayBounds;
    }

    public void setParentTransform(Matrix4 parentTransform) {
        this.parentTransform.set(parentTransform);
    }
}
