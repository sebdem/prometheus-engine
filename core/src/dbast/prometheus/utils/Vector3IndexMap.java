package dbast.prometheus.utils;

import com.badlogic.gdx.math.Vector3;

import java.util.*;
import java.util.stream.Collectors;

public class  Vector3IndexMap<T> extends TreeMap<Vector3, T> {

    public Vector3IndexMap(Comparator<Vector3> comparator) {
        super(comparator);
    }

    public List<T> getMultiple(List<Vector3> keys) {
        return this.entrySet().stream().filter(entry->  keys.contains(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
    }
}
