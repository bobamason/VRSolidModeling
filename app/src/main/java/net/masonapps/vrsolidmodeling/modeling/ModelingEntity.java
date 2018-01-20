package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob Mason on 1/2/2018.
 */

public class ModelingEntity {

    public final ModelingObject modelingObject;
    public final ModelInstance modelInstance;
    private final BoundingBox boundingBox;
    @Nullable
    private AABBTree.Node node = null;
    private Matrix4 parentTransform = new Matrix4();


    public ModelingEntity(ModelingObject modelingObject, ModelInstance modelInstance) {
        this.modelingObject = modelingObject;
        this.modelInstance = modelInstance;
        boundingBox = modelingObject.getPrimitive().createBounds();
    }

    @Nullable
    public AABBTree.Node getNode() {
        return node;
    }

    public void setNode(@Nullable AABBTree.Node node) {
        this.node = node;
    }

    public BoundingBox getTransformedBounds(BoundingBox out) {
        return out.set(boundingBox).mul(modelingObject.getTransform());
    }

    public PolyhedronsSet toPolyhedronsSet() {
        return modelingObject.getPrimitive().toPolyhedronsSet(modelingObject.getTransform(new Matrix4()));
    }

    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        if (!modelingObject.isUpdated()) modelingObject.recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(modelingObject.getInverseTransform());
        final boolean intersectRayBounds = modelingObject.getPrimitive().rayTest(tmpRay, hitPoint);
        if (intersectRayBounds && hitPoint != null) hitPoint.mul(modelingObject.getTransform());

        Pools.free(tmpRay);
        return intersectRayBounds;
    }

    @Nullable
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

    public Matrix4 getParentTransform() {
        return parentTransform;
    }

    public void setParentTransform(Matrix4 parentTransform) {
        this.parentTransform.set(parentTransform);
        modelInstance.transform.set(modelingObject.getTransform());
        modelInstance.transform.mulLeft(parentTransform);

        Logger.d("parent" + parentTransform.toString());
        Logger.d("object" + modelingObject.getTransform().toString());
        Logger.d("modelinstance" + modelInstance.transform.toString());
    }

    public void update() {
        if (!modelingObject.isUpdated()) {
            modelingObject.recalculateTransform();
            modelInstance.transform.set(modelingObject.getTransform());
            modelInstance.transform.mulLeft(parentTransform);
        }
    }

    public BoundingBox getBounds() {
        return boundingBox;
    }
}
