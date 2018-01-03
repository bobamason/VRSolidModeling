package net.masonapps.vrsolidmodeling.bvh;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.vrsolidmodeling.mesh.MeshData;
import net.masonapps.vrsolidmodeling.mesh.Triangle;

import java.util.Arrays;

/**
 * Created by Bob Mason on 7/5/2017.
 */
public class BVH {
    public Node root;
    private MeshData meshData;

    public BVH(@NonNull MeshData meshData, BVHBuilder.Method method, int splitLimit) {
        this.meshData = meshData;
        this.root = new BVHBuilder(method, splitLimit).build(meshData);
    }

    public BVH(@NonNull MeshData meshData, BVH.Node root) {
        this.meshData = meshData;
        this.root = root;
    }

    private static boolean intersectSphereBounds(Vector3 center, float radius, BoundingBox bb) {
        return squareDistanceToBounds(center, bb) <= radius * radius;
    }

    private static float squareDistanceToBounds(Vector3 point, BoundingBox bb) {

        float dx = 0;
        if (point.x < bb.min.x) {
            dx = bb.min.x - point.x;
            dx *= dx;
        } else if (point.x > bb.max.x) {
            dx = point.x - bb.max.x;
            dx *= dx;
        }

        float dy = 0;
        if (point.y < bb.min.y) {
            dy = bb.min.y - point.y;
            dy *= dy;
        } else if (point.y > bb.max.y) {
            dy = point.y - bb.max.y;
            dy *= dy;
        }

        float dz = 0;
        if (point.z < bb.min.z) {
            dz = bb.min.z - point.z;
            dz *= dz;
        } else if (point.z > bb.max.z) {
            dz = point.z - bb.max.z;
            dz *= dz;
        }
        final float sqDist = dx + dy + dz;
        return sqDist;
    }

    private static BoundingBox bb(Triangle[] triangles) {

        final BoundingBox boundingBox = new BoundingBox();
        boundingBox.inf();
        if (triangles.length == 0)
            boundingBox.clr();
        for (Triangle triangle : triangles) {
            triangle.extendBounds(boundingBox);
        }
        return boundingBox;
    }

    /**
     * Find closest intersection between the ray and any object in the BVH.
     *
     * @return {@code true} if there exists any intersection
     */
    public boolean closestIntersection(Ray ray, IntersectionInfo intersection) {
        return Intersector.intersectRayBoundsFast(ray, root.bb) && root.closestIntersection(ray, intersection);
    }

    /**
     * Find any intersection between the ray and any object in the BVH. For simple
     * intersection tests this method is quicker. The closest point search costs a
     * bit more.
     *
     * @return {@code true} if there exists any intersection
     */
    public boolean anyIntersection(Ray ray) {
        return Intersector.intersectRayBoundsFast(ray, root.bb) && root.anyIntersection(ray);
    }

    public MeshData getMeshData() {
        return meshData;
    }

    public void setMeshData(MeshData meshData) {
        this.meshData = meshData;
    }

    public void refit() {
        root.refit();
    }

    public static abstract class Node {
        public final BoundingBox bb;
        public final Triangle[] triangles;
        @Nullable
        protected Node parent = null;
        private boolean needsRefit;

        /**
         * Create a new BVH node.
         */
        public Node(Triangle[] triangles) {
            this.bb = bb(triangles);
            this.triangles = triangles;
        }

        /**
         * Create new BVH node with specific extendBounds.
         */
        public Node(BoundingBox bb, Triangle[] triangles) {
            this.bb = bb;
            this.triangles = triangles;
        }

        abstract public boolean closestIntersection(Ray ray, IntersectionInfo intersection);

        abstract public boolean anyIntersection(Ray ray);

        abstract public void refit();

        abstract public int size();

        public void flagNeedsRefit() {
            needsRefit = true;
            if (parent != null) parent.flagNeedsRefit();
        }

        public void clearRefit() {
            needsRefit = false;
        }

        public boolean needsRefit() {
            return needsRefit;
        }
    }

    public static class Group extends Node {
        private final int numPrimitives;
        public Node child1;
        public Node child2;

        /**
         * Create a new BVH node.
         */
        public Group() {
            super(new BoundingBox(), new Triangle[0]);
            this.numPrimitives = 0;
        }

        public Group(Node child1, Node child2) {
            super(new BoundingBox(child1.bb).ext(child2.bb), new Triangle[0]);
            this.numPrimitives = child1.size() + child2.size();
            this.child1 = child1;
            this.child2 = child2;
            this.child1.parent = this;
            this.child2.parent = this;
        }

        @Override
        public boolean closestIntersection(Ray ray, IntersectionInfo intersection) {
            intersection.t = Float.POSITIVE_INFINITY;
            if (!Intersector.intersectRayBoundsFast(ray, bb))
                return false;
            final IntersectionInfo intersection1 = new IntersectionInfo();
            final IntersectionInfo intersection2 = new IntersectionInfo();
            final boolean hit1 = child1.closestIntersection(ray, intersection1);
            final boolean hit2 = child2.closestIntersection(ray, intersection2);
            if (hit1 && hit2) {
//                Log.d(Group.class.getSimpleName() + ".closestIntersection", "hit1 && hit2");
                if (intersection1.t < intersection2.t) {
                    intersection.triangle = intersection1.triangle;
                    intersection.hitPoint.set(intersection1.hitPoint);
                    intersection.t = intersection1.t;
                } else {
                    intersection.triangle = intersection2.triangle;
                    intersection.hitPoint.set(intersection2.hitPoint);
                    intersection.t = intersection2.t;
                }
                return true;
            } else if (hit1) {
//                Log.d(Group.class.getSimpleName()  + ".closestIntersection", "hit1");
                intersection.triangle = intersection1.triangle;
                intersection.hitPoint.set(intersection1.hitPoint);
                intersection.t = intersection1.t;
                return true;
            } else if (hit2) {
//                Log.d(Group.class.getSimpleName() + ".closestIntersection", "hit2");
                intersection.triangle = intersection2.triangle;
                intersection.hitPoint.set(intersection2.hitPoint);
                intersection.t = intersection2.t;
                return true;
            }
//            Log.d(Group.class.getSimpleName() + ".closestIntersection", "hit none");
            return false;
        }

        @Override
        public boolean anyIntersection(Ray ray) {
            return (Intersector.intersectRayBoundsFast(ray, child1.bb) && child1.anyIntersection(ray)) || (Intersector.intersectRayBoundsFast(ray, child2.bb) && child2.anyIntersection(ray));
        }

        @Override
        public void refit() {
            if (triangles.length > 0)
                Arrays.stream(triangles)
                        .filter(Triangle::needsUpdate)
                        .forEach(Triangle::update);
            bb.inf();
            if (child1.needsRefit) child1.refit();
            if (child2.needsRefit) child2.refit();
            bb.ext(child1.bb);
            bb.ext(child2.bb);
            clearRefit();
        }

        @Override
        public int size() {
            return numPrimitives;
        }
    }

    public static class Leaf extends Node {

        public Leaf(Triangle[] triangles) {
            super(triangles);
        }

        @Override
        public boolean closestIntersection(Ray ray, IntersectionInfo intersection) {
            boolean hit = false;
            intersection.t = Float.POSITIVE_INFINITY;
            final Vector3 hitPoint = new Vector3();
            for (Triangle triangle : triangles) {
                if (triangle.intersect(ray, hitPoint)) {
                    float dst = ray.origin.dst(hitPoint);
                    if (dst < intersection.t) {
                        intersection.t = dst;
                        intersection.hitPoint.set(hitPoint);
                        intersection.triangle = triangle;
                        hit = true;
                    }
                }
            }
//            Log.d(Leaf.class.getSimpleName() + ".closestIntersection", "hit = " + hit);
            return hit;
        }

        @Override
        public boolean anyIntersection(Ray ray) {
            for (Triangle triangle : triangles) {
                if (triangle.intersect(ray)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int size() {
            return triangles.length;
        }

        @Override
        public void refit() {
            bb.inf();
            Arrays.stream(triangles)
                    .filter(Triangle::needsUpdate)
                    .forEach(Triangle::update);
            for (Triangle triangle : triangles) {
                triangle.extendBounds(bb);
            }
            clearRefit();
        }
    }

    public static class IntersectionInfo {
        public float t = -1f;
        public Vector3 hitPoint = new Vector3();
        @Nullable
        public Triangle triangle = null;
    }
}