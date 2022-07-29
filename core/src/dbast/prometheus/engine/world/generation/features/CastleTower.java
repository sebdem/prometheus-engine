package dbast.prometheus.engine.world.generation.features;

import com.badlogic.gdx.math.MathUtils;
import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.PlaceFeature;
import dbast.prometheus.engine.world.tile.TileRegistry;
import dbast.prometheus.utils.GeneralUtils;

public class CastleTower implements PlaceFeature {

    private String tileTag;

    public CastleTower(String tileTag) {
        this.tileTag = tileTag;
    }

    @Override
    public void place(WorldSpace world, float x, float y, float z) {
        float maxZ = (float) (Math.floor((Math.random() * 3) + 1) * 4);

        int scaleX = 4;
        int scaleY = 4;

        for (float z2 = 0; z2 <= maxZ; z2++) {
            for (float y2 = 0; y2 < scaleY; y2++) {
                for (float x2 = 0; x2 < scaleX; x2++) {
                    world.removeTile(x+x2, y+y2, z+z2);
                    if (z2 %4 == 0 || z2 == maxZ) {
                        world.placeTile(TileRegistry.idOfTag(tileTag), x+x2, y+y2, z+z2);
                        /*if (x2 % (scaleX - 1)== 0 || y2 % (scaleY - 1) == 0) {
                            world.placeTile(TileRegistry.idOfTag(tileTag), x+x2, y+y2, z+z2);
                        } else {
                            world.placeTile(TileRegistry.idOfTag("glass_top"),x+x2, y+y2, z+z2);
                        }*/
                    }
                    else if (!(
                                GeneralUtils.isBetween(x2, 0f, scaleX - 1, false)
                            && GeneralUtils.isBetween(y2, 0f, scaleY - 1, false)
                    )) {
                        world.removeTile(x+x2, y+y2, z+z2);
                        world.placeTile(TileRegistry.idOfTag(tileTag), x+x2, y+y2, z+z2);
                    } /*else if (!( GeneralUtils.isBetween(x2, 0f, scaleX - 1, false)
                            || GeneralUtils.isBetween(y2, 0f, scaleY - 1, false))) {
                        world.placeTile(TileRegistry.idOfTag(tileTag), x+x2, y+y2, z+z2);
                    }*/
                    if (z2 == maxZ) {
                        if (!(GeneralUtils.isBetween(x2, 0f, scaleX - 1, false)
                            || GeneralUtils.isBetween(y2, 0f, scaleY - 1, false)
                        )) {
                            world.placeTile(TileRegistry.idOfTag(tileTag), x+x2, y+y2, z+z2+ 1);
                        }
                    }
                }
            }
        }
    }
}
