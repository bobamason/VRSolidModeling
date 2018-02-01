package net.masonapps.vrsolidmodeling.actions;

import net.masonapps.vrsolidmodeling.modeling.AABBTree;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class RemoveAction extends Action {

    private final AABBTree tree;

    public RemoveAction(ModelingEntity entity, AABBTree tree) {
        super(entity);
        this.tree = tree;
    }

    @Override
    public void redoAction() {
        tree.remove(getEntity());
    }

    @Override
    public void undoAction() {
        tree.insert(getEntity());
    }
}
