package dbast.prometheus.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.PooledLinkedList;

import java.util.ArrayList;

public class SpriteDataQueue extends ArrayList<SpriteDataQueue.SpriteData> {


    public void add(Vector3 spritePos3D, Sprite Sprite) {
        this.add(new SpriteData(Sprite, spritePos3D));
    }

    public static class SpriteData implements Comparable<SpriteData> {
        public Float orderIndex;
        public Sprite sprite;
        public Vector3 position;
        //public Vector3 spriteOrigin;

        public SpriteData(Sprite sprite, Vector3 orderPosition) {
            // factually correct
            //this.spriteOrigin = spritePos3D.cpy().add(sprite.getOriginX(), 0,  sprite.getOriginY());
            this.sprite = sprite;
            this.position = orderPosition;
            recalcOrderIndex();
        }
        public float recalcOrderIndex() {
            // as grid origin is the closest point to the camera, x + y needs to be inversed
            //this.orderIndex = -(spriteOrigin.x + spriteOrigin.y) + spriteOrigin.z;
            // proper solution for entities...
            // TODO ADJUST Z MULTIPLIER!!!
            this.orderIndex = -(position.x + sprite.getOriginX() + position.y) + position.z * 0.125f + sprite.getOriginY();
            // almost solution for terrain...
           // this.orderIndex = -(spritePos3D.x + sprite.getOriginX() + spritePos3D.y) + (spritePos3D.z / (spritePos3D.x + spritePos3D.y)) + sprite.getOriginY();
            //this.orderIndex = -(spritePos3D.x + sprite.getOriginX() + spritePos3D.y) + ((spritePos3D.z+ sprite.getOriginY()) / (spritePos3D.x + spritePos3D.y)) ;
            return this.orderIndex;
        }

        public void update(Vector3 newPositon) {
            this.position = newPositon;
            this.recalcOrderIndex();
        }
        public void update(Sprite newSprite) {
            this.sprite = newSprite;
            this.recalcOrderIndex();
        }

        @Override
        public int compareTo(SpriteData o) {
            /*
            // also a valid solution, but the using orderIndex built on creation of of this data is preferred
            int zComp = Float.compare(this.spritePos3D.z + sprite.getOriginY(), o.spritePos3D.z + o.sprite.getOriginY());
            int yComp = Double.compare(Math.ceil(o.spritePos3D.y), Math.ceil(this.spritePos3D.y));
            int xComp = Double.compare(Math.ceil(o.spritePos3D.x +  + o.sprite.getOriginX()), Math.ceil(this.spritePos3D.x + sprite.getOriginX()));

            if (xComp == 0 && yComp == 0) {
                return zComp;
            }
            return yComp == 0 ? zComp == 0 ? xComp : zComp: yComp;
            */
            return this.orderIndex.compareTo(o.orderIndex);
        }
    }
}
