package dbast.prometheus.engine.entity;

import dbast.prometheus.engine.entity.components.Component;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    private Long id; // todo? string id

    public List<Component> properties;

    public Entity(Long id) {
        this.id = id;
        this.properties = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public Entity addComponent(Component property) {
        // TODO each entity can only have one of each property?
        this.properties.add(property);
        property.setEntity(this);
        return this;
    }

    public Component getComponent(Class<? extends Component> classOf) {
        return properties.stream().filter(classOf::isInstance).findFirst().orElse(null);
    }

    public boolean hasComponents(List<Class<? extends Component>> targets) {
        return targets.stream().allMatch(targetClass -> this.properties.stream().anyMatch(targetClass::isInstance));
        //this.properties.forEach(property-> (targets.get(0)).isInstance(property));
        //return property_types.containsAll(targets);
    }
}
