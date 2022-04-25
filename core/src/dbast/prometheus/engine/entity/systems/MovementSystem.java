package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;

import java.util.Arrays;
import java.util.List;

public class MovementSystem extends ComponentSystem {

    private Rectangle boundaries;

    public MovementSystem(Rectangle boundaries) {
        this.boundaries = boundaries;
    }

    @Override
    public void execute(float updateDelta, List<Entity> qualifiedEntities) {
        for(Entity entity : qualifiedEntities) {
            PositionComponent position = entity.getComponent(PositionComponent.class);
            CollisionBox collisionBox = entity.getComponent(CollisionBox.class);
            VelocityComponent velocity = entity.getComponent(VelocityComponent.class);

            float newXpos = position.getX_pos() + velocity.getVelocity_x() * updateDelta;
            float newYpos = position.getY_pos() + velocity.getVelocity_y() * updateDelta;
            // TODO ignore entity collisionBox for now...

            if (boundaries.x > newXpos) {
                newXpos = boundaries.x;
                velocity.setVelocity_x(-velocity.getVelocity_x());
            } else if (boundaries.width < newXpos + collisionBox.getWidth()) {
                newXpos = boundaries.width - collisionBox.getWidth();
                velocity.setVelocity_x(-velocity.getVelocity_x());
            }
            position.setX_pos(newXpos);

            if (boundaries.y > newYpos) {
                newYpos = boundaries.y;
                velocity.setVelocity_y(-velocity.getVelocity_y());
            } else if (boundaries.height < newYpos + collisionBox.getHeight()) {
                newYpos = boundaries.height - collisionBox.getHeight();
                velocity.setVelocity_y(-velocity.getVelocity_y());
            }
            position.setY_pos(newYpos);
        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class, CollisionBox.class);
    }
}
