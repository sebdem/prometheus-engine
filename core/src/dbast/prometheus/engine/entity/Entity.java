package dbast.prometheus.engine.entity;

import dbast.prometheus.engine.entity.components.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    public <C extends Component> C detachComponent(Class<C> clazz) {
        for(int i = 0; i < this.properties.size(); i++) {
            if (clazz.isInstance(this.properties.get(i))) {
                return clazz.cast(this.properties.remove(i));
            }
        }
        return null;
    }

    /*public Component getComponent(Class<? extends Component> classOf) {
        return properties.stream().filter(classOf::isInstance).findFirst().orElse(null);
    }*/
    public <CT extends Component> CT getComponent(Class<CT> classOf) {
        return classOf.cast(properties.stream().filter(classOf::isInstance).findFirst().orElse(null));
    }
    public <CT extends Component> Optional<CT> optionalComponent(Class<CT> classOf) {
        return (Optional<CT>)properties.stream().filter(classOf::isInstance).findFirst();
    }

    public boolean hasComponent(Class<? extends Component> target) {
        return  this.properties.stream().anyMatch(target::isInstance);
    }
    public boolean hasComponents(List<Class<? extends Component>> targets) {
        return targets.stream().allMatch(targetClass -> this.properties.stream().anyMatch(targetClass::isInstance));
    }
}
