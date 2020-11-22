package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AbstractScene {

    protected BitmapFont font;
    protected Color background;

    public AbstractScene() {
        background = Color.DARK_GRAY;
    }

    public abstract void create();

    public void render(SpriteBatch spriteBatch, int windowWidth, int windowHeight, float aspect){
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
    }

    public void update(int windowWidth, int windowHeight){

    }

    public abstract void dispose();
}
