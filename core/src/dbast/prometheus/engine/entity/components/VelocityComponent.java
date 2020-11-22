package dbast.prometheus.engine.entity.components;

public class VelocityComponent extends Component {
    float velocity_x;
    float velocity_y;

    public VelocityComponent(float velocity_x, float velocity_y) {
        this.velocity_x = velocity_x;
        this.velocity_y = velocity_y;
    }

}
