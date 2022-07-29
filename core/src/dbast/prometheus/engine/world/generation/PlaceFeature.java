package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.world.WorldSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntSupplier;

public interface PlaceFeature {
    public void place(WorldSpace world, float x, float y, float z);
}