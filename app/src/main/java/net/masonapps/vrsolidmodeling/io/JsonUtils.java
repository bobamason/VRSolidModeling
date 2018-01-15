package net.masonapps.vrsolidmodeling.io;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

/**
 * Created by Bob Mason on 1/15/2018.
 */

public class JsonUtils {

    public static JSONArray floatArrayToJsonArray(float[] floats) {
        return new JSONArray(Arrays.asList(floats));
    }

    public static float[] jsonArrayToFloatArray(JSONArray jsonArray) {
        final int length = jsonArray.length();
        final float[] floats = new float[length];
        for (int i = 0; i < length; i++) {
            try {
                floats[i] = Float.parseFloat(jsonArray.getString(i));
            } catch (NumberFormatException | JSONException e) {
                e.printStackTrace();
            }
        }
        return floats;
    }
}
