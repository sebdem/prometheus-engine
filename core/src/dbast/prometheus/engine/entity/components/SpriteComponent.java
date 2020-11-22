package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteComponent extends Component {

    public Sprite sprite;

    public SpriteComponent(Sprite sprite) {
        this.sprite = sprite;
    }
}
