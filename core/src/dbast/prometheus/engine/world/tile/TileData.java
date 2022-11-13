package dbast.prometheus.engine.world.tile;

import dbast.prometheus.engine.entity.components.StateComponent;

public class TileData {

    // TODO migrate to entity components
    public String state;

    public TileData() {
    }
    public TileData(String state) {
        super();
        this.state = state;
    }
}
