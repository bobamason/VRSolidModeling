package net.masonapps.vrsolidmodeling.modeling.primitives;

import java.util.HashMap;

/**
 * Created by Bob Mason on 1/15/2018.
 */

public class Primitives {

    private static HashMap<String, Primitive> map = new HashMap<>();

    static {
        final Primitive cube = new Cube();
        map.put(cube.getName(), cube);
        final Primitive sphere = new Sphere();
        map.put(sphere.getName(), sphere);
    }

    public static Primitive get(String name) {
        return map.get(name);
    }
}
