package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DefaultScene extends AbstractScene {

    protected Texture background_image;

    // bullshit
    protected int tickX = 0, tickY;
    protected boolean growX = true, growY = true;

    @Override
    public void create() {
        background_image = new Texture(Gdx.files.internal("ui/background.png"));
        background_image.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        font = new BitmapFont();
        font.setColor(Color.RED);
    }

    @Override
    public void render(SpriteBatch batch, int windowWidth, int windowHeight, float aspect){
        super.render(batch, windowWidth, windowHeight, aspect);
        batch.draw(background_image, 0,0, 0,0, windowWidth, windowHeight);

        font.draw(batch, "" + System.currentTimeMillis() + ", " + Gdx.graphics.getFramesPerSecond() + "FPS", tickX, tickY);
    }

    @Override
    public void update(int windowWidth, int windowHeight) {
        if (growX) {
            tickX++;
            growX = tickX < windowWidth;
        } else {
            tickX--;
            growX = tickX == 0;
        }
        if (growY) {
            tickY++;
            growY = tickY < windowHeight;
        } else {
            tickY--;
            growY = tickY == 0;
        }
    }

    @Override
    public void dispose() {
        background_image.dispose();
        font.dispose();
    }
}
