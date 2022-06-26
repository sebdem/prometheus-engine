package dbast.prometheus.engine.entity.systems;

import dbast.prometheus.engine.entity.Entity;
import dbast.prometheus.engine.entity.components.CollisionBox;
import dbast.prometheus.engine.entity.components.Component;
import dbast.prometheus.engine.entity.components.StateComponent;

import java.util.Collections;
import java.util.List;

public class StateUpdateSystem extends ComponentSystem {

    @Override
    public void execute(float updateDelta, List<Entity> qualifiedEntities) {
        for(Entity entity : qualifiedEntities) {
            StateComponent entityState = entity.getComponent(StateComponent.class);

            entityState.setState("");
            CollisionBox collisionComponent = entity.getComponent(CollisionBox.class);
            if (collisionComponent != null) {
                //  Gdx.app.getApplicationLogger().log("Collision System:", String.format("update for entity %s state; new state is%s colliding", entityA.getId(), isColliding ? "" : " not"));
                if (collisionComponent.isColliding()) {
                    entityState.setState("colliding");
                }
            }

        };
    }

    @Override
    public List<Class<? extends Component>> neededComponents() {
        return Collections.singletonList(StateComponent.class);
    }
}
