package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.Nullable;

import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.jcsg.Cube;
import net.masonapps.vrsolidmodeling.jcsg.Cylinder;
import net.masonapps.vrsolidmodeling.jcsg.Primitive;
import net.masonapps.vrsolidmodeling.jcsg.Sphere;
import net.masonapps.vrsolidmodeling.ui.PrimitiveSelector;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bob Mason on 1/26/2018.
 */

public class Primitives {
    public static final String KEY_CUBE = "cube";
    public static final String KEY_SPHERE = "sphere";
    public static final String KEY_CYLINDER = "cylinder";

    public static List<PrimitiveSelector.PrimitiveItem> createListItems() {
        return Arrays.asList(
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_CUBE, "Cube", Style.Drawables.ic_shape_cube),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_SPHERE, "Sphere", Style.Drawables.ic_shape_sphere),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_CYLINDER, "Cylinder", Style.Drawables.ic_shape_cylinder));
    }

    @Nullable
    public static Primitive createPrimitive(String key) {
        if (key.equals(KEY_CUBE))
            return new Cube();
        else if (key.equals(KEY_SPHERE))
            return new Sphere();
        else if (key.equals(KEY_CYLINDER))
            return new Cylinder();
        else
            return null;
    }
}
