package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.math.Vector3;

import java.util.*;

public class TileRuleEntry extends HashMap<Vector3, Collection<Tile>> {

    public Tile self;

    public TileRuleEntry(Tile self) {
        this.self = self;
    }

    @Override
    public Collection<Tile> get(Object key) {

        return super.getOrDefault(key, Collections.singletonList(self));
    }

    public void put(Vector3 key, Tile... tiles) {
        this.put(key, Arrays.asList(tiles));
    }

    public enum Direction {
        NORTH(new Vector3(0, 1, 0)),
        EAST(new Vector3(1, 0, 0)),
        SOUTH(new Vector3(0, -1, 0)),
        WEST(new Vector3(-1, 0, 0));

        public Vector3 dir;
        Direction(Vector3 dir) {
            this.dir = dir;
        }
    }
}