package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

public class SpriteRenderData {
    public TextureRegion diffuse;
    public TextureRegion normal;
    // TODO other stuff i'll probably add and regret later

    // render this thing as if it were somewhere else, without changing it's actual position.
    public Vector3 renderIndexOffset;

    public SpriteRenderData(TextureRegion diffuse, TextureRegion normal, Vector3 renderIndexOffset) {
        this.diffuse = diffuse;
        this.normal = normal;
        this.renderIndexOffset = (renderIndexOffset != null) ? renderIndexOffset : Vector3.Zero;
    }
    public SpriteRenderData(TextureRegion diffuse, TextureRegion normal) {
        this(diffuse, normal, null);
    }
}
