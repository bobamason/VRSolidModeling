package net.masonapps.vrsolidmodeling.csg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import net.masonapps.jcsg.CSG;
import net.masonapps.jcsg.Polygon;
import net.masonapps.jcsg.Vertex;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Bob on 12/20/2017.
 */

public class ModelDataBuilder {

    public static ModelData fromCSG(CSG csg) {
        final List<Polygon> polygons = csg.getPolygons();
        ModelData data = new ModelData();

        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();

        final Vector3 pos = new Vector3();
        final Vector3 nor = new Vector3();

        int offset = 0;
        for (Polygon polygon : polygons) {
            final List<Vertex> verts = polygon.vertices;
            for (int i = 0; i < verts.size() - 2; i++) {
                final Vertex v0 = verts.get(0);
                final Vertex v1 = verts.get(i + 1);
                final Vertex v2 = verts.get(i + 2);

                ConversionUtil.toVector3(v0.pos, pos);
                vertices.add(pos.x);
                vertices.add(pos.y);
                vertices.add(pos.z);
                ConversionUtil.toVector3(v0.normal, nor);
                vertices.add(nor.x);
                vertices.add(nor.y);
                vertices.add(nor.z);

                ConversionUtil.toVector3(v1.pos, pos);
                vertices.add(pos.x);
                vertices.add(pos.y);
                vertices.add(pos.z);
                ConversionUtil.toVector3(v1.normal, nor);
                vertices.add(nor.x);
                vertices.add(nor.y);
                vertices.add(nor.z);

                ConversionUtil.toVector3(v2.pos, pos);
                vertices.add(pos.x);
                vertices.add(pos.y);
                vertices.add(pos.z);
                ConversionUtil.toVector3(v2.normal, nor);
                vertices.add(nor.x);
                vertices.add(nor.y);
                vertices.add(nor.z);

                indices.add(offset);
                indices.add(offset + i + 1);
                indices.add(offset + i + 2);
            }
            offset += verts.size();
        }

        Array<VertexAttribute> attributes = new Array<>();
        attributes.add(new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attributes.add(new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE));

        String nodeId = "node";
        String meshId = "mesh";
        String partId = "part";
        String materialName = "material";
        ModelNode node = new ModelNode();
        node.id = nodeId;
        node.meshId = meshId;
        node.scale = new Vector3(1, 1, 1);
        node.translation = new Vector3();
        node.rotation = new Quaternion();
        ModelNodePart pm = new ModelNodePart();
        pm.meshPartId = partId;
        pm.materialId = materialName;
        node.parts = new ModelNodePart[]{pm};
        ModelMeshPart part = new ModelMeshPart();
        part.id = partId;
        part.indices = indices.toArray();
        part.primitiveType = GL20.GL_TRIANGLES;
        ModelMesh mesh = new ModelMesh();
        mesh.id = meshId;
        mesh.attributes = attributes.toArray(VertexAttribute.class);
        mesh.vertices = vertices.toArray();
        mesh.parts = new ModelMeshPart[]{part};
        data.nodes.add(node);
        data.meshes.add(mesh);
        ModelMaterial mat = new ModelMaterial();
        mat.id = "mat1";
        mat.diffuse = new Color(Color.GREEN);
        data.materials.add(mat);

        return data;
    }

    public static CompletableFuture<ModelData> fromCsgAsync(final CSG csg) {
        return CompletableFuture.supplyAsync(() -> fromCSG(csg));
    }
}
