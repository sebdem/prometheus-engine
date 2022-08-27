package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.GsonBuildConfig;
import dbast.prometheus.engine.entity.components.RenderComponent;
import dbast.prometheus.engine.serializing.builder.TileBuilder;

public class Tile {
    public final static Tile MISSING_TEXTURE = new Tile("missing", Gdx.files.internal("missing.png"));

    public RenderComponent renderComponent;
    public String tag;

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
}
