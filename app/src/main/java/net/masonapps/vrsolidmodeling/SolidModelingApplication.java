package net.masonapps.vrsolidmodeling;

import android.app.Application;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Matrix4;

import net.masonapps.vrsolidmodeling.mesh.MeshData;

/**
 * Created by Bob Mason on 12/20/2017.
 */

public class SolidModelingApplication extends Application {

    private final Matrix4 transform = new Matrix4();
    @Nullable
    private MeshData meshData = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        meshData = null;
        super.onTerminate();
    }

    @Nullable
    public MeshData getMeshData() {
        return meshData;
    }

    public void setMeshData(@Nullable MeshData meshData, Matrix4 transform) {
        this.meshData = meshData;
        this.transform.set(transform);
    }

    public Matrix4 getTransform() {
        return transform;
    }
}
