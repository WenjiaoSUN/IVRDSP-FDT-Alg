package Generator;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.Depot;
import Solution.DriverSchedule;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.util.ArrayList;
import java.util.Collections;

public class PathsGeneratorBasedOnGivenTrip {
    private Instance instance;

    public PathsGeneratorBasedOnGivenTrip(Instance instance) {
        this.instance = instance;
    }

    public ArrayList<DriverSchedule> generatePaths(Trip startingTrip, int maxNbPathsGenerate, int maxLengthPreviousTrip) {
        //Step1: define a list used for contains all the feasible paths
        ArrayList<DriverSchedule> driverSchedules = new ArrayList<>();
        ///Schedules schedules = new Schedules(this.instance);

        //Step2: sort all the trips
        ArrayList<Trip> allTrips = new ArrayList<>();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            allTrips.add(instance.getTrip(i));
        }
        Collections.sort(allTrips);
        /**
         * check how to sort the trip
         * */
        //System.out.println("check:" + allTrips);

        //step3:we put all the trips into nodes
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Node node = new Node(allTrips.get(i));
            nodes.add(node);
        }
        //System.out.println("check nodes" + nodes);

        //step4:extend the empty labels to the given startingTrips, then get the initialized nodes
        Label label = new Label(instance, startingTrip);//here set label is only useful for starting Trip==this.starting Trip

        for (Node node : nodes) {
            if (node.getTrip().equals(startingTrip)) {
                Label initLabelWithDriving = label.extend(node.getTrip());
                node.addLabel(initLabelWithDriving);
                System.out.println("node with initial label with driving" + initLabelWithDriving);
            }
        }

        //step5: extend to the labels of the previous node the current node(randomly choose some has arcs to the previous one)
        for (int i = 0; i < nodes.size(); i++) {//for all the current node i
            Node currentNode = nodes.get(i);
            Trip currentTrip = currentNode.getTrip();
            for (int j = 0; j < i; j++) {//for the node before this node i (according to the order of the nodes set)
                Node previousNode = nodes.get(j);
                if (!previousNode.getLabels().isEmpty()) {
                    //System.out.println("check:" + previousNode);
                    for (int k = 0; k < previousNode.getLabels().size(); k++) {
                        Label preLabel = previousNode.getLabels().get(k);
                        if (!(preLabel == null) && preLabel.getSequenceOfTrips().size() <= maxLengthPreviousTrip - 1) {
                            //limited the number of trips in the final schedule we generate is less than 5

                            Label newLabelOfMiddleWithNonDriving = preLabel.extend(currentTrip);
                            currentNode.addLabel(newLabelOfMiddleWithNonDriving);
                            if (i == 0) {
                                System.out.println("check new labels with driving status" + currentNode.getLabels().get(0));
                            }

                        }
                    }

                }
            }
        }

        //step6: extend all the labels to the null
        int nbGenerated = 0;//here is to control the number of paths

        for (Node node : nodes) {
            for (Label labelToEnd : node.getLabels()) {
                if (labelToEnd != null) {
                    if ((labelToEnd.getTrip().getIdOfEndCity() == startingTrip.getIdOfStartCity())) {
                        Label finialLabel = labelToEnd.extend(null);//check whether there is a problem
                        nbGenerated++;
                        if (nbGenerated <= maxNbPathsGenerate) {
                            if (finialLabel != null && finialLabel.getEndId() != -1 && finialLabel.getTotalDrivingTime() <= instance.getMaxDrivingTime()
                                    && finialLabel.getTotalWorkingTime() <= instance.getMaxWorkingTime()
                                    && finialLabel.getCost() < Double.MAX_VALUE) {
                                DriverSchedule driverSchedule = new DriverSchedule(instance);
                                for (int j = 0; j < finialLabel.getSequenceOfTrips().size(); j++) {
                                    int idOfStartCity = finialLabel.getSequenceOfTrips().get(0).getIdOfStartCity();
                                    for (int p = 0; p < instance.getNbDepots(); p++) {
                                        Depot depot = instance.getDepot(p);
                                        if (depot.getIdOfCityAsDepot() == idOfStartCity) {
                                            driverSchedule.setIdOfDepot(depot.getIdOfDepot());
                                            driverSchedule.setIndexDepotAsStartingPoint(depot.getIndexOfDepotAsStartingPoint());
                                            driverSchedule.setIndexDepotAsEndingPoint(depot.getIndexOfDepotAsEndingPoint());
                                        }
                                    }
                                    boolean whetherDrive = true;
                                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus = new TripWithWorkingStatusAndDepartureTime(finialLabel.getSequenceOfTrips().get(j), whetherDrive,-1);
                                    driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus);
                                }
                                driverSchedules.add(driverSchedule);
                            }
                        }

                    }
                }

            }

        }
        return driverSchedules;

    }

    @Override
    public String toString() {
        return "PathsGeneratorBasedOnGivenTrip{" +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        //System.out.println(instance);
        PathsGeneratorBasedOnGivenTrip pathsGeneratorBasedOnGivenTrip = new PathsGeneratorBasedOnGivenTrip(instance);
        System.out.println(pathsGeneratorBasedOnGivenTrip.generatePaths(instance.getTrip(0), 8, 9));

    }
}
