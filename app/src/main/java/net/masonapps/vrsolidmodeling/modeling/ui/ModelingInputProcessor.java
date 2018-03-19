package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.modeling.ModelingProjectEntity;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public abstract class ModelingInputProcessor implements VrInputProcessor {

    protected final ModelingProjectEntity modelingProject;
    protected final AABBTree.IntersectionInfo intersectionInfo = new AABBTree.IntersectionInfo();
    protected boolean isCursorOver = false;

    public ModelingInputProcessor(ModelingProjectEntity modelingProject) {
        this.modelingProject = modelingProject;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        isCursorOver = modelingProject.getAABBTree().rayTest(ray, intersectionInfo);
        return isCursorOver;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    @Nullable
    @Override
    public Vector2 getHitPoint2D() {
        return null;
    }

    @Nullable
    @Override
    public Vector3 getHitPoint3D() {
        return intersectionInfo.hitPoint;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
