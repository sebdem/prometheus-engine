package dbast.prometheus.engine.entity;

import dbast.prometheus.engine.entity.components.Component;

import java.util.ArrayList;

public class EntityRegistry extends ArrayList<Entity> {

    public Entity addNewEntity(Component... components) {
        return this.addNewEntity(new Long(this.size() + this.hashCode()), components);
    }
    public Entity addNewEntity(Long entityId, Component... components) {
        Entity entity = new Entity(entityId);
        this.add(entity);
        for(Component comp : components) {
            entity.addComponent(comp);
        }
        return entity;
    }
}
