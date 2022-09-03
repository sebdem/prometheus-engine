package dbast.prometheus.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AstarNode <T> implements Comparable{
    public AstarNode<T> parent;
    public T reference;
    public float f;
    public float g;
    public float h;

    public AstarNode(T reference, float f) {
        this.reference = reference;
        this.f = f;
    }

    public AstarNode(T reference, float g, float h) {
        this(reference, g + h);
        this.g = g;
        this.h = h;
    }

    public AstarNode() {
    }

    // normal compare, get the cheapest route, closer to the starting point
    @Override
    public int compareTo(Object o) {
        if (o instanceof AstarNode) {
            int fCompare = compareByF(o);
            return (fCompare == 0) ? compareByG(o) : fCompare;
        }
        return 0;
    }
    public int compareByF(Object o) {
        if (o instanceof AstarNode) {
            return Float.compare(this.f, ((AstarNode) o).f);
        }
        return 0;
    }
    // sort by whose closer to the target
    public int compareByH(Object o) {
        if (o instanceof AstarNode) {
            return Float.compare(this.h, ((AstarNode) o).h);
        }
        return 0;
    }
    // sort by whose closer to the start
    public int compareByG(Object o) {
        if (o instanceof AstarNode) {
            return Float.compare(this.g, ((AstarNode) o).g);
        }
        return 0;
    }

    public String toString() {
        return getClass().getSimpleName() + String.format("@(ref: %s with distanceToPrevious: %s,  distanceToTarget: %s and f: %s)", this.reference.toString(), g, h, f);
    }

    public boolean sameAndLowerF(AstarNode<T> otherNode) {
        return this.reference.equals(otherNode.reference) && this.f < otherNode.f;
    }

    public List<AstarNode<T>> getTree() {
        ArrayList<AstarNode<T>> parents = new ArrayList<>();
        parents.add(this);
        if (this.parent != null) {
            for(AstarNode<T> parentNode = parent; parentNode != null; parentNode = parentNode.parent) {
                parents.add(parentNode);
            }
            parents.sort(AstarNode::compareByG);
        }
        Gdx.app.getApplicationLogger().log("A*System", String.format("Parents for Node %s are as follows: [%s]",
                this.reference.toString(),
                String.join(
                        "->",
                        parents.stream().map(AstarNode::toString).toArray(String[]::new)
                )
        ));
        return parents;
    }
}
