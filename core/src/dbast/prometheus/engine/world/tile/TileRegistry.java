package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.GsonBuilder;
import dbast.prometheus.engine.serializing.TileLoader;

import java.util.Map;
import java.util.TreeMap;

public class TileRegistry {
    private static final Map<Integer, Tile> registry = new TreeMap<>();

    public static void register(Tile... newTile) {
        int size = registry.size();

        int nextIndex = size > 0 ? registry.keySet().stream().max(Integer::compareTo).orElse(size - 1) + 1: 0;

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

    public static boolean registerPath(FileHandle registryHandle) {
        if (registryHandle.exists()) {
            if(registryHandle.isDirectory()) {
                FileHandle[] directoryContents = registryHandle.list(((dir, name) -> !name.startsWith("ig_")));
                for(int i = 0; i < directoryContents.length; i++) {
                    boolean success = registerPath(directoryContents[i]);
                    if (!success) {
                        Gdx.app.getApplicationLogger().log("TileRegistry", String.format("Failed to register file: %s", directoryContents[i].path()));
                    } else {
                        Gdx.app.getApplicationLogger().log("TileRegistry", String.format("Successfully registered file: %s", directoryContents[i].path()));

                    }
                }
            } else {
                try {
                    TileRegistry.register(TileLoader.fromJson(registryHandle).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    Gdx.app.getApplicationLogger().log("TileRegistry", String.format("Failed to register file: %s", registryHandle.path()));
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static void output() {
        Gdx.app.getClipboard().setContents(
                new GsonBuilder().setPrettyPrinting().create().toJson(TileRegistry.registry));
    }
}
