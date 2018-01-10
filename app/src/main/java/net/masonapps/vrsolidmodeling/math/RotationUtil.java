package net.masonapps.vrsolidmodeling.math;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import org.masonapps.libgdxgooglevr.utils.Logger;

/**
 * Created by Bob on 7/26/2017.
 */

public class RotationUtil {

    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    private static final Vector3 up = new Vector3();
    private static final Vector3 dir = new Vector3();
    private static final Matrix4 tmpMat = new Matrix4();

    public static void rotateToViewSide(Quaternion rotation, Side side) {
        switch (side) {
            case FRONT:
                rotation.idt();
                break;
            case BACK:
                rotation.set(Vector3.Y, 180);
                break;
            case LEFT:
                rotation.set(Vector3.Y, -90);
                break;
            case RIGHT:
                rotation.set(Vector3.Y, 90);
                break;
            case TOP:
                rotation.set(Vector3.X, 90);
                break;
            case BOTTOM:
                rotation.set(Vector3.X, -90);
                break;
        }
    }
    
    public static void snap(Quaternion in, Quaternion out) {
        dir.set(0, 0, 1).mul(in);
        setToClosestUnitVector(dir);
        up.set(0, 1, 0).mul(in);
        setToClosestUnitVector(up);
        if (dir.isCollinear(up, 1e-5f)) {
            Logger.d("collinear: dir[" + dir + "] up[" + up + "]");
            up.set(0, 1, 0);
        }
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        Logger.d("x[" + tmp + "] y[" + tmp2 + "] z[" + dir + "]");
        out.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
//        out.setFromAxes(tmp.x, tmp.y, tmp.z, tmp2.x, tmp2.y, tmp2.z, dir.x, dir.y, dir.z);
    }

//    public static void snap(Quaternion in, Quaternion out) {
//        tmpMat.set(in);
//        final float[] m = tmpMat.getValues();
//
//        tmp.set(m[Matrix4.M00], m[Matrix4.M10], m[Matrix4.M20]);
//        setToClosestUnitVector(tmp);
//        m[Matrix4.M00] = tmp.x;
//        m[Matrix4.M10] = tmp.y;
//        m[Matrix4.M20] = tmp.z;
//
//        tmp.set(m[Matrix4.M01], m[Matrix4.M11], m[Matrix4.M21]);
//        setToClosestUnitVector(tmp);
//        m[Matrix4.M01] = tmp.x;
//        m[Matrix4.M11] = tmp.y;
//        m[Matrix4.M21] = tmp.z;
//
//        tmp.set(m[Matrix4.M02], m[Matrix4.M12], m[Matrix4.M22]);
//        setToClosestUnitVector(tmp);
//        m[Matrix4.M02] = tmp.x;
//        m[Matrix4.M12] = tmp.y;
//        m[Matrix4.M22] = tmp.z;
//
//        out.setFromMatrix(tmpMat);
//    }

    public static Vector3 setToClosestUnitVector(Vector3 v) {
        if (Math.abs(v.x) > Math.abs(v.y)) {
            if (Math.abs(v.x) > Math.abs(v.z)) {
                v.x = Math.signum(v.x);
                v.y = 0f;
                v.z = 0f;
            } else {
                v.z = Math.signum(v.z);
                v.x = 0f;
                v.y = 0f;
            }
        } else {
            if (Math.abs(v.y) > Math.abs(v.z)) {
                v.y = Math.signum(v.y);
                v.x = 0f;
                v.z = 0f;
            } else {
                v.z = Math.signum(v.z);
                v.x = 0f;
                v.y = 0f;
            }
        }
        return v;
    }
}
