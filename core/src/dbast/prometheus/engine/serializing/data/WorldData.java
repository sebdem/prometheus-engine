package dbast.prometheus.engine.serializing.data;

import dbast.prometheus.engine.world.WorldSpace;

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
    @Deprecated
    public Map<String, ArrayList<float[]>> tiles;
    public List<PositionHash> chunks;
    public List<EntityData> entities;

    public transient Map<String, WorldChunkData> chunkData;

    public WorldData() {
    }

    public WorldData(WorldSpace worldSpace) {
        super();
        // metadata
        this.width = worldSpace.width;
        this.height = worldSpace.height;

        // tiles
        this.tiles = new HashMap<>();

        this.chunks = new ArrayList<>();
        this.chunkData = new HashMap<>();


        chunks.addAll(worldSpace.getChunkRegister().getKnownChunks());
        // TODO persisting of Chunks
        worldSpace.getChunkRegister().getLoadedChunks().forEach((vector3, chunk) -> {
            PositionHash chunkPosition = new PositionHash(chunk);
            if (!this.chunks.contains(chunkPosition)) {
                this.chunks.add(chunkPosition);
            }
            this.chunkData.put(chunkPosition.chunkHash, new WorldChunkData(chunk, chunkPosition));
        });

        /*
        for (Map.Entry<Vector3, Tile> entry : worldSpace.terrainTiles.entrySet()) {
            ArrayList<float[]> positionsOfTile = this.tiles.getOrDefault(entry.getValue().tag, new ArrayList<>());
            positionsOfTile.add(new float[]{entry.getKey().x,entry.getKey().y, entry.getKey().z});
            tiles.put(entry.getValue().tag, positionsOfTile);
        }*/
        // entities
        this.entities = worldSpace.entities.values().stream().map(EntityData::new).collect(Collectors.toList());
    }
}
