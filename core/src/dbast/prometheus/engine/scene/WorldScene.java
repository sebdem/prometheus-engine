package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Logger;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.entity.systems.CollisionDetectionSystem;
import dbast.prometheus.engine.entity.systems.MovementSystem;
import dbast.prometheus.engine.entity.systems.PlayerInputSystem;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.WorldSpaceVariable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldScene extends AbstractScene{

    protected BitmapFont font;
    protected SpriteBatch batch;
    protected WorldSpaceVariable world;
    private Map<Integer, Texture> tileMap = new HashMap<>();

    private CollisionDetectionSystem collisionDetectionSystem;
    private PlayerInputSystem playerInputSystem;
    private List<Class<? extends Component>> entityRenderComponents;
    private MovementSystem movementSystem;
    private Camera cam;

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

        cam = new PerspectiveCamera(80f, 32 ,18);
        //cam = new OrthographicCamera(16 ,9);

        //cam.position.set(0,0,6f);
        //cam.rotate(20, 1f,0,0f);

        tileMap.put(0, new Texture(Gdx.files.internal("world/terrain/water.png")));
        tileMap.put(1, new Texture(Gdx.files.internal("world/terrain/dirt.png")));
        tileMap.put(2, new Texture(Gdx.files.internal("world/terrain/grass.png")));
        tileMap.put(3, new Texture(Gdx.files.internal("world/terrain/debug_tile.png")));

        world = WorldSpaceVariable.testLevel();

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
                logger.log("Events", "setting viewing angle from " + Math.toDegrees(viewingAngle)  );
                if (keycode == Input.Keys.PAGE_UP) {
                    viewingAngle = Math.toRadians(Math.toDegrees(viewingAngle) + 15f);
                }
                if (keycode == Input.Keys.PAGE_DOWN) {
                    viewingAngle = Math.toRadians(Math.toDegrees(viewingAngle) - 15f);
                }

                logger.log("Events", "to " + Math.toDegrees(viewingAngle)  );
                return super.keyDown(event, keycode);
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (character == '+') {
                    cameraDistance += 0.125f;
                }
                if (character == '-') {
                    cameraDistance -= 0.125f;
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

    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        Texture tile;
        int[] xArray;

        //logger.log("rendering:", String.format("width: %s, height: %s, aspectRatio:%s, density: %s, dpiX: %s|%s, dpiY: %s|%s, GWidth: %s, GHeight: %s",
        //        windowWidth, windowHeight, aspect, Gdx.graphics.getDensity(), Gdx.graphics.getPpiX(), Gdx.graphics.getPpcX(), Gdx.graphics.getPpiY(), Gdx.graphics.getPpcY(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        font.setColor(Color.WHITE);

        world.terrainTiles.forEach((tilePos, tileId)-> {
            Sprite tileSprite = new Sprite(tileMap.get(tileId));
            //tileSprite.setOrigin(0,0);
            tileSprite.setPosition(tilePos.x,(float)(tilePos.y + tilePos.z * Math.sin(viewingAngle)));
            tileSprite.setSize(1f,1f);
            tileSprite.draw(batch);
        });
        /*
        for(int y = 0; y < world.height; y++) {
            xArray = world.terrainTiles[y];
            for(int x = 0; x < world.width; x++) {
                tile = tileMap.get(xArray[x]);
                //batch.draw(tile, x * tileWidth, y * tileHeight, tileWidth, tileHeight) ;
                Sprite tileSprite = new Sprite(tile);
                tileSprite.setOrigin(0,0);
                tileSprite.setOriginBasedPosition(x,y);
                tileSprite.setSize(1f,1f);
                tileSprite.draw(batch);

                if(gui.isDebugAll()) {
                    //logger.log("rendering:", String.format("(%s,%s) with tile %s (managed=%s)", x, y, tileMap.get(xArray[x]), tile.isManaged()));

                    //Vector3 debugPos = cam.project(new Vector3(x,y,-1f));
                    Vector3 debugPos = new Vector3(x,y,1f);
                    logger.log("rendering:", String.format("(%s,%s) ", debugPos.x, debugPos.y));
                    font.draw(batch, String.format("(%s,%s)", x, y), debugPos.x, debugPos.y, 0f, Align.bottomLeft, false);

                    //font.draw(batch, String.format("(%s,%s)", x, y), 0, 32, coordWidth, Align.bottomLeft, false);
                }

            }
        }*/

        for(int entityIndex = 0; entityIndex < world.entities.size(); entityIndex++) {
            Entity entity = world.entities.get(entityIndex);
            if (entity.hasComponents(entityRenderComponents)) {
                SpriteComponent spriteComponent = (SpriteComponent)entity.getComponent(SpriteComponent.class);
                PositionComponent positionComponent = (PositionComponent)entity.getComponent(PositionComponent.class);
                SizeComponent sizeComponent = (SizeComponent)entity.getComponent(SizeComponent.class);

               // batch.draw(spriteComponent.getSprite(), positionComponent.getX_pos() * tileWidth, positionComponent.getY_pos() * tileHeight, sizeComponent.getWidth() * tileWidth,   sizeComponent.getHeight() * tileHeight) ;
                Sprite sprite = spriteComponent.getSprite();

                sprite.setOrigin(0,0);
                sprite.setOriginBasedPosition(positionComponent.getX_pos(), positionComponent.getY_pos());
                sprite.setSize(sizeComponent.getWidth(),sizeComponent.getHeight());

                sprite.draw(batch);
                if(gui.isDebugAll()) {
                    //font.setColor(Color.GOLD);
                    //font.draw(batch, String.format("(%s,%s)", positionComponent.getX_pos(), positionComponent.getY_pos()), positionComponent.getX_pos() * tileWidth, (positionComponent.getY_pos()+1) * tileHeight, coordWidth, Align.bottomLeft, false);
                }
            }
        }

    }

    public static double viewingAngle = Math.toRadians(90);
    public static float cameraDistance = 6f;

    public void update(float deltaTime){
        super.update(deltaTime);

        collisionDetectionSystem.execute(deltaTime, collisionDetectionSystem.onlyQualified(world.entities));
        playerInputSystem.execute(deltaTime, playerInputSystem.onlyQualified(world.entities));
        movementSystem.execute(deltaTime, movementSystem.onlyQualified(world.entities));

        // TODO stop looking through all entities. Just grab one and keep it, this way, scenes could also move the camera with an invisible entity...
        Entity cameraFocus = world.entities.stream().filter(entity -> entity.hasComponents(Arrays.asList(PositionComponent.class, InputControllerComponent.class))).findAny().orElse(null);
        if (cameraFocus != null) {
            PositionComponent position = (PositionComponent)cameraFocus.getComponent(PositionComponent.class);
            SizeComponent size = (SizeComponent)cameraFocus.getComponent(SizeComponent.class);
            // this shit definitely worked, do not delete yet...
            //cam.position.set(position.getX_pos(), position.getY_pos() - cam.viewportHeight *0.25f, cam.position.z);
            Vector3 newPosition = position.toVector3().add(size.toVector3().scl(0.5f));



            double tans =  cameraDistance;
           // logger.log("rendering:","camera distance: " + cameraDistance);

           // cam.position.set(newPosition.cpy().add(0, (float) -(Math.cos(viewingAngle) * tans),(float) (Math.sin(viewingAngle)  * tans)));
            //cam.position.set(newPosition.cpy().add(0, (float) -(Math.cos(viewingAngle) + tans ),(float) (Math.sin(viewingAngle)  + tans)));
            cam.position.set(newPosition.cpy().add(0,-(float)Math.cos(viewingAngle) * cameraDistance,(float)Math.sin(viewingAngle) * cameraDistance));
            //cam.position.set(newPosition.cpy().sub(0,2f,-10f));
            cam.lookAt(newPosition);
        }
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
