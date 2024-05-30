package dbast.prometheus.engine.serializing.data;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.base.PositionProvider;
import dbast.prometheus.utils.GeneralUtils;

public class PositionHash {
    public float[] position;
    public String chunkHash;

    public PositionHash() {
    }

    public PositionHash(PositionProvider positionProvider) {
        this();
        Vector3 position = positionProvider.getPosition();
        this.position = GeneralUtils.vectorToFloat(position);
        this.chunkHash = GeneralUtils.floatToString(this.position, true);
    }
}
