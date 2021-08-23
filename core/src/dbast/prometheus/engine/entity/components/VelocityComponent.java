package dbast.prometheus.engine.entity.components;

public class VelocityComponent extends Component {
    float velocity_x;
    float velocity_y;

    public VelocityComponent(float velocity_x, float velocity_y) {
        this.velocity_x = velocity_x;
        this.velocity_y = velocity_y;
    }

    public float getVelocity_x() {
        return velocity_x;
    }

    public void setVelocity_x(float velocity_x) {
        this.velocity_x = velocity_x;
    }

    public float getVelocity_y() {
        return velocity_y;
    }

    public void setVelocity_y(float velocity_y) {
        this.velocity_y = velocity_y;
    }

}
