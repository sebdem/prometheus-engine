package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import dbast.prometheus.engine.entity.components.RenderComponent;

import java.util.Optional;

public class Tile {
    public final static Tile MISSING_TEXTURE = new Tile("missing", Gdx.files.internal("missing.png"));

    public RenderComponent renderComponent;
    public String tag;
    public Float height = 1f;
    public Vector3 bounds = new Vector3(1f,1f,1f);

    public Tile() {
    }
    public Tile(String tag, FileHandle tileTexture) {
        this(tag, tileTexture, 1, 1, 1f);
    }
    public Tile(String tag, FileHandle tileTexture, int columns, int rows, float frameDuration) {
        this();
        this.tag = tag;
        this.renderComponent = new RenderComponent()
                .registerAnimation(tileTexture, columns, rows, frameDuration, true, "default");
    }

    public BoundingBox getBoundsFor(Vector3 offset) {
        // rounding issue forces us to substract 0.01f
        BoundingBox tileBox = null;
        if (bounds.len() != 0) {
            tileBox = new BoundingBox(offset, offset.cpy().add(bounds).sub(
                    bounds.x != 0 ? 0.001f : 0,
                    bounds.y != 0 ? 0.001f : 0,
                    bounds.z != 0 ? 0.001f : 0
            ));
        }
        return tileBox;
    }
}
