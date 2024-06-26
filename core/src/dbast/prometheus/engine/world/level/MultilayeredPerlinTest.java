package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldChunk;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.OpenSimplexNoise;
import dbast.prometheus.engine.world.generation.WorldGenLayer;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class MultilayeredPerlinTest {


    private final BufferedImage worldHeightMap;
    public int width;
    public int height;
    private final int worldDepth;

    private final Random randomGenerator;

    // generate what world map level (ocean, (beach), land, mountain)
    private final OpenSimplexNoise landmassNoise;

    // mainHeightNoise
    private final OpenSimplexNoise mainHeightNoise;
    private final OpenSimplexNoise terrainSmoothingNoise;

    // determines biome generation, ignore for now
    private final OpenSimplexNoise temperatureNoise;

    private final OpenSimplexNoise featureNoise;

    private final boolean generateMinimap = true;

    protected long seed;

    public MultilayeredPerlinTest(int width, int height) {
        this(width, height, new Random().nextLong());
    }

    public MultilayeredPerlinTest(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.worldDepth = 32;

        this.seed = seed;
        randomGenerator = new Random(seed);

        landmassNoise = new OpenSimplexNoise(randomGenerator.nextLong());
        mainHeightNoise = new OpenSimplexNoise(randomGenerator.nextLong());
        terrainSmoothingNoise = new OpenSimplexNoise(randomGenerator.nextLong());
        temperatureNoise = new OpenSimplexNoise(randomGenerator.nextLong());
        featureNoise = new OpenSimplexNoise(randomGenerator.nextLong());

        if (generateMinimap) {
            worldHeightMap = new BufferedImage(width * 2 + 2, height *2 + 2, BufferedImage.TYPE_INT_ARGB);
        } else {
            worldHeightMap = null;
        }
    }

    public WorldSpace setup() {

        WorldSpace worldSpace = new WorldSpace(-width, -height, width, height);


        int totalHeight = (height * 2);
        int totalWidth = width * 2;
        int numberOfChunksY = totalHeight / worldSpace.chunkSize/* + 2*/;
        int numberOfChunksX = totalWidth / worldSpace.chunkSize/* + 2*/;



        // TODO separate Chunk creation from generation...
        WorldChunk[] chunks = new WorldChunk[numberOfChunksY * numberOfChunksX];

        for (int y = 0; y < numberOfChunksY; y++) {
            for (int x = 0; x < numberOfChunksX; x++) {
                int index = (y * numberOfChunksX) + x;
                chunks[index] = new WorldChunk();
                chunks[index].position = new Vector3(x - numberOfChunksX / 2, y - numberOfChunksY / 2, 0);
            }
        }

        Arrays.stream(chunks).forEach(
                worldChunk -> {

                float chunkY = (worldChunk.position.y)  * worldSpace.chunkSize;
                float chunkX = (worldChunk.position.x) * worldSpace.chunkSize;
                generateChunk(chunkX, chunkY, worldSpace);
            }
        );

        if (generateMinimap) {
            generateMinimap(totalWidth, totalHeight, worldSpace);
        }
        Gdx.app.getApplicationLogger().log("WorldSetup", "Generating Entities");
        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                /*new CollisionBox(new Vector3(0.99f,0.99f,1.49f).scl(0.75f), false),*/
                new SizeComponent(1f,1f),
                new PositionComponent(new Vector3(0,0,10)),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                new StateComponent(),
                RenderComponent.playerRenderComponent()
        );
        return worldSpace;
    }


    public void generateChunk(float chunkX, float chunkY, WorldSpace worldSpace) {
        Tile tileToPlace = null;

        int landmassWeight = 30;
        int heightWeight = 10;
        int smoothingWeight = 2;

        int landmassScale = 96;
        int mainHeightScale = 64;
        int terrainSmoothingScale = 16;
        int temperatureScale = 112;

        for (float yc = 0; yc < worldSpace.chunkSize; yc++) {
            float yAbs = yc + chunkY;
            for (float xc = 0; xc < worldSpace.chunkSize; xc++) {
                float xAbs = xc + chunkX;

                float landmassNoiseVal = (float) landmassNoise.eval(xAbs / landmassScale, yAbs / landmassScale);
                float landmassValue = (float)Math.pow(Math.abs(landmassNoiseVal), 0.75) * ((landmassNoiseVal < 0) ? -1 : 1);

               // Gdx.app.log("world_...", String.format("generating landmass noise at %s %s : %s", xAbs, yAbs, landmassValue));

                WorldGenLayer currentLayer = WorldGenLayer.getForValue(landmassValue);
                //WorldGenLayer currentLayer = WorldGenLayer.values()[(int)(Math.floor(((landmassValue+1 ) / 2) * WorldGenLayer.values().length))];

                float mainHeightValue =  (float)mainHeightNoise.eval(xAbs / mainHeightScale, yAbs / mainHeightScale);
                float smoothingValue =  (float)terrainSmoothingNoise.eval(xAbs / terrainSmoothingScale, yAbs / terrainSmoothingScale);

                /*
                if (currentLayer.equals(WorldGenLayer.BEACH)) {
                    smoothingValue = (smoothingValue * (float)terrainSmoothingNoise.eval(xAbs / 4, yAbs / 4));
                }
                */
                //float biomeValue =  (float)temperatureNoise.eval(xAbs / biomeScale, yAbs / biomeScale);

                float landmassWithWeight = (landmassValue * landmassWeight);
                float heightWithWeight = (mainHeightValue * heightWeight);
                float smoothingWithWeight = (smoothingValue * smoothingWeight);
                float product = landmassWithWeight + heightWithWeight + smoothingWithWeight;
                float productDividedByWeight = product / (landmassWeight + smoothingWeight + heightWeight);

                float terrainLimit = (float) ((worldDepth) * (2 * Math.pow(Math.abs(productDividedByWeight), 1.85)) * ((product < 0) ? -1 : 1));

                if (Math.abs(terrainLimit) >= worldDepth) {
                    terrainLimit = worldDepth * ((product < 0) ? -1 : 1 );
                }
              //  Gdx.app.log("world_...", String.format("generating at x%s/y%s => terrainLimit: %s - product: %s (layer: %s | landmass: %s (noise: %s) | height: %s | smoothing %s", xAbs, yAbs, terrainLimit, product, currentLayer.name(), landmassWithWeight, landmassNoiseVal, heightWithWeight, smoothingWithWeight));

                float maxZ = (terrainLimit < 0) ? 0 : (float)Math.floor(terrainLimit);

                String tileState = "default";
                Color minimapColor = null;

                switch (currentLayer) {
                    case OCEAN:
                        tileToPlace = TileRegistry.getByTag("water");
                        minimapColor = Color.blue;
                        break;
                    case BEACH:
                        tileToPlace = TileRegistry.getByTag("sand");
                        minimapColor = Color.yellow;
                        break;
                    case LAND:  tileToPlace = TileRegistry.getByTag("grass_0");
                        minimapColor = Color.green;
                        break;
                    case MOUNTAIN:  tileToPlace = TileRegistry.getByTag("stone");
                        minimapColor = Color.gray;
                        break;
                    default:
                        tileToPlace = TileRegistry.getByTag("dirt_0");
                        minimapColor = new Color(128, 64, 0);
                }

                setMinimapPixel((int)xAbs, (int)yAbs, minimapColor, terrainLimit);

                /*
                worldSpace.placeTile(
                        tileToPlace,
                        xAbs, yAbs, terrainLimit,
                        tileState
                );*/

                float temperatureValue = normalizeNoise((float) (temperatureNoise.eval(xAbs / temperatureScale, yAbs / temperatureScale)));

                for (float z = maxZ -5f; z<= maxZ; z+=1f) {
                    tileToPlace = currentLayer.tileAtHeightGenerator.get(z, maxZ, temperatureValue);

                    if (tileToPlace != null) {
                        worldSpace.placeTile(
                                tileToPlace,
                                xAbs, yAbs, z,
                                tileState
                        );
                    }

                }
            }
        }
    }


    public void setMinimapPixel(int x, int y, Color color, float terrainLimit) {
        if (generateMinimap) {
            float alphaValue = normalizeNoise((Math.abs(terrainLimit) > worldDepth ? worldDepth : (terrainLimit / worldDepth)));
            Gdx.app.log("world_...", String.format("rendering worldmap, %s %s | height: %s | color: %s", x, y, terrainLimit,alphaValue));

            Color c = new Color((int)(color.getRed() * alphaValue),
                    (int)(color.getGreen()* alphaValue),
                    (int)(color.getBlue()* alphaValue),
                    255
            );

            try {
                worldHeightMap.setRGB(x+width, y+height, c.getRGB());
            } catch (ArrayIndexOutOfBoundsException e) {
                Gdx.app.log("error", String.format("coordinates %s/%s do not fit into image %s/%s", x+width, y+height, worldHeightMap.getWidth(), worldHeightMap.getHeight()));
                e.printStackTrace();
            }
        }
    }
    public void generateMinimap(int totalWidth, int totalHeight, WorldSpace worldSpace) {
        try {
            ImageIO.write(worldHeightMap, "png", Gdx.files.local("save/" + worldSpace.id + "_map_"+this.seed + ".png").file());
        } catch (IOException e) {
            // handle exception
            Gdx.app.getApplicationLogger().log("WorldSetup", "error writing worldmap!");
        }
    }

    public static float normalizeNoise(float noiseValue) {
        return (noiseValue + 1) / 2;
    }

}
