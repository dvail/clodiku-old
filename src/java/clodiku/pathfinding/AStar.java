package clodiku.pathfinding;

import java.util.*;

/**
 * Created by dave on 1/17/15.
 */
public class AStar {

    public static int manhattenDistance(Node a, Node b) {

        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);

    }

    public static List<Node> getNeighbors(int[][] grid, Node parent, Node goal) {
        List<Node> neighbors = new ArrayList<>(4);

        int xLeft = parent.x - 1, xRight = parent.x + 1;
        int yUp = parent.y - 1, yDown = parent.y + 1;

        // Check if each potential neighbor is within the grid bounds, and does not have a value < 0 (impassable)
        if (xLeft > -1 && grid[xLeft][parent.y] > -1) {
            neighbors.add(new Node(xLeft, parent.y, grid[xLeft][parent.y], parent, goal));
        }

        if (xRight < grid.length && grid[xRight][parent.y] > -1) {
            neighbors.add(new Node(xRight, parent.y, grid[xRight][parent.y], parent, goal));
        }

        if (yUp > -1  && grid[parent.x][yUp] > -1) {
            neighbors.add(new Node(parent.x, yUp, grid[parent.x][yUp], parent, goal));
        }

        if (yDown < grid[0].length && grid[parent.x][yDown] > -1) {
            neighbors.add(new Node(parent.x, yDown, grid[parent.x][yDown], parent, goal));
        }

        return neighbors;
    }

    public static Node[] findPath(int[][] grid, Node start, Node goal) {

        final Comparator<Node> comparator = new Comparator<Node>() {

            @Override
            public int compare(Node o, Node t1) {
                int comparatorValue;

                if (o.cost < t1.cost) {
                    comparatorValue = -1;
                } else if (o.cost > t1.cost) {
                    comparatorValue = 1;
                } else {
                    comparatorValue = 0;
                }

                return comparatorValue;
            }

            @Override
            public boolean equals(Object o) {

                return false;
            }
        };

        Node currentNode;
        List<Node> neighbors;
        PriorityQueue<Node> openNodes = new PriorityQueue<>(5, comparator);
        HashSet<Node> closedNodes = new HashSet<>();

        openNodes.add(start);

        while (!openNodes.isEmpty()) {

            currentNode = openNodes.poll();
            closedNodes.add(currentNode);

            neighbors = getNeighbors(grid, currentNode, goal);

            for (Node node : neighbors) {

                if (openNodes.contains(node)) {

                    // TODO Update cost value here for more efficient routes

                } else if (!closedNodes.contains(node)) {

                    openNodes.add(node);
                }

            }

            if (currentNode.equals(goal)) {

                ArrayList<Node> path = new ArrayList<>();

                while (currentNode.parent != null) {
                    path.add(currentNode);
                    currentNode = currentNode.parent;
                }

                return path.toArray(new Node[path.size()]); // Path is found
            }

        }

        return new Node[]{start}; // No path, return the starting node
    }

    static public class Node {

        public int x;
        public int y;
        public int cost;
        public Node parent;

        public Node (int x, int y) {
            this.x = x;
            this.y = y;
            this.cost = 0;
            this.parent = null;
        }

        public Node (int x, int y, int cost, Node parent, Node goal) {
            this(x,y);
            this.cost = cost + parent.cost + manhattenDistance(this, goal);
            this.parent = parent;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            return x == node.x && y == node.y;

        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }

}
