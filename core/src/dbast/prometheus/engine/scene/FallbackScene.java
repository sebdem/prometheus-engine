package dbast.prometheus.engine.scene;

public class FallbackScene extends AbstractScene {

    public static final AbstractScene DEFAULT = new FallbackScene("FALLBACK").create();

    public FallbackScene(String key) {
        super(key);
    }
}
