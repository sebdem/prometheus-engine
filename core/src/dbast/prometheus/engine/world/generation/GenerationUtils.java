package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.math.Vector3;

public class GenerationUtils {

    public static Vector3[] nearby8Of(Vector3 center) {
        return new Vector3[]{
                center.cpy().add(1, -1, 0),
                center.cpy().add(0, -1, 0),
                center.cpy().add(-1, -1, 0),
                center.cpy().add(-1, 0, 0),
                center.cpy().add(1, 0, 0),
                center.cpy().add(1, 1, 0),
                center.cpy().add(0, 1, 0),
                center.cpy().add(-1, 1, 0)
        };
    }
    public static Vector3[] nearby4Of(Vector3 center) {
        return new Vector3[]{
                center.cpy().add(0, 1, 0),
                center.cpy().add(0, -1, 0),
                center.cpy().add(1, 0, 0),
                center.cpy().add(-1, 0, 0)

        };
    }
}
