package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.entity.systems.*;
import dbast.prometheus.engine.events.Event;
import dbast.prometheus.engine.events.EventBus;
import dbast.prometheus.engine.graphics.SpriteData;
import dbast.prometheus.engine.graphics.SpriteType;
import dbast.prometheus.engine.world.Direction;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.level.Superflat;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;
import dbast.prometheus.engine.graphics.SpriteDataQueue;

import java.util.*;
import java.util.stream.Collectors;

public class WorldScene extends AbstractScene{

    protected BitmapFont font;
    protected SpriteBatch batch;

    protected WorldSpace world;
    private LockOnCamera cam;

    private CollisionDetectionSystem collisionDetectionSystem;
    private PlayerInputSystem playerInputSystem;
    private MovementSystem movementSystem;
    private StateUpdateSystem stateSystem;
    private AIInputSystem aiInputSystem;

    private List<Class<? extends Component>> entityRenderComponents;

    public static ApplicationLogger logger = Gdx.app.getApplicationLogger();

    protected Texture background_image;

    // TODO move them somewhere more fit...
    protected Sprite borderSpriteNorth;
    protected Sprite borderSpriteEast;

    public WorldScene(String key) {
        super(key);
    }

    public Music bgmMusic;
    @Override
    public WorldScene create() {
        super.create();
        font = new BitmapFont();
        font.setColor(Color.valueOf("#FFFFFF"));
      //  font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      //  font.getData().setScale(0.21f);
        font.setUseIntegerPositions(false);
        font.getData().setScale(0.0125f);
        font.getData().setLineHeight(1.0f);

        this.background = Color.valueOf("0077FF");

        // ==== [ load files ] ============================

        TileRegistry.registerPath(Gdx.files.local("data/tiles"));
        TileRegistry.output();

        bgmMusic = Gdx.audio.newMusic(Gdx.files.local("resources/sounds/song.wav"));
        bgmMusic.setLooping(true);

        background_image = new Texture(Gdx.files.internal("skybox.png"));
       // background_image.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        borderSpriteNorth = new Sprite(new Texture(Gdx.files.local("world/terrain/iso/border_n.png")));
        borderSpriteNorth.setOrigin(0.5f, 1f-(0.5f/(borderSpriteNorth.getRegionHeight() / baseSpriteSize)));
        borderSpriteNorth.setSize(borderSpriteNorth.getRegionWidth() / (float)baseSpriteSize, borderSpriteNorth.getRegionHeight() / baseSpriteSize);

        borderSpriteEast = new Sprite(new Texture(Gdx.files.local("world/terrain/iso/border_e.png")));
        borderSpriteEast.setOrigin(0.5f, 1f-(0.5f/(borderSpriteEast.getRegionHeight() / baseSpriteSize)));
        borderSpriteEast.setSize(borderSpriteEast.getRegionWidth() / (float)baseSpriteSize, borderSpriteEast.getRegionHeight() / baseSpriteSize);


        // ==== [ prepare world ] ============================
       /* world = new WaveFunctionTest(100, 100, 20, false, true,
                Arrays.asList(
                        new CastleTower("brickF"),
                        new CastleTower("dirt_0"),
                        new Hole()
                ), 6,20).setup();*/
        world = new Superflat(30, 30).setup();
       // world = new MinimalLevel2().setup();
       // world = WorldMapLoader.fromJson(Gdx.files.local("save/world_38571562605.json")).build();

        // ==== [ camera setup ] ============================
        Entity cameraFocus = world.getCameraFocus();
        //Entity cameraFocus = world.entities.get((int) (Math.random()*world.entities.size()));
        cam = new LockOnCamera(90f, 16 ,9);
        //cam = new LockOnCamera(90f, Gdx.graphics.getWidth() ,Gdx.graphics.getHeight());
        cam.lockOnEntity(cameraFocus);
        cam.setEntityOffset(cameraFocus.getComponent(SizeComponent.class).toVector3().scl(0.5f,0.25f,0.5f));//.scl(0.5f));
        cam.setCameraDistance(7f);
        cam.setViewingAngle(Math.toRadians(90));


        // ==== [ enable ECS ] ============================
        collisionDetectionSystem = new CollisionDetectionSystem();
        playerInputSystem = new PlayerInputSystem(this.cam);
        aiInputSystem = new AIInputSystem(this.world);
        movementSystem = new MovementSystem(this.world);
        stateSystem = new StateUpdateSystem(this.world);

        entityRenderComponents = Arrays.asList(RenderComponent.class, PositionComponent.class, SizeComponent.class);

        // TODO am thinking i can get rid of GUI again... Good idea or not, future me? Could shove all that shit into a separate class based on our logic.
        Gdx.input.setInputProcessor(this.gui);

        EventBus.subscribe("key_input", (event) -> {
            int keycode = (Integer)event.properties.getOrDefault("keycode", "0");

            if (keycode == Input.Keys.F3) {
                gui.setDebugAll(!gui.isDebugAll());
            }
            if (keycode == Input.Keys.F4) {
                naturalRenderOrder = !naturalRenderOrder;
            }
            if (keycode == Input.Keys.F6) {
                cam.lockOnEntity(GeneralUtils.randomElement(world.entities.values()));
            }
            if (keycode == Input.Keys.F7) {
                cam.lockOnEntity(world.getCameraFocus());
            }
            if (keycode == Input.Keys.PAGE_UP) {
                cam.setViewingAngle(Math.toRadians(Math.toDegrees(cam.getViewingAngle()) + 15f));
                logger.log("moving camera up ", "15deg" );
            }
            if (keycode == Input.Keys.PAGE_DOWN) {
                cam.setViewingAngle(Math.toRadians(Math.toDegrees(cam.getViewingAngle()) - 15f));
                logger.log("moving camera down ", "15deg" );
            }
            if (keycode == Input.Keys.MINUS) {
                cam.setCameraDistance(cam.getCameraDistance() + 0.25f);
                renderDistance += 0.25f;
            }
            if (keycode == Input.Keys.PLUS) {
                cam.setCameraDistance(cam.getCameraDistance() - 0.25f);
                renderDistance -= 0.25f;
            }
            if (keycode == Input.Keys.F8) {
                world.persist();
            }
            return null;
        });

        this.gui.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // Todo add a parameter to differentiate between down and typed events, if needed.
               // EventBus.trigger(new Event("key_input", Collections.singletonMap("keycode", keycode)));
                return super.keyDown(event, keycode);
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                EventBus.trigger(new Event("key_input", Collections.singletonMap(
                        "keycode", event.getKeyCode()
                )));
                return super.keyTyped(event, character);
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
               // PlayerInputSystem.levelOffset += amount;

                if (amount > 0) {
                    cam.setCameraDistance(cam.getCameraDistance() + 0.25f);
                    renderDistance += 0.25f;
                }
                if (amount < 0) {
                    cam.setCameraDistance(cam.getCameraDistance() - 0.25f);
                    renderDistance -= 0.25f;
                }
                logger.log("scroll event", String.format("camera distance %s, render distance %s", cam.getCameraDistance(), renderDistance));
                return super.scrolled(event, x, y, amount);
            }
        });

        // ==== [ init batch ] ============================
        batch = new SpriteBatch();
        spriteQueue = new SpriteDataQueue();

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);

        //bgmMusic.play();
        return this;
    }

    @Override
    public void render(int windowWidth, int windowHeight, float aspect) {
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        preRender(windowWidth, windowHeight, aspect);
        mainRender(windowWidth, windowHeight, aspect);
        batch.end();
        afterRender(windowWidth, windowHeight, aspect);
    }

    // TODO move sprite building out of here. Update new reference whenever things actually change. Then just render to batch
    SpriteDataQueue spriteQueue = null;
    protected static float renderDistance = (Float)PrometheusConfig.conf.getOrDefault("renderDistance",18f);
    protected static float baseSpriteSize = (Float)PrometheusConfig.conf.getOrDefault("baseSpriteSize",16f);

    public void prepareTileSprites(float deltaTime) {
        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        //  logger.log("rendering:", "tiles " + world.terrainTiles.size());
        world.terrainTiles.forEach((tilePos, tile)-> {
            if (lockOnPosition.isNearby(tilePos.x, tilePos.y, renderDistance)) {
                String tileState = "default";
                TileData tileData = world.tileDataMap.get(tilePos);
                if (tileData != null) {
                    tileState = tileData.state;
                }

                Sprite sprite = new Sprite(tile.renderComponent.getTexture(animatorLife,tileState));

                float spriteX = tilePos.x;
                float spriteY = tilePos.y;

                //sprite.setOrigin(0.5f * (sprite.getRegionWidth() / baseSpriteSize), 1f/*0.f*/);
               // sprite.setOrigin(0.5f * (sprite.getRegionWidth() / baseSpriteSize), (sprite.getRegionHeight() / baseSpriteSize)-0.5f/*0.f*/);
                float originX  = 0.5f;
                float originY  = 1f-(0.5f/(sprite.getRegionHeight() / baseSpriteSize));
                sprite.setOrigin(originX, originY/*0.f*/);

                sprite.setPosition(spriteX, spriteY);

               // tileSprite.setSize(tileSprite.getRegionWidth() / (float)tile.tileTexture.getWidth(), tileSprite.getRegionHeight() / (float)tile.tileTexture.getWidth());
                sprite.setSize(sprite.getRegionWidth() / (float)baseSpriteSize, sprite.getRegionHeight() / baseSpriteSize);

                //tileBatch.put(tilePos, tileSprite);
                SpriteData spriteData = spriteQueue.add(tilePos, sprite);

                spriteData.update(SpriteType.TILE);

                for(Direction dirEnum : Direction.values()) {
                    if (world.lookupTile(tilePos.cpy().add(dirEnum.dir)) != tile) {
                        spriteData.notBlocked.add(dirEnum);
                    }
                }

                // =========================================================================================================
                // <editor-fold desc="---- start debugging nonsense ----"
                // =========================================================================================================
                if (gui.isDebugAll() && !debuggedSprites.contains(sprite.hashCode())) {
                    TextureData spriteTexture = sprite.getTexture().getTextureData();
                    debuggedSprites.add(sprite.hashCode());
                    if (!spriteTexture.isPrepared()) {
                        spriteTexture.prepare();
                    }

                    Pixmap pixmap = spriteTexture.consumePixmap();
                    pixmap.setColor(0xCCCCCCCC);
                    pixmap .drawRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());

                    pixmap.setColor(0xFF0000FF);
                    pixmap.drawPixel(0,0);

                    pixmap.setColor(0xFF00FFFF);
                    pixmap.drawPixel((int)(sprite.getOriginX() * (pixmap.getWidth()-1)),(int)(sprite.getOriginY() * (pixmap.getHeight()-1)));


                    spriteTexture.disposePixmap();

                    sprite.setTexture(new Texture(pixmap));
                }
                // =========================================================================================================
                //</editor-fold>
                // =========================================================================================================

            }
        });
    }

    private Set<Integer> debuggedSprites = new HashSet<>();


    // TODO delegate to AnimatorSystem or whatever we'll call it, or replace with a state life when extending StateComponent
    private float animatorLife = 0f;

    public void prepareEntitySprites(float deltaTime) {
        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        List<Entity> entities = world.entities.values().stream().filter(entity ->
                entity.hasComponents(entityRenderComponents) && lockOnPosition.isNearby(entity.getComponent(PositionComponent.class),renderDistance)
        ).collect(Collectors.toList());

        for (Entity entity : entities) {
           // SpriteComponent spriteComponent = entity.getComponent(SpriteComponent.class);
            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            SizeComponent sizeComponent = entity.getComponent(SizeComponent.class);

            Vector3 entityPos = positionComponent.position;
            //world.toNextUpperLevel(entityPos);
            float spriteX = entityPos.x;
            float spriteY = entityPos.y;


            Sprite sprite = new Sprite(renderComponent.getTexture(animatorLife));
            // sprite.setOrigin(0.5f, 1f);
            sprite.setOrigin(0.5f * (sprite.getRegionWidth() / baseSpriteSize), 0.25f + (sprite.getRegionHeight() / baseSpriteSize));

           // spriteY += entityPos.z * (sprite.getOriginY() * sprite.getHeight());

            sprite.setPosition(spriteX, spriteY);
           // sprite.setSize(sizeComponent.getWidth(), sizeComponent.getHeight());
            sprite.setSize(sprite.getRegionWidth() / (float)baseSpriteSize, sprite.getRegionHeight() / baseSpriteSize);

            //entityBatch.put(entityPos, sprite);
           // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[entity %s] sprite %s | X %s | Y %s | Z  %s | KEY: %s", entity.getId(), sprite.hashCode(), spriteX, spriteY, entityPos.z, skey ));
            //spriteQueue.put(skey, sprite);
            spriteQueue.add(entityPos, sprite).update(SpriteType.ENTITY);

            // =========================================================================================================
            // <editor-fold desc="---- start debugging nonsense ----"
            // =========================================================================================================
            if (gui.isDebugAll() && !debuggedSprites.contains(sprite.hashCode())) {
                Vector3 camPosition = cam.position;
                TextureData spriteTexture = sprite.getTexture().getTextureData();
                debuggedSprites.add(sprite.hashCode());

                if (!spriteTexture.isPrepared()) {
                    spriteTexture.prepare();
                }

                Pixmap pixmap = spriteTexture.consumePixmap();
                pixmap.setColor(0xCCCCCCCC);
                pixmap.drawRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());

                if (entity.hasComponent(CollisionBox.class)) {
                    CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
                    pixmap.setColor(0xEEEE00A0);
                    int hbX = (int) collisionBox.getWidth() * pixmap.getWidth();
                    int hbY = (int) collisionBox.getHeight() * pixmap.getHeight();
                    for (int y = 0; y < hbY; y++) {
                        for (int x = 0; x < hbX; x++) {
                            if (x == 0 || x == hbX-1 || y == 0 || y == hbY - 1
                                    || ( (x% 2 == 0 && y % 2 != 0) || (x % 2 != 0 && y % 2 == 0) )) {
                                pixmap.drawPixel(x,y);
                            }
                        }
                    }
                }
                pixmap.setColor(0xFF0000FF);
                pixmap.drawPixel(0,0);

                pixmap.setColor(0xFF00FFFF);
                pixmap.drawPixel((int)(sprite.getOriginX() * baseSpriteSize-1),(int)(sprite.getOriginY() * baseSpriteSize-1));

                spriteTexture.disposePixmap();

                sprite.setTexture(new Texture(pixmap));
            }
            // =========================================================================================================
            //</editor-fold>
            // =========================================================================================================
        }
    }


    protected static float gridSnapIncrement = (Float)PrometheusConfig.conf.getOrDefault("gridSnapIncrement", 0.0625f);
    protected static boolean useGridSnapping = (Boolean)PrometheusConfig.conf.getOrDefault("gridSnapping", false);
    protected static boolean useIsometric = (Boolean)PrometheusConfig.conf.getOrDefault("isometric", false);
    protected static boolean useWorldTimeShading = (Boolean)PrometheusConfig.conf.getOrDefault("useWorldTimeShading", false);

    public  static boolean naturalRenderOrder = true;

    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        font.setColor(Color.WHITE);
        spriteQueue.clear();

        float deltaTime = Gdx.graphics.getDeltaTime();
        this.animatorLife += deltaTime;

        prepareTileSprites(deltaTime);
        prepareEntitySprites(deltaTime);

        if (naturalRenderOrder) {
            spriteQueue.sort(Comparator.naturalOrder());
        } else {
            spriteQueue.sort(Comparator.reverseOrder());
        }

        PositionComponent lockOnPosition =  cam.getLockOnEntity().getComponent(PositionComponent.class);
        Vector3 lockOnVector = lockOnPosition.position;
        Vector3 higlightThis = playerInputSystem.inWorldPos;
      //  logger.log("Highlight:", String.format("Highlighting %s", higlightThis.toString()));

        float fogOfWarRange = world.getSightRange();
        Color lightingColor = world.worldTime.getLightingColor(world.age);

        boolean renderShadow = false;
        boolean renderBorderSprites = true;
        boolean renderOrderIndex = false;
        boolean viewCulling = false;

        spriteQueue.forEach((spriteData)-> {
            // begin of render pipeline...
            // TODO can this be delegated to actual shader logic, hopefully sparing sweet CPU calculation time??
            float intendedX = spriteData.levelPosition.x;
            float intendedY = spriteData.levelPosition.y;
            // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[sprite %s 1/2] intendedX %s | intendedY %s", spriteData.hashCode(), intendedX, intendedY));
            if (!useIsometric) {
             //   intendedY += spriteData.spritePos3D.z;
            }

            if (useGridSnapping) {
                double xPos = Math.round(intendedX);
                double yPos = Math.round(intendedY);

                intendedX = (float)(xPos + (Math.round((intendedX - xPos) / gridSnapIncrement)) * gridSnapIncrement);
                intendedY = (float)(yPos + (Math.round((intendedY - yPos) / gridSnapIncrement)) * gridSnapIncrement);
            }
            if (useIsometric) {
                float unmodifiedX = intendedX;
                float unmodifiedY = intendedY;
                Vector3 unmodified = new Vector3(unmodifiedX, unmodifiedY, spriteData.levelPosition.z);
                //unmodified = GeneralUtils.projectIso(unmodified, spriteData.sprite.getWidth() * 0.5f, spriteData.sprite.getHeight() * 0.5f);
                unmodified.prj(LockOnCamera.isoTransform);

                // sprite height should be used here according to render logic... For some reason spritebased sizes fuck this up however, therefor use width
               // intendedX = (float) (unmodifiedX * 0.5 * spriteData.sprite.getWidth() - unmodifiedY * 0.5 * spriteData.sprite.getWidth());
                //intendedY = (float) (unmodifiedX * 0.25 * spriteData.sprite.getWidth() + unmodifiedY * 0.25 * spriteData.sprite.getWidth());
                intendedX = unmodified.x ;//* spriteData.sprite.getWidth();
                intendedY = unmodified.y;// * spriteData.sprite.getWidth();
                // offset, because why the fuck?
               // intendedY += spriteData.levelPosition.z * 0.5f /*- 0.6f*/;// * spriteData.sprite.getWidth();
                intendedY += spriteData.levelPosition.z * 0.5f /*- 0.6f*/;// * spriteData.sprite.getWidth();

               // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[sprite %s 2/2] intendedX %s | intendedY %s", spriteData.hashCode(), intendedX, intendedY));
                // offset
                intendedX -= (spriteData.sprite.getOriginX() * spriteData.sprite.getWidth())- 0.5f;
               // intendedX -= spriteData.sprite.getOriginX();

                if ((int)higlightThis.x == (int) spriteData.levelPosition.x
                        && (int)higlightThis.y == (int) spriteData.levelPosition.y
                       /* && (int)higlightThis.z == (int) spriteData.position.z*/) {
                    intendedY += 0.125f; //* spriteData.sprite.getWidth();
                }
            } else {
                if ((int)higlightThis.x == (int) spriteData.levelPosition.x
                        && (int)higlightThis.y == (int) spriteData.levelPosition.y) {
                    spriteData.sprite.rotate(45);
                }
            }

            // draw "shadow"
            // kinda works, but looks absolutely stupid
            if (renderShadow) {
                spriteData.sprite.setPosition(
                        intendedX,
                        intendedY
                );
                spriteData.sprite.setScale(spriteData.sprite.getScaleX() + 0.0625f, spriteData.sprite.getScaleY() + 0.0625f);

                Color beforeShadow = spriteData.sprite.getColor();

                spriteData.sprite.setColor(new Color(0f,0f,0f,0.33f));
                spriteData.sprite.draw(batch);

                // reset
                spriteData.sprite.setColor(beforeShadow);
                spriteData.sprite.setScale(spriteData.sprite.getScaleX() - 0.0625f, spriteData.sprite.getScaleY() - 0.0625f);
            }

            // reposition sprite
            spriteData.sprite.setPosition(
                intendedX,
                intendedY
            );

            float distanceFromFocus = lockOnVector.dst(spriteData.levelPosition);
            float distanceToFocus = 1- (distanceFromFocus / renderDistance);//tilePos.x, tilePos.y, tilePos.z*0.5f));
            //Gdx.app.getApplicationLogger().log("Render Pipeline", String.format("distance of tile %s to focus is %s", tile.tag, distanceToFocus));


            float distanceFog =1 - (distanceFromFocus / fogOfWarRange);

            // this should definitely be delegated to a some shader...
            if (useWorldTimeShading) {
                spriteData.sprite.setColor(this.background.cpy().lerp(lightingColor, 0.5f).mul(distanceFog, distanceFog, distanceFog, 1));
            } else {
                spriteData.sprite.setColor(new Color(distanceFog, distanceFog, distanceFog, 1f));
            }

            if (viewCulling) {
                // TODO intersection with view camera...
                int lockonX = (int) (Math.round(lockOnVector.x) - 4f);
                int lockonY = (int) (Math.round(lockOnVector.y) - 4f);
                int lockonZ = Math.round(lockOnVector.z);
                if (
                        spriteData.levelPosition.z > lockonZ  &&
                                /*spriteData.position.z - lockonZ > 3 && */(
                                0.5f * ( Math.max(spriteData.levelPosition.x, lockonX) - Math.min(spriteData.levelPosition.x, lockonX) ) < spriteData.levelPosition.z - lockonZ - 1 &&
                                        0.5f * ( Math.max(spriteData.levelPosition.y, lockonY) - Math.min(spriteData.levelPosition.y, lockonY) ) < spriteData.levelPosition.z - lockonZ - 1
                        )
                ) {
                    //  spriteData.sprite.draw(batch, 0.125f);
                    return;
                }
            }

            float spriteAlpha =  distanceToFocus > 0.3f ? 1f : distanceToFocus > 0.20 ? 0.5f : distanceToFocus > 0.10 ? 0.25f : 0f;

            // draw border sprites
            if (renderBorderSprites) {
                float intendedXBorder = (intendedX + (spriteData.sprite.getOriginX() * spriteData.sprite.getWidth())- 0.5f);
                if (spriteData.notBlocked.contains(Direction.NORTH)) {
                    borderSpriteNorth.setPosition(
                            intendedXBorder - ((borderSpriteNorth.getOriginX() * borderSpriteNorth.getWidth())- 0.5f),
                            intendedY
                    );
                    borderSpriteNorth.draw(batch, spriteAlpha);
                }
                if (spriteData.notBlocked.contains(Direction.EAST)) {
                    borderSpriteEast.setPosition(
                            intendedXBorder - ((borderSpriteEast.getOriginX() * borderSpriteEast.getWidth())- 0.5f),
                            intendedY
                    );
                    borderSpriteEast.draw(batch, spriteAlpha);
                }
            }

            // draw regular sprite
            spriteData.sprite.draw(batch, spriteAlpha);

            if (renderOrderIndex) {
                /*if(spriteData.position.equals(lockOnVector)) {
                    logger.log("renderer", String.format("Player sprite has %s as order index", spriteData.orderIndex.toString()));
                }*/
                font.setColor(Color.YELLOW);
                font.draw(batch,  spriteData.orderIndex.toString(),
                        intendedX + spriteData.sprite.getWidth() * 0.25f,
                        intendedY + 2 * spriteData.sprite.getOriginY());
            }
        });
    }

    public void update(float deltaTime){
        super.update(deltaTime);
        EventBus.update(deltaTime);

        world.update(deltaTime);
        this.background = world.getSkyboxColor();

        // ECS updates - TODO might have to delegate this to worlds update?
        collisionDetectionSystem.entityHitboxCache.clear();
        // TODO can this be simplified? Instead of passing lists of entities to all systems, iterate entities and pass to systems if qualified components are here
        collisionDetectionSystem.execute(deltaTime, world.entities.compatibleWith(collisionDetectionSystem) );
        playerInputSystem.execute(deltaTime, world.entities.compatibleWith(playerInputSystem));
        aiInputSystem.execute(deltaTime, world.entities.compatibleWith(aiInputSystem));
        movementSystem.execute(deltaTime, world.entities.compatibleWith(movementSystem));
        stateSystem.execute(deltaTime, world.entities.compatibleWith(stateSystem));

        cam.update();
    }

    @Override
    public void afterRender(int windowWidth, int windowHeight, float aspect) {
        this.gui.draw();
    }

    public void drawWorld(SpriteBatch batch, int windowWidth, int windowHeight, float aspect) {
        batch.draw(background_image, 0,0, 0,0, windowWidth, windowHeight);
    }

}
