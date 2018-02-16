package net.masonapps.vrsolidmodeling.modeling.primitives;

import net.masonapps.vrsolidmodeling.Assets;
import net.masonapps.vrsolidmodeling.Style;
import net.masonapps.vrsolidmodeling.bvh.BVH;
import net.masonapps.vrsolidmodeling.mesh.MeshInfo;
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
    public static final String KEY_CONE = "cone";
    public static final String KEY_TORUS = "torus";
    private static final HashMap<String, Primitive> map = new HashMap<>();

    static {
        addToMap(new Cube());
        addToMap(new Sphere());
        addToMap(new AssetPrimitive(KEY_CYLINDER, Assets.SHAPE_CYLINDER));
        addToMap(new AssetPrimitive(KEY_CONE, Assets.SHAPE_CONE));
        addToMap(new AssetPrimitive(KEY_TORUS, Assets.SHAPE_TORUS));
    }

    private static void addToMap(AssetPrimitive primitive) {
        map.put(primitive.getName(), primitive);
    }

    public static List<PrimitiveSelector.PrimitiveItem> createListItems() {
        return Arrays.asList(
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_CUBE, "Cube", Style.Drawables.ic_shape_cube),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_SPHERE, "Sphere", Style.Drawables.ic_shape_sphere),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_CYLINDER, "Cylinder", Style.Drawables.ic_shape_cylinder),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_CONE, "Cone", Style.Drawables.ic_shape_cone),
                new PrimitiveSelector.PrimitiveItem(Primitives.KEY_TORUS, "Torus", Style.Drawables.ic_shape_torus));
    }

    public static HashMap<String, Primitive> getMap() {
        return map;
    }

    public static Primitive getPrimitive(String key) {
        return map.get(key);
    }

    public static MeshInfo getPrimitiveMeshInfo(String key) {
        return map.get(key).getMeshInfo();
    }

    public static BVH getPrimitiveBVH(String key) {
        return map.get(key).getBVH();
    }
}
