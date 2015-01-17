package clodiku.pathfinding;

/**
 * Created by dave on 1/17/15.
 */
public class AStar {

    static public class Node {

        public int x;
        public int y;
        public Node parent;

        public Node (int x, int y) {
            this.x = x;
            this.y = y;
            this.parent = null;
        }

        public Node (int x, int y, Node parent) {
            this(x,y);
            this.parent = parent;
        }

    }

    public static Node[] findPath(int[][] grid, Node start, Node goal) {

        System.out.println(grid.length);

        return new Node[]{};
    }
}
