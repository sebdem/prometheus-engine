package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.tile.TileRegistry;

public class Superflat {

    public int width;
    public int height;

    public Superflat(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public WorldSpace setup() {
        WorldSpace worldSpace = new WorldSpace(width, height);
        for (float y = 0; y < width; y++) {
            for (float x = 0; x < width; x++) {
                worldSpace.placeTile(TileRegistry.getByTag("grass_0"), x, y, 0);
                worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), x, y, -1);
                worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), x, y, -2);
            }
        }
        Gdx.app.getApplicationLogger().log("WorldSetup", "Generating Entities");
        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                CollisionBox.createBasic(),
                new SizeComponent(1f,1f),
                new PositionComponent(worldSpace.getSpawnPoint()),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                new StateComponent(),
                new RenderComponent()
                        .registerAnimation(Gdx.files.internal("sprites/player/player_idle.png"
                        ), 8, 1, 1.25f, true, "default")
                        .registerAnimation(Gdx.files.internal(
                               "sprites/player/player_moving_down.png"
                        ), 8, 1, 0.125f, true, "moving")
        );
        return worldSpace;
    }
}
