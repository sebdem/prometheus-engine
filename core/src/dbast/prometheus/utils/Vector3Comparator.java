package dbast.prometheus.utils;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.base.PositionProvider;
import dbast.prometheus.engine.config.PrometheusConfig;

import java.util.Comparator;

// make comparator relative to another vector
public class Vector3Comparator {

    public static Comparator<Vector3> getConfigured() {
        if ((Boolean) PrometheusConfig.conf.getOrDefault("isometric", false)) {
            return new Isometric();
        } else {
            return new Planar();
        }
    }

    public static class Planar implements Comparator<Vector3> {

        @Override
        public int compare(Vector3 o1, Vector3 o2) {
            // ignore x for now
            int zComp = Float.compare(o1.z, o2.z);
            int yComp = Float.compare(o2.y, o1.y);
            int xComp = Float.compare(o1.x, o2.x);
            return (zComp == 0) ?  (yComp == 0) ?  xComp  : yComp : zComp;
        }
    }

    /**
     * Start with upper right corner of the world (max x and max y)
     * This currently cannot deal with negative values.
     */
    public static class Isometric implements Comparator<Vector3> {
        @Override
        public int compare(Vector3 o1, Vector3 o2) {
            // should result in a more accurate sorting, but likely takes up to much time... Needs performance testing
            // float xySqrt1 = (float) Math.sqrt(o1.x * o1.x + o1.y * o1.y) * -1;
            // float xySqrt2 = (float) Math.sqrt(o2.x * o2.x + o2.y * o2.y) * -1;
            byte sortingVersion = 1; // 0 = Legacy, 1 = 'nearest', 2 = use new Key...
            if (sortingVersion == 1) {
                float xySqrt1 = -(o1.x + o1.y);
                float xySqrt2 = -(o2.x + o2.y);
                int zComp = Float.compare(o1.z, o2.z);

                if (zComp != 0) {
                    return zComp;
                }

                int xyComp = Float.compare(xySqrt1, xySqrt2);
                if (xyComp != 0) {
                    return xyComp;
                } else {
                    int yComp = Float.compare(o2.y, o1.y);
                    int xComp = Float.compare(o2.x, o1.x);
                    return (xComp == 0) ? yComp : xComp;
                    // return (yComp == 0) ? xComp : yComp;
                }
            } else {
                // ignore x for now
                int zComp = Float.compare(o1.z, o2.z);
                int yComp = Float.compare(o2.y, o1.y);
                int xComp = Float.compare(o2.x, o1.x);

                if (xComp == 0 && yComp == 0) {
                    return zComp;
                }
                return yComp == 0 ? zComp == 0 ? xComp : zComp: yComp;
            }
        }
    }
/*
    *//**
     * Idea was to make a direction based comparer. Issue is the camera only has one point, when the 'real' screen plane would be a better idea... Maybe rebuild using camera frustrum???
     *//*
    public static class Relative implements Comparator<Vector3> {
        protected PositionProvider provider;
        public Relative(PositionProvider relativeTo) {
            this.provider = relativeTo;
        }

        @Override
        public int compare(Vector3 o1, Vector3 o2) {
            Vector3 relativeTo = provider.getPosition();
            float dst1 = relativeTo.dst(o1);
            float dst2 = relativeTo.dst(o2);

            return Float.compare(dst1, dst1);
        }
    }*/
}
