package dbast.prometheus.engine.entity.components;

public class PositionComponent extends Component {

    private float x_pos;

    private float y_pos;

    public PositionComponent(float x_pos, float y_pos) {
        this.x_pos = x_pos;
        this.y_pos = y_pos;
    }

    public static PositionComponent initial() {
        return new PositionComponent(0f,0f);
    }

    public float getX_pos() {
        return x_pos;
    }

    public void setX_pos(float x_pos) {
        this.x_pos = x_pos;
    }

    public float getY_pos() {
        return y_pos;
    }
    public void setY_pos(float y_pos) {
        this.y_pos = y_pos;
    }
}
