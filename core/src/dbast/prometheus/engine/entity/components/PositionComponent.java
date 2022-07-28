package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.math.Vector3;

public class PositionComponent extends Component {

    public Vector3 position;

    public PositionComponent(Vector3 position) {
        this.position = position;
    }

    public PositionComponent(float x, float y, float z) {
        this.position = new Vector3(x,y,z);
    }

    @Deprecated
    public PositionComponent(float x_pos, float y_pos) {
        this(x_pos, y_pos, 0f);
    }

    public static PositionComponent initial() {
        return new PositionComponent(0f,0f, 0f);
    }


    public float getX() {
        return this.position.x;
    }

    public void setX(float x_pos) {
        this.position.x = x_pos;
    }

    public float getY() {
        return this.position.y;
    }
    public void setY(float y_pos) {
        this.position.y = y_pos;
    }


    public float getZ() {
        return this.position.z;
    }
    public void setZ(float z_pos) {
        this.position.z = z_pos;
    }

    public boolean isNearby(PositionComponent other, float maxDistance) {
        return Math.abs(other.getX() - getX()) <= maxDistance && Math.abs(other.getY() - getY()) <= maxDistance;
        /*
        return (other.x_pos > this.x_pos - maxDistance && other.x_pos < this.x_pos + maxDistance)
                && (other.y_pos > this.y_pos - maxDistance && other.y_pos < this.y_pos + maxDistance);*/

    }
    public boolean isNearby(float otherXPos, float otherYPos, float maxDistance) {
        return Math.abs(otherXPos - getX()) <= maxDistance && Math.abs(otherYPos - getY()) <= maxDistance;
        /*
        return (other.x_pos > this.x_pos - maxDistance && other.x_pos < this.x_pos + maxDistance)
                && (other.y_pos > this.y_pos - maxDistance && other.y_pos < this.y_pos + maxDistance);*/

    }
}
