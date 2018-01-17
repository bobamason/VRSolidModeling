package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.math.SnapUtil;
import net.masonapps.vrsolidmodeling.modeling.ModelingEntity;

import org.masonapps.libgdxgooglevr.math.PlaneUtils;
import org.masonapps.libgdxgooglevr.ui.VirtualStage;
import org.masonapps.libgdxgooglevr.utils.Logger;

import static net.masonapps.vrsolidmodeling.modeling.ui.TransformUI.TransformAction.DRAG_X;
import static net.masonapps.vrsolidmodeling.modeling.ui.TransformUI.TransformAction.DRAG_Y;
import static net.masonapps.vrsolidmodeling.modeling.ui.TransformUI.TransformAction.NONE;
import static net.masonapps.vrsolidmodeling.modeling.ui.TransformUI.TransformAction.ROTATE;

/**
 * Created by Bob on 1/9/2018.
 */

@SuppressWarnings("ConstantConditions")
public class TransformUI extends VirtualStage {

    private static final Vector3[] axes = new Vector3[]{
            new Vector3(1, 0, 0),
            new Vector3(-1, 0, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, -1, 0),
            new Vector3(0, 0, 1),
            new Vector3(0, 0, -1)
    };
    private final Vector3 tmpV = new Vector3();
    private final Matrix4 tmpM = new Matrix4();
    private final Vector3 lastHitPoint = new Vector3();
    private final Vector3 currentHitPoint = new Vector3();
    private final ImageButton dragLeft;
    private final ImageButton dragUp;
    private final ImageButton rotateBtn;
    private final float rotateRadius = 254;
    private final Image circleImage;
    @Nullable
    private ModelingEntity entity = null;
    private TransformAction transformAction = NONE;
    private float lastRotation = 0f;


    public TransformUI(Batch batch, Skin skin) {
        super(batch, 1200, 1200);

        circleImage = new Image(skin.newDrawable(Style.Drawables.dash_circle, Color.LIGHT_GRAY));
        circleImage.setOrigin(Align.center);
        circleImage.setPosition(getCenterX(), getCenterY(), Align.center);
        addActor(circleImage);
        
        dragLeft = new ImageButton(Style.createImageButtonStyleNoBg(skin, Style.Drawables.ic_drag_left));
        dragLeft.setPosition(1000, 600, Align.left);
        dragLeft.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                transformAction = DRAG_X;
                lastHitPoint.set(currentHitPoint);
                currentHitPoint.set(getHitPoint3D());
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
                lastHitPoint.set(currentHitPoint);
                currentHitPoint.set(getHitPoint3D());
                return true;
            }
        });
        addActor(dragUp);

        final ImageButton.ImageButtonStyle rotateButtonStyle = new ImageButton.ImageButtonStyle();
        rotateButtonStyle.imageUp = skin.newDrawable(Style.Drawables.ic_rotate_handle, Color.BLUE);
        rotateButtonStyle.imageDown = skin.newDrawable(Style.Drawables.ic_rotate_handle, Color.NAVY);
        rotateButtonStyle.imageDisabled = skin.newDrawable(Style.Drawables.ic_rotate_handle, Color.GRAY);
        rotateBtn = new ImageButton(rotateButtonStyle);
        rotateBtn.setPosition(getCenterX() + rotateRadius, getCenterY(), Align.center);
        rotateBtn.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                transformAction = ROTATE;
                lastRotation = MathUtils.atan2(getHitPoint2D().y - getCenterY(), getHitPoint2D().x - getCenterX());
                Logger.d("rotate start " + (lastRotation * MathUtils.radiansToDegrees));
                return true;
            }
        });
        addActor(rotateBtn);
    }

    protected float getCenterY() {
        return getHeight() / 2f;
    }

    protected float getCenterX() {
        return getWidth() / 2f;
    }

    @Override
    public void act() {
        super.act();
        switch (transformAction) {
            case DRAG_X:
                if (entity != null) {
                    tmpV.set(1, 0, 0).rot(tmpM.set(entity.getParentTransform()).inv());
                    entity.modelingObject.translate(tmpV.scl(currentHitPoint.x - lastHitPoint.x));
                    SnapUtil.snap(entity.modelingObject.getPosition(), 0.01f);
                    entity.update();
                    entity.modelInstance.transform.getTranslation(position);
                    recalculateTransform();
                }
                break;
            case DRAG_Y:
                if (entity != null) {
                    tmpV.set(0, 1, 0).rot(tmpM.set(entity.getParentTransform()).inv());
                    entity.modelingObject.translate(tmpV.scl(currentHitPoint.y - lastHitPoint.y));
                    SnapUtil.snap(entity.modelingObject.getPosition(), 0.01f);
                    entity.update();
                    entity.modelInstance.transform.getTranslation(position);
                    recalculateTransform();
                }
                break;
            case ROTATE:
                if (entity != null) {
                    final float currentRotation = MathUtils.atan2(getHitPoint2D().y - getCenterY(), getHitPoint2D().x - getCenterX());
                    Logger.d("rotating" + (currentRotation * MathUtils.radiansToDegrees));
                    tmpV.set(0, 0, 1).rot(tmpM.set(entity.getParentTransform()).inv());
                    entity.modelingObject.getRotation().mul(new Quaternion(tmpV, (currentRotation - lastRotation) * MathUtils.radiansToDegrees));
                    entity.modelingObject.recalculateTransform();
                    entity.modelingObject.getTransform(entity.modelInstance.transform);
                    rotateBtn.setPosition(rotateRadius * MathUtils.cos(currentRotation) + getCenterX(), rotateRadius * MathUtils.sin(currentRotation) + getCenterY(), Align.center);
                    circleImage.setRotation(currentRotation * MathUtils.radiansToDegrees);
                    lastRotation = currentRotation;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (transformAction != NONE) {
            if (!updated) recalculateTransform();
            final Vector3 tmp = Pools.obtain(Vector3.class);
            final Vector3 tmp2 = Pools.obtain(Vector3.class);
            final Vector2 tmpV2 = Pools.obtain(Vector2.class);
            transform.getTranslation(tmp);
            if (Intersector.intersectRayPlane(ray, getPlane(), getHitPoint3D())) {
                tmp2.set(getHitPoint3D()).sub(tmp);
                PlaneUtils.toSubSpace(getPlane(), tmp2, tmpV2);
                getHitPoint2D().set(tmpV2).scl(1f / (pixelSizeWorld * scale.x), 1f / (pixelSizeWorld * scale.y));
                isCursorOver = true;
                lastHitPoint.set(currentHitPoint);
                currentHitPoint.set(getHitPoint3D());
            }
            Pools.free(tmp);
            Pools.free(tmp2);
            Pools.free(tmpV2);
            return isCursorOver;
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
        NONE, DRAG_X, DRAG_Y, ROTATE, SCALE_X, SCALE_Y
    }
}
