package net.masonapps.vrsolidmodeling.actions;

import net.masonapps.vrsolidmodeling.modeling.AABBTree;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class AddAction extends Action {

    private final AABBTree tree;

    public AddAction(ModelingEntity entity, AABBTree tree) {
        super(entity);
        this.tree = tree;
    }

    @Override
    public void redoAction() {
        tree.insert(getEntity());
    }

    @Override
    public void undoAction() {
        tree.remove(getEntity());
    }
}
