package net.masonapps.vrsolidmodeling.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public class AABBTree {

    public final InnerNode root;

    public AABBTree() {
        root = new InnerNode();
    }

    private static float surfaceArea(BoundingBox bb) {
        float x = bb.max.x - bb.min.x;
        float y = bb.max.y - bb.min.y;
        float z = bb.max.z - bb.min.z;
        return 2f * (y * z + x * z * x * y);
    }

    public void insert(AABBObject object) {
        root.insert(object);
    }

    public void remove(AABBObject object) {
        final LeafNode leafNode = object.getNode();
        if (leafNode != null && leafNode.parent instanceof InnerNode) {
            final InnerNode parent = (InnerNode) leafNode.parent;
            if (leafNode == parent.child1) {
                parent.child1 = null;
            } else if (leafNode == parent.child2) {
                parent.child2 = null;
            }
            parent.pruneInvalidNodes();
        }
    }

    public interface AABBObject {

        @Nullable
        LeafNode getNode();

        void setNode(@Nullable LeafNode node);

        BoundingBox getAABB();

        boolean rayTest(Ray ray, IntersectionInfo intersection);
    }

    public static abstract class Node {
        public final BoundingBox bb;
        @Nullable
        public Node parent = null;

        protected Node() {
            this.bb = new BoundingBox();
        }

        public abstract boolean rayTest(Ray ray, IntersectionInfo intersection);

        public abstract void refit();
    }

    public static class InnerNode extends Node {

        private static final BoundingBox tmpBB = new BoundingBox();
        @Nullable
        public Node child1;
        @Nullable
        public Node child2;

        protected InnerNode() {
            this(null, null);
        }

        protected InnerNode(Node child1, Node child2) {
            super();

            bb.inf();

            this.child1 = child1;
            if (this.child1 != null) {
                this.child1.parent = this;
                bb.ext(this.child1.bb);
            }

            this.child2 = child2;
            if (this.child2 != null) {
                this.child2.parent = this;
                bb.ext(this.child2.bb);
            }
        }

        private static float getExtendedSurfaceArea(Node node, BoundingBox aabb) {
            tmpBB.inf();
            tmpBB.ext(node.bb);
            tmpBB.ext(aabb);
            return surfaceArea(tmpBB);
        }

        public void insert(AABBObject object) {
            if (child1 == null) {
                child1 = new LeafNode(object);
                refit();
                return;
            }
            if (child2 == null) {
                child2 = new LeafNode(object);
                refit();
                return;
            }
            final BoundingBox aabb = object.getAABB();
            float surfaceArea1 = getExtendedSurfaceArea(child1, aabb);
            float surfaceArea2 = getExtendedSurfaceArea(child2, aabb);
            if (surfaceArea1 < surfaceArea2) {
                if (child1 instanceof InnerNode)
                    ((InnerNode) child1).insert(object);
                else if (child1 instanceof LeafNode) {
                    final Node tmp = child1;
                    child1 = new InnerNode(tmp, new LeafNode(object));
                }
            } else {
                if (child2 instanceof InnerNode)
                    ((InnerNode) child2).insert(object);
                else if (child2 instanceof LeafNode) {
                    final Node tmp = child2;
                    child2 = new InnerNode(tmp, new LeafNode(object));
                }
            }
            refit();
        }

        @Override
        public boolean rayTest(Ray ray, IntersectionInfo intersection) {
            if (child1 == null && child2 == null) return false;
            intersection.t = Float.POSITIVE_INFINITY;
            if (!Intersector.intersectRayBoundsFast(ray, bb))
                return false;
            final IntersectionInfo intersection1 = new IntersectionInfo();
            final IntersectionInfo intersection2 = new IntersectionInfo();
            final boolean hit1 = child1 != null && child1.rayTest(ray, intersection1);
            final boolean hit2 = child2 != null && child2.rayTest(ray, intersection2);
            if (hit1 && hit2) {
                if (intersection1.t < intersection2.t) {
                    intersection.object = intersection1.object;
                    intersection.hitPoint.set(intersection1.hitPoint);
                    intersection.t = intersection1.t;
                } else {
                    intersection.object = intersection2.object;
                    intersection.hitPoint.set(intersection2.hitPoint);
                    intersection.t = intersection2.t;
                }
                return true;
            } else if (hit1) {
                intersection.object = intersection1.object;
                intersection.hitPoint.set(intersection1.hitPoint);
                intersection.t = intersection1.t;
                return true;
            } else if (hit2) {
                intersection.object = intersection2.object;
                intersection.hitPoint.set(intersection2.hitPoint);
                intersection.t = intersection2.t;
                return true;
            }
            return false;
        }

        @Override
        public void refit() {
            bb.inf();
            if (child1 != null)
                bb.ext(child1.bb);
            if (child2 != null)
                bb.ext(child2.bb);
            if (parent != null)
                parent.refit();
        }

        private void pruneInvalidNodes() {
            if (child1 instanceof InnerNode) {
                if (((InnerNode) child1).child1 == null && ((InnerNode) child1).child2 == null)
                    child1 = null;
            }
            if (child2 instanceof InnerNode) {
                if (((InnerNode) child2).child1 == null && ((InnerNode) child2).child2 == null)
                    child2 = null;
            }
            if (parent instanceof InnerNode)
                ((InnerNode) parent).pruneInvalidNodes();
        }
    }

    public static class LeafNode extends Node {

        private final AABBObject object;

        protected LeafNode(AABBObject object) {
            super();
            bb.set(object.getAABB());
            this.object = object;
            this.object.setNode(this);
        }

        @Override
        public boolean rayTest(Ray ray, IntersectionInfo intersection) {
            if (!Intersector.intersectRayBoundsFast(ray, bb))
                return false;
            return object.rayTest(ray, intersection);
        }

        @Override
        public void refit() {
            bb.set(object.getAABB());
        }
    }

    public static class IntersectionInfo {
        public float t = -1f;
        public Vector3 hitPoint = new Vector3();
        @Nullable
        public AABBObject object = null;
    }
}
