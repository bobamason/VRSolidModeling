package net.masonapps.vrsolidmodeling;

import android.app.Application;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Matrix4;

import net.masonapps.vrsolidmodeling.modeling.ModelingObject;

import java.util.List;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class SolidModelingApplication extends Application {

    private final Matrix4 transform = new Matrix4();
    @Nullable
    private List<ModelingObject> modelingObjects = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        modelingObjects = null;
        super.onTerminate();
    }

    @Nullable
    public List<ModelingObject> getModelingObjects() {
        return modelingObjects;
    }

    public void setModelingProject(@Nullable List<ModelingObject> objectList, Matrix4 transform) {
        this.modelingObjects = objectList;
        this.transform.set(transform);
    }

    public Matrix4 getTransform() {
        return transform;
    }
}
