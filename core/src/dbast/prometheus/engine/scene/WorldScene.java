package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.entity.systems.*;
import dbast.prometheus.engine.events.Event;
import dbast.prometheus.engine.events.EventBus;
import dbast.prometheus.engine.serializing.WorldMapLoader;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.features.CastleTower;
import dbast.prometheus.engine.world.generation.features.Hole;
import dbast.prometheus.engine.world.level.Superflat;
import dbast.prometheus.engine.world.level.WaveFunctionTest;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;
import dbast.prometheus.utils.SpriteBuffer;

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

    public WorldScene(String key) {
        super(key);
    }

    public Music bgmMusic;
    @Override
    public WorldScene create() {
        super.create();
        font = new BitmapFont();
        font.setColor(Color.valueOf("#FFFFFF"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(0.1f);
        font.getData().setLineHeight(1.0f);

        this.background = Color.valueOf("0077FF");

        // ==== [ load files ] ============================

        TileRegistry.registerPath(Gdx.files.local("data/tiles"));
        TileRegistry.output();

        bgmMusic = Gdx.audio.newMusic(Gdx.files.local("resources/sounds/song.wav"));
        bgmMusic.setLooping(true);

        // ==== [ prepare world ] ============================
        /*world = new WaveFunctionTest(100, 100, 20, false, true,
                Arrays.asList(
                        new CastleTower("brickF"),
                        new CastleTower("dirt_0"),
                        new Hole()
                ), 6,20).setup();*/
        world = new Superflat(256, 256).setup();
        //world = WorldMapLoader.fromJson(Gdx.files.local("save/world_38571562605.json")).build();

        // ==== [ camera setup ] ============================
        Entity cameraFocus = world.getCameraFocus();
        //Entity cameraFocus = world.entities.get((int) (Math.random()*world.entities.size()));
       // cam = new LockOnCamera(90f, 16 ,9);
        cam = new LockOnCamera(90f, 16 ,9);
        cam.lockOnEntity(cameraFocus);
        cam.setEntityOffset(cameraFocus.getComponent(SizeComponent.class).toVector3().scl(0.5f,0.25f,0.5f));//.scl(0.5f));
        cam.setCameraDistance(7f);
        cam.setViewingAngle(Math.toRadians(90));


        // ==== [ enable ECS ] ============================
        collisionDetectionSystem = new CollisionDetectionSystem();
        playerInputSystem = new PlayerInputSystem();
        aiInputSystem = new AIInputSystem(this.world);
        movementSystem = new MovementSystem(this.world);
        stateSystem = new StateUpdateSystem();

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
            public boolean mouseMoved(InputEvent event, float x, float y) {
                boolean defaultHandle = super.mouseMoved(event, x, y);
                Entity cameraLockOn = cam.getLockOnEntity();
                PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

                Vector3 mousePos = new Vector3((float)Gdx.input.getX() / (float)Gdx.graphics.getWidth(), 1f -((float)Gdx.input.getY() / (float)Gdx.graphics.getHeight()), 0);
               // logger.log("Input event: ","Offset Mouse position to center is " + mousePos.toString());
                // focus on middle
                mousePos.add(-0.5f, -0.5f,0f);

                logger.log("Input event: ","Offset Mouse position to center is " + mousePos.toString());

                byte version = 3;

                if (version == 3) {
                    // adjust for tiles on screen "quadrant"
                    float ratioWidthToHeight = (cam.viewportWidth/cam.viewportHeight);
                    float ratioHeightToWidth = (cam.viewportHeight/cam.viewportWidth);

                    float halfX = (cam.viewportWidth / 2) * ratioWidthToHeight;
                    float halfY = cam.viewportHeight / ratioHeightToWidth;
                    float distortionOffset = 1f;

                    mousePos.scl(halfX, halfY, 1f);
                  //  mousePos.scl(cam.getCameraDistance() / 2);

                    mousePos.set(
                            (mousePos.x / 0.5f + mousePos.y /  0.5f) / 2 + distortionOffset,
                            ((mousePos.y / 0.5f - (mousePos.x /  0.5f)) / 2) + distortionOffset,
                            mousePos.z
                    );

                    mousePos.add(lockOnPosition.position);
                } else if (version == 2) {
                    if (useIsometric) {
                        Vector3 boundaries = new Vector3(cam.viewportWidth, cam.viewportHeight, 0f);
                        logger.log("Input event: ","boundaries is " + boundaries.toString());
                        boundaries.prj(LockOnCamera.isoTransform);
                        logger.log("Input event: ","boundaries is " + boundaries.toString());

                        boolean useIsoTransformForScreen = true;
                        if (useIsoTransformForScreen) {
                           /* mousePos.prj(LockOnCamera.isoTransform);

                            // for some odd reason these two appear to be switched...
                            float mouseX = mousePos.x;
                            mousePos.x = mousePos.y;
                            mousePos.y = -mouseX;*/
                        } else {
                            // use original transform matrix that seems to logically be incorrect for it's actual purpose????
                            mousePos.prj(LockOnCamera.screenToGridTransform);
                        }
                        mousePos.scl(cam.getCameraDistance());
                        //mousePos.scl(boundaries);
                        mousePos.scl( boundaries);


                        mousePos.add(lockOnPosition.position);
                        // mousePos.scl(renderDistance);
                        logger.log("Input event: ","transformed Mouse position to center is " + mousePos.toString());
                    } else {
                        mousePos.scl(cam.viewportWidth, cam.viewportHeight, 1f);
                        mousePos.scl(1.75f);
                        mousePos.add(lockOnPosition.position);
                    }
                    logger.log("Input event: ", String.format("Camera angled at %s", ((float)Math.sin(cam.getViewingAngle())) * cam.getCameraDistance()));

                }
                higlightThis.set(mousePos);

/*
TODO somehow translate mouse selection into world object selection?
                Entity cameraLockOn = cam.getLockOnEntity();
                PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

                //
                Vector3 mousePos = new Vector3((float)Gdx.input.getX() / (float)Gdx.graphics.getWidth(), 1f-(float)Gdx.input.getY() / (float)Gdx.graphics.getHeight(), 0);
                logger.log("Input event: ","Original Mouse position is " + mousePos.toString());
                // focus on middle
                mousePos.add(-0.5f, -0.5f,0f);
                logger.log("Input event: ","Offset Mouse position to center is " + mousePos.toString());
                // do iso transform, if needed...
                if (useIsometric) {
                    float unmodifiedX = mousePos.x;
                    float unmodifiedY = mousePos.y;


                    mousePos.prj(LockOnCamera.isoTransform);
                    float intendedX = mousePos.x * baseSpriteSize;
                    float intendedY = mousePos.y * baseSpriteSize;
                    mousePos.scl(baseSpriteSize);
                   //  intendedX = (float) (unmodifiedX * 0.5  - unmodifiedY * 0.5);
                   //  intendedY = (float) (unmodifiedX * 0.25  + unmodifiedY * 0.25 );

                    //mousePos.set(intendedX, intendedY, mousePos.z);
                    logger.log("Input event: ","Mouse position in ISO is " + mousePos.toString());
                    //mousePos.add(lockOnPosition)
                }
                // multiply by "distance", as in the "size" of the stuff being shown
                //mousePos.scl(cam.viewportWidth, cam.viewportHeight, 1f);
                //mousePos.scl(renderDistance);

                // add lock on position to mousePos
                mousePos.add(lockOnPosition.position);
                higlightThis.set(mousePos);

                logger.log("Input event: ","Mouse position is " + mousePos.toString());
*/
                return defaultHandle;
            }
        });

        // ==== [ init batch ] ============================
        batch = new SpriteBatch();
        spriteQueue = new SpriteBuffer();

        //bgmMusic.play();
        return this;
    }

    public Vector3 higlightThis = new Vector3(0f,0f,0f);

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
    SpriteBuffer spriteQueue = null;
    // static float renderDistance = 18f;
    protected static float renderDistance = (Float)PrometheusConfig.conf.getOrDefault("renderDistance",18f);
    protected static float baseSpriteSize = (Float)PrometheusConfig.conf.getOrDefault("baseSpriteSize",16f);

    public void prepareTileSprites(float deltaTime) {
        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        //  logger.log("rendering:", "tiles " + world.terrainTiles.size());
        world.terrainTiles.forEach((tilePos, tile)-> {
            if (lockOnPosition.isNearby(tilePos.x, tilePos.y, renderDistance)) {

                Sprite tileSprite = new Sprite(tile.renderComponent.getTexture(animatorLife,"default"));

                float spriteX = tilePos.x;
                float spriteY = tilePos.y;

                tileSprite.setOrigin(0.5f * (tileSprite.getRegionWidth() / baseSpriteSize), 0.f);
                tileSprite.setPosition(spriteX, spriteY);

               // tileSprite.setSize(tileSprite.getRegionWidth() / (float)tile.tileTexture.getWidth(), tileSprite.getRegionHeight() / (float)tile.tileTexture.getWidth());
                tileSprite.setSize(tileSprite.getRegionWidth() / (float)baseSpriteSize, tileSprite.getRegionHeight() / baseSpriteSize);

                //tileBatch.put(tilePos, tileSprite);
                spriteQueue.add(tilePos, tileSprite);

                // =========================================================================================================
                // <editor-fold desc="---- start debugging nonsense ----"
                // =========================================================================================================
                if (gui.isDebugAll() && !debuggedSprites.contains(tileSprite.hashCode())) {
                    TextureData spriteTexture = tileSprite.getTexture().getTextureData();
                    debuggedSprites.add(tileSprite.hashCode());
                    if (!spriteTexture.isPrepared()) {
                        spriteTexture.prepare();
                    }

                    Pixmap pixmap = spriteTexture.consumePixmap();
                    pixmap.setColor(0xCCCCCCCC);
                    pixmap.drawRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());

                    pixmap.setColor(0xFF0000FF);
                    pixmap.drawPixel(0,0);

                    pixmap.setColor(0xFF00FFFF);
                    pixmap.drawPixel((int)(tileSprite.getOriginX() * (pixmap.getWidth()-1)),(int)(tileSprite.getOriginY() * (pixmap.getHeight()-1)));


                    spriteTexture.disposePixmap();

                    tileSprite.setTexture(new Texture(pixmap));
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

            // TODO might needs cpy()
            Vector3 entityPos = positionComponent.position;
            //world.toNextUpperLevel(entityPos);
            float spriteX = entityPos.x;
            float spriteY = entityPos.y;


            Sprite sprite = new Sprite(renderComponent.getTexture(animatorLife));
            // sprite.setOrigin(0.5f, 1f);
            sprite.setOrigin(0.5f * (sprite.getRegionWidth() / baseSpriteSize), 1f * (sprite.getRegionHeight() / baseSpriteSize));

           // spriteY += entityPos.z * (sprite.getOriginY() * sprite.getHeight());

            sprite.setPosition(spriteX, spriteY);
           // sprite.setSize(sizeComponent.getWidth(), sizeComponent.getHeight());
            sprite.setSize(sprite.getRegionWidth() / (float)baseSpriteSize, sprite.getRegionHeight() / baseSpriteSize);

            //entityBatch.put(entityPos, sprite);
           // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[entity %s] sprite %s | X %s | Y %s | Z  %s | KEY: %s", entity.getId(), sprite.hashCode(), spriteX, spriteY, entityPos.z, skey ));
            //spriteQueue.put(skey, sprite);
            spriteQueue.add(entityPos, sprite);

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
        Vector3 viewVector = lockOnVector.cpy().sub(cam.getPosition());
        Vector3 lockOn2d = lockOnPosition.position.cpy().scl(1,1,0f);

        float fogOfWarRange = world.getSightRange();
        Color lightingColor = world.currentTime.getLightingColor(world.age);

        spriteQueue.forEach((spriteData)-> {
            // begin of render pipeline...
            // TODO can this be delegated to actual shader logic, hopefully sparing sweet CPU calculation time??
            float intendedX = spriteData.spritePos3D.x;
            float intendedY = spriteData.spritePos3D.y;
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
                Vector3 unmodified = new Vector3(unmodifiedX, unmodifiedY, 0);
                unmodified.prj(LockOnCamera.isoTransform);

                // sprite height should be used here according to render logic... For some reason spritebased sizes fuck this up however, therefor use width
               // intendedX = (float) (unmodifiedX * 0.5 * spriteData.sprite.getWidth() - unmodifiedY * 0.5 * spriteData.sprite.getWidth());
                //intendedY = (float) (unmodifiedX * 0.25 * spriteData.sprite.getWidth() + unmodifiedY * 0.25 * spriteData.sprite.getWidth());
                intendedX = unmodified.x ;//* spriteData.sprite.getWidth();
                intendedY = unmodified.y;// * spriteData.sprite.getWidth();
                // offset, because why the fuck?
                intendedY += spriteData.spritePos3D.z * 0.5f - 0.5f;// * spriteData.sprite.getWidth();

               // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[sprite %s 2/2] intendedX %s | intendedY %s", spriteData.hashCode(), intendedX, intendedY));
                // offset
                intendedX -= spriteData.sprite.getOriginX() - 0.5f;
               // intendedX -= spriteData.sprite.getOriginX();

                if ((int)higlightThis.x == (int) spriteData.spritePos3D.x
                        && (int)higlightThis.y == (int) spriteData.spritePos3D.y) {
                    intendedY += 0.125f; //* spriteData.sprite.getWidth();
                }
            } else {
                if ((int)higlightThis.x == (int) spriteData.spritePos3D.x
                        && (int)higlightThis.y == (int) spriteData.spritePos3D.y) {
                    spriteData.sprite.rotate(45);
                }
            }
            spriteData.sprite.setPosition(
                intendedX,
                intendedY
            );

            float distanceFromFocus = lockOnVector.dst(spriteData.spritePos3D);
            float distanceToFocus = 1- (distanceFromFocus / renderDistance);//tilePos.x, tilePos.y, tilePos.z*0.5f));
            //Gdx.app.getApplicationLogger().log("Render Pipeline", String.format("distance of tile %s to focus is %s", tile.tag, distanceToFocus));


            float distanceFog =1 - (distanceFromFocus / fogOfWarRange);

            // this should definitely be delegated to a some shader...
            if (useWorldTimeShading) {
                spriteData.sprite.setColor(this.background.cpy().lerp(lightingColor, 0.5f).mul(distanceFog, distanceFog, distanceFog, 1));
            } else {
                spriteData.sprite.setColor(new Color(distanceFog, distanceFog, distanceFog, 1f));
            }

            spriteData.sprite.draw(batch, distanceToFocus > 0.3f ? 1f : distanceToFocus > 0.20 ? 0.5f : distanceToFocus > 0.10 ? 0.25f : 0f);
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
       // batch.draw(background_image, 0,0, 0,0, windowWidth, windowHeight);
    }

}
