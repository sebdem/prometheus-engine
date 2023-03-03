package dbast.prometheus.engine.world;


import com.badlogic.gdx.math.Vector3;

public enum Direction {
    NORTH(new Vector3(0, 1, 0)),
    EAST(new Vector3(1, 0, 0)),
    SOUTH(new Vector3(0, -1, 0)),
    WEST(new Vector3(-1, 0, 0)),
    UP(new Vector3(0, 0, 1)),
    DOWN(new Vector3(0, 0, -1));

    public final Vector3 dir;
    Direction(Vector3 dir) {
        this.dir = dir;
    }
    public Direction invert() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            case UP: return DOWN;
            case DOWN: return UP;
            default: return this;
        }
    }
}
