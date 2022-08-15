package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.utils.AstarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerationUtils {

    public static Vector3[] nearby8Of(Vector3 center) {
        return new Vector3[]{
                center.cpy().add(1, -1, 0),
                center.cpy().add(0, -1, 0),
                center.cpy().add(-1, -1, 0),
                center.cpy().add(-1, 0, 0),
                center.cpy().add(1, 0, 0),
                center.cpy().add(1, 1, 0),
                center.cpy().add(0, 1, 0),
                center.cpy().add(-1, 1, 0)
        };
    }
    public static Vector3[] nearby8Of(Vector3 center, float offset) {
        return new Vector3[]{
                center.cpy().add(offset, -offset, 0),
                center.cpy().add(0, -offset, 0),
                center.cpy().add(-offset, -offset, 0),
                center.cpy().add(-offset, 0, 0),
                center.cpy().add(offset, 0, 0),
                center.cpy().add(offset, offset, 0),
                center.cpy().add(0, offset, 0),
                center.cpy().add(-offset, offset, 0)
        };
    }
    public static Vector3[] nearby4Of(Vector3 center) {
        return new Vector3[]{
                center.cpy().add(0, 1, 0),
                center.cpy().add(0, -1, 0),
                center.cpy().add(1, 0, 0),
                center.cpy().add(-1, 0, 0)

        };
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint) {
        return findPath(startPoint, endPoint, 1f, (value) -> true);
    }
    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float step) {
        return findPath(startPoint, endPoint, step, (value) -> true);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, Function<Vector3, Boolean> stepValidation) {
        return findPath(startPoint, endPoint, 1f, stepValidation);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float steps, Function<Vector3, Boolean> stepValidation) {
        SortedSet<AstarNode<Vector3>> openList = new TreeSet<>(AstarNode::compareTo);
        List<AstarNode<Vector3>> closedList = new ArrayList<>();

        openList.add(new AstarNode<>(startPoint, 0f, 0f));

        AstarNode<Vector3> endNode = new AstarNode<>(endPoint, 0f, 0f);

        boolean targetFound = false;
        while(!(openList.isEmpty() || targetFound)) {
            AstarNode<Vector3> qNode = qNode = openList.first();
            openList.remove(qNode);

            Vector3[] successors = GenerationUtils.nearby8Of(qNode.reference, steps);

            for (Vector3 sucVec: successors) {

                if (!targetFound && stepValidation.apply(sucVec)) {
                    if (sucVec.equals(endPoint)) {
                        targetFound = true;
                        endNode.parent = qNode;
                    } else {
                        AstarNode<Vector3> sucNode = new AstarNode<>(sucVec,
                                qNode.g + sucVec.dst(qNode.reference),
                                endPoint.dst(sucVec)
                        );
                        sucNode.parent = qNode;

                        if (openList.stream().anyMatch(node->node.sameAndLowerF(sucNode))) {
                            continue;
                        }
                        if (closedList.stream().anyMatch(node->node.sameAndLowerF(sucNode))) {
                            continue;
                        } else {
                            openList.add(sucNode);
                        }
                    }
                }
            }
            closedList.add(qNode);
        }
        return  endNode.getParents().stream().map(node -> node.reference).collect(Collectors.toList());
    }
}
