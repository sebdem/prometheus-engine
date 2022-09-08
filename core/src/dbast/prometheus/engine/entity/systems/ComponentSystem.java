package dbast.prometheus.engine.entity.systems;

import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.Component;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class ComponentSystem {

    public List<Entity> onlyQualified(List<Entity> entities) {
        final List<Class<? extends Component>> components = neededComponents();
        return entities.stream().filter(entity -> entity.hasComponents(components)).collect(Collectors.toList());
    }

    /**
     * never call this without making sure the list is onlyQualified !!!!!
     * @param updateDelta w
     * @param qualifiedEntities w
     */
    public abstract void execute(float updateDelta, List<Entity> qualifiedEntities);

    public abstract List<Class<? extends Component>> neededComponents();
}
