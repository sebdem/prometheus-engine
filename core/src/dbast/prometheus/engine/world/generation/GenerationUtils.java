package dbast.prometheus.engine.world.generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import dbast.prometheus.utils.AstarNode;

import java.util.*;
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

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint,  Function<Vector3, Boolean> boundaryValidation) {
        return findPath(startPoint, endPoint, 1f, boundaryValidation, (value) -> true, GenerationUtils::nearby8Of);
    }
    public static List<Vector3> find3DPath(Vector3 startPoint, Vector3 endPoint, Function<Vector3, Boolean> boundaryValidation, Function<Vector3, Boolean> stepValidation) {
        return findPath(startPoint, endPoint, 1f, boundaryValidation, stepValidation, GenerationUtils::nearby4Of);
       // return findPath(startPoint, endPoint, 1f, stepValidation, GenerationUtils::nearby18Of);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float step) {
        return findPath(startPoint, endPoint, step, (value) -> true, (value) -> true, GenerationUtils::nearby8Of);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float steps, Function<Vector3, Boolean> boundaryValidation, Function<Vector3, Boolean> stepValidation) {
        return findPath(startPoint, endPoint, steps, boundaryValidation, stepValidation, GenerationUtils::nearby8Of);
    }

    public static List<Vector3> findPath(Vector3 startPoint, Vector3 endPoint, float steps, Function<Vector3, Boolean> boundaryValidaiton, Function<Vector3, Boolean> stepValidation, GetNearby successorGet) {
        SortedSet<AstarNode<Vector3>> openList = new TreeSet<>(AstarNode::compareByF);
        List<AstarNode<Vector3>> closedList = new ArrayList<>();

        AstarNode<Vector3> startNode = new AstarNode<>(startPoint, 0f, 0f);
        AstarNode<Vector3> endNode = new AstarNode<>(endPoint, Float.MAX_VALUE, Float.MAX_VALUE);
        openList.add(startNode);

        boolean targetFound = false;
        while(!(openList.isEmpty() || targetFound)) {
/*
            Gdx.app.getApplicationLogger().log("A*", String.format("- openList currently contains %s items | closedList %s items", openList.size(), closedList.size()));
*/

            AstarNode<Vector3> qNode = openList.first();
            openList.remove(qNode);
            closedList.add(qNode);

            // TODO test what happens if we add to Z
            Vector3[] successors = successorGet.apply(qNode.reference, steps);

            for (Vector3 sucVec: successors) {
                // TODO optimization 1: Is in valid boundaries instead of the stepValidation
                if (!targetFound && boundaryValidaiton.apply(sucVec)) {
                    if (sucVec.equals(endPoint)) {
                        targetFound = true;
                        endNode.parent = qNode;
                    } else {
                        AstarNode<Vector3> sucNode = new AstarNode<>(sucVec,
                                qNode.g + sucVec.dst(qNode.reference),
                                endPoint.dst(sucVec)
                        );
                        sucNode.parent = qNode;
                        // TODO optimization 2: This can be optimized a bit and should contain stepValidation

                        if (closedList.stream().anyMatch(node->node.sameAndLowerF(sucNode))) {
                        } else if (stepValidation.apply(sucVec)) {
                            if (openList.stream().anyMatch(node->node.sameAndLowerF(sucNode))) {
                            } else {
/*
                                Gdx.app.getApplicationLogger().log("A*", String.format("Successor %s to origin %s added to openList", sucNode.toString(), qNode.toString()));
*/
                                openList.add(sucNode);//openList.add(sucNode);
                            }
                        } /*else {
                            Gdx.app.getApplicationLogger().log("A*", String.format("Successor %s from origin %s is not valid per stepValidation", sucVec.toString(), qNode.toString()));
                        }*/
                    }
                } /*else if(!targetFound) {
                    Gdx.app.getApplicationLogger().log("A*", String.format("Successor %s from origin %s is not valid per boundary", sucVec.toString(), qNode.toString()));
                }*/
            }
           /* if (!targetFound) {
                Gdx.app.getApplicationLogger().log("A*", String.format("--- Target not yet found"));
            }*/
        }
        // no more successors available
        if (!targetFound) {
            endNode = closedList.stream().filter(node->!node.equals(startNode)).min(AstarNode::compareByH).orElse(startNode);
        }
        return  endNode.getTree().stream().map(node -> node.reference).collect(Collectors.toList());
    }
}
