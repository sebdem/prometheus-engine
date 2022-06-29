package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
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
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.Vector3Comparator;

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

        // ==== [ load files ] ============================
        if (useIsometric) {
            TileRegistry.register(
                    new Tile("water", new Texture(Gdx.files.internal("world/terrain/iso/water.png"))),
                    new Tile("waterD", new Texture(Gdx.files.internal("world/terrain/iso/water_deep.png"))),
                    new Tile("dirt_0", new Texture(Gdx.files.internal("world/terrain/iso/dirt_full.png"))),
                    new Tile("grass_0", new Texture(Gdx.files.internal("world/terrain/iso/grass.png"))),
                    new Tile("grass_top", new Texture(Gdx.files.internal("world/terrain/iso/grass_top.png"))),
                    new Tile("debug", new Texture(Gdx.files.internal("world/terrain/debug_tile.png"))),
                    new Tile("brickF", new Texture(Gdx.files.internal("world/terrain/iso/brick_full.png"))),
                    new Tile("tree", new Texture(Gdx.files.internal("world/environment/"+ ((useIsometric) ?  "iso_" : "") +"tree.png"))),
                    new Tile("cube", new Texture(Gdx.files.internal("iso_cube.png")))
            );
        } else {
            TileRegistry.register(
                new Tile("water", new Texture(Gdx.files.internal("world/terrain/water.png"))),
                new Tile("dirt_0", new Texture(Gdx.files.internal("world/terrain/dirt_full.png"))),
                new Tile("grass_0", new Texture(Gdx.files.internal("world/terrain/grass.png"))),
                new Tile("debug", new Texture(Gdx.files.internal("world/terrain/debug_tile.png")))
            );
        }

        // ==== [ prepare world ] ============================
        world = WorldSpace.testLevel();

        // ==== [ camera setup ] ============================
        Entity cameraFocus = world.entities.stream().filter(entity -> entity.hasComponent(InputControllerComponent.class)).findAny().orElse(world.entities.get(0));
        cam = new LockOnCamera(80f, 16 ,9);
        cam.lockOnEntity(cameraFocus);
        cam.setEntityOffset(cameraFocus.getComponent(SizeComponent.class).toVector3().scl(0.5f,0.25f,0.5f));//.scl(0.5f));
        cam.setCameraDistance(6f);
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
                this.tileBatch.clear();
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
                cam.setCameraDistance(cam.getCameraDistance() + 0.125f);
                renderDistance -= 1f;
            }
            if (keycode == Input.Keys.PLUS) {
                cam.setCameraDistance(cam.getCameraDistance() - 0.125f);
                renderDistance += 1f;
            }
            Vector3 mousePos = cam.unproject( new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            logger.log("Input event: ","Mouse position is " + mousePos.toString());
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
        });

        // ==== [ init batch ] ============================
        batch = new SpriteBatch();
        //drawToBatch = new TreeMap<>(new Vector3Comparator.Relative(cam));
        drawToBatch = new TreeMap<>(Vector3Comparator.getConfigured());

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
    //Map<Vector3, Sprite> drawToBatch = new TreeMap<>(Vector3Comparator.getConfigured());
    Map<Vector3, Sprite> drawToBatch = null;
    Map<Vector3, Sprite> tileBatch = new HashMap<>();
    Map<Vector3, Sprite> entityBatch = new HashMap<>();

    static float renderDistance = 24f;

    public void prepareTileSprites() {
        tileBatch.clear();
        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        if (tileBatch.isEmpty()) {
            //  logger.log("rendering:", "tiles " + world.terrainTiles.size());
            world.terrainTiles.forEach((tilePos, tile)-> {
                if (lockOnPosition.isNearby(tilePos.x, tilePos.y, renderDistance)) {

                    Sprite tileSprite = new Sprite(tile.tileTexture);
                    float distanceToFocus = lockOnPosition.toVector3().dst(tilePos.cpy().set(tilePos.x, tilePos.y, tilePos.z*0.5f));
                    //Gdx.app.getApplicationLogger().log("Render Pipeline", String.format("distance of tile %s to focus is %s", tile.tag, distanceToFocus));
                    if (distanceToFocus > renderDistance) {
                        tileSprite.setAlpha(0.5f);
                    }

                    tileSprite.setOrigin(0.5f, 0f);
                    tileSprite.setPosition(tilePos.x, tilePos.y);
                    tileSprite.setSize(tileSprite.getRegionWidth() / (float)tile.tileTexture.getWidth(), tileSprite.getRegionHeight() / (float)tile.tileTexture.getWidth());

                    tileBatch.put(tilePos, tileSprite);

    // =========================================================================================================
    //  --------------------- start debugging nonsense --------------------------------------------------------
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
                        pixmap.drawPixel((int)(tileSprite.getOriginX() * pixmap.getWidth()-1),(int)(tileSprite.getOriginY() * pixmap.getHeight()-1));

                        spriteTexture.disposePixmap();

                        tileSprite.setTexture(new Texture(pixmap));
                    }
    // =========================================================================================================
    //  --------------------- end debugging nonsense ----------------------------------------------------------
    // =========================================================================================================

                }
            });
        }
        drawToBatch.putAll(tileBatch);
    }

    private Set<Integer> debuggedSprites = new HashSet<>();

    public void prepareEntitySprites() {
        entityBatch.clear();

        Entity cameraLockOn = cam.getLockOnEntity();
        PositionComponent lockOnPosition = cameraLockOn.getComponent(PositionComponent.class);

        List<Entity> entities = world.entities.stream().filter(entity ->
                entity.hasComponents(entityRenderComponents) && lockOnPosition.isNearby(entity.getComponent(PositionComponent.class),renderDistance)
        ).collect(Collectors.toList());


        for (Entity entity : entities) {
            SpriteComponent spriteComponent = entity.getComponent(SpriteComponent.class);
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            SizeComponent sizeComponent = entity.getComponent(SizeComponent.class);

            Sprite sprite = spriteComponent.getSprite();
           // sprite.setOrigin(0.25f, 0.5f);
            sprite.setOrigin(0.5f, 1f);
            sprite.setPosition(positionComponent.getX_pos(), positionComponent.getY_pos());
            sprite.setSize(sizeComponent.getWidth(), sizeComponent.getHeight());

            // sprite.draw(batch);
            Vector3 entityPos = positionComponent.toVector3();
            world.toNextUpperLevel(entityPos);
            //entityPos.z = 0f;

            entityBatch.put(entityPos, sprite);

// =========================================================================================================
//  --------------------- start debugging nonsense --------------------------------------------------------
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
                pixmap.drawPixel((int)(sprite.getOriginX() * pixmap.getWidth()-1),(int)(sprite.getOriginY() * pixmap.getHeight()-1));

                spriteTexture.disposePixmap();

                sprite.setTexture(new Texture(pixmap));
            }
// =========================================================================================================
//  --------------------- end debugging nonsense ----------------------------------------------------------
// =========================================================================================================
        }
        drawToBatch.putAll(entityBatch);
    }


    //public static float gridSnapIncrement =  0.0625f;
    protected static float gridSnapIncrement = (Float)PrometheusConfig.conf.getOrDefault("gridSnapIncrement", 0.0625f);
    protected static boolean useGridSnapping = (Boolean)PrometheusConfig.conf.getOrDefault("gridSnapping", false);
    protected static boolean useIsometric = (Boolean)PrometheusConfig.conf.getOrDefault("isometric", false);
    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        font.setColor(Color.WHITE);
        drawToBatch.clear();

        prepareTileSprites();
        prepareEntitySprites();

        drawToBatch.forEach((spritePos, sprite)-> {
            // beginn of render pipeline...
            // TODO can this be delegated to actual shader logic, hopefully sparing sweet CPU calculation time??
            float intendedX = spritePos.x;
            float intendedY = spritePos.y;
            if (useGridSnapping) {
                double xPos = Math.round(intendedX);
                double yPos = Math.round(intendedY);

                intendedX = (float)(xPos + (Math.round((intendedX - xPos) / gridSnapIncrement)) * gridSnapIncrement);
                intendedY = (float)(yPos + (Math.round((intendedY - yPos) / gridSnapIncrement)) * gridSnapIncrement);
            }
            if (useIsometric) {
                float unmodifiedX = intendedX;
                float unmodifiedY = intendedY;

                // sprite height should be used here according to render logic... For some reason spritebased sizes fuck this up however, therefor use width
                intendedX = (float) (unmodifiedX * 0.5 * sprite.getWidth() - unmodifiedY * 0.5 * sprite.getWidth());
                intendedY = (float) (unmodifiedX * 0.25 * sprite.getWidth() + unmodifiedY * 0.25 * sprite.getWidth());

                // offset all by 0.5f ~16px which would be half a base sprite, then do the same for every additional z axis
              //  intendedY += spritePos.z * (sprite.getOriginY() + 0.5f)* sprite.getWidth() - 0.5f;
               // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[sprite %s] intendedX %s | intendedY %s | offsetting Y by %s", sprite.hashCode(), intendedX, intendedY, spritePos.z));
                intendedY += spritePos.z * 0.5f - 0.5f * sprite.getWidth(); //(sprite.getOriginY());
            }
           // if(cam.frustum.pointInFrustum(intendedX, intendedY - spritePos.z * 0.5f * sprite.getHeight(), spritePos.z - 1f)) {
                sprite.setPosition(
                        intendedX,
                        intendedY
                );
          //  sprite.getTexture().draw(, 0, 0 );
                sprite.draw(batch);
           // }
        });
    }

    public void update(float deltaTime){
        super.update(deltaTime);

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
