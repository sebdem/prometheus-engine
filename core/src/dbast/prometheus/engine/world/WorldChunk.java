package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.world.tile.TileData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// TODO migrate prepared sprites and collision data to this for a neat performance boost.
// TODO consider actual world data as well (~~tiles~~, entities etc.)
public class WorldChunk {

    public Vector3 position;
    protected Vector3 screenPosition;

    protected boolean requiredDataUpdate = true;
    protected boolean loaded = false;
    private Map<Vector3, TileData> tileDataMap;

    public Map<Vector3, TileData> visibleTiles;

    public WorldChunk() {
        this.tileDataMap = new HashMap<>();
        this.visibleTiles = new HashMap<>();
        this.load();
    }

    public WorldChunk(Vector3 position) {
        this();
        this.position = position;
        this.screenPosition = LockOnCamera.project_custom(this.position.cpy(),
            WorldSpace.BASE_CHUNK_SIZE,
            0.5f
        );
    }

    public void update(float deltaTime) {
        if (requiredDataUpdate) {

          //  Gdx.app.log("world_chunk", "updating chunk...");
            this.visibleTiles.clear();

            this.visibleTiles.putAll(
                    this.getTileDataMap().entrySet().stream()
                    .filter(vector3TileDataEntry -> {
                        boolean shouldBeVisible = vector3TileDataEntry.getValue().isVisibleFrom(Vector3.Zero);
                        Gdx.app.log("world_chunk", String.format("Tile with %s neighbors is visible = %s", vector3TileDataEntry.getValue().neighbors.size(), shouldBeVisible));
                        return shouldBeVisible;
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

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
        return this;
    }

    /**
     * unloads data
     * @return itself
     */
    public WorldChunk unload() {
        loaded = false;
        return this;
    }

    public Map<Vector3, TileData> getTileDataMap() {
        return tileDataMap;
    }

    public void putTileData(Vector3 inWorldPosition, TileData tileData) {
        this.getTileDataMap().put(inWorldPosition, tileData);
        requiredDataUpdate = true;
    }

    public Map<Vector3, TileData> getVisibleTiles() {
        return this.visibleTiles;
    }
}
