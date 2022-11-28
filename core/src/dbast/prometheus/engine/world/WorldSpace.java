package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.google.gson.Gson;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.InputControllerComponent;
import dbast.prometheus.engine.entity.components.PositionComponent;
import dbast.prometheus.engine.entity.components.SizeComponent;
import dbast.prometheus.engine.serializing.data.WorldData;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.Vector3Comparator;
import dbast.prometheus.utils.Vector3IndexMap;

import java.util.*;

public class WorldSpace {

    /*
        Basic World data
     */
    public int minX;
    public int minY;
    public int height;
    public int width;
    // TODO introduce boundaries as a cube "playarea", adjust methods accordingly

    public Vector3IndexMap<Tile> terrainTiles;
    public Vector3IndexMap<TileData> tileDataMap;
    public EntityRegistry entities;
    int chunkSize = 16;
    /*
        Time data
     */
    public float age = 720;
    public long realTime;

    public WorldTime worldTime;


    /*
        Generic data for runtime
     */
    public Vector3IndexMap<List<BoundingBox>> boundariesPerChunk;
    // TODO It's chunkin' time
    public Vector3IndexMap<WorldChunk> chunks;

    protected boolean dataUpdate;

    public WorldSpace(int width, int height) {
        this(0,0, width, height);
    }
    public WorldSpace(int minX, int minY, int width, int height) {
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
        this.terrainTiles = new Vector3IndexMap<>(new Vector3Comparator.Planar());
        this.tileDataMap = new Vector3IndexMap<>(new Vector3Comparator.Planar());
        this.dataUpdate = true;
        this.boundariesPerChunk = new Vector3IndexMap<>(new Vector3Comparator.Planar());
        this.worldTime = new WorldTime();
    }


    public void update(float updateDelta) {
        this.age += updateDelta;
        this.realTime = System.currentTimeMillis();

        if (dataUpdate) {
            Vector3IndexMap<List<BoundingBox>> tempBoundaries = new Vector3IndexMap<>(new Vector3Comparator.Planar());

            for (Map.Entry<Vector3, Tile> tileEntry : terrainTiles.entrySet()) {
                Tile tile = tileEntry.getValue();
                Vector3 position = tileEntry.getKey();
                Vector3 chunkPosition = getChunkFor(position);

                List<BoundingBox> chunkBoundaries = tempBoundaries.getOrDefault(chunkPosition, new ArrayList<>());
                BoundingBox tileBounds = tile.getBoundsFor(position);
                if (tileBounds != null) {
                    chunkBoundaries.add(tileBounds);
                    tempBoundaries.put(chunkPosition, chunkBoundaries);
                }
            }

            this.boundariesPerChunk = tempBoundaries;
            dataUpdate = false;
        }

    //   Gdx.app.getApplicationLogger().log("World", String.format("Current age: %s | RealTime: %s | CurrentTime: %s | currentOClock %s", this.age, realTime, currentTime.name(), (this.age / 60) % 24 ));
    }


    public Vector3 getChunkFor(Vector3 worldPosition) {
        return new Vector3(
                (float)(Math.floor(worldPosition.x / chunkSize) * chunkSize),
                (float)(Math.floor(worldPosition.y / chunkSize) * chunkSize),
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
        this.terrainTiles.put(new Vector3(x, y, z), tile);
        this.dataUpdate = true;
        return this;
    }
    public Tile lookupTile(float x, float y, float z) {
        return this.terrainTiles.getOrDefault(new Vector3(x, y, z), null);
    }
    public Tile lookupTile(Vector3 vector3) {
        return this.terrainTiles.getOrDefault(vector3,  null);
    }


    public WorldSpace removeTile(float x, float y, float z) {
        this.terrainTiles.remove(new Vector3(x, y, z));
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
                    1f
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


    public void persist() {
        FileHandle file = Gdx.files.local("save/world_" + (System.nanoTime() / 1000) + ".json");
        file.writeString(new Gson().toJson(new WorldData(this)), false);
        //Gdx.app.getClipboard().setContents();
    }

    public float getTopZ(Vector3 entityPos) {
        Vector3 topMost = this.terrainTiles.keySet().stream().filter(key -> key.x == Math.floor(entityPos.x) && key.y ==  Math.floor(entityPos.y)).max((key1, key2) -> Float.compare(key1.z, key2.z)).orElse(new Vector3(0,0,0));
        return topMost.z;
    }
    public void toNextUpperLevel(Vector3 entityPos) {
        Vector3 topMost = this.terrainTiles.keySet().stream()
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
}
