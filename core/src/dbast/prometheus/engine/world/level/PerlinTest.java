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
import dbast.prometheus.utils.GeneralUtils;
import net.dermetfan.gdx.math.MathUtils;

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
        int numberOfChunksY = totalHeight / worldSpace.chunkSize + 2;
        int numberOfChunksX = totalWidth / worldSpace.chunkSize + 2;

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
                Tile tileToPlace = null;

                float chunkY = (worldChunk.position.y)  * worldSpace.chunkSize;
                float chunkX = (worldChunk.position.x) * worldSpace.chunkSize;
                for (float yc = 0; yc < worldSpace.chunkSize; yc++) {
                    float yAbs = yc + chunkY;
                    for (float xc = 0; xc < worldSpace.chunkSize; xc++) {
                        float xAbs = xc + chunkX;

                        float simplexValue =  (float)simplexNoise.eval(xAbs / 24, yAbs / 24);
                        float biomeValue =  (float)biomeNoise.eval(xAbs / 16, yAbs / 16);
                        Gdx.app.getApplicationLogger().log("WorldSetup", String.format("For X %s and Y %s opensimplex: %s", xAbs, yAbs, simplexValue));

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
                new RenderComponent()
                        .registerAnimation(Gdx.files.internal("sprites/player/player_idle.png"
                        ), 8, 1, 1.25f, true, "default")
                        .registerAnimation(Gdx.files.internal(
                                "sprites/player/player_moving_down.png"
                        ), 8, 1, 0.125f, true, "moving")
        );
        return worldSpace;
    }
}
