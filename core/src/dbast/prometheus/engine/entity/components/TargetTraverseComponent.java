package dbast.prometheus.engine.entity.components;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.engine.world.generation.GenerationUtils;

import java.util.List;

public class TargetTraverseComponent extends Component{

    public List<Vector3> path;
    public Vector3 previousTarget;
    public Vector3 currentTarget;
    public Vector3 finalTarget;

 //   public boolean notReachable = false;

    public void nextTarget() {
        this.previousTarget = path.remove(0).cpy();
        if (!path.isEmpty()) {
            currentTarget = path.get(0);
        } else {
            currentTarget = null;
        }
    }

    public void reachedTarget() {
        this.path = null;
        this.currentTarget = null;
        this.finalTarget = null;
    }
}
