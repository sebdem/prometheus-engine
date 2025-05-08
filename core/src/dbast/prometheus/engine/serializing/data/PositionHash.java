package dbast.prometheus.engine.serializing.data;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.base.PositionProvider;
import dbast.prometheus.utils.GeneralUtils;

import java.util.Arrays;

public class PositionHash implements PositionProvider {
    public float[] position;
    public String chunkHash;

    public PositionHash() {
    }

    public PositionHash(PositionProvider positionProvider) {
        this(positionProvider.getPosition());
    }

    public PositionHash(Vector3 position) {
        this();
        this.position = GeneralUtils.vectorToFloat(position);
        this.chunkHash = GeneralUtils.floatToString(this.position, true);
    }

    @Override
    public String toString() {
        return String.format("Position[%s]", getPosition().toString());
    }

    @Override
    public int hashCode() {
        return chunkHash.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PositionHash) {
            return Arrays.equals(this.position, ((PositionHash)obj).position);
        }
        return super.equals(obj);
    }

    @Override
    public Vector3 getPosition() {
        return GeneralUtils.floatToVector(this.position);
    }
}
