package dbast.prometheus.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.base.PositionProvider;
import dbast.prometheus.engine.config.PrometheusConfig;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.PositionComponent;

import javax.xml.ws.Provider;

public class LockOnCamera extends PerspectiveCamera implements PositionProvider {

    protected Entity lockOnEntity;
    protected PositionComponent lockOnComponent;
    protected Vector3 cameraOffset;
    private boolean recalcOffset = false;

    protected Vector3 entityOffset;
    protected double viewingAngle = 90f;
    protected float cameraDistance = 6f;

    public LockOnCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);
        //isoTransform.scl(Gdx.graphics.getWidth() / this.viewportWidth, Gdx.graphics.getHeight() / this.viewportHeight, this.cameraDistance);
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
        entity.executeFor(PositionComponent.class, targetComponent -> {
            this.lockOnEntity = entity;
            this.lockOnComponent = targetComponent;
            this.recalcOffset = true;
        });
        return this.lockOnEntity != null;
    }

    public Entity getLockOnEntity() {
        return this.lockOnEntity;
    }

    protected static float gridSnapIncrement = (Float)PrometheusConfig.conf.getOrDefault("gridSnapIncrement", 0.0625f);
    protected static boolean useIsometric = (Boolean) PrometheusConfig.conf.getOrDefault("isometric", false);

    // This doesn't seem to work properly...
    // EDIT: [1] and [4] were reversed???
    public static Matrix4 isoTransform = new Matrix4(
            new float[]{
                    // EDIT:
                    0.5f, 0.25f , 0, 0,
                    -0.5f, 0.25f, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1
            });

    public static Matrix4 screenToGridTransform = new Matrix4(
            new float[]{
                    // EDIT:
                    0.5f, -0.5f, 0, 0,
                    0.25f , 0.25f, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1
            });
    @Override
    public Vector3 unproject(Vector3 screenCoords) {
        return super.unproject(screenCoords);
        // TODO none of this works, dude you suck...
        /*
        Vector3 unprojected = super.unproject(screenCoords);
        Gdx.app.getApplicationLogger().log("camera", String.format("unprojected %s", unprojected));
        if (useIsometric) {
            float unmodifiedX = unprojected.x;
            float unmodifiedY = unprojected.y;
            float baseSpriteWidth = 32f;

            float i_x = 1f;
            float i_y = 0.5f;
            float j_x = -1f;
            float j_y = 0.5f;

            BigDecimal a = new BigDecimal(i_x * 0.5f * baseSpriteWidth); // 16f
            BigDecimal b = new BigDecimal(j_x * 0.5f * baseSpriteWidth); // -16f
            BigDecimal c = new BigDecimal(i_y * 0.5f * baseSpriteWidth); // 8f
            BigDecimal d = new BigDecimal(j_y * 0.5f * baseSpriteWidth); // 8f

            BigDecimal detVal = a.multiply(d).subtract(b.multiply(c));
            BigDecimal det = new BigDecimal(1).divide(detVal);
            Gdx.app.getApplicationLogger().log("camera", String.format("a:%s, b:%s, c:%s, d:%s  ||| Actual Determinate %s : Calculation Det %s ", a,b,c,d,detVal,det));

            double focusX = (unmodifiedX * (det.multiply(d)).doubleValue()
                            - (unmodifiedY * det.multiply(b).doubleValue()));
            double focusY = - (unmodifiedX * (det.multiply(c)).doubleValue())
                            + unmodifiedY * (det.multiply(a)).doubleValue();

            if (Double.isNaN(focusX)) {
                Gdx.app.getApplicationLogger().log("camera", "unprojection failed: focusX is NaN with " + String.format("a:%s, b:%s, c:%s, d:%s", a, b, c, d));
                Gdx.app.getApplicationLogger().log("camera", "unprojection failed: focusX is NaN with " + String.format("unmodifiedX:%s, det:%s, unmodifiedY:%s", unmodifiedX, det, unmodifiedY));
            }
            unprojected.x = (float)focusX;
            unprojected.y = (float)focusY;
        }
        return unprojected;*/
    }

    @Override
    public void update() {
        int largestFactor = 16;
        int height =  Gdx.graphics.getHeight();
        int width =  Gdx.graphics.getWidth();
        float ratio = ((float)Math.min(height, width) / (float)Math.max(height, width));
        if (width >= height) {
            this.viewportHeight = largestFactor * ratio;
            this.viewportWidth = largestFactor;
        } else {
            this.viewportWidth = largestFactor * ratio;
            this.viewportHeight = largestFactor;
        }

        if (lockOnEntity != null) {
            if (this.recalcOffset) {
                this.recalcOffset = false;
                //if (!useIsometric) {
                    this.cameraOffset = new Vector3(
                            entityOffset.x,
                            entityOffset.y -(float)(Math.cos(viewingAngle) * cameraDistance),
                            (useIsometric ? 0.5f : (float)Math.sin(viewingAngle)) * cameraDistance);
                //} else {
                //    this.cameraOffset = new Vector3(entityOffset.x, entityOffset.y, entityOffset.z);
                //}
            }
            Vector3 targetPosition = lockOnComponent.position.cpy();

            if ((Boolean) PrometheusConfig.conf.getOrDefault("gridSnapping", false)) {
                double xPos = Math.round(targetPosition.x);
                double yPos = Math.round(targetPosition.y);

                targetPosition.set(
                        (float)(xPos + (Math.round((targetPosition.x - xPos) / gridSnapIncrement)) * gridSnapIncrement),
                        (float)(yPos + (Math.round((targetPosition.y - yPos) / gridSnapIncrement)) * gridSnapIncrement),
                        targetPosition.z
                );
            }

            if (useIsometric) {
                targetPosition.set(
                        (float) (targetPosition.x * 0.5- targetPosition.y * 0.5),
                        (float) (targetPosition.x * 0.25+ targetPosition.y * 0.25) + targetPosition.z * 0.5f,
                        1f//targetPosition.z * 0.5f
                );
            }
            Vector3 newPosition = targetPosition.cpy().add(cameraOffset);
            Vector3 newLookAt = targetPosition.cpy().add(entityOffset);
            //Gdx.app.getApplicationLogger().log("camera update", String.format("Updating camera... Current Position: %s | Target Position: %s (with offset: %s) | Looking At %s", this.position, targetPosition, newPosition, newLookAt));
            this.position.set(newPosition);
            this.lookAt(newLookAt);
        }
        super.update();
    }

    @Override
    public Vector3 getPosition() {
        return this.position;
    }
}
