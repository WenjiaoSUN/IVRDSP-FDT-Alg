package ShortestTimeCalculate;

import NewGraph.GraphRelatedToGivenDepot;
import NewGraph.NewNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class SolutionOfShortestTimeToDepot {
    /**
     * The graph where we are finding a shortest path
     */
    private GraphRelatedToGivenDepot graph;
    /**
     * The list of nodes in the shortest path
     * Has to start with the source 0, an ends with the sink n-1*/

    private ArrayList<NewNode> shortestPath;
    /**
     * The cost of the solution
     * The sum of the distances of the arcs in the shortest path*/

    private double timeCost;

    public SolutionOfShortestTimeToDepot(GraphRelatedToGivenDepot graph) {
        this.graph = graph;
        this.timeCost = 0;
        this.shortestPath = new ArrayList<>();
        //this.shortestPath.add(0);
    }


    public void addNodeBeforeInShortestPath(NewNode node) {
        if (!this.shortestPath.isEmpty()) {
            NewNode firstNode = this.shortestPath.get(0);
            this.timeCost = this.timeCost + graph.getArcWeightOnBetweenTwoNodesOnNewGraph(node, firstNode);
        }
        this.shortestPath.add(0, node); // add i at the beginning of the list
    }

    //在这里定义写出文件，以后才能调用写出解到某文档中
    public void printInFile(String fileName) {
        // number 10
        // print the solution in the file 'fileName'

        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName);
            writer.println("The minCost = " + this.timeCost);
            for (NewNode node : this.shortestPath) {
                if(node.getNodeType()=="trip") {
                    writer.println(node);
                }
            }
            writer.close();
        } catch (IOException ex) {
            System.out.println("Erreur fichier écriture");
            System.out.println(ex);
        }

    }


    @Override
    public String toString() {
        String s = "The shortest path of the source in this graph has cost " + this.timeCost;
        s += " - Nodes visited : ";
        for (NewNode val : this.shortestPath) {
            s += "\n\t" + val;
        }
        // number 1
        // to continue : add information about the shortest path and the cost
        return s;
    }


}
