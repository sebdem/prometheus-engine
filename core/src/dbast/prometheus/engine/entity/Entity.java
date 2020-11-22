package dbast.prometheus.engine.entity;

import dbast.prometheus.engine.entity.components.Component;

import java.util.Arrays;
import java.util.List;

public class Entity {

    private Long id; // todo? string id

    public List<Component> properties;
    public List<Class<? extends Component>> property_types; // todo is this needed?

    public Entity(Long id) {
        this.id = id;
    }


    public Long getId() {
        return id;
    }

    public Entity addComponent(Component property) {
        this.properties.add(property);
        this.property_types.add(property.getClass());
        property.setEntity(this);
        return this;
    }

    public boolean hasComponents(Component... targets) {
        return properties.containsAll(Arrays.asList(targets));
    }
}
