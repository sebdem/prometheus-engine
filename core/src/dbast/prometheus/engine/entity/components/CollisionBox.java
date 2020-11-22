package dbast.prometheus.engine.entity.components;

public class CollisionBox extends Component {
    private float width;
    private float height;

    private boolean permeable;

    public CollisionBox(float width, float height, boolean permeable) {
        this.width = width;
        this.height = height;
        this.permeable = permeable;
    }
}
