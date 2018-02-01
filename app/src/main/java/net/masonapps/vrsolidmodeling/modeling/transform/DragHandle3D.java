package net.masonapps.vrsolidmodeling.modeling.transform;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.Transformable;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public abstract class DragHandle3D extends Entity implements VrInputProcessor {

    protected final Axis axis;
    private final Vector3 hitPoint = new Vector3();
    protected boolean dragging = false;
    @Nullable
    protected Transformable transformable = null;
    private boolean isCursorOver = false;
    private Matrix4 parentTransform = new Matrix4();

    public DragHandle3D(@Nullable ModelInstance modelInstance, BoundingBox bounds, Axis axis) {
        super(modelInstance, bounds);
        this.axis = axis;
    }

    public DragHandle3D(@Nullable ModelInstance modelInstance, Axis axis) {
        super(modelInstance);
        this.axis = axis;
    }

    @Override
    public boolean intersectsRayBoundsFast(Ray ray) {
        isCursorOver = super.intersectsRayBounds(ray, hitPoint);
        return isCursorOver;
    }

    public abstract void update();

    public void render(ModelBatch batch) {
        if (!updated) recalculateTransform();
            batch.render(modelInstance);
    }

    public abstract void drawShapes(ShapeRenderer renderer);

    @Override
    public boolean performRayTest(Ray ray) {
        return intersectsRayBoundsFast(ray);
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        if (modelInstance != null)
            getTransform(modelInstance.transform).mulLeft(parentTransform);
    }

    @Override
    public Entity setTransform(Matrix4 transform) {
        super.setTransform(transform);
        if (modelInstance != null && parentTransform != null)
            getTransform(modelInstance.transform).mulLeft(parentTransform);
        return this;
    }

    public Matrix4 getParentTransform() {
        return parentTransform;
    }

    public void setParentTransform(Matrix4 parentTransform) {
        this.parentTransform.set(parentTransform);
        getTransform(modelInstance.transform).mulLeft(parentTransform);
    }

    public boolean isDragging() {
        return dragging;
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
    @NonNull
    public Vector3 getHitPoint3D() {
        return hitPoint;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (transformable == null) return false;
        dragging = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        dragging = false;
        return transformable != null;
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

    public void setTransformable(@Nullable Transformable transformable) {
        this.transformable = transformable;
    }

    public enum Axis {
        AXIS_X, AXIS_Y, AXIS_Z
    }
}
