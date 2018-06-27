package net.masonapps.vrsolidmodeling.modeling;


import net.masonapps.vrsolidmodeling.jcsg.Polygon;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;

import java.util.List;

/**
 * Created by Bob Mason on 6/20/2018.
 */
public class PolygonAABBTree extends AABBTree {

    private final List<Polygon> polygons;

    public PolygonAABBTree(List<Polygon> polygons) {
        this.polygons = polygons;
        for (Polygon polygon : polygons) {
            insert(new PolygonAABBObject(polygon));
        }
    }
}
