package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.Gson;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.serializing.data.WorldData;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;
import dbast.prometheus.utils.Vector3Comparator;
import dbast.prometheus.utils.Vector3IndexMap;

import java.util.Map;
import java.util.TreeMap;

// TODO WorldSpace builder from a level file
public class WorldSpace {

    public int height;
    public int width;
    public Vector3IndexMap<Tile> terrainTiles;
    public EntityRegistry entities;

    public WorldSpace(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new Vector3IndexMap<>(new Vector3Comparator.Planar());
    }

    public WorldSpace placeTile(int tileId, float x, float y, float z) {
        this.terrainTiles.put(new Vector3(x, y, z), TileRegistry.get(tileId));
        return this;
    }
    public WorldSpace placeTile(Tile tile, float x, float y, float z) {
        this.terrainTiles.put(new Vector3(x, y, z), tile);
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
        return this;
    }

    public boolean isValidPosition(Vector3 position) {
       /* Tile positionTile = lookupTile(position);

        return positionTile != null;//&& !positionTile.tag.equals("water");*/

        return GeneralUtils.isBetween(position.x, 0, this.width -1, true)
                && GeneralUtils.isBetween(position.y, 0, this.height -1, true);
    }
    public boolean isOccupied(Vector3 position) {
        return lookupTile(position) == null;
       /* Tile positionTile = lookupTile(position);

        return positionTile != null;//&& !positionTile.tag.equals("water");*/
    }
    public boolean canStandOn(Vector3 position) {
        Tile atPosition = lookupTile(position);
        Tile underPosition = lookupTile(position.cpy().sub(0,0,1f));
        return atPosition == null & !(underPosition == null || underPosition.tag.equals("water"));
       /* Tile positionTile = lookupTile(position);

        return positionTile != null;//&& !positionTile.tag.equals("water");*/
    }

    public Vector3 getSpawnPoint() {
        boolean isValidPosition = false;
        Vector3 targetPosition = new Vector3(0,0,0);
        int attempts = 0;
        do {
            targetPosition.set(
                    (float) (MathUtils.random(0, this.width)),
                    (float) (MathUtils.random(0, this.height)),
                    1f
            );
            attempts++;
            isValidPosition = this.isValidPosition(targetPosition) && this.canStandOn(targetPosition);
        } while (!isValidPosition && attempts < 100);
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

}
