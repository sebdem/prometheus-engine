package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class VelocityComponent extends Component {
    Vector3 velocity;

    public VelocityComponent(float velocity_x, float velocity_y) {
        this(velocity_x, velocity_y, 0f);
    }

    public VelocityComponent(float velocity_x, float velocity_y, float velocity_z) {
        super();
        this.velocity = new Vector3(velocity_x, velocity_y, velocity_z);
    }

    public float getVelocity_x() {
        return velocity.x;
    }

    public void setVelocity_x(float velocity_x) {
        this.velocity.x = velocity_x;
    }

    public float getVelocity_y() {
        return velocity.y;
    }

    public void setVelocity_y(float velocity_y) {
        this.velocity.y = velocity_y;
    }

    public float getVelocity_z() {
        return velocity.z;
    }

    public void setVelocity_z(float velocity_z) {
        this.velocity.z = velocity_z;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public boolean isMoving() {
        return velocity.len() > 0;
    }

    // deprecated since it's unusable right now (not compatible with AIInputSystem
    @Deprecated
    public void normalize() {
        if (velocity.x != 0 && velocity.y != 0) {
            //float total = velocity.x + velocity.y;
          //  double divBy =  Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y);
            velocity.x /= 1.41421356237;
            velocity.y /= 1.41421356237;
        }
    }
}
