package dbast.prometheus.engine.entity.systems;

import dbast.prometheus.engine.entity.components.Component;

import java.util.List;

public abstract class ComponentSystem {



    public abstract void execute(float updateDelta);

    public abstract List<Class<? extends Component>> neededComponents();
}
