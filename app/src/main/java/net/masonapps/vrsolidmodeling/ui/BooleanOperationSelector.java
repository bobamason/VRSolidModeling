package net.masonapps.vrsolidmodeling.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.vrsolidmodeling.R;
import net.masonapps.vrsolidmodeling.Style;

/**
 * Created by Bob Mason on 6/29/2018.
 */
public class BooleanOperationSelector extends Table {

    private BooleanOperation selectedOperation = BooleanOperation.UNION;

    public BooleanOperationSelector(Skin skin, BooleanOperationSelectedListener listener) {
        super(skin);

        final VerticalImageTextButton union = new VerticalImageTextButton(Style.getStringResource(R.string.union, "union"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_union, true));
        add(union).pad(8, 8, 8, 4);

        final VerticalImageTextButton difference = new VerticalImageTextButton(Style.getStringResource(R.string.difference, "difference"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_difference, true));
        add(difference).pad(8, 0, 8, 4);

        final VerticalImageTextButton intersection = new VerticalImageTextButton(Style.getStringResource(R.string.intersection, "intersection"), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_intersection, true));
        add(intersection).pad(8, 0, 8, 8);


        union.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                union.setChecked(true);
                difference.setChecked(false);
                intersection.setChecked(false);
                selectedOperation = BooleanOperation.UNION;
                listener.onBooleanOperationSelected(selectedOperation);
            }
        });
        difference.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                union.setChecked(false);
                difference.setChecked(true);
                intersection.setChecked(false);
                selectedOperation = BooleanOperation.DIFFERENCE;
                listener.onBooleanOperationSelected(selectedOperation);
            }
        });
        intersection.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                union.setChecked(false);
                difference.setChecked(false);
                intersection.setChecked(true);
                selectedOperation = BooleanOperation.INTERSECTION;
                listener.onBooleanOperationSelected(selectedOperation);
            }
        });


        union.setChecked(true);
        difference.setChecked(false);
        intersection.setChecked(false);
    }

    public BooleanOperation getSelectedOperation() {
        return selectedOperation;
    }

    public enum BooleanOperation {
        UNION, DIFFERENCE, INTERSECTION
    }

    public interface BooleanOperationSelectedListener {
        void onBooleanOperationSelected(BooleanOperation booleanOperation);
    }
}
