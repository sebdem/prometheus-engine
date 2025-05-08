package dbast.prometheus.engine.entity.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.*;
import dbast.prometheus.engine.world.WorldSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovementSystem extends ComponentSystem {

    protected WorldSpace worldSpace;

    public MovementSystem(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
    }

    @Override
    public void execute(float updateDelta) {
        for(Entity entity : qualifiedEntities) {
            PositionComponent positionComponent = entity.getComponent(PositionComponent.class);
            VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
/*
            Tile tileUnder = worldSpace.lookupTile(new Vector3(Math.round(positionComponent.position.x), Math.round(positionComponent.position.y), positionComponent.position.z -1f));
            if (tileUnder == null || tileUnder.height != 1) {
                velocity.setVelocity_z(velocity.getVelocity_z() - 1f);
            }
*/
            if (velocity.isMoving()) {
                float oldXpos = positionComponent.getX();
                float newXpos = positionComponent.getX() + velocity.getVelocity_x() * updateDelta;
                float oldYpos = positionComponent.getY();
                float newYpos = positionComponent.getY() + velocity.getVelocity_y() * updateDelta;
                float oldZpos = positionComponent.getZ();
                float newZpos = positionComponent.getZ() + velocity.getVelocity_z() * updateDelta;
               // Gdx.app.getApplicationLogger().log("MovementSystem", String.format("Velocity for entity %s is %s/%s/%s", entity.getId(), velocity.getVelocity_x(), velocity.getVelocity_y(), velocity.getVelocity_z()));

                Vector3 newPos = new Vector3(newXpos, newYpos, newZpos);
                boolean canMoveTo = worldSpace.isPositionInWorld(newPos);

                CollisionBox collisionBox = entity.getComponent(CollisionBox.class);

                if (canMoveTo && collisionBox != null && !collisionBox.isPermeable()) {
                    BoundingBox entityBoundary =collisionBox.getBoundsAt(newPos);
               // TODO a lot of this stuff, while related to movement, really shouldn't be in here. Gravity/ascend, only makes sense inside a movement system if it is limited only to the grid. Currently it's more physics based.
                    float allowedStep = 0.66f;
                    //float allowedStep = 2f;
                    Vector3 newPosHigher = newPos.cpy().add(0,0,allowedStep);
                    Vector3 newPosLower = newPos.cpy().add(0,0,-allowedStep);
                    BoundingBox entityBoundaryHigher = collisionBox.getBoundsAt(newPosHigher);
                    BoundingBox entityBoundaryLower= collisionBox.getBoundsAt(newPosLower);


                    Vector3 minChunk = worldSpace.convertToChunkPos(entityBoundary.min);
                    Vector3 maxChunk = worldSpace.convertToChunkPos(entityBoundary.max);

                    List<BoundingBox> chunkBounds = new ArrayList<>();

                    if (minChunk.equals(maxChunk)) {
                        chunkBounds.addAll(worldSpace.getChunkRegister().getChunk(minChunk).boundaries);
                    } else {
                        chunkBounds.addAll(worldSpace.getChunkRegister().getChunk(minChunk).boundaries);
                        chunkBounds.addAll(worldSpace.getChunkRegister().getChunk(maxChunk).boundaries);
                    }

                    if (!chunkBounds.isEmpty()) {
                        for(BoundingBox bb : chunkBounds) {
                            if (bb.intersects(entityBoundary)) {
                                if (!bb.intersects(entityBoundaryHigher)) {
                                    newZpos = bb.max.z;
                                } else {
                                    canMoveTo = false;
                                    Gdx.app.getApplicationLogger().log("Movement", "Player corner " + newPos.toString() + " intersects with bounds " + bb.toString());
                               }
                            }
                        }
                    }


                    /*

                    Vector3[] corners = collisionBox.getCorners(newPos);
                    for(int i = 0; i < corners.length && canMoveTo; i++ ) {
                        Vector3 cornerVector = GeneralUtils.floorVector3(corners[i]);


                        // New Rules should be:
                        *//*
                            0. All corner locations should be in the world?
                            => For Each location under entity: {

                            }


                         *//*
                        if (worldSpace.isPositionInWorld(cornerVector)) {
                            *//*Tile tileAt = worldSpace.lookupTile(cornerVector);

                            if (tileAt != null) {
                                canMoveTo = false;
                            } else {
                                if (velocity.getVelocity_z() == 0 && !worldSpace.canStandIn(cornerVector)) {
                                    canMoveTo = false;
                                }
                                // entity is good to go

                            }*//*
                        } else {
                            canMoveTo = false;
                        }

                        *//*if (!(worldSpace.isPositionInWorld(cornerVector)
                               // && worldSpace.isPositionFree(cornerVector)
                                && (velocity.getVelocity_z() != 0 || worldSpace.canStandIn(cornerVector))
                        )) {
                            canMoveTo = false;
                        }*//*
                    }*/
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
                } else {
                    // AI entity cannot move further, so let's cut it here...
                    entity.executeFor(TargetTraverseComponent.class, TargetTraverseComponent::reachedTarget);
                }

            }
        }
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(PositionComponent.class, VelocityComponent.class/*, CollisionBox.class*/);
    }
}
