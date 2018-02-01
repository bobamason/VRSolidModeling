package net.masonapps.vrsolidmodeling.modeling.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.vrsolidmodeling.R;
import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.ui.VerticalImageTextButton;

/**
 * Created by Bob Mason on 1/29/2018.
 */

public class TransformModeTable extends Table {


    public TransformModeTable(Skin skin, final OnTransformModeChangedListener listener) {
        super(skin);
        final VerticalImageTextButton translateBtn = new VerticalImageTextButton(Style.getStringResource(R.string.translate, "move"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_translate));
        translateBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onTransformModeChanged(TransformMode.TRANSLATE);
            }
        });
        add(translateBtn);

        final VerticalImageTextButton rotateBtn = new VerticalImageTextButton(Style.getStringResource(R.string.rotate, "rotate"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_rotate));
        rotateBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onTransformModeChanged(TransformMode.ROTATE);
            }
        });
        add(rotateBtn);

        final VerticalImageTextButton scaleBtn = new VerticalImageTextButton(Style.getStringResource(R.string.scale, "scale"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_scale));
        scaleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onTransformModeChanged(TransformMode.SCALE);
            }
        });
        add(scaleBtn);
    }

    public enum TransformMode {
        TRANSLATE, ROTATE, SCALE
    }

    public interface OnTransformModeChangedListener {
        void onTransformModeChanged(TransformMode mode);
    }
}
