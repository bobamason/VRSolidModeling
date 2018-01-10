package net.masonapps.vrsolidmodeling.modeling.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.vrsolidmodeling.Style;

import org.masonapps.libgdxgooglevr.ui.ImageButtonVR;
import org.masonapps.libgdxgooglevr.ui.VrUiContainer;

/**
 * Created by Bob on 1/9/2018.
 */

public class TransformUI extends VrUiContainer {

    private final ImageButtonVR dragLeft;
    private final ImageButtonVR dragUp;

    public TransformUI(Batch batch, Skin skin) {
        super();
        dragLeft = new ImageButtonVR(batch, Style.createImageButtonStyleNoBg(skin, Style.Drawables.ic_drag_up));
        dragLeft.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });
        addProcessor(dragLeft);

        dragUp = new ImageButtonVR(batch, Style.createImageButtonStyleNoBg(skin, Style.Drawables.ic_drag_up));
        dragUp.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

            }
        });
        addProcessor(dragUp);
    }
}
