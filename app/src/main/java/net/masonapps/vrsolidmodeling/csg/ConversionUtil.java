package net.masonapps.vrsolidmodeling.csg;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

/**
 * Created by Bob on 12/20/2017.
 */

public class ConversionUtil {

    public static Transform toTransform(Vector3 position, Quaternion rotation, Vector3 scale) {
        return new Transform()
                .translate(position.x, position.y, position.z)
                .rot(rotation.getPitch(), rotation.getYaw(), rotation.getRoll())
                .scale(scale.x, scale.y, scale.z);
    }

    public static Vector3d toVector3d(Vector3 vec) {
        return Vector3d.xyz(vec.x, vec.y, vec.z);
    }

    public static Vector3 toVector3(Vector3d vec) {
        return new Vector3((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public static Vector3 toVector3(Vector3d in, Vector3 out) {
        return out.set((float) in.getX(), (float) in.getY(), (float) in.getZ());
    }
}
