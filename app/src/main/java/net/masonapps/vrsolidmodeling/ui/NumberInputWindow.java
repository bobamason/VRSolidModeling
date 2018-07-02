package net.masonapps.vrsolidmodeling.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.masonapps.libgdxgooglevr.ui.WindowTableVR;

/**
 * Created by Bob Mason on 1/17/2018.
 */

public class NumberInputWindow extends WindowTableVR {


    private final float padding = 8f;

    public NumberInputWindow(Batch batch, Skin skin, WindowVrStyle windowStyle) {
        super(batch, skin, 100, 100, "", windowStyle);
        addKeys(skin);
        resizeToFitTable();
    }

    public void clearKeyboardFocus() {
        setKeyboardFocus(null);
    }

    public void show(TextField textField) {
        setKeyboardFocus(textField);
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            clearKeyboardFocus();
    }

    private void addKeys(Skin skin) {
        addKey(skin, '7', false);
        addKey(skin, '8', false);
        addKey(skin, '9', false);
        addKey(skin, '/', true);

        addKey(skin, '4', false);
        addKey(skin, '5', false);
        addKey(skin, '6', false);
        addKey(skin, '*', true);

        addKey(skin, '1', false);
        addKey(skin, '2', false);
        addKey(skin, '3', false);
        addKey(skin, '-', true);

        final TextButton key = new TextButton("DEL", skin);
        key.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Actor keyboardFocus = getKeyboardFocus();
                if (keyboardFocus instanceof TextField) {
                    final InputListener textFieldInputListener = ((TextField) keyboardFocus).getDefaultInputListener();
                    if (textFieldInputListener != null)
                        textFieldInputListener.keyTyped(null, (char) 8);
                }
            }
        });
        getTable().add(key).right().pad(padding, padding, padding, padding);
        addKey(skin, '.', false);
        addKey(skin, '0', false);
        addKey(skin, '+', false);
    }

    private void addKey(Skin skin, final char c, boolean row) {
        final TextButton key = new TextButton(String.valueOf(c), skin);
        key.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Actor keyboardFocus = getKeyboardFocus();
                if (keyboardFocus instanceof TextField) {
                    final InputListener textFieldInputListener = ((TextField) keyboardFocus).getDefaultInputListener();
                    if (textFieldInputListener != null)
                        textFieldInputListener.keyTyped(null, c);
                }
            }
        });
        final Cell<TextButton> cell = getTable().add(key).pad(padding, padding, padding, padding);
        if (row) cell.row();
    }
}
