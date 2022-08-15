package dbast.prometheus.utils;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public int compareTo(Object o) {
        if (o instanceof AstarNode) {
            return Float.compare(this.f, ((AstarNode) o).f);
        }
        return 0;
    }

    public boolean sameAndLowerF(AstarNode<T> otherNode) {
        return this.reference.equals(otherNode.reference) && this.f < otherNode.f;
    }

    public List<AstarNode<T>> getParents() {
        ArrayList<AstarNode<T>> parents = new ArrayList<>();
        if (this.parent != null) {
            for(AstarNode<T> parentNode = parent; parentNode != null; parentNode = parentNode.parent) {
                parents.add(parentNode);
            }
            parents.sort(AstarNode::compareTo);
        }
        return parents;
    }
}
