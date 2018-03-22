package net.masonapps.vrsolidmodeling.mesh;

import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob Mason on 3/22/2018.
 */

public class SweepMesh {

    private static final Vector3 d = new Vector3();
    private static final Vector3 pos = new Vector3();
    private static final Vector3 nor = new Vector3();

    public static MeshInfo sweep(Polygon polygon, Path<Vector3> path, int sections) {
        final MeshInfo meshInfo = new MeshInfo();
        final float step = 1f / sections;
        path.valueAt(pos, 0f);
        path.derivativeAt(d, 0f);
        for (int i = 0; i < sections; i++) {

        }
        path.derivativeAt(d, 1f);
        return meshInfo;
    }
}
