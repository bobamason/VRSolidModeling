package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class AddNodeInput extends ModelingInputProcessor {

    private final OnNodeAddedListener listener;
    private final ModelInstance modelInstance;
    private final Material material;
    @Nullable
    private EditableNode previewNode = null;
    private float distance = 3f;

    public AddNodeInput(ModelingProjectEntity modelingProject, OnNodeAddedListener listener) {
        super(modelingProject);
        this.listener = listener;
        modelInstance = new ModelInstance(new Model());
        material = new Material(ColorAttribute.createDiffuse(Color.CYAN), new BlendingAttribute(true, 0.5f));
    }

    public void render(ModelBatch modelBatch) {
        if (isVisible() && previewNode != null)
            modelBatch.render(modelInstance);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        final boolean rayTest = super.performRayTest(ray);
        if (previewNode != null) {
//            Logger.d("hit = " + String.valueOf(rayTest) + " normal = " + intersectionInfo.normal);
            if (rayTest) {
                if (intersectionInfo.normal.isZero(0.001f))
                    intersectionInfo.normal.set(Vector3.Y);
                final float offset = previewNode.getBounds().getHeight() * previewNode.getScaleY() / 2f;
                previewNode.getPosition().set(intersectionInfo.normal).scl(offset).add(intersectionInfo.hitPoint);
                previewNode.getRotation().setFromCross(Vector3.Y, intersectionInfo.normal);
                previewNode.invalidate();
            } else {
                previewNode.getPosition().set(ray.direction).scl(distance).add(ray.origin);
                previewNode.getRotation().idt();
                previewNode.invalidate();
            }
        }
        return rayTest;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (previewNode != null) {
            final EditableNode copy = previewNode.copy();
            previewNode.getPosition().mul(modelingProject.getInverseTransform());
            previewNode.getRotation().mul(new Quaternion(modelingProject.getRotation()).conjugate());
            previewNode.invalidate();
            modelingProject.add(copy);
            listener.nodeAdded(copy);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return isVisible() && previewNode != null;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return isVisible() && previewNode != null;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return isVisible() && previewNode != null;
    }

    public void setPreviewNode(@Nullable EditableNode previewNode) {
        this.previewNode = previewNode;
        if (previewNode != null) {
            modelInstance.nodes.clear();
            modelInstance.model.nodes.clear();

            modelInstance.nodes.add(previewNode);
            modelInstance.model.nodes.add(previewNode);

            final NodePart nodePart = previewNode.parts.get(0);

            modelInstance.model.meshParts.clear();
            modelInstance.model.meshes.clear();

            modelInstance.model.meshParts.add(nodePart.meshPart);
            modelInstance.model.meshes.add(nodePart.meshPart.mesh);

            modelInstance.materials.clear();
            modelInstance.model.materials.clear();

            modelInstance.materials.add(material);
            modelInstance.model.materials.add(material);
        }
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public interface OnNodeAddedListener {
        void nodeAdded(EditableNode node);
    }
}
