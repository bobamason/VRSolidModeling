package net.masonapps.vrsolidmodeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import org.masonapps.libgdxgooglevr.ui.WindowTableVR;
import org.masonapps.libgdxgooglevr.ui.WindowVR;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class ColorPickerWindow extends WindowTableVR {

    private final ColorPickerSimple colorPicker;

    public ColorPickerWindow(Batch batch, Skin skin, int width, int height, String title, WindowVR.WindowVrStyle windowStyle) {
        super(batch, skin, width, height, title, windowStyle);
        colorPicker = new ColorPickerSimple(skin, width, height);
        table.add(colorPicker);
        resizeToFitTable();
    }

    public ColorPickerSimple getColorPicker() {
        return colorPicker;
    }

    @Override
    public void dispose() {
        super.dispose();
        colorPicker.dispose();
    }

    public void setColor(Color color) {
        colorPicker.setColor(color);
    }
}
