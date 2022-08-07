package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Tile {
    public final static Tile MISSING_TEXTURE = new Tile("missing", new Texture(Gdx.files.internal("missing.png")));

    public Texture tileTexture;
    public String tag;

    public Tile(String tag, Texture tileTexture) {
        this.tileTexture = tileTexture;
        this.tag = tag;
    }



}
