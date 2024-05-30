package dbast.prometheus.engine.world.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldChunk;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.OpenSimplexNoise;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class PerlinTest {


    public int width;
    public int height;

    public PerlinTest(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public WorldSpace setup() {
        OpenSimplexNoise simplexNoise = new OpenSimplexNoise(System.currentTimeMillis());

        OpenSimplexNoise biomeNoise = new OpenSimplexNoise(simplexNoise.hashCode());

        WorldSpace worldSpace = new WorldSpace(-width, -height, width, height);


        int totalHeight = (height * 2);
        int totalWidth = width * 2;
        int numberOfChunksY = totalHeight / worldSpace.chunkSize/* + 2*/;
        int numberOfChunksX = totalWidth / worldSpace.chunkSize/* + 2*/;

        int perlinScale = 24;
        int biomeScale = 16;

        BufferedImage worldHeightMap = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);

        WorldChunk[] chunks = new WorldChunk[numberOfChunksY * numberOfChunksX];

        for (int y = 0; y < numberOfChunksY; y++) {
            for (int x = 0; x < numberOfChunksX; x++) {
                int index = (y * numberOfChunksX) + x;
                chunks[index] = new WorldChunk();
                chunks[index].setPosition(new Vector3(x - numberOfChunksX / 2, y - numberOfChunksY / 2, 0));
            }
        }

        Arrays.stream(chunks).forEach(
                worldChunk -> {
                Tile tileToPlace = null;

                float chunkY = (worldChunk.getPosition().y)  * worldSpace.chunkSize;
                float chunkX = (worldChunk.getPosition().x) * worldSpace.chunkSize;
                for (float yc = 0; yc < worldSpace.chunkSize; yc++) {
                    float yAbs = yc + chunkY;
                    for (float xc = 0; xc < worldSpace.chunkSize; xc++) {
                        float xAbs = xc + chunkX;

                        float simplexValue =  (float)simplexNoise.eval(xAbs / perlinScale, yAbs / perlinScale);
                        float biomeValue =  (float)biomeNoise.eval(xAbs / biomeScale, yAbs / biomeScale);
                        //Gdx.app.getApplicationLogger().log("WorldSetup", String.format("For X %s and Y %s opensimplex: %s", xAbs, yAbs, simplexValue));

                        float terrainLimit = (int)(simplexValue * perlinScale * biomeValue);
                        float maxZ = (terrainLimit < 0) ? 0 : terrainLimit;


                        if (xAbs >= 0 && xAbs < totalWidth && yAbs >= 0 && yAbs < totalHeight) {
                            int rgbValue = (int) (127 + 120 * (terrainLimit / perlinScale));
                            Color c = new Color(rgbValue, rgbValue, rgbValue,
                                    255
                            );
                            if ( terrainLimit < 0) {
                                c = new Color( 0, 0, c.getBlue(), c.getAlpha());
                            }

                            Gdx.app.log("world_...", String.format("rendering worldmap, %s %s | color: %s", xAbs, yAbs, rgbValue));
                            worldHeightMap.setRGB(
                                    (int)xAbs,
                                    (int)yAbs,
                                   c.getRGB());

                        }
                        String tileState = "default";
                        for (float z = -8f; z<= maxZ; z+=1f) {

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
                                    } else {
                                        tileToPlace = TileRegistry.getByTag("sand");
                                    }
                                }
                            }


                            worldSpace.placeTile(
                                    tileToPlace,
                                    xAbs, yAbs, z,
                                    tileState
                            );
                        }
                    }
                }
            }
        );

        /*
        for (float y = worldSpace.minY; y < worldSpace.height; y++) {
            for (float x = worldSpace.minX; x < worldSpace.width; x++) {
                float simplexValue =  (float)simplexNoise.eval(x / 16, y / 22);
                float biomeValue =  (float)biomeNoise.eval(x / 16, y / 16);
                Gdx.app.getApplicationLogger().log("WorldSetup", String.format("For X %s and Y %s opensimplex: %s", x, y, simplexValue));

                float terrainLimit = (int)(simplexValue *8f);
                float maxZ = (terrainLimit < 0) ? 0 : terrainLimit;
                String tileState = "default";
                for (float z = -8f; z<= maxZ; z+=1f) {

                    if (z > terrainLimit && z <= maxZ) {
                        tileToPlace = TileRegistry.getByTag("water");
                        tileState = "north";
                    } else {
                        if (z < maxZ - 2) {
                            tileToPlace = TileRegistry.getByTag("stone");
                        } else if (z < maxZ) {
                            tileToPlace = TileRegistry.getByTag("dirt_0");
                        } else if (z == maxZ) {
                            tileToPlace = TileRegistry.getByTag("grass_0");
                        }
                    }

                    worldSpace.placeTile(
                            tileToPlace,
                            x, y, z,
                            tileState
                    );
                }
            }
        }*/


        try {
            ImageIO.write(worldHeightMap, "png", Gdx.files.local("save/" + worldSpace.id + "_map.png").file());
        } catch (IOException e) {
            // handle exception
            Gdx.app.getApplicationLogger().log("WorldSetup", "error writing worldmap!");
        }
        Gdx.app.getApplicationLogger().log("WorldSetup", "Generating Entities");
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
        return worldSpace;
    }
}
