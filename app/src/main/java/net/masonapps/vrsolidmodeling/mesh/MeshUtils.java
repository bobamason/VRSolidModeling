package net.masonapps.vrsolidmodeling.mesh;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 1/3/2018.
 */

public class MeshUtils {

    public static ModelData createModelData(float[] vertices, short[] indices, VertexAttribute... attributes) {
        final ModelData data = new ModelData();
        ModelNode node = new ModelNode();
        final String nodeId = "node";
        final String meshId = "mesh";
        final String partId = "part";
        final String materialName = "mat0";
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
        part.indices = indices;
        part.primitiveType = GL20.GL_TRIANGLES;
        ModelMesh mesh = new ModelMesh();
        mesh.id = meshId;
        mesh.attributes = attributes;
        mesh.vertices = vertices;
        mesh.parts = new ModelMeshPart[]{part};
        data.nodes.add(node);
        data.meshes.add(mesh);
        ModelMaterial mat = new ModelMaterial();
        mat.id = "mat0";
        mat.ambient = new Color(Color.WHITE);
        mat.diffuse = new Color(Color.WHITE);
        mat.opacity = 1f;
        data.materials.add(mat);
        return data;
    }
}
