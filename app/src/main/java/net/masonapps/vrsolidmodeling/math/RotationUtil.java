package net.masonapps.vrsolidmodeling.math;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 7/26/2017.
 */

public class RotationUtil {

    private static final Vector3 tempVec = new Vector3();
    private static final Matrix4 tempMat = new Matrix4();

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
        tempMat.set(in);
        final float[] m = tempMat.getValues();

        tempVec.set(m[Matrix4.M00], m[Matrix4.M10], m[Matrix4.M20]);
        setToClosestUnitVector(tempVec);
        m[Matrix4.M00] = tempVec.x;
        m[Matrix4.M10] = tempVec.y;
        m[Matrix4.M20] = tempVec.z;

        tempVec.set(m[Matrix4.M01], m[Matrix4.M11], m[Matrix4.M21]);
        setToClosestUnitVector(tempVec);
        m[Matrix4.M01] = tempVec.x;
        m[Matrix4.M11] = tempVec.y;
        m[Matrix4.M21] = tempVec.z;

        tempVec.set(m[Matrix4.M02], m[Matrix4.M12], m[Matrix4.M22]);
        setToClosestUnitVector(tempVec);
        m[Matrix4.M02] = tempVec.x;
        m[Matrix4.M12] = tempVec.y;
        m[Matrix4.M22] = tempVec.z;

        out.setFromMatrix(tempMat);
    }

    private static void setToClosestUnitVector(Vector3 v) {
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
    }
}
