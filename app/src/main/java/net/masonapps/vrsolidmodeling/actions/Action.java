package net.masonapps.vrsolidmodeling.actions;

import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public abstract class Action {

    private final ModelingEntity entity;

    public Action(ModelingEntity entity) {
        this.entity = entity;
    }

    public abstract void redoAction();

    public abstract void undoAction();

    public ModelingEntity getEntity() {
        return entity;
    }
}
