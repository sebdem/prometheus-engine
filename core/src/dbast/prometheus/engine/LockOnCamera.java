package dbast.prometheus.engine;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.PositionComponent;
import dbast.prometheus.engine.entity.components.SizeComponent;

public class LockOnCamera extends PerspectiveCamera {

    protected Entity lockOnEntity;
    protected PositionComponent lockOnComponent;
    protected Vector3 cameraOffset;
    private boolean recalcOffset = false;

    protected Vector3 entityOffset;
    protected double viewingAngle = 90f;
    protected float cameraDistance = 6f;

    public LockOnCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);
    }

    public void setEntityOffset(Vector3 entityOffset) {
        this.entityOffset = entityOffset;
        this.recalcOffset = true;
    }

    public void setViewingAngle(double viewingAngle) {
        this.viewingAngle = viewingAngle;
        this.recalcOffset = true;
    }

    public void setCameraDistance(float cameraDistance) {
        this.cameraDistance = cameraDistance;
        this.recalcOffset = true;
    }

    public Vector3 getEntityOffset() {
        return entityOffset;
    }

    public double getViewingAngle() {
        return viewingAngle;
    }

    public float getCameraDistance() {
        return cameraDistance;
    }

    public boolean lockOnEntity(Entity entity) {
        PositionComponent targetComponent = entity.getComponent(PositionComponent.class);
        if (targetComponent != null) {
            this.lockOnEntity = entity;
            this.lockOnComponent = targetComponent;
            this.recalcOffset = true;
            return true;
        }
        return false;
    }

    protected static float gridSnapIncrement = (Float)PrometheusConfig.conf.getOrDefault("gridSnapIncrement", 0.0625f);

    @Override
    public void update() {
        if (lockOnEntity != null) {
            if (this.recalcOffset = true) {
                this.recalcOffset = false;
                this.cameraOffset = new Vector3(
                        entityOffset.x,
                        entityOffset.y -(float)(Math.cos(viewingAngle) * cameraDistance),
                        (float)Math.sin(viewingAngle) * cameraDistance);
            }
            Vector3 targetPosition = lockOnComponent.toVector3();

            if ((Boolean) PrometheusConfig.conf.getOrDefault("gridSnapping", false)) {
                double xPos = Math.round(targetPosition.x);
                double yPos = Math.round(targetPosition.y);

                targetPosition.set(
                        (float)(xPos + (Math.round((targetPosition.x - xPos) / gridSnapIncrement)) * gridSnapIncrement),
                        (float)(yPos + (Math.round((targetPosition.y - yPos) / gridSnapIncrement)) * gridSnapIncrement),
                        targetPosition.z
                );
            }

            this.position.set(targetPosition.cpy().add(cameraOffset));
            this.lookAt(targetPosition.cpy().add(entityOffset));
        }
        super.update();
    }
}
