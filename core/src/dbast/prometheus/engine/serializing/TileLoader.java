package dbast.prometheus.engine.serializing;

import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import dbast.prometheus.engine.serializing.data.RenderComponentMap;
import dbast.prometheus.engine.world.tile.Tile;

public class TileLoader extends AbstractLoader<Tile> {

    public String name;
    public String tag;

    public RenderComponentMap renderData;

    public Tile build() {
        Tile tile = new Tile();
        tile.tag = tag;
        tile.renderComponent = this.renderData.build();
        return tile;
    }

    public static TileLoader fromJson(FileHandle fileHandle) {
        return new Gson().fromJson(fileHandle.reader(), TileLoader.class);
    }
}
