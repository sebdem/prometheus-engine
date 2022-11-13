package dbast.prometheus.engine.config;

// TODO load this form a file or command line arguments? Store into a static map available anywhere!

import java.util.HashMap;

public class PrometheusConfig extends HashMap<String, Object> {

    public static PrometheusConfig conf;

    public static PrometheusConfig init() {
        conf = new PrometheusConfig();
        return conf;
    }

    public <E> E get(String key, Class<E> valueType) {
        Object value = super.get(key);
        return value == null ? null : valueType.cast(super.get(key));
    }

    public static <E> E get(String key, Class<E> valueType, E defaultIfNull) {
        return valueType.cast(conf.getOrDefault(key, defaultIfNull));
    }
}
