package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.vrsolidmodeling.math.SnapUtil;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

/**
 * Created by Bob Mason on 1/23/2018.
 */

public class TranslateRotateWidget extends UiContainer3D {

    private final Vector3 tmp = new Vector3();
    @Nullable
    private ModelingEntity entity = null;
    private Vector3 startPosition = new Vector3();

    public TranslateRotateWidget() {
        super();
        final ModelBuilder builder = new ModelBuilder();
        addTranslationHandles(builder);
    }

    protected void addTranslationHandles(ModelBuilder builder) {
        final TranslateHandle3D.TranslationListener translationListener = new TranslateHandle3D.TranslationListener() {
            @Override
            public void touchDown(Input3D.Axis axis) {
                if (entity == null) return;
                startPosition.set(entity.modelingObject.getPosition());
            }

            @Override
            public void dragged(Input3D.Axis axis, float value) {
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
                SnapUtil.snap(entity.modelingObject.getPosition(), 0.1f);
            }

            @Override
            public void touchUp(Input3D.Axis axis) {

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

    public void setEntity(@Nullable ModelingEntity entity) {
        this.entity = entity;
        if (this.entity != null) {
            this.entity.modelInstance.transform.getTranslation(tmp);
            setPosition(tmp);
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
