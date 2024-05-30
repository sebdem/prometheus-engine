package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.PlaceFeature;
import dbast.prometheus.engine.world.generation.features.CastleTower;
import dbast.prometheus.engine.world.generation.features.Mountain;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;

import java.util.Arrays;
import java.util.List;

public class TestLevel {

    public WorldSpace setup() {
        WorldSpace worldSpace = new WorldSpace(64, 64);
        boolean useIsometric = (Boolean) PrometheusConfig.conf.getOrDefault("isometric", false);

        /* for(float z = -1f; z < 2; z++ ) {
            for(float y = 0; y < worldSpace.height; y++) {
                for(float x = 0; x < worldSpace.width; x++) {
                    switch ((int)z) {
                        case -1:
                            worldSpace.placeTile(0, x, y, z); break;
                        case 0: {
                            if (x > 0 && x +1 < worldSpace.width) {
                                int tileId = Math.random() > 0.5 ? 2 : 1;

                                worldSpace.placeTile(tileId, x, y, z);
                            }
                        } break;
                        case 1: {
                            // if (y + 2 == worldSpace.height) {
                             if (Math.sqrt(y*y) == Math.sqrt(x*x)) {
                                worldSpace.placeTile(3, x, y, z);
                            }
                        }
                    }
                }
            }
        }*/

        // Step 1 Fill everything with water
        for(float y = 0; y < worldSpace.height; y++) {
            for (float x = 0; x < worldSpace.width; x++) {
                worldSpace.placeTile(TileRegistry.idOfTag("dirt_0"), x, y, -3f);
                worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -2f);
                worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -1f);

                worldSpace.placeTile(TileRegistry.idOfTag("water"), x, y, 0f);
            }
        }

        PlaceFeature placePyramid = (world, x, y, z) -> {
            int scale = (int) (Math.random() * 10);
            for(float z2 = z; z2 <= scale; z2 ++) {
                for (float y2 = -scale; y2 <= scale; y2++) {
                    for (float x2 = -scale; x2 <= scale; x2++) {
                        //  world.placeTile(TileRegistry.idOfTag("dirt_0"), x+x2, y+y2, z+z2);
                        // if (y2 == -scale+z2 || x2 == -scale+z2) {
                        // if (z > 0 && (x2 == -1+z2 || y2 == -1+z2)) {
                        if((-scale + z2 < y2 && y2 < scale - z2) && (-scale + z2 < x2 && x2 < scale - z2) ) {
                            world.placeTile(TileRegistry.idOfTag("dirt_0"), x+x2, y+y2, z+z2-1);
                            world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z+z2);
                        }
                    }
                }
            }
        };

        List<PlaceFeature> features = Arrays.asList(
                (world, x, y, z) -> {
                    // place dirtblock
                    world.placeTile(TileRegistry.idOfTag("dirt_0") , x, y, z);
                },
                (world, x, y, z) -> {
                    // place grassblock
                    world.placeTile(TileRegistry.idOfTag("grass_0"), x, y, z);
                },
                (world, x, y, z) -> {
                    // place "tower"
                    world.placeTile(TileRegistry.idOfTag("grass_0"), x, y, z+1);
                    world.placeTile(TileRegistry.idOfTag("dirt_0"), x, y, z);
                },
                new Mountain("grass_0"),
                (world, x, y, z) -> {
                    // place plain
                    int scale = 2+ (int) (Math.random() * 10);
                    float half = (float) Math.floor(scale *0.5f);
                    float offX = 0f, offY = 0f;
                    for (float y2 = -(half +1); y2 <= (half + 1); y2++) {
                        offY = (float)((4*Math.random()) *0.125f);
                        for (float x2 = -(half +1); x2 <= (half + 1); x2++) {
                            offX = (float)((4*Math.random()) *0.125f);
                            world.removeTile(x+x2, y+y2, z);
                            if (x2 >= -half && x2 <= half && y2 >= -half && y2 <= half) {
                                // world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, (float) (z + ((8*Math.random()) *0.125f)));
                                world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, (float) (z + (offY + offX / 2)));
                            } else {
                                world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z);
                            }
                        }
                    }
                },
                (world, x, y, z) -> {
                    // place hole
                    int scale = 2+(int) (Math.random() * 10);
                    float half = (float) Math.floor(scale *0.5f);
                    for (float y2 = -(half +1); y2 <= (half + 1); y2++) {
                        for (float x2 = -(half +1); x2 <= (half + 1); x2++) {
                            world.removeTile(x+x2, y+y2, z);
                            if (x2 >= -half && x2 <= half && y2 >= -half && y2 <= half) {
                                world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z-1);
                            } else {
                                world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z);
                            }
                        }
                    }
                },
                (world, x, y, z) -> {
                    // place puddle
                    int scale = 2+(int) (Math.random() * 10);
                    float half = (float) Math.floor(scale *0.5f);
                    for (float y2 = -(half +1); y2 <= (half + 1); y2++) {
                        for (float x2 = -(half +1); x2 <= (half + 1); x2++) {
                            world.removeTile(x+x2, y+y2, z);
                            if (x2 >= -half && x2 <= half && y2 >= -half && y2 <= half) {
                                world.placeTile(TileRegistry.idOfTag("water"), x+x2, y+y2, z);
                            } else {
                                world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z);
                            }
                        }
                    }
                }
        );
        features = Arrays.asList(
                new Mountain("grass_0"), new CastleTower("brickF"));
        // Step 2 go over terrain and randomly place features

        for(int i = 0; i <= 5; i++) {
            float y = (float) Math.floor((Math.random()*worldSpace.height));
            float x = (float) Math.floor((Math.random()*worldSpace.width));
            float z = 0f;
            int feature = (int) (Math.random() * features.size());

            features.get(feature).place(worldSpace, x, y, z);
        }
        /*for(float y = 0; y < worldSpace.height; y++) {
            for (float x = 0; x < worldSpace.width; x++) {
                if (Math.random() * 15 < 5) {
                    worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), x, y, 0);
                    if (Math.random() * 60 < 5) {
                        int feature = (int) (Math.random() * features.size());
                        features.get(feature).place(worldSpace, x, y, 0f);
                    }
                }
            }
        }*/

        // Step 3 smooth the shit out of it...
        boolean skipStep3 = true;
        if (!skipStep3) {

            int maxTerrainCycles = 64;
            for(int tCycle = 1; tCycle <= maxTerrainCycles; tCycle++) {
                float z = 0f;
                for(float y = 0; y < worldSpace.height; y++) {
                    for(float x = 0; x < worldSpace.width; x++) {
                        // worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -1f);
                        //worldSpace.placeTile(TileRegistry.idOfTag("water"), x, y, 0);
                        Vector2[] nearbys = new Vector2[]{
                                new Vector2(x-1,y),
                                new Vector2(x+1,y),
                                new Vector2(x,y-1),
                                new Vector2(x,y+1),
                        };
                        Tile tile = worldSpace.lookupTile(x, y, z);

                        if (tile != null && tile.tag.equals("water")) {
                            for(Vector2 nearbyTile : nearbys) {
                                if (Math.random() < 0.5f) {
                                    Tile tile2 = worldSpace.lookupTile(nearbyTile.x, nearbyTile.y, z);
                                    if (tile2 != null && tile2.tag.equals("water")) {
                                        int feature = (int) (Math.random() * features.size());
                                        if(feature == 2 && Math.random() > 0.33) {
                                            feature -= 1;
                                        }
                                        features.get(feature).place(worldSpace, x, y, 0f);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                new CollisionBox(new Vector3(0.99f,0.99f,1.49f).scl(0.75f), false),
                new SizeComponent(1f,1f),
                new PositionComponent(new Vector3(0,0,10)),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                new StateComponent(),
                RenderComponent.playerRenderComponent()
        );

        RenderComponent[] blobs = new RenderComponent[]{
                new RenderComponent().registerAnimation(Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_0.png"), "default"),
                new RenderComponent().registerAnimation(Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_1.png"), "default"),
                new RenderComponent().registerAnimation(Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_2.png"), "default")
        };
        for(int i = 0; i < 100; i++) {
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    new PositionComponent(worldSpace.getSpawnPoint()),
                    blobs[(int)(Math.random() * 3)],
                    new VelocityComponent((float)((Math.random() * 3) - 1f),(float)((Math.random() * 3) - 1f))
            );
        }


        for(int i = 0; i < 20; i++) {
            /*SpriteComponent tree = SpriteComponent.fromFile(Gdx.files.internal("world/environment/"+ ((useIsometric) ?  "iso_" : "") +"tree.png"));
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic().setPermeable(false),
                    new SpriteBoundSizeComponent(32f),
                    new PositionComponent((float)Math.floor(Math.random() * worldSpace.width), (float)Math.floor(Math.random() * worldSpace.height)),
                    PositionComponent.initial(),
                    tree
            );*/
            float treeX = (float)Math.floor(Math.random() * worldSpace.width);
            float treeY = (float)Math.floor(Math.random() * worldSpace.height);

            worldSpace.placeTile( TileRegistry.idOfTag("tree"), treeX, treeY, 1f);
        }

        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(4f, 1f, 2f),
                new StateComponent(),
                new RenderComponent()
                        .registerAnimation(Gdx.files.internal("world/objects/iso/chest_active.png"), "default")
                        .registerAnimation(Gdx.files.internal("world/objects/iso/chest_open.png"), 3, 1, 0.25f, false, "colliding" )
                        .registerAnimation(Gdx.files.internal("world/objects/iso/chest_open.png"), 3, 1, 0.25f, false, "open")
        );

        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(6f, 4f, 3f),
                SpriteComponent.fromFile(Gdx.files.internal("world/objects/iso/chest_active.png"))
        );*/

        // generate structures...
        int brickId = TileRegistry.idOfTag("brickF");
       /* for(float y = 20; y < 33; y++) {
            for(float x = 20; x < 36; x++) {
                worldSpace.placeTile(brickId, x, y, 0);
                if (x % 5 == 0 || y % 4 == 0) {
                    if (Math.random() * 6 > 1) {
                        worldSpace.placeTile( TileRegistry.idOfTag("brickF"), x, y, 1);
                        worldSpace.placeTile( TileRegistry.idOfTag("brickF"), x, y, 2);
                    }

                    worldSpace.placeTile( TileRegistry.idOfTag("brickF"), x, y, 3);
                }
            }
        }*/

        return worldSpace;
    }
}
