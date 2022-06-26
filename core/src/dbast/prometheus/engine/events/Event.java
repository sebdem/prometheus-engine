package dbast.prometheus.engine.events;

import dbast.prometheus.engine.base.IKeyed;

import java.util.Map;

public class Event implements IKeyed {

    private String key;

    public Map<String, Object> properties;

    public boolean cancelled = false;

    public Event(String key, Map<String,Object> params) {
        this.key = key;
        this.properties = params;
    }

    public Event(String key) {
        this.key = key;
    }

    public Event withParam(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
