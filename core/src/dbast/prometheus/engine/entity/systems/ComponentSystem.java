package dbast.prometheus.engine.entity.systems;

import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ComponentSystem {

    public List<Entity> qualifiedEntities = new ArrayList<>();

    public void clear() {
        this.qualifiedEntities.clear();
    }

    public void registerIfQualified(Entity entity) {
        if (entity.hasComponents(this.neededComponents())) {
            this.qualifiedEntities.add(entity);
        }
    }

    public List<Entity> onlyQualified(List<Entity> entities) {
        final List<Class<? extends Component>> components = neededComponents();
        return entities.stream().filter(entity -> entity.hasComponents(components)).collect(Collectors.toList());
    }

    /**
     * never call this without making sure the list is onlyQualified !!!!!
     *
     * @param updateDelta w
     */
    public abstract void execute(float updateDelta);

    public abstract List<Class<? extends Component>> neededComponents();

    public String getSystemID() {
        return this.getClass().getName();
    }

}
