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

public class ScaleWidget extends UiContainer3D {

    private final Vector3 tmp = new Vector3();
    private final Matrix4 tmpM = new Matrix4();
    @Nullable
    private ModelingEntity entity = null;
    private Vector3 startScale = new Vector3();

    public ScaleWidget() {
        super();
        final ModelBuilder builder = new ModelBuilder();
        addTranslationHandles(builder);
        bounds.set(new Vector3(-1.5f, -1.5f, -1.5f), new Vector3(1.5f, 1.5f, 1.5f));
    }

    protected void addTranslationHandles(ModelBuilder builder) {
        final ScaleHandle3D.ScaleListener scaleListener = new ScaleHandle3D.ScaleListener() {
            @Override
            public void touchDown(Input3D.Axis axis) {
                Logger.d("scale drag start " + axis.name());
                if (entity == null) return;
                startScale.set(entity.modelingObject.getScale());
            }

            @Override
            public void dragged(Input3D.Axis axis, float value) {
                Logger.d("scale dragging " + axis.name() + " by " + value);
                if (entity == null) return;
                entity.modelingObject.setScale(startScale.x, startScale.y, startScale.z);
                switch (axis) {
                    case AXIS_X:
                        entity.modelingObject.scaleX(value);
                        break;
                    case AXIS_Y:
                        entity.modelingObject.scaleY(value);
                        break;
                    case AXIS_Z:
                        entity.modelingObject.scaleZ(value);
                        break;
                }
//                SnapUtil.snap(entity.modelingObject.getPosition(), 0.1f);
//                tmpM.set(entity.getParentTransform())
//                        .translate(entity.modelingObject.getPosition());
//                setTransform(tmpM);
            }

            @Override
            public void touchUp(Input3D.Axis axis) {
                Logger.d("scale drag end " + axis.name());
            }
        };

        final ScaleHandle3D scaleX = new ScaleHandle3D(builder, Input3D.Axis.AXIS_X);
        scaleX.setListener(scaleListener);
        add(scaleX);

        final ScaleHandle3D scaleY = new ScaleHandle3D(builder, Input3D.Axis.AXIS_Y);
        scaleY.setListener(scaleListener);
        add(scaleY);

        final ScaleHandle3D scaleZ = new ScaleHandle3D(builder, Input3D.Axis.AXIS_Z);
        scaleZ.setListener(scaleListener);
        add(scaleZ);
    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        if (!isVisible()) return;
        renderer.setTransformMatrix(transform);
        for (Input3D processor : processors) {
            if (processor instanceof ScaleHandle3D)
                ((ScaleHandle3D) processor).drawLines(renderer);
        }
    }

    @Override
    public void setEntity(@Nullable ModelingEntity entity) {
        this.entity = entity;
        if (this.entity != null) {
            tmpM.idt()
                    .translate(this.entity.modelingObject.getPosition())
                    .rotate(this.entity.modelingObject.getRotation());
            setTransform(tmpM);
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
