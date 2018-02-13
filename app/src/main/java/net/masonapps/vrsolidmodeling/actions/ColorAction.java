package net.masonapps.vrsolidmodeling.actions;

import com.badlogic.gdx.graphics.Color;

import net.masonapps.vrsolidmodeling.modeling.EditableNode;

import java.util.function.Consumer;

/**
 * Created by Bob Mason on 2/1/2018.
 */

public class ColorAction extends Action {

    private final Color oldColor;
    private final Color newColor;
    private final Consumer<Color> consumer;

    public ColorAction(EditableNode node, Color oldColor, Color newColor, Consumer<Color> consumer) {
        super(node);
        this.oldColor = oldColor;
        this.newColor = newColor;
        this.consumer = consumer;
    }

    @Override
    public void redoAction() {
        getNode().setDiffuseColor(newColor);
        consumer.accept(newColor);
    }

    @Override
    public void undoAction() {
        getNode().setDiffuseColor(oldColor);
        consumer.accept(oldColor);
    }
}
