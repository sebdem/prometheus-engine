package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.PooledLinkedList;
import dbast.prometheus.engine.graphics.SpriteData;

import java.util.ArrayList;

/**
 * Since Multiple sprites can have the same orderIndex, using a Map of any kind would likely not work very well.
 */
public class SpriteDataQueue extends ArrayList<SpriteData> {

    public SpriteData add(Vector3 spritePos3D, Sprite Sprite) {
        SpriteData data = new SpriteData(Sprite, spritePos3D);
        this.add(data);
        return data;
    }
}
