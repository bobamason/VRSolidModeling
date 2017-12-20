package net.masonapps.vrsolidmodeling.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import net.masonapps.vrsolidmodeling.SolidModelingGame;

import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.gfx.World;

/**
 * Created by Bob on 12/28/2016.
 */
public abstract class RoomScreen extends VrWorldScreen {
    private final SolidModelingGame solidModelingGame;

    public RoomScreen(VrGame game) {
        super(game);
        if (!(game instanceof SolidModelingGame))
            throw new IllegalArgumentException("game must be SculptingVrGame");
        solidModelingGame = (SolidModelingGame) game;
    }

    @Override
    protected Environment createEnvironment() {
        final Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.DARK_GRAY));
        return environment;
    }

    @Override
    protected World createWorld() {
        return new World() {
            @Override
            public void render(ModelBatch batch, Environment lights) {
                final ModelInstance roomInstance = solidModelingGame.getRoomInstance();
                if (roomInstance != null)
                    batch.render(roomInstance, getEnvironment());
                super.render(batch, lights);
            }
        };
    }

    public SolidModelingGame getSolidModelingGame() {
        return solidModelingGame;
    }

    public abstract void onControllerBackButtonClicked();
}
