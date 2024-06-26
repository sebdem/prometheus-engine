package dbast.prometheus.engine.serializing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.google.gson.Gson;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.serializing.data.WorldData;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;

public class WorldMapLoader extends AbstractLoader<WorldSpace> {

    public WorldData data;

    @Override
    public WorldSpace build() {
        WorldSpace buildResult = new WorldSpace(data.width, data.height);
        // placeTiles
        data.tiles.forEach((tileString, positions) -> {
            Tile tile = TileRegistry.getByTag(tileString);
            positions.forEach(positionXYZ ->{
                buildResult.placeTile(tile, positionXYZ[0],positionXYZ[1], positionXYZ[2]);
            });
        });
        // TODO rebuiltEntities...
        data.entities.forEach((entityData) -> {
            Gdx.app.getApplicationLogger().log("WorldMapLoader", String.format("Found entity %s but cannot build", entityData.id));
        });

        // built player entity
        buildResult.entities = new EntityRegistry();
        buildResult.entities.addNewEntity(
                1L,
                CollisionBox.createBasic(),
                new SizeComponent(1f,1f),
                new PositionComponent(buildResult.getSpawnPoint()),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                new StateComponent(),
                RenderComponent.playerRenderComponent()
        );

        return buildResult;
    }


    public static WorldMapLoader fromJson(FileHandle fileHandle) {
        WorldMapLoader worldMapLoader = new WorldMapLoader();
        worldMapLoader.data = new Gson().fromJson(fileHandle.reader(), WorldData.class);
        return worldMapLoader;
    }
}
