package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class WorldSpaceVariable {

    public Map<Vector3, Integer> terrainTiles;
    public int height;
    public int width;

    public EntityRegistry entities;

    public WorldSpaceVariable(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new TreeMap<>(new Comparator<Vector3>() {
            @Override
            public int compare(Vector3 o1, Vector3 o2) {
                // ignore x for now
                int zComp = Float.compare(o1.z, o2.z);
                int yComp = Float.compare(o1.y, o2.y);
                int xComp = Float.compare(o1.x, o2.x);
                return (zComp == 0) ?  (yComp == 0) ?  xComp  : yComp : zComp;
            }
        });
    }

    public WorldSpaceVariable placeTile(int tileId, float x, float y, float z) {
        this.terrainTiles.put(new Vector3(x, y, z), tileId);
        return this;
    }


    public static WorldSpaceVariable testLevel() {

        WorldSpaceVariable worldSpace = new WorldSpaceVariable(20, 20);
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
                            if (y + 1 == worldSpace.height) {
                                worldSpace.placeTile(3, x, y, z);
                            }
                        }
                    }
                }
            }
        }

        //GeneralUtils.populate2DInt(tilePlane.terrainTiles, 0, 3);

        worldSpace.entities = new EntityRegistry();
        worldSpace.entities.addNewEntity(
                1L,
                new CollisionBox(0.9f,1.45f,false),
                new SizeComponent(1f,1.5f),
                PositionComponent.initial(),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                SpriteComponent.fromFile(Gdx.files.internal("world/environment/tree.png"))
        );

        for(int i = 0; i < 18; i++) {
            worldSpace.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    PositionComponent.initial(),
                    SpriteComponent.fromFile(Gdx.files.internal("sprites/enemies/blob_" + (int)(Math.random() * 3)+ ".png")),
                    new VelocityComponent((float)((Math.random() * 3) - 1f),(float)((Math.random() * 3) - 1f))
            );
        }

        StateComponent stateComponent =  new StateComponent();
        worldSpace.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(4f, 4f),
                stateComponent,
                StateBasedSpriteComponent
                        .fromFile(Gdx.files.internal("world/objects/chest_locked.png"))
                        .addState("colliding", Gdx.files.internal("world/objects/chest_open_1.png"))
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
