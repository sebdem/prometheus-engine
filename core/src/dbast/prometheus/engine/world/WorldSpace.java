package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.Vector3Comparator;

import java.util.*;

// TODO WorldSpace builder from a level file
public class WorldSpace {

    public Map<Vector3, Tile> terrainTiles;
    public int height;
    public int width;

    public EntityRegistry entities;

    public WorldSpace(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new TreeMap<>(new Vector3Comparator());
    }

    public WorldSpace placeTile(int tileId, float x, float y, float z) {
        this.terrainTiles.put(new Vector3(x, y, z), TileRegistry.get(tileId));
        return this;
    }


    public static WorldSpace testLevel() {
        WorldSpace worldSpace = new WorldSpace(16, 16);

        for(float z = -1f; z < 2; z++ ) {
            for(float y = 0; y < worldSpace.height; y++) {
                for(float x = 0; x < worldSpace.width; x++) {
                    switch ((int)z) {
                        case -1:
                            worldSpace.placeTile(0, x, y, z); break;
                        case 0: {
                            if (x > 0 && x +1 < worldSpace.width) {
                                worldSpace.placeTile(Math.random() > 0.5 ? 2 : 1, x, y, z);
                            }
                        } break;
                        case 1: {
                           // if (y + 2 == worldSpace.height) {
                             if (Math.sqrt(y*y) == Math.sqrt(x*x)) {
                                worldSpace.placeTile(3, x, y, z);
                            }
                        }
                    }
                }
            }
        }

        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                new CollisionBox(1f,1f,false),
                new SizeComponent(1f,1.5f),
                new PositionComponent(3f, 0f),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                SpriteComponent.fromFile(Gdx.files.internal("sprites/player/test_01.png"))
        );

        Texture[] blobTextures = new Texture[]{
                    new Texture(Gdx.files.internal("sprites/enemies/blob_0.png")),
                    new Texture(Gdx.files.internal("sprites/enemies/blob_1.png")),
                    new Texture(Gdx.files.internal("sprites/enemies/blob_2.png"))
        };
        for(int i = 0; i < 100; i++) {
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    PositionComponent.initial(),
                    SpriteComponent.fromTexture(blobTextures[(int)(Math.random() * 3)]),
                    new VelocityComponent((float)((Math.random() * 3) - 1f),(float)((Math.random() * 3) - 1f))
            );
        }

        for(int i = 0; i < 20; i++) {
            SpriteComponent tree = SpriteComponent.fromFile(Gdx.files.internal("world/environment/tree.png"));
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic().setPermeable(false),
                    new SpriteBoundSizeComponent(16f),
                    new PositionComponent((float)Math.floor(Math.random() * worldSpace.width), (float)Math.floor(Math.random() * worldSpace.height)),
                    PositionComponent.initial(),
                    tree
            );
        }

        StateComponent stateComponent =  new StateComponent();
        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(4f, 1f),
                stateComponent,
                StateBasedSpriteComponent
                        .fromFile(Gdx.files.internal("world/objects/chest_locked.png"))
                        .addState("colliding", Gdx.files.internal("world/objects/chest_active.png"))
                        .addState("open", Gdx.files.internal("world/objects/chest_open_1.png"))
                        .bindTo(stateComponent)
        );

        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(6f, 4f),
                SpriteComponent.fromFile(Gdx.files.internal("world/objects/chest_locked.png"))
        );

        return worldSpace;
    }
}
