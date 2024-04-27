package dbast.prometheus.engine.serializing.data;

public class RenderComponentData {
    public String path;
    public String normal;

    public Float[] offset = new Float[]{0.0f,0.0f,0.0f};

    public Boolean animated = false;
    public Boolean loop = true;
    public Integer columns = 1;
    public Integer rows = 1;
    public Float frameDuration = 1f;
}
