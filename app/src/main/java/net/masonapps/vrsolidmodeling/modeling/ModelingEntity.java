package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
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
            modelInstance.transform.mulLeft(parentTransform);
    }

    @Override
    public Entity setTransform(Matrix4 transform) {
        super.setTransform(transform);
        if (parentTransform != null)
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

    @Nullable
    public Color getDiffuseColor() {
        final Material material = modelInstance.materials.get(0);
        final ColorAttribute diffuse = (ColorAttribute) material.get(ColorAttribute.Diffuse);
        if (diffuse != null)
            return diffuse.color;
        else
            return null;
    }

    public void setDiffuseColor(Color color) {
        final Material material = modelInstance.materials.get(0);
        final ColorAttribute diffuse = (ColorAttribute) material.get(ColorAttribute.Diffuse);
        if (diffuse != null)
            diffuse.color.set(color);
        else
            material.set(ColorAttribute.createDiffuse(color));
    }

    public void setSpecularColor(Color color) {
        final Material material = modelInstance.materials.get(0);
        final ColorAttribute specular = (ColorAttribute) material.get(ColorAttribute.Specular);
        if (specular != null)
            specular.color.set(color);
        else
            material.set(ColorAttribute.createSpecular(color));
    }

    public void setShininess(float value) {
        final Material material = modelInstance.materials.get(0);
        final FloatAttribute shininess = (FloatAttribute) material.get(FloatAttribute.Shininess);
        if (shininess != null)
            shininess.value = value;
        else
            material.set(FloatAttribute.createShininess(value));
    }

    public Matrix4 getParentTransform() {
        return parentTransform;
    }

    public void setParentTransform(Matrix4 parentTransform) {
        this.parentTransform.set(parentTransform);
        modelInstance.transform.set(transform).mulLeft(parentTransform);
//        Logger.d(modelInstance.transform.toString());
    }
}
