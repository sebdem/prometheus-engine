package dbast.prometheus.engine.entity.components;

public class PositionComponent extends Component {

    private float x_pos;

    private float y_pos;


    public PositionComponent(float x_pos, float y_pos) {
        this.x_pos = x_pos;
        this.y_pos = y_pos;
    }
}
