package net.masonapps.vrsolidmodeling.actions;

import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.ModelingProject;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class RemoveAction extends Action {

    private final ModelingProject project;

    public RemoveAction(ModelingEntity entity, ModelingProject project) {
        super(entity);
        this.project = project;
    }

    @Override
    public void redoAction() {
        project.remove(getEntity());
    }

    @Override
    public void undoAction() {
        project.add(getEntity());
    }
}
