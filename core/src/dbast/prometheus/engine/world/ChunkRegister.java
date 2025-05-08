package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.serializing.WorldChunkLoader;
import dbast.prometheus.engine.serializing.data.PositionHash;
import dbast.prometheus.engine.world.generation.WorldGenerator;
import dbast.prometheus.utils.Vector3Comparator;
import dbast.prometheus.utils.Vector3IndexMap;

import java.util.*;

// Serverside
public class ChunkRegister {

    protected List<PositionHash> knownChunks;

    protected Set<PositionHash> requestedChunks;

    protected Vector3IndexMap<WorldChunk> loadedChunks;

    protected WorldGenerator generator;
    protected WorldSpace world;

    public ChunkRegister(WorldSpace world, WorldGenerator generator) {
        this.world = world;
        this.generator = generator;
        this.knownChunks = new ArrayList<>();
        this.requestedChunks = new LinkedHashSet<>();
        this.loadedChunks = new Vector3IndexMap<>(new Vector3Comparator.Planar());

    }

    // TODO segregate into optionals and "chunk load requests" integrated into engine update logic.
    public Set<WorldChunk> getChunks(Vector3... chunkPositions) {
        Set<WorldChunk> results = new HashSet<>();
        for (Vector3 chunkPosition : chunkPositions) {
            WorldChunk result = this.getChunk(chunkPosition);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }
    public WorldChunk getChunk(Vector3 chunkPos) {
        PositionHash queryHash = new PositionHash(chunkPos);

        if (loadedChunks.containsKey(chunkPos)) {
            Gdx.app.log("ChunkRegistry", String.format("Chunk %s found and loaded", queryHash));

            return loadedChunks.getOrDefault(chunkPos, null);
        } else if (knownChunks.contains(queryHash)) {
            requestedChunks.add(queryHash);

            Gdx.app.log("ChunkRegistry", String.format("Chunk %s does not appear to exist as a file", queryHash));
            return null;
        } else {
            Gdx.app.log("ChunkRegistry", String.format("Chunk %s does not appear to exist yet. Starting Generator...", queryHash));
            return createNewChunk(chunkPos, queryHash);
        }
    }


    public WorldChunk getLoadedChunk(Vector3 chunkPos) {
        return loadedChunks.getOrDefault(chunkPos, null);
    }

    public Vector3IndexMap<WorldChunk> getLoadedChunks() {
        return this.loadedChunks;
    }
    public List<PositionHash> getKnownChunks() {
        return this.knownChunks;
    }

    public void update(float updateDelta) {
        for (PositionHash chunkHash : this.requestedChunks) {
            loadScheduledChunk(updateDelta, chunkHash);
        }
        this.requestedChunks.clear();

        // TODO always load nearest chunks into memory, instead of just the one the player is in
        for (WorldChunk chunk : this.loadedChunks.values()) {
            chunk.update(updateDelta);
        }
    }

    public void loadScheduledChunk(float updateDelta, PositionHash chunkHash) {
        Gdx.app.log("ChunkRegistry", String.format("Chunk %s found but not loaded. Checking file...", chunkHash));
        FileHandle chunkFile = Gdx.files.local(String.format("save/%s/data/%s.json", world.id, chunkHash.chunkHash));

        if (chunkFile.exists()) {
            WorldChunkLoader chunkLoader = WorldChunkLoader.fromJson(chunkFile);
            WorldChunk loadedChunk = chunkLoader.build();
            loadedChunk.load();

            this.loadedChunks.put(chunkHash.getPosition(), loadedChunk);
        }
    }

    protected WorldChunk createNewChunk(Vector3 chunkPos, PositionHash positionHash) {
        WorldChunk newChunk = new WorldChunk();
        newChunk.setPosition(chunkPos);
        newChunk.setWorldSpace(this.world);
        newChunk.load();

        this.loadedChunks.put(chunkPos, newChunk);
        knownChunks.add(positionHash);

        Gdx.app.log("ChunkRegistry", String.format("Added Chunk %s to world %s at %s - Populating with tiles...", positionHash, this.world.id, chunkPos));

        return generator.populateChunk(chunkPos.x, chunkPos.y, newChunk);
    }

}
