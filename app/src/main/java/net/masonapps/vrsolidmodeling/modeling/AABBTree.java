package net.masonapps.vrsolidmodeling.modeling;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.Entity;

/**
 * Created by Bob Mason on 12/28/2017.
 */

public class AABBTree {

    public AABBTree() {
        root = new GroupNode();
    }

    public abstract class Node {
        public final BoundingBox bb;
        public Node parent = null;

        protected Node(BoundingBox bb) {
            this.bb = bb;
        }

        public abstract void rayTest(Ray ray);

        public abstract void insert(Entity entity);

        public abstract void remove(Entity entity);

        public abstract void refit();
    }


}
