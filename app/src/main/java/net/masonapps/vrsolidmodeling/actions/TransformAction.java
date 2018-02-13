package net.masonapps.vrsolidmodeling.actions;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class TransformAction extends Action {

    private final Transform oldTransform;
    private final Transform newTransform;

    public TransformAction(EditableNode node, Transform oldTransform, Transform newTransform) {
        super(node);
        this.oldTransform = oldTransform;
        this.newTransform = newTransform;
    }

    @Override
    public void redoAction() {
        getNode().setTransform(newTransform);
    }

    @Override
    public void undoAction() {
        getNode().setTransform(oldTransform);
    }

    public static class Transform implements Pool.Poolable {
        public Vector3 position = new Vector3();
        public Quaternion rotation = new Quaternion();
        public Vector3 scale = new Vector3(1, 1, 1);

        @Override
        public void reset() {
            position.set(0, 0, 0);
            rotation.idt();
            scale.set(1, 1, 1);
        }
    }
}
