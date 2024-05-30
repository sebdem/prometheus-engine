package dbast.prometheus.engine.serializing.data;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.world.tile.TileData;
import dbast.prometheus.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TileContainerData {
    public Map<String, Short> tileMap;
    public Map<Short, ArrayList<float[]>> data;



    public TileContainerData() {
        this.data = new HashMap<>();
        this.tileMap = new HashMap<>();
    }

    public void fillContainerData(PositionHash positionHash, Map<Vector3, TileData> tileDataMap) {
        for (Map.Entry<Vector3, TileData> entry : tileDataMap.entrySet()) {
            short tileMapIndex = tileMap.computeIfAbsent(entry.getValue().tile.tag, k -> (short)tileMap.size());

            this.data.computeIfAbsent(tileMapIndex, k -> new ArrayList<>())
                    .add(GeneralUtils.vectorToFloat(entry.getKey().sub(GeneralUtils.floatToVector(positionHash.position))));
        }
    }

    public Map<Vector3, TileData> getContainerData() {

        // TODO
        return new HashMap<>();
    }
}
