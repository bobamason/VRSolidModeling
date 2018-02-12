package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.bvh.BVH;
import net.masonapps.vrsolidmodeling.io.PLYAssetLoader;
import net.masonapps.vrsolidmodeling.mesh.Face;
import net.masonapps.vrsolidmodeling.mesh.MeshData;
import net.masonapps.vrsolidmodeling.mesh.MeshUtils;
import net.masonapps.vrsolidmodeling.mesh.PolyhedronUtils;
import net.masonapps.vrsolidmodeling.mesh.Triangle;
import net.masonapps.vrsolidmodeling.mesh.Vertex;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 1/3/2018.
 */

public class AssetPrimitive extends Primitive {
    protected final String name;
    protected final String asset;
    protected BVH bvh;
    protected boolean isInitialized = false;
    private BVH.IntersectionInfo intersectionInfo;
    private List<Face> faceList = new ArrayList<>();
    private List<Vertex> vertexList = new ArrayList<>();
    private Triangle[] triangles;

    public AssetPrimitive(String name, String asset) {
        this.name = name;
        this.asset = asset;
        intersectionInfo = new BVH.IntersectionInfo();
    }

    @Override
    public void initialize(InputStream inputStream) {
        try {
            PLYAssetLoader.parseFaceList(inputStream, false, vertexList, faceList);
            triangles = MeshData.fromFaces(faceList);
//            final BVH.Node root = new BVHBuilder().build(triangles);
            final BVH.Node root = new BVH.Group();
            bvh = new BVH(root);
            isInitialized = true;
        } catch (Exception e) {
            throw new RuntimeException("unable to load modelData for " + name, e);
        }
    }

    @Override
    public Mesh createMesh() {
        throwErrorIfNotInitialized();
        final Mesh mesh = new Mesh(true, vertexList.size(), triangles.length * 3, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        mesh.setVertices(MeshUtils.toVertices(vertexList.toArray(new Vertex[vertexList.size()]), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates));
        mesh.setIndices(MeshUtils.toIndices(triangles));
        return mesh;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getAsset() {
        return asset;
    }

    @Override
    public BoundingBox createBounds() {
        throwErrorIfNotInitialized();
        return new BoundingBox(bvh.root.bb);
    }

    @Override
    public PolyhedronsSet toPolyhedronsSet(Matrix4 transform) {
        throwErrorIfNotInitialized();
        return PolyhedronUtils.fromFaces(vertexList, faceList, transform);
    }

    @Override
    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final boolean intersection = bvh.closestIntersection(ray, intersectionInfo);
        if (intersection && hitPoint != null)
            hitPoint.set(intersectionInfo.hitPoint);
        return intersection;
    }

    private void throwErrorIfNotInitialized() {
        if (!isInitialized)
            throw new RuntimeException("primitive " + getName() + " must be initialized");
    }
}
