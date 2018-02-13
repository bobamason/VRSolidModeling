package net.masonapps.vrsolidmodeling.modeling.transform;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;

import org.masonapps.libgdxgooglevr.gfx.Transformable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public abstract class TransformWidget3D extends Transformable {

    protected List<DragHandle3D> processors;
    protected BoundingBox bounds = new BoundingBox();
    @Nullable
    protected EditableNode entity = null;
    private boolean isCursorOver = false;
    private Vector3 hitPoint = new Vector3();
    @Nullable
    private OnTransformActionListener listener = null;
    @Nullable
    private DragHandle3D focusedProcessor = null;
    private Ray transformedRay = new Ray();
    private boolean visible = false;
    private boolean touchDown = false;

    public TransformWidget3D() {
        processors = new ArrayList<>();
    }

    public void add(DragHandle3D processor) {
        processors.add(processor);
        processor.setParentTransform(transform);
        bounds.ext(processor.getBounds());
    }

    public void setListener(@Nullable OnTransformActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        processors.forEach(processor -> processor.setParentTransform(transform));
    }

    @Override
    public Transformable setTransform(Matrix4 transform) {
        super.setTransform(transform);
        processors.forEach(processor -> processor.setParentTransform(transform));
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean performRayTest(Ray ray) {
        if (!visible) return false;
        if (!updated) recalculateTransform();
        transformedRay.set(ray).mul(inverseTransform);
        if (focusedProcessor != null && focusedProcessor.isDragging()) {
            if (focusedProcessor.performRayTest(transformedRay)) {
                hitPoint.set(focusedProcessor.getHitPoint3D()).mul(transform);
                isCursorOver = true;
            } else
                isCursorOver = false;
        } else {
            DragHandle3D tempProcessor = null;
//            if (Intersector.intersectRayBoundsFast(transformedRay, bounds)) {
                float d = Float.MAX_VALUE;
                for (DragHandle3D processor : processors) {
                    if (processor.performRayTest(transformedRay)) {
                        if (processor.getHitPoint3D().dst2(ray.origin) < d) {
                            hitPoint.set(processor.getHitPoint3D()).mul(transform);
                            tempProcessor = processor;
                        }
                    }
                }
//            }
            if (tempProcessor != focusedProcessor && focusedProcessor != null)
                focusedProcessor.touchUp();
            focusedProcessor = tempProcessor;
            isCursorOver = focusedProcessor != null;
        }
        return isCursorOver;
    }

    public boolean isCursorOver() {
        return isCursorOver;
    }

    public Vector3 getHitPoint3D() {
        return hitPoint;
    }

    public boolean touchDown() {
        touchDown = focusedProcessor != null && focusedProcessor.touchDown();
        if (touchDown && listener != null && entity != null)
            listener.onTransformStarted(entity);
        return touchDown;
    }

    public boolean touchUp() {
        if (touchDown) {
            if (listener != null && entity != null)
                listener.onTransformFinished(entity);
            touchDown = false;
        }
        return focusedProcessor != null && focusedProcessor.touchUp();
    }

    public void update() {
        if (!updated) recalculateTransform();
        processors.forEach(DragHandle3D::update);
    }

    public void render(ModelBatch batch) {
        if (!isVisible()) return;
        processors.forEach(processor -> processor.render(batch));
    }

    @CallSuper
    public void drawShapes(ShapeRenderer renderer) {
        if (!isVisible()) return;
        renderer.setTransformMatrix(transform);
        processors.forEach(processor -> processor.drawShapes(renderer));
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @CallSuper
    public void setEntity(@Nullable EditableNode entity, Transformable transformable) {
        this.entity = entity;
        processors.forEach(processor -> processor.setTransformable(entity));
        if (this.entity != null) {
            setPosition(transformable.getPosition());
            setRotation(transformable.getRotation());
            setScale(transformable.getScaleX(), transformable.getScaleY(), transformable.getScaleZ());
        }
    }

    public interface OnTransformActionListener {
        void onTransformStarted(@NonNull EditableNode entity);

        void onTransformFinished(@NonNull EditableNode entity);
    }
}
