package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.world.WorldChunk;
import dbast.prometheus.engine.world.level.MultilayeredPerlinTest;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.WeightedRandomBag;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 */
public class WorldGenerator {
    protected Map<String, Long> seeds;

    protected final int worldDepth;

    protected final static String MAIN = "main";

    private Random randomGenerator;

    private OpenSimplexNoise landmassNoise;
    private OpenSimplexNoise mainHeightNoise;
    private OpenSimplexNoise terrainSmoothingNoise;
    private OpenSimplexNoise temperatureNoise;
    private OpenSimplexNoise featureNoise;

    private final int landmassScale = 96;
    private final int mainHeightScale = 64;
    private final int terrainSmoothingScale = 16;

    private final int temperatureScale = 64;


    public WorldGenerator(long worldSeed) {
        this.worldDepth = 32;
        seeds = new HashMap<>();

        seeds.put(MAIN, worldSeed);
        randomGenerator = new Random(worldSeed);

        seeds.put("landmassNoise", randomGenerator.nextLong());
        seeds.put("mainHeight", randomGenerator.nextLong());
        seeds.put("smoothingNoise", randomGenerator.nextLong());
        seeds.put("temperatureNoise", randomGenerator.nextLong());
        seeds.put("featureNoise", randomGenerator.nextLong());

        landmassNoise = new OpenSimplexNoise(getSeed("landmassNoise"));
        mainHeightNoise = new OpenSimplexNoise(getSeed("mainHeight"));
        terrainSmoothingNoise = new OpenSimplexNoise(getSeed("smoothingNoise"));
        temperatureNoise = new OpenSimplexNoise(getSeed("temperatureNoise"));
        featureNoise = new OpenSimplexNoise(getSeed("featureNoise"));
    }

    protected Long getSeed(String key) {
        return this.seeds.getOrDefault(key, seeds.get(MAIN));
    }

    public WorldChunk populateChunk(float chunkX, float chunkY, WorldChunk chunk) {
        Tile tileToPlace = null;

        int landmassWeight = 30;
        int heightWeight = 10;
        int smoothingWeight = 2;

        WeightedRandomBag<String> grass_deco = new WeightedRandomBag<>();
        grass_deco.addEntry("none",60);
        grass_deco.addEntry("default",25);
        grass_deco.addEntry("tall1",15);

        for (float yc = 0; yc < WorldChunk.CHUNK_SIZE; yc++) {
            float yAbs = yc + chunkY;
            for (float xc = 0; xc < WorldChunk.CHUNK_SIZE; xc++) {
                float xAbs = xc + chunkX;

                float landmassNoiseVal = (float) landmassNoise.eval(xAbs / landmassScale, yAbs / landmassScale);
                float landmassValue = (float)Math.pow(Math.abs(landmassNoiseVal), 0.75) * ((landmassNoiseVal < 0) ? -1 : 1) / 2;

                // Gdx.app.log("world_...", String.format("generating landmass noise at %s %s : %s", xAbs, yAbs, landmassValue));

                MultilayeredPerlinTest.WorldGenLayer currentLayer = MultilayeredPerlinTest.WorldGenLayer.getForValue(landmassValue);
                //WorldGenLayer currentLayer = WorldGenLayer.values()[(int)(Math.floor(((landmassValue+1 ) / 2) * WorldGenLayer.values().length))];

                float mainHeightValue =  (float)mainHeightNoise.eval(xAbs / mainHeightScale, yAbs / mainHeightScale);
                float smoothingValue =  (float)terrainSmoothingNoise.eval(xAbs / terrainSmoothingScale, yAbs / terrainSmoothingScale);

                if (currentLayer.equals(MultilayeredPerlinTest.WorldGenLayer.BEACH)) {
                    smoothingValue = (smoothingValue * (float)terrainSmoothingNoise.eval(xAbs / 4, yAbs / 4));
                }
                //float biomeValue =  (float)temperatureNoise.eval(xAbs / biomeScale, yAbs / biomeScale);


                float landmassWithWeight = (landmassValue * landmassWeight);
                float heightWithWeight = (mainHeightValue * heightWeight);
                float smoothingWithWeight = (smoothingValue * smoothingWeight);
                float product = landmassWithWeight + heightWithWeight + smoothingWithWeight;
                float productDividedByWeight = product / (landmassWeight + smoothingWeight + heightWeight);

                float terrainLimit = (float) ((worldDepth) * (2 * Math.pow(Math.abs(productDividedByWeight), 1.85)) * ((product < 0) ? -1 : 1));

                Gdx.app.log("world_...", String.format("generating at x%s/y%s => terrainLimit: %s - product: %s (layer: %s | landmass: %s (noise: %s) | height: %s | smoothing %s", xAbs, yAbs, terrainLimit, product, currentLayer.name(), landmassWithWeight, landmassNoiseVal, heightWithWeight, smoothingWithWeight));

                float maxZ = (terrainLimit < 0) ? 0 : (float)Math.floor(terrainLimit);

                String tileState = "default";

                for (float z = -6f; z<= maxZ; z+=1f) {
// TODO next step, rebuild the actual block placing...

                    if (z > terrainLimit && z <= maxZ) {
                        tileToPlace = TileRegistry.getByTag("water");
                        //    tileState = "north";
                    } else {
                        if (z < maxZ - 2) {
                            tileToPlace = TileRegistry.getByTag("stone");
                        } else if (z < maxZ) {
                            tileToPlace = TileRegistry.getByTag("dirt_0");
                        } else if (z == maxZ) {
                            if (z != 0) {
                                tileToPlace = TileRegistry.getByTag("grass_0");
                                String deco = grass_deco.getRandom();
                                if (!deco.equals("none")) {
                                    chunk.placeTile(TileRegistry.getByTag("grass_deco_0"), xAbs, yAbs, z + 1f, deco);
                                }
                            } else {
                                tileToPlace = TileRegistry.getByTag("sand");
                            }
                        }
                    }
                    chunk.placeTile(
                            tileToPlace,
                            xAbs, yAbs, z,
                            tileState
                    );

                }

                if (xc == 0 && yc == 0) {
                    chunk.placeTile(
                            TileRegistry.getByTag("grass_0"),
                            xAbs, yAbs, 7,
                            "tileState"
                    );
                }
                if (xc == 0 && yc != 0) {
                    chunk.placeTile(
                            TileRegistry.getByTag("bridge"),
                            xAbs, yAbs, 7,
                            "variant"
                    );
                }
                if (yc == 0 && xc != 0) {
                    chunk.placeTile(
                            TileRegistry.getByTag("bridge"),
                            xAbs, yAbs, 7,
                            "default"
                    );
                }
            }
        }
        return chunk;
    }
}
