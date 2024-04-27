package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;
import net.dermetfan.gdx.math.MathUtils;

public class Superflat {

    public int width;
    public int height;

    public Superflat(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public WorldSpace setup() {
        WorldSpace worldSpace = new WorldSpace(-width, -height, width, height);
        for (float y = worldSpace.minY; y < worldSpace.height; y++) {
            for (float x = worldSpace.minX; x < worldSpace.width; x++) {
                if (!(MathUtils.between(x, 10, 20) && MathUtils.between(y, 20, 28))){
                    worldSpace.placeTile(TileRegistry.getByTag("grass_0"), x, y, 0);
                    worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), x, y, -1);
                };
              //  worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), x, y, 8);
               // worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), x, y, 9);
                worldSpace.placeTile(TileRegistry.getByTag("stone"), x, y, -2);
                worldSpace.placeTile(TileRegistry.getByTag("stone"), x, y, -3);
                worldSpace.placeTile(TileRegistry.getByTag("stone"), x, y, -4);
            }
        }

        worldSpace.placeTile(TileRegistry.getByTag("stone_cliff_e"), 5, 5 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 6, 5 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_0"), 6, 5 , 2);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 7, 5 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_0"), 7, 5 , 2);

        worldSpace.placeTile(TileRegistry.getByTag("grass_ramp"), 5, 6 , 1, "east");
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 6, 6 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_0"), 6, 6 , 2);
        worldSpace.placeTile(TileRegistry.getByTag("grass_ramp"), 6, 6 , 2, "east");
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 7, 6 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_0"), 7, 6 , 2);

        worldSpace.placeTile(TileRegistry.getByTag("grass_ramp"), 5, 7 , 1, "east");
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 6, 7 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_ramp"), 6, 7 , 2, "east");
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 7, 7 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_0"), 7, 7 , 2);


        worldSpace.placeTile(TileRegistry.getByTag("stone_cliff_e"), 4, 8 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 5, 8 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_0"), 5, 8 , 2);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 6, 8 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("grass_0"), 6, 8 , 2);


        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 17, 17 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 17, 19 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 17, 17 , 2);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 17, 19 , 2);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 17, 17 , 3);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 17, 18 , 3);
        worldSpace.placeTile(TileRegistry.getByTag("dirt_0"), 17, 19 , 3);

        worldSpace.placeTile(TileRegistry.getByTag("slap"), 20, 5 , 1);
        worldSpace.placeTile(TileRegistry.getByTag("slap"), 0, 0 , 0);

        Gdx.app.getApplicationLogger().log("WorldSetup", "Generating Entities");
        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                new CollisionBox(new Vector3(0.99f,0.99f,1.49f).scl(0.75f), false),
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


        FileHandle[] blobTextures = new FileHandle[]{
                Gdx.files.internal("sprites/enemies/iso_blob_0.png"),
                Gdx.files.internal("sprites/enemies/iso_blob_1.png"),
                Gdx.files.internal("sprites/enemies/iso_blob_2.png")
        };

        for (int i = 0; i < 32; i++) {
            Vector3 pos = worldSpace.getSpawnPoint();
            worldSpace.placeTile(TileRegistry.getByTag("rock"), pos.x, pos.y, pos.z);
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    new PositionComponent(worldSpace.getSpawnPoint()),
                    new StateComponent(),
                    new RenderComponent().registerAnimation(GeneralUtils.randomElement(blobTextures), "default"),
                    //SpriteComponent.fromTexture(new Texture(blobTextures[(int)(Math.random() * blobTextures.length)])),
                    new VelocityComponent(0f,0f,0f),
                    new TargetTraverseComponent()
                    // new VelocityComponent((float)((Math.random() - 0.5f) * maxSpeed),(float)((Math.random() - 0.5f) * maxSpeed))
            );
        }

        return worldSpace;
    }
}
