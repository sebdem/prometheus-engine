package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.Vector3Comparator;

import java.util.*;
import java.util.Vector;

// TODO WorldSpace builder from a level file
public class WorldSpace {

    public Map<Vector3, Tile> terrainTiles;
    public int height;
    public int width;

    public EntityRegistry entities;

    public WorldSpace(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new TreeMap<>(new Vector3Comparator.Planar());
    }

    public WorldSpace placeTile(int tileId, float x, float y, float z) {
        this.terrainTiles.put(new Vector3(x, y, z), TileRegistry.get(tileId));
        return this;
    }
    public Tile lookupTile(float x, float y, float z) {
        return this.terrainTiles.get(new Vector3(x, y, z));
    }


    public WorldSpace removeTile(float x, float y, float z) {
        this.terrainTiles.remove(new Vector3(x, y, z));
        return this;
    }

    public static WorldSpace minimalLevel() {
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

        /*
        for(int i = 0; i < Math.sqrt(worldSpace.width + worldSpace.height); i++) {
            float treeX = (float)Math.floor(Math.random() * (worldSpace.width- 2)) + 2;
            float treeY = (float)Math.floor(Math.random() * (worldSpace.height - 2)) + 2;

            Gdx.app.getApplicationLogger().log("World Creation", String.format("Spawning tree at %s / %s", treeX,treeY ));
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic().setPermeable(false),
                    new SpriteBoundSizeComponent(32f),
                    new PositionComponent(treeX, treeY),
                    tree
            );
        }*/

        return worldSpace;
    }

    public static WorldSpace testLevel() {
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
                /*worldSpace.placeTile(TileRegistry.idOfTag("dirt_0"), x, y, -4f);
                worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -3f);
                worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -2f);
                worldSpace.placeTile(TileRegistry.idOfTag("waterD"), x, y, -1f);
                */
                worldSpace.placeTile(TileRegistry.idOfTag("water"), x, y, 0f);
            }
        }

        PlaceFeature placePyramid = (world, x, y, z) -> {
            int scale = 5;
            for(float z2 = -1; z2 <= scale; z2++) {
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
                }/*,

                (world, x, y, z) -> {
                    // place "tower2"
                    world.placeTile(TileRegistry.idOfTag("grass_0"), x, y, z+2);
                    world.placeTile(TileRegistry.idOfTag("dirt_0"), x, y, z+1);
                    world.placeTile(TileRegistry.idOfTag("dirt_0"), x, y, z);
                }*/
               /*,// placePyramid,
                (world, x, y, z) -> {
                    // place plain
                    for (float y2 = -2; y2 < 2; y2++) {
                        for (float x2 = -2; x2 < 2; x2++) {
                            world.removeTile(x+x2, y+y2, z);
                            world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z);
                        }
                    }
                },
                (world, x, y, z) -> {
                    // place hole
                    for (float y2 = -1; y2 < 1; y2++) {
                        for (float x2 = -1; x2 < 1; x2++) {
                            world.removeTile(x+x2, y+y2, z);
                            world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z-1);
                        }
                    }
                }*/
        );
        // Step 2 go over terrain and randomly place features

        for(int i = 0; i <= 20; i++) {
            float y = (float) Math.floor((Math.random()*worldSpace.height));
            float x = (float) Math.floor((Math.random()*worldSpace.width));

            int feature = (int) (Math.random() * features.size());
            features.get(feature).place(worldSpace, x, y, 0f);
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

        Texture[] blobTextures = new Texture[]{
                    new Texture(Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_0.png")),
                    new Texture(Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_1.png")),
                    new Texture(Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_2.png"))
        };
        for(int i = 0; i < 100; i++) {
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    PositionComponent.initial(),
                    SpriteComponent.fromTexture(blobTextures[(int)(Math.random() * 3)]),
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

        StateComponent stateComponent =  new StateComponent();
        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(4f, 1f),
                stateComponent,

                StateBasedSpriteComponent
                        /*
                        .fromFile(Gdx.files.internal("world/objects/chest_locked.png"))
                        .addState("colliding", Gdx.files.internal("world/objects/chest_active.png"))
                        .addState("open", Gdx.files.internal("world/objects/chest_open_1.png"))
                        */

                        .fromFile(Gdx.files.internal("world/objects/iso/chest_active.png"))
                        .addState("colliding", Gdx.files.internal("world/objects/iso/chest_active.png"))
                        .addState("open", Gdx.files.internal("world/objects/iso/chest_active.png"))
                        .bindTo(stateComponent)
        );

        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(6f, 4f),
                SpriteComponent.fromFile(Gdx.files.internal("world/objects/iso/chest_active.png"))
        );

        // genereate structures...
        int brickId = TileRegistry.idOfTag("brickF");
        for(float y = 20; y < 33; y++) {
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
        }

        return worldSpace;
    }

    public float getTopZ(Vector3 entityPos) {
        Vector3 topMost = this.terrainTiles.keySet().stream().filter(key -> key.x == Math.floor(entityPos.x) && key.y ==  Math.floor(entityPos.y)).max((key1, key2) -> Float.compare(key1.z, key2.z)).orElse(new Vector3(0,0,0));
        return topMost.z;
    }
    public void toNextUpperLevel(Vector3 entityPos) {
        Vector3 topMost = this.terrainTiles.keySet().stream()
                .filter(key -> key.x == Math.floor(entityPos.x) && key.y ==  Math.floor(entityPos.y) && key.z <= Math.floor(entityPos.z + 1f))
                .max((key1, key2) -> Float.compare(key1.z, key2.z))
            .orElse(new Vector3(0,0,0));
        entityPos.z = topMost.z + 1f;
    }

   public interface PlaceFeature {
        public void place(WorldSpace world, float x, float y, float z);
    }
}
