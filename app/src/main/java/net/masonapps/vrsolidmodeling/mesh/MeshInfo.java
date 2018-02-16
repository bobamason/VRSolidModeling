package net.masonapps.vrsolidmodeling.mesh;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;

/**
 * Created by Bob Mason on 2/16/2018.
 */

public class MeshInfo {
    public float[] vertices;
    public short[] indices;
    public int numVertices;
    public int numIndices;
    public VertexAttributes vertexAttributes;

    public Mesh createMesh() {
        final Mesh mesh = new Mesh(true, true, numVertices, numIndices, vertexAttributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        return mesh;
    }
}
