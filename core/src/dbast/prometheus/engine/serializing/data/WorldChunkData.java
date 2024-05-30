package dbast.prometheus.engine.serializing.data;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.world.WorldChunk;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.utils.GeneralUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorldChunkData extends TileContainerData implements Serializable {

  //  public int chunkSize;
    public String chunkHash;

    public WorldChunkData(WorldChunk chunk, PositionHash chunkPosition) {
        super();
        this.chunkHash = chunkPosition.chunkHash;
        this.fillContainerData(chunkPosition, chunk.tileDataMap);
    }
}
