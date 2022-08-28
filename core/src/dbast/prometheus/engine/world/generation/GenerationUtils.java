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

    public static interface GetNearby extends Function<Vector3, Vector3[]>{
        default Vector3[] apply(Vector3 vector3) {
            return this.apply(vector3, 1f);
        }
        Vector3[] apply(Vector3 t, float offset);
    }

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
    public static Vector3[] nearby4Of(Vector3 center, float offset) {
        return new Vector3[]{
                center.cpy().add(0, offset, 0),
                center.cpy().add(0, -offset, 0),
                center.cpy().add(-offset, 0, 0),
                center.cpy().add(offset, 0, 0),
        };
    }

    public static Vector3[] nearby6Of(Vector3 center, float offset) {
        return new Vector3[]{
                center.cpy().add(0, offset, 0),
                center.cpy().add(0, -offset, 0),
                center.cpy().add(-offset, 0, 0),
                center.cpy().add(offset, 0, 0),
                center.cpy().add(0, 0, offset),
                center.cpy().add(0, 0, -offset),
        };
    }
    public static Vector3[] nearby18Of(Vector3 center, float offset) {
        return new Vector3[]{
                center.cpy().add(0, offset, 0),
                center.cpy().add(0, -offset, 0),
                center.cpy().add(offset, 0, 0),
                center.cpy().add(offset, offset, 0),
                center.cpy().add(offset, -offset, 0),
                center.cpy().add(-offset, offset, 0),
                center.cpy().add(-offset, -offset, 0),
                center.cpy().add(-offset, 0, 0),
                center.cpy().add(-offset, 0, offset),
                center.cpy().add(-offset, 0, -offset),
                center.cpy().add(0, 0, offset),
                center.cpy().add(0, 0, -offset),
                center.cpy().add(offset, 0, offset),
                center.cpy().add(offset, 0, -offset),
                center.cpy().add(0, -offset, offset),
                center.cpy().add(0, -offset, -offset),
                center.cpy().add(0, offset, offset),
                center.cpy().add(0, offset, -offset)
        };
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint) {
        return findPath(startPoint, endPoint, 1f, (value) -> true, GenerationUtils::nearby8Of);
    }
    public static List<Vector3> find3DPath(Vector3 startPoint, Vector3 endPoint, Function<Vector3, Boolean> stepValidation) {
        return findPath(startPoint, endPoint, 1f, stepValidation, GenerationUtils::nearby4Of);
       // return findPath(startPoint, endPoint, 1f, stepValidation, GenerationUtils::nearby18Of);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float step) {
        return findPath(startPoint, endPoint, step, (value) -> true, GenerationUtils::nearby8Of);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float steps, Function<Vector3, Boolean> stepValidation) {
        return findPath(startPoint, endPoint, steps, stepValidation, GenerationUtils::nearby8Of);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float steps, Function<Vector3, Boolean> stepValidation, GetNearby successorGet) {
        SortedSet<AstarNode<Vector3>> openList = new TreeSet<>(AstarNode::compareByF);
        List<AstarNode<Vector3>> closedList = new ArrayList<>();

        AstarNode<Vector3> startNode = new AstarNode<>(startPoint, 0f, 0f);
        openList.add(startNode);

        AstarNode<Vector3> endNode = new AstarNode<>(endPoint, Float.MAX_VALUE, Float.MAX_VALUE);

        boolean targetFound = false;

        while(!(openList.isEmpty() || targetFound)) {
            AstarNode<Vector3> qNode = openList.first();
            openList.remove(qNode);

            // TODO test what happens if we add to Z
            Vector3[] successors = successorGet.apply(qNode.reference, steps);

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
                            openList.add(sucNode);//openList.add(sucNode);
                        }
                    }
                }
            }
            closedList.add(qNode);
            // no more successors available
            if (!targetFound && openList.isEmpty()) {
                endNode = closedList.stream().filter(node->!node.equals(startNode)).min(AstarNode::compareByH).get();
            }
        }
        return  endNode.getTree().stream().map(node -> node.reference).collect(Collectors.toList());
    }
}
