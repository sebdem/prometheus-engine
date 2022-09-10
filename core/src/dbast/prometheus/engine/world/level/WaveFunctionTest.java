package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.GenerationUtils;
import dbast.prometheus.engine.world.generation.PlaceFeature;
import dbast.prometheus.engine.world.generation.VectorListPair;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.engine.world.tile.TileRuleEntry;
import dbast.prometheus.engine.world.tile.TileRules;
import dbast.prometheus.utils.GeneralUtils;
import dbast.prometheus.utils.Vector3Comparator;
import dbast.prometheus.utils.WeightedRandomBag;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private boolean drawRiver = false;
    private boolean drawPaths = false;

    public WaveFunctionTest(int width, int height, int numberOfRandomBlocks, boolean simpleSmoothing, boolean growthSmoothing, List<PlaceFeature> features, int numberOfFeatures, int entitiesToGenerate) {
        this.width = width;
        this.height = height;
        this.numberOfRandomBlocks = numberOfRandomBlocks == -1 ? (width + height) / 2 : numberOfRandomBlocks;
        this.simpleSmoothing = simpleSmoothing && !growthSmoothing;
        this.growthSmoothing = growthSmoothing && !simpleSmoothing;
        this.features = features;
        this.placeFeatures = features != null && features.size() > 0 && numberOfFeatures > 0;
        this.numberOfFeatures = numberOfFeatures;
        this.entitiesToGenerate = entitiesToGenerate;
    }

    public WorldSpace setup() {
        WorldSpace worldSpace = new WorldSpace(width, height);
        boolean useIsometric = (Boolean) PrometheusConfig.conf.getOrDefault("isometric", false);

        // Step 0: Define tile "rules"
        Gdx.app.getApplicationLogger().log("WorldSetup", "Setting up Tile Rules");


        // TODO bet i can delegate this to a json. maybe even that of the tiles themselves

        Tile emptyTile = TileRegistry.getByTag("cube");
        Tile waterTile = TileRegistry.getByTag("water");
       // Tile waterMovingTile = TileRegistry.getByTag("waterM");
        Tile grassTile = TileRegistry.getByTag("grass_0");
        Tile grassToWaterNorth = TileRegistry.getByTag("grass_water_010");
        Tile grassToWaterEast = TileRegistry.getByTag("grass_water_100");
        Tile grassHighTile = TileRegistry.getByTag("grass_1");
        Tile dirtTile = TileRegistry.getByTag("dirt_0");
        Tile bigTree = TileRegistry.getByTag("tree");
        Tile treeTile = TileRegistry.getByTag("treeS");
        Tile pathTile = TileRegistry.getByTag("path_dirt");
        WeightedRandomBag<Tile> all = new WeightedRandomBag<>();
        all.addEntry(waterTile, 30);
        all.addEntry(treeTile, 5);
        //all.addEntry(pathTile, 1);
        all.addEntry(dirtTile, 15);
        all.addEntry(grassTile, 40);

        TileRules tileRules = new TileRules();
        TileRuleEntry waterRules = tileRules.forTile(waterTile);
       // TileRuleEntry waterMovingRules = tileRules.forTile(waterMovingTile);
        TileRuleEntry grassRules = tileRules.forTile(grassTile);
        TileRuleEntry grassHighRules = tileRules.forTile(grassHighTile);
        TileRuleEntry dirtRules = tileRules.forTile(dirtTile);
        TileRuleEntry bigTreeRules = tileRules.forTile(bigTree);
        TileRuleEntry treeRules = tileRules.forTile(treeTile);
        TileRuleEntry grassToWaterNorthRules = tileRules.forTile(grassToWaterNorth);
        TileRuleEntry grassToWaterEastRules = tileRules.forTile(grassToWaterEast);
        //TileRuleEntry pathTileRules = tileRules.forTile(pathTile);

        List<Vector3> nearby = Arrays.asList(GenerationUtils.nearby4Of(new Vector3(0, 0, 0)));

        WeightedRandomBag<Tile> waterChances = new WeightedRandomBag<>();
        waterChances.addEntry(grassTile, 5);
        waterChances.addEntry(waterTile, 95);
        nearby.forEach(offset ->
                waterRules.put(offset, grassTile, waterTile, grassToWaterNorth, grassToWaterEast)
        );
        waterRules.put(TileRuleEntry.Direction.SOUTH.dir, grassTile, waterTile, grassToWaterEast);
        waterRules.put(TileRuleEntry.Direction.WEST.dir, grassTile, waterTile, grassToWaterNorth);


      /*  nearby.forEach(offset ->
                waterMovingRules.put(offset, Arrays.asList(waterMovingTile, waterTile))
        );
*/
        WeightedRandomBag<Tile> dirtChances = new WeightedRandomBag<>();
        dirtChances.addEntry(dirtTile, 5);
        dirtChances.addEntry(grassTile, 30);
        nearby.forEach(offset ->
                dirtRules.put(offset, dirtChances)
        );
        nearby.forEach(offset ->
                treeRules.put(offset, Arrays.asList(treeTile, bigTree, grassHighTile))
        );
        nearby.forEach(offset ->
                bigTreeRules.put(offset, Arrays.asList(bigTree, treeTile))
        );

        WeightedRandomBag<Tile> grassChances = new WeightedRandomBag<>();
        grassChances.addEntry(waterTile, 10);
        grassChances.addEntry(grassHighTile, 10);
        /*grassChances.addEntry(pathTile, 5);*/
        grassChances.addEntry(dirtTile, 5);
        grassChances.addEntry(grassTile, 70);

        nearby.forEach(offset ->
                grassRules.put(offset, grassChances)
        );
        grassRules.remove(TileRuleEntry.Direction.SOUTH.dir);
        grassRules.put(TileRuleEntry.Direction.SOUTH.dir, Arrays.asList(grassHighTile, grassTile, dirtTile, grassToWaterNorth, grassToWaterEast));

        grassRules.remove(TileRuleEntry.Direction.EAST.dir);
        grassRules.put(TileRuleEntry.Direction.EAST.dir, Arrays.asList(grassHighTile, grassTile, dirtTile, grassToWaterNorth));

        grassRules.remove(TileRuleEntry.Direction.NORTH.dir);
        grassRules.put(TileRuleEntry.Direction.EAST.dir, Arrays.asList(grassHighTile, grassTile, dirtTile, grassToWaterEast));

        grassRules.remove(TileRuleEntry.Direction.WEST.dir);
        grassRules.put(TileRuleEntry.Direction.WEST.dir, Arrays.asList(grassHighTile, grassTile, dirtTile, grassToWaterNorth, grassToWaterEast));

        grassToWaterNorthRules.put(TileRuleEntry.Direction.NORTH.dir, grassTile);
        grassToWaterNorthRules.put(TileRuleEntry.Direction.SOUTH.dir, waterTile);
        grassToWaterNorthRules.put(TileRuleEntry.Direction.EAST.dir, grassTile, waterTile, grassToWaterNorth);
        grassToWaterNorthRules.put(TileRuleEntry.Direction.WEST.dir, grassTile, waterTile, grassToWaterNorth);

        grassToWaterEastRules.put(TileRuleEntry.Direction.EAST.dir, grassTile);
        grassToWaterEastRules.put(TileRuleEntry.Direction.WEST.dir, waterTile);
        grassToWaterEastRules.put(TileRuleEntry.Direction.NORTH.dir, grassTile, waterTile, grassToWaterEast);
        grassToWaterEastRules.put(TileRuleEntry.Direction.SOUTH.dir, grassTile, waterTile, grassToWaterEast);

        nearby.forEach(offset ->
                grassHighRules.put(offset, Arrays.asList(grassTile, grassHighTile, treeTile))
        );
       /* nearby.forEach(offset ->
                pathTileRules.put(offset, Arrays.asList(grassTile, pathTile))
        );*/

        // TODO migrate to WeightedBag, group getAllowedFromNeighbors multiples to get chances for bag
        Map<Vector3, List<Tile>> allowedTiles = new TreeMap<>(new Vector3Comparator.Planar());

        Function<Vector3, List<Tile>> getAllowedFromNeighbors = (atPosition) -> {
            Set<Tile> result = new HashSet<>(tileRules.allowedTilesPerVectorPerTile.keySet());

            Gdx.app.getApplicationLogger().log("WorldSetup",
                    String.format("1. Getting List from neighbors of %s", atPosition));

            for(Vector3 offset : GenerationUtils.nearby4Of(Vector3.Zero)) {
                Vector3 atOffsetPosition = atPosition.cpy().add(offset);

                List<Tile> atNeighbor = allowedTiles.getOrDefault(atOffsetPosition, new ArrayList<>());

                Gdx.app.getApplicationLogger().log("WorldSetup",
                        String.format(" --1. Getting allowed for %s, currently: %s tiles", atOffsetPosition, atNeighbor.size()));

                List<Tile> allowedForThis = tileRules.allowedForThisOffset(atNeighbor, offset);

                Gdx.app.getApplicationLogger().log("WorldSetup",
                    String.format(
                        " --2. Getting Tiles that allow [%s] as neighbors to %s: [%s]",
                        atNeighbor.stream().map(tile -> tile.tag).collect(Collectors.joining(", ")),
                        offset,
                        allowedForThis.stream().map(tile -> tile.tag).collect(Collectors.joining(", "))
                    )
                );

                result.retainAll(allowedForThis);
            }

            Gdx.app.getApplicationLogger().log("WorldSetup.getAllowedFromNeighbors",
                    String.format(
                            "2. Allowed for %s are tiles: [%s] ", atPosition,
                            result.stream().map(tile -> tile.tag).collect(Collectors.joining(", "))
                    )
            );
            return new ArrayList<>(result);
        };


        // Step 1 go over terrain and randomly add blocks
        Gdx.app.getApplicationLogger().log("WorldSetup", "Place random blocks");

        for(int i = 0; i < numberOfRandomBlocks; i++) {
            float y = (float) Math.floor((Math.random()*worldSpace.height));
            float x = (float) Math.floor((Math.random()*worldSpace.width));

            float z = 0f;

            Vector3 position = new Vector3(x, y, z);
            Tile selectedForPosition = all.getRandom();
            allowedTiles.put(position, Collections.singletonList(selectedForPosition));

            updateNearbyTiles(worldSpace, allowedTiles, position, getAllowedFromNeighbors);
        }

      /*  Vector3 underground = new Vector3(0,0,-7f);
        allowedTiles.put(underground, Collections.singletonList(dirtTile));
        updateNearbyTiles(worldSpace, allowedTiles, underground, getAllowedFromNeighbors);
*/
        // step 1.5 manually place blocks to try how things adapt
        // draw a "river"
        if (drawRiver) {
            Gdx.app.getApplicationLogger().log("WorldSetup", "Drawing River");

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
        }

        // Step 2 go over terrain and determine a random allowed tile from the given list and update all neighbors if not already determined

        if (simpleSmoothing) {
            Gdx.app.getApplicationLogger().log("WorldSetup", "Simple Smoothing");
/*
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
            }*/
        }


        // New Step 2 go over all and finalize if needed.
        if (growthSmoothing) {
            Gdx.app.getApplicationLogger().log("WorldSetup", "Test real Wave Function");

            long remainingUnresolvedTiles =
                    allowedTiles.entrySet().stream().filter(allowed->
                            worldSpace.isValidPosition(allowed.getKey()) &&
                                    allowed.getValue().size() > 1
                    ).count();
            Set<Vector3> collapsedPositions = new HashSet<>();
            while (remainingUnresolvedTiles > 0) {
                Gdx.app.getApplicationLogger().log("WorldSetup", "Remaining " + remainingUnresolvedTiles + " Tiles with unresolved values");

                List<Vector3> uncollapsedPositions = allowedTiles.entrySet().stream()
                        .filter(allowedForPosition -> !collapsedPositions.contains(allowedForPosition.getKey()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                for (Vector3 uncollapsed : uncollapsedPositions) {
                    List<Tile> allowedFromNeighbors = getAllowedFromNeighbors.apply(uncollapsed);
                    if (allowedFromNeighbors.size() > 0) {
                        Tile positionTile = GeneralUtils.randomElement(allowedFromNeighbors);

                        allowedTiles.put(uncollapsed, Collections.singletonList(positionTile));
                        collapsedPositions.add(uncollapsed);
                        updateNearbyTiles(worldSpace, allowedTiles, uncollapsed, getAllowedFromNeighbors);
                    } else {
                        allowedTiles.put(uncollapsed, Collections.singletonList(grassTile));
                        collapsedPositions.add(uncollapsed);
                    }
                    // Gdx.app.getApplicationLogger().log("World Setup", String.format("Position %s has %s number of valid tiles", position, allowedForPosition.size()));
                }

                remainingUnresolvedTiles =
                        allowedTiles.entrySet().stream().filter(allowed->
                                worldSpace.isValidPosition(allowed.getKey()) &&
                                        allowed.getValue().size() > 1
                        ).count();
            }

        }
        if (growthSmoothing && false) {
            Gdx.app.getApplicationLogger().log("WorldSetup", "Growth Smoothing");

            Set<VectorListPair<Tile, Collection<Tile>>> openList = new LinkedHashSet<>();
            HashSet<Vector3> closedList = new HashSet<>();

            allowedTiles.forEach((key, value) -> openList.add(new VectorListPair<>(key, value)));

            int iterationNumber = 0;
            while(!openList.isEmpty()) {
                iterationNumber++;
                VectorListPair<Tile, Collection<Tile>> openListEntry = openList.stream().min(
                        Comparator.comparingInt(pair -> pair.getValue().size())
                ).get();
                Vector3 position = openListEntry.getKey();
                Collection<Tile> allowed = openListEntry.getValue();

                Gdx.app.getApplicationLogger().log("WorldGen", String.format("============== Iteration Nr.= %s ========================", iterationNumber));
                Gdx.app.getApplicationLogger().log("WorldGen", String.format("Checking node %s with %s tiles", position.toString(), allowed.size()));


                if (closedList.contains(position)) {
                    Gdx.app.getApplicationLogger().log("WorldGen", String.format("Node %s is already closed, removing it...", position.toString()));
                    // position is already empty so make sure it's gone from open as well
                } else if (allowed.isEmpty()){
                    // tile node was evaluated
                } else {
                    Tile positionTile = GeneralUtils.randomElement(allowed);
                    // TODO reduce by neighbors instead...
                    allowed = Collections.singletonList(positionTile);
                    openListEntry.setValue(allowed);

                    // tile node was evaluated
                    Gdx.app.getApplicationLogger().log("WorldGen", String.format("Allowing for position %s: %s tiles", position, allowed.size()));
                    if (allowed.size() == 1) {
                        allowedTiles.put(position, Collections.singletonList((Tile)allowed.toArray()[0]));
                    } else{
                        closedList.remove(position);
                        openList.add(openListEntry);
                    }

                    // successor:
                    TileRuleEntry allowedNeighbors = tileRules.forTile(positionTile);
                    for(Vector3 offset : GenerationUtils.nearby8Of(Vector3.Zero)) {
                        Vector3 nearbyTile = position.cpy().add(offset);
                        Gdx.app.getApplicationLogger().log("WorldGen", String.format("- For position %s: Checking neighbor %s", position, nearbyTile));

                        if (worldSpace.isValidPosition(nearbyTile) && !closedList.contains(nearbyTile)) {
                            VectorListPair<Tile, Collection<Tile>> successor = openList.stream().filter(entry -> entry.getKey().equals(nearbyTile))
                                    .findFirst()
                                    .orElse(
                                        new VectorListPair<>(nearbyTile, allowedNeighbors.get(offset))
                                    );

                            List<Tile> allowedForPosition = new ArrayList<>(successor.getValue());
                            if (allowedForPosition.size() > 1){
                                allowedForPosition.retainAll(allowedNeighbors.get(offset));
                            }
                            if (allowedForPosition.size() == 0) {
                                allowedForPosition = Collections.singletonList(allowedNeighbors.self);
                            }
                            successor.setValue(allowedForPosition);

                            boolean wasRemoved = openList.remove(successor);
                            boolean wasAdded = openList.add(successor);
                            if (wasRemoved)
                                Gdx.app.getApplicationLogger().log("WorldGen", String.format("--- For position %s Entry was removed from open", nearbyTile));
                            Gdx.app.getApplicationLogger().log("WorldGen", String.format("--- For position %s Entry %s was %s added to open", nearbyTile, successor.getKey().toString() +"@"+ successor.getValue().size(), !wasAdded ? "not " : "" ));
                        }
                    }
                }
                openList.remove(openListEntry);
                closedList.add(position);
                Gdx.app.getApplicationLogger().log("WorldGen", String.format("Current sizes: openlist %s | closedlist %s", openList.size(), closedList.size()));
            }


            // openList by Vector3 and Tiles
            // closedList Set only containing Vector3
/*
            long remainingUnresolvedTiles =
                    allowedTiles.entrySet().stream().filter(allowed->
                            worldSpace.isValidPosition(allowed.getKey()) &&
                            allowed.getValue().size() > 1
                    ).count();
            while (remainingUnresolvedTiles > 0) {
                Gdx.app.getApplicationLogger().log("WorldSetup", "Remaining " + remainingUnresolvedTiles + " Tiles with unresolved values");

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

                remainingUnresolvedTiles =
                        allowedTiles.entrySet().stream().filter(allowed->
                                worldSpace.isValidPosition(allowed.getKey()) &&
                                        allowed.getValue().size() > 1
                        ).count();
            }*/

            /* // OLD "best" version...
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
             */
        }


        //allowedTiles.put(new Vector3(0,0,0), Collections.singletonList(pathTile));
        //allowedTiles.put(new Vector3(9,9,0), Collections.singletonList(pathTile));
        // allowedTiles.put(new Vector3(20,6,0), Collections.singletonList(pathTile));

        if (drawPaths) {
            Vector3[] pathPoints = new Vector3[]{new Vector3(1,1,0),
                    new Vector3((float)Math.floor(worldSpace.width * 0.5f), (float)Math.floor(worldSpace.height * 0.5f), 0),
                    new Vector3(this.width-1, this.height -1, 0)};

            Gdx.app.getApplicationLogger().log("WorldSetup", "Drawing "+pathPoints.length+" Paths ");


            for (int i = 0; i < pathPoints.length - 1; i++) {
                Vector3 startPoint = pathPoints[i];
                Vector3 endPoint = pathPoints[(i+1 < pathPoints.length) ? i+1 : 0];

                //  if (startPoint.dst(endPoint) < 100f) {
                List<Vector3> pathSteps = GenerationUtils.findPath(startPoint, endPoint, 1f,
                        worldSpace::isValidPosition,
                        // avoid trees and dirt
                        (groundPosition) -> {

                       /* List<Tile> tileAt = allowedTiles.get(vector3);
                        if (tileAt != null && tileAt.contains(waterTile)) {
                            Gdx.app.getApplicationLogger().log("generation", String.format("Path finding for %s issue tile is water!", vector3.toString()));
                            return false;
                        }*/
                            List<Tile> tile = allowedTiles.get(groundPosition);
                            return tile == null || tile.isEmpty() || (
                                    tile.size() == 1 &&
                                    !tile.get(0).equals(dirtTile) &&
                                    !tile.get(0).equals(treeTile) &&
                                    !tile.get(0).equals(bigTree)
                            );
                        });

                pathSteps.forEach(step -> {
                    allowedTiles.put(step, Collections.singletonList(pathTile));

                });
                //  }
            }

        }

        // STEP 3: PLace actual tiles in worldspace
        Gdx.app.getApplicationLogger().log("WorldSetup", "Resolving AllowedTiles");
        allowedTiles.forEach((position, allowed) -> {
            if (allowed.size() > 1) {
                Gdx.app.getApplicationLogger().log("generation", String.format("Error at %s: Position has multiple states: %s", position, allowed.size()));
            } else if (allowed.size() == 0) {
                Gdx.app.getApplicationLogger().log("generation", String.format("Error at %s: Position has NO states: %s", position, allowed.size()));
                allowed = Collections.singletonList(emptyTile);
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
            } /*else if (tile.equals(dirtTile)) {
                worldSpace.placeTile(dirtTile, position.x, position.y, z - 1);
                worldSpace.placeTile(dirtTile, position.x, position.y, z);
                worldSpace.placeTile(grassTile, position.x, position.y, z + 1);
            } */else {
                worldSpace.placeTile(dirtTile, position.x, position.y, z - 1);
                worldSpace.placeTile(tile, position.x, position.y, z);
            }
        });


        // Step 4 as terrain was generated, build features
        if (placeFeatures) {
            Gdx.app.getApplicationLogger().log("WorldSetup", "Placing Features");
            for(int i = 0; i <= numberOfFeatures; i++) {
                float y = (float) Math.floor((Math.random()*worldSpace.height));
                float x = (float) Math.floor((Math.random()*worldSpace.width));
                float z = 0f;
                GeneralUtils.randomElement(features).place(worldSpace, x, y, z);
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
                    new PositionComponent(worldSpace.getSpawnPoint()),
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

    private void updateNearbyTiles(WorldSpace worldSpace, Map<Vector3, List<Tile>> allowedTiles, Vector3 position, Function<Vector3, List<Tile>> getAllowedFromNeighbors) {
        Collection<Tile> allowedForPosition;

        for(Vector3 offset : GenerationUtils.nearby4Of(new Vector3(0,0,0))) {
            Vector3 nearbyTile = position.cpy().add(offset);

            if (worldSpace.isValidPosition(nearbyTile)) {
                allowedForPosition = allowedTiles.get(nearbyTile);
                if (allowedForPosition != null && allowedForPosition.size() == 1) {
                    continue;
                } else {
                    allowedForPosition = getAllowedFromNeighbors.apply(nearbyTile);
                }
                allowedTiles.put(nearbyTile, new ArrayList<Tile>(allowedForPosition));
            }
        }
    }
}
