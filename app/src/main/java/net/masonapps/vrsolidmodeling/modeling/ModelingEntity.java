package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;

/**
 * Created by Bob Mason on 1/2/2018.
 */

public class ModelingEntity implements AABBTree.AABBObject {

    public final ModelingObject modelingObject;
    public final ModelInstance modelInstance;
    private final BoundingBox boundingBox;
    @Nullable
    private AABBTree.LeafNode node = null;
    private Matrix4 parentTransform = new Matrix4();
    private BoundingBox aabb = new BoundingBox();


    public ModelingEntity(ModelingObject modelingObject, ModelInstance modelInstance) {
        this.modelingObject = modelingObject;
        this.modelInstance = modelInstance;
        boundingBox = modelingObject.getPrimitive().createBounds();
        aabb.set(boundingBox).mul(modelingObject.getTransform());
        modelingObject.setOnTransformChangedListener(() -> {
            aabb.set(boundingBox).mul(modelingObject.getTransform());
            modelingObject.getTransform(modelInstance.transform).mulLeft(parentTransform);
            if (node != null)
                node.refit();
        });
    }

    @Nullable
    public AABBTree.LeafNode getNode() {
        return node;
    }

    public void setNode(@Nullable AABBTree.LeafNode node) {
        this.node = node;
    }

    @Override
    public BoundingBox getAABB() {
        return aabb;
    }

    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        if (!modelingObject.isUpdated()) modelingObject.recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(modelingObject.getInverseTransform());
        tmpRay.direction.nor();
        final boolean rayTest = modelingObject.getPrimitive().rayTest(tmpRay, intersection.hitPoint);
        if (rayTest) {
            intersection.object = this;
            intersection.hitPoint.mul(modelingObject.getTransform());
            intersection.t = ray.origin.dst(intersection.hitPoint);
        }

        Pools.free(tmpRay);
        return rayTest;
    }

    public PolyhedronsSet toPolyhedronsSet() {
        return modelingObject.getPrimitive().toPolyhedronsSet(modelingObject.getTransform(new Matrix4()));
    }

    public Color getDiffuseColor() {
        return modelingObject.diffuseColor;
    }

    public void setDiffuseColor(Color color) {
        final Material material = modelInstance.materials.get(0);
        final ColorAttribute diffuse = (ColorAttribute) material.get(ColorAttribute.Diffuse);
        if (diffuse != null)
            diffuse.color.set(color);
        modelingObject.diffuseColor.set(color);
    }

    public void setAmbientColor(Color color) {
        final Material material = modelInstance.materials.get(0);
        final ColorAttribute ambient = (ColorAttribute) material.get(ColorAttribute.Ambient);
        if (ambient != null)
            ambient.color.set(color);
        modelingObject.ambientColor.set(color);
    }

    public void setSpecularColor(Color color) {
        final Material material = modelInstance.materials.get(0);
        final ColorAttribute specular = (ColorAttribute) material.get(ColorAttribute.Specular);
        if (specular != null)
            specular.color.set(color);
        modelingObject.specularColor.set(color);
    }

    public void setShininess(float value) {
        final Material material = modelInstance.materials.get(0);
        final FloatAttribute shininess = (FloatAttribute) material.get(FloatAttribute.Shininess);
        if (shininess != null)
            shininess.value = value;
        modelingObject.shininess = value;
    }

    public ModelingEntity copy() {
        final ModelingEntity modelingEntity = new ModelingEntity(this.modelingObject.copy(), this.modelInstance.copy());
        modelingEntity.setParentTransform(this.parentTransform);
        return modelingEntity;
    }

    public Matrix4 getParentTransform() {
        return parentTransform;
    }

    public void setParentTransform(Matrix4 parentTransform) {
        this.parentTransform.set(parentTransform);
        modelingObject.getTransform(modelInstance.transform).mulLeft(parentTransform);
    }

    public void update() {
        modelingObject.validate();
    }

    public BoundingBox getBounds() {
        return boundingBox;
    }
}
