package dbast.prometheus.engine.events;

import dbast.prometheus.engine.base.IKeyed;

public class Event implements IKeyed {

    private String key;

    public Object[] properties;

    public boolean cancelled = false;

    public Event(String key, Object... properties) {
        this.key = key;
        this.properties = properties;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
