package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.LockOnCamera;
import dbast.prometheus.engine.world.Direction;

import java.util.HashSet;
import java.util.Set;

public class SpriteData implements Comparable<SpriteData> {

    public PrometheusSprite sprite;

    public Vector3 levelPosition;
    public Set<Direction> notBlocked;
    protected boolean dataUpdate = false;

    public Float orderIndex;

    public Vector3 screenPosition;
    public Vector3 renderOrderPosition;

    public SpriteData() {
        this.notBlocked = new HashSet<>();
        this.screenPosition = Vector3.Zero.cpy();
        this.renderOrderPosition = Vector3.Zero.cpy();
    }
    public SpriteData(PrometheusSprite sprite, Vector3 orderPosition) {
        this();
        // factually correct
        //this.spriteOrigin = spritePos3D.cpy().add(sprite.getOriginX(), 0,  sprite.getOriginY());
        setSprite(sprite);
        setPosition(orderPosition);
        recalcOrderIndex();
    }

    private static final Vector3 comparePoint = new Vector3(-256,-256,256);

    public float recalcOrderIndex() {
        // as grid origin is the closest point to the camera, x + y needs to be inversed
        //this.orderIndex = -(spriteOrigin.x + spriteOrigin.y) + spriteOrigin.z;
        // proper solution for entities...
        // TODO ADJUST Z MULTIPLIER!!!
        //this.orderIndex = -(position.x + sprite.getOriginX() + position.y) + position.z * 0.125f + sprite.getOriginY();
        float zWeight = 0.125f;
        float xyWeight = 1f;

        // this was a long time solution for 'oversized' sprites. Creates a buggy mess however.
       /* this.orderIndex = type.priorityOffset +
                (levelPosition.z * zWeight + sprite.getOriginY() * sprite.getHeight())
                        -(levelPosition.x * xyWeight + sprite.getOriginX() * sprite.getWidth() + levelPosition.y * xyWeight);*/
        // this is a more expensive, but 'correct' priority, based on the pure levelPosition distance to predefined comparePoint
       /*===>this.orderIndex = this.levelPosition.cpy().add(0,0,type.priorityOffset).dst2(comparePoint);*/
        /*this.orderIndex =
                (
                        (this.levelPosition.x + sprite.renderIndexOffset.x)
                )
                + (
                        (this.levelPosition.y + sprite.renderIndexOffset.y) * 16
                )
                - (
                        (this.levelPosition.z + sprite.renderIndexOffset.z) * 16 * 16  *//*+ sprite.type.priorityOffset*//*
                ); */
        // TODO deprecate type in favour of renderIndexOffset
        this.renderOrderPosition = this.levelPosition.cpy().add(this.sprite.renderIndexOffset).add(0,0,sprite.type.priorityOffset);
        this.orderIndex = renderOrderPosition.dst2(comparePoint);

       // this.orderIndex = levelPosition.z + type.priorityOffset - (levelPosition.x + levelPosition.y);

        // almost solution for terrain...
        // this.orderIndex = -(spritePos3D.x + sprite.getOriginX() + spritePos3D.y) + (spritePos3D.z / (spritePos3D.x + spritePos3D.y)) + sprite.getOriginY();
        //this.orderIndex = -(spritePos3D.x + sprite.getOriginX() + spritePos3D.y) + ((spritePos3D.z+ sprite.getOriginY()) / (spritePos3D.x + spritePos3D.y)) ;
        return this.orderIndex;
    }

    protected void setRequiresUpdate() {
        this.dataUpdate = true;
    }

    public void setPosition(Vector3 newPositon) {
        if (!newPositon.equals(this.levelPosition)) {
            this.setRequiresUpdate();
        }
        this.levelPosition = newPositon;
    }
    public void setSprite(PrometheusSprite newSprite) {
        this.setRequiresUpdate();
        this.sprite = newSprite;
    }

    public void setType(SpriteType type) {
        if (!type.equals(this.sprite.type)) {
            this.setRequiresUpdate();
        }
        this.sprite.type = type;
    }
    public void setOffset(Vector3 newOffset) {
        if (!newOffset.equals(this.sprite.renderIndexOffset)) {
            this.setRequiresUpdate();
        }
        this.sprite.setRenderIndexOffset(newOffset);
    }

    public void update(float updateDelta) {
        if (this.dataUpdate) {
            this.recalcOrderIndex();

            screenPosition = this.levelPosition.cpy();

            if (LockOnCamera.useGridSnapping) {
                float gridSnapIncrement = LockOnCamera.gridSnapIncrement;

                double xPos = Math.round(screenPosition.x);
                double yPos = Math.round(screenPosition.y);

                screenPosition.set(
                    (float)(xPos + (Math.round((screenPosition.x - xPos) / gridSnapIncrement)) * gridSnapIncrement),
                    (float)(yPos + (Math.round((screenPosition.y - yPos) / gridSnapIncrement)) * gridSnapIncrement),
                    screenPosition.z
                );
            }
            Vector3 projected = LockOnCamera.project_custom(
                    screenPosition,
                    -((this.sprite.getOriginX() * this.sprite.getWidth())- 0.5f),
                    this.levelPosition.z * 0.5f
            );

            this.screenPosition.set(projected.x, projected.y, this.levelPosition.z);
        }
    }

    @Override
    public int compareTo(SpriteData o) {
            // also a valid solution, but the using orderIndex built on creation of of this data is preferred
 /*           int zComp = Float.compare(this.levelPosition.z + sprite.getOriginY(), o.levelPosition.z + o.sprite.getOriginY());
            int yComp = Double.compare(Math.ceil(o.levelPosition.y), Math.ceil(this.levelPosition.y));
            int xComp = Double.compare(Math.ceil(o.levelPosition.x +  + o.sprite.getOriginX()), Math.ceil(this.levelPosition.x + sprite.getOriginX()));

            if (xComp == 0 && yComp == 0) {
                return zComp;
            }
            return yComp == 0 ? zComp == 0 ? xComp : zComp: yComp;*/

        //return this.orderIndex.compareTo(o.orderIndex);
        return o.orderIndex.compareTo(this.orderIndex);
/*

        int xComp = Float.compare(o.renderOrderPosition.x, this.renderOrderPosition.x);
        int yComp = Float.compare(o.renderOrderPosition.y, this.renderOrderPosition.y);
        int zComp = Float.compare(this.renderOrderPosition.z, o.renderOrderPosition.z);

        int compResult = 0;
        if (xComp == 0 && yComp == -1) {
            switch (zComp) {
                case 0:
                case 1:
                case -1:
                    compResult = 1; break;
                default: break;
            }
        } else if (xComp == 0 && yComp == 1) {
            switch (zComp) {
                case 0:
                case -1:
                case 1:
                    compResult = -1; break;
                default: break;
            }
        } else if (xComp == -1 && yComp == 0) {
            switch (zComp) {
                case 0:
                case 1:
                case -1:
                    compResult = 1; break;
                default: break;
            }
        } else if (xComp == 1 && yComp == 0) {
            switch (zComp) {
                case 0:
                case 1:
                case -1:
                    compResult = -1; break;
                default: break;
            }
        } else if (xComp == 0 && yComp == 0) {
            switch (zComp) {
                case 0: compResult = Float.compare(o.sprite.type.priorityOffset, this.sprite.type.priorityOffset); break;
                case -1: compResult = 1; break;
                case 1: compResult = -1; break;
                default: break;
            }
        } else if (xComp == -1 && yComp == -1) {
            switch (zComp) {
                case 0:
                case -1:
                case 1:
                    compResult = -1; break;
                default: break;
            }
        }  else if (xComp == 1 && yComp == 1) {
            switch (zComp) {
                case 0:
                case -1:
                case 1:
                    compResult = 1; break;
                default: break;
            }

        } else if (xComp == -1 && yComp == 1) {
            switch (zComp) {
                case 0: compResult = Float.compare(o.sprite.type.priorityOffset, this.sprite.type.priorityOffset); break;
                case -1: compResult = 1; break;
                case 1: compResult = -1; break;
                default: break;
            }
        } else if (xComp == 1 && yComp == -1) {
            switch (zComp) {
                case 0: compResult = Float.compare(this.sprite.type.priorityOffset, o.sprite.type.priorityOffset); break;
                case -1: compResult = 1; break;
                case 1: compResult = -1; break;
                default: break;
            }
        }
        return compResult;*/
        // return Float.compare(this.levelPosition.dst(comparePoint), o.levelPosition.dst(comparePoint));
        //return this.orderIndex.compareTo(o.orderIndex);
    }
}