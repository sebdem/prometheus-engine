package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.tile.TileRegistry;

public class MinimalLevel2 {
    public WorldSpace setup() {
        WorldSpace worldSpace = new WorldSpace(16, 16);
        boolean useIsometric = (Boolean) PrometheusConfig.conf.getOrDefault("isometric", false);

        int borderWater = 15;
        for(float y = -borderWater; y < worldSpace.height + borderWater; y++) {
            for(float x = -borderWater; x < worldSpace.width + borderWater; x++) {
                if (x < 0 || x >= worldSpace.width ||  y < 0 || y >= worldSpace.height) {
                    worldSpace.placeTile(TileRegistry.idOfTag("dirt_0"), x, y, -3f);
                    worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -2f);
                    worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -1f);
                    worldSpace.placeTile(TileRegistry.idOfTag("water"), x, y, 0);
                } else if (x >= 0 && y >= 0) {
                    worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x, y, 0);
                }
            }
        }


        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                new CollisionBox(1f,1f,false),
                new SizeComponent(1f,1f),
                new PositionComponent(0f, 0f),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                SpriteComponent.fromFile(Gdx.files.internal(
                        (useIsometric) ?  "sprites/player/iso_test_01.png" : "sprites/player/test_01.png"
                ))
        );

        worldSpace.placeTile(TileRegistry.idOfTag("tree"), 2, 2, 1);
        worldSpace.placeTile(TileRegistry.idOfTag("treeS"), 2, 8, 1);



        // generate a mountain
        /*
        for(float z = 0; z < 10; z += 0.5f) {
            for (float y = 0; y < worldSpace.height; y++) {
                for (float x = 0; x < worldSpace.width; x++) {

                   // if (y == 5 && z < (-0.03125f * Math.sqrt(x - 5)) + 5) {
                    if (z < ((-0.125f * Math.pow((y - 8), 2)) + 6)&& z < ((-0.125f * Math.pow((x - 8), 2)) + 6)) {
                   // if (z < +0.25f * Math.sqrt(x - (worldSpace.width / 2f)) - 5) {
                       // for(int z2 = z; z2 != 0; z2)
                       // worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x, y, z-1);
                        worldSpace.placeTile(TileRegistry.idOfTag("dirt_0"), x, y, z-1);
                        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x, y, z);
                    }
                }
            }
        }*/

        float zz = 0;
        for (float x = 5; x < worldSpace.width; x++) {
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,6, zz-1);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,6, zz);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,6, zz+1);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,5, zz);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,4, zz);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,3, zz);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,2, zz-1);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,2, zz);
            worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x,2, zz+1);
            zz+= 1f;
        }
        SpriteComponent tree = SpriteComponent.fromFile(Gdx.files.internal("world/environment/"+ ((useIsometric) ?  "iso_" : "") +"tree.png"));

        return worldSpace;
    }
}
