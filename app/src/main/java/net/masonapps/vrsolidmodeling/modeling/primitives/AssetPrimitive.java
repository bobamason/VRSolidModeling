package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.bvh.BVH;
import net.masonapps.vrsolidmodeling.bvh.BVHBuilder;
import net.masonapps.vrsolidmodeling.io.PLYAssetLoader;
import net.masonapps.vrsolidmodeling.mesh.Face;
import net.masonapps.vrsolidmodeling.mesh.MeshData;
import net.masonapps.vrsolidmodeling.mesh.MeshInfo;
import net.masonapps.vrsolidmodeling.mesh.MeshUtils;
import net.masonapps.vrsolidmodeling.mesh.Triangle;
import net.masonapps.vrsolidmodeling.mesh.Vertex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 1/3/2018.
 */

public class AssetPrimitive extends Primitive {
    protected final String name;
    protected final String asset;
    @Nullable
    protected final String hullAsset;
    protected BVH bvh;
    protected boolean isInitialized = false;
    private BVH.IntersectionInfo intersectionInfo;
    private MeshInfo meshInfo = new MeshInfo();


    public AssetPrimitive(String name, String asset, @Nullable String hullAsset) {
        this.name = name;
        this.asset = asset;
        this.hullAsset = hullAsset;
        intersectionInfo = new BVH.IntersectionInfo();
    }

    @Override
    public void initialize(@NonNull InputStream meshStream, @Nullable InputStream hullStream) {
        try {
            List<Face> faceList = new ArrayList<>();
            List<Vertex> vertexList = new ArrayList<>();
            PLYAssetLoader.parseFaceList(meshStream, false, vertexList, faceList);

            meshInfo.vertexAttributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));

            final Triangle[] triangles = MeshData.fromFaces(faceList);

            meshInfo.vertices = MeshUtils.toVertices(vertexList.toArray(new Vertex[vertexList.size()]), meshInfo.vertexAttributes.getMask());
            meshInfo.indices = MeshUtils.toIndices(triangles);
            meshInfo.numVertices = vertexList.size();
            meshInfo.numIndices = triangles.length * 3;
            vertexList.clear();
            faceList.clear();

            final BVH.Node root;
            if (hullStream != null) {
                PLYAssetLoader.parseFaceList(hullStream, false, vertexList, faceList);
                final Triangle[] hullTriangles = MeshData.fromFaces(faceList);
                root = new BVHBuilder().build(hullTriangles);
            } else {
                root = new BVHBuilder().build(triangles);
            }
            bvh = new BVH(root);
            isInitialized = true;
        } catch (Exception e) {
            throw new RuntimeException("unable to load mesh info for " + name, e);
        }
    }

    @Override
    public MeshInfo getMeshInfo() {
        return meshInfo;
    }

    @Override
    public BVH getBVH() {
        return bvh;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getAsset() {
        return asset;
    }

    @Nullable
    public String getHullAsset() {
        return hullAsset;
    }

    @Override
    public BoundingBox createBounds() {
        throwErrorIfNotInitialized();
        return new BoundingBox(bvh.root.bb);
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
