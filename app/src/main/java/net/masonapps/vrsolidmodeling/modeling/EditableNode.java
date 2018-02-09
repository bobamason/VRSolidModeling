package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob Mason on 2/9/2018.
 */

public class EditableNode extends Node implements AABBTree.AABBObject {

    protected final Matrix4 inverseTransform = new Matrix4();
    private boolean updated = false;
    @Nullable
    private AABBTree.LeafNode leafNode = null;
    private BoundingBox bounds = new BoundingBox();
    private BoundingBox aabb = new BoundingBox();

    public EditableNode(final Mesh mesh, final Material material) {
        super();
        final MeshPart meshPart = new MeshPart();
        meshPart.id = "part";
        meshPart.primitiveType = GL20.GL_TRIANGLES;
        meshPart.mesh = mesh;
        meshPart.offset = 0;
        meshPart.size = mesh.getNumIndices();
        parts.add(new NodePart(meshPart, material));
        bounds.inf();
        extendBoundingBox(bounds, false);
    }

    @Nullable
    @Override
    public AABBTree.LeafNode getNode() {
        return leafNode;
    }

    @Override
    public void setNode(@Nullable AABBTree.LeafNode node) {
        leafNode = node;
    }

    @Override
    public BoundingBox getAABB() {
        return aabb;
    }

    @Override
    public boolean rayTest(Ray ray, AABBTree.IntersectionInfo intersection) {
        validate();
        final boolean intersectRayBounds = Intersector.intersectRayBounds(ray, getAABB(), intersection.hitPoint);
        if (intersectRayBounds) {
            intersection.object = this;
            intersection.t = ray.origin.dst(intersection.hitPoint);
        }
        return intersectRayBounds;
    }

    public void validate() {
        if (!updated) {
            calculateTransforms(false);
        }
    }

    public void invalidate() {
        updated = false;
    }

    @Override
    public void calculateTransforms(boolean recursive) {
        super.calculateTransforms(recursive);
        aabb.set(bounds).mul(localTransform);
        updated = true;
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

    public Matrix4 getTransform() {
        validate();
        return localTransform;
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
}
