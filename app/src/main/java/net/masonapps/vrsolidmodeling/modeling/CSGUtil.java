package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import net.masonapps.vrsolidmodeling.jcsg.CSG;
import net.masonapps.vrsolidmodeling.mesh.MeshInfo;
import net.masonapps.vrsolidmodeling.ui.BooleanOperationSelector;

/**
 * Created by Bob Mason on 7/2/2018.
 */
public class CSGUtil {

    @Nullable
    public static EditableNode apply(EditableNode node1, EditableNode node2, BooleanOperationSelector.BooleanOperation booleanOperation) {
        switch (booleanOperation) {
            case UNION:
                return union(node1, node2);
            case DIFFERENCE:
                return difference(node1, node2);
            case INTERSECTION:
                return intersection(node1, node2);
        }
        return null;
    }

    @Nullable
    public static EditableNode union(EditableNode node1, EditableNode node2) {
        final CSG csg1 = node1.getTransformedCSG();
        final CSG csg2 = node2.getTransformedCSG();
        if (csg1 != null && csg2 != null) {
            final CSG union = csg1.union(csg2);
            final MeshInfo meshInfo = MeshInfo.fromPolygons(union.getPolygons());
            final PolygonAABBTree bvh = new PolygonAABBTree(union.hull().getPolygons());
            return new EditableNode(meshInfo, union, bvh);
        }
        return null;
    }

    @Nullable
    public static EditableNode difference(EditableNode node1, EditableNode node2) {
        final CSG csg1 = node1.getTransformedCSG();
        final CSG csg2 = node2.getTransformedCSG();
        if (csg1 != null && csg2 != null) {
            final CSG difference = csg1.difference(csg2);
            final MeshInfo meshInfo = MeshInfo.fromPolygons(difference.getPolygons());
            final PolygonAABBTree bvh = new PolygonAABBTree(difference.hull().getPolygons());
            return new EditableNode(meshInfo, difference, bvh);
        }
        return null;
    }

    @Nullable
    public static EditableNode intersection(EditableNode node1, EditableNode node2) {
        final CSG csg1 = node1.getTransformedCSG();
        final CSG csg2 = node2.getTransformedCSG();
        if (csg1 != null && csg2 != null) {
            final CSG intersection = csg1.intersect(csg2);
            final MeshInfo meshInfo = MeshInfo.fromPolygons(intersection.getPolygons());
            final PolygonAABBTree bvh = new PolygonAABBTree(intersection.hull().getPolygons());
            return new EditableNode(meshInfo, intersection, bvh);
        }
        return null;
    }
}
