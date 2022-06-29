package dbast.prometheus.engine.world.tile;

import java.util.Map;
import java.util.TreeMap;

public class TileRegistry {
    private static final Map<Integer, Tile> registry = new TreeMap<>();

    public static void register(Tile... newTile) {
        int size = registry.size();
        int nextIndex = size > 0 ? registry.keySet().toArray(new Integer[0])[size - 1] : 0;

        for(Tile tile : newTile) {
            registry.put(nextIndex, tile);
            nextIndex++;
        }
    }
    public static void register(Tile tile, int tileId) {
        registry.put(tileId, tile);
    }

    public static Tile get(int tileId) {
        return registry.getOrDefault(tileId, Tile.MISSING_TEXTURE);
    }
    public static Tile getByTag(String tileTag) {
        return registry.values().stream().filter(tile -> tile.tag.equals(tileTag)).findFirst().orElse(Tile.MISSING_TEXTURE);
    }

    public static Integer idOfTag(String tileTag) {
        return registry.entrySet().stream().filter(integerTileEntry -> integerTileEntry.getValue().tag.equals(tileTag)).map(Map.Entry::getKey).findFirst().orElse(-1);
    }
}
