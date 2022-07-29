package dbast.prometheus.engine.world.generation.features;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.PlaceFeature;
import dbast.prometheus.engine.world.generation.ScalableFeature;
import dbast.prometheus.engine.world.tile.TileRegistry;

public class Mountain extends ScalableFeature {

    private String tileTag;

    public Mountain(String tileTag) {
        super(vector3 -> (int)(3+Math.random() * 16f));
        this.tileTag = tileTag;
    }

    @Override
    public void place(WorldSpace world, float x, float y, float z) {
        float maxWidth = scaleXSupplier.apply(new Vector3(x, y, z));
        float maxHeight = scaleYSupplier.apply(new Vector3(x, y, z));
        int halfWidth = (int) (maxWidth / 2);
        int halfHeight = (int) (maxHeight / 2);


        int xOff = 0, yOff = 0;
        float zMax = (float)((halfWidth + halfHeight) / 2);
        float zInc = 1f;
        for(float z2 = z; z2 <= zMax; z2 += zInc) {
            for (float y2 = -halfHeight + yOff; y2 <= halfHeight - yOff; y2++) {
                for (float x2 = -halfWidth + xOff; x2 <= halfWidth - xOff; x2++) {
                    float pX = x + x2;
                    float pY = y + y2;
                   //world.placeTile(TileRegistry.idOfTag("dirt_0"), pX, pY, z2-1);
                    world.removeTile(pX, pY, z2);
                    world.placeTile(TileRegistry.idOfTag(tileTag), pX, pY, z2);
                }
            }
            xOff++;
            yOff++;
            if (zInc * 0.75 > 0.125f)
                zInc *=0.75f;
        }
    }
}
