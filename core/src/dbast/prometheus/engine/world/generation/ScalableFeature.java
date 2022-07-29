package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.math.Vector3;

import java.util.function.Function;

public abstract class ScalableFeature implements PlaceFeature{

    protected Function<Vector3, Integer> scaleXSupplier;
    protected Function<Vector3, Integer> scaleYSupplier;

    public ScalableFeature(Function<Vector3, Integer> scaleXSupplier, Function<Vector3, Integer> scaleYSupplier) {
        this.scaleXSupplier = scaleXSupplier;
        this.scaleYSupplier = scaleYSupplier;
    }
    public ScalableFeature(Function<Vector3, Integer> scaleSupplier) {
        this(scaleSupplier, scaleSupplier);
    }
}
