package net.masonapps.vrsolidmodeling.actions;

import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.ModelingProject;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class AddAction extends Action {

    private final ModelingProject project;

    public AddAction(ModelingEntity entity, ModelingProject project) {
        super(entity);
        this.project = project;
    }

    @Override
    public void redoAction() {
        project.add(getEntity());
    }

    @Override
    public void undoAction() {
        project.remove(getEntity());
    }
}
