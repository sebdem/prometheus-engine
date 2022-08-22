package dbast.prometheus.utils;

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
        return (T)collection.toArray()[(int) (collection.size() * Math.random())];
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
}
