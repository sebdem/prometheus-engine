package dbast.prometheus.engine.serializing.builder;

import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import dbast.prometheus.engine.world.tile.Tile;

public class TileBuilder {

    public String name;
    public String tag;

    public RenderComponentMap renderData;

    public Tile build() {
        Tile tile = new Tile();
        tile.tag = tag;
        tile.renderComponent = this.renderData.build();
        return tile;
    }

    public static Tile fromJson(FileHandle fileHandle) {
        TileBuilder builder = new Gson().fromJson(fileHandle.reader(), TileBuilder.class);
        return builder.build();
    }
}
