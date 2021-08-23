package dbast.prometheus.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.utils.GeneralUtils;
import net.dermetfan.gdx.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class TilePlane {

    public int[][] terrainTiles;
    public int height;
    public int width;

    public List<Entity> entities;

    public TilePlane(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrainTiles = new int[height][width];
    }


    public static TilePlane testLevel() {
        int[][] level = new int[][] {
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {1,0,0,1,1,1,1,1,0,1,1,1,1,1,0,0,0},
                {2,1,1,2,2,2,2,2,1,1,1,1,2,2,1,0,0},
                {2,2,2,2,2,2,2,2,1,2,2,2,2,2,2,1,1},
                {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2},
                {2,2,0,0,2,2,2,2,2,2,2,2,2,2,2,2,2},
                {2,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2},
                {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2},
        };

        TilePlane tilePlane = new TilePlane(level[0].length, level.length);

        tilePlane.terrainTiles = level;
        //GeneralUtils.populate2DInt(tilePlane.terrainTiles, 0, 3);

        tilePlane.populate();
        return tilePlane;
    }

    public void populate() {
        this.entities = new ArrayList<>();
        Entity entity = new Entity(1L);
        entity.addComponent(new CollisionBox(1f,1.5f,false));
        entity.addComponent(PositionComponent.initial());
        entity.addComponent(new InputControllerComponent());
        entity.addComponent(new VelocityComponent(0,0));
        entity.addComponent(new HealthComponent(200f));
        entity.addComponent(SpriteComponent.fromFile(Gdx.files.internal("world/environment/tree.png")));
        this.entities.add(entity);

        for(int i = 0; i < 18; i++) {
            entity = new Entity(2L + i);
            entity.addComponent(CollisionBox.createBasic());
            entity.addComponent(PositionComponent.initial());
            entity.addComponent(SpriteComponent.fromFile(Gdx.files.internal("sprites/enemies/blob_" + (int)(Math.random() * 3)+ ".png")));
            entity.addComponent(new VelocityComponent((float)((Math.random() * 2) - 1f),(float)((Math.random() * 2) - 1f)));
            this.entities.add(entity);
        }

        entity = new Entity(32L);
        entity.addComponent(CollisionBox.createBasic().setPermeable(false));
        entity.addComponent(new PositionComponent(4f, 4f));
        entity.addComponent(new StateComponent());
       // entity.addComponent(SpriteComponent.fromFile(Gdx.files.internal("world/objects/wd_chest_locked.png")));
        entity.addComponent(StateBasedSpriteComponent
                .fromFile(Gdx.files.internal("world/objects/chest_locked.png"))
                .addState("colliding", Gdx.files.internal("world/objects/chest_active.png"))
                .addState("open", Gdx.files.internal("world/objects/chest_open_1.png"))
                .bindTo((StateComponent)entity.getComponent(StateComponent.class)));
        this.entities.add(entity);
        /*
        entity = new Entity(21L);
        entity.addComponent(CollisionBox.createBasic().setPermeable(false));
        entity.addComponent(new PositionComponent(6f, 4f));
        entity.addComponent(SpriteComponent.fromFile(Gdx.files.internal("world/objects/chest_locked.png")));
        this.entities.add(entity);*/
    }

}
