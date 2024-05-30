package dbast.prometheus.engine.entity.systems;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.graphics.*;
import dbast.prometheus.engine.scene.WorldScene;
import dbast.prometheus.engine.world.Direction;
import dbast.prometheus.engine.world.WorldChunk;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.GenerationUtils;
import dbast.prometheus.engine.world.tile.TileData;

import java.util.*;

/**
 * Hear that developer, engine wants something a bit meatier!
 *
 */
public class RenderSystem extends ComponentSystem {

    public WorldSpace world;

    public LockOnCamera camera;

    protected Map<String, SpriteData> spriteDataCache;
    public SpriteDataQueue spriteQueue;

    public float animatorLife;

    public float baseSpriteSize;

    public Texture defaultCubeNormal;
    public RenderSystem(WorldSpace world, LockOnCamera cam) {
        this.world = world;
        this.camera = cam;
        this.defaultCubeNormal = new Texture(Gdx.files.internal("world/terrain/iso/normal_c.png"));
        resetRenderSystem();
    }

    public void resetRenderSystem() {
        if (spriteQueue != null) {
            spriteQueue.clear();
        }
        spriteQueue = new SpriteDataQueue();
        spriteDataCache = new HashMap<>();

        this.animatorLife = 0f;
    }

    @Override
    public void execute(float updateDelta) {
        animatorLife += updateDelta;
        Entity cameraLockOn = camera.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        spriteQueue.clear();

        this.baseSpriteSize = WorldScene.baseSpriteSize;

        // prepare EntitySpirtes and add em to SpriteQueue
        long beforeEntity = System.nanoTime();
        for(Entity entity : qualifiedEntities) {
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            SizeComponent sizeComponent = entity.getComponent(SizeComponent.class);
            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            StateComponent stateComponent = entity.getComponent(StateComponent.class);

            SpriteData spriteData = updateSpriteData(updateDelta,"Ent"+entity.getId(), SpriteType.ENTITY, positionComponent, renderComponent, stateComponent);

            if (lockOnPosition.isNearby(spriteData.levelPosition, WorldScene.renderDistance)) {
                spriteQueue.add(spriteData);
            }
        }
        long afterEntity = System.nanoTime();


        // TODO migrate world rendering to building of a "mesh" (an object holding the chunks data since the last update instead of doing this *every* frame)
        Vector3 centerChunk = world.getChunkFor(lockOnPosition.position);
        // chunks are currently only horizontally, hence the 8 surroundings should suffice
       // List<Vector3> visibleChunkCords = new ArrayList<>(Arrays.asList(GenerationUtils.nearby8Of(centerChunk, world.chunkSize)));
        List<Vector3> visibleChunkCords = new ArrayList<>(Arrays.asList(GenerationUtils.nearbyDiamond(centerChunk, world.chunkSize * 2, world.chunkSize)));
        visibleChunkCords.add(centerChunk);

        List<WorldChunk> visibleChunks = world.chunks.getMultiple(visibleChunkCords);

        for(WorldChunk chunk : visibleChunks) {

            for (TileData tileData : chunk.visibleTileData) {
                RenderComponent renderComponent = tileData.tile.renderComponent;
                PositionComponent positionComponent = tileData.positionComponent;
                StateComponent stateComponent = tileData.stateComponent;

                String cacheKey = "Tile" + tileData.positionComponent.position.toString();

                // TODO check if it's possible and feasible to get set the order index first based by the chunk then multiplied by the chunk internal order index, shoul get rid of current North/East issue.
                SpriteData spriteData = updateSpriteData(updateDelta, cacheKey, SpriteType.TILE, positionComponent, renderComponent, stateComponent);

                    for(Direction dirEnum : Direction.values()) {
                        TileData neighbor = tileData.getNeighbor(dirEnum);
                        if (neighbor == null || neighbor.tile != tileData.tile) {
                            spriteData.notBlocked.add(dirEnum);
                        } else {
                            spriteData.notBlocked.remove(dirEnum);
                        }
                    }

                    spriteQueue.add(spriteData);
              //  } else {
                    // TODO implement cleaning of sprite cache when sprites in it haven't been used for some time
                    // spriteDataCache.remove(cacheKey);
              //  }
            }
        }

        long afterTile = System.nanoTime();

        spriteQueue.sort(Comparator.naturalOrder());

        long afterSort = System.nanoTime();

     //   Gdx.app.log("RenderSystem", String.format("Entity: %s, Tiles: %s, Sort: %s | Whole Render Update: %s", afterEntity - beforeEntity, afterTile - afterEntity, afterSort - afterTile, afterSort - beforeEntity));
    }


    protected SpriteData updateSpriteData(float updateDelta, String spriteDataKey, SpriteType spriteType, PositionComponent positionComponent, RenderComponent renderComponent, StateComponent stateComponent) {
        SpriteData spriteData = spriteDataCache.get(spriteDataKey);
        Vector3 entityPos = positionComponent.position;

        PrometheusSprite sprite = null;
        SpriteRenderData currentRenderData = getTextureForState(renderComponent, stateComponent);

        if (spriteData == null) {
            spriteData = new SpriteData();

            float spriteX = entityPos.x;
            float spriteY = entityPos.y;

            sprite = new PrometheusSprite(currentRenderData);

            float originX  = 0;
            float originY  = 0;

            if (spriteType.equals(SpriteType.TILE)) {
                originX  = 0.5f;
                originY  = 1f - (0.5f/(sprite.getRegionHeight() / baseSpriteSize));
            } else if (spriteType.equals(SpriteType.ENTITY)) {
                originX  = 0.5f * (sprite.getRegionWidth() / baseSpriteSize);
                originY  = 0.25f + (sprite.getRegionHeight() / baseSpriteSize);
            }

            sprite.setOrigin(originX, originY);
            sprite.setPosition(spriteX, spriteY);
            sprite.setSize(sprite.getRegionWidth() / baseSpriteSize, sprite.getRegionHeight() / baseSpriteSize);

            spriteData.setSprite(sprite);
            spriteDataCache.put(spriteDataKey, spriteData);
        }

        // TODO improve update of render data...
        spriteData.setPosition(entityPos);
        spriteData.setType(spriteType);
        spriteData.setOffset(currentRenderData.renderIndexOffset);
        spriteData.sprite.setNormal(currentRenderData.normal);
        spriteData.sprite.setRegion(currentRenderData.diffuse);

        spriteData.update(updateDelta);
        return spriteData;
    }

    protected SpriteRenderData getTextureForState(RenderComponent renderComponent, StateComponent stateComponent) {
        float stateAge = animatorLife;
        Map<String, Animation<SpriteRenderData>> animations = renderComponent.getAnimations();

        Animation<SpriteRenderData> currentAnimation = null;
        if (stateComponent != null) {
            if (stateComponent.getCurrentAge() != 0f) {
                stateAge = stateComponent.getCurrentAge();
            }
            currentAnimation = animations.getOrDefault(stateComponent.getState(), null);
        }
        if (currentAnimation == null ) {
            currentAnimation = animations.getOrDefault("default", null);
        }

        if (currentAnimation != null) {
            return currentAnimation.getKeyFrame(stateAge);
        }

        return renderComponent.getDefaultRenderData();
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(RenderComponent.class, PositionComponent.class, SizeComponent.class);
    }
}
