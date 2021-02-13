package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DefaultScene extends AbstractScene {

    protected Texture background_image;

    @Override
    public void create() {

        background_image = new Texture(Gdx.files.internal("ui/background.png"));
        background_image.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        font = new BitmapFont();
        font.setColor(Color.RED);
    }

    @Override
    public void render(int windowWidth, int windowHeight, float aspect){
        super.render(windowWidth, windowHeight, aspect);
        //batch.draw(background_image, 0,0, 0,0, windowWidth, windowHeight);

        // font.draw(this.uiStage.getBatch(), "" + System.currentTimeMillis() + ", " + Gdx.graphics.getFramesPerSecond() + "FPS", tickX, tickY);
    }

    @Override
    public void update(int windowWidth, int windowHeight) {

    }

    @Override
    public void dispose() {
        background_image.dispose();
        font.dispose();
    }
}
