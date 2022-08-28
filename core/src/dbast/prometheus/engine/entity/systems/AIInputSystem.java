package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.GenerationUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class AIInputSystem extends ComponentSystem{

    public WorldSpace worldSpace;

    public AIInputSystem(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
    }

    @Override
    public void execute(float updateDelta, List<Entity> qualifiedEntities) {
        for(Entity entity : qualifiedEntities) {
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            VelocityComponent velocityComponent = entity.getComponent(VelocityComponent.class);
            TargetTraverseComponent targetTraverseComponent = entity.getComponent(TargetTraverseComponent.class);

            Vector3 currentPosition = positionComponent.position;
            if (targetTraverseComponent.finalTarget == null) {
                if ((Math.random() * 100) / updateDelta > 98 / updateDelta) {

                    Gdx.app.getApplicationLogger().log("AISystem", String.format("1. Getting new target for entity %s at %s", entity.getId(), currentPosition.toString()));
                    Vector3 targetPosition = worldSpace.getSpawnPoint();

                    calculateNewPath(entity, positionComponent, targetTraverseComponent, currentPosition, targetPosition);
                    Gdx.app.getApplicationLogger().log("AISystem", String.format("3. Found target %s for entity %s", targetTraverseComponent.finalTarget.toString(), entity.getId()));
                }
            }
            if (targetTraverseComponent.finalTarget != null) {
                float distanceX = (targetTraverseComponent.currentTarget.x - currentPosition.x );
                float distanceY = (targetTraverseComponent.currentTarget.y - currentPosition.y);
                float distanceZ = (targetTraverseComponent.currentTarget.z - currentPosition.z);
                //Vector3 pathDistance = targetTraverseComponent.currentTarget.cpy().sub(targetTraverseComponent.previousTarget)
                //Vector3 currentDistance = targetTraverseComponent.currentTarget.cpy().sub(currentPosition)

                Gdx.app.getApplicationLogger().log("AISystem", String.format("4.Distance from %s to target %s for entity %s ---- X: %s, Y: %s, Z: %s", currentPosition.toString(), targetTraverseComponent.currentTarget.toString(), entity.getId(), distanceX, distanceY, distanceZ));

                // if position was reached
                float velocityX = 0f, velocityY = 0f, velocityZ = 0f;

                if (distanceX == 0 && distanceY == 0f && distanceZ == 0) {
                    targetTraverseComponent.nextTarget();
                    if (targetTraverseComponent.currentTarget != null) {
                        Gdx.app.getApplicationLogger().log("AISystem", String.format("6. Next target step %s for entity %s", targetTraverseComponent.currentTarget.toString(), entity.getId()));
                    }

                    if (currentPosition.equals(targetTraverseComponent.finalTarget)) {
                        targetTraverseComponent.reachedTarget();
                    }
                } else {
                    float baseSpeed = 0.125f;
                    velocityX = (targetTraverseComponent.currentTarget.x - targetTraverseComponent.previousTarget.x) * baseSpeed;
                    velocityY = (targetTraverseComponent.currentTarget.y - targetTraverseComponent.previousTarget.y) * baseSpeed;
                    velocityZ = (targetTraverseComponent.currentTarget.z - targetTraverseComponent.previousTarget.z) * baseSpeed;

                    velocityX /= updateDelta;
                    velocityY /= updateDelta;
                    velocityZ /= updateDelta;

                    Gdx.app.getApplicationLogger().log("AISystem", String.format("5. Distance from previous %s to target %s for entity %s ---- X: %s, Y: %s, Z: %s", targetTraverseComponent.previousTarget.toString(), targetTraverseComponent.currentTarget.toString(), entity.getId(), velocityX, velocityY, velocityZ));

                   // velocityZ = 0f;
                }
                velocityComponent.setVelocity_x(velocityX);
                velocityComponent.setVelocity_y(velocityY);
                velocityComponent.setVelocity_z(velocityZ);
                Gdx.app.getApplicationLogger().log("AISystem", String.format("Velocity for entity %s is %s/%s/%s", entity.getId(), velocityX, velocityY, velocityZ));
            }
        }
    }

    public void calculateNewPath(Entity entity, PositionComponent position,
                                 TargetTraverseComponent targetTraverse,
                                 Vector3 start,
                                 Vector3 end) {
        List<Vector3> path = GenerationUtils.find3DPath(start, end,
                // TODO path validation
              //  (vector3)->worldSpace.isValidPosition(vector3)
                (vector3)->Boolean.TRUE
        );
        if (path.size() > 0) {
            Gdx.app.getApplicationLogger().log("AISystem", "found Path with " + path.size()+
                    " steps: " + String.join(
                            "->",
                        path.stream().map(Vector3::toString).toArray(String[]::new)
                    )
            );
            targetTraverse.path = path;
            // initial node is always start target
            targetTraverse.nextTarget();
            targetTraverse.finalTarget = path.get(path.size() - 1);
        }
    }


    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class, TargetTraverseComponent.class);
    }
}
