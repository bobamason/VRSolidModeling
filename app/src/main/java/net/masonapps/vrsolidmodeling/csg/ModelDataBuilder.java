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

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;

/**
 * Created by Bob on 12/20/2017.
 */

public class ModelDataBuilder {

    public static ModelData fromPolyhedronsSet(PolyhedronsSet polyhedronsSet) {
        polyhedronsSet.getTree(true).visit(new BSPTreeVisitor<Euclidean3D>() {
            @Override
            public Order visitOrder(BSPTree<Euclidean3D> node) {
                return Order.MINUS_SUB_PLUS;
            }

            @Override
            public void visitInternalNode(BSPTree<Euclidean3D> node) {

            }

            @Override
            public void visitLeafNode(BSPTree<Euclidean3D> node) {

            }
        });
        ModelData data = new ModelData();

        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();

        final Vector3 pos = new Vector3();
        final Vector3 nor = new Vector3();

        // TODO: 1/3/2018 

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
        mat.id = materialName;
        mat.diffuse = new Color(Color.GREEN);
        data.materials.add(mat);

        return data;
    }
}
