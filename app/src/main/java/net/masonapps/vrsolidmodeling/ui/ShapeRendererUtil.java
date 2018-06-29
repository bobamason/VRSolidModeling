package net.masonapps.vrsolidmodeling.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;

import org.masonapps.libgdxgooglevr.gfx.AABBTree;

/**
 * Created by Bob Mason on 6/29/2018.
 */
public class ShapeRendererUtil {


    public static void drawBounds(ShapeRenderer shapeRenderer, BoundingBox bounds) {
        shapeRenderer.box(bounds.min.x, bounds.min.y, bounds.max.z,
                bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    public static void drawNodeBounds(ShapeRenderer shapeRenderer, AABBTree.AABBObject node, Color color) {
        shapeRenderer.setColor(color);
        final BoundingBox bounds = node.getAABB();
        drawBounds(shapeRenderer, bounds);
    }
}
