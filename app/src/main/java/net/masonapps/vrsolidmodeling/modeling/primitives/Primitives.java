package net.masonapps.vrsolidmodeling.modeling.primitives;

import java.util.HashMap;

/**
 * Created by Bob Mason on 1/26/2018.
 */

public class Primitives {
    public static final String KEY_CUBE = "cube";
    public static final String KEY_SPHERE = "sphere";
    private static final HashMap<String, Primitive> map = new HashMap<>();

    static {
        final Cube cube = new Cube();
        map.put(cube.getName(), cube);
        final Sphere sphere = new Sphere();
        map.put(sphere.getName(), sphere);
    }

    public static HashMap<String, Primitive> getMap() {
        return map;
    }

    public static Primitive getPrimitive(String key) {
        return map.get(key);
    }
}
