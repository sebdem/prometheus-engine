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

}