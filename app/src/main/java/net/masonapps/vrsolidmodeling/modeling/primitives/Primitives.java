package net.masonapps.vrsolidmodeling.modeling.primitives;

import net.masonapps.vrsolidmodeling.Assets;
import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.ui.PrimitiveSelector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bob Mason on 1/26/2018.
 */

public class Primitives {
    public static final String KEY_CUBE = "cube";
    public static final String KEY_SPHERE = "sphere";
    public static final String KEY_CYLINDER = "cylinder";
    private static final HashMap<String, Primitive> map = new HashMap<>();

    static {
        final Cube cube = new Cube();
        map.put(cube.getName(), cube);
        final Sphere sphere = new Sphere();
        map.put(sphere.getName(), sphere);
        final AssetPrimitive cylinder = new AssetPrimitive(KEY_CYLINDER, Assets.SHAPE_CYLINDER);
        map.put(cylinder.getName(), cylinder);
    }

    public static List<PrimitiveSelector.PrimitiveItem> createListItems() {
        return Arrays.asList(
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_CUBE, "Cube", Style.Drawables.ic_shape_cube),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_SPHERE, "Sphere", Style.Drawables.ic_shape_sphere),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_CYLINDER, "Cylinder", Style.Drawables.ic_shape_cylinder));
    }

    public static HashMap<String, Primitive> getMap() {
        return map;
    }

    public static Primitive getPrimitive(String key) {
        return map.get(key);
    }
}
