package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;
import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;
import net.masonapps.vrsolidmodeling.ui.ShapeRenderableInput;
import net.masonapps.vrsolidmodeling.ui.ShapeRendererUtil;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class MultiNodeSelector extends ModelingInputProcessor implements ShapeRenderableInput {

    private final OnSelectionChangedListener listener;
    private List<EditableNode> selectedNodes = new ArrayList<>();
    @Nullable
    private AABBTree.AABBObject focusedObj = null;

    public MultiNodeSelector(ModelingProjectEntity modelingProject, OnSelectionChangedListener listener) {
        super(modelingProject);
        this.listener = listener;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        final boolean rayTest = super.performRayTest(ray);
        if (rayTest)
            focusedObj = intersectionInfo.object;
        else
            focusedObj = null;
        return rayTest;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver() && intersectionInfo.object instanceof EditableNode) {
            final EditableNode node = (EditableNode) intersectionInfo.object;
            if (selectedNodes.contains(node))
                selectedNodes.remove(node);
            else
                selectedNodes.add(node);
            listener.selectionChanged(selectedNodes);

            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return !selectedNodes.isEmpty();
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return !selectedNodes.isEmpty();
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return !selectedNodes.isEmpty();
    }

    @Override
    public boolean onBackButtonClicked() {
        // FIXME: 6/28/2018 notify listener that selection is complete
        return false;
    }

    @Override
    public void draw(ShapeRenderer renderer) {
        renderer.setTransformMatrix(modelingProject.getTransform());
        if (focusedObj != null)
            ShapeRendererUtil.drawNodeBounds(renderer, focusedObj, Color.BLACK);
        for (EditableNode node : selectedNodes) {
            ShapeRendererUtil.drawNodeBounds(renderer, node, Color.WHITE);
        }
    }

    public List<EditableNode> getSelectedNodes() {
        return selectedNodes;
    }

    public interface OnSelectionChangedListener {
        void selectionChanged(List<EditableNode> nodes);
    }
}
