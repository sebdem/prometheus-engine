package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import dbast.prometheus.engine.base.PositionProvider;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO migrate prepared sprites and collision data to this for a neat performance boost.
// TODO consider actual world data as well (~~tiles~~, entities etc.)
public class WorldChunk implements PositionProvider {

    public static int CHUNK_SIZE = 16;
    private Vector3 position;

    public boolean requiredDataUpdate = true;
    protected boolean loaded = false;
    public Map<Vector3, TileData> tileDataMap;

    public List<BoundingBox> boundaries;
    public List<TileData> visibleTileData;

    protected WorldSpace worldSpace;

    public WorldChunk() {
        this.tileDataMap = new HashMap<>();
        this.boundaries = new ArrayList<>();
        this.visibleTileData = new ArrayList<>();
        // Todo refactor to "load" chunks dynamically eventually
        //this.load();
    }

    public WorldChunk(Vector3 position) {
        this();
        this.setPosition(position);
    }

    public void update(float deltaTime) {
        //Gdx.app.log("ChunkUpdate", String.format("Chunk %s does require an update: %s", this.getPosition(), requiredDataUpdate));
        if (requiredDataUpdate) {
            Gdx.app.log("ChunkUpdate", "Detected World Chunk Data Update");
            // Todo boundary generation could be tied to visible tiles only?
            for (Map.Entry<Vector3, TileData> tiles : this.tileDataMap.entrySet()) {
                // update chunk boundaries
                List<BoundingBox> tileBounds = tiles.getValue().tile.getBoundsForPositon(tiles.getKey());
                if (tileBounds != null && !tileBounds.isEmpty()) {
                    this.boundaries.addAll(tileBounds);
                }
                // update visibleTile Data
                if (tiles.getValue().isVisibleFrom(/*lockOnPosition.position*/)) {
                    this.visibleTileData.add(tiles.getValue());
                }
            }
           // this.boundariesPerChunk.put(chunk.getKey(), chunkBoundaries);
            this.requiredDataUpdate = false;
        }

    }
    // TODO consider dumber version of update, idea is to have update run on any chunks nearby players. Everything else will only simulate. Server may call update on anything anyway?
    public void simulate(float deltaTime) {
        this.update(deltaTime);
    }

    /**
     * loads data from associated source
     * @return itself
     */
    public WorldChunk load() {
        loaded = true;
        requiredDataUpdate = true;
        return this;
    }

    /**
     * unloads data
     * @return itself
     */
    public WorldChunk unload() {
        loaded = false;

        this.boundaries.clear();
        this.visibleTileData.clear();
        return this;
    }

    @Override
    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public void placeTile(Tile tile, float x, float y, float z, String state) {
        Vector3 position = new Vector3(x, y, z);
        TileData tileData = new TileData(tile, getWorldSpace(), position, state);

        placeTile(position, tileData);
    }
    public void placeTile(Vector3 inWorldPosition, TileData tileData) {
        Gdx.app.log("Chunk", String.format("Chunk %s : Placing tile %s at position %s", this.getPosition(), tileData.tile.tag, inWorldPosition));
        this.tileDataMap.put(inWorldPosition, tileData);
        this.requiredDataUpdate = true;
        this.worldSpace.dataUpdate = true;
    }

    public WorldSpace getWorldSpace() {
        return worldSpace;
    }

    public void setWorldSpace(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
    }
}
