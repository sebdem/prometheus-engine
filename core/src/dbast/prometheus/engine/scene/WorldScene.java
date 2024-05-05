package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import dbast.prometheus.engine.graphics.PrometheusSpriteBatch;
import dbast.prometheus.engine.graphics.SpriteData;
import dbast.prometheus.engine.graphics.SpriteType;
import dbast.prometheus.engine.serializing.WorldMapLoader;
import dbast.prometheus.engine.world.Direction;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.WorldTime;
import dbast.prometheus.engine.world.generation.features.CastleTower;
import dbast.prometheus.engine.world.generation.features.Hole;
import dbast.prometheus.engine.world.level.*;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;
import dbast.prometheus.engine.graphics.SpriteDataQueue;

import java.util.*;

public class WorldScene extends AbstractScene{

    private static final Vector3 LIGHT_POS = new Vector3(0,0, 0);
    private static final Vector3 SUN_DIR = new Vector3(-1f,0, 1);
    protected BitmapFont font;
    protected PrometheusSpriteBatch batch;

    protected WorldSpace world;
    private LockOnCamera cam;

    private CollisionDetectionSystem collisionDetectionSystem;
    private PlayerInputSystem playerInputSystem;
    private MovementSystem movementSystem;
    private StateUpdateSystem stateSystem;
    private AIInputSystem aiInputSystem;

    private RenderSystem renderSystem;

    @Deprecated
    private List<Class<? extends Component>> entityRenderComponents;

    public static ApplicationLogger logger = Gdx.app.getApplicationLogger();

    protected Texture background_image;

    // TODO move them somewhere more fit...
    protected Sprite borderSpriteNorth;
    protected Sprite borderSpriteEast;
    private Texture iso_normal;
    private Texture iso_height;

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
        iso_normal = new Texture(Gdx.files.internal("world/terrain/iso/normal_c.png"));
        iso_height = new Texture(Gdx.files.internal("world/terrain/iso/height_c.png"));

        borderSpriteNorth = new Sprite(new Texture(Gdx.files.local("world/terrain/iso/border_n.png")));
        borderSpriteNorth.setOrigin(0.5f, 1f-(0.5f/(borderSpriteNorth.getRegionHeight() / baseSpriteSize)));
        borderSpriteNorth.setSize(borderSpriteNorth.getRegionWidth() / (float)baseSpriteSize, borderSpriteNorth.getRegionHeight() / baseSpriteSize);

        borderSpriteEast = new Sprite(new Texture(Gdx.files.local("world/terrain/iso/border_e.png")));
        borderSpriteEast.setOrigin(0.5f, 1f-(0.5f/(borderSpriteEast.getRegionHeight() / baseSpriteSize)));
        borderSpriteEast.setSize(borderSpriteEast.getRegionWidth() / (float)baseSpriteSize, borderSpriteEast.getRegionHeight() / baseSpriteSize);


        // ==== [ prepare world ] ============================
      /*world = new WaveFunctionTest(100, 100, 20, false, true,
                Arrays.asList(
                        new CastleTower("brickF"),
                        new CastleTower("dirt_0"),
                        new Hole()
                ), 6,20).setup();*/
        //world = new Superflat(30, 30).setup();
       world = new MultilayeredPerlinTest(200, 200).setup();
      //  world = new MinimalLevel2().setup();
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
        renderSystem = new RenderSystem(this.world, this.cam);

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
                //cam.setCameraDistance(cam.getCameraDistance() + 0.25f);
                //renderDistance += 0.25f;
                LIGHT_POS.z += 0.0125f;
            }
            if (keycode == Input.Keys.PLUS) {
              //  cam.setCameraDistance(cam.getCameraDistance() - 0.25f);
               // renderDistance -= 0.25f;
                LIGHT_POS.z -= 0.0125f;
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

        ShaderProgram customShader = new ShaderProgram(
                Gdx.files.internal("resources/native/shaders/vertex.glsl").readString(),
                Gdx.files.internal("resources/native/shaders/fragment.glsl").readString()
        );
        ShaderProgram.pedantic = false;
        customShader.begin();
        if (customShader.hasUniform("u_normal")) {
            customShader.setUniformi("u_normal", 1);
        } else {
            logger.log("shader", "Screaming into the void");
        }
        if (customShader.hasUniform("u_height")) {
            customShader.setUniformi("u_height", 2);
        } else {
            logger.log("shader", "Screaming into the void");
        }
        customShader.end();

        batch = new PrometheusSpriteBatch(1000, customShader);

        spriteQueue = new SpriteDataQueue();

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);

        //bgmMusic.play();
        return this;
    }

    @Override
    public void render(int windowWidth, int windowHeight, float aspect) {
        batch.setProjectionMatrix(cam.combined);
       // batch.maxSpritesInBatch = 4000;

        /*Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        iso_normal.bind();*/
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
        iso_height.bind();
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        batch.begin();

        ShaderProgram shader = batch.getShader();
/*
Gdx.app.log("shader", shader.getLog());
*/
        //update light position, normalized to screen resolution
        float x = this.playerInputSystem.cursorInput.x / (float)Gdx.graphics.getWidth();
        float y = this.playerInputSystem.cursorInput.y / (float)Gdx.graphics.getHeight();
        LIGHT_POS.x = x;
        LIGHT_POS.y = 1f-y;
       // LIGHT_POS.z = 0;

        float dayProgress = world.worldTime.getDayProgress(world.age);
        SUN_DIR.x = (dayProgress * 2) - 1f;
        SUN_DIR.x = (playerInputSystem.aspectInput.x * 2) - 1f;
        SUN_DIR.y = (playerInputSystem.aspectInput.y * 2) - 1f;
        //send a Vector4f to GLSL

        shader.setUniformf("Resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shader.setUniformf("LightPos", LIGHT_POS);
        shader.setUniformf("GlobalLightDir", SUN_DIR);
        Color timeLightingColor = world.worldTime.getLightingColor(world.age);
        //shader.setUniformf("LightColor", new Color(1f, 0.8f, 0.6f, 1f));
        shader.setUniformf("LightColor", new Color(1f, 0.8f, 0.6f, 1f));
        Color timeEnvironmentColor = world.worldTime.getSkyboxColor(world.age);
       // shader.setUniformf("AmbientColor", new Color(0.6f, 0.6f, 1f, 0.1f));
        timeEnvironmentColor.a = 0.1f;
        shader.setUniformf("AmbientColor", timeEnvironmentColor);
        float timeVisibleRange = world.worldTime.getSightRange(world.age);
       // shader.setUniformf("Falloff", new Vector3(.4f, 3f, 20f));
        //shader.setUniformf("Falloff", new Vector3(0.5f, 1f, 10f));
        shader.setUniformf("Falloff", new Vector3(1,0.5f,0));
        //shader.setUniformf("Falloff", new Vector3(0.5f, 0.5f, 20f - (timeVisibleRange / 20f)/*0.125f*/));

        preRender(windowWidth, windowHeight, aspect);
        mainRender(windowWidth, windowHeight, aspect);
        batch.end();
       // logger.log("BATCH RENDERER", "Render Calls for frame: " + batch.renderCalls);
        afterRender(windowWidth, windowHeight, aspect);
    }

    SpriteDataQueue spriteQueue = null;
    public static float renderDistance = (Float)PrometheusConfig.conf.getOrDefault("renderDistance",18f);
    public static float baseSpriteSize = (Float)PrometheusConfig.conf.getOrDefault("baseSpriteSize",16f);

    protected static float gridSnapIncrement = (Float)PrometheusConfig.conf.getOrDefault("gridSnapIncrement", 0.0625f);
    protected static boolean useGridSnapping = (Boolean)PrometheusConfig.conf.getOrDefault("gridSnapping", false);
    protected static boolean useIsometric = (Boolean)PrometheusConfig.conf.getOrDefault("isometric", false);
    protected static boolean useWorldTimeShading = (Boolean)PrometheusConfig.conf.getOrDefault("useWorldTimeShading", false);

    public  static boolean naturalRenderOrder = true;

    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        font.setColor(Color.WHITE);
      //  spriteQueue.clear();
        float deltaTime = Gdx.graphics.getDeltaTime();
        //prepareTileSprites(deltaTime);
        //prepareEntitySprites(deltaTime);

       /* if (naturalRenderOrder) {
            spriteQueue.sort(Comparator.naturalOrder());
        } else {
            spriteQueue.sort(Comparator.reverseOrder());
        }*/
        ShaderProgram shader = batch.getShader();

        PositionComponent lockOnPosition =  cam.getLockOnEntity().getComponent(PositionComponent.class);
        Vector3 lockOnVector = lockOnPosition.position;
        Vector3 highlightThis = playerInputSystem.inWorldPos;
      //  logger.log("Highlight:", String.format("Highlighting %s", highlightThis.toString()));

        float fogOfWarRange = world.getSightRange();
        Color lightingColor = world.worldTime.getLightingColor(world.age);

        boolean renderShadow = false;
        boolean renderBorderSprites = false;
        boolean renderOrderIndex = false;
        boolean viewCulling = false;
        boolean fogOfWar = false;


        for (SpriteData spriteData: renderSystem.spriteQueue) {
         /*   logger.log("Rendering", "Sprite at " +  spriteData.levelPosition.toString()); */

            // begin of render pipeline...
            // TODO can this be delegated to actual shader logic, hopefully sparing sweet CPU calculation time??
            float intendedX = spriteData.screenPosition.x;
            float intendedY = spriteData.screenPosition.y;
            // Gdx.app.getApplicationLogger().log("render pipeline", String.format("[sprite %s 1/2] intendedX %s | intendedY %s", spriteData.hashCode(), intendedX, intendedY));

            if (useIsometric) {
                if ((int)highlightThis.x == (int) spriteData.levelPosition.x
                        && (int)highlightThis.y == (int) spriteData.levelPosition.y
                       /* && (int)highlightThis.z == (int) spriteData.position.z*/) {
                    intendedY += 0.125f; //* spriteData.sprite.getWidth();
                }
            } else {
                if ((int)highlightThis.x == (int) spriteData.levelPosition.x
                        && (int)highlightThis.y == (int) spriteData.levelPosition.y) {
                    spriteData.sprite.rotate(45);
                }
            }

            // draw "shadow"
            // kinda works, but looks absolutely stupid
            /*if (renderShadow) {
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
            }*/

            // reposition sprite
            spriteData.sprite.setPosition(
                intendedX,
                intendedY
            );

            float distanceFromFocus = (fogOfWar) ? lockOnVector.dst(spriteData.levelPosition) : 1f;
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
                if (!spriteData.notBlocked.isEmpty()) {
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
        }
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

        renderSystem.execute(deltaTime, world.entities.compatibleWith(renderSystem));

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
