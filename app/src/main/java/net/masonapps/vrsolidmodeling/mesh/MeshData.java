package net.masonapps.vrsolidmodeling.mesh;

/**
 * Created by Bob on 7/24/2017.
 */

public class MeshData {
    public final Vertex[] vertices;
    public final Triangle[] triangles;

    public MeshData(Vertex[] vertices, Triangle[] triangles) {
        this.vertices = vertices;
        this.triangles = triangles;
    }

    public MeshData(MeshData other) {
        this(other.vertices, other.triangles);
    }

    public Triangle[] getTriangles() {
        return triangles;
    }

    public Vertex getVertex(int i) {
        return vertices[i];
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public int getVertexCount() {
        return vertices.length;
    }

    public int getTriangleCount() {
        return triangles.length;
    }

    public MeshData copy() {
        return new MeshData(this);
    }

    public Triangle getTriangle(int index) {
        return triangles[index];
    }
}
