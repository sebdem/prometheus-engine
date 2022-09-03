package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.math.Vector3;

import java.util.*;

public class TileRules {

    public HashMap<Tile, TileRuleEntry> allowedTilesPerVectorPerTile;

    public TileRules() {
        this.allowedTilesPerVectorPerTile = new HashMap<>();
    }

    public TileRuleEntry forTile(String tileTag) {
        return forTile(TileRegistry.getByTag(tileTag));
    }
    public TileRuleEntry forTile(Tile tile) {
        if (!allowedTilesPerVectorPerTile.containsKey(tile)) {
            allowedTilesPerVectorPerTile.put(tile, new TileRuleEntry(tile));
        }
       return allowedTilesPerVectorPerTile.get(tile);
    }

    public List<Tile> allowedForThisOffset(List<Tile> tilesAtOffset, Vector3 offset) {
        Set<Tile> allowedForOrigin = new HashSet<>();
        allowedTilesPerVectorPerTile.forEach((originTile,ruleEntry) -> {
            List<Tile> entryTilesAtOffset = new ArrayList<>(ruleEntry.get(offset));
            if (tilesAtOffset.isEmpty() || entryTilesAtOffset.containsAll(tilesAtOffset)) {
                allowedForOrigin.add(originTile);
            }
        });
        return new ArrayList<>(allowedForOrigin);
    }
}
