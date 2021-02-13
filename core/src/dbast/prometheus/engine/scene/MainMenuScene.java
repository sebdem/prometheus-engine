package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.ArrayList;
import java.util.List;

public class MainMenuScene extends AbstractScene {

    public List<String> navItems = new ArrayList<String>();

    @Override
    public void create() {
        super.create();
        this.font.setColor(Color.valueOf("#EEEE70"));

        navItems.add("Continue");
        navItems.add("New Game");
        navItems.add("Load Save");
        navItems.add("Multiplayer");
        navItems.add("Settings");
        navItems.add("Close");

        Actor navItemActor;

        for(int i = 0; i < navItems.size(); i++) {
            navItemActor = new TextButton(navItems.get(i), this.uiSkinDefault);
            navItemActor.setPosition(1f, uiStage.getHeight() - (i + 1) * navItemActor.getHeight());
            this.uiStage.addActor(navItemActor);
        }

    }

    @Override
    public void render(int windowWidth, int windowHeight, float aspect){
        super.render(windowWidth, windowHeight, aspect);
    }
    @Override
    public void dispose() {

    }
}
