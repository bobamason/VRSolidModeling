package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.bvh.BVH;
import net.masonapps.vrsolidmodeling.bvh.BVHBuilder;
import net.masonapps.vrsolidmodeling.io.PLYAssetLoader;
import net.masonapps.vrsolidmodeling.mesh.MeshData;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 1/3/2018.
 */

public class AssetPrimitive extends Primitive {
    private final String name;
    private final String fileName;
    private BoundingBox boundingBox;
    private com.badlogic.gdx.graphics.g3d.model.data.ModelData modelData;
    private BVH bvh;
    private BVH.IntersectionInfo intersectionInfo;

    public AssetPrimitive(String name, String fileName) {
        this.name = name;
        this.fileName = fileName;
        intersectionInfo = new BVH.IntersectionInfo();
    }

    @Override
    public void initialize(File dir) {
        try {
            modelData = PLYAssetLoader.parse(new File(dir, fileName), false);
            final MeshData meshData = MeshData.fromModelData(modelData);
            final BVH.Node root = new BVHBuilder().build(meshData);
            bvh = new BVH(meshData, root);
        } catch (IOException e) {
            throw new RuntimeException("unable to load modelData for " + name, e);
        }
    }

    @Override
    public Model createModel() {
        return new Model(modelData);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BoundingBox createBounds() {
        return new BoundingBox(boundingBox);
    }

    @Override
    public PolyhedronsSet toPolyhedronsSet(Matrix4 transform) {
        final List<Vector3D> vertices = new ArrayList<>();
        final List<int[]> facets = new ArrayList<>();
        return new PolyhedronsSet(vertices, facets, 1e-10);
    }

    @Override
    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        final boolean intersection = bvh.closestIntersection(ray, intersectionInfo);
        if (intersection && hitPoint != null)
            hitPoint.set(intersectionInfo.hitPoint);
        return intersection;
    }
}
