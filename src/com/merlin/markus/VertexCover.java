package com.merlin.markus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VertexCover {

    private HashMap<Integer, Set<Integer>> originalGraph;

    public VertexCover(String fileUrl) {
        File file = new File(fileUrl);
        originalGraph = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String st;
            st = br.readLine();
            String[] nAndM = st.split(" ");
            int n = Integer.parseInt(nAndM[0]);
            for(int i = 1; i <= n; i++) {
                originalGraph.put(i, new HashSet<>());
            }

            while ((st = br.readLine()) != null) {
                String[] edge = st.split(" ");
                int n1 = Integer.parseInt(edge[0]);
                int n2 = Integer.parseInt(edge[1]);
                originalGraph.get(n1).add(n2);
                originalGraph.get(n2).add(n1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int node : originalGraph.keySet()) {
            if (originalGraph.get(node).isEmpty()) {
                originalGraph.remove(node);
            }
        }
        System.out.println("lol");
    }

    public static void main(String[] args) {
        String fileUrl = "graphs/graph-2.graph";
        VertexCover vertexCover = new VertexCover(fileUrl);
        Set<Integer> solution = vertexCover.solve();
        System.out.println(solution.size());
        for (int node: solution) {
            System.out.println(node);
        }
    }

    private void addNodesToSolution(Iterable<Integer> vertices, Set<Integer> solution, Map<Integer, Set<Integer>> graph) {
        for (int vertex: vertices) {
            addNodeToSolution(vertex, solution, graph);
        }
    }

    private void addNodesToSolution(Integer[] vertices, Set<Integer> solution, Map<Integer, Set<Integer>> graph) {
        for (int vertex: vertices) {
            addNodeToSolution(vertex, solution, graph);
        }
    }

    private void addNodeToSolution(Integer vertex, Set<Integer> solution, Map<Integer, Set<Integer>> graph) {
        solution.add(vertex);
        for (Integer neighbour: graph.get(vertex)) {
            graph.get(neighbour).remove(vertex);
        }
        graph.entrySet().removeIf(e -> e.getValue().isEmpty());
        graph.remove(vertex);
    }

    private int getNodesWithOptimalDegree(Map<Integer, Set<Integer>> graph) {
        int someNode = graph.keySet().iterator().next();
        int minDegree = graph.get(someNode).size();
        int minNode = someNode;
        int maxDegree = graph.get(someNode).size();
        int maxNode = someNode;
        for (Map.Entry<Integer, Set<Integer>> entry: graph.entrySet()) {
            int degree = entry.getValue().size();
            if (degree < minDegree) {
                minDegree = degree;
                minNode = entry.getKey();
            }
            if (degree > maxDegree) {
                maxDegree = degree;
                maxNode = entry.getKey();
            }
        }
        if (minDegree < 4) {
            return minNode;
        }
        return maxNode;
    }

    private Set<Integer> solve() {
        for (int k = 1; k <= originalGraph.size(); k++) {
            System.out.println("lol " + k);
            Set<Integer> result = solveWith(copyGraph(originalGraph), k, new HashSet<>());
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private boolean areNeigbours(int nodeA, int nodeB, HashMap<Integer, Set<Integer>> graph) {
        return graph.get(nodeA).contains(nodeB);
    }

    private Set<Integer> solveWith(HashMap<Integer, Set<Integer>> graph, int k, Set<Integer> solution) {
        System.out.flush();
        if(k < 0) return null;
        if(graph.isEmpty()) return solution;

        int nextNode = getNodesWithOptimalDegree(graph);
        Integer[] neighbours = new Integer[graph.get(nextNode).size()];
        graph.get(nextNode).toArray(neighbours);
        switch (neighbours.length) {
            case 1:
                addNodeToSolution(neighbours[0], solution, graph);
                return solveWith(graph, k - 1, solution);
            case 2:
                if (areNeigbours(neighbours[0], neighbours[1], graph)) {
                    addNodeToSolution(neighbours[0], solution, graph);
                    addNodeToSolution(neighbours[1], solution, graph);
                    return solveWith(graph, k - 2, solution);
                } else {
                    Set<Integer> abNeighbours = new HashSet<>(graph.get(neighbours[0]));
                    abNeighbours.addAll(graph.get(neighbours[1]));

                    if (abNeighbours.size() == 2) {
                        addNodesToSolution(abNeighbours, solution, graph);
                        return solveWith(graph, k - 2, solution);
                    } else {
                        HashMap<Integer, Set<Integer>> graphCopy = copyGraph(graph);
                        Set<Integer> solutionCopy = new HashSet<>(solution);

                        addNodesToSolution(abNeighbours, solutionCopy, graphCopy);
                        Set<Integer> result = solveWith(graphCopy, k - abNeighbours.size(), solutionCopy);
                        if(result != null) {
                            return result;
                        }
                        addNodeToSolution(neighbours[0], solution, graph);
                        addNodeToSolution(neighbours[1], solution, graph);
                        return solveWith(graph, k - 2, solution);
                    }
                }
            case 3:
                Set<Integer> abcNeighbours = new HashSet<>(graph.get(neighbours[0]));
                abcNeighbours.addAll(graph.get(neighbours[1]));
                abcNeighbours.addAll(graph.get(neighbours[2]));

                if(abcNeighbours.size() == graph.get(neighbours[0]).size() + graph.get(neighbours[1]).size() + graph.get(neighbours[2]).size() - 2) {
                    HashMap<Integer, Set<Integer>> graphCopy = copyGraph(graph);
                    Set<Integer> solutionCopy = new HashSet<>(solution);

                    Set<Integer> bcNeighboursAndA = new HashSet<>(graph.get(neighbours[1]));
                    bcNeighboursAndA.addAll(graph.get(neighbours[2]));
                    bcNeighboursAndA.add(neighbours[0]);
                    addNodesToSolution(bcNeighboursAndA, solutionCopy, graphCopy);
                    Set<Integer> result = solveWith(graphCopy, k - bcNeighboursAndA.size(), solutionCopy);
                    if(result != null) {
                        return result;
                    }

                    graphCopy = copyGraph(graph);
                    solutionCopy = new HashSet<>(solution);
                    addNodesToSolution(graph.get(neighbours[0]), solutionCopy, graphCopy);
                    result = solveWith(graphCopy, k - graph.get(neighbours[0]).size(), solutionCopy);
                    if(result != null) {
                        return result;
                    }

                    addNodesToSolution(neighbours, solution, graph);
                    return solveWith(graph, k - neighbours.length, solution);
                } else {
                    int aNode = 0, bNode = 0, cNode = 0;
                    if (areNeigbours(neighbours[0], neighbours[1], graph)) {
                        aNode = neighbours[0];
                        bNode = neighbours[1];
                        cNode = neighbours[2];
                    } else if (areNeigbours(neighbours[1], neighbours[2], graph)) {
                        aNode = neighbours[1];
                        bNode = neighbours[2];
                        cNode = neighbours[0];
                    } else if (areNeigbours(neighbours[0], neighbours[2], graph)) {
                        aNode = neighbours[0];
                        bNode = neighbours[2];
                        cNode = neighbours[1];
                    }

                    if (aNode * bNode * cNode != 0) {
                        HashMap<Integer, Set<Integer>> graphCopy = copyGraph(graph);
                        Set<Integer> solutionCopy = new HashSet<>(solution);

                        addNodesToSolution(graph.get(cNode), solutionCopy, graphCopy);
                        Set<Integer> result = solveWith(graphCopy, k - graph.get(cNode).size(), solutionCopy);
                        if(result != null) {
                            return result;
                        }

                        addNodesToSolution(neighbours, solution, graph);
                        return solveWith(graph, k - neighbours.length, solution);
                    }

                    HashMap<Integer, Set<Integer>> graphCopy = copyGraph(graph);
                    Set<Integer> solutionCopy = new HashSet<>(solution);

                    addNodesToSolution(neighbours, solutionCopy, graphCopy);
                    Set<Integer> result = solveWith(graphCopy, k - neighbours.length, solutionCopy);
                    if(result != null) {
                        return result;
                    }
                    if (haveTwoCommonNeigbours(neighbours[0], neighbours[1], graph)) {
                        aNode = neighbours[0];
                        bNode = neighbours[1];
                    } else if (haveTwoCommonNeigbours(neighbours[1], neighbours[2], graph)) {
                        aNode = neighbours[1];
                        bNode = neighbours[2];
                    } else if (haveTwoCommonNeigbours(neighbours[0], neighbours[2], graph)) {
                        aNode = neighbours[0];
                        bNode = neighbours[2];
                    }
                    int commonNeighbour = getCommonNeighbour(aNode, bNode, nextNode, graph);
                    addNodeToSolution(nextNode, solution, graph);
                    addNodeToSolution(commonNeighbour, solution, graph);
                    return solveWith(graph, k - 2, solution);

                }
            default:
                HashMap<Integer, Set<Integer>> graphCopy = copyGraph(graph);
                Set<Integer> solutionCopy = new HashSet<>(solution);

                addNodesToSolution(neighbours, solutionCopy, graphCopy);
                Set<Integer> result = solveWith(graphCopy, k - neighbours.length, solutionCopy);
                if(result != null) {
                    return result;
                }
                addNodeToSolution(nextNode, solution, graph);
                return solveWith(graph, k - 1, solution);
        }
    }

    private int getCommonNeighbour(Integer a, Integer b, int v, HashMap<Integer, Set<Integer>> graph) {
        for (int aNeighbour: graph.get(a)) {
            if(aNeighbour != v && graph.get(b).contains(aNeighbour)) {
                return aNeighbour;
            }
        }
        return 0;
    }

    private boolean haveTwoCommonNeigbours(Integer a, Integer b, HashMap<Integer, Set<Integer>> graph) {
        Set<Integer> abNeighbours = new HashSet<>(graph.get(a));
        abNeighbours.addAll(graph.get(b));

        return graph.get(a).size() + graph.get(b).size() - 2 >= abNeighbours.size();
    }

    public static HashMap<Integer, Set<Integer>> copyGraph(HashMap<Integer, Set<Integer>> original)
    {
        HashMap<Integer, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : original.entrySet())
        {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }
}
