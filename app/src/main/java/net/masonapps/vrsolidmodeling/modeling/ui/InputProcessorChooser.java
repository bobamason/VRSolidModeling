package net.masonapps.vrsolidmodeling.modeling.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob Mason on 3/19/2018.
 */

public class InputProcessorChooser implements VrInputProcessor {
    private VrInputProcessor activeProcessor = null;

    @Override
    public boolean performRayTest(Ray ray) {
        return activeProcessor != null && activeProcessor.performRayTest(ray);
    }

    @Override
    public boolean isCursorOver() {
        return activeProcessor != null && activeProcessor.isCursorOver();
    }

    @Nullable
    @Override
    public Vector2 getHitPoint2D() {
        return activeProcessor != null ? activeProcessor.getHitPoint2D() : null;
    }

    @Nullable
    @Override
    public Vector3 getHitPoint3D() {
        return activeProcessor != null ? activeProcessor.getHitPoint3D() : null;
    }

    @Override
    public boolean keyDown(int keycode) {
        return activeProcessor != null && activeProcessor.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return activeProcessor != null && activeProcessor.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return activeProcessor != null && activeProcessor.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return activeProcessor != null && activeProcessor.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return activeProcessor != null && activeProcessor.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return activeProcessor != null && activeProcessor.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return activeProcessor != null && activeProcessor.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        return activeProcessor != null && activeProcessor.scrolled(amount);
    }

    public void setActiveProcessor(@Nullable VrInputProcessor activeProcessor) {
        this.activeProcessor = activeProcessor;
    }

    public VrInputProcessor getActiveProcessor() {
        return activeProcessor;
    }
}
