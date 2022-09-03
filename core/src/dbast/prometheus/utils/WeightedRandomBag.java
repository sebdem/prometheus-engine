package dbast.prometheus.utils;

import java.util.*;
import java.util.stream.Collectors;

public class WeightedRandomBag<T extends Object> implements Collection<T> {

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.entries.stream().anyMatch(entry->entry.object.equals(o));
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return getRandom();
            }
        };
    }

    @Override
    public Object[] toArray() {
        return this.entries.stream().map(entry-> entry.object).toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return (T1[]) this.entries.stream().map(entry-> entry.object).toArray();
    }

    /**
     * add value with 50% change
     * @param t
     * @return
     */
    @Override
    public boolean add(T t) {
        return this.addEntry(t, 50);
    }

    @Deprecated
    @Override
    public boolean remove(Object o) {
        return this.entries.removeIf(entry -> entry.object.equals(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.entries.stream().map(entry -> entry.object).collect(Collectors.toList()).containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        double beforeAttempt = this.accumulatedWeight;
        c.forEach(toAdd -> this.addEntry(toAdd, 50));
        return beforeAttempt < this.accumulatedWeight;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        double beforeAttempt = this.accumulatedWeight;
        c.forEach(this::remove);
        return beforeAttempt > this.accumulatedWeight;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<Entry> saveRef = this.entries;
        try {
            Map<T, Double> weightMap = new HashMap<>();
            this.entries.forEach(weightedEntry -> {
                if (c.contains(weightedEntry.object)) {
                    weightMap.put(weightedEntry.object,
                            weightMap.getOrDefault(weightedEntry.object, 0d)
                                    + weightedEntry.originalWeight);
                }
            });
            this.entries = new ArrayList<>();
            weightMap.forEach(this::addEntry);
            return true;
        } catch (Exception ignore) {
            ignore.printStackTrace();
            this.entries = saveRef;
            return false;
        }
    }

    @Override
    public void clear() {
        this.accumulatedWeight = 0;
        this.entries = new ArrayList<>();
    }

    private class Entry {
        double accumulatedWeight;
        double originalWeight;
        T object;
        public Entry(T object, double accumulatedWeight, double originalWeight) {
            this.object = object;
            this.accumulatedWeight = accumulatedWeight;
            this.originalWeight = originalWeight;
        }
    }

    private List<Entry> entries = new ArrayList<>();
    private double accumulatedWeight;
    private Random rand = new Random();

    public boolean addEntry(T object, double weight) {
        accumulatedWeight += weight;
        return entries.add(new Entry(object, accumulatedWeight, weight));
    }


    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry: entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }
        return null; //should only happen when there are no entries
    }
}