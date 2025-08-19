package Generator;

import NewGraph.*;

import ColumnGe.MasterProblem;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.TripComparators;
import Instance.TripWithWorkingStatus;
import Instance.TripWithWorkingStatusAndDepartureTime;
import Solution.DriverSchedule;
import Solution.SchedulesReader;
import Solution.Solution;

import java.io.IOException;
import java.util.*;

/**
 * This class use labeling algorithm to generate the driver schedules go back to the given depot with  minimum-reduced cost
 */
public class PathsGeneratorBasedOnGivenDepot {
    private GraphRelatedToGivenDepot graphRelatedToGivenDepot;//including instance and given starting depot
    private MasterProblem masterProblem;
    private double minReducedCost;

    private int nbGenerateLabelsWithGivenDepot;

    private double durationOfDominanceInNanoSecondTime;

    final double epsilon = 0.000001;

    public PathsGeneratorBasedOnGivenDepot(GraphRelatedToGivenDepot graphRelatedToGivenDepot, MasterProblem masterProblem) {
        this.graphRelatedToGivenDepot = graphRelatedToGivenDepot;
        this.masterProblem = masterProblem;
        this.minReducedCost = Double.MAX_VALUE; // Initialize to a large value;
        this.nbGenerateLabelsWithGivenDepot = 0;
        this.durationOfDominanceInNanoSecondTime = 0;
    }

    public ArrayList<DriverSchedule> generateMiniReducedCostPathsBasedOnAGivenStartingDepot() {
        //Step1: define a list used for contains all the feasible paths
        ArrayList<DriverSchedule> driverSchedules = new ArrayList<>();

        //Step2: sort all the trips
        ArrayList<Trip> allTrips = new ArrayList<>();
        for (int i = 0; i < this.graphRelatedToGivenDepot.getInstance().getNbTrips(); i++) {
            allTrips.add(this.graphRelatedToGivenDepot.getInstance().getTrip(i));
        }
        Collections.sort(allTrips, TripComparators.BY_EARLIEST_DEPARTURE);
        //System.out.println("after sort by earliest departure " + allTrips);

        //step3:   initialize the  nodeWithLabels list
        ArrayList<NodeWithLabels> listOfNodeWithLabels = new ArrayList<>();


        //step4:  initialize trip type node and its empty labels, put all of them into NodeWithLabels list
        for (int i = 0; i < this.graphRelatedToGivenDepot.getInstance().getNbTrips(); i++) {
            NewNode node = new NewNode(this.graphRelatedToGivenDepot.getInstance().getTrip(i));
            NodeWithLabels nodeWithLabels = new NodeWithLabels(node);
            listOfNodeWithLabels.add(nodeWithLabels);//add all the trip into the nodeWithLabels
        }

        //step5: initialize depot type node and its labels, put all of them into NodeWithLabels list
        // the starting depot and its one initialized label be put into the nodeWithLabels list
        // the ending depot  and its empty labels be put into nodeWithLabels list
        NewNode nodeAsStartingDepot = new NewNode(this.graphRelatedToGivenDepot.getGivenDepot(), true);
        NodeWithLabels nodeWithLabels_StartingDepot = new NodeWithLabels(nodeAsStartingDepot);

        Label iniLabel = new Label(this.graphRelatedToGivenDepot, masterProblem);
        nodeWithLabels_StartingDepot.addLabel(iniLabel);
        listOfNodeWithLabels.add(nodeWithLabels_StartingDepot);

        NewNode nodeAsEndingDepot = new NewNode(this.graphRelatedToGivenDepot.getGivenDepot(), false);
        NodeWithLabels nodeWithLabelsAsEndingDepot = new NodeWithLabels(nodeAsEndingDepot);
        listOfNodeWithLabels.add(nodeWithLabelsAsEndingDepot);

        String startingDepotType = "startingDepot";
        String tripType = "trip";
        String endingDepotType = "endingDepot";
        double maxPlanTime = graphRelatedToGivenDepot.getInstance().getMaxPlanTime();

        boolean termination = false;
        while (termination != true) {
            termination = true;
            for (int n = 0; n < listOfNodeWithLabels.size(); n++) {
                NodeWithLabels node_n_WithLabels = listOfNodeWithLabels.get(n);

                if (!node_n_WithLabels.getNewNode().getNodeType().equals(endingDepotType) && (node_n_WithLabels.getLabels().size() != 0)) {//considering all the trips and startingDepot
                    LinkedList<Label> labels = node_n_WithLabels.getLabels();
                    // Create ListIterator for the labels within nodeWithLabels
                    ListIterator<Label> labelIterator = labels.listIterator();
                    // Iterate through the labels using a ListIterator // for all the label of current nodeWithLabels I
                    while (labelIterator.hasNext()) {
                        Label labelOfI = labelIterator.next();

                        // Check if labelOfI is not null and has not been extended yet
                        if (labelOfI != null && (!labelOfI.getWhetherBeExtended())) {//Here we just choose those labels which are not be extended yet
                            int nb_i=0;
                            for (int m = 0; m < listOfNodeWithLabels.size(); m++) {
                                NodeWithLabels nodeWithLabelsJ = listOfNodeWithLabels.get(m);// for all nodeWithLabels J
                                double arcWeightOnBetweenTwoNodesOnNewGraph = graphRelatedToGivenDepot.getArcWeightOnBetweenTwoNodesOnNewGraph(node_n_WithLabels.getNewNode(), nodeWithLabelsJ.getNewNode());
                                if (arcWeightOnBetweenTwoNodesOnNewGraph <= maxPlanTime && labelOfI.getTotalDrivingTime() <= graphRelatedToGivenDepot.getInstance().getMaxDrivingTime()) {//only try to extend to the neighbour node
                                    int idOfFollowerNode = nodeWithLabelsJ.getNewNode().getId();
                                    // it could also start from deport or start from trip, which have different departure time; we need to separately extend
                                    // it could be a trip or the ending depot type, thus we need to separately extend
                                    if (nodeWithLabelsJ.getNewNode().getNodeType().equals(tripType) && (labelOfI.getUnreachableTripId().get(idOfFollowerNode)) == false) {//

                                        //Here is Situation 1: the neighbor is a reachable trip, then we extend to driving status and passenger status
                                        int earliestDepartureTime = nodeWithLabelsJ.getNewNode().getTrip().getEarliestDepartureTime();
                                        int latestDepartureTime = nodeWithLabelsJ.getNewNode().getTrip().getLatestDepartureTime();
                                        for (int t_prime = earliestDepartureTime; t_prime <= latestDepartureTime; t_prime++) {
                                            Label labelJ1 = labelOfI.extendTo(nodeWithLabelsJ.getNewNode(), 1, t_prime);
                                            Label labelJ2 = labelOfI.extendTo(nodeWithLabelsJ.getNewNode(), 0, t_prime);
                                            nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
//                                            //_____the following is for the labeling without dominance rule case
                                            nodeWithLabelsJ.addLabel(labelJ1);
                                            nodeWithLabelsJ.addLabel(labelJ2);

//                                            // then we need to re-exam the current non-extension label in the whole nodeWithLabels of the graph
//                                            // that means we need to do the for loop again, instead of just do once
//                                            // TODO: Think why? to get the minimum reduced cost?
//                                            // *****Here is the labeling with dominance code case ******************************************************
                                        }

                                        termination = false;// as long as there is an extension behaviour happened,

                                        //****************************************************************************************************************************
                                    } else if (nodeWithLabelsJ.getNewNode().getNodeType().equals(endingDepotType)) {
                                        // Here is Situation 2: the neighbor is ending depot, then we will extend to non-defined driving status
                                        Label labelJ = labelOfI.extendTo(nodeWithLabelsJ.getNewNode(), -1, -1);// TODO: check the secondDepTime whether influence
                                        nodeWithLabelsJ.addLabel(labelJ);
                                        termination = false;
                                    }
                                }
                            }
                           // System.out.println("count n_bi"+nb_i);
                        }
                        labelOfI.setWhetherBeExtended(true);
                    }
                }
            }
        }

        Set<Label> minReducedCostLabels = new HashSet<>(); // Initialize an empty list  because there maybe more than one mini-Reduced cost labels
        for (NodeWithLabels nodeWithLabels : listOfNodeWithLabels) {
            if (nodeWithLabels.getNewNode().getNodeType().equals(endingDepotType) && !nodeWithLabels.getLabels().isEmpty()) {
                for (int l = 0; l < nodeWithLabels.getLabels().size(); l++) {
                    Label label = nodeWithLabels.getLabels().get(l);
                    if (label != null && label.checkAtLeastOneIsDriving()
                            //here is to ensure the feasible path has at least one drip
                            && label.getTotalReducedCostCost() < minReducedCost - epsilon) {
                        minReducedCost = label.getTotalReducedCostCost();
                        minReducedCostLabels.clear();//delete the previous min-reduced cost label
                        minReducedCostLabels.add(label);// add the current min-reduced cost label
                    } else if (label != null && label.checkAtLeastOneIsDriving()
                            //here is to ensure the feasible path has at least one driving status
                            && label.getTotalReducedCostCost() > minReducedCost - epsilon && label.getTotalReducedCostCost() < minReducedCost + epsilon) {
                        minReducedCostLabels.add(label);
                    }
                }
            }
        }

        if (!minReducedCostLabels.isEmpty()) {

            for (Label minReducedCostLabel : minReducedCostLabels) {
                DriverSchedule driverSchedule = new DriverSchedule(this.graphRelatedToGivenDepot.getInstance());
                driverSchedule.setIdOfDepot(this.graphRelatedToGivenDepot.getGivenDepot().getIdOfDepot());
                driverSchedule.setIndexDepotAsStartingPoint(this.graphRelatedToGivenDepot.getGivenDepot().getIndexOfDepotAsStartingPoint());
                driverSchedule.setIndexDepotAsEndingPoint(this.graphRelatedToGivenDepot.getGivenDepot().getIndexOfDepotAsEndingPoint());

                for (TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime : minReducedCostLabel.getSequenceOfTripsWithWorkingStatusAndDepartureTime()) {
                    driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTime);
                }
                driverSchedules.add(driverSchedule);
                //System.out.println("Add driver schedule in the mini reduced cost "+driverSchedule);
            }

        }

        return driverSchedules;
    }

//    private boolean checkDominanceAddNonDominatedLabel(NodeWithLabels nodeWithLabelsJ, Label newLabel) {
//        double reducedCostOfNewLabel = newLabel.getTotalReducedCostCost();
//        LinkedList<Label> existingLabelsInNodeJ = nodeWithLabelsJ.getLabels();
//        // Create a ListIterator for the labels within nodeWithLabelsJ
//        ListIterator<Label> existingLabelIterator = existingLabelsInNodeJ.listIterator();
//
//        // Iterate through the labels in nodeWithLabelsJ using a ListIterator
//        int counter = 0;
//
//        while (existingLabelIterator.hasNext()) {
//            Label labelJ = existingLabelIterator.next();
//            double reducedCostJ = labelJ.getTotalReducedCostCost();
//            if (reducedCostJ < reducedCostOfNewLabel) {
//                if (isLabel_FDominatesLabel_S(labelJ, newLabel)) {
//                    return false;
//                }
//            } else if (reducedCostJ == reducedCostOfNewLabel) {
//                if (isLabel_FDominatesLabel_S(labelJ, newLabel)) {
//                    return false;
//                }
//                if (isLabel_FDominatesLabel_S(newLabel, labelJ)) {
//                    // remove oldLabel J from the existingList
//                    existingLabelIterator.remove();
//                }
//            } else {
//                counter++;
//                if (counter == 1) {
//                    /**
//                     * here is to insert the new label and ensure it will be put by increasing reduced cost order*/
//                    existingLabelIterator.previous();
//                    // here we want to add the label before the current one to keep the list in the reduced cost increasing order,
//                    // so we first change the adverse iteration direction, by using previous() to put the current label with smaller reduced cost
//                    existingLabelIterator.add(newLabel);// then add the elements
//                    existingLabelIterator.next();// then change back to the direction next() to move forward,
//                }
//
//                if (isLabel_FDominatesLabel_S(newLabel, labelJ)) {
//                    existingLabelIterator.remove();
//                }
//            }
//        }
//        if (counter == 0) { //if all the while loop run, and there the new label never comes to the third part compare
//            // and never be dominated in the first two steps, so we need to add this non-dominated label
//            existingLabelsInNodeJ.add(newLabel);
//        }
//        return true;
//    }


//    private boolean isLabel_FDominatesLabel_S(Label labelF, Label labelS) {
//        boolean isLabel_FDominatesLabel_S = false;
//        //Comment:  boolean type check at least one driving, when we check we need check each element  of the list
//        boolean atLeastOneIsDrivingLabelF = labelF.checkAtLeastOneIsDriving();
//        boolean atLeastOneIsDrivingLabelS = labelS.checkAtLeastOneIsDriving();
//        if (atLeastOneIsDrivingLabelF && atLeastOneIsDrivingLabelS) {
//            if (labelF.checkWhetherDrivingStatusIsSame(labelS)) {
//                if (labelF.whetherThisDominatesUnderSameDrivingStatus(labelS)) {
//                    //Dominance 1
//                    isLabel_FDominatesLabel_S = true;
//                }
//            } else {
//                if (labelF.whetherThisDominatesUnderDifferentDrivingStatus(labelS)) {
//                    //Dominance 2
//                    isLabel_FDominatesLabel_S = true;
//                }
//            }
//        } else if ((!atLeastOneIsDrivingLabelF) && (!atLeastOneIsDrivingLabelS)) {
//            //Dominance 1
//            if (labelF.whetherThisDominatesUnderSameDrivingStatus(labelS)) {
//                isLabel_FDominatesLabel_S = true;
//            }
//        }
//        return isLabel_FDominatesLabel_S;
//    }
//

    public double getMinReducedCost() {
        return minReducedCost;
    }

    public int getNbGenerateLabelsWithGivenDepot() {
        return nbGenerateLabelsWithGivenDepot;
    }

    public double getDurationOfDominanceInNanoSecondTime() {
        return durationOfDominanceInNanoSecondTime;
    }

    @Override
    public String toString() {
        return "PathsGeneratorBasedOnGivenDepot{" +
                '}';
    }

    public static void main(String[] args) throws IOException {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);

        SchedulesReader schedulesReader = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt", instance);
        Solution initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);
        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());

        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
        masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况

        GraphRelatedToGivenDepot graphRelatedToGivenDepot1 = new GraphRelatedToGivenDepot(instance, instance.getDepot(1));

        PathsGeneratorBasedOnGivenDepot pathsGeneratorBasedOnGivenDepot = new PathsGeneratorBasedOnGivenDepot(graphRelatedToGivenDepot1, masterProblem);
        System.out.println("schedule: " + pathsGeneratorBasedOnGivenDepot.generateMiniReducedCostPathsBasedOnAGivenStartingDepot());
        System.out.println("reduced cost 1: " + pathsGeneratorBasedOnGivenDepot.getMinReducedCost());
        System.out.println("reduced cost 2:" + pathsGeneratorBasedOnGivenDepot.minReducedCost);
    }
}
