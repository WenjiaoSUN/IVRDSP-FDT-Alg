package Solution;


import Instance.Depot;
import Instance.Instance;
import Instance.InstanceReader;

public class Node {
    private int id;
    private TripWithStartingInfos tripWithStartingInfos;
    private Depot depot;
    private String nodeType;

    public Node(TripWithStartingInfos tripWithStartingInfos) {
        this.tripWithStartingInfos = tripWithStartingInfos;
        this.id = tripWithStartingInfos.getIdOfTrip();
        this.nodeType = "trip";
    }

    public Node(Depot depot, boolean isStartingDepot) {
        this.depot=depot;
        this.id = depot.getIdOfDepot();
        if (isStartingDepot) {
            this.nodeType = "startingDepot";
        } else {
            this.nodeType = "endingDepot";
        }
    }

    public int getId() {
        return id;
    }

    public String getNodeType() {
        return nodeType;
    }

    public TripWithStartingInfos getTripWithStartInfos() {
        if (nodeType == "trip") {
            return tripWithStartingInfos;
        } else {
            throw new IllegalStateException("This node is not a Trip.");
        }
    }

    public Depot getDepot() {
        if (nodeType == "startingDepot"||nodeType=="endingDepot") {
            return depot;
        }else {
            throw new IllegalStateException("This node is not a depot.");
        }
    }

    @Override
    public String toString() {
        String s = "Node {";

        if ("trip".equals(nodeType)) {  // Updated to "trip"
            s += "idTrip = " + this.tripWithStartingInfos.getIdOfTrip();
        } else if ("startingDepot".equals(nodeType)) {  // Updated to "startingDepot"
            s += "indexOfStartingDepot " + this.depot.getIndexOfDepotAsStartingPoint();
        } else if ("endingDepot".equals(nodeType)) {  // Updated to "endingDepot"
            s += "indexOfEndingDepot " + this.depot.getIndexOfDepotAsEndingPoint();
        }

        return s + "}";
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity05_Size180_Day1_nbTrips020_combPer0.0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);

        Node node1 = new Node(instance.getDepot(0), true);
        TripWithStartingInfos tripWithStartingInfos1= new TripWithStartingInfos(instance.getTrip(1),2);
        Node node2 = new Node(tripWithStartingInfos1);
        System.out.println(node1);
        System.out.println(node2);

    }
}