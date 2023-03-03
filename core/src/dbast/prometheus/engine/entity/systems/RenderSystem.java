package dbast.prometheus.engine.entity.systems;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.graphics.SpriteData;
import dbast.prometheus.engine.graphics.SpriteDataQueue;
import dbast.prometheus.engine.graphics.SpriteType;
import dbast.prometheus.engine.scene.WorldScene;
import dbast.prometheus.engine.world.Direction;
import dbast.prometheus.engine.world.WorldChunk;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.GenerationUtils;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.utils.GeneralUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    public RenderSystem(WorldSpace world, LockOnCamera cam) {
        this.world = world;
        this.camera = cam;
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
    public void execute(float updateDelta, List<Entity> qualifiedEntities) {
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

        Vector3 centerChunk = world.getChunkFor(lockOnPosition.position);
        // chunks are currently only horizontally, hence the 8 surroundings should suffice
        List<Vector3> visibleChunkCords = new ArrayList<>(Arrays.asList(GenerationUtils.nearby8Of(centerChunk, world.chunkSize)));
        visibleChunkCords.add(centerChunk);

        List<WorldChunk> visibleChunks = world.chunks.getMultiple(visibleChunkCords);

        for(WorldChunk chunk : visibleChunks) {

            for (Map.Entry<Vector3, TileData> tileDataEntry : chunk.tileDataMap.entrySet()) {
                Vector3 tilePos = tileDataEntry.getKey();
                TileData tileData = tileDataEntry.getValue();

                String cacheKey = "Tile" + tilePos.toString();
               // if (lockOnPosition.isNearby(tilePos, WorldScene.renderDistance)) {
                    RenderComponent renderComponent = tileData.tile.renderComponent;
                    PositionComponent positionComponent = tileData.positionComponent;
                    StateComponent stateComponent = tileData.stateComponent;

                    // TODO check if it's possible and feasible to get set the order index first based by the chunk then multiplied by the chunk internal order index, shoul get rid of current North/East issue.
                    SpriteData spriteData = updateSpriteData(updateDelta, cacheKey, SpriteType.TILE, positionComponent, renderComponent, stateComponent);

                    if (tileData.isVisibleFrom(lockOnPosition.position)) {
                        for(Direction dirEnum : Direction.values()) {
                            TileData neighbor = tileData.getNeighbor(dirEnum);
                            if (neighbor == null || neighbor.tile != tileData.tile) {
                                spriteData.notBlocked.add(dirEnum);
                            } else {
                                spriteData.notBlocked.remove(dirEnum);
                            }
                        }

                        spriteQueue.add(spriteData);
                    }
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

        Sprite sprite = null;
        TextureRegion currentTextureRegion = (stateComponent != null) ? renderComponent.getTexture(animatorLife, stateComponent.getState()) : renderComponent.getTexture(animatorLife);

        if (spriteData == null) {
            spriteData = new SpriteData();

            float spriteX = entityPos.x;
            float spriteY = entityPos.y;

            sprite = new Sprite(currentTextureRegion);

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

        spriteData.setPosition(entityPos);
        spriteData.setType(spriteType);
        spriteData.sprite.setRegion(currentTextureRegion);

        spriteData.update(updateDelta);
        return spriteData;
    }


    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(RenderComponent.class, PositionComponent.class, SizeComponent.class);
    }
}
