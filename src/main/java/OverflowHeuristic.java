public abstract class OverflowHeuristic {
    protected RTree node;

    public OverflowHeuristic(RTree node) {
        this.node = node;
    }

    public abstract void divideTree();
}
