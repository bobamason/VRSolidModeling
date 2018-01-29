package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob Mason on 1/23/2018.
 */

public class TranslateRotateWidget extends UiContainer3D {

    private final Vector3 tmp = new Vector3();
    private final Matrix4 tmpM = new Matrix4();
    @Nullable
    private ModelingEntity entity = null;
    private Vector3 startPosition = new Vector3();

    public TranslateRotateWidget() {
        super();
        final ModelBuilder builder = new ModelBuilder();
        addRotationHandles(builder);
        addTranslationHandles(builder);
        bounds.set(new Vector3(-1.5f, -1.5f, -1.5f), new Vector3(1.5f, 1.5f, 1.5f));
    }

    protected void addTranslationHandles(ModelBuilder builder) {
        final TranslateHandle3D.TranslationListener translationListener = new TranslateHandle3D.TranslationListener() {
            @Override
            public void touchDown(Input3D.Axis axis) {
                Logger.d("drag start " + axis.name());
                if (entity == null) return;
                startPosition.set(entity.modelingObject.getPosition());
            }

            @Override
            public void dragged(Input3D.Axis axis, float value) {
                Logger.d("dragging " + axis.name() + " by " + value);
                if (entity == null) return;
                entity.modelingObject.setPosition(startPosition);
                switch (axis) {
                    case AXIS_X:
                        entity.modelingObject.translateX(value);
                        break;
                    case AXIS_Y:
                        entity.modelingObject.translateY(value);
                        break;
                    case AXIS_Z:
                        entity.modelingObject.translateZ(value);
                        break;
                }
//                SnapUtil.snap(entity.modelingObject.getPosition(), 0.1f);
            }

            @Override
            public void touchUp(Input3D.Axis axis) {
                setEntity(entity);
                Logger.d("drag end " + axis.name());
            }
        };

        final TranslateHandle3D transX = new TranslateHandle3D(builder, Input3D.Axis.AXIS_X);
        transX.setListener(translationListener);
        add(transX);

        final TranslateHandle3D transY = new TranslateHandle3D(builder, Input3D.Axis.AXIS_Y);
        transY.setListener(translationListener);
        add(transY);

        final TranslateHandle3D transZ = new TranslateHandle3D(builder, Input3D.Axis.AXIS_Z);
        transZ.setListener(translationListener);
        add(transZ);
    }

    protected void addRotationHandles(ModelBuilder builder) {
        final RotateHandle3D rotX = new RotateHandle3D(builder, Input3D.Axis.AXIS_X);
        final RotateHandle3D rotY = new RotateHandle3D(builder, Input3D.Axis.AXIS_Y);
        final RotateHandle3D rotZ = new RotateHandle3D(builder, Input3D.Axis.AXIS_Z);

        final RotateHandle3D.RotationListener rotationListener = new RotateHandle3D.RotationListener() {
            @Override
            public void touchDown(Input3D.Axis axis) {
                Logger.d("drag rotation start " + axis.name());
            }

            @Override
            public void dragged(Input3D.Axis axis, float angleDeg) {
                Logger.d("dragging rotation " + axis.name() + " angle " + angleDeg);
                if (entity == null) return;
                entity.modelingObject.setRotation(rotY.getAngleDeg(), rotX.getAngleDeg(), rotZ.getAngleDeg());
//                SnapUtil.snap(entity.modelingObject.getPosition(), 0.1f);
                setEntity(entity);
            }

            @Override
            public void touchUp(Input3D.Axis axis) {
                Logger.d("drag rotation end " + axis.name());
            }
        };

        rotX.setListener(rotationListener);
        add(rotX);

        rotY.setListener(rotationListener);
        add(rotY);

        rotZ.setListener(rotationListener);
        add(rotZ);
    }

    public void drawShapes(ShapeRenderer renderer) {
        if (!isVisible()) return;
        renderer.setTransformMatrix(transform);
        for (Input3D processor : processors) {
            if (processor instanceof RotateHandle3D)
                ((RotateHandle3D) processor).drawCircle(renderer);
        }
    }

    public void setEntity(@Nullable ModelingEntity entity) {
        this.entity = entity;
        if (this.entity != null) {
            tmpM.set(this.entity.getParentTransform())
                    .translate(this.entity.modelingObject.getPosition());
            setTransform(tmpM);
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
