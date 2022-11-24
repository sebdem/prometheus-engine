package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.world.WorldSpace;

public class SpriteData implements Comparable<SpriteData> {
    public Float orderIndex;
    public SpriteType type = SpriteType.DEFAULT;
    public Sprite sprite;
    public Vector3 levelPosition;
    //public Vector3 spriteOrigin;

    public SpriteData(Sprite sprite, Vector3 orderPosition) {
        // factually correct
        //this.spriteOrigin = spritePos3D.cpy().add(sprite.getOriginX(), 0,  sprite.getOriginY());
        this.sprite = sprite;
        this.levelPosition = orderPosition;
        recalcOrderIndex();
    }
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
        this.orderIndex = this.levelPosition.cpy().add(0,0,type.priorityOffset).dst2(comparePoint);

        // almost solution for terrain...
        // this.orderIndex = -(spritePos3D.x + sprite.getOriginX() + spritePos3D.y) + (spritePos3D.z / (spritePos3D.x + spritePos3D.y)) + sprite.getOriginY();
        //this.orderIndex = -(spritePos3D.x + sprite.getOriginX() + spritePos3D.y) + ((spritePos3D.z+ sprite.getOriginY()) / (spritePos3D.x + spritePos3D.y)) ;
        return this.orderIndex;
    }

    public void update(Vector3 newPositon) {
        this.levelPosition = newPositon;
        this.recalcOrderIndex();
    }
    public void update(Sprite newSprite) {
        this.sprite = newSprite;
        this.recalcOrderIndex();
    }
    public void update(SpriteType type) {
        this.type = type;
        this.recalcOrderIndex();
    }


    private static final Vector3 comparePoint = new Vector3(-1000,-1000,1000);
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

       // return this.orderIndex.compareTo(o.orderIndex);
        return o.orderIndex.compareTo(this.orderIndex);
       // return Float.compare(this.levelPosition.dst(comparePoint), o.levelPosition.dst(comparePoint));
        //return this.orderIndex.compareTo(o.orderIndex);
    }
}