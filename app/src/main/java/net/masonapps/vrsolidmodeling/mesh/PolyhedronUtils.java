package net.masonapps.vrsolidmodeling.mesh;

import android.util.SparseIntArray;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.vrsolidmodeling.csg.ConversionUtil;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Mason on 2/5/2018.
 */

public class PolyhedronUtils {
    public static PolyhedronsSet fromFaces(List<Vertex> vertexList, List<Face> faces, Matrix4 transform) {
        final List<Vector3D> vertices = new ArrayList<>();
        final List<int[]> facets = new ArrayList<>();
        SparseIntArray indexMap = new SparseIntArray();
        final Vector3 tmp = new Vector3();

        for (int i = 0; i < vertexList.size(); i++) {
            boolean isDouble = false;
            final Vertex vertex = vertexList.get(i);
            for (int j = 0; j < i; j++) {
                if (vertexList.get(j).position.dst2(vertex.position) <= 1e-5f) {
                    indexMap.put(i, j);
                    isDouble = true;
                    break;
                }
            }
            if (!isDouble) {
                indexMap.put(i, vertices.size());
                vertices.add(ConversionUtil.toVector3D(tmp.set(vertex.position).mul(transform)));
            }
        }

        for (Face face : faces) {
            final int[] indices = new int[face.vertices.length];
            for (int i = 0; i < indices.length; i++) {
                final int index = face.vertices[i].index;
                indices[i] = indexMap.get(index, index);
            }
        }

        return new PolyhedronsSet(vertices, facets, 1e-10);
    }
}
