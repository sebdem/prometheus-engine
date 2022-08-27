package dbast.prometheus.utils;

import com.badlogic.gdx.math.Vector3;

import java.util.Comparator;
import java.util.TreeMap;

public class  Vector3IndexMap<T> extends TreeMap<Vector3, T> {

    public Vector3IndexMap(Comparator<Vector3> comparator) {
        super(comparator);
    }
}
