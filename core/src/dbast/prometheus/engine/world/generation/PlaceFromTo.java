package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.world.WorldSpace;

import java.util.List;

public abstract class PlaceFromTo implements PlaceFeature {
    public float featureStep = 1f;

    public void place(WorldSpace world, float x1, float y1, float z1, float x2, float y2, float z2) {
        Vector3 startPoint = new Vector3(x1, y1, z1);
        Vector3 endPoint = new Vector3(x2, y2, z2);
        List<Vector3> steps = GenerationUtils.findPath(startPoint, endPoint, featureStep);
        for (Vector3 step : steps) {
            place(world, step.x, step.y, step.y);
        }
    }
}