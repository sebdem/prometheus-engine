package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.math.Vector3;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class VectorListPair <E, L extends Collection<E>> {

    private Vector3 key;

    /**
     * Gets the key for this pair.
     * @return key for this pair
     */
    public Vector3 getKey() { return key; }

    public void setKey(Vector3 key) {
        this.key = key;
    }


    /**
     * Value of this this <code>Pair</code>.
     */
    private L value;

    public L getValue() {
        return value;
    }

    public void setValue(L value) {
        this.value = value;
    }


    /**
     * Creates a new pair
     *
     * @param key   The key for this pair
     * @param value The value to use for this pair
     */
    public VectorListPair(Vector3 key, L value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof VectorListPair) {
            VectorListPair<E, L> pair = (VectorListPair<E, L>) o;
            return this.getKey().equals(pair.getKey()) || super.equals(o);
        }
        return false;
    }
}
