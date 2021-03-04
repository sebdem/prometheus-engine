package dbast.prometheus.engine.scene;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import dbast.prometheus.engine.world.TilePlane;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class WorldScene extends AbstractScene{

    protected BitmapFont font;
    protected SpriteBatch batch;
    protected TilePlane world;
    private Map<Integer, Texture> tileMap = new HashMap<>();

    public WorldScene(String key) {
        super(key);
    }

    @Override
    public WorldScene create() {
        super.create();
        font = new BitmapFont();
        font.setColor(Color.valueOf("#FF0088"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        world = TilePlane.testLevel();

        batch = new SpriteBatch();
        tileMap.put(0, new Texture(Gdx.files.internal("world/terrain/water.png")));
        tileMap.put(1, new Texture(Gdx.files.internal("world/terrain/dirt.png")));
        tileMap.put(2, new Texture(Gdx.files.internal("world/terrain/grass.png")));

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
        int tileWidth = windowWidth / world.width;
        int tileHeight = windowHeight / world.height;
        int coordWidth = tileWidth / 2;
        font.getData().setScale(((float)coordWidth) / 32f);

        ApplicationLogger logger =  Gdx.app.getApplicationLogger();

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
       // int test = 2 / 0;
    }

    @Override
    public void afterRender(int windowWidth, int windowHeight, float aspect) {
        this.gui.draw();
    }

    public void drawWorld(SpriteBatch batch, int windowWidth, int windowHeight, float aspect) {
       // batch.draw(background_image, 0,0, 0,0, windowWidth, windowHeight);
    }

}
