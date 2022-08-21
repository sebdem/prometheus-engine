package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.entity.systems.CollisionDetectionSystem;
import dbast.prometheus.engine.entity.systems.MovementSystem;
import dbast.prometheus.engine.entity.systems.PlayerInputSystem;
import dbast.prometheus.engine.entity.systems.StateUpdateSystem;
import dbast.prometheus.engine.events.Event;
import dbast.prometheus.engine.events.EventBus;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.level.WaveFunctionTest;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.SpriteBuffer;
import dbast.prometheus.utils.Vector3Comparator;
import javafx.collections.transformation.SortedList;

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

    private List<Class<? extends Component>> entityRenderComponents;

    public static ApplicationLogger logger = Gdx.app.getApplicationLogger();

    public WorldScene(String key) {
        super(key);
    }

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
        if (useIsometric) {
            TileRegistry.register(
                    new Tile("water", new Texture(Gdx.files.internal("world/terrain/iso/water.png"))),
                    new Tile("waterD", new Texture(Gdx.files.internal("world/terrain/iso/water_deep.png"))),
                    new Tile("dirt_0", new Texture(Gdx.files.internal("world/terrain/iso/dirt_full.png"))),
                    new Tile("grass_0", new Texture(Gdx.files.internal("world/terrain/iso/grass.png"))),
                    new Tile("grass_top", new Texture(Gdx.files.internal("world/terrain/iso/grass_top.png"))),
                    new Tile("grass_1", new Texture(Gdx.files.internal("world/terrain/iso/grass_high.png"))),
                    new Tile("debug", new Texture(Gdx.files.internal("world/terrain/debug_tile.png"))),
                    new Tile("brickF", new Texture(Gdx.files.internal("world/terrain/iso/brick_full.png"))),
                    new Tile("glass_top", new Texture(Gdx.files.internal("world/terrain/iso/glass.png"))),
                    new Tile("tree", new Texture(Gdx.files.internal("world/environment/"+ ((useIsometric) ?  "iso_" : "") +"tree.png"))),
                    new Tile("treeS", new Texture(Gdx.files.internal("world/environment/"+ ((useIsometric) ?  "iso_" : "") +"tree_short.png"))),
                    new Tile("cube", new Texture(Gdx.files.internal("iso_cube.png"))),
                    new Tile("path_dirt", new Texture(Gdx.files.internal("world/terrain/iso/path_stone.png")))
            );
        } else {
            TileRegistry.register(
                new Tile("water", new Texture(Gdx.files.internal("world/terrain/water.png"))),
                new Tile("dirt_0", new Texture(Gdx.files.internal("world/terrain/dirt_full.png"))),
                new Tile("grass_0", new Texture(Gdx.files.internal("world/terrain/grass.png"))),
                new Tile("debug", new Texture(Gdx.files.internal("world/terrain/debug_tile.png"))),
                    new Tile("tree", new Texture(Gdx.files.internal("world/environment/tree.png"))),
                    new Tile("treeS", new Texture(Gdx.files.internal("world/environment/tree.png")))
            );
        }

        // ==== [ prepare world ] ============================
        world = new WaveFunctionTest(50,50, 25, false, true, null, 0,10).setup();

        // ==== [ camera setup ] ============================
        Entity cameraFocus = world.entities.stream().filter(entity -> entity.hasComponent(InputControllerComponent.class)).findAny().orElse(world.entities.get(0));
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
        movementSystem = new MovementSystem(new Rectangle(0f,0f, world.width, world.height));
        stateSystem = new StateUpdateSystem();

        entityRenderComponents = Arrays.asList(SpriteComponent.class, PositionComponent.class, SizeComponent.class);

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
                cam.lockOnEntity(world.entities.get((int) (Math.random()*world.entities.size())));
            }
            if (keycode == Input.Keys.F7) {
                cam.lockOnEntity(world.entities.stream().filter(entity -> entity.hasComponent(InputControllerComponent.class)).findAny().orElse(world.entities.get(0)));
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

                if (useIsometric) {
                    Vector3 boundaries = new Vector3(cam.viewportWidth, cam.viewportHeight, 0f);
                    logger.log("Input event: ","boundaries is " + boundaries.toString());
                    boundaries.prj(LockOnCamera.isoTransform);
                    logger.log("Input event: ","boundaries is " + boundaries.toString());

                    boolean useIsoTransformForScreen = true;
                    if (useIsoTransformForScreen) {
                        mousePos.prj(LockOnCamera.isoTransform);

                        // for some odd reason these two appear to be switched...
                        float mouseX = mousePos.x;
                        mousePos.x = mousePos.y;
                        mousePos.y = -mouseX;
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

    public void prepareTileSprites() {
        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        //  logger.log("rendering:", "tiles " + world.terrainTiles.size());
        world.terrainTiles.forEach((tilePos, tile)-> {
            if (lockOnPosition.isNearby(tilePos.x, tilePos.y, renderDistance)) {

                Sprite tileSprite = new Sprite(tile.tileTexture);

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

    public void prepareEntitySprites() {

        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        List<Entity> entities = world.entities.stream().filter(entity ->
                entity.hasComponents(entityRenderComponents) && lockOnPosition.isNearby(entity.getComponent(PositionComponent.class),renderDistance)
        ).collect(Collectors.toList());

        for (Entity entity : entities) {
            SpriteComponent spriteComponent = entity.getComponent(SpriteComponent.class);
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            SizeComponent sizeComponent = entity.getComponent(SizeComponent.class);

            // TODO might needs cpy()
            Vector3 entityPos = positionComponent.position;
            world.toNextUpperLevel(entityPos);
            float spriteX = entityPos.x;
            float spriteY = entityPos.y;

            Sprite sprite = spriteComponent.getSprite();
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

    public  static boolean naturalRenderOrder = true;

    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        font.setColor(Color.WHITE);

        spriteQueue.clear();

        prepareTileSprites();
        prepareEntitySprites();

        if (naturalRenderOrder) {
            spriteQueue.sort(Comparator.naturalOrder());
        } else {
            spriteQueue.sort(Comparator.reverseOrder());
        }

        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        spriteQueue.forEach((spriteData)-> {
            // begin of render pipeline...
            // TODO can this be delegated to actual shader logic, hopefully sparing sweet CPU calculation time??
            float intendedX = spriteData.spritePos3D.x;
            float intendedY = spriteData.spritePos3D.y;
            // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[sprite %s 1/2] intendedX %s | intendedY %s", spriteData.hashCode(), intendedX, intendedY));
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

                int sx = (int) spriteData.spritePos3D.x;
                int sy = (int) spriteData.spritePos3D.y;
                if ((int)higlightThis.x == sx && (int)higlightThis.y == sy) {
                    intendedY += 0.125f; //* spriteData.sprite.getWidth();
                }
            } else {
                int sx = (int) spriteData.spritePos3D.x;
                int sy = (int) spriteData.spritePos3D.y;
                if ((int)higlightThis.x == sx && (int)higlightThis.y == sy) {
                    spriteData.sprite.rotate(45);
                }
            }
            spriteData.sprite.setPosition(
                intendedX,
                intendedY
            );

            float distanceToFocus = 1- (lockOnPosition.position.cpy().scl(1,1,0f).dst(spriteData.spritePos3D.cpy().scl(1,1,0f)) / renderDistance);//tilePos.x, tilePos.y, tilePos.z*0.5f));
            //Gdx.app.getApplicationLogger().log("Render Pipeline", String.format("distance of tile %s to focus is %s", tile.tag, distanceToFocus));

            spriteData.sprite.draw(batch, distanceToFocus > 0.3f ? 1f : distanceToFocus > 0.20 ? 0.5f : distanceToFocus > 0.10 ? 0.25f : 0f);
           // spriteData.sprite.draw(batch, distanceToFocus );
           // spriteData.sprite.draw(batch, distanceToFocus > renderDistance ? 0.5f : 1f);
        });


    }

    public void update(float deltaTime){
        super.update(deltaTime);
        EventBus.update(deltaTime);

        collisionDetectionSystem.execute(deltaTime, collisionDetectionSystem.onlyQualified(world.entities));
        playerInputSystem.execute(deltaTime, playerInputSystem.onlyQualified(world.entities));
        movementSystem.execute(deltaTime, movementSystem.onlyQualified(world.entities));
        stateSystem.execute(deltaTime, stateSystem.onlyQualified(world.entities));

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
