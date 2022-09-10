package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.utils.GeneralUtils;

import java.util.Arrays;
import java.util.List;

public class MovementSystem extends ComponentSystem {

    protected Rectangle boundaries;
    protected WorldSpace worldSpace;

    public MovementSystem(WorldSpace worldSpace) {
        this.boundaries = new Rectangle(0f,0f, worldSpace.width, worldSpace.height);
        this.worldSpace = worldSpace;
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
           // Gdx.app.getApplicationLogger().log("MovementSystem", String.format("Velocity for entity %s is %s/%s/%s", entity.getId(), velocity.getVelocity_x(), velocity.getVelocity_y(), velocity.getVelocity_z()));

            Vector3 newPos = new Vector3(newXpos, newYpos, newZpos);
            boolean canMoveTo = worldSpace.isValidPosition(newPos);

            if (collisionBox != null && !collisionBox.isPermeable()) {
                /*if (collisionBox.isColliding()) {
                    canMoveTo = false;
                }*/
                Vector3[] corners = collisionBox.getCorners(newPos);
                for(int i = 0; i < corners.length && canMoveTo; i++ ) {
                    Vector3 cornerVector = GeneralUtils.floorVector3(corners[i]);
                    if (!(worldSpace.isValidPosition(cornerVector)
                            && worldSpace.isOccupied(cornerVector)
                            && (velocity.getVelocity_z() != 0 || worldSpace.canStandIn(cornerVector))
                    )) {
                        canMoveTo = false;
                    }
                }
            }

            //Vector3 newPosRounded = new Vector3(Math.round(newXpos), Math.round(newYpos), Math.round(newZpos));

            /*if (boundaries.x > newXpos) {
                newXpos = boundaries.x;
                velocity.setVelocity_x(-velocity.getVelocity_x());
            } else if (boundaries.width < newXpos + collisionBox.getWidth()) {
                newXpos = boundaries.width - collisionBox.getWidth();
                velocity.setVelocity_x(-velocity.getVelocity_x());
            }

            if (boundaries.y > newYpos) {
                newYpos = boundaries.y;
                velocity.setVelocity_y(-velocity.getVelocity_y());
            } else if (boundaries.height < newYpos + collisionBox.getHeight()) {
                newYpos = boundaries.height - collisionBox.getHeight();
                velocity.setVelocity_y(-velocity.getVelocity_y());
            }*/

            if (canMoveTo) {
                positionComponent.setX(newXpos);
                positionComponent.setY(newYpos);
                positionComponent.setZ(newZpos);
            }

        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class, CollisionBox.class);
    }
}
