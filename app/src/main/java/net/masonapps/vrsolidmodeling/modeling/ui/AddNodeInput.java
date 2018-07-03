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
import com.badlogic.gdx.math.collision.Ray;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;
import net.masonapps.vrsolidmodeling.ui.RenderableInput;

import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class AddNodeInput extends ModelingInputProcessor implements RenderableInput, DaydreamControllerInputListener {

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
        material = new Material(ColorAttribute.createDiffuse(Color.YELLOW), new BlendingAttribute(true, 0.25f));
    }

    @Override
    public void update() {

    }

    @Override
    public void render(ModelBatch modelBatch) {
        if (isVisible() && previewNode != null)
            modelBatch.render(modelInstance);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        final boolean rayTest = super.performRayTest(ray);
        if (previewNode != null) {
            if (rayTest) {
                previewNode.getPosition().set(intersectionInfo.hitPoint);
                previewNode.getRotation().idt();
                previewNode.invalidate();
            } else {
                previewNode.getPosition().set(ray.direction).scl(distance).add(ray.origin);
                intersectionInfo.hitPoint.set(previewNode.getPosition());
                previewNode.getRotation().idt();
                previewNode.invalidate();
            }
            isCursorOver = true;
            return true;
        }
        return rayTest;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return isVisible() && previewNode != null;
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
        modelInstance.nodes.clear();
        modelInstance.model.nodes.clear();

        modelInstance.model.meshParts.clear();
        modelInstance.model.meshes.clear();

        modelInstance.materials.clear();
        modelInstance.model.materials.clear();

        if (previewNode != null) {
            modelInstance.nodes.add(previewNode);
            modelInstance.model.nodes.add(previewNode);

            final NodePart nodePart = previewNode.parts.get(0);

            modelInstance.model.meshParts.add(nodePart.meshPart);
            modelInstance.model.meshes.add(nodePart.meshPart.mesh);

            modelInstance.materials.add(material);
            modelInstance.model.materials.add(material);
        }
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {

    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
        if (isVisible() && event.button == DaydreamButtonEvent.BUTTON_TOUCHPAD) {
            switch (event.action) {
                case DaydreamButtonEvent.ACTION_DOWN:
                    if (previewNode != null) {
                        final EditableNode copy = previewNode.copy();

                        copy.getPosition().mul(modelingProject.getInverseTransform());
                        copy.getRotation().mul(new Quaternion(modelingProject.getRotation()).conjugate());
                        copy.invalidate();
                        modelingProject.add(copy);
                        listener.nodeAdded(copy);
                    }
                    break;
            }
        }
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {

    }

    @Override
    public boolean onBackButtonClicked() {
        return false;
    }

    public interface OnNodeAddedListener {
        void nodeAdded(EditableNode node);
    }
}
