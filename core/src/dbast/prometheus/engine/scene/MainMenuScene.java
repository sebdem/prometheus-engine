package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class MainMenuScene extends AbstractScene {

    public List<String> navItems = new ArrayList<String>();

    @Override
    public void create() {
        navItems.add("Continue");
        navItems.add("New Game");
        navItems.add("Load Save");
        navItems.add("Multiplayer");
        navItems.add("Settings");
        navItems.add("Close");

        this.background = Color.valueOf("#1a1a1a");

        this.font = new BitmapFont();
        this.font.setColor(Color.valueOf("#CECECE"));
    }

    @Override
    public void render(SpriteBatch batch, int windowWidth, int windowHeight, float aspect){
        super.render(batch, windowWidth, windowHeight, aspect);

        int startY = windowHeight;
        for(String item : navItems) {
            startY -=40;
            font.draw(batch, item, 32, startY);
        }
    }
    @Override
    public void dispose() {

    }
}
