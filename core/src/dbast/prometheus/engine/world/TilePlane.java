package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.utils.GeneralUtils;
import net.dermetfan.gdx.utils.ArrayUtils;

import java.util.List;

public class TilePlane {

    public int[][] terrainTiles;
    public int height;
    public int width;

    public List<Entity> entities;

    public TilePlane(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new int[height][width];
    }


    public static TilePlane testLevel() {
        TilePlane tilePlane = new TilePlane(16,9);

        GeneralUtils.populate2DInt(tilePlane.terrainTiles, 0, 3);

       // Gdx.app.getApplicationLogger().log("Level:", tilePlane.terrainTiles);
        return tilePlane;
    }


}
