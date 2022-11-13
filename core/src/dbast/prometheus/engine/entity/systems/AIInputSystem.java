package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.GenerationUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIInputSystem extends ComponentSystem{

    public WorldSpace worldSpace;

    public Set<Entity> pathCalcInProcess = new HashSet<>();
    protected ExecutorService executor;

    public AIInputSystem(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(float updateDelta, List<Entity> qualifiedEntities) {
        for(Entity entity : qualifiedEntities) {
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            VelocityComponent velocityComponent = entity.getComponent(VelocityComponent.class);
            TargetTraverseComponent targetTraverseComponent = entity.getComponent(TargetTraverseComponent.class);

            Vector3 currentPosition = positionComponent.position;
            if (targetTraverseComponent.finalTarget == null && !pathCalcInProcess.contains(entity)) {
                if (Math.random() * (100 / updateDelta) > 90 / updateDelta) {
                    pathCalcInProcess.add(entity);
                    final Entity currentEntity = entity;
                    Vector3 targetPosition = worldSpace.getRandomInRangeOf(currentEntity, currentPosition, 20);

                    if (!targetPosition.equals(currentPosition)) {
                        CompletableFuture.runAsync(()-> {
                            calculateNewPath(currentEntity, positionComponent, targetTraverseComponent, currentPosition, targetPosition);
                            pathCalcInProcess.remove(currentEntity);
                        }, executor);
                    }
                }
            }
            if (targetTraverseComponent.finalTarget != null) {
                float distanceX = (targetTraverseComponent.currentTarget.x - currentPosition.x );
                float distanceY = (targetTraverseComponent.currentTarget.y - currentPosition.y);
                float distanceZ = (targetTraverseComponent.currentTarget.z - currentPosition.z);
                //Vector3 pathDistance = targetTraverseComponent.currentTarget.cpy().sub(targetTraverseComponent.previousTarget)
                //Vector3 currentDistance = targetTraverseComponent.currentTarget.cpy().sub(currentPosition)

               // Gdx.app.getApplicationLogger().log("AISystem", String.format("4.Distance from %s to target %s for entity %s ---- X: %s, Y: %s, Z: %s", currentPosition.toString(), targetTraverseComponent.currentTarget.toString(), entity.getId(), distanceX, distanceY, distanceZ));

                // if position was reached
                float velocityX = 0f, velocityY = 0f, velocityZ = 0f;

                if (distanceX == 0 && distanceY == 0f && distanceZ == 0) {
                    targetTraverseComponent.nextTarget();
                  /*  if (targetTraverseComponent.currentTarget != null) {
                        Gdx.app.getApplicationLogger().log("AISystem", String.format("6. Next target step %s for entity %s", targetTraverseComponent.currentTarget.toString(), entity.getId()));
                    }*/

                    if (currentPosition.equals(targetTraverseComponent.finalTarget)) {
                        targetTraverseComponent.reachedTarget();
                    }
                } else {
                    float baseSpeed = 0.125f;
                    velocityX = (targetTraverseComponent.currentTarget.x - targetTraverseComponent.previousTarget.x) * baseSpeed;
                    velocityY = (targetTraverseComponent.currentTarget.y - targetTraverseComponent.previousTarget.y) * baseSpeed;
                    velocityZ = (targetTraverseComponent.currentTarget.z - targetTraverseComponent.previousTarget.z) * baseSpeed;

                    // TODO find a way to "normalize" directional speed

                    velocityX /= updateDelta;
                    velocityY /= updateDelta;
                    velocityZ /= updateDelta;

                   // Gdx.app.getApplicationLogger().log("AISystem", String.format("5. Distance from previous %s to target %s for entity %s ---- X: %s, Y: %s, Z: %s", targetTraverseComponent.previousTarget.toString(), targetTraverseComponent.currentTarget.toString(), entity.getId(), velocityX, velocityY, velocityZ));
                }
                velocityComponent.setVelocity_x(velocityX);
                velocityComponent.setVelocity_y(velocityY);
                velocityComponent.setVelocity_z(velocityZ);
                //Gdx.app.getApplicationLogger().log("AISystem", String.format("Velocity for entity %s is %s/%s/%s", entity.getId(), velocityX, velocityY, velocityZ));
            }
        }
    }

    public void calculateNewPath(Entity entity, PositionComponent position,
                                 TargetTraverseComponent targetTraverse,
                                 Vector3 start,
                                 Vector3 end) {
        List<Vector3> path = GenerationUtils.find3DPath(start, end,
                (vector3)->worldSpace.isPositionInWorld(vector3),
                (vector3)->worldSpace.canStandIn(vector3)
        );
        if (path.size() > 1) {
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
           // Gdx.app.getApplicationLogger().log("AISystem", String.format("3. Found target %s for entity %s", targetTraverse.finalTarget.toString(), entity.getId()));
        }
    }


    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class, TargetTraverseComponent.class);
    }
}
