package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;

public class WorldSpace {

    public int[][] terrainTiles;
    public int height;
    public int width;

    public EntityRegistry entities;

    public WorldSpace(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new int[height][width];
    }


    public static WorldSpace testLevel() {
        int[][] level = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0},
                {2, 1, 1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 1, 0, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 1},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2},
                {2, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 2, 2},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0},
                {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 0},
        };

        WorldSpace worldSpace = new WorldSpace(level[0].length, level.length);

        worldSpace.terrainTiles = level;
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
