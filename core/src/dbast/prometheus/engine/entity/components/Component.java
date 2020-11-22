package dbast.prometheus.engine.entity.components;

import dbast.prometheus.engine.entity.Entity;

public abstract class Component {

    protected Entity entity;

    public Component setEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public <E> E getProperty(String property) {
        try {
            return (E)this.getClass().getField(property).get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
