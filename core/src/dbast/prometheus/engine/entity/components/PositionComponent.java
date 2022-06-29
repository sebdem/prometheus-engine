package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.math.Vector3;

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

    public boolean isNearby(PositionComponent other, float maxDistance) {
        return Math.abs(other.x_pos - x_pos) <= maxDistance && Math.abs(other.y_pos - y_pos) <= maxDistance;
        /*
        return (other.x_pos > this.x_pos - maxDistance && other.x_pos < this.x_pos + maxDistance)
                && (other.y_pos > this.y_pos - maxDistance && other.y_pos < this.y_pos + maxDistance);*/

    }
    public boolean isNearby(float otherXPos, float otherYPos, float maxDistance) {
        return Math.abs(otherXPos - x_pos) <= maxDistance && Math.abs(otherYPos - y_pos) <= maxDistance;
        /*
        return (other.x_pos > this.x_pos - maxDistance && other.x_pos < this.x_pos + maxDistance)
                && (other.y_pos > this.y_pos - maxDistance && other.y_pos < this.y_pos + maxDistance);*/

    }

    public Vector3 toVector3() {
        return new Vector3(x_pos, y_pos, 0f);
    }
    public Vector3 toVector3(float z) {
        return new Vector3(x_pos, y_pos, z);
    }
}
