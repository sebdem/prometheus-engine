package dbast.prometheus.engine.entity;

import dbast.prometheus.engine.entity.components.Component;
import dbast.prometheus.engine.entity.systems.ComponentSystem;

import java.util.*;
import java.util.stream.Collectors;

public class EntityRegistry extends HashMap<Long, Entity> {

    public Entity addNewEntity(Component... components) {
        return this.addNewEntity(new Long(this.size() + this.hashCode()), components);
    }
    public Entity addNewEntity(Long entityId, Component... components) {
        Entity entity = new Entity(entityId);
        this.put(entity.getId(), entity);
        for(Component comp : components) {
            entity.addComponent(comp);
        }
        return entity;
    }

    public List<Entity> havingComponents(List<Class<? extends Component>> components) {
        return this.values().stream().filter(entity ->
            entity.hasComponents(components)
        ).collect(Collectors.toList());
    }
    public List<Entity> compatibleWith(ComponentSystem system) {
        return system.onlyQualified(new ArrayList<>(this.values()));
    }
}
