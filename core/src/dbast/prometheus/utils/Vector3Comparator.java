package dbast.prometheus.utils;

import com.badlogic.gdx.math.Vector3;

import java.util.Comparator;

// make comparator relative to another vector
public class Vector3Comparator implements Comparator<Vector3> {

    @Override
    public int compare(Vector3 o1, Vector3 o2) {
        // ignore x for now
        int zComp = Float.compare(o1.z, o2.z);
        int yComp = Float.compare(o2.y, o1.y);
        int xComp = Float.compare(o1.x, o2.x);
        return (zComp == 0) ?  (yComp == 0) ?  xComp  : yComp : zComp;
    }
}
