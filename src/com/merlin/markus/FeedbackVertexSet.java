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
       /*Integer[] a = {1, 2, 3, 4, 5};
        Integer[] b = {1, 3, 2, 5, 4};
        ArrayList<Integer> lcs = this.LCS(new ArrayList<>(Arrays.asList(a)), new ArrayList<>(Arrays.asList(b)));
        System.out.println(lcs.size());*/

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
            System.out.println("Solution" + node);
        }
    }


    private ArrayList<Integer> solve() {
        int k = 0;
        ArrayList<Integer> solution = new ArrayList<>();

        for(int graphSize = k; graphSize < originalGraph.size(); graphSize++) {
            System.out.println("--------------- graphSize:" + graphSize);
            HashMap<Integer, Set<Integer>> kPlusOneGraph = getSubGraph(originalGraph, graphSize + 1);
            solution.add(graphSize + 1);
            if(solution.size() <= k) continue;
            ArrayList<Integer> newSolution = compress(kPlusOneGraph, solution, k);
            if(newSolution == null) {
                k++;
                System.out.println("--------------- K:" + k);
            } else {
                solution = newSolution;
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
            ArrayList<Integer> solution = solveDisjoint(graphWithoutX, Y, Y.size() - 1);
            if(solution != null) {
                solution.addAll(X);
                System.out.println(X.size() + " - " + Y.size() + " - " + graphWithoutX.size() + " - " + kPlusOneGraph.size() + " - " + oldSolution.size() + " - " + solution.size());
                return solution;
            }
        }
        return null;
    }

    private ArrayList<Integer> solveDisjoint(HashMap<Integer, Set<Integer>> graphWithoutX, ArrayList<Integer> y, long k) {
        ArrayList<Integer> totalOrderInY = getTotalOrder(graphWithoutX, y);
        if (totalOrderInY == null) return null;
        List<Integer> x = graphWithoutX.keySet().stream().filter(key -> !y.contains(key)).collect(Collectors.toList());
        ArrayList<Integer> totalOrderInX = getTotalOrder(graphWithoutX, new ArrayList<>(x));
        if (totalOrderInX == null) return null;

        ArrayList<Integer> solution = new ArrayList<>();
        for(int node : totalOrderInX) {
            if(isCyclic(node, totalOrderInY, graphWithoutX)) {
                solution.add(node);
            }
        }

        if(solution.size() > k) return null;

        totalOrderInX.removeAll(solution);

        //Einsortieren
        ArrayList<Integer> orderCopy = new ArrayList<>(totalOrderInX);
        ArrayList<Integer> order2 = new ArrayList<>();

        for(int yNode : totalOrderInY) {
            for (int i = 0; i < orderCopy.size(); i++) {
                int xNode = orderCopy.get(i);
                if(graphWithoutX.get(xNode).contains(yNode)) {
                    order2.add(xNode);
                    orderCopy.remove(i);
                    i--;
                }
            }
        }
        order2.addAll(orderCopy);
        //LCS

        ArrayList<Integer> lcs = LCS(new ArrayList<>(totalOrderInX), new ArrayList<>(order2));
        for(int node : totalOrderInX) {
            if(!lcs.contains(node)) {
                solution.add(node);
            }
        }

        System.out.println("post" + solution.size());
        if(solution.size() <= k) {
            return solution;
        }
        return null;
    }

    private ArrayList<Integer> LCS(ArrayList<Integer> a, ArrayList<Integer> b) {
        ArrayList<Integer>[] lastRow = new ArrayList[a.size()+1];
        ArrayList<Integer>[] currentRow = new ArrayList[a.size()+1];

        for (int i = 0; i <= b.size(); i++) {
            lastRow[i] = new ArrayList<Integer>();
        }

        for (int i = 0; i < a.size(); i++) {
            currentRow[0] = new ArrayList<Integer>();
            for (int j = 1; j < b.size() + 1; j++) {
                if(a.get(i).equals(b.get(j - 1))) {
                    lastRow[j-1].add(a.get(i));
                    currentRow[j] = lastRow[j-1];
                } else {
                    ArrayList<Integer> commonString;
                    if(lastRow[j].size() > currentRow[j-1].size()) {
                        commonString = new ArrayList<>(lastRow[j]);
                    } else {
                        commonString = new ArrayList<>(currentRow[j-1]);
                    }
                    currentRow[j] = commonString;
                }
            }
            lastRow = currentRow;
            currentRow = new ArrayList[a.size()+1];
        }
        return lastRow[a.size()];

        /*
        if(a.isEmpty() || b.isEmpty()) return new ArrayList<>();
        int lastA = a.get(a.size() - 1);
        int lastB = b.get(b.size() - 1);
        if(lastA == lastB) {
            a.remove(a.size() - 1);
            b.remove(b.size() - 1);
            ArrayList<Integer> solution = LCS(a, b);
            solution.add(lastA);
            return solution;
        } else {
            ArrayList<Integer> aCopy = new ArrayList<>(a);
            a.remove(a.size() - 1);
            ArrayList<Integer> bCopy = new ArrayList<>(b);
            b.remove(b.size() - 1);

            ArrayList<Integer> solution1 = LCS(aCopy, b);
            ArrayList<Integer> solution2 = LCS(a, bCopy);
            return solution1.size() > solution2.size() ? solution1 : solution2;
        }
        */
    }

    private boolean isCyclic(int node, ArrayList<Integer> totalOrderInY, HashMap<Integer, Set<Integer>> graphWithoutX) {
        OptionalInt minOutgoingPosition = graphWithoutX.get(node).stream().map(totalOrderInY::indexOf).filter((index) -> index >= 0).mapToInt((lol) -> lol).min();
        OptionalInt maxIncomingPosition = totalOrderInY.stream().filter(n -> graphWithoutX.get(n).contains(node)).map(totalOrderInY::indexOf).mapToInt((lol) -> lol).max();
        if(!minOutgoingPosition.isPresent() || !maxIncomingPosition.isPresent()) return false;
        return maxIncomingPosition.getAsInt() > minOutgoingPosition.getAsInt();
    }

    private ArrayList<Integer> getTotalOrder(HashMap<Integer, Set<Integer>> graphWithoutX, ArrayList<Integer> y) {
        HashMap<Integer, Integer> outDegreeY = new HashMap<>();
        for(int node : y) {
            Set<Integer> neighbours = graphWithoutX.get(node);
            outDegreeY.put(node, (int) neighbours.stream().filter(y::contains).count());
        }
        ArrayList<Integer> totalOrderInY = new ArrayList<>();
        while(!outDegreeY.isEmpty()) {
            boolean foundSomething = false;
            for(int neighbour : outDegreeY.keySet()){
                if(outDegreeY.get(neighbour) == 0) {
                    foundSomething = true;
                    totalOrderInY.add(0, neighbour);
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
        HashMap<Integer, Set<Integer>> newGraph = new HashMap<>(kPlusOneGraph);
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
