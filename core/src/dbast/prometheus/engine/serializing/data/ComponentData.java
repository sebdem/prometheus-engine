package dbast.prometheus.engine.serializing.data;

import dbast.prometheus.engine.entity.components.Component;

public class ComponentData {
    public String componentType;
    public Object data;

    public ComponentData(Component component) {
        this.componentType = component.getClass().getName();
        data = component;
    }
}
