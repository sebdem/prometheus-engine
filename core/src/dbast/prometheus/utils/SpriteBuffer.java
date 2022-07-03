package dbast.prometheus.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class SpriteBuffer extends ArrayList<SpriteBuffer.SpriteData> {



    public void add(Vector3 spritePos3D, Sprite Sprite) {
        this.add(new SpriteData(Sprite, spritePos3D));
    }

    public static class SpriteData implements Comparable<SpriteData> {
        public Float orderIndex;
        public Sprite sprite;
        public Vector3 spritePos3D;
        //public Vector3 spriteOrigin;

        public SpriteData(Sprite sprite, Vector3 spritePos3D) {
            // factually correct
            //this.spriteOrigin = spritePos3D.cpy().add(sprite.getOriginX(), 0,  sprite.getOriginY());
            this.sprite = sprite;
            this.spritePos3D = spritePos3D;
            recalcOrderIndex();
        }
        public float recalcOrderIndex() {
            // as grid origin is the closest point to the camera, x + y needs to be inversed
            //this.orderIndex = -(spriteOrigin.x + spriteOrigin.y) + spriteOrigin.z;
            this.orderIndex = -(spritePos3D.x + sprite.getOriginX() + spritePos3D.y) + spritePos3D.z + sprite.getOriginY();
            return this.orderIndex;
        }

        public void update(Vector3 newPositon) {
            this.spritePos3D = newPositon;
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
            int zComp = Float.compare(this.spriteOrigin.z, o.spriteOrigin.z);
            int yComp = Double.compare(Math.ceil(o.spriteOrigin.y), Math.ceil(this.spriteOrigin.y));
            int xComp = Double.compare(Math.ceil(o.spriteOrigin.x), Math.ceil(this.spriteOrigin.x));

            if (xComp == 0 && yComp == 0) {
                return zComp;
            }
            return yComp == 0 ? zComp == 0 ? xComp : zComp: yComp;*/
            return this.orderIndex.compareTo(o.orderIndex);
        }
    }
}
