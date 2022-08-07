package dbast.prometheus.engine.serializing.data;

import dbast.prometheus.engine.entity.Entity;

import java.util.List;
import java.util.stream.Collectors;

public class EntityData {

    public Long id;
    public List<ComponentData> properties;

    public EntityData() {
    }

    public EntityData(Entity entity) {
        super();
        this.id = entity.getId();
        this.properties = entity.properties.stream().map(ComponentData::new).collect(Collectors.toList());
    }
}
