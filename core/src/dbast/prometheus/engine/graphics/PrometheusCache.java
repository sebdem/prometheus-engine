package dbast.prometheus.engine.graphics;

import java.util.HashMap;

public class PrometheusCache <K, V> extends HashMap<K,V> {

    private final float maxUnusedTime;
    private final HashMap<K, TimedCacheEntry> timingMap;

    public PrometheusCache(float maxUnusedTime) {
        super();
        this.maxUnusedTime = maxUnusedTime;
        this.timingMap = new HashMap<>(this.size());
    }

    /**
     * Creates a Cache with the default time of 100 units
     */
    public PrometheusCache() {
        this(10);
    }

    public void update(float delta) {
        timingMap.values().forEach(vTimedCacheEntry -> vTimedCacheEntry.update(delta));
        this.keySet().removeIf(key -> timingMap.get(key).isUnused(maxUnusedTime));
         //.removeIf(cacheEntry -> timingMap.get( >= maxUnusedTime);
    }
    public static class TimedCacheEntry {
        private float unusedLivingTime;

        public void update(float delta) {
            unusedLivingTime += delta;
        }

        public boolean isUnused(float maxUnusedTime) {
            return this.unusedLivingTime >= maxUnusedTime;
        }
        public void reset() {
            this.unusedLivingTime = 0;
        }
    }



    @Override
    public V put(K key, V value) {
        this.timingMap.put(key, new TimedCacheEntry());
        return super.put(key, value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        this.timingMap.putIfAbsent(key, new TimedCacheEntry());
        return super.putIfAbsent(key, value);
    }

    @Override
    public V get(Object key) {
        this.timingMap.getOrDefault(key, new TimedCacheEntry()).reset();
        return super.get(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        this.timingMap.getOrDefault(key, new TimedCacheEntry()).reset();
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        this.timingMap.getOrDefault(key, new TimedCacheEntry()).reset();
        return super.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        this.timingMap.getOrDefault(key, new TimedCacheEntry()).reset();
        return super.replace(key, value);
    }

}
