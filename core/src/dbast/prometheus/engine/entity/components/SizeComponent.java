package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.math.Vector3;

// TODO This can be deprecated i think
//@Deprecated
public class SizeComponent extends Component {
    protected float width;
    protected float height;

    public SizeComponent(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public static SizeComponent createBasic() {
        return new SizeComponent(1f,1f);
    }


    public float getHeight() {
        return height;
    }

    public SizeComponent setHeight(float height) {
        this.height = height;
        return this;
    }

    public float getWidth() {
        return width;
    }

    public SizeComponent setWidth(float width) {
        this.width = width;
        return this;
    }

    public Vector3 toVector3() {
        return new Vector3(width, height, 0f);
    }
}
