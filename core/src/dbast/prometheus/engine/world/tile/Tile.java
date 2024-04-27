package dbast.prometheus.engine.world.tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import dbast.prometheus.engine.entity.components.RenderComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Tile {
    public final static Tile MISSING_TEXTURE = new Tile("missing", Gdx.files.internal("missing.png"));

    public RenderComponent renderComponent;
    public String tag;
    public Float height = 1f;
   // public Vector3 bounds = new Vector3(1f,1f,1f);
    public float[][] bounds = new float[][]{{0f,0f,0f,1f,1f,1f}};

    public Tile() {
    }
    public Tile(String tag, FileHandle tileTexture) {
        this(tag, tileTexture, 1, 1, 1f);
    }
    public Tile(String tag, FileHandle tileTexture, int columns, int rows, float frameDuration) {
        this();
        this.tag = tag;
        this.renderComponent = new RenderComponent()
                .registerAnimation(tileTexture, columns, rows, frameDuration, true, "default");
    }

    protected static float BOUNDARY_OFFSET = 0.001f;

    @Deprecated
    public BoundingBox getBoundsFor(Vector3 offset) {
        // rounding issue forces us to substract 0.01f
        BoundingBox basic = new BoundingBox(offset, offset);
        BoundingBox tileBox = new BoundingBox(offset, offset);

        Vector3 boundsMin = new Vector3(bounds[0][0], bounds[0][1], bounds[0][2]);
        Vector3 boundsMax = new Vector3(bounds[0][bounds.length-3], bounds[0][bounds.length-2], bounds[0][bounds.length-1]);


        /*if (boundsMin.len() != 0) {
            tileBox.ext(boundsMin.add(BOUNDARY_OFFSET));
        }
        if (boundsMax.len() != 0) {
            tileBox.ext(boundsMax.sub(BOUNDARY_OFFSET));
        }*/
        tileBox.set(Arrays.asList(boundsMin.add(offset).add(BOUNDARY_OFFSET), boundsMax.add(offset).sub(BOUNDARY_OFFSET)));
        /*
        if (bounds.len() != 0) {
            tileBox = new BoundingBox(offset, offset.cpy().add(bounds).sub(
                    bounds.x != 0 ? 0.001f : 0,
                    bounds.y != 0 ? 0.001f : 0,
                    bounds.z != 0 ? 0.001f : 0
            ));
        }*/
        return tileBox.equals(basic) ? null : tileBox;
    }

    public List<BoundingBox> getBoundsForPositon(Vector3 offset) {
        // rounding issue forces us to substract 0.01f
        BoundingBox basic = new BoundingBox(offset, offset);

        List<BoundingBox> boundList = new ArrayList<>();

        BoundingBox tileBox;
        Vector3 boundsMin, boundsMax;

        for (float[] bound : bounds) {
            tileBox = new BoundingBox(offset, offset);
            boundsMin = new Vector3(bound[0], bound[1], bound[2]);
            boundsMax = new Vector3(bound[bound.length - 3], bound[bound.length - 2], bound[bound.length - 1]);

            tileBox.set(Arrays.asList(boundsMin.add(offset).add(BOUNDARY_OFFSET), boundsMax.add(offset).sub(BOUNDARY_OFFSET)));
            if (!tileBox.equals(basic)) {
                boundList.add(tileBox);
            }
        }
        return boundList.isEmpty() ? null : boundList;
    }
}
