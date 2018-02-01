package net.masonapps.vrsolidmodeling.actions;

import com.badlogic.gdx.math.Matrix4;

import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class TransformAction extends Action {

    private final Matrix4 oldTransform;
    private final Matrix4 newTransform;

    public TransformAction(ModelingEntity entity, Matrix4 oldTransform, Matrix4 newTransform) {
        super(entity);
        this.oldTransform = oldTransform;
        this.newTransform = newTransform;
    }

    @Override
    public void redoAction() {
        getEntity().modelingObject.setTransform(newTransform);
    }

    @Override
    public void undoAction() {
        getEntity().modelingObject.setTransform(oldTransform);
    }
}
