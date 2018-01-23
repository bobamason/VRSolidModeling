package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.Transformable;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public abstract class UiContainer3D extends Transformable implements VrInputProcessor {

    private boolean isCursorOver = false;
    private Vector3 hitPoint = new Vector3();
    private List<Input3D> processors;
    private BoundingBox bounds = new BoundingBox();
    @Nullable
    private Input3D focusedProcessor = null;
    private Ray transformedRay = new Ray();
    private boolean visible = false;

    public UiContainer3D() {
        processors = new ArrayList<>();
    }

    public void add(Input3D processor) {
        processors.add(processor);
        processor.setParentTransform(transform);
        bounds.ext(processor.getBounds());
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        for (Input3D processor : processors) {
            processor.setParentTransform(transform);
        }
    }

    @Override
    public Transformable setTransform(Matrix4 transform) {
        super.setTransform(transform);
        for (Input3D processor : processors) {
            processor.setParentTransform(transform);
        }
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean performRayTest(Ray ray) {
        if (!updated) recalculateTransform();
        transformedRay.set(ray).mul(inverseTransform);
        if (Intersector.intersectRayBoundsFast(transformedRay, bounds)) {
            float d = Float.MAX_VALUE;
            for (Input3D processor : processors) {
                if (processor.performRayTest(transformedRay))
                    if (processor.getHitPoint3D().dst2(ray.origin) < d) {
                        focusedProcessor = processor;
                    }
            }
        }
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return focusedProcessor != null && focusedProcessor.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return focusedProcessor != null && focusedProcessor.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return focusedProcessor != null && focusedProcessor.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void update() {
        if (!updated) recalculateTransform();
    }

    public void render(ModelBatch batch, Environment environment) {
        if (!isVisible()) return;
        for (Input3D processor : processors) {
            if (processor.isLightingEnabled())
                batch.render(processor.modelInstance, environment);
            else
                batch.render(processor.modelInstance);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
