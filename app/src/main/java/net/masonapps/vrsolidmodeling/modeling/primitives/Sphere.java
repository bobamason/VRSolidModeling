package net.masonapps.vrsolidmodeling.modeling.primitives;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;

import java.io.InputStream;

/**
 * Created by Bob on 1/2/2018.
 */

public class Sphere extends Primitive {
    private BoundingBox boundingBox;
    private com.badlogic.gdx.graphics.g3d.model.data.ModelData modelData;

    public Sphere() {
        boundingBox = new BoundingBox(new Vector3(-1, -1, -1), new Vector3(1, 1, 1));
    }
    
    @Override
    public void initialize(InputStream inputStream) {
//        final MeshBuilder meshBuilder = new MeshBuilder();
//        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
//        SphereShapeBuilder.build(meshBuilder, 2f, 2f, 2f, 24, 12);
//        final float[] vertices = new float[meshBuilder.getNumVertices()];
//        meshBuilder.getVertices(vertices, 0);
//        final short[] indices = new short[meshBuilder.getNumIndices()];
//        meshBuilder.getIndices(indices, 0);
//        modelData = MeshUtils.createModelData(vertices, indices, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
    }

    @Override
    public Model createModel() {
//        return new Model(modelData);
        return new ModelBuilder().createSphere(2f, 2f, 2f, 24, 12, new Material(ColorAttribute.createDiffuse(Color.WHITE)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    @Override
    public String getName() {
        return Primitives.KEY_SPHERE;
    }

    @Override
    public BoundingBox createBounds() {
        return new BoundingBox(boundingBox);
    }

    @Override
    public PolyhedronsSet toPolyhedronsSet(Matrix4 transform) {
        return null;
    }

    @Override
    public boolean rayTest(Ray ray, @Nullable Vector3 hitPoint) {
        return Intersector.intersectRaySphere(ray, Vector3.Zero, 1f, hitPoint);
    }
}
