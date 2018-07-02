package net.masonapps.vrsolidmodeling.modeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.vrsolidmodeling.R;
import net.masonapps.vrsolidmodeling.Style;

import org.mariuszgromada.math.mxparser.Expression;
import org.masonapps.libgdxgooglevr.ui.TableVR;
import org.masonapps.libgdxgooglevr.utils.Logger;

import java.text.DecimalFormat;

/**
 * Created by Bob Mason on 7/2/2018.
 */
public class NodePropertiesTable extends TableVR {

    public static final int PAD_LARGE = 8;
    public static final int PAD_SMALL = 4;
    private final Skin skin;

    public NodePropertiesTable(Batch batch, Skin skin) {
        super(batch, skin, 200, 200);
        this.skin = skin;

        addPositionSettings();
        addRotationSettings();
        addScaleSettings();

        resizeToFitTable();
    }

    private void addPositionSettings() {
        table.add(Style.getStringResource(R.string.position, "position")).pad(PAD_LARGE, PAD_LARGE, 0, PAD_LARGE).growX().center().row();
        table.add(new ValueTable("x:", skin)).row();
        table.add(new ValueTable("y:", skin)).row();
        table.add(new ValueTable("z:", skin)).row();
    }

    private void addRotationSettings() {
        table.add(Style.getStringResource(R.string.rotation, "rotation")).pad(0, PAD_LARGE, 0, PAD_LARGE).growX().center().row();
        table.add(new ValueTable("x:", skin)).row();
        table.add(new ValueTable("y:", skin)).row();
        table.add(new ValueTable("z:", skin)).row();
    }

    private void addScaleSettings() {
        table.add(Style.getStringResource(R.string.scale, "scale")).pad(0, PAD_LARGE, 0, PAD_LARGE).center().row();
        table.add(new ValueTable("x:", skin)).row();
        table.add(new ValueTable("y:", skin)).row();
        table.add(new ValueTable("z:", skin)).row();
    }

    private static class ValueTable extends Table {
        private static final DecimalFormat df = new DecimalFormat("#,###.###");
        private float value = 0f;
        private float step = 0.05f;

        public ValueTable(String label, Skin skin) {
            super(skin);
            add(label).pad(PAD_LARGE, PAD_LARGE, PAD_LARGE, PAD_LARGE);

            final TextField valueField = new TextField("0.000", skin);

            final ImageButton leftArrow = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.left_arrow_small));
            leftArrow.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    value -= step;
                    valueField.setText(df.format(value));
                }
            });
            add(leftArrow).pad(PAD_LARGE, 0, PAD_LARGE, PAD_SMALL);

            valueField.setTextFieldListener((textField, c) -> {
                // TODO: 6/21/2018 add value listener 
                final Expression expression = new Expression(textField.getText());
                if (expression.checkSyntax()) {
                    textField.getStyle().fontColor.set(Color.WHITE);
                    value = (float) expression.calculate();
                    Logger.d("value = " + value);
                } else {
                    textField.getStyle().fontColor.set(Color.RED);
                }
            });
            add(valueField).pad(PAD_LARGE, 0, PAD_LARGE, PAD_SMALL);

            final ImageButton rightArrow = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.right_arrow_small));
            rightArrow.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    value += step;
                    valueField.setText(df.format(value));
                }
            });
            add(rightArrow).pad(PAD_LARGE, 0, PAD_LARGE, PAD_LARGE);
        }
    }
}
