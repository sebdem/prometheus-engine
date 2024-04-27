package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.Direction;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.tile.Tile;
import dbast.prometheus.engine.world.tile.TileData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateUpdateSystem extends ComponentSystem {

    public WorldSpace world;

    public StateUpdateSystem(WorldSpace world) {
        this.world = world;
    }

    @Override
    public void execute(float updateDelta, List<Entity> qualifiedEntities) {
        for(Entity entity : qualifiedEntities) {
            StateComponent entityState = entity.getComponent(StateComponent.class);

            entityState.updateCurrentAge(updateDelta);

            final String collidingState = "colliding";
            entity.executeFor(CollisionBox.class, collisionBox -> {
                //  Gdx.app.getApplicationLogger().log("Collision System:", String.format("update for entity %s state; new state is%s colliding", entityA.getId(), isColliding ? "" : " not"));
                boolean currentlyColliding = collisionBox.isColliding();
                boolean containsStatus = entityState.states.contains(collidingState);

                if (currentlyColliding && !containsStatus) {
                    entityState.setState(collidingState);
                } else if (!currentlyColliding && containsStatus) {
                    entityState.dropState(collidingState);
                } else {
                    // do nothing
                }
            });

            entity.executeFor(VelocityComponent.class, velocityComponent -> {
                //  Gdx.app.getApplicationLogger().log("Collision System:", String.format("update for entity %s state; new state is%s colliding", entityA.getId(), isColliding ? "" : " not"));
                boolean currentlyMoving = velocityComponent.isMoving();

                String movingState = "moving:";
                boolean containsStatus = entityState.states.stream().anyMatch(state-> state.startsWith("moving:"));

                if (currentlyMoving && !containsStatus) {
                    entityState.setState(movingState);
                } else if (!currentlyMoving && containsStatus) {
                    entityState.dropCurrentState();
                } else if (currentlyMoving){
                    // update direction "substate"
                    // TODO maybe this could look better if there was an actual MovementComponent, containing that specific information already, with the StateSystem just "composing" the final state per components
                    float abs_x = Math.abs(velocityComponent.getVelocity_x());
                    float abs_y = Math.abs(velocityComponent.getVelocity_y());

                    if (abs_y > 0) {
                        if (velocityComponent.getVelocity_y() > 0) {
                            movingState += "up";
                        } else if (velocityComponent.getVelocity_y() < 0) {
                            movingState += "down";
                        }
                    }
                    if (abs_x > 0) {
                        if (velocityComponent.getVelocity_x() > 0) {
                            movingState += "right";
                        } else if (velocityComponent.getVelocity_x() < 0) {
                            movingState += "left";
                        }
                    }

                    entityState.updateCurrentState(movingState);
                } else {
                    // Do nothing
                }

            });

            /*// TODO improve???
            entity.executeFor(PositionComponent.class, positionComponent -> {
                Vector3 underPosition = new Vector3(
                        (float)Math.floor(positionComponent.getX()),
                        (float)Math.floor(positionComponent.getY()),
                        positionComponent.getZ() - 1f
                );
                Tile standingOn = world.lookupTile(underPosition);
                if (standingOn != null) {
                    TileData standingOnData = world.tileDataMap.get(underPosition);
                    if (standingOnData == null) {
                        standingOnData = new TileData();
                        world.tileDataMap.put(underPosition, standingOnData);
                    }
                    standingOnData.state = "steppedOn";
                }
            });*/
        };
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Collections.singletonList(StateComponent.class);
    }
}
