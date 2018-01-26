package net.masonapps.vrsolidmodeling.csg;

import com.badlogic.gdx.math.Vector3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


/**
 * Created by Bob on 12/20/2017.
 */

public class ConversionUtil {

    public static Vector3D toVector3D(Vector3 vec) {
        return new Vector3D(vec.x, vec.y, vec.z);
    }

    public static Vector3 toVector3(Vector3D vec) {
        return new Vector3((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public static Vector3 toVector3(Vector3D in, Vector3 out) {
        return out.set((float) in.getX(), (float) in.getY(), (float) in.getZ());
    }
}
