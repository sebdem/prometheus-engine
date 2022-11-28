package dbast.prometheus.engine.world;


import com.badlogic.gdx.math.Vector3;

public enum Direction {
    NORTH(new Vector3(0, 1, 0)),
    EAST(new Vector3(1, 0, 0)),
    SOUTH(new Vector3(0, -1, 0)),
    WEST(new Vector3(-1, 0, 0));

    public Vector3 dir;
    Direction(Vector3 dir) {
        this.dir = dir;
    }
}
