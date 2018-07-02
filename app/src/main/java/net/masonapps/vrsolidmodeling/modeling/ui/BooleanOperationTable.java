package net.masonapps.vrsolidmodeling.modeling.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.vrsolidmodeling.ui.BooleanOperationSelector;

import org.masonapps.libgdxgooglevr.ui.TableVR;

/**
 * Created by Bob Mason on 7/2/2018.
 */
public class BooleanOperationTable extends TableVR {


    public BooleanOperationTable(Batch batch, Skin skin, BooleanOperationTableListener listener) {
        super(batch, skin, 200, 200);
        final BooleanOperationSelector booleanOperationSelector = new BooleanOperationSelector(skin, listener::onSelectedOperationChanged);
        table.add(booleanOperationSelector).row();
        final TextButton doneBtn = new TextButton("done", skin);
        doneBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onDoneClicked(booleanOperationSelector.getSelectedOperation());
            }
        });
        table.add(doneBtn).pad(8);
        resizeToFitTable();
    }

    public interface BooleanOperationTableListener {
        void onSelectedOperationChanged(BooleanOperationSelector.BooleanOperation booleanOperation);

        void onDoneClicked(BooleanOperationSelector.BooleanOperation booleanOperation);
    }
}
