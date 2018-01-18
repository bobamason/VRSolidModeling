package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public abstract class Input3D extends Entity implements VrInputProcessor {

    protected final Axis axis;
    private final Vector3 hitPoint = new Vector3();
    private boolean isCursorOver = false;

    public Input3D(@Nullable ModelInstance modelInstance, BoundingBox bounds, Axis axis) {
        super(modelInstance, bounds);
        this.axis = axis;
    }

    @Override
    public boolean intersectsRayBoundsFast(Ray ray) {
        isCursorOver = super.intersectsRayBounds(ray, hitPoint);
        return isCursorOver;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        return intersectsRayBoundsFast(ray);
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

    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint;
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
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public enum Axis {
        AXIS_X, AXIS_Y, AXIS_Z
    }
}
