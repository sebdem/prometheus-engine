package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.GenerationUtils;
import dbast.prometheus.engine.world.generation.PlaceFeature;
import dbast.prometheus.engine.world.generation.features.CastleTower;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;
import dbast.prometheus.utils.Vector3Comparator;

import java.util.*;
import java.util.stream.IntStream;

public class WaveFunctionTest {
    public int width;
    public int height;
    public int numberOfRandomBlocks;
    public boolean simpleSmoothing;
    public boolean growthSmoothing;
    public boolean placeFeatures;
    public List<PlaceFeature> features;
    public int numberOfFeatures;
    public int entitiesToGenerate;
    private boolean asMountain = false;

    public WaveFunctionTest(int width, int height, int numberOfRandomBlocks, boolean simpleSmoothing, boolean growthSmoothing, List<PlaceFeature> features, int numberOfFeatures, int entitiesToGenerate) {
        this.width = width;
        this.height = height;
        this.numberOfRandomBlocks = numberOfRandomBlocks;
        this.simpleSmoothing = simpleSmoothing && !growthSmoothing;
        this.growthSmoothing = growthSmoothing && !simpleSmoothing;
        this.features = features;
        this.placeFeatures = features != null && features.size() > 0;
        this.numberOfFeatures = numberOfFeatures;
        this.entitiesToGenerate = entitiesToGenerate;
    }

    public WorldSpace setup() {
        WorldSpace worldSpace = new WorldSpace(width, height);
        boolean useIsometric = (Boolean) PrometheusConfig.conf.getOrDefault("isometric", false);

        // Step 0: Define tile "rules"
        Map<Tile, List<Tile>> tileRules = new HashMap<>();
        Tile waterTile = TileRegistry.getByTag("water");
        Tile waterMovingTile = TileRegistry.getByTag("waterM");
        Tile grassTile = TileRegistry.getByTag("grass_0");
        Tile grassHighTile = TileRegistry.getByTag("grass_1");
        Tile dirtTile = TileRegistry.getByTag("dirt_0");
        Tile bigTree = TileRegistry.getByTag("tree");
        Tile treeTile = TileRegistry.getByTag("treeS");
        Tile pathTile = TileRegistry.getByTag("path_dirt");
        List<Tile> all = Arrays.asList(grassTile, treeTile, pathTile, waterTile);

        tileRules.put(waterTile, Arrays.asList(
                waterTile, waterTile, grassTile
        ));
        tileRules.put(dirtTile, Arrays.asList(
                dirtTile, dirtTile, grassTile
        ));
        tileRules.put(treeTile, Arrays.asList(
                treeTile, bigTree, grassTile, grassHighTile
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
                grassTile, grassHighTile, grassHighTile, treeTile
        ));
        tileRules.put(pathTile, Arrays.asList(
                grassTile
        ));

        Map<Vector3, List<Tile>> allowedTiles = new TreeMap<>(new Vector3Comparator.Planar());

        // Step 1 go over terrain and randomly add blocks

        for(int i = 0; i <= numberOfRandomBlocks; i++) {
            float y = (float) Math.floor((Math.random()*worldSpace.height));
            float x = (float) Math.floor((Math.random()*worldSpace.width));
            /*
            float y = (float) Math.floor((Math.random()*20));
            float x = (float) Math.floor((Math.random()*20));*/
            float z = 0f;

            Vector3 position = new Vector3(x, y, z);
            Tile selectedForPosition = GeneralUtils.randomElement(all);
            allowedTiles.put(position, Collections.singletonList(selectedForPosition));

            updateNearbyTiles(worldSpace, tileRules, allowedTiles, position, selectedForPosition);
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

        //allowedTiles.put(new Vector3(0,0,0), Collections.singletonList(pathTile));
        //allowedTiles.put(new Vector3(9,9,0), Collections.singletonList(pathTile));
        // allowedTiles.put(new Vector3(20,6,0), Collections.singletonList(pathTile));

        Vector3[] pathPoints = allowedTiles.entrySet().stream()
                .filter(entrySet ->
                        entrySet.getValue().size() == 1 &&
                                entrySet.getValue().contains(pathTile)
                ).map(Map.Entry::getKey)
                .toArray(Vector3[]::new);


        for (int i = 0; i < pathPoints.length - 1; i++) {
            Vector3 startPoint = pathPoints[i];
            Vector3 endPoint = pathPoints[(i+1 < pathPoints.length) ? i+1 : 0];

            List<Vector3> pathSteps = GenerationUtils.findPath(startPoint, endPoint, 1f, (vector3) -> {
               /* List<Tile> tileAt = allowedTiles.get(vector3);
                if (tileAt != null && tileAt.contains(waterTile)) {
                    Gdx.app.getApplicationLogger().log("generation", String.format("Path finding for %s issue tile is water!", vector3.toString()));
                    return false;
                }*/
                return true;
            });

            pathSteps.forEach(step -> {
                allowedTiles.put(step, Collections.singletonList(pathTile));
                if (!step.equals(endPoint)) {
                   /* for (Vector3 nearbyTile : GenerationUtils.nearby8Of(step)) {
                        allowedTiles.put(nearbyTile, Collections.singletonList(pathTile));
                    }*/
                }
            });
        }

        // Step 2 go over terrain and determine a random allowed tile from the given list and update all neighbors if not already determined

        if (simpleSmoothing) {
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

                            updateNearbyTiles(worldSpace, tileRules, allowedTiles, position, selectedForPosition);
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

                        updateNearbyTiles(worldSpace, tileRules, allowedTiles, position, positionTile);
                    }
                    // Gdx.app.getApplicationLogger().log("World Setup", String.format("Position %s has %s number of valid tiles", position, allowedForPosition.size()));
                }
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
            if (asMountain) {
                z += 0.125f * (-Math.abs(position.x - worldSpace.width * 0.5f) + worldSpace.width * 0.5f)
                        * 0.125f * (-Math.abs(position.y - worldSpace.height * 0.5f) + worldSpace.height * 0.5f);
            }

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
            } else if (tile.equals(dirtTile)) {
                worldSpace.placeTile(dirtTile, position.x, position.y, z - 1);
                worldSpace.placeTile(dirtTile, position.x, position.y, z);
                worldSpace.placeTile(dirtTile, position.x, position.y, z + 1);
                worldSpace.placeTile(grassTile, position.x, position.y, z + 2);
            } else {
                worldSpace.placeTile(dirtTile, position.x, position.y, z - 1);
                worldSpace.placeTile(tile, position.x, position.y, z);
            }
        });

        // Step 4 as terrain was generated, build features
        if (placeFeatures) {
            for(int i = 0; i <= numberOfFeatures; i++) {
                float y = (float) Math.floor((Math.random()*worldSpace.height));
                float x = (float) Math.floor((Math.random()*worldSpace.width));
                float z = 0f;
                GeneralUtils.randomElement(features).place(worldSpace, x, y, z);
            }
        }


        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                new CollisionBox(1f,1f,false),
                new SizeComponent(1f,1f),
                new PositionComponent(0f, 0f, 1f),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                new StateComponent(),
                new RenderComponent()
                        .registerAnimation(Gdx.files.internal(
                                (useIsometric) ?  "sprites/player/player_idle.png" : "sprites/player/test_01.png"
                        ), 8, 1, 1.25f, true, "default")
                        .registerAnimation(Gdx.files.internal(
                            (useIsometric) ?  "sprites/player/player_moving_down.png" : "sprites/player/test_01.png"
                ), 8, 1, 0.125f, true, "moving")
        );


        FileHandle[] blobTextures = new FileHandle[]{
                Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_0.png"),
                Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_1.png"),
                Gdx.files.internal("sprites/enemies/" + ((useIsometric) ?  "iso_" : "") + "blob_2.png")
        };

        float maxSpeed = 8f;
        for(int i = 0; i < entitiesToGenerate; i++) {
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    new PositionComponent(1f, 1f, 1f),
                    new StateComponent(),
                    new RenderComponent().setDefaultTexture(GeneralUtils.randomElement(blobTextures)),
                    //SpriteComponent.fromTexture(new Texture(blobTextures[(int)(Math.random() * blobTextures.length)])),
                    new VelocityComponent(0f,0f,0f),
                   new TargetTraverseComponent()
                   // new VelocityComponent((float)((Math.random() - 0.5f) * maxSpeed),(float)((Math.random() - 0.5f) * maxSpeed))
            );
        }

        /*
        StateComponent stateComponent = new StateComponent();
        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(4f, 1f, 1f),
                stateComponent,
                new RenderComponent()
                    .setDefaultTexture(Gdx.files.internal("world/objects/iso/chest_active.png"))
                    .registerAnimation(Gdx.files.internal("world/objects/iso/chest_open.png"), 3, 1, 0.5f, false, "colliding")
        );
*/
        return worldSpace;
    }

    private void updateNearbyTiles(WorldSpace worldSpace, Map<Tile, List<Tile>> tileRules, Map<Vector3, List<Tile>> allowedTiles, Vector3 position, Tile tileAtPosition ) {
        List<Tile> allowedForPosition;
        List<Tile> allowedNeighbors = tileRules.get(tileAtPosition);

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
}
