package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;
import net.masonapps.vrsolidmodeling.ui.ShapeRenderableInput;
import net.masonapps.vrsolidmodeling.ui.ShapeRendererUtil;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class SingleNodeSelector extends ModelingInputProcessor implements ShapeRenderableInput {

    private final OnNodeSelectedListener listener;
    @Nullable
    private EditableNode selectedNode = null;

    public SingleNodeSelector(ModelingProjectEntity modelingProject, OnNodeSelectedListener listener) {
        super(modelingProject);
        this.listener = listener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver() && intersectionInfo.object instanceof EditableNode) {
            selectedNode = (EditableNode) intersectionInfo.object;
            listener.nodeSelected(selectedNode);
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

    @Override
    public boolean onBackButtonClicked() {
        return false;
    }

    @Override
    public void draw(ShapeRenderer renderer) {
        if (selectedNode == null) return;
        renderer.setTransformMatrix(modelingProject.getTransform());
        ShapeRendererUtil.drawNodeBounds(renderer, selectedNode, Color.WHITE);
    }

    public interface OnNodeSelectedListener {
        void nodeSelected(EditableNode node);
    }
}
