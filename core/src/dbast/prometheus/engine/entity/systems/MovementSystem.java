package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;

import java.util.Arrays;
import java.util.List;

public class MovementSystem extends ComponentSystem {

    private Rectangle boundaries;
    protected WorldSpace worldSpace;

    public MovementSystem(Rectangle boundaries) {
        this.boundaries = boundaries;
    }

    @Override
    public void execute(float updateDelta, List<Entity> qualifiedEntities) {
        for(Entity entity : qualifiedEntities) {
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
            VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

            float newXpos = positionComponent.getX() + velocity.getVelocity_x() * updateDelta;
            float newYpos = positionComponent.getY() + velocity.getVelocity_y() * updateDelta;
            float newZpos = positionComponent.getZ() + velocity.getVelocity_z() * updateDelta;
            Gdx.app.getApplicationLogger().log("MovementSystem", String.format("Velocity for entity %s is %s/%s/%s", entity.getId(), velocity.getVelocity_x(), velocity.getVelocity_y(), velocity.getVelocity_z()));

            // TODO ignore entity collisionBox for now, might even have to merge with CollisionDetectionSystem...
            // TODO [AI System]: Fetch Targets, if any, and calculate velocity needed to reach it.

            if (boundaries.x > newXpos) {
                newXpos = boundaries.x;
                velocity.setVelocity_x(-velocity.getVelocity_x());
            } else if (boundaries.width < newXpos + collisionBox.getWidth()) {
                newXpos = boundaries.width - collisionBox.getWidth();
                velocity.setVelocity_x(-velocity.getVelocity_x());
            }
            positionComponent.setX(newXpos);

            if (boundaries.y > newYpos) {
                newYpos = boundaries.y;
                velocity.setVelocity_y(-velocity.getVelocity_y());
            } else if (boundaries.height < newYpos + collisionBox.getHeight()) {
                newYpos = boundaries.height - collisionBox.getHeight();
                velocity.setVelocity_y(-velocity.getVelocity_y());
            }
            positionComponent.setY(newYpos);

            positionComponent.setZ(newZpos);
        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class, CollisionBox.class);
    }
}
