package dbast.prometheus.engine.graphics;

public enum SpriteType {
    DEFAULT(0f),
    TILE(0f),
    ENTITY(1f),
    OVERSIZE(0f);

    public float priorityOffset = 0f;

    private SpriteType(float priorityOffset) {
        this.priorityOffset = priorityOffset;
    }
}
