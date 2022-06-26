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
    @Deprecated
    private Map<Integer, Texture> tileMap = new HashMap<>();
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
        TileRegistry.register(
            new Tile("water", new Texture(Gdx.files.internal("world/terrain/water.png"))),
            new Tile("dirt_0", new Texture(Gdx.files.internal("world/terrain/dirt_full.png"))),
            new Tile("grass_0", new Texture(Gdx.files.internal("world/terrain/grass.png"))),
            new Tile("debug", new Texture(Gdx.files.internal("world/terrain/debug_tile.png")))
        );

        // ==== [ prepare world ] ============================
        world = WorldSpace.testLevel();

        // ==== [ camera setup ] ============================
        Entity cameraFocus = world.entities.stream().filter(entity -> entity.hasComponent(InputControllerComponent.class)).findAny().orElse(world.entities.get(0));
        cam = new LockOnCamera(80f, 32 ,18);
        cam.lockOnEntity(cameraFocus);
        cam.setEntityOffset(cameraFocus.getComponent(SizeComponent.class).toVector3().scl(0.5f));
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
            }
            if (keycode == Input.Keys.PLUS) {
                cam.setCameraDistance(cam.getCameraDistance() - 0.125f);
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
        });

        // ==== [ init batch ] ============================
        batch = new SpriteBatch();

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
    Map<Vector3, Sprite> drawToBatch = new TreeMap<>(new Vector3Comparator());
    Map<Vector3, Sprite> tileBatch = new HashMap<>();
    Map<Vector3, Sprite> entityBatch = new HashMap<>();

    public void prepareTileSprites() {
        if (tileBatch.isEmpty()) {
            //  logger.log("rendering:", "tiles " + world.terrainTiles.size());
            world.terrainTiles.forEach((tilePos, tile)-> {
                Sprite tileSprite = new Sprite(tile.tileTexture);
                tileSprite.setPosition(tilePos.x, tilePos.y);//(float)(tilePos.y + tilePos.z * Math.sin(cam.getViewingAngle())));
                tileSprite.setSize(1f,1f);
                //  logger.log("rendering-tiles:", "tile at " + tilePos.toString());
                tileBatch.put(tilePos, tileSprite);
            });
            //  logger.log("rendering:", "finished tile preparation " + tileBatch.size());
        }
        drawToBatch.putAll(tileBatch);
    }


    public void prepareEntitySprites() {
        entityBatch.clear();
        List<Entity> entities = world.entities.stream().filter(entity -> entity.hasComponents(entityRenderComponents)).collect(Collectors.toList());
        for (Entity entity : entities) {
            SpriteComponent spriteComponent = entity.getComponent(SpriteComponent.class);
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            SizeComponent sizeComponent = entity.getComponent(SizeComponent.class);

            Sprite sprite = spriteComponent.getSprite();
            sprite.setPosition(positionComponent.getX_pos(), positionComponent.getY_pos());
            sprite.setSize(sizeComponent.getWidth(), sizeComponent.getHeight());

            // sprite.draw(batch);
            entityBatch.put(positionComponent.toVector3().add(0, 0, 0.5f), sprite);

            //entity debug rendering
            if (gui.isDebugAll()) {
                Pixmap pixmap = new Pixmap(sprite.getRegionWidth(), sprite.getRegionHeight(), Pixmap.Format.RGBA8888);
                pixmap.setColor(0xCCCCCC88);
                pixmap.drawRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
                batch.draw(new Texture(pixmap), positionComponent.getX_pos(), positionComponent.getY_pos(), sizeComponent.getWidth(), sizeComponent.getHeight());

                if (entity.hasComponent(CollisionBox.class)) {
                    CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
                    pixmap = new Pixmap(sprite.getRegionWidth(), sprite.getRegionHeight(), Pixmap.Format.RGBA8888);
                    pixmap.setColor(0xDDDD00CC);
                    pixmap.drawRectangle(0, 0, pixmap.getWidth(), pixmap.getHeight());
                    batch.draw(new Texture(pixmap), positionComponent.getX_pos(), positionComponent.getY_pos(), collisionBox.getWidth(), collisionBox.getHeight());
                }
            }
        }
        drawToBatch.putAll(entityBatch);
    }


    //public static float gridSnapIncrement =  0.0625f;
    protected static float gridSnapIncrement = (Float)PrometheusConfig.conf.getOrDefault("gridSnapIncrement", 0.0625f);

    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        font.setColor(Color.WHITE);
        drawToBatch.clear();

        prepareTileSprites();
        prepareEntitySprites();
        if ((Boolean)PrometheusConfig.conf.getOrDefault("gridSnapping", false)) {
            drawToBatch.forEach((spritePos, sprite)-> {
                double xPos = Math.round(spritePos.x);
                double yPos = Math.round(spritePos.y);


                sprite.setPosition(
                    (float)(xPos + (Math.round((spritePos.x - xPos) / gridSnapIncrement)) * gridSnapIncrement),
                    (float)(yPos + (Math.round((spritePos.y - yPos) / gridSnapIncrement)) * gridSnapIncrement)
                );
                sprite.draw(batch);
            });
        } else {
            drawToBatch.forEach((spritePos, sprite)-> {
                sprite.draw(batch);
            });
        }
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
