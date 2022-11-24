package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.tile.TileRegistry;

public class MinimalLevel {
    public WorldSpace setup() {
        WorldSpace worldSpace = new WorldSpace(4, 4);
        boolean useIsometric = (Boolean) PrometheusConfig.conf.getOrDefault("isometric", false);

        int borderWater = 1;
        for(float y = -borderWater; y < worldSpace.height + borderWater; y++) {
            for(float x = -borderWater; x < worldSpace.width + borderWater; x++) {
                if (x < 0 || x >= worldSpace.width ||  y < 0 || y >= worldSpace.height) {
                    worldSpace.placeTile(TileRegistry.idOfTag("water"), x, y, 0);
                } else if (x >= 0 && y >= 0) {
                    worldSpace.placeTile(TileRegistry.idOfTag("brickF"), x, y, 0);
                    if (x > 0) {
                        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x, y, 3);
                    }
                }
            }
        }
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 3, 3, 4);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 2, 3, 4);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 3, 3, 5);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 2, 3, 5);

        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                new CollisionBox(new Vector3(0.99f,0.99f,1.49f), false),
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


        //worldSpace.placeTile(TileRegistry.idOfTag("tree"), 2, 2, 1);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,0, 0.75f);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,1, 1.5f);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,2, 2.25f);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,3, 3f);

        return worldSpace;
    }
}
