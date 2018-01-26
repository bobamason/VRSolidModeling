package net.masonapps.vrsolidmodeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.vrsolidmodeling.Style;

import java.util.List;

/**
 * Created by Bob Mason on 1/25/2018.
 */

public class ShapeSelector extends DialogVR {
    @Nullable
    private OnShapeItemClickedListener listener = null;

    public ShapeSelector(Batch batch, Skin skin, List<ShapeItem> items) {
        super(batch, 100, 100);
        final Table table = getTable();
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            final ShapeItem shapeItem = items.get(index);
            final VerticalImageTextButton button = new VerticalImageTextButton(shapeItem.text, Style.createImageTextButtonStyle(skin, shapeItem.drawableName));
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (listener != null)
                        listener.onItemClicked(shapeItem);
                    dismiss();
                }
            });
            final Cell<VerticalImageTextButton> cell = table.add(button);
            cell.pad(8f, i == 0 ? 8f : 0f, 8f, 8f);
        }
        resizeToFitTable();
    }

    public void setListener(@Nullable OnShapeItemClickedListener listener) {
        this.listener = listener;
    }

    public interface OnShapeItemClickedListener {
        void onItemClicked(ShapeItem item);
    }

    public static class ShapeItem {
        public final String primitiveKey;
        public final String text;
        public final String drawableName;

        public ShapeItem(String primitiveKey, String text, String drawableName) {
            this.primitiveKey = primitiveKey;
            this.text = text;
            this.drawableName = drawableName;
        }
    }
}
