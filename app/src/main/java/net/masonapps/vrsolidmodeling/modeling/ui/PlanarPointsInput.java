package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 3/22/2018.
 */

public class PlanarPointsInput implements VrInputProcessor {

    private final Plane plane = new Plane();
    private final List<Vector3> points = new ArrayList<>();
    //    private final Vector2 hitPoint2D = new Vector2();
    private final Vector3 hitPoint3D = new Vector3();
    private final OnPointAddedListener listener;
    protected boolean isCursorOver = false;

    public PlanarPointsInput(OnPointAddedListener listener) {
        this.listener = listener;
    }

    public Plane getPlane() {
        return plane;
    }

    public List<Vector3> getPoints() {
        return points;
    }

    public void reset() {
        points.clear();
    }

    @Override
    public boolean performRayTest(Ray ray) {
        isCursorOver = Intersector.intersectRayPlane(ray, plane, hitPoint3D);
        return isCursorOver;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    @Nullable
    @Override
    public Vector2 getHitPoint2D() {
        return null;
    }

    @Nullable
    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint3D;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isCursorOver) {
            final Vector3 cpy = hitPoint3D.cpy();
            points.add(cpy);
            listener.pointAdded(cpy);
        }
        return isCursorOver;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return isCursorOver;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    public interface OnPointAddedListener {
        void pointAdded(Vector3 point);
    }
}
