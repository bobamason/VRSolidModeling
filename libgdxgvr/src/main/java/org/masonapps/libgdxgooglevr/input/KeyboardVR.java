package org.masonapps.libgdxgooglevr.input;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.masonapps.libgdxgooglevr.ui.TableVR;

/**
 * Created by Bob Mason on 2/8/2018.
 */

public class KeyboardVR extends TableVR {
    private final Skin skin;
    private final OnKeyEventListener listener;

    public KeyboardVR(Batch batch, Skin skin, OnKeyEventListener listener) {
        super(batch, skin, 100, 100);
        this.skin = skin;
        this.listener = listener;
    }

    private void addLetterKey(final char key, boolean row) {
        final TextButton button = new TextButton(String.valueOf(key), skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.keyPressed(key);
            }
        });
        final Cell<TextButton> cell = getTable().add(button);
        if (row) cell.row();
    }

    private void addSpaceBar() {
        final TextButton button = new TextButton("Space", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.keyPressed(' ');
            }
        });
        getTable().add(button);
    }

    private void addBackspace() {
        final TextButton button = new TextButton("Backspace", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.backSpacePressed();
            }
        });
        getTable().add(button);
    }

    private void addDone() {
        final TextButton button = new TextButton("Done", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.donePressed();
            }
        });
        getTable().add(button);
    }

    public interface OnKeyEventListener {

        void keyPressed(char key);

        void backSpacePressed();

        void donePressed();
    }
}
