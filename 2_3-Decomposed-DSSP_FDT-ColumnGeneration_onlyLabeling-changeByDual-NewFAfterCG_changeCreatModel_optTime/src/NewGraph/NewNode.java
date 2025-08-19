package NewGraph;

import Instance.Depot;
import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;

public class NewNode {
    private int id;
    private Trip trip;
    private Depot depot;
    private String nodeType;

    public NewNode(Trip trip) {
        this.trip = trip;
        this.id = trip.getIdOfTrip();
        this.nodeType = "trip";
    }

    // Constructor for Depot nodes
    public NewNode(Depot depot, boolean isStartingDepot) {
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

    public Trip getTrip() {
        if (nodeType == "trip") {
            return trip;
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
        String s = "NewNode {";

        if ("trip".equals(nodeType)) {  // Updated to "trip"
            s += "idTrip = " + this.trip.getIdOfTrip();
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

        NewNode newNode1 = new NewNode(instance.getDepot(0), true);
        NewNode newNode2 = new NewNode(instance.getTrip(1));
        System.out.println(newNode1);
        System.out.println(newNode2);

    }
}
