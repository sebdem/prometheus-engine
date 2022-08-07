package dbast.prometheus.engine.serializing.data;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.tile.Tile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldData implements Serializable {
    public int width;
    public int height;
   // public Map<String, ArrayList<Vector3>> tiles;
    public Map<String, ArrayList<float[]>> tiles;
    public List<EntityData> entities;

    public WorldData() {
    }

    public WorldData(WorldSpace worldSpace) {
        super();
        // metadata
        this.width = worldSpace.width;
        this.height = worldSpace.height;

        // tiles
        this.tiles = new HashMap<>();
        for (Map.Entry<Vector3, Tile> entry : worldSpace.terrainTiles.entrySet()) {
            ArrayList<float[]> positionsOfTile = this.tiles.getOrDefault(entry.getValue().tag, new ArrayList<>());
            positionsOfTile.add(new float[]{entry.getKey().x,entry.getKey().y, entry.getKey().z});
            tiles.put(entry.getValue().tag, positionsOfTile);
        }
        // entities
        this.entities = worldSpace.entities.stream().map(EntityData::new).collect(Collectors.toList());
    }
}
