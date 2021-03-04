package dbast.prometheus.engine.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DefaultScene extends AbstractScene {

    protected Texture background_image;

    protected SpriteBatch batch;

    public DefaultScene(String key) {
        super(key);
    }

    @Override
    public DefaultScene create() {
        super.create();
        batch = new SpriteBatch();

        background_image = new Texture(Gdx.files.internal("ui/background.png"));
        background_image.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        return this;
    }

    @Override
    public void mainRender(int windowWidth, int windowHeight, float aspect){
        batch.begin();
        drawBatch(batch, windowWidth, windowHeight, aspect);
        batch.end();

        super.mainRender(windowWidth, windowHeight, aspect);
    }

    public void drawBatch(SpriteBatch batch, int windowWidth, int windowHeight, float aspect) {
        batch.draw(background_image, 0,0, 0,0, windowWidth, windowHeight);
    }

    @Override
    public void dispose() {
        batch.dispose();
        background_image.dispose();
    }
}
