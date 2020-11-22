package dbast.prometheus.engine.entity.systems;

import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.CollisionBox;
import dbast.prometheus.engine.entity.components.Component;
import dbast.prometheus.engine.entity.components.PositionComponent;

import java.util.Arrays;
import java.util.List;

public class CollisionDetectionSystem extends ComponentSystem {
    @Override
    public void execute(float updateDelta) {
        Entity test = null;
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Arrays.asList(CollisionBox.class, PositionComponent.class);
    }
}
