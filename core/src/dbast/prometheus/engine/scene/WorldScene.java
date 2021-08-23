package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.CollisionBox;
import dbast.prometheus.engine.entity.components.Component;
import dbast.prometheus.engine.entity.components.PositionComponent;
import dbast.prometheus.engine.entity.components.SpriteComponent;
import dbast.prometheus.engine.entity.systems.CollisionDetectionSystem;
import dbast.prometheus.engine.entity.systems.MovementSystem;
import dbast.prometheus.engine.entity.systems.PlayerInputSystem;
import dbast.prometheus.engine.world.TilePlane;

import javax.swing.text.Position;
import java.util.*;

public class WorldScene extends AbstractScene{

    protected BitmapFont font;
    protected SpriteBatch batch;
    protected TilePlane world;
    private Map<Integer, Texture> tileMap = new HashMap<>();

    private CollisionDetectionSystem collisionDetectionSystem;
    private PlayerInputSystem playerInputSystem;
    private List<Class<? extends Component>> entityRenderComponents;
    private MovementSystem movementSystem;

    public WorldScene(String key) {
        super(key);
    }

    @Override
    public WorldScene create() {
        super.create();
        font = new BitmapFont();
        font.setColor(Color.valueOf("#FFFFFF"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        tileMap.put(0, new Texture(Gdx.files.internal("world/terrain/water.png")));
        tileMap.put(1, new Texture(Gdx.files.internal("world/terrain/dirt.png")));
        tileMap.put(2, new Texture(Gdx.files.internal("world/terrain/grass.png")));

        world = TilePlane.testLevel();

        collisionDetectionSystem = new CollisionDetectionSystem();
        playerInputSystem = new PlayerInputSystem();
        movementSystem = new MovementSystem(new Rectangle(0f,0f, world.width, world.height));
        entityRenderComponents = Arrays.asList(SpriteComponent.class, PositionComponent.class, CollisionBox.class);


        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(this.gui);
        this.gui.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
            if (keycode == Input.Keys.F3) {
                gui.setDebugAll(!gui.isDebugAll());
            }
            return super.keyDown(event, keycode);
            }
        });

        return this;
    }

    @Override
    public void render(int windowWidth, int windowHeight, float aspect) {
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
        //int unknownMultiplier = 150; // seriously, what is that?
        int tileScaleFactor = 100; // seriously, what is that?
        if (windowHeight > windowWidth) {
            tileScaleFactor = windowHeight / 9;
        } else {
            tileScaleFactor = windowWidth / 16;
        }
        int tileWidth = tileScaleFactor; //  (int)Math.round(unknownMultiplier * aspect);
        int tileHeight = tileScaleFactor; // (int)Math.round(unknownMultiplier * aspect);

        int coordWidth = tileWidth / 2;
        font.getData().setScale(aspect);

        ApplicationLogger logger =  Gdx.app.getApplicationLogger();

        //logger.log("rendering:", String.format("width: %s, height: %s, aspectRatio:%s, density: %s, dpiX: %s|%s, dpiY: %s|%s, GWidth: %s, GHeight: %s",
        //        windowWidth, windowHeight, aspect, Gdx.graphics.getDensity(), Gdx.graphics.getPpiX(), Gdx.graphics.getPpcX(), Gdx.graphics.getPpiY(), Gdx.graphics.getPpcY(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        font.setColor(Color.WHITE);
        for(int y = 0; y < world.height; y++) {
            xArray = world.terrainTiles[y];
            for(int x = 0; x < world.width; x++) {
                tile = tileMap.get(xArray[x]);
                batch.draw(tile, x * tileWidth, y * tileHeight, tileWidth, tileHeight) ;
                if(gui.isDebugAll()) {
                    logger.log("rendering:", String.format("(%s,%s) with tile %s (managed=%s)", x, y, tileMap.get(xArray[x]), tile.isManaged()));
                    font.draw(batch, String.format("(%s,%s)", x, y), x * tileWidth, (y+1) * tileHeight, coordWidth, Align.bottomLeft, false);
                    //font.draw(batch, String.format("(%s,%s)", x, y), 0, 32, coordWidth, Align.bottomLeft, false);
                }
            }
        }


        for(int entityIndex = 0; entityIndex < world.entities.size(); entityIndex++) {
            Entity entity = world.entities.get(entityIndex);
            if (entity.hasComponents(entityRenderComponents)) {
                SpriteComponent spriteComponent = (SpriteComponent)entity.getComponent(SpriteComponent.class);
                PositionComponent positionComponent = (PositionComponent)entity.getComponent(PositionComponent.class);
                CollisionBox sizeComponent = (CollisionBox)entity.getComponent(CollisionBox.class);

                batch.draw(spriteComponent.getSprite(), positionComponent.getX_pos() * tileWidth, positionComponent.getY_pos() * tileHeight, sizeComponent.getWidth() * tileWidth,   sizeComponent.getHeight() * tileHeight) ;
                if(gui.isDebugAll()) {
                    font.setColor(Color.GOLD);
                    font.draw(batch, String.format("(%s,%s)", positionComponent.getX_pos(), positionComponent.getY_pos()), positionComponent.getX_pos() * tileWidth, (positionComponent.getY_pos()+1) * tileHeight, coordWidth, Align.bottomLeft, false);
                }
            }
        }
    }

    public void update(float deltaTime){
        super.update(deltaTime);

        collisionDetectionSystem.execute(deltaTime, collisionDetectionSystem.onlyQualified(world.entities));
        playerInputSystem.execute(deltaTime, playerInputSystem.onlyQualified(world.entities));
        movementSystem.execute(deltaTime, movementSystem.onlyQualified(world.entities));
    }

    @Override
    public void afterRender(int windowWidth, int windowHeight, float aspect) {
        this.gui.draw();
    }

    public void drawWorld(SpriteBatch batch, int windowWidth, int windowHeight, float aspect) {
       // batch.draw(background_image, 0,0, 0,0, windowWidth, windowHeight);
    }

}
