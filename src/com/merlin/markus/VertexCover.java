package com.merlin.markus;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VertexCover {

    private Map<Integer, List<Integer>> graph;

    public VertexCover(String fileUrl) {
        File file = new File(fileUrl);
        graph = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String st;
            st = br.readLine();
            String[] nAndM = st.split(" ");
            int n = Integer.parseInt(nAndM[0]);
            for(int i = 1; i <= n; i++) {
                graph.put(i, new ArrayList<>());
            }

            while ((st = br.readLine()) != null) {
                String[] edge = st.split(" ");
                int n1 = Integer.parseInt(edge[0]);
                int n2 = Integer.parseInt(edge[1]);
                graph.get(n1).add(n2);
                graph.get(n2).add(n1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("lol");
    }

    public static void main(String[] args) {
        String fileUrl = "graphs/graph-1.graph";
        VertexCover vertexCover = new VertexCover(fileUrl);
        vertexCover.solve();

    }

    private void solve() {
    }
}
