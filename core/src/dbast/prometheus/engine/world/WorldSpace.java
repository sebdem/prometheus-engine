package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.serializing.data.WorldData;
import dbast.prometheus.engine.world.generation.GenerationUtils;
import dbast.prometheus.engine.world.generation.PlaceFeature;
import dbast.prometheus.engine.world.generation.features.CastleTower;
import dbast.prometheus.engine.world.generation.features.Mountain;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;
import dbast.prometheus.utils.Vector3Comparator;
import jdk.nashorn.internal.runtime.JSONFunctions;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public WorldSpace placeTile(Tile tile, float x, float y, float z) {
        this.terrainTiles.put(new Vector3(x, y, z), tile);
        return this;
    }
    public Tile lookupTile(float x, float y, float z) {
        return this.terrainTiles.get(new Vector3(x, y, z));
    }


    public WorldSpace removeTile(float x, float y, float z) {
        this.terrainTiles.remove(new Vector3(x, y, z));
        return this;
    }

    public void persist() {
        FileHandle file = Gdx.files.local("save/world_" + (System.nanoTime() / 1000) + ".json");
        file.writeString(new Gson().toJson(new WorldData(this)), false);
        //Gdx.app.getClipboard().setContents();
    }

    public static WorldSpace mostMinimalLevel() {
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

        //worldSpace.placeTile(TileRegistry.idOfTag("tree"), 2, 2, 1);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,0, 0.75f);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,1, 1.5f);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,2, 2.25f);
        worldSpace.placeTile(TileRegistry.idOfTag("grass_0"), 0,3, 3f);

        return worldSpace;
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

    public static WorldSpace waveFunctionTest() {
        WorldSpace worldSpace = new WorldSpace(64, 64);
        boolean useIsometric = (Boolean) PrometheusConfig.conf.getOrDefault("isometric", false);

        boolean smoothing = false;
        boolean growthSmoothing = true;
        boolean placeFeatures = true;

        // Step 0: Define tile "rules"
        Map<Tile, List<Tile>> tileRules = new HashMap<>();
        Tile waterTile = TileRegistry.getByTag("water");
        Tile grassTile = TileRegistry.getByTag("grass_0");
        Tile grassHighTile = TileRegistry.getByTag("grass_1");
        Tile dirtTile = TileRegistry.getByTag("dirt_0");
        Tile bigTree = TileRegistry.getByTag("tree");
        Tile treeTile = TileRegistry.getByTag("treeS");
        Tile pathTile = TileRegistry.getByTag("path_dirt");
        List<Tile> all = Arrays.asList(grassTile, treeTile, pathTile, waterTile);

        tileRules.put(waterTile, Arrays.asList(
                waterTile, waterTile, waterTile, grassTile
        ));
        tileRules.put(dirtTile, Arrays.asList(
                dirtTile, dirtTile, dirtTile, grassTile
        ));
        tileRules.put(treeTile, Arrays.asList(
                treeTile, bigTree, grassTile
        ));
        tileRules.put(bigTree, Arrays.asList(
                bigTree, treeTile
        ));
        tileRules.put(grassTile, Arrays.asList(
               grassTile, grassTile, grassTile, treeTile, dirtTile,
                grassTile, grassTile, grassTile, treeTile, dirtTile,
                grassTile, grassHighTile,
                waterTile
        ));
        tileRules.put(grassHighTile, Arrays.asList(
                grassTile, grassHighTile, grassHighTile
        ));
        tileRules.put(pathTile, Arrays.asList(
                grassTile
        ));

        Map<Vector3, List<Tile>> allowedTiles = new TreeMap<>(new Vector3Comparator.Planar());

        // Step 1 go over terrain and randomly add blocks

        for(int i = 0; i <= 30; i++) {
            float y = (float) Math.floor((Math.random()*worldSpace.height));
            float x = (float) Math.floor((Math.random()*worldSpace.width));
            /*
            float y = (float) Math.floor((Math.random()*20));
            float x = (float) Math.floor((Math.random()*20));*/
            float z = 0f;

            Vector3 position = new Vector3(x, y, z);
            Tile selectedForPosition = GeneralUtils.randomElement(all);
            allowedTiles.put(position, Collections.singletonList(selectedForPosition));

            List<Tile> allowedNeighbors = tileRules.get(selectedForPosition);

            for(Vector3 nearbyTile : GenerationUtils.nearby8Of(position)) {
                List<Tile> allowedForPosition = allowedTiles.get(nearbyTile);
                if (allowedForPosition == null) {
                    allowedForPosition = allowedNeighbors;
                } else if (allowedForPosition.size() > 1){
                    allowedForPosition.retainAll(allowedNeighbors);
                } else if (allowedForPosition.size() == 1) {
                    continue;
                }
                allowedTiles.put(nearbyTile, new ArrayList<Tile>(allowedForPosition));
            }
        }

        // step 1.5 manually place blocks to try how things adapt
        // draw a "river"
        IntStream.range(0,worldSpace.width).mapToObj(x->
          //  new Vector3(x, (float) Math.floor(Math.sin(0.125*x)*4) + 4, 0)
           // new Vector3(x, (float) Math.floor(0.25*x) + 7, 0)
           // new Vector3(x, (float) (x), 0)
            new Vector3(x, (float) Math.floor(0.5*x+ (Math.pow(Math.sin(Math.sqrt(x) ),2))*0.5*x), 0)
        ).forEach(position -> {
                allowedTiles.put(position, Collections.singletonList(waterTile));
                Vector3[] nearbys = new Vector3[]{
                        position.cpy().add(0, 2, 0),
                        position.cpy().add(0, 1, 0),
                        position.cpy().add(0, -1, 0),
                        position.cpy().add(0, -2, 0),
                };
                for (Vector3 nearbyTile : nearbys) {
                    allowedTiles.put(nearbyTile, Collections.singletonList(waterTile));
                }
        });


        Vector3[] pathPoints = allowedTiles.entrySet().stream()
                .filter(entrySet ->
                        entrySet.getValue().size() == 1 &&
                        entrySet.getValue().contains(pathTile)
                ).map(Map.Entry::getKey)
                .toArray(Vector3[]::new);

        for (int i = 0; i < pathPoints.length - 1; i++) {
            Vector3 startPoint = pathPoints[i];
            Vector3 endPoint = pathPoints[(i+1 < pathPoints.length) ? i+1 : 0];

            allowedTiles.put(startPoint, Collections.singletonList(pathTile));
            allowedTiles.put(endPoint, Collections.singletonList(pathTile));
            boolean rightBound = Math.random() > 0.5f;
            float startY = Math.min(startPoint.y, endPoint.y);
            float endY = Math.max(startPoint.y, endPoint.y);

            float startX = Math.min(startPoint.x, endPoint.x);
            float endX = Math.max(startPoint.x, endPoint.x);
/*
            float dX = endPoint.x - startPoint.x;
            float dY = endPoint.y - startPoint.y;
            float delta = dY / dX;
            float brushSize = 2;
*/
            for (float y = startY; y < endY + 1; y++) {
                for (float x = startX; x < endX + 1; x++) {
/*
                        for(float y2 = y - brushSize; y2 < y + brushSize; y2++) {
                            for(float x2 = x - brushSize; x2 < x + brushSize; x2++) {
                                float deltaN = (endPoint.y - y2) / (endPoint.x - x2);
                                float delta2 = (y2 - startPoint.y) / (x2 - startPoint.x);
                                if (deltaN ==delta2) {
                                    allowedTiles.put(new Vector3(x2, y2, startPoint.z), Collections.singletonList(pathTile));
                                }
                            }
                        }
*/
                    if (
                        (y == (rightBound ? startPoint.y : endPoint.y) &&
                                GeneralUtils.isBetween(x, startX, endX, true)) ||
                        (x == (rightBound ? endPoint.x : startPoint.x) &&
                                GeneralUtils.isBetween(y, startY, endY, true))
                    ) {
                        allowedTiles.put(new Vector3(x, y, startPoint.z), Collections.singletonList(pathTile));
                    }
                }
            }
            if (Math.random() * 100 >= 80) {
                i++;
            }
        }

        // Step 2 go over terrain and determine a random allowed tile from the given list and update all neighbors if not already determined

        if (smoothing) {
            int maxTerrainCycles = 64;
            for(int tCycle = 1; tCycle <= maxTerrainCycles; tCycle++) {
                float z = 0f;
                for(float y = 0; y < worldSpace.height; y++) {
                    for(float x = 0; x < worldSpace.width; x++) {
                        Vector3 position = new Vector3(x,y,z);
                        List<Tile> allowedForPosition = allowedTiles.get(position);
                        // when the state of the tile is neither determined or completely up in the air
                        if (allowedForPosition != null && !(allowedForPosition.size() == 1 && allowedForPosition.containsAll(all))) {
                            Tile selectedForPosition = GeneralUtils.randomElement(allowedForPosition);
                            allowedTiles.put(position, Collections.singletonList(selectedForPosition));

                            List<Tile> allowedNeighbors = tileRules.get(selectedForPosition);

                            for(Vector3 nearbyTile : GenerationUtils.nearby8Of(position)) {
                                allowedForPosition = allowedTiles.get(nearbyTile);
                                if (allowedForPosition == null) {
                                    allowedForPosition = allowedNeighbors;
                                } else if (allowedForPosition.size() > 1){
                                    allowedForPosition.retainAll(allowedNeighbors);
                                } else if (allowedForPosition.size() == 1) {
                                    continue;
                                }
                                allowedTiles.put(nearbyTile, new ArrayList<Tile>(allowedForPosition));
                            }
                        } else {
                            // all states allowed, do nothing
                        }
                    }
                }
            }
        }

        // New Step 2 go over all and finalize if needed.
        if (growthSmoothing) {
            while (allowedTiles.entrySet().stream().anyMatch(allowed->
                    allowed.getKey().x >= 0 && allowed.getKey().y >= 0 &&
                    allowed.getKey().x < worldSpace.width && allowed.getKey().y < worldSpace.height &&
                    allowed.getValue().size() > 1)) {
                List<Vector3> positions = Arrays.asList(allowedTiles.keySet().toArray(new Vector3[0]));
                Collections.shuffle(positions);

                for (Vector3 position : positions) {
                    List<Tile> allowedForPosition = allowedTiles.get(position);

                    if (allowedForPosition.size() >= 1) {
                        Tile positionTile;
                        if (allowedForPosition.size() == 1) {
                            positionTile = allowedForPosition.get(0);
                        } else {
                            positionTile = GeneralUtils.randomElement(allowedForPosition);
                            allowedTiles.put(position, Collections.singletonList(positionTile));
                        }

                        List<Tile> allowedNeighbors = tileRules.get(positionTile);

                        for(Vector3 nearbyTile : GenerationUtils.nearby8Of(position)) {
                            if (nearbyTile.x >= 0 && nearbyTile.y >= 0 &&
                                nearbyTile.x < worldSpace.width && nearbyTile.y < worldSpace.height
                            ) {
                                allowedForPosition = allowedTiles.get(nearbyTile);
                                if (allowedForPosition == null) {
                                    allowedForPosition = allowedNeighbors;
                                } else if (allowedForPosition.size() > 1){
                                    allowedForPosition.retainAll(allowedNeighbors);
                                } else if (allowedForPosition.size() == 1) {
                                    continue;
                                }
                                allowedTiles.put(nearbyTile, new ArrayList<Tile>(allowedForPosition));
                            }
                        }
                    }
                   // Gdx.app.getApplicationLogger().log("World Setup", String.format("Position %s has %s number of valid tiles", position, allowedForPosition.size()));
                };
            }
        }

        // STEP 3: PLace actual tiles in worldspace
        allowedTiles.forEach((position, allowed) -> {
            if (allowed.size() > 1) {
                Gdx.app.getApplicationLogger().log("generation", String.format("Error at %s: Position has multiple states: %s", position, allowed.size()));
            } else if (allowed.size() == 0) {
                Gdx.app.getApplicationLogger().log("generation", String.format("Error at %s: Position has NO states: %s", position, allowed.size()));
                allowed = Collections.singletonList(grassTile);
            }
            Tile tile = GeneralUtils.randomElement(allowed);
            float z = position.z;
            if (tile.equals(treeTile)) {
                worldSpace.placeTile(treeTile, position.x, position.y, z + 1);
                tile = grassTile;
            }
            if (tile.equals(bigTree)) {
                worldSpace.placeTile(bigTree, position.x, position.y, z + 1);
                tile = grassTile;
            }
            if (tile.equals(waterTile)) {
                worldSpace.placeTile(dirtTile, position.x, position.y, z - 1);
                worldSpace.placeTile(tile, position.x, position.y, z);
            } else {
                worldSpace.placeTile(dirtTile, position.x, position.y, z - 1);
                worldSpace.placeTile(tile, position.x, position.y, z);
            }
        });

        // Step 4 as terrain was generated, build features
        if (placeFeatures) {
            List<PlaceFeature> features = Arrays.asList(
                    /*new Mountain("grass_0"),*/ new CastleTower("brickF")
            );

            for(int i = 0; i <= 15; i++) {
                float y = (float) Math.floor((Math.random()*worldSpace.height));
                float x = (float) Math.floor((Math.random()*worldSpace.width));
                float z = 0f;
                int feature = (int) (Math.random() * features.size());

                features.get(feature).place(worldSpace, x, y, z);
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
                //new VelocityComponent(3f,5f),
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

        float maxSpeed = 8f;
        for(int i = 0; i < 200; i++) {
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    PositionComponent.initial(),
                    SpriteComponent.fromTexture(blobTextures[(int)(Math.random() * 3)]),
                    new VelocityComponent((float)((Math.random() - 0.5f) * maxSpeed),(float)((Math.random() - 0.5f) * maxSpeed))
            );
        }

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

        for(int i = 0; i <= 30; i++) {
            float y = (float) Math.floor((Math.random()*worldSpace.height));
            float x = (float) Math.floor((Math.random()*worldSpace.width));
            float z = 0f;
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


}
