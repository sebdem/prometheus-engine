package dbast.prometheus.engine.world.generation;


import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;

import java.util.function.Function;
import java.util.function.Supplier;

/*
//generator originally based on this piece of code
if (z > terrainLimit && z == 0f) {
    tileToPlace = TileRegistry.getByTag("water");
    //    tileState = "north";
} else {
    if (z < maxZ - 2) {
        tileToPlace = TileRegistry.getByTag("stone");
    } else if (z < maxZ) {
        if (temperatureValue > 0.9) {
            tileToPlace = TileRegistry.getByTag("sand");
        } else {
            tileToPlace = TileRegistry.getByTag("dirt_0");
        }
    } else if (z == maxZ) {
        if (z != 0) {

            if (temperatureValue < 0.2) {
                tileToPlace = TileRegistry.getByTag("snow");
            } else if (temperatureValue > 0.9) {
                tileToPlace = TileRegistry.getByTag("sand");
            } else {
                tileToPlace = TileRegistry.getByTag("grass_0");
            }
        } else {
            tileToPlace = TileRegistry.getByTag("sand");
        }
    }
}
 */
public enum WorldGenLayer {

    OCEAN (1, (zCurrent, zMax, temperature) -> {
        Tile result = null;
        if (zCurrent < zMax - 3) {
            result = TileRegistry.getByTag("stone");
        } else if (zCurrent < zMax - 2) {
            result = TileRegistry.getByTag("sand");
        } else if (zCurrent == 0) {
            result = TileRegistry.getByTag("water");
        }
        return result;
    }),
    BEACH (1, (zCurrent, zMax, temperature) -> {
        Tile result = null;
        if (zCurrent < zMax - 3) {
            result = TileRegistry.getByTag("stone");
        } else {
            result = TileRegistry.getByTag("sand");
        }
        return result;
    }),
    LAND(1, (zCurrent, zMax, temperature) -> {
        Tile result = null;
        if (zCurrent < zMax - 2) {
            result = TileRegistry.getByTag("stone");
        } else if (zCurrent < zMax) {
            if (temperature > 0.9) {
                result = TileRegistry.getByTag("sand");
            } else {
                result = TileRegistry.getByTag("dirt_0");
            }
        } else if (zCurrent == zMax) {
            if (temperature < 0.2) {
                result = TileRegistry.getByTag("snow");
            } else if (temperature > 0.9) {
                result = TileRegistry.getByTag("sand");
            } else {
                result = TileRegistry.getByTag("grass_0");
            }
        }
        return result;
    }),
    MOUNTAIN(1, (zCurrent, zMax, temperature) -> TileRegistry.getByTag("stone"));



    public interface LayerGenerator {
        Tile get(float zCurrent, float zMax, float temperature);
    }

    WorldGenLayer(float layerWeight, LayerGenerator tileAtHeightGenerator) {
        this.layerWeight = layerWeight;
        this.tileAtHeightGenerator = tileAtHeightGenerator;
    }

    public final LayerGenerator tileAtHeightGenerator;

    public final float layerWeight;


    public static WorldGenLayer getForValue(float noiseResult) {
        WorldGenLayer result = OCEAN;
        if (noiseResult > 0.85) {
            result = MOUNTAIN;
        } else if (noiseResult > 0.2) {
            result = LAND;
        } else if (noiseResult > -0.1) {
            result = BEACH;
        }

        return result;
    }
}