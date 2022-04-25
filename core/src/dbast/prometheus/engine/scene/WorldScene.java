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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Logger;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.entity.systems.CollisionDetectionSystem;
import dbast.prometheus.engine.entity.systems.MovementSystem;
import dbast.prometheus.engine.entity.systems.PlayerInputSystem;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.WorldSpaceVariable;
import dbast.prometheus.utils.Vector3Comparator;

import java.util.*;

public class WorldScene extends AbstractScene{

    protected BitmapFont font;
    protected SpriteBatch batch;
    protected WorldSpaceVariable world;
    private Map<Integer, Texture> tileMap = new HashMap<>();

    private CollisionDetectionSystem collisionDetectionSystem;
    private PlayerInputSystem playerInputSystem;
    private List<Class<? extends Component>> entityRenderComponents;
    private MovementSystem movementSystem;
    private LockOnCamera cam;

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

        // TODO replace tileMap with separate class / registry / whatever
        tileMap.put(0, new Texture(Gdx.files.internal("world/terrain/water.png")));
        tileMap.put(1, new Texture(Gdx.files.internal("world/terrain/dirt.png")));
        tileMap.put(2, new Texture(Gdx.files.internal("world/terrain/grass.png")));
        tileMap.put(3, new Texture(Gdx.files.internal("world/terrain/debug_tile.png")));

        world = WorldSpaceVariable.testLevel();

        Entity cameraFocus = world.entities.stream().filter(entity -> entity.hasComponents(Arrays.asList(InputControllerComponent.class))).findAny().orElse(world.entities.get(0));

        cam = new LockOnCamera(80f, 32 ,18);
        cam.lockOnEntity(cameraFocus);
        cam.setEntityOffset(cameraFocus.getComponent(SizeComponent.class).toVector3().scl(0.5f));
        cam.setCameraDistance(6f);
        cam.setViewingAngle(Math.toRadians(90));

        collisionDetectionSystem = new CollisionDetectionSystem();
        playerInputSystem = new PlayerInputSystem();
        movementSystem = new MovementSystem(new Rectangle(0f,0f, world.width, world.height));
        entityRenderComponents = Arrays.asList(SpriteComponent.class, PositionComponent.class, SizeComponent.class);

        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(this.gui);
        this.gui.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.F3) {
                    gui.setDebugAll(!gui.isDebugAll());
                }

                if (keycode == Input.Keys.PAGE_UP) {
                    cam.setViewingAngle(Math.toRadians(Math.toDegrees(cam.getViewingAngle()) + 15f));
                }
                if (keycode == Input.Keys.PAGE_DOWN) {
                    cam.setViewingAngle(Math.toRadians(Math.toDegrees(cam.getViewingAngle()) - 15f));
                }

                return super.keyDown(event, keycode);
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (character == '+') {
                    cam.setCameraDistance(cam.getCameraDistance() + 0.125f);
                }
                if (character == '-') {
                    cam.setCameraDistance(cam.getCameraDistance() - 0.125f);
                }
                return super.keyTyped(event, character);
            }
        });


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

    Map<Vector3, Sprite> drawToBatch = new TreeMap<>(new Vector3Comparator());

    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        Texture tile;
        int[] xArray;
        font.setColor(Color.WHITE);

        drawToBatch.clear();

        world.terrainTiles.forEach((tilePos, tileId)-> {
            Sprite tileSprite = new Sprite(tileMap.get(tileId));
            //tileSprite.setPosition(tilePos.x,(float)(tilePos.y + tilePos.z * Math.sin(cam.getViewingAngle())));
            tileSprite.setPosition(tilePos.x,tilePos.y);
            tileSprite.setSize(1f,1f);

            drawToBatch.put(tilePos, tileSprite);
           // tileSprite.draw(batch);
        });

        for(int entityIndex = 0; entityIndex < world.entities.size(); entityIndex++) {
            Entity entity = world.entities.get(entityIndex);
            if (entity.hasComponents(entityRenderComponents)) {
                SpriteComponent spriteComponent = entity.getComponent(SpriteComponent.class);
                PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
                SizeComponent sizeComponent = entity.getComponent(SizeComponent.class);

                Sprite sprite = spriteComponent.getSprite();
                sprite.setPosition(positionComponent.getX_pos(), positionComponent.getY_pos());
                sprite.setSize(sizeComponent.getWidth(),sizeComponent.getHeight());

               // sprite.draw(batch);
                drawToBatch.put(positionComponent.toVector3().add(0,0,0.5f), sprite);

                if(gui.isDebugAll()) {
                    Pixmap pixmap = new Pixmap(sprite.getRegionWidth(),sprite.getRegionHeight(), Pixmap.Format.RGBA8888);
                    pixmap.setColor(0xCCCCCC88);
                    pixmap.drawRectangle(0,0,pixmap.getWidth(),pixmap.getHeight());
                    batch.draw(new Texture(pixmap), positionComponent.getX_pos(), positionComponent.getY_pos(), sizeComponent.getWidth(), sizeComponent.getHeight());

                    if(entity.hasComponent(CollisionBox.class)) {
                        CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
                        pixmap = new Pixmap(sprite.getRegionWidth(),sprite.getRegionHeight(), Pixmap.Format.RGBA8888);
                        pixmap.setColor(0xDDDD00CC);
                        pixmap.drawRectangle(0,0,pixmap.getWidth(),pixmap.getHeight());
                        batch.draw(new Texture(pixmap), positionComponent.getX_pos(), positionComponent.getY_pos(), collisionBox.getWidth(), collisionBox.getHeight());
                    }
                }
            }
        }


       drawToBatch.forEach((spritePos, sprite)-> {
           sprite.draw(batch);
       });

    }

    public void update(float deltaTime){
        super.update(deltaTime);

        collisionDetectionSystem.execute(deltaTime, collisionDetectionSystem.onlyQualified(world.entities));
        playerInputSystem.execute(deltaTime, playerInputSystem.onlyQualified(world.entities));
        movementSystem.execute(deltaTime, movementSystem.onlyQualified(world.entities));

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
