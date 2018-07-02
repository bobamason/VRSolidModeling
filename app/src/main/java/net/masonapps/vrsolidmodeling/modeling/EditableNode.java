package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import net.masonapps.vrsolidmodeling.actions.TransformAction;
import net.masonapps.vrsolidmodeling.io.Base64Utils;
import net.masonapps.vrsolidmodeling.io.JsonUtils;
import net.masonapps.vrsolidmodeling.jcsg.CSG;
import net.masonapps.vrsolidmodeling.jcsg.Polygon;
import net.masonapps.vrsolidmodeling.mesh.MeshInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.masonapps.libgdxgooglevr.gfx.AABBTree;

import java.util.List;
import java.util.Map;

import eu.mihosoft.vvecmath.Transform;

/**
 * Created by Bob Mason on 2/9/2018.
 */

public class EditableNode extends Node implements AABBTree.AABBObject {

    public static final String KEY_PRIMITIVE = "primitive";
    public static final String KEY_MESH = "mesh";
    public static final String KEY_GROUP = "group";
    public static final String KEY_CHILDREN = "children";
    public static final String KEY_POLYGON_COUNT = "numPolygons";
    public static final String KEY_POLYGONS = "polygons";
    public static final String KEY_POSITION = "position";
    public static final String KEY_ROTATION = "rotation";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_AMBIENT = "ambient";
    public static final String KEY_DIFFUSE = "diffuse";
    public static final String KEY_SPECULAR = "specular";
    public static final String KEY_SHININESS = "shininess";
    protected final Matrix4 inverseTransform = new Matrix4();
    @Nullable
    protected final MeshInfo meshInfo;
    @Nullable
    private final PolygonAABBTree bvh;
    @Nullable
    private final CSG csg;
    private final Ray transformedRay = new Ray();
    private final String primitiveKey;
    private final boolean isGroup;
    protected BoundingBox bounds = new BoundingBox();
    private boolean updated = false;
    @Nullable
    private AABBTree.Node node = null;
    private BoundingBox aabb = new BoundingBox();
    private Color ambientColor = new Color(Color.GRAY);
    private Color diffuseColor = new Color(Color.GRAY);
    private Color specularColor = new Color(0x3f3f3fff);
    private float shininess = 8f;
    private Vector3 origin = new Vector3();

    public EditableNode() {
        super();
        meshInfo = null;
        csg = null;
        bvh = null;
        primitiveKey = KEY_GROUP;
        isGroup = true;
    }

    public EditableNode(@NonNull MeshInfo meshInfo, @NonNull CSG csg, @NonNull PolygonAABBTree bvh) {
        super();
        this.meshInfo = meshInfo;
        this.csg = csg;
        this.bvh = bvh;
        primitiveKey = KEY_MESH;
        isGroup = false;
    }

    public static EditableNode fromJSONObject(JSONObject jsonObject) throws JSONException {
        final String primitiveKey = jsonObject.optString(KEY_PRIMITIVE, KEY_MESH);
        final EditableNode editableNode;

        if (primitiveKey.equals(KEY_GROUP)) {
            editableNode = new EditableNode();
            final JSONArray children = jsonObject.getJSONArray(KEY_CHILDREN);
            if (children != null) {
                for (int i = 0; i < children.length(); i++) {
                    editableNode.addChild(fromJSONObject(children.getJSONObject(i)));
                }
            }
        } else {
            if (jsonObject.has(KEY_MESH)) {
                final List<Polygon> polygons = parsePolygons(jsonObject.getJSONObject(KEY_MESH));
                final CSG csg = CSG.fromPolygons(polygons);
                editableNode = new EditableNode(MeshInfo.fromPolygons(polygons), csg, new PolygonAABBTree(csg.hull().getPolygons()));
            } else {
                return null;
            }
        }
        final Color ambient = Color.valueOf(jsonObject.optString(KEY_AMBIENT, "000000FF"));
        final Color diffuse = Color.valueOf(jsonObject.optString(KEY_DIFFUSE, "7F7F7FFF"));
        final Color specular = Color.valueOf(jsonObject.optString(KEY_SPECULAR, "000000FF"));
        final float shininess = (float) jsonObject.optDouble(KEY_SHININESS, 8.);
        editableNode.setAmbientColor(ambient);
        editableNode.setDiffuseColor(diffuse);
        editableNode.setSpecularColor(specular);
        editableNode.setShininess(shininess);
        editableNode.translation.fromString(jsonObject.optString(KEY_POSITION, "(0.0,0.0,0.0)"));
        final String rotationString = jsonObject.optString(KEY_ROTATION, "(0.0,0.0,0.0,1.0)");
        editableNode.rotation.set(JsonUtils.quaternionFromString(rotationString));
        editableNode.scale.fromString(jsonObject.optString(KEY_SCALE, "(1.0,1.0,1.0)"));
        editableNode.calculateTransforms(true);
        return editableNode;
    }

    public static List<Polygon> parsePolygons(JSONObject jsonObject) throws JSONException {
        final String polygonsString = jsonObject.getString(KEY_POLYGONS);
        return Base64Utils.decodePolygons(polygonsString);
    }

    public boolean isGroup() {
        return isGroup;
    }

    protected Material createDefaultMaterial() {
        return new Material(ColorAttribute.createAmbient(ambientColor),
                ColorAttribute.createDiffuse(diffuseColor),
                ColorAttribute.createSpecular(specularColor),
                FloatAttribute.createShininess(shininess));
    }

    public void initMesh(Map<String, Mesh> meshes) {
        // TODO: 6/28/2018 recycle and cache meshes 
        if (isGroup || parts.size > 0 || meshInfo == null) return;

        final MeshPart meshPart = meshInfo.createMeshPart(new MeshBuilder());
        parts.add(new NodePart(meshPart, createDefaultMaterial()));
        origin.set(meshPart.center);
        bounds.set(bounds.min.set(meshPart.halfExtents).scl(-1).add(meshPart.center), bounds.max.set(meshPart.halfExtents).add(meshPart.center));
        invalidate();
    }

    @Nullable
    @Override
    public AABBTree.Node getNode() {
        return node;
    }

    @Override
    public void setNode(@Nullable AABBTree.Node node) {
        this.node = node;
    }

    @Override
    public BoundingBox getAABB() {
        validate();
        return aabb;
    }

    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        validate();
        boolean rayTest;
        transformedRay.set(ray).mul(inverseTransform);
//        if (isGroup || bvh == null)
            rayTest = Intersector.intersectRayBounds(transformedRay, bounds, intersection.hitPoint);
//        else
//            rayTest = bvh.rayTest(transformedRay, intersection);
        if (rayTest) {
            intersection.hitPoint.set(intersection.hitPoint).mul(getTransform());
            intersection.object = this;
            intersection.t = ray.origin.dst(intersection.hitPoint);
        }
        return rayTest;
    }

    public void validate() {
        if (!updated)
            calculateTransforms(true);
    }

    public void invalidate() {
        updated = false;
    }

    @Override
    public void calculateTransforms(boolean recursive) {
        super.calculateTransforms(recursive);
        try {
//            inverseTransform.set(-translation.x, -translation.y, -translation.z, -rotation.x, -rotation.y, -rotation.z, rotation.w, 1f / scale.x, 1f / scale.y, 1f / scale.z);
            inverseTransform.set(localTransform).inv();
        } catch (Exception ignored) {
        }
        if (isGroup && getChildCount() > 0) {
            bounds.inf();
            final Iterable<Node> children = getChildren();
            for (Node child : children) {
                if (child instanceof EditableNode)
                    bounds.ext(((EditableNode) child).getAABB());
            }
        }
        aabb.set(bounds).mul(localTransform);
        updated = true;
    }

    @Override
    public Matrix4 calculateLocalTransform() {
        localTransform.idt()
                .translate(translation)
                .rotate(rotation)
                .translate(-origin.x, -origin.y, -origin.z)
                .scale(scale.x, scale.y, scale.z);
        return localTransform;
    }

    @Override
    public EditableNode copy() {
        validate();
        final EditableNode node;
        if (primitiveKey.equals(KEY_MESH) && meshInfo != null && csg != null && bvh != null)
            node = new EditableNode(meshInfo, csg, bvh);
        else
            node = new EditableNode();
        node.translation.set(translation);
        node.rotation.set(rotation);
        node.scale.set(scale);
        node.localTransform.set(localTransform);
        node.globalTransform.set(globalTransform);
        if (isGroup) {
            final Iterable<Node> children = getChildren();
            for (Node child : children) {
                if (child instanceof EditableNode)
                    node.addChild(((EditableNode) child).copy());
            }
        }
        node.ambientColor.set(ambientColor);
        node.diffuseColor.set(diffuseColor);
        node.specularColor.set(specularColor);
        node.shininess = shininess;
        return node;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(Color color) {
        ambientColor.set(color);
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final ColorAttribute ambient = (ColorAttribute) material.get(ColorAttribute.Ambient);
        if (ambient != null)
            ambient.color.set(color);
        else
            material.set(ColorAttribute.createAmbient(color));
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Color color) {
        diffuseColor.set(color);
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final ColorAttribute diffuse = (ColorAttribute) material.get(ColorAttribute.Diffuse);
        if (diffuse != null)
            diffuse.color.set(color);
        else
            material.set(ColorAttribute.createDiffuse(color));
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Color color) {
        specularColor.set(color);
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final ColorAttribute specular = (ColorAttribute) material.get(ColorAttribute.Specular);
        if (specular != null)
            specular.color.set(color);
        else
            material.set(ColorAttribute.createSpecular(color));
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float value) {
        this.shininess = value;
        if (parts.size == 0) return;
        final Material material = parts.get(0).material;
        final FloatAttribute shininess = (FloatAttribute) material.get(FloatAttribute.Shininess);
        if (shininess != null)
            shininess.value = value;
        else
            material.set(FloatAttribute.createShininess(value));
    }

    public JSONObject toJSONObject() throws JSONException {
        validate();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_PRIMITIVE, primitiveKey);
        if (isGroup) {
            final Iterable<Node> children = getChildren();
            JSONArray jsonArray = new JSONArray();
            for (Node child : children) {
                if (child instanceof EditableNode)
                    jsonArray.put(((EditableNode) child).toJSONObject());
            }
            jsonObject.put(KEY_CHILDREN, jsonArray);
        } else if (primitiveKey.equals(KEY_MESH) && csg != null) {
            jsonObject.put(KEY_MESH, polygonsToJsonObject(csg.getPolygons()));
        }
        jsonObject.put(KEY_AMBIENT, getAmbientColor().toString());
        jsonObject.put(KEY_DIFFUSE, getDiffuseColor().toString());
        jsonObject.put(KEY_SPECULAR, getSpecularColor().toString());
        jsonObject.put(KEY_SHININESS, getShininess());
        jsonObject.put(KEY_POSITION, translation.toString());
        jsonObject.put(KEY_ROTATION, JsonUtils.quaternionToString(rotation));
        jsonObject.put(KEY_SCALE, scale.toString());
        return jsonObject;
    }

    private JSONObject polygonsToJsonObject(List<Polygon> polygons) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_POLYGON_COUNT, polygons.size());
        jsonObject.put(KEY_POLYGONS, Base64Utils.encode(polygons));
        return jsonObject;
    }

    /**
     * Methods needed to make it compatible with other code written earlier
     * |
     * |
     * |
     * |
     * \   |   /
     * \  |  /
     * \ | /
     * \|/
     */

    public Matrix4 getTransform(Matrix4 out) {
        validate();
        return out.set(localTransform);
    }

    public TransformAction.Transform getTransform(TransformAction.Transform out) {
        validate();
        out.position.set(getPosition());
        out.rotation.set(getRotation());
        out.scale.set(getScale());
        return out;
    }

    public Matrix4 getTransform() {
        validate();
        return localTransform;
    }

    public void setTransform(TransformAction.Transform transform) {
        setPosition(transform.position);
        setRotation(transform.rotation);
        final Vector3 s = transform.scale;
        setScale(s.x, s.y, s.z);
    }

    public EditableNode setScale(float x, float y, float z) {
        scale.set(x, y, z);
        invalidate();
        return this;
    }

    public EditableNode scaleX(float x) {
        scale.x *= x;
        invalidate();
        return this;
    }

    public EditableNode scaleY(float y) {
        scale.y *= y;
        invalidate();
        return this;
    }

    public EditableNode scaleZ(float z) {
        scale.z *= z;
        invalidate();
        return this;
    }

    public EditableNode scale(float s) {
        scale.scl(s, s, s);
        invalidate();
        return this;
    }

    public EditableNode scale(float x, float y, float z) {
        scale.scl(x, y, z);
        invalidate();
        return this;
    }

    public float getScaleX() {
        return this.scale.x;
    }

    public EditableNode setScaleX(float x) {
        scale.x = x;
        invalidate();
        return this;
    }

    public float getScaleY() {
        return this.scale.y;
    }

    public EditableNode setScaleY(float y) {
        scale.y = y;
        invalidate();
        return this;
    }

    public float getScaleZ() {
        return this.scale.z;
    }

    public EditableNode setScaleZ(float z) {
        scale.z = z;
        invalidate();
        return this;
    }

    public EditableNode setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
        return this;
    }

    public EditableNode setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
        return this;
    }

    public EditableNode setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
        return this;
    }

    public EditableNode rotateX(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public EditableNode rotateY(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public EditableNode rotateZ(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public EditableNode setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
        return this;
    }

    public EditableNode setRotation(Vector3 dir, Vector3 up) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
        Pools.free(tmp);
        Pools.free(tmp2);
        return this;
    }

    public EditableNode lookAt(Vector3 position, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(position).sub(this.translation).nor();
        setRotation(dir, up);
        Pools.free(dir);
        return this;
    }

    public Quaternion getRotation() {
        validate();
        return rotation;
    }

    public EditableNode setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
        return this;
    }

    public EditableNode translateX(float units) {
        this.translation.x += units;
        invalidate();
        return this;
    }

    public float getX() {
        return this.translation.x;
    }

    public EditableNode setX(float x) {
        this.translation.x = x;
        invalidate();
        return this;
    }

    public EditableNode translateY(float units) {
        this.translation.y += units;
        invalidate();
        return this;
    }

    public float getY() {
        return this.translation.y;
    }

    public EditableNode setY(float y) {
        this.translation.y = y;
        invalidate();
        return this;
    }

    public EditableNode translateZ(float units) {
        this.translation.z += units;
        invalidate();
        return this;
    }

    public float getZ() {
        return this.translation.z;
    }

    public EditableNode setZ(float z) {
        this.translation.z = z;
        invalidate();
        return this;
    }

    public EditableNode translate(float x, float y, float z) {
        this.translation.add(x, y, z);
        invalidate();
        return this;
    }

    public EditableNode translate(Vector3 translate) {
        this.translation.add(translate);
        invalidate();
        return this;
    }

    public EditableNode setPosition(float x, float y, float z) {
        this.translation.set(x, y, z);
        invalidate();
        return this;
    }

    public Vector3 getPosition() {
        validate();
        return translation;
    }

    public EditableNode setPosition(Vector3 pos) {
        this.translation.set(pos);
        invalidate();
        return this;
    }

    public Matrix4 getInverseTransform(Matrix4 out) {
        return out.set(inverseTransform);
    }

    public Matrix4 getInverseTransform() {
        return inverseTransform;
    }

    public boolean isUpdated() {
        return updated;
    }

    public Vector3 getScale() {
        validate();
        return scale;
    }

    public EditableNode setScale(float scale) {
        this.scale.set(scale, scale, scale);
        invalidate();
        return this;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    @Nullable
    public CSG getCSG() {
        return csg;
    }

    @Nullable
    public CSG getTransformedCSG() {
        if (csg == null) return null;
        return csg.transformed(Transform.unity()
                .translate(translation.x, translation.y, translation.z)
                .rot(rotation.getPitch(), rotation.getYaw(), rotation.getRoll())
                .scale(scale.x, scale.y, scale.z)
                .translate(-origin.x, -origin.y, -origin.z));
    }
}
