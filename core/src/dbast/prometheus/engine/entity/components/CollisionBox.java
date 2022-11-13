package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;

// TODO migrate to BoundingBox?
public class CollisionBox extends Component {

    private Vector3 boundaries;

    private boolean permeable;

    private boolean isColliding;

    @Deprecated
    public CollisionBox(float width, float height, boolean permeable) {
        this.boundaries = new Vector3(width, height, 1f);
        this.permeable = permeable;
    }
    public CollisionBox(Vector3 boundaries, boolean permeable) {
        this.boundaries = boundaries;
        this.permeable = permeable;
    }

    public static CollisionBox createBasic() {
        return new CollisionBox(new Vector3(0.99f,0.99f,0.99f), false);
    }


    public float getWidth() {
        return boundaries.x;
    }

    public CollisionBox setWidth(float width) {
        this.boundaries.x = width;
        return this;
    }

    public float getHeight() {
        return boundaries.y;
    }

    public CollisionBox setHeight(float height) {
        this.boundaries.y = height;
        return this;
    }
    public float getDepth() {
        return boundaries.z;
    }

    public CollisionBox setDepth(float depth) {
        this.boundaries.z = depth;
        return this;
    }

    public Vector3 getBoundaries() {
        return this.boundaries;
    }

    public boolean isPermeable() {
        return permeable;
    }

    public CollisionBox setPermeable(boolean permeable) {
        this.permeable = permeable;
        return this;
    }

    public boolean isColliding() {
        return isColliding;
    }

    public CollisionBox setColliding(boolean colliding) {
        this.isColliding = colliding;
        return this;
    }

    // TODO consider offset?
    public Vector3[] getCorners(Vector3 origin) {
        return new Vector3[]{
                origin,
                origin.cpy().add(boundaries.x, 0, 0),
                origin.cpy().add(boundaries.x, 0, boundaries.z),
                origin.cpy().add(boundaries.x, boundaries.y, 0),
                origin.cpy().add(boundaries.x, boundaries.y, boundaries.z),
                origin.cpy().add(0,  boundaries.y, 0),
                origin.cpy().add(0,  boundaries.y, boundaries.z),
                origin.cpy().add(0,  0, boundaries.z),
        };
    }

    @Deprecated
    public Vector3 getMax(Vector3 toMin) {
        return toMin.cpy().add(boundaries);
    }
}
