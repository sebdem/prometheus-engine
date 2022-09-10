package dbast.prometheus.engine.world.generation.features;

import dbast.prometheus.engine.world.WorldSpace;
import dbast.prometheus.engine.world.generation.ScalableFeature;
import dbast.prometheus.engine.world.tile.TileRegistry;

public class Hole extends ScalableFeature {

    public Hole() {
        super(vector3 -> (int)Math.floor(Math.random() * 4) + 3);
    }

    @Override
    public void place(WorldSpace world, float x, float y, float z) {
        // place hole
        int scale = 2+(int) (Math.random() * 10);
        float half = (float) Math.floor(scale *0.5f);
        for (float y2 = -(half +1); y2 <= (half + 1); y2++) {
            for (float x2 = -(half +1); x2 <= (half + 1); x2++) {
                world.removeTile(x+x2, y+y2, z-1);
                world.removeTile(x+x2, y+y2, z);
                if (x2 >= -half && x2 <= half && y2 >= -half && y2 <= half) {
                  //  world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z-1);
                } else {
                    world.placeTile(TileRegistry.idOfTag("grass_0"), x+x2, y+y2, z);
                }
            }
        }
    }
}
