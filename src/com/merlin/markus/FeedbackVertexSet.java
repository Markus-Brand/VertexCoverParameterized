package com.merlin.markus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FeedbackVertexSet {

    private HashMap<Integer, Set<Integer>> originalGraph;

    public FeedbackVertexSet(String fileUrl) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String fileUrl = "graphs/FVS/graph-1.graph";
        FeedbackVertexSet vertexCover = new FeedbackVertexSet(fileUrl);
        ArrayList<Integer> solution = vertexCover.solve();
        for (int node: solution) {
            System.out.println(node);
        }
    }

    private ArrayList<Integer> solve() {
        for (int k = 0; k < originalGraph.size(); k++) {
            ArrayList<Integer> solution = solveWithK(k);
            if(solution != null) return solution;
        }
        return null;
    }

    private ArrayList<Integer> solveWithK(int k) {
        ArrayList<Integer> solution = new ArrayList<>();
        for(int i = 1; i <= k + 1; i++) {
            solution.add(i);
        }

        for(int graphSize = k ;graphSize < originalGraph.size() - 1; graphSize++) {
            HashMap<Integer, Set<Integer>> kPlusOneGraph = getSubGraph(originalGraph, graphSize + 1);
            solution = compress(kPlusOneGraph, solution, k);
            if(solution == null || solution.size() > k) {
                return null;
            }
        }

        return solution;
    }

    private ArrayList<Integer> compress(HashMap<Integer, Set<Integer>> kPlusOneGraph, ArrayList<Integer> oldSolution, long k) {
        if(k > 32) {
            System.out.println("Think about BigInteger!");
        }

        for(long i = 0; i < 1 << k + 1; i++) {
            long bitMask = i;
            ArrayList<Integer> X = new ArrayList<>();
            ArrayList<Integer> Y = new ArrayList<>();
            for(int position = 0; position < oldSolution.size(); position++) {
                boolean condition = bitMask % 2 == 1;
                if(condition) {
                    X.add(oldSolution.get(position));
                } else {
                    Y.add(oldSolution.get(position));
                }
                bitMask >>= 1;
            }
            HashMap<Integer, Set<Integer>> graphWithoutX = removeNodesFromGraph(kPlusOneGraph, X);
            System.out.println(X.size() + " - " + Y.size() + " - " + graphWithoutX.size());
            ArrayList<Integer> solution = solveDisjoint(graphWithoutX, Y, k - X.size() + 1);
            if(solution != null) {
                return solution;
            }
        }
        return null;
    }

    private ArrayList<Integer> solveDisjoint(HashMap<Integer, Set<Integer>> graphWithoutX, ArrayList<Integer> y, long l) {
        ArrayList<Integer> totalOrderInY = getTotalOrder(graphWithoutX, y);
        if (totalOrderInY == null) return null;
        List<Integer> x = graphWithoutX.keySet().stream().filter(key -> !y.contains(key)).collect(Collectors.toList());
        ArrayList<Integer> totalOrderInX = getTotalOrder(graphWithoutX, new ArrayList<>(x));
        if (totalOrderInX == null) return null;
        return null;
    }

    private ArrayList<Integer> getTotalOrder(HashMap<Integer, Set<Integer>> graphWithoutX, ArrayList<Integer> y) {
        HashMap<Integer, Integer> outDegreeY = new HashMap<>();
        for(int node : y) {
            Set<Integer> neighbours = graphWithoutX.get(node);
            outDegreeY.put(node, neighbours.parallelStream().filter(y::contains).collect(Collectors.toSet()).size());
        }
        ArrayList<Integer> totalOrderInY = new ArrayList<>();
        while(!outDegreeY.isEmpty()) {
            boolean foundSomething = false;
            for(int neighbour : outDegreeY.keySet()){
                if(outDegreeY.get(neighbour) == 0) {
                    foundSomething = true;
                    totalOrderInY.add(neighbour);
                    for (int neighbour2 : outDegreeY.keySet()) {
                        if (graphWithoutX.get(neighbour2).contains(neighbour)) {
                            outDegreeY.put(neighbour2, outDegreeY.get(neighbour2) - 1);
                        }
                    }
                    outDegreeY.remove(neighbour);
                    break;
                }
            }
            if(!foundSomething) {
                return null;
            }
        }
        return totalOrderInY;
    }

    private HashMap<Integer, Set<Integer>> removeNodesFromGraph(HashMap<Integer, Set<Integer>> kPlusOneGraph, ArrayList<Integer> x) {
        HashMap<Integer, Set<Integer>> newGraph = new HashMap<Integer, Set<Integer>>(kPlusOneGraph);
        for (int node : x) {
            newGraph.remove(node);
        }
        for(Set<Integer> connectedNodes : newGraph.values()) {
            for (int node : x) {
                connectedNodes.remove(node);
            }
        }

        return newGraph;
    }

    private HashMap<Integer,Set<Integer>> getSubGraph(HashMap<Integer, Set<Integer>> originalGraph, int size) {
        HashMap<Integer, Set<Integer>> subGraph = new HashMap<>();
        for(int i = 1; i <= size; i++) {
            Set<Integer> node = originalGraph.get(i).stream().filter(neighbour -> neighbour <= size).collect(Collectors.toSet());
            subGraph.put(i, node);
        }
        return subGraph;
    }

}
