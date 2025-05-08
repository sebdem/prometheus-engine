package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.components.PositionComponent;
import dbast.prometheus.engine.entity.components.StateComponent;
import dbast.prometheus.engine.world.Direction;
import dbast.prometheus.engine.world.WorldSpace;

import java.util.*;

public class TileData {
// TODO check if this can be nicely generated from serialized data. I have my doubts.
    public Tile tile;
    public StateComponent stateComponent;

    public PositionComponent positionComponent;
    protected WorldSpace worldSpace;

    public Map<Direction, TileData> neighbors;
    public TileData() {
    }
    public TileData(Tile referencedTile, WorldSpace worldSpace, Vector3 position, String state) {
        super();
        this.tile = referencedTile;
        this.positionComponent = PositionComponent.ofVector3(position);
        this.stateComponent = new StateComponent();
        this.stateComponent.setState(state);

        this.setWorldSpace(worldSpace);
    }

    public TileData updateNeighbors() {
        this.neighbors = new HashMap<>();
        TileData adjacentTile = null;
        for(Direction dirEnum : Direction.values()) {
            adjacentTile = this.worldSpace.lookupTileDataAbsolute(positionComponent.position.cpy().add(dirEnum.dir));
            if (adjacentTile != null) {
                this.neighbors.put(dirEnum, adjacentTile);
                adjacentTile.neighbors.put(dirEnum.invert(), this);
            }
        }
        return this;
    }

    public TileData getNeighbor(Direction dir) {
        return this.neighbors.get(dir);
    }

    public TileData setWorldSpace(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
        return updateNeighbors();
    }

    public boolean isVisibleFrom(/*Vector3 pointOfView*/) {
        // TODO find a way to make this 'smart': based on pointOfView, different directions will be checked, wheather we have a "full, blocking" sprite as a neighbor or not
        if (neighbors.containsKey(Direction.UP)
                && neighbors.containsKey(Direction.SOUTH)
                && neighbors.containsKey(Direction.WEST)) {
            TileData neighborUp = neighbors.get(Direction.UP);
            return !neighborUp.neighbors.containsKey(Direction.UP)
                    || !neighborUp.neighbors.containsKey(Direction.SOUTH)
                    || !neighborUp.neighbors.containsKey(Direction.WEST);
        } else {
            return true;
        }
    }
}
