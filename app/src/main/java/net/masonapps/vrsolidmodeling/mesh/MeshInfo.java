package net.masonapps.vrsolidmodeling.mesh;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import net.masonapps.vrsolidmodeling.jcsg.Polygon;

import java.util.List;

/**
 * Created by Bob Mason on 2/16/2018.
 */

public class MeshInfo {
    public float[] vertices;
    public short[] indices;
    public VertexAttributes vertexAttributes;
    private static int idIndex = 0;

    public MeshInfo() {
        vertices = new float[0];
        indices = new short[0];
        vertexAttributes = new VertexAttributes();
    }

    public MeshInfo(float[] vertices, short[] indices, VertexAttributes vertexAttributes) {
        this.vertices = vertices;
        this.indices = indices;
        this.vertexAttributes = vertexAttributes;
    }

    public static MeshInfo fromPolygons(List<Polygon> polygons) {
        FloatArray vertices = new FloatArray();
        ShortArray indices = new ShortArray();
        final VertexAttributes vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal());
        final int stride = vertexAttributes.vertexSize / Float.BYTES;
        polygons.stream()
                .filter(Polygon::isValid)
                .filter(polygon -> polygon.vertices.size() >= 3)
                .forEach(polygon -> {
                    final int offset = vertices.size / stride;
                    polygon.vertices.forEach(vertex -> {
                        vertices.add((float) vertex.pos.x());
                        vertices.add((float) vertex.pos.y());
                        vertices.add((float) vertex.pos.z());
                        vertices.add((float) vertex.normal.x());
                        vertices.add((float) vertex.normal.y());
                        vertices.add((float) vertex.normal.z());
                    });
                    for (int i = 0; i < polygon.vertices.size() - 2; i++) {
                        indices.add(offset);
                        indices.add(offset + i + 1);
                        indices.add(offset + i + 2);
                    }
                });
//        polygons.stream()
//                .filter(Polygon::isValid)
//                .filter(polygon -> polygon.vertices.size() >= 3)
//                .forEach(polygon -> polygon.toTriangles().forEach(tri -> {
//                    tri.vertices.forEach(vertex -> {
//                        vertices.add((float) vertex.pos.x());
//                        vertices.add((float) vertex.pos.y());
//                        vertices.add((float) vertex.pos.z());
//                        vertices.add((float) vertex.normal.x());
//                        vertices.add((float) vertex.normal.y());
//                        vertices.add((float) vertex.normal.z());
//                        indices.add(indices.size);
//                    });
//                }));
        return new MeshInfo(vertices.toArray(), indices.toArray(), vertexAttributes);
    }

    public int numVertices() {
        return vertices.length;
    }

    public int numIndices() {
        return indices.length;
    }

    public MeshPart createMeshPart(MeshBuilder builder) {
        builder.begin(vertexAttributes);
        final MeshPart meshPart = builder.part("p" + idIndex++, GL20.GL_TRIANGLES);
        builder.ensureVertices(vertices.length / (vertexAttributes.vertexSize / Float.BYTES));
        builder.ensureIndices(indices.length);
        builder.vertex(vertices);
        for (int i = 0; i < indices.length; i++) {
            builder.index(indices[i]);
        }
        builder.end();
        return meshPart;
    }
}
