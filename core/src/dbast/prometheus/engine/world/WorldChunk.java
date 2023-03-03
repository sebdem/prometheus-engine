package dbast.prometheus.engine.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.world.tile.TileData;

import java.util.HashMap;
import java.util.Map;

// TODO migrate prepared sprites and collision data to this for a neat performance boost.
// TODO consider actual world data as well (~~tiles~~, entities etc.)
public class WorldChunk {

    public Vector3 position;
    protected Vector3 screenPosition;

    protected boolean requiredDataUpdate = true;
    protected boolean loaded = false;
    public Map<Vector3, TileData> tileDataMap;

    public WorldChunk() {
        this.tileDataMap = new HashMap<>();
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
            // todo update other data based on actual data;

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
}
