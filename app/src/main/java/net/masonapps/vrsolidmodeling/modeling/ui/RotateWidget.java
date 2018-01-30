package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;
import net.masonapps.vrsolidmodeling.modeling.ModelingObject;

import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob Mason on 1/23/2018.
 */

public class RotateWidget extends UiContainer3D {

    private final Vector3 tmp = new Vector3();
    private final Matrix4 tmpM = new Matrix4();
    @Nullable
    private ModelingEntity entity = null;
    private RotateHandle3D rotX;
    private RotateHandle3D rotY;
    private RotateHandle3D rotZ;

    public RotateWidget() {
        super();
        final ModelBuilder builder = new ModelBuilder();
        rotX = new RotateHandle3D(builder, Input3D.Axis.AXIS_X);
        rotY = new RotateHandle3D(builder, Input3D.Axis.AXIS_Y);
        rotZ = new RotateHandle3D(builder, Input3D.Axis.AXIS_Z);

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
        bounds.set(new Vector3(-1.5f, -1.5f, -1.5f), new Vector3(1.5f, 1.5f, 1.5f));
    }

    @Override
    public void drawShapes(ShapeRenderer renderer) {
        if (!isVisible()) return;
        renderer.setTransformMatrix(transform);
        for (Input3D processor : processors) {
            if (processor instanceof RotateHandle3D)
                ((RotateHandle3D) processor).drawCircle(renderer);
        }
    }

    @Override
    public void setEntity(@Nullable ModelingEntity entity) {
        this.entity = entity;
        if (this.entity != null) {
            final ModelingObject modelingObject = this.entity.modelingObject;
            tmpM.set(this.entity.getParentTransform())
                    .translate(modelingObject.getPosition());
            setTransform(tmpM);
            final Quaternion rotation = modelingObject.getRotation();
            rotZ.setAngleDeg(rotation.getRoll());
            rotX.setAngleDeg(rotation.getPitch());
            rotY.setAngleDeg(rotation.getYaw());
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
