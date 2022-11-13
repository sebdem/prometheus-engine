package dbast.prometheus.engine.world;

// TODO migrate prepared sprites and collision data to this for a neat performance boost.
// TODO consider actual world data as well (tiles, entities etc.)
public class WorldChunk {

    protected boolean requiredDataUpdate = true;

    public void update(float deltaTime) {
        if (requiredDataUpdate) {
            // todo update other data based on actual data;

            this.requiredDataUpdate = false;
        }

    }
    // TODO consider dumber version of update, idea is to have update run on any chunks nearby players. Everything else will only simulate. Server may call update on anything anyway?
    public void simulate(float deltaTime) {

    }
}
