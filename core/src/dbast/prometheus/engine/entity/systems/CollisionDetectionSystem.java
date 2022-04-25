package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.CollisionBox;
import dbast.prometheus.engine.entity.components.Component;
import dbast.prometheus.engine.entity.components.PositionComponent;
import dbast.prometheus.engine.entity.components.StateComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionDetectionSystem extends ComponentSystem {

    private static boolean LOG_COLLISIONS = false;

    @Override
    public void execute(float updateDelta, List<Entity> entities) {
        Map<Long, Rectangle> entityHitboxes = new HashMap<>();

        for(Entity entityA : entities) {
            PositionComponent positionA = entityA.getComponent(PositionComponent.class);
            CollisionBox collisionA = entityA.getComponent(CollisionBox.class);

            Rectangle hitboxA = new Rectangle(positionA.getX_pos(), positionA.getY_pos(), collisionA.getWidth(), collisionA.getHeight());
            entityHitboxes.put(entityA.getId(), hitboxA);

            boolean isColliding = false;
            for(Entity entityB: entities) {
                if (!entityA.getId().equals(entityB.getId())) {
                    PositionComponent positionB = entityB.getComponent(PositionComponent.class);
                    CollisionBox collisionB = entityB.getComponent(CollisionBox.class);

                    Rectangle hitboxB = entityHitboxes.get(entityB.getId());
                    if (hitboxB == null) {
                        hitboxB = new Rectangle(positionB.getX_pos(), positionB.getY_pos(), collisionB.getWidth(), collisionB.getHeight());
                        entityHitboxes.put(entityB.getId(), hitboxB);
                    }

                    if (hitboxA.overlaps(hitboxB)) {
                        if (LOG_COLLISIONS) {
                            Gdx.app.getApplicationLogger().log("Collision System:", String.format("Entity %s collides with Entity %s", entityA.getId(),entityB.getId()));
                        }
                        isColliding = true;
                    }
                }
            }
            collisionA.setColliding(isColliding);
            StateComponent entityState = entityA.getComponent(StateComponent.class);
            if (entityState != null) {
              //  Gdx.app.getApplicationLogger().log("Collision System:", String.format("update for entity %s state; new state is%s colliding", entityA.getId(), isColliding ? "" : " not"));
                if (isColliding) {
                    entityState.setState("colliding");
                } else {
                    entityState.setState("");
                }
            }
        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(CollisionBox.class, PositionComponent.class);
    }
}
