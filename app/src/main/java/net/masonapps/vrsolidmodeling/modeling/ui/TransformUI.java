package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

import static net.masonapps.vrsolidmodeling.modeling.ui.TransformUI.TransformAction.DRAG_X;
import static net.masonapps.vrsolidmodeling.modeling.ui.TransformUI.TransformAction.DRAG_Y;
import static net.masonapps.vrsolidmodeling.modeling.ui.TransformUI.TransformAction.NONE;

/**
 * Created by Bob on 1/9/2018.
 */

@SuppressWarnings("ConstantConditions")
public class TransformUI extends VirtualStage {

    private final Vector3 tmpV = new Vector3();
    private final Matrix4 tmpM = new Matrix4();
    private final Vector3 lastHitPoint = new Vector3();
    private final Vector3 currentHitPoint = new Vector3();
    private final ImageButton dragLeft;
    private final ImageButton dragUp;
    @Nullable
    private ModelingEntity entity = null;
    private TransformAction transformAction = NONE;

    public TransformUI(Batch batch, Skin skin) {
        super(batch, 1200, 1200);
        dragLeft = new ImageButton(Style.createImageButtonStyleNoBg(skin, Style.Drawables.ic_drag_left));
        dragLeft.setPosition(1000, 600, Align.left);
        dragLeft.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                transformAction = DRAG_X;
                return true;
            }
        });
        addActor(dragLeft);

        dragUp = new ImageButton(Style.createImageButtonStyleNoBg(skin, Style.Drawables.ic_drag_up));
        dragUp.setPosition(600, 1000, Align.top);
        dragUp.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                transformAction = DRAG_Y;
                return true;
            }
        });
        addActor(dragUp);
    }

    @Override
    public void act() {
        super.act();
        switch (transformAction) {
            case DRAG_X:
                if (entity != null) {
                    tmpV.set(1, 0, 0).rot(tmpM.set(entity.getParentTransform()).inv());
                    entity.translate(tmpV.scl(currentHitPoint.x - lastHitPoint.x));
                    entity.recalculateTransform();
                    entity.modelInstance.transform.getTranslation(position);
                    recalculateTransform();
                }
                break;
            case DRAG_Y:
                if (entity != null) {
                    tmpV.set(0, 1, 0).rot(tmpM.set(entity.getParentTransform()).inv());
                    entity.translate(tmpV.scl(currentHitPoint.y - lastHitPoint.y));
                    entity.recalculateTransform();
                    entity.modelInstance.transform.getTranslation(position);
                    recalculateTransform();
                }
                break;
            default:
                break;
        }
    }

    private void absVector(Vector3 v) {
        v.x = Math.abs(v.x);
        v.y = Math.abs(v.y);
        v.z = Math.abs(v.z);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (transformAction != NONE) {
            if (Intersector.intersectRayPlane(ray, getPlane(), getHitPoint3D())) {
                lastHitPoint.set(currentHitPoint);
                currentHitPoint.set(getHitPoint3D());
            }
            return true;
        }
        return super.performRayTest(ray);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        final boolean touchDown = super.touchDown(screenX, screenY, pointer, button);
        if (touchDown) {
            lastHitPoint.set(currentHitPoint);
            currentHitPoint.set(getHitPoint3D());
        }
        return touchDown;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        final boolean touchUp = super.touchUp(screenX, screenY, pointer, button);
        if (transformAction != NONE) {
            transformAction = NONE;
            return true;
        }
        return touchUp;
    }

    public void setEntity(@Nullable ModelingEntity entity) {
        this.entity = entity;
        if (entity != null) {
            entity.modelInstance.transform.getTranslation(position);
            lookAt(tmpV.set(position).add(0, 0, 1), Vector3.Y);
            recalculateTransform();
            setVisible(true);
        } else
            setVisible(false);
    }

    enum TransformAction {
        NONE, DRAG_X, DRAG_Y
    }
}
