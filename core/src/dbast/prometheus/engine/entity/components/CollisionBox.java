package dbast.prometheus.engine.entity.components;

public class CollisionBox extends Component {
    private float width;
    private float height;

    private boolean permeable;

    private boolean isColliding;

    public CollisionBox(float width, float height, boolean permeable) {
        this.width = width;
        this.height = height;
        this.permeable = permeable;
    }

    public static CollisionBox createBasic() {
        return new CollisionBox(1f,1f,false);
    }


    public float getWidth() {
        return width;
    }

    public CollisionBox setWidth(float width) {
        this.width = width;
        return this;
    }

    public float getHeight() {
        return height;
    }

    public CollisionBox setHeight(float height) {
        this.height = height;
        return this;
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
}
