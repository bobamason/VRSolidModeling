package net.masonapps.vrsolidmodeling.modeling.ui;

import android.icu.text.DecimalFormat;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.modeling.ModelingObject;

import org.masonapps.libgdxgooglevr.ui.WindowTableVR;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public class TransformWindow extends WindowTableVR {

    @Nullable
    private ModelingObject modelingObject = null;
    private DecimalFormat df = new DecimalFormat("#,###.##");

    public TransformWindow(Batch batch, Skin skin, int virtualPixelWidth, int virtualPixelHeight, WindowVrStyle windowStyle) {
        super(batch, skin, virtualPixelWidth, virtualPixelHeight, Style.getStringResource(net.masonapps.vrsolidmodeling.R.string.title_transform, "Transform"), windowStyle);

        addPositionTable(skin);
        addRotationTable(skin);
        addScaleTable(skin);
        resizeToFitTable();
    }

    private void addPositionTable(Skin skin) {
        final Table posTable = new Table(skin);
        getTable().add(posTable).row();
    }

    private void addRotationTable(Skin skin) {
        final Table rotTable = new Table(skin);
        getTable().add(rotTable).row();
    }

    private void addScaleTable(Skin skin) {
        final Table sclTable = new Table(skin);
        getTable().add(sclTable).row();
    }

    public void setModelingObject(@Nullable ModelingObject modelingObject) {
        this.modelingObject = modelingObject;
        if (modelingObject != null) {
            setPositionValues(modelingObject.getPosition());
            setRotationValues(modelingObject.getRotation());
            setScaleValues(modelingObject.getScale());
        }
    }

    private void setPositionValues(Vector3 position) {

    }

    private void setRotationValues(Quaternion rotation) {
        float yaw = rotation.getYaw();
        float pitch = rotation.getPitch();
        float roll = rotation.getRoll();
    }

    private void setScaleValues(Vector3 scale) {
    }
}
