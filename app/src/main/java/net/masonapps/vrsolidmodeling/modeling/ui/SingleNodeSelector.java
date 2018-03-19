package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class SingleNodeSelector extends ModelingInputProcessor {

    @Nullable
    private EditableNode selectedNode = null;

    public SingleNodeSelector(ModelingProjectEntity modelingProject) {
        super(modelingProject);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver() && intersectionInfo.object instanceof EditableNode) {
            selectedNode = (EditableNode) intersectionInfo.object;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return selectedNode != null;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return selectedNode != null;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return selectedNode != null;
    }

    @Nullable
    public EditableNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(@Nullable EditableNode selectedNode) {
        this.selectedNode = selectedNode;
    }
}
