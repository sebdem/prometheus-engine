package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.EntityRegistry;
import dbast.prometheus.engine.entity.components.*;

@Deprecated
public class WorldSpaceDeprecated {

    public int[][] terrainTiles;
    public int height;
    public int width;

    public EntityRegistry entities;

    public WorldSpaceDeprecated(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new int[height][width];
    }


    public static WorldSpaceDeprecated testLevel() {
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

        WorldSpaceDeprecated worldSpaceDeprecated = new WorldSpaceDeprecated(level[0].length, level.length);

        worldSpaceDeprecated.terrainTiles = level;
        //GeneralUtils.populate2DInt(tilePlane.terrainTiles, 0, 3);

        worldSpaceDeprecated.entities = new EntityRegistry();
        worldSpaceDeprecated.entities.addNewEntity(
                1L,
                new CollisionBox(new Vector3(0.9f,1.45f, 0.9f), false),
                new SizeComponent(1f,1.5f),
                PositionComponent.initial(),
                new InputControllerComponent(),
                new VelocityComponent(0,0),
                new HealthComponent(200f),
                SpriteComponent.fromFile(Gdx.files.internal("sprites/player/test_01.png"))
        );

        for(int i = 0; i < 18; i++) {
            worldSpaceDeprecated.entities.addNewEntity(
                    CollisionBox.createBasic(),
                    SizeComponent.createBasic(),
                    PositionComponent.initial(),
                    SpriteComponent.fromFile(Gdx.files.internal("sprites/enemies/blob_" + (int)(Math.random() * 3)+ ".png")),
                    new VelocityComponent((float)((Math.random() * 3) - 1f),(float)((Math.random() * 3) - 1f))
            );
        }

        StateComponent stateComponent =  new StateComponent();
        worldSpaceDeprecated.entities.addNewEntity(
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

        worldSpaceDeprecated.entities.addNewEntity(
                CollisionBox.createBasic().setPermeable(false),
                SizeComponent.createBasic(),
                new PositionComponent(6f, 4f),
                SpriteComponent.fromFile(Gdx.files.internal("world/objects/chest_locked.png"))
        );

        return worldSpaceDeprecated;
    }
}
