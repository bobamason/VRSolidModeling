package net.masonapps.vrsolidmodeling.mesh;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
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
        polygons.stream()
                .filter(Polygon::isValid)
                .filter(polygon -> polygon.vertices.size() >= 3)
                .forEach(polygon -> {
                    final int offset = vertices.size;
                    polygon.vertices.forEach(vertex -> {
                        vertices.add((float) vertex.pos.x());
                        vertices.add((float) vertex.pos.y());
                        vertices.add((float) vertex.pos.z());
                        vertices.add((float) vertex.normal.x());
                        vertices.add((float) vertex.normal.y());
                        vertices.add((float) vertex.normal.z());
                    });
                    for (int i = 0; i < polygon.vertices.size() - 2; i++) {
                        indices.add(offset + i);
                        indices.add(offset + i + 1);
                        indices.add(offset + i + 2);
                    }
                });
        return new MeshInfo(vertices.toArray(), indices.toArray(), new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal()));
    }

    public int numVertices() {
        return vertices.length;
    }

    public int numIndices() {
        return indices.length;
    }

    public Mesh createMesh() {
        final Mesh mesh = new Mesh(true, true, numVertices(), numIndices(), vertexAttributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        return mesh;
    }

    public Mesh createDynamicMesh() {
        if (vertexAttributes == null)
            vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal());
        final Mesh mesh = new Mesh(false, false, 1024 * 10, 1024 * 20, vertexAttributes);
        if (vertices == null)
            vertices = new float[0];
        mesh.setVertices(vertices);
        if (indices == null)
            indices = new short[0];
        mesh.setIndices(indices);
        return mesh;
    }
}
