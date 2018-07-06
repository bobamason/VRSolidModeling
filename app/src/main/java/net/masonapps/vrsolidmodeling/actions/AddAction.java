package net.masonapps.vrsolidmodeling.actions;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class AddAction extends Action {

    private final ModelingProjectEntity project;

    public AddAction(EditableNode node, ModelingProjectEntity project) {
        super(node);
        this.project = project;
    }

    @Override
    public void redoAction() {
        project.add(getNode());
    }

    @Override
    public void undoAction() {
        project.remove(getNode());
    }
}
