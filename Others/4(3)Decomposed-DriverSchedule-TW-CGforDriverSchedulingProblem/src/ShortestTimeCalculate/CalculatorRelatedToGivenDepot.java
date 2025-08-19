package ShortestTimeCalculate;


import NewGraph.GraphRelatedToGivenDepot;
import NewGraph.NewNode;
import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.TripComparators;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class use Dijkstra algorithm to calculate the minimum time for each trip go back to the given depot
 * */

public class CalculatorRelatedToGivenDepot {
    private GraphRelatedToGivenDepot graph;
    private int[] dist;
    private ArrayList<NewNode> nodes;
    private ArrayList<Trip> trips;

    public CalculatorRelatedToGivenDepot(GraphRelatedToGivenDepot graphRelatedToGivenDepot) {
        this.graph = graphRelatedToGivenDepot;
        this.nodes = new ArrayList<>();
        this.trips=new ArrayList<>();
        this.dist = new int[graph.getInstance().getNbTrips() + 1];
    }


    public SolutionOfShortestTimeToDepot solveWithDijkstra() {
        //this is to solve the minimum time of each trip need to arrive at a given ending depot
        Instance instance = this.graph.getInstance();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            this.trips.add(i,trip);
        }

        Collections.sort(trips, TripComparators.BY_LATEST_DEPARTURE);
        //System.out.println("after sort:"+trips);//Todo: here try to sort them by latest arriving time!

        for(int n=0;n<this.trips.size();n++){
            NewNode node = new NewNode(trips.get(n));
            this.nodes.add(node);
        }

        NewNode nodeAsDepot = new NewNode(this.graph.getGivenDepot(), false);// now the depot as an ending depot, the last nodes
        this.nodes.add(nodeAsDepot);

        int[] prev;
        prev = new int[this.nodes.size()];
        ArrayList<NewNode> setQ = new ArrayList<>();// setQ is a set of node in the graph which waiting for dealing

        //initialize
        for (int n = 0; n < this.nodes.size(); n++) {
            dist[n] = Integer.MAX_VALUE;
            prev[n] = -1;
            setQ.add(this.nodes.get(n));
        }
        //System.out.println("Set Q" + setQ);

        // set distance of the ending depot i
        dist[this.nodes.size() - 1] = 0;//this is to put the ending depot dist=0;
        int n = 0;// this is not useful , but just add it for helping me check the code in while loop whether it stuck or not
        while (!(setQ.isEmpty())) {
            // Through all uncertain nodes, pick the one with mini distance
            NewNode currentNodeWithMinTimeToDepot = getNodeWithMinTimeToDepot(dist, setQ);

            //remove U for setQ which stand for the node waiting to be dealt with
            setQ.remove(currentNodeWithMinTimeToDepot);
            //System.out.println("setQ after remove node "+ setQ);

            // focus on the neighbor ( which is not deal yet) of the current node with mini time to depot  //Repeat
            for (NewNode node : setQ) {
                if (graph.getArcWeightOnBetweenTwoNodesOnNewGraph(node, currentNodeWithMinTimeToDepot) < Double.MAX_VALUE) {// here is the problem
                    int dis = Integer.MAX_VALUE;
                    if (currentNodeWithMinTimeToDepot.getNodeType() == "endingDepot") {
                        dis =dist[this.nodes.size() - 1] + (int)graph.getArcWeightOnBetweenTwoNodesOnNewGraph(node, currentNodeWithMinTimeToDepot);
                    } else {
                        dis = dist[currentNodeWithMinTimeToDepot.getId()] + (int) graph.getArcWeightOnBetweenTwoNodesOnNewGraph(node, currentNodeWithMinTimeToDepot);
                    }
                    if (dis < dist[node.getId()]) {
                        dist[node.getId()] = dis;
                        prev[node.getId()] = currentNodeWithMinTimeToDepot.getId();
                    }
                }
            }
            n++;
            //System.out.println("iteration n " +n );
        }
        return buildSolutionFromPredecessor(prev);
    }

    private NewNode getNodeWithMinTimeToDepot(int[] dist, ArrayList<NewNode> setQ) {
        //given the current dist and the uncertain nodes, calculate the distance which is the mini
        NewNode nodeWithMinTimeToDepot = this.nodes.get(this.nodes.size() - 1);// initialize as the ending depot

        double minDist = Double.MAX_VALUE;
        for (NewNode node : setQ) {
            int id = Integer.MAX_VALUE;
            if (node.getNodeType() .equals("endingDepot") ) {
                id = this.graph.getInstance().getNbTrips();
            } else if (node.getNodeType().equals("trip")){
                id = node.getId();
            }
            if (dist[id] < minDist) {
                minDist = dist[id];
                nodeWithMinTimeToDepot = node;
            }
        }
        return nodeWithMinTimeToDepot;// all this node With minTimeToDepot stands for those nodes have already been deal
    }

    private SolutionOfShortestTimeToDepot buildSolutionFromPredecessor(int[] prev) {
        SolutionOfShortestTimeToDepot sol = new SolutionOfShortestTimeToDepot(graph);
        // starting from the last (here is the starting point node)
        int nodeIndex = this.nodes.size() - 1;//
        NewNode node = this.nodes.get(nodeIndex);
        while (nodeIndex != -1) {// check there is still a node in the path
            //add the node in the solution;
            sol.addNodeBeforeInShortestPath(node);
            //update node
            nodeIndex = prev[nodeIndex];
        }
        return sol;
    }

    public int[] getDistances() {
        return dist;
    }

    // Add a method to save all the distances related to the given depot
    public void saveDistancesToFile(String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            int[] distances = getDistances();
            writer.write("//Each trip minTime to arrive Depot_" + this.graph.getGivenDepot() + " idTrip   distance");
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
        return "CalculatorRelatedToGivenDepot{" +
                ", graph=" + graph +
                ", shortestTime=" + Arrays.toString(dist) +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);
        GraphRelatedToGivenDepot graphRelatedToGivenDepot = new GraphRelatedToGivenDepot(instance, instance.getDepot(1));

        CalculatorRelatedToGivenDepot calculatorRelatedToGivenDepot = new CalculatorRelatedToGivenDepot(graphRelatedToGivenDepot);
        SolutionOfShortestTimeToDepot solution = calculatorRelatedToGivenDepot.solveWithDijkstra();
        System.out.println("solution :" + solution);
        int[] distances = calculatorRelatedToGivenDepot.getDistances();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            //System.out.println("shortest time to arrive Depot_" + instance.getDepot(0) + " " + " from trip_" + i + " is: " + (int) distances[i]);
        }
        calculatorRelatedToGivenDepot.saveDistancesToFile("distance.txt");

    }

}
