package dbast.prometheus.engine.entity;

import dbast.prometheus.engine.entity.components.Component;
import dbast.prometheus.engine.entity.components.ComponentProvider;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Entity implements ComponentProvider {

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

    public boolean hasComponent(Class<? extends Component> target) {
        return  this.properties.stream().anyMatch(target::isInstance);
    }
    public boolean hasComponents(List<Class<? extends Component>> targets) {
        return targets.stream().allMatch(targetClass -> this.properties.stream().anyMatch(targetClass::isInstance));
    }

    public <CT extends Component> void executeFor(Class<CT> classOf, Consumer<CT> componentExecution) {
        this.properties.forEach(component -> {
            if (classOf.isInstance(component)) {
                componentExecution.accept(classOf.cast(component));
            }
        });
    }

    // for serialization and debug purpose
    public Map<String, Object> getData() {
        Map<String, Object> propertyData = new HashMap<>();
        this.properties.forEach(component -> {
            for(Field componentField : component.getClass().getFields()) {
                if (!componentField.getName().equals("entity")) {
                    Object data = null;
                    try {
                        data = componentField.get(component);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    propertyData.put(componentField.getName(), data);
                }
            }
        });
        return propertyData;
    }
}
