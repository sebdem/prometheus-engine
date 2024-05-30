package dbast.prometheus.engine.serializing;

import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import dbast.prometheus.engine.serializing.data.WorldChunkData;
import dbast.prometheus.engine.serializing.data.WorldData;
import dbast.prometheus.engine.world.WorldChunk;

public class WorldChunkLoader extends AbstractLoader<WorldChunk> {

    public WorldChunkData data;

    @Override
    public WorldChunk build() {

        return null;
    }

    public static WorldChunkLoader fromJson(FileHandle fileHandle) {
        WorldChunkLoader worldChunkLoader = new WorldChunkLoader();
        worldChunkLoader.data = new Gson().fromJson(fileHandle.reader(), WorldChunkData.class);
        return worldChunkLoader;
    }
}
