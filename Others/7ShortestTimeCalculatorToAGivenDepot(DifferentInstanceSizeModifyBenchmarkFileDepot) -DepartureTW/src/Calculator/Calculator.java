package Calculator;

import Instance.NewGraph;
import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.NewNode;
import Solution.SolutionOfShortestTimeToDepot;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Calculator {
    private NewGraph graph;
    private double[] dist;
    private ArrayList<NewNode> newNodes;
    private ArrayList<Trip> trips;

    public Calculator(NewGraph newGraph) {
        this.graph = newGraph;
        this.newNodes = new ArrayList<>();
        this.trips=new ArrayList<>();
        this.dist = new double[graph.getInstance().getNbTrips() + 1];
    }

    public SolutionOfShortestTimeToDepot solveWithDijkstra() {
        //this is to solve the minimum time of each trip need to arrive at a given ending depot
        //Todo: now think about how to sort them
        Instance instance = this.graph.getInstance();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            this.trips.add(i,trip);

        }

        Collections.sort(trips);
        System.out.println("after sort:"+trips);

        for(int n=0;n<this.trips.size();n++){
            NewNode newNode = new NewNode(trips.get(n));
            this.newNodes.add(newNode);
        }

        NewNode newNodeAsDepot= new NewNode(this.graph.getGivenDepot(), false);
        this.newNodes.add(newNodeAsDepot);

        int[] prev;
        prev = new int[this.newNodes.size()];
        ArrayList<NewNode> setQ = new ArrayList<>();// setQ is a set of node in the graph which waiting for dealing

        //initialize
        for (int n = 0; n < this.newNodes.size(); n++) {
            dist[n] = Double.MAX_VALUE;
            prev[n] = -1;
            setQ.add(this.newNodes.get(n));
        }
        System.out.println("Set Q" + setQ);

        // set distance of the ending depot i
        dist[this.newNodes.size() - 1] = 0;//this is to put the ending depot dist=0;
        int n = 0;// this is not useful just for helping me check the code in while loop whether it stuck or not
        while (!(setQ.isEmpty())) {
            // Through all uncertain nodes, pick the one with mini distance
            NewNode currentNewNodeWithMinTimeToDepot = getNodeWithMinTimeToDepot(dist, setQ);

            //remove U for setQ which stand for the node waiting to be dealt with
            setQ.remove(currentNewNodeWithMinTimeToDepot);
            //System.out.println("setQ after remove node "+ setQ);

            // focus on the neighbor ( which is not deal yet) of the current node with mini time to depot  //Repeat
            for (NewNode newNode : setQ) {
                if (graph.getArcWeightOnBetweenTwoNodesOnNewGraph(newNode, currentNewNodeWithMinTimeToDepot) < Double.MAX_VALUE) {// here is the problem
                    double dis = Double.MAX_VALUE;
                    if (currentNewNodeWithMinTimeToDepot.getNodeType() == "endingDepot") {
                        dis = dist[this.newNodes.size() - 1] + graph.getArcWeightOnBetweenTwoNodesOnNewGraph(newNode, currentNewNodeWithMinTimeToDepot);
                    } else {
                        dis = dist[currentNewNodeWithMinTimeToDepot.getId()] + graph.getArcWeightOnBetweenTwoNodesOnNewGraph(newNode, currentNewNodeWithMinTimeToDepot);
                    }
                    if (dis < dist[newNode.getId()]) {
                        dist[newNode.getId()] = dis;
                        prev[newNode.getId()] = currentNewNodeWithMinTimeToDepot.getId();

                    }
                }
            }
            n++;
            //System.out.println("iteration n " +n );
        }
        return buildSolutionFromPredecessor(prev);
    }

    private NewNode getNodeWithMinTimeToDepot(double[] dist, ArrayList<NewNode> setQ) {
        //given the current dist and the uncertain nodes, calculate the distance which is the mini
        NewNode newNodeWithMinTimeToDepot = this.newNodes.get(this.newNodes.size() - 1);// initialize as the ending depot

        double minDist = Double.MAX_VALUE;
        for (NewNode newNode : setQ) {
            int id = Integer.MAX_VALUE;
            if (newNode.getNodeType() .equals("endingDepot") ) {
                id = this.graph.getInstance().getNbTrips();
            } else if (newNode.getNodeType().equals("trip")){
                id = newNode.getId();
            }
            if (dist[id] < minDist) {
                minDist = dist[id];
                newNodeWithMinTimeToDepot = newNode;
            }
        }
        return newNodeWithMinTimeToDepot;// all this node With minTimeToDepot stands for those nodes have already been deal
    }

    private SolutionOfShortestTimeToDepot buildSolutionFromPredecessor(int[] prev) {
        SolutionOfShortestTimeToDepot sol = new SolutionOfShortestTimeToDepot(graph);
        // starting from the last (here is the starting point node)
        int nodeIndex = this.newNodes.size() - 1;//
        NewNode newNode = this.newNodes.get(nodeIndex);
        while (nodeIndex != -1) {// check there is still a node in the path
            //add the node in the solution;
            sol.addNodeBeforeInShortestPath(newNode);
            //update node
            nodeIndex = prev[nodeIndex];
        }
        return sol;
    }

    public double[] getDistances() {
        return dist;
    }

    // Add a method to save all the distances related to the given depot
    public void saveDistancesToFile(String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            double[] distances = getDistances();
            writer.write("//idTrip   distance(time) : Each trip minTime to arrive Depot_" + this.graph.getGivenDepot().getIdOfDepot() );
            writer.newLine(); // Add a newline for each entry
            for (int i = 0; i < distances.length - 1; i++) {
                writer.write(+i + " " + (int) distances[i]);
                writer.newLine(); // Add a newline for each entry
            }
            writer.close();
            System.out.println("Distances saved to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Calculator{" +
                ", graph=" + graph +
                ", shortestTime=" + Arrays.toString(dist) +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);
        NewGraph newGraph = new NewGraph(instance, instance.getDepot(0));

        Calculator calculator = new Calculator(newGraph);
        SolutionOfShortestTimeToDepot solution = calculator.solveWithDijkstra();
        System.out.println("solution :" + solution);
        double[] distances = calculator.getDistances();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            System.out.println("shortest time to arrive Depot_" + instance.getDepot(0) + " " + " from trip_" + i + " is: " + (int) distances[i]);
        }
        calculator.saveDistancesToFile("distance.txt");

    }

}
