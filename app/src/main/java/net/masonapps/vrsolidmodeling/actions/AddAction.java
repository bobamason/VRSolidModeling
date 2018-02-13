package net.masonapps.vrsolidmodeling.actions;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProject2;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class AddAction extends Action {

    private final ModelingProject2 project;

    public AddAction(EditableNode entity, ModelingProject2 project) {
        super(entity);
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
