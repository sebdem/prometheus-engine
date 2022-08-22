package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

// TODO Refactor into RenderComponent
@Deprecated
public class SpriteComponent extends Component {

    protected Sprite defaultSprite;

    public SpriteComponent(Sprite sprite) {
        this.defaultSprite = sprite;
    }

    public static SpriteComponent fromFile(FileHandle internal) {
        return new SpriteComponent(new Sprite(new Texture(internal)));
    }

    public static SpriteComponent fromTexture(Texture texture) {
        return new SpriteComponent(new Sprite(texture));
    }

    public Sprite getSprite() {
        return defaultSprite;
    }

    public void setSprite(Sprite sprite) {
        this.defaultSprite = sprite;
    }
}
