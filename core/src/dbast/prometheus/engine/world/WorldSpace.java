package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.Gson;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.InputControllerComponent;
import dbast.prometheus.engine.entity.components.PositionComponent;
import dbast.prometheus.engine.entity.components.SizeComponent;
import dbast.prometheus.engine.serializing.data.WorldData;
import dbast.prometheus.engine.world.generation.WorldGenerator;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.Vector3Comparator;
import dbast.prometheus.utils.Vector3IndexMap;

import java.util.*;


// TODO remove old chunk property.
// TODO fix serialization to depend on the new ChunkRegistry class
public class WorldSpace {

    public String id;

    /*
        Basic World data
     */
    public int minX;
    public int minY;
    public int height;
    public int width;
    // TODO introduce boundaries as a cube "playarea", adjust methods accordingly

    public EntityRegistry entities;
    /*
        Time data
     */
    public float age = 720;
    public long realTime;

    public WorldTime worldTime;


    /*
        Generic data for runtime
     */
    @Deprecated
    public Vector3IndexMap<WorldChunk> chunks;

    @Deprecated //?
    protected boolean dataUpdate;

    protected WorldGenerator generator;

    protected ChunkRegister chunkRegister;

    public WorldSpace(int width, int height) {
        this(0,0, width, height);
    }
    public WorldSpace(int minX, int minY, int width, int height) {
        this.id ="world_" + (System.nanoTime() / 1000);
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
        //this.terrainTiles = new Vector3IndexMap<>(new Vector3Comparator.Planar());
        //this.tileDataMap = new HashMap<>();
        this.dataUpdate = true;
        this.worldTime = new WorldTime();
        this.chunks = new Vector3IndexMap<>(new Vector3Comparator.Planar());
    }

    public void attachGenerator(WorldGenerator newGenerator) {
        this.generator = newGenerator;
        this.dataUpdate = true;
    }


    public void update(float updateDelta) {
        this.age += updateDelta;
        this.realTime = System.currentTimeMillis();

        //if (dataUpdate) {
        //    Gdx.app.log("WorldUpdate", "Detected World Data Update");

            if (chunkRegister != null) {
                chunkRegister.update(updateDelta);
            }

            dataUpdate = false;
        //    Gdx.app.log("WorldUpdate", "Finished World Data Update");
        //}

    //   Gdx.app.getApplicationLogger().log("World", String.format("Current age: %s | RealTime: %s | CurrentTime: %s | currentOClock %s", this.age, realTime, currentTime.name(), (this.age / 60) % 24 ));
    }


    public Vector3 convertToChunkPos(Vector3 worldPosition) {
        return new Vector3(
                (float)(Math.floor(worldPosition.x / WorldChunk.CHUNK_SIZE) * WorldChunk.CHUNK_SIZE),
                (float)(Math.floor(worldPosition.y / WorldChunk.CHUNK_SIZE) * WorldChunk.CHUNK_SIZE),
                0f
                //(Math.floor(worldPosition.z / chunkSize) * chunkSize),
        );
    }

    public Color getSkyboxColor() {
        return this.worldTime.getSkyboxColor(this.age);
        //return this.currentTime.getSkyboxColor(this.age);
    }

    public float getSightRange() {
      //  return this.currentTime.getSightRange(this.age);
        return this.worldTime.getSightRange(this.age);
        //return 50f;
    }

    public WorldSpace placeTile(int tileId, float x, float y, float z) {
        return placeTile(TileRegistry.get(tileId), x, y, z);
    }
    public WorldSpace placeTile(Tile tile, float x, float y, float z) {
        return placeTile(tile, x, y, z, "default");
    }
    public WorldSpace placeTile(Tile tile, float x, float y, float z, String state) {
        Vector3 position = new Vector3(x, y, z);
       // this.terrainTiles.put(position, tile);
        TileData tileData = new TileData(tile, this, position, state);
       // this.tileDataMap.put(position, tileData);
        this.placeInChunk(position, tileData);
        this.dataUpdate = true;
        return this;
    }

    @Deprecated
    public WorldChunk placeInChunk(Vector3 inWorldPosition, TileData tileData) {
        Vector3 chunkPos = this.convertToChunkPos(inWorldPosition);
        WorldChunk chunk = this.chunks.getOrDefault(chunkPos, new WorldChunk(chunkPos));
        chunk.tileDataMap.put(inWorldPosition, tileData);
        chunk.requiredDataUpdate = true;
        this.chunks.put(chunkPos, chunk);
        return chunk;
    }
    public Tile lookupTile(float x, float y, float z) {
        return lookupTile(new Vector3(x, y, z));
    }
    public Tile lookupTile(Vector3 vector3) {
        TileData tileData = lookupTileDataAbsolute(vector3);
        return tileData == null ? null : tileData.tile;
    }
    public TileData lookupTileDataAbsolute(Vector3 vector3) {
        Vector3 targetChunk = convertToChunkPos(vector3);

        WorldChunk chunk = chunkRegister.getLoadedChunk(targetChunk);;
        if (chunk == null) {
            return null;
        } else {
            return chunk.tileDataMap.getOrDefault(vector3,  null);
        }
    }


    public WorldSpace removeTile(float x, float y, float z) {
        //this.terrainTiles.remove();
        Vector3 targetPosition = new Vector3(x, y, z);
        Vector3 targetChunk = convertToChunkPos(targetPosition);
        this.chunks.get(targetChunk).tileDataMap.remove(targetPosition);
        this.chunks.get(targetChunk).requiredDataUpdate = true;
        this.dataUpdate = true;
        return this;
    }

    public boolean isPositionInWorld(Vector3 position) {
       /* Tile positionTile = lookupTile(position);

        return positionTile != null;//&& !positionTile.tag.equals("water");*/
        return  position.x >= minX && position.x < this.width &&
                position.y >= minY && position.y < this.height;
    }
    public boolean isPositionFree(Vector3 position) {
        return lookupTile(position) == null;
       /* Tile positionTile = lookupTile(position);

        return positionTile != null;//&& !positionTile.tag.equals("water");*/
    }
    public boolean canStandIn(Vector3 position) {
        Tile underPosition = lookupTile(position.cpy().sub(0,0,1f));
        Tile atPosition = lookupTile(position);
        return !(
                underPosition == null || underPosition.tag.equals("water")
        ) && atPosition == null
        ;
       /* Tile positionTile = lookupTile(position);

        return positionTile != null;//&& !positionTile.tag.equals("water");*/
    }

    public Vector3 getSpawnPoint() {
        boolean isValidPosition = false;
        Vector3 targetPosition = new Vector3(0,0,0);
        int attempts = 0;
        do {
            targetPosition.set(
                    (float) (MathUtils.random(minX, this.width)),
                    (float) (MathUtils.random(minY, this.height)),
                    10f
            );
            attempts++;
            isValidPosition = this.isPositionInWorld(targetPosition) && this.canStandIn(targetPosition);
        } while (!isValidPosition && attempts < 100);
        return targetPosition;
    }

    public Vector3 getRandomInRangeOf(Entity entity, Vector3 currentPosition, int range) {
        boolean isValidPosition = false;
        Vector3 targetPosition;
        int attempts = 0;
        do {
            targetPosition = currentPosition.cpy().add(
                    (float) (MathUtils.random(-range, range)),
                    (float) (MathUtils.random(-range, range)),
                    0f
            );
            attempts++;
            isValidPosition = this.isPositionInWorld(targetPosition) && this.canStandIn(targetPosition);
        } while (!isValidPosition && attempts < 100);
        //Gdx.app.getApplicationLogger().log("WorldSpace", String.format("Entity %s Found valid?%s target %s in range for origin %s", entity.getId(), isValidPosition, targetPosition.toString(), currentPosition.toString()));
        if (!isValidPosition) {
            targetPosition = currentPosition;
        }
        return targetPosition;
    }


    //TODO map to new ChunkRegister concept
    public void persist() {
        WorldData worldData = new WorldData(this);
        Gson gsonMapper = new Gson();

        FileHandle file = Gdx.files.local(String.format("save/%s", this.id));
        file.mkdirs();

        file.child("world.json").writeString(gsonMapper.toJson(worldData), false);

        FileHandle chunkFolder = file.child("data");
        chunkFolder.mkdirs();
        worldData.chunkData.forEach((chunkHash, chunkData) -> {
            FileHandle chunkFile = chunkFolder.child(chunkHash + ".json");// Gdx.files.local(String.format("save/%s/data/%s.json", this.id, chunkHash));
            try {
                chunkFile.writeString(gsonMapper.toJson(chunkData), false);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        //Gdx.app.getClipboard().setContents();
    }

    public void toNextUpperLevel(Vector3 entityPos) {
        Vector3 chunkPos = convertToChunkPos(entityPos);
        Vector3 topMost = this.chunks.get(chunkPos).tileDataMap.keySet().stream()
                .filter(key -> key.x == Math.floor(entityPos.x) && key.y ==  Math.floor(entityPos.y) && key.z <= Math.floor(entityPos.z + 1f))
                .max((key1, key2) -> Float.compare(key1.z, key2.z))
            .orElse(new Vector3(0,0,0));
        entityPos.z = topMost.z + 1f;
    }

    public Entity getCameraFocus() {
        return this.entities.values().stream().filter(entity ->
                entity.hasComponent(InputControllerComponent.class) && entity.getComponent(InputControllerComponent.class).active
        ).findAny().orElse(
                this.entities.values().stream().filter(entity ->
                        entity.hasComponents(Arrays.asList(SizeComponent.class, PositionComponent.class))
                ).findAny().orElse(
                        this.entities.values().toArray(new Entity[0])[0]
                )
        );
    }

    public ChunkRegister getChunkRegister() {
        if (chunkRegister == null) {
            this.chunkRegister = new ChunkRegister(this, generator);
            this.dataUpdate = true;
        }
        return chunkRegister;
    }
}
