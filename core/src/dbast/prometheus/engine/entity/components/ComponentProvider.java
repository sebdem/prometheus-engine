package dbast.prometheus.engine.entity.components;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface ComponentProvider {

    /*public Component getComponent(Class<? extends Component> classOf) {
        return properties.stream().filter(classOf::isInstance).findFirst().orElse(null);
    }*/
    public <CT extends Component> CT getComponent(Class<CT> classOf);

    public boolean hasComponent(Class<? extends Component> target);

    public boolean hasComponents(List<Class<? extends Component>> targets);

    public <CT extends Component> void executeFor(Class<CT> classOf, Consumer<CT> componentExecution);

}
