package dbast.prometheus.engine.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import dbast.prometheus.utils.GeneralUtils;

import java.util.Arrays;

public enum Resolution {
    ULTRAWIDE(64,27),
    SIXTEEN_BY_NINE(16, 9),
    FOUR_BY_THREE(4,3),
    VERTICAL(1,2);


    private int width;
    private int height;
    private double ratio;

    Resolution(int width, int height) {
        this.width = width;
        this.height = height;
        this.ratio = ((double)height / (double)width);
    }


    public static Resolution getCurrentGDX() {
        return getFor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public static <N extends Number> Resolution getFor(N width, N height) {
        double ratio = height.doubleValue() / width.doubleValue();

        double rangeStart = 0;
        for (Resolution resolution : values()) {
            if (GeneralUtils.isBetween(ratio, rangeStart, resolution.ratio, true)) {
                return resolution;
            }
            rangeStart = resolution.ratio;
        }
        // This should never happen in a normal case;
        // Values should be checked in order of declaration, which is already sorted by the smallest ratio of height/width
        // so any *supported* resolution should be returned. Any crazy overscaled device, is not supported and doomed to use 4:3 .
        return FOUR_BY_THREE;
    }

    public double getRatio() {
        return ratio;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }


    @Override
    public String toString() {
        return String.format("{Resolution=%s[%s](width=%s, height=%s, ratio=%s)}", name(), ordinal(), getWidth(), getHeight(), getRatio());
    }
}
