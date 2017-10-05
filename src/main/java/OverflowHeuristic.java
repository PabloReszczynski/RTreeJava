import java.io.IOException;

public interface OverflowHeuristic {
    void divideTree(int nodeId) throws IOException, ClassNotFoundException;
    String toString();
}
