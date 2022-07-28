package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.CollisionBox;
import dbast.prometheus.engine.entity.components.Component;
import dbast.prometheus.engine.entity.components.PositionComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollisionDetectionSystem extends ComponentSystem {

    private static boolean LOG_COLLISIONS = false;

    @Override
    public void execute(float updateDelta, List<Entity> entities) {
        Map<Long, Rectangle> entityHitboxes = new HashMap<>();

        // TODO i believe this can be optimized for local comparing of collisions

        for(Entity entityA : entities) {
            PositionComponent positionA = entityA.getComponent(PositionComponent.class);
            CollisionBox collisionA = entityA.getComponent(CollisionBox.class);

            Rectangle hitboxA = new Rectangle(positionA.getX(), positionA.getY(), collisionA.getWidth(), collisionA.getHeight());
            entityHitboxes.put(entityA.getId(), hitboxA);

            collisionA.setColliding(false);

            entities.stream().filter(entityB ->
                !entityB.getId().equals(entityA.getId())
                && positionA.isNearby(entityB.getComponent(PositionComponent.class), 4f)
            ).forEach(entityB -> {
                PositionComponent positionB = entityB.getComponent(PositionComponent.class);
                CollisionBox collisionB = entityB.getComponent(CollisionBox.class);

                Rectangle hitboxB = entityHitboxes.get(entityB.getId());
                if (hitboxB == null) {
                    hitboxB = new Rectangle(positionB.getX(), positionB.getY(), collisionB.getWidth(), collisionB.getHeight());
                    entityHitboxes.put(entityB.getId(), hitboxB);
                }

                if (hitboxA.overlaps(hitboxB)) {
                    if (LOG_COLLISIONS) {
                        Gdx.app.getApplicationLogger().log("Collision System:", String.format("Entity %s collides with Entity %s", entityA.getId(),entityB.getId()));
                    }
                    collisionA.setColliding(true);
                }
            });
            /*
            for(Entity entityB: entities) {
                if (!entityA.getId().equals(entityB.getId())) {

                }
            }*/
            //collisionA.setColliding(isColliding);
        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(CollisionBox.class, PositionComponent.class);
    }
}
