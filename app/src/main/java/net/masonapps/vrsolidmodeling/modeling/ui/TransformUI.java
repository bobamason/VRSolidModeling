package net.masonapps.vrsolidmodeling.modeling.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.vrsolidmodeling.Style;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

/**
 * Created by Bob on 1/9/2018.
 */

public class TransformUI extends VirtualStage {

    private Rectangle worldRect = new Rectangle();
    private Rectangle widgetRect = new Rectangle();
    private int extraWidth;
    private int extraHeight;

    public TransformUI(Batch batch, Skin skin) {
        super(batch, 100, 100);
        final Image dragBox = new Image(skin.getPatch(Style.Drawables.drag_box));
        dragBox.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
            }
        });
        addActor(dragBox);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        final boolean rayTest = super.performRayTest(ray);
        if (rayTest) {
            final Vector3 hitPoint = getHitPoint3D();
        }
        return rayTest;
    }

    public void fitToWorldRectangle(Rectangle rect) {
        worldRect.set(rect);
        updateSize();
    }

    private void updateSize() {
        setX(worldRect.x + worldRect.width / 2f);
        setY(worldRect.y + worldRect.height / 2f);
        final int w = (int) (worldRect.width / pixelSizeWorld);
        final int h = (int) (worldRect.height / pixelSizeWorld);
        setSize(w + extraWidth * 2, h + extraHeight * 2);
        widgetRect.set(extraWidth, extraHeight, w, h);

    }
}
