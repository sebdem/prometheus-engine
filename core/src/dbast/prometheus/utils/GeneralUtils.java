package dbast.prometheus.utils;

import com.badlogic.gdx.math.Vector3;
import net.dermetfan.utils.ArrayUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Some lazy fuck did this...
 */
public class GeneralUtils {
    // these guys might be useful at some point???
    public static <T> void forEach(T[] array, Consumer<T> executeOrder66) {
        Arrays.stream(array).forEach(executeOrder66);
    }
    public static <T> void setForEach(T[] array, Function<T, T> executeOrder66) {
        for(int i = 0; i < array.length; i++) {
            array[i] = executeOrder66.apply(array[i]);
        }
    }

    public static void populate2DInt(int[][] array, int min, int max) {
        int [] row;
        for(int y = 0; y < array.length; y++) {
            row = array[y];
            for (int x = 0; x < row.length; x++) {
                row[x] = (int)(Math.random() * (max - min));
            }
        }
    }

    public static <T> T randomElement(Collection<T> collection) {
        if (collection instanceof WeightedRandomBag) {
            return randomElement((WeightedRandomBag<T>) collection);
        }
        return (T)collection.toArray()[(int) (collection.size() * Math.random())];
    }
    public static <T> T randomElement(WeightedRandomBag<T> bag) {
        return bag.getRandom();
    }
    public static <T> T randomElement(T[] array) {
        return array[(int) (array.length * Math.random())];
    }

    public static <N extends Number> boolean isBetween(N value, N rangeStart, N rangeEnd, boolean includeRange) {
        if (includeRange) {
            return value.doubleValue() >= rangeStart.doubleValue() && value.doubleValue() <= rangeEnd.doubleValue();
        } else {
            return value.doubleValue() > rangeStart.doubleValue() && value.doubleValue() < rangeEnd.doubleValue();
        }
    }

    public static Vector3 floorVector3(Vector3 vector3) {
        return vector3.set(
                (float)Math.floor(vector3.x),
                (float)Math.floor(vector3.y),
                (float)Math.floor(vector3.z));
    }

    // for some reason, this doesn't apply all to well, but by all accounts, it doesn't make any sense why
    @Deprecated
    public static Vector3 projectIso(Vector3 vector3, float scaleX, float scaleY) {
        // storing unmodified z as "depth"
        return new Vector3((vector3.x - vector3.y) * scaleX, ((vector3.x/2) + (vector3.y / 2) - vector3.z) * scaleY, vector3.z);
    }


    public static Vector3 floatToVector(float[] floats) {
        return new Vector3(floats);
    }
    public static Vector3[] floatsToVectors(float[] floats) {
        Vector3[] vector3s = new Vector3[(int) Math.floor(floats.length / 3f)];
        for(int i = 0; i <= floats.length - 3; i += 3) {
            vector3s[i] = new Vector3(floats[i], floats[i+1], floats[i+2]);
        }
        return vector3s;
    }

    public static float[] vectorToFloat(Vector3 vector3) {
        return new float[]{vector3.x, vector3.y, vector3.z};
    }


    public final static char FLOAT_HASH_DELIMITER = '_';
    public static String floatToString(float[] floats, boolean toInt) {
        StringBuilder hashBuilder = new StringBuilder();

        for(int i = 0; i < floats.length; i++) {
            if (toInt) {
                hashBuilder.append(Math.round(floats[i]));
            } else {
                hashBuilder.append(floats[i]);
            }
            if (i < floats.length - 1) {
                hashBuilder.append(FLOAT_HASH_DELIMITER);
            }
        }

        return hashBuilder.toString();
    }

    public static float[] stringToFloat(String floatHash) {
        String[] elements = floatHash.split(String.valueOf(FLOAT_HASH_DELIMITER));
        float[] floats = new float[elements.length];

        for(int i = 0; i < elements.length; i++) {
            floats[i] = Float.parseFloat(elements[i]);
        }

        return floats;
    }
}
