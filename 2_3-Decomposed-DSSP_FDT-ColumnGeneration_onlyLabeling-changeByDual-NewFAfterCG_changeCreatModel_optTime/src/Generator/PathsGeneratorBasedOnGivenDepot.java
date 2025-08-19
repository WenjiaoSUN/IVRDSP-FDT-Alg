package Generator;

import NewGraph.*;

import ColumnGe.MasterProblem;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.TripComparators;
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
    private double minReducedCost_ForwardLabel;
    private double minReducedCost_BackwardLabel;
    private int nbGenerateLabelsWithGivenDepot;
    private double durationOfDominanceInMilliSec;

    private double durationOfLabelingInMilliSec = 0;


    final double epsilon = 0.0000001;
    final int nbNegSchedulesToGenEachDepot = 100;// change from 100 to 50 2025 04 221

    private int realNbGeneratedNegReducedCostSchedule = 0;


    public PathsGeneratorBasedOnGivenDepot(GraphRelatedToGivenDepot graphRelatedToGivenDepot, MasterProblem masterProblem) {
        this.graphRelatedToGivenDepot = graphRelatedToGivenDepot;
        this.masterProblem = masterProblem;
        this.minReducedCost = Double.MAX_VALUE; // Initialize to a large value;
        this.minReducedCost_ForwardLabel=Double.MAX_VALUE;
        this.minReducedCost_BackwardLabel=Double.MAX_VALUE;
        this.nbGenerateLabelsWithGivenDepot = 0;
        this.durationOfDominanceInMilliSec = 0;
        this.realNbGeneratedNegReducedCostSchedule = 0;

    }

    public ArrayList<DriverSchedule> generateNegReducedCostPathsBasedOnAGivenStartingDepotForwardLabeling() {
        long startLabeling = System.currentTimeMillis();

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

        Label iniLabel = new Label(this.graphRelatedToGivenDepot, masterProblem, true);
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
                if (!node_n_WithLabels.getNewNode().getNodeType().equals(endingDepotType) && (node_n_WithLabels.getLabels().size() != 0)) {
                    //considering all the trips and startingDepot
                    // so now the label could be from the start depot or the trip
                    LinkedList<Label> labels = node_n_WithLabels.getLabels();
                    // Create ListIterator for the labels within nodeWithLabels
                    ListIterator<Label> labelIterator = labels.listIterator();
                    // Iterate through the labels using a ListIterator // for all the label of current nodeWithLabels I
                    while (labelIterator.hasNext()) {
                        Label labelOfI = labelIterator.next();
                        // Check if labelOfI is not null and has not been extended yet
                        if (labelOfI != null && (!labelOfI.getWhetherBeExtendedToNextNode())) {//Here we just choose those labels which are not be extended yet
                            int depTimeOfLabelI = labelOfI.getCurrentNodeDepTime();
                            int idOfNodeOfLabelI = labelOfI.getCurrentNode().getId();
                            for (int m = 0; m < listOfNodeWithLabels.size(); m++) {
                                NodeWithLabels nodeWithLabelsJ = listOfNodeWithLabels.get(m);// for all nodeWithLabels J
                                double arcWeightOnBetweenTwoNodesOnNewGraph = graphRelatedToGivenDepot.getArcWeightOnBetweenTwoNodesOnNewGraph(node_n_WithLabels.getNewNode(), nodeWithLabelsJ.getNewNode());
                                if (arcWeightOnBetweenTwoNodesOnNewGraph <= maxPlanTime && labelOfI.getTotalDrivingTime() <= graphRelatedToGivenDepot.getInstance().getMaxDrivingTime()) {
                                    //only try to extend to the neighbour node
                                    int idOfFollowerNode = nodeWithLabelsJ.getNewNode().getId();
                                    /**
                                     * // it could also start from deport or start from trip, which have different departure time; we need to separately extend
                                     * it could be a trip or the ending depot type, thus we need to separately extend
                                     * */
                                    if (nodeWithLabelsJ.getNewNode().getNodeType().equals(tripType) && (labelOfI.getUnreachableTripId().get(idOfFollowerNode)) == false) {
                                        /**
                                         *  here is the Case 1: Node J is trip type
                                         *  */

                                        //Here is Situation 1: the extended neighbor is a reachable trip, then we extend to driving status and passenger status
                                        int earliestDepartureTimeJ = nodeWithLabelsJ.getNewNode().getTrip().getEarliestDepartureTime();
                                        int latestDepartureTimeJ = nodeWithLabelsJ.getNewNode().getTrip().getLatestDepartureTime();
                                        Instance instance = this.graphRelatedToGivenDepot.getInstance();
                                        Trip tripJ = instance.getTrip(nodeWithLabelsJ.getNewNode().getId());
                                        if (labelOfI.getCurrentNode().getNodeType().equals(startingDepotType)) {
                                            /**
                                             *  here is the Case 1.1: Node J is trip type; Node I is a starting depot
                                             *  */
                                            // here special for time window need to think whether this label is null or not
                                            if (nodeWithLabelsJ.getLabels().isEmpty()) {// add this new check for time attributes
                                                // 这里也没进行dominance check受不管这里受不受，因为J是第一个Tirp
                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ej = (int) Math.round(earliestDepartureTimeJ / timeUnit);
                                                int nbU_Lj = (int) Math.round(latestDepartureTimeJ / timeUnit);

                                                for (int n_j = nbU_Ej; n_j <= nbU_Lj; n_j++) {//part1 change to unit folder 2025.4.18
                                                    int t_j = n_j * timeUnit;
                                                    Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j);
                                                    Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j);
                                                    //System.out.println("starting label1: "+labelJ1);
                                                    //System.out.println("starting label2: "+labelJ2);
                                                    nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                    if (labelJ1 != null && labelJ2 != null) {
                                                        double reducedCostJ1 = labelJ1.getTotalReducedCost();
                                                        double reducedCostJ2 = labelJ2.getTotalReducedCost();
                                                        //Case1 :if there is no any label yet, we put the two label by the reduced cost increasing way
                                                        if (reducedCostJ1 < reducedCostJ2) {
                                                            nodeWithLabelsJ.addLabel(labelJ1);
                                                            nodeWithLabelsJ.addLabel(labelJ2);
                                                            termination = false;
                                                        } else {
                                                            nodeWithLabelsJ.addLabel(labelJ2);
                                                            nodeWithLabelsJ.addLabel(labelJ1);
                                                            termination = false;
                                                        }
                                                    } else if (labelJ1 != null && labelJ2 == null) {
                                                        nodeWithLabelsJ.addLabel(labelJ1);
                                                        termination = false;
                                                    } else if (labelJ1 == null && labelJ2 != null) {
                                                        nodeWithLabelsJ.addLabel(labelJ2);
                                                        termination = false;
                                                    }
                                                }
                                            } else {
                                                /**
                                                 *  here is the Case 1.1.2: Node J is trip type; Node I is a starting depot; Node J has already some labels on it
                                                 *  */
                                                // 这里不管这里受不受时间窗影响的话，暂时也考虑所有的开始时间窗，因为J是第一个Trip
                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ej = (int) Math.round(earliestDepartureTimeJ / timeUnit);
                                                int nbU_Lj = (int) Math.round(latestDepartureTimeJ / timeUnit);
                                                for (int n_j = nbU_Ej; n_j <= nbU_Lj; n_j++) {//part2: change to unit folder 2025.4.18
                                                    int t_j = n_j * timeUnit;
                                                    Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j);
                                                    Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j);
                                                    nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                    // below here is the case without dominance rule 1
                                                    if (labelJ1 != null && labelJ2 != null) {
                                                        double reducedCostJ1 = labelJ1.getTotalReducedCost();
                                                        double reducedCostJ2 = labelJ2.getTotalReducedCost();
                                                        //Case1 :if there is no any label yet, we put the two label by the reduced cost increasing way
                                                        if (reducedCostJ1 < reducedCostJ2) {
                                                            nodeWithLabelsJ.addLabel(labelJ1);
                                                            nodeWithLabelsJ.addLabel(labelJ2);
                                                            termination = false;
                                                        } else {
                                                            nodeWithLabelsJ.addLabel(labelJ2);
                                                            nodeWithLabelsJ.addLabel(labelJ1);
                                                            termination = false;
                                                        }
                                                    } else if (labelJ1 != null && labelJ2 == null) {
                                                        nodeWithLabelsJ.addLabel(labelJ1);
                                                        termination = false;
                                                    } else if (labelJ1 == null && labelJ2 != null) {
                                                        nodeWithLabelsJ.addLabel(labelJ2);
                                                        termination = false;
                                                    }
                                                    // above here is the case without dominance rule 1

////                                                    //********************** below here is the dominance rule 1.1 ****************************************
//                                                    if (labelJ1 != null && labelJ2 != null) {
//                                                        double reducedCostJ1 = labelJ1.getTotalReducedCost();
//                                                        double reducedCostJ2 = labelJ2.getTotalReducedCost();
//                                                        if (reducedCostJ1 < reducedCostJ2) {
//                                                            double startTimeDom2_L1 = System.currentTimeMillis();
//                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
//
//                                                            double endTimeDom2_L1 = System.currentTimeMillis();
//                                                            double durationOfCurrentDominance2_L1 = endTimeDom2_L1 - startTimeDom2_L1;
//                                                            durationOfDominanceInMilliSec = durationOfDominanceInMilliSec + durationOfCurrentDominance2_L1;
//                                                            double startTimeDom2_L2 = System.currentTimeMillis();
//                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
//                                                            double endTimeDom2_L2 = System.currentTimeMillis();
//                                                            double durationOfCurrentDominance2_L2 = endTimeDom2_L2 - startTimeDom2_L2;
//                                                            durationOfDominanceInMilliSec = durationOfDominanceInMilliSec + durationOfCurrentDominance2_L2;
//
//                                                            if (isLabelAdded1 || isLabelAdded2) {
//                                                                termination = false;
//                                                            }
//                                                        } else {
//                                                            double startTimeDom2_L2 = System.currentTimeMillis();
//                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
//                                                            double endTimeDom2_L2 = System.currentTimeMillis();
//                                                            double durationOfCurrentDominance2_L2 = endTimeDom2_L2 - startTimeDom2_L2;
//                                                            durationOfDominanceInMilliSec = durationOfDominanceInMilliSec + durationOfCurrentDominance2_L2;
//
//                                                            double startTimeDom2_L1 = System.currentTimeMillis();
//                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
//                                                            double endTimeDom2_L1 = System.currentTimeMillis();
//                                                            double durationOfCurrentDominance2_L1 = endTimeDom2_L1 - startTimeDom2_L1;
//                                                            durationOfDominanceInMilliSec = durationOfDominanceInMilliSec + durationOfCurrentDominance2_L1;
//
//
//                                                            if (isLabelAdded1 || isLabelAdded2) {
//                                                                termination = false;
//                                                            }
//                                                        }
//
//
//                                                    } else if (labelJ1 != null && labelJ2 == null) {
//                                                        //nodeWithLabelsJ.addLabel(labelJ1);
//                                                        double startTimeDom2_2 = System.currentTimeMillis();
//                                                        boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
//                                                        double endTimeDom2_2 = System.currentTimeMillis();
//                                                        double durationOfCurrentDominance2_2 = endTimeDom2_2 - startTimeDom2_2;
//                                                        durationOfDominanceInMilliSec = durationOfDominanceInMilliSec + durationOfCurrentDominance2_2;
//                                                        if (isLabelAdded1) {
//                                                            termination = false;
//                                                        }
//
//                                                    } else if (labelJ1 == null && labelJ2 != null) {
//                                                        //nodeWithLabelsJ.addLabel(labelJ2);
//                                                        double startTimeDom2_3 = System.currentTimeMillis();
//                                                        boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
//                                                        double endTimeDom2_3 = System.currentTimeMillis();
//                                                        double durationOfCurrentDominance2_3 = endTimeDom2_3 - startTimeDom2_3;
//                                                        durationOfDominanceInMilliSec = durationOfDominanceInMilliSec + durationOfCurrentDominance2_3;
//                                                        if (isLabelAdded2) {
//                                                            termination = false;
//                                                        }
//                                                    }
//                                                    //********** above here consider the dominance rule

                                                }
                                                //System.out.println("nb Label generated"+nbGenerateLabelsWithGivenDepot);
                                            }
                                        } else {
                                            /**
                                             *  here is the Case 1.2: Node J is trip type; Node I is trip type
                                             *  */
                                            Trip tripI = instance.getTrip(idOfNodeOfLabelI);
                                            int duration = tripI.getDuration();
                                            if (nodeWithLabelsJ.getLabels().isEmpty()) {// add this new check for time attributes
                                                /**
                                                 *  here is the Case 1.2.1: Node J is trip type; Node I is trip type;There is no label on J before
                                                 *  */
                                                //*************************************************************Here we consider the extend the label according to the dual value****************************8
                                                //change code2025.4.25
                                                boolean whetherDelta_j_t_IsStrictNegative = false;
                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ej = (int) Math.round(earliestDepartureTimeJ / timeUnit);
                                                int nbU_Lj = (int) Math.round(latestDepartureTimeJ / timeUnit);
                                                double sumDelta = 0;
                                                for (int n_j = nbU_Ej; n_j <= nbU_Lj; n_j++) {
                                                    int t_j = n_j * timeUnit;
                                                    double delta_j_t = this.masterProblem.getDualValueFromLinkSelectScheduleAndTripOneDepTime(tripJ.getIdOfTrip(), t_j);
                                                    sumDelta = sumDelta + delta_j_t;
                                                    if (sumDelta < -epsilon) {
                                                        System.out.println("the sum of Delta is not zero");
                                                    }
                                                    if (delta_j_t < -epsilon) {
                                                        System.out.println("there is negative delta_" + delta_j_t);
                                                        whetherDelta_j_t_IsStrictNegative = true;
                                                        break;
                                                    }
                                                }

                                                boolean whetherZeta_ij_IsStrictNegative = false;
                                                double zeta_ij = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripI.getIdOfTrip(), tripJ.getIdOfTrip());
                                                if (zeta_ij < -epsilon) {
                                                    whetherZeta_ij_IsStrictNegative = true;
                                                    System.out.println("there is zeta_ij is strict negative" + zeta_ij);
                                                }


                                                //***********根据dual 拓展

                                                if (whetherDelta_j_t_IsStrictNegative) {
                                                    System.out.println("there is there is extend Case 1: Exist t s.t. Delta is strict negative");
                                                    //Case1: extend to all possible choice
                                                    for (int n_j = nbU_Ej; n_j <= nbU_Lj; n_j++) {

                                                        int t_j = n_j * timeUnit;
                                                        Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j);
                                                        Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                        if (labelJ1 != null && labelJ2 != null) {
                                                            double reducedCost1 = labelJ1.getTotalReducedCost();
                                                            double reducedCost2 = labelJ2.getTotalReducedCost();
                                                            if (reducedCost1 < reducedCost2) {
                                                                nodeWithLabelsJ.addLabel(labelJ1);
                                                                nodeWithLabelsJ.addLabel(labelJ2);
                                                                termination = false;
                                                            } else {
                                                                nodeWithLabelsJ.addLabel(labelJ2);
                                                                nodeWithLabelsJ.addLabel(labelJ1);
                                                                termination = false;
                                                            }

                                                        } else if (labelJ1 != null && labelJ2 == null) {
                                                            nodeWithLabelsJ.addLabel(labelJ1);
                                                            termination = false;

                                                        } else if (labelJ1 == null && labelJ2 != null) {
                                                            nodeWithLabelsJ.addLabel(labelJ2);
                                                            termination = false;

                                                        }

                                                    }

                                                } else {
//                                                    System.out.println("delta_t is all zero");
                                                    if (whetherZeta_ij_IsStrictNegative) {
                                                        System.out.println("there is extend Case 2 zeta_ij is strict negative" + zeta_ij);
                                                        // Case2: extend to minPlan or short connection depend on zeta_ij
                                                        int t_j_1 = earliestDepartureTimeJ;
                                                        int t_jj = depTimeOfLabelI + duration + instance.getMinPlanTurnTime();// earliest  feasible time for driver and passenger
                                                        if (t_jj > t_j_1 && t_jj <= latestDepartureTimeJ) {
                                                            t_j_1 = t_jj;
                                                        }
                                                        int t_j_2 = earliestDepartureTimeJ;

                                                        int t_jjj = depTimeOfLabelI + duration + instance.getShortConnectionTimeForDriver(); // earliest avoid short connection time 2024.4.18 for driver

                                                        if (t_jjj > t_j_2 && t_jjj <= latestDepartureTimeJ) {
                                                            t_j_2 = t_jjj;
                                                        }

                                                        Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j_1);
                                                        Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j_1);
                                                        Label labelJ3 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j_2);
                                                        Label labelJ4 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j_2);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 4;
                                                        if (labelJ1 != null) {
                                                            nodeWithLabelsJ.addLabel(labelJ1);
                                                            termination = false;
                                                        }
                                                        if (labelJ2 != null) {
                                                            nodeWithLabelsJ.addLabel(labelJ2);
                                                            termination = false;
                                                        }
                                                        if (labelJ3 != null) {
                                                            nodeWithLabelsJ.addLabel(labelJ3);
                                                            termination = false;
                                                        }
                                                        if (labelJ4 != null) {
                                                            nodeWithLabelsJ.addLabel(labelJ4);
                                                            termination = false;
                                                        }
                                                    } else {
                                                        //System.out.println("dela_t; zij is all zero");
                                                        // Case3: only extend to minPlan

                                                        int t_j_1 = earliestDepartureTimeJ;
                                                        int t_jj = depTimeOfLabelI + duration + instance.getMinPlanTurnTime();// earliest  feasible time for driver and passenger
                                                        if (t_jj > t_j_1 && t_jj <= latestDepartureTimeJ) {
                                                            t_j_1 = t_jj;
                                                        }

                                                        Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j_1);
                                                        Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j_1);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                        if (labelJ1 != null) {
                                                            nodeWithLabelsJ.addLabel(labelJ1);
                                                            termination = false;
                                                        }
                                                        if (labelJ2 != null) {
                                                            nodeWithLabelsJ.addLabel(labelJ2);
                                                            termination = false;
                                                        }
                                                    }
                                                    //初始列少，RMP解差，dual value也很偏，这时候不能只盯着 strict dual，应该多尝试，aggressive多扩展一些可能有潜力的出发时间，防止陷入局部最优。
                                                    // System.out.println("nbLabel Genetated " + nbGenerateLabelsWithGivenDepot);
                                                }
                                                //****************************************************************************************above code according to the dual value to extend into three different case**********************
                                            } else {
                                                /**
                                                 *  here is the Case 1.2.2: Node J is trip type; Node I is trip type;There are already some labels on J before
                                                 *  */
                                                // 不管受不受时间窗影响，可以统一写为max{earliestDepartureTimeJ,depTimeOfLabelI + duration + instance.getMinPlanTurnTime()}
                                                // 这里受时间窗影响的话，从上一次的出发时间，找到feasible的时间 开始拓展

                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ej = (int) Math.round(earliestDepartureTimeJ / timeUnit);
                                                int nbU_Lj = (int) Math.round(latestDepartureTimeJ / timeUnit);

                                                boolean whetherDelta_j_t_IsStrictNegative = false;

                                                for (int n_j = nbU_Ej; n_j <= nbU_Lj; n_j++) {
                                                    int t_j = n_j * timeUnit;
                                                    double delta_j_t = this.masterProblem.getDualValueFromLinkSelectScheduleAndTripOneDepTime(tripJ.getIdOfTrip(), t_j);
                                                    if (delta_j_t < -epsilon) {
                                                        System.out.println("Pay attention delta_" + delta_j_t);
                                                        whetherDelta_j_t_IsStrictNegative = true;
                                                        break;
                                                    }
                                                }

                                                boolean whetherZeta_ij_IsStrictNegative = false;
                                                double zeta_ij = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripI.getIdOfTrip(), tripJ.getIdOfTrip());
                                                if (zeta_ij < -epsilon) {
                                                    whetherZeta_ij_IsStrictNegative = true;
                                                    //System.out.println("there is zeta_ij is strict negative" + zeta_ij);
                                                }

                                                //change extend according to the dual information
                                                if (whetherDelta_j_t_IsStrictNegative) {
                                                    for (int n_j = nbU_Ej; n_j <= nbU_Lj; n_j++) {//part 4: change to consider each time unite instead of only one extension
                                                        int t_j = n_j * timeUnit;
                                                        Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j);
                                                        Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                        //********************** below here is the dominance rule  ****************************************
                                                        if (labelJ1 != null && labelJ2 != null) {
                                                            double reducedCost1 = labelJ1.getTotalReducedCost();
                                                            double reducedCost2 = labelJ2.getTotalReducedCost();
                                                            //there are some existing labels which need to try to compare the earlier labels
                                                            //check the dominance before add the new label
                                                            if (reducedCost1 < reducedCost2) {
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            } else {
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            }
                                                        } else if (labelJ1 != null && labelJ2 == null) {
                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
                                                            if (isLabelAdded1) {
                                                                termination = false;
                                                            }

                                                        } else if (labelJ1 == null && labelJ2 != null) {
                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
                                                            if (isLabelAdded2) {
                                                                termination = false;
                                                            }
                                                        }

                                                    }
                                                } else {
                                                    // delta_t are all zero, now we check the value of zeta
                                                    if (whetherZeta_ij_IsStrictNegative) {
                                                        // extend to minplan and short connection
                                                        int t_j_1 = earliestDepartureTimeJ;
                                                        int t_jj = depTimeOfLabelI + duration + instance.getMinPlanTurnTime();// earliest  feasible time for driver and passenger
                                                        if (t_jj > t_j_1 && t_jj <= latestDepartureTimeJ) {
                                                            t_j_1 = t_jj;
                                                        }
                                                        int t_j_2 = earliestDepartureTimeJ;

                                                        int t_jjj = depTimeOfLabelI + duration + instance.getShortConnectionTimeForDriver(); // earliest avoid short connection time 2024.4.18 for driver

                                                        if (t_jjj > t_j_2 && t_jjj <= latestDepartureTimeJ) {
                                                            t_j_2 = t_jjj;
                                                        }

                                                        //int t_j_3=latestDepartureTimeJ-1;
                                                        Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j_1);
                                                        Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j_1);
                                                        Label labelJ3 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j_2);
                                                        Label labelJ4 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j_2);

                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 4;
                                                        // 收集非空标签
                                                        List<Label> candidateLabels = new ArrayList<>();
                                                        if (labelJ1 != null) candidateLabels.add(labelJ1);
                                                        if (labelJ2 != null) candidateLabels.add(labelJ2);
                                                        if (labelJ3 != null) candidateLabels.add(labelJ3);
                                                        if (labelJ4 != null) candidateLabels.add(labelJ4);

// 按 reduced cost 升序排序
                                                        candidateLabels.sort(Comparator.comparingDouble(Label::getTotalReducedCost));
// 依次检查并插入（按 reduced cost 顺序）
                                                        for (Label label : candidateLabels) {
                                                            boolean isAdded = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, label);
                                                            if (isAdded) {
                                                                termination = false;
                                                            }
                                                        }

                                                    } else {
                                                        // only extend to the minPlan
                                                        int t_j_1 = earliestDepartureTimeJ;
                                                        int t_jj = depTimeOfLabelI + duration + instance.getMinPlanTurnTime();// earliest  feasible time for driver and passenger
                                                        if (t_jj > t_j_1 && t_jj <= latestDepartureTimeJ) {
                                                            t_j_1 = t_jj;
                                                        }
                                                        Label labelJ1 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 1, t_j_1);
                                                        Label labelJ2 = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), 0, t_j_1);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
//
//
                                                        // below here is to add without dominance rule
//                                                        if (labelJ1 != null && labelJ2 != null) {
//                                                            double reducedCostJ1 = labelJ1.getTotalReducedCost();
//                                                            double reducedCostJ2 = labelJ2.getTotalReducedCost();
                                                        //Case1 :if there is no any label yet, we put the two label by the reduced cost increasing way
//                                                            if (reducedCostJ1 < reducedCostJ2) {
//                                                                nodeWithLabelsJ.addLabel(labelJ1);
//                                                                nodeWithLabelsJ.addLabel(labelJ2);
//                                                                termination = false;
//                                                            } else {
//                                                                nodeWithLabelsJ.addLabel(labelJ2);
//                                                                nodeWithLabelsJ.addLabel(labelJ1);
//                                                                termination = false;
//                                                            }
//                                                        } else if (labelJ1 != null && labelJ2 == null) {
//                                                            nodeWithLabelsJ.addLabel(labelJ1);
//                                                            termination = false;
//                                                        } else if (labelJ1 == null && labelJ2 != null) {
//                                                            nodeWithLabelsJ.addLabel(labelJ2);
//                                                            termination = false;
//                                                        }
                                                        // above here is to add without dominance rule
//                                                        //********************** below here is the dominance rule  ****************************************
                                                        if (labelJ1 != null && labelJ2 != null) {
                                                            double reducedCost1 = labelJ1.getTotalReducedCost();
                                                            double reducedCost2 = labelJ2.getTotalReducedCost();
                                                            //there are some existing labels which need to try to compare the earlier labels
                                                            // check the dominance before add the new label
                                                            if (reducedCost1 < reducedCost2) {
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            } else {
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            }

                                                        } else if (labelJ1 != null && labelJ2 == null) {
                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ1);
                                                            if (isLabelAdded1) {
                                                                nodeWithLabelsJ.addLabel(labelJ1);
                                                                termination = false;
                                                            }


                                                        } else if (labelJ1 == null && labelJ2 != null) {
                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ2);
                                                            if (isLabelAdded2) {
                                                                termination = false;
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        //****************************************************************************************************************************
                                    } else if (nodeWithLabelsJ.getNewNode().getNodeType().equals(endingDepotType)) {
                                        //case 3: from a trip to a ending depot
                                        // Here is Situation 2: the neighbor is ending depot, then we will extend to non-defined driving status
                                        Label labelJ = labelOfI.extendToNextNode(nodeWithLabelsJ.getNewNode(), -1, -1);// TODO: check the secondDepTime whether influence
                                        //nodeWithLabelsJ.addLabel(labelJ);
                                        boolean isLabelAdded = checkDominanceAddNonDominatedLabel_ForwardLabeling(nodeWithLabelsJ, labelJ);
                                        if (isLabelAdded) {
                                            termination = false;
                                        }
                                    }
                                }
                            }
                        }
                        labelOfI.setWhetherBeExtendedToNextNode(true);//防止死循环，应该上了就设置I被扩展了
                    }
                }
            }
        }

        // List<Label> minReducedCostLabels = new ArrayList<>(); // Initialize an empty list  because there maybe more than one mini-Reduced cost labels
        //改成
        List<Label> negativeReducedCostLabels = new ArrayList<>();

        for (NodeWithLabels nodeWithLabels : listOfNodeWithLabels) {
            if (nodeWithLabels.getNewNode().getNodeType().equals(endingDepotType) && !nodeWithLabels.getLabels().isEmpty()) {
                for (int l = 0; l < nodeWithLabels.getLabels().size(); l++) {
                    Label label = nodeWithLabels.getLabels().get(l);
                    if (label != null && label.checkAtLeastOneIsDriving()
                            //here is to ensure the feasible path has at least one drip
                            && label.getTotalReducedCost() <= 0 + epsilon) {
                        double negReducedCost = label.getTotalReducedCost();
                        negativeReducedCostLabels.add(label);
                    }
                }
            }
        }

        if (!negativeReducedCostLabels.isEmpty()) {
            //step1: sort label by the reduced cost(the most negative first)

            System.out.println(negativeReducedCostLabels.get(0).getTotalReducedCost());
            negativeReducedCostLabels.sort(Comparator.comparingDouble(Label::getTotalReducedCost));

            System.out.println(negativeReducedCostLabels.get(0).getTotalReducedCost());

            //step2: get the first GivenNumberNegLabels, and keep the minReduced cost
            List<Label> givenNegRedLabels = negativeReducedCostLabels.subList(0, Math.min(nbNegSchedulesToGenEachDepot, negativeReducedCostLabels.size()));
            minReducedCost_ForwardLabel = givenNegRedLabels.get(0).getTotalReducedCost();

            //Step3: firstNeg labels
            for (Label givenNegReducedCostLabel : givenNegRedLabels) {
                DriverSchedule driverSchedule = new DriverSchedule(this.graphRelatedToGivenDepot.getInstance());
                driverSchedule.setIdOfDepot(this.graphRelatedToGivenDepot.getGivenDepot().getIdOfDepot());
                driverSchedule.setIndexDepotAsStartingPoint(this.graphRelatedToGivenDepot.getGivenDepot().getIndexOfDepotAsStartingPoint());
                driverSchedule.setIndexDepotAsEndingPoint(this.graphRelatedToGivenDepot.getGivenDepot().getIndexOfDepotAsEndingPoint());
                for (TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime : givenNegReducedCostLabel.getSequenceOfTripsWithWorkingStatusAndDepartureTime()) {
                    driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTime);
                }

////// here i add some information to check where is the better schedule in the final integera solution
                DriverSchedule comparedSchedule = new DriverSchedule(this.graphRelatedToGivenDepot.getInstance());

                comparedSchedule.setIdOfDepot(1);
                comparedSchedule.setIndexDepotAsStartingPoint(26);
                comparedSchedule.setIndexDepotAsEndingPoint(28);



                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(7), true, 60));
                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(8), true, 135));
                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(9), true, 210));
                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(10), true, 270));
                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(11), true, 360));
                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(12), true, 435));
                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(13), true, 495));




//                comparedSchedule.setIdOfDepot(1);
//                comparedSchedule.setIndexDepotAsStartingPoint(51);
//                comparedSchedule.setIndexDepotAsEndingPoint(53);
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(47), true, 170));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(48), true, 230));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(49), true, 305));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(19), true, 365));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(20), true, 425));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(23), true, 500));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(31), true, 575));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(32), true, 700));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(36), true, 785));
//
//
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(30), true, 405));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(34), true, 465));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(18), true, 525));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(24), true, 590));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(25), true, 660));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(26), true, 725));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(4), true, 800));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(37), true, 870));
//                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(6), true, 930));
//
//
//                comparedSchedule.setIdOfDepot(0);                // depotId
//                comparedSchedule.setIndexDepotAsStartingPoint(50);
//                comparedSchedule.setIndexDepotAsEndingPoint(52);
//
//// 构造 trip 列表（tripId, drivingStatus, departureTime）
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(32), false, 700));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(33), true, 780));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(5), true, 840));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(27), true, 900));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(28), true, 960));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(29), true, 1020));
//
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(7), true, 30));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(8), true, 105));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(9), true, 190));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(10), true, 290));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(11), true, 360));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(12), true, 465));
////                comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(this.graphRelatedToGivenDepot.getInstance().getTrip(13), true, 525));
                if(driverSchedule.isExactlySameAs(comparedSchedule)){
                    System.out.println("Check to Add driver schedule with exactly same with negative reduced cost"+ driverSchedule+" with reduced cost "+givenNegReducedCostLabel.getTotalReducedCost());
                }

                if(driverSchedule.isSameStructureIgnoreDepartureTime(comparedSchedule)){
                    System.out.println("Check to Add driver schedule with same structure the with negative reduced cost: " + driverSchedule + " with reduced cost" + givenNegReducedCostLabel.getTotalReducedCost());


//                    System.out.println("Check to the driver schedule reduced cost calculated by hand" + driverSchedule
//                            + " with reduced cost " + driverSchedule.getReducedCost(masterProblem.getDualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect(),
//                            masterProblem.getDualFromConstraintOneDriving(),
//                            masterProblem.getDualFromConstraintLinkShortConnection(),
//                            masterProblem.getDualFromConstraintAvailableDriver()));
                }
                //
                driverSchedules.add(driverSchedule);
                realNbGeneratedNegReducedCostSchedule++;
                System.out.println("Add driver schedule in the with negative reduced cost: " + driverSchedule + " with reduced cost" + givenNegReducedCostLabel.getTotalReducedCost());
            }
        }
        long endLabelingTime = System.currentTimeMillis();
        durationOfLabelingInMilliSec = endLabelingTime - startLabeling;
        //System.out.println("The number of schedules we generated  negative reduced cost is: "+driverSchedules.size());

        return driverSchedules;
    }

    /**
     * //上面是forward labeling
     * //下面是需要一个从endDepot 出发的backward labeling algorithm 要注意这个初始化node的时候是否是starting Depot 选false
     */

    public ArrayList<DriverSchedule> generateNegReducedCostPathsBasedOnAGivenEndingDepotBackwardLabeling() {
        long startLabeling = System.currentTimeMillis();

        //Step1: define a list used for contains all the feasible paths
        ArrayList<DriverSchedule> driverSchedules = new ArrayList<>();

        //Step2: sort all the trips
        ArrayList<Trip> allTrips = new ArrayList<>();
        for (int i = 0; i < this.graphRelatedToGivenDepot.getInstance().getNbTrips(); i++) {
            allTrips.add(this.graphRelatedToGivenDepot.getInstance().getTrip(i));
        }
        Collections.sort(allTrips, TripComparators.BY_LATEST_DEPARTURE);//不同点1： change from latest departurue sorting
        //System.out.println("after sort by latest departure " + allTrips);

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
        NewNode nodeAsEndingDepot = new NewNode(this.graphRelatedToGivenDepot.getGivenDepot(), false);//change
        NodeWithLabels nodeWithLabels_EndingDepot = new NodeWithLabels(nodeAsEndingDepot);

        Label iniLabel = new Label(this.graphRelatedToGivenDepot, masterProblem, false);
        nodeWithLabels_EndingDepot.addLabel(iniLabel);
        listOfNodeWithLabels.add(nodeWithLabels_EndingDepot);

        NewNode nodeAsStartingDepot = new NewNode(this.graphRelatedToGivenDepot.getGivenDepot(), true);
        NodeWithLabels nodeWithLabelsAsStartingDepot = new NodeWithLabels(nodeAsStartingDepot);
        listOfNodeWithLabels.add(nodeWithLabelsAsStartingDepot);

        String startingDepotType = "startingDepot";
        String tripType = "trip";
        String endingDepotType = "endingDepot";
        double maxPlanTime = graphRelatedToGivenDepot.getInstance().getMaxPlanTime();

        boolean termination = false;

        // Step 6: 主循环（只考虑还未扩展的 label）
        while (termination != true) {
            termination = true;
            for (int n = 0; n < listOfNodeWithLabels.size(); n++) {
                NodeWithLabels node_J_WithLabels = listOfNodeWithLabels.get(n);
                if (!node_J_WithLabels.getNewNode().getNodeType().equals(startingDepotType) && (node_J_WithLabels.getLabels().size() != 0)) { //不同点2：这里只考虑 current nodeJ 是 trip及endingDepot才可能向前拓展，所以关注点也从Node I 上的标签变成了 node J上的标签
                    //considering all the trips and ending depot(those whole could have preNode)
                    // so now the label could be from the start depot or the trip
                    LinkedList<Label> labels = node_J_WithLabels.getLabels();
                    // Create ListIterator for the labels within nodeWithLabels
                    ListIterator<Label> labelIterator = labels.listIterator();
                    // Iterate through the labels using a ListIterator // for all the label of current nodeWithLabels j
                    while (labelIterator.hasNext()) {
                        Label labelOfJ = labelIterator.next();
                        // Check if labelOfI is not null and has not been extended yet
                        if (labelOfJ != null && (!labelOfJ.getWhetherBeExtendedFromPreNode())) {//Here we just choose those labels which are not be extended yet
                            int depTimeOfLabelJ = labelOfJ.getCurrentNodeDepTime();
                            int idOfNodeOfLabelJ = labelOfJ.getCurrentNode().getId();
                            for (int m = 0; m < listOfNodeWithLabels.size(); m++) {
                                NodeWithLabels nodeWithLabelsI = listOfNodeWithLabels.get(m);// for all nodeWithLabels I
                                double arcWeightOnBetweenTwoNodesOnNewGraph = graphRelatedToGivenDepot.getArcWeightOnBetweenTwoNodesOnNewGraph(nodeWithLabelsI.getNewNode(), node_J_WithLabels.getNewNode());//不同点3：检查是否有边的方向也变了
                                if (arcWeightOnBetweenTwoNodesOnNewGraph <= maxPlanTime && labelOfJ.getTotalDrivingTime() <= graphRelatedToGivenDepot.getInstance().getMaxDrivingTime()) {
                                    //only try to extend from the pre- neighbour node
                                    int idOfFormerNodeI = nodeWithLabelsI.getNewNode().getId();
                                    /**
                                     * // it could also start from deport or start from trip, which have different departure time; we need to separately extend
                                     * it could be a trip or the ending depot type, thus we need to separately extend
                                     * */
                                    if (nodeWithLabelsI.getNewNode().getNodeType().equals(tripType) && (labelOfJ.getUnreachableTripId().get(idOfFormerNodeI)) == false) {
                                        /**
                                         *  here is the Case 1: Node Z is ending depot type
                                         *  */

                                        //Here is Situation 1: the extended neighbor is a reachable trip, then we extend to driving status and passenger status
                                        int earliestDepartureTimeI = nodeWithLabelsI.getNewNode().getTrip().getEarliestDepartureTime();
                                        int latestDepartureTimeI = nodeWithLabelsI.getNewNode().getTrip().getLatestDepartureTime();
                                        Instance instance = this.graphRelatedToGivenDepot.getInstance();
                                        Trip tripI = instance.getTrip(nodeWithLabelsI.getNewNode().getId());

                                        if (labelOfJ.getCurrentNode().getNodeType().equals(endingDepotType)) {
                                            /**
                                             *  here is the Case 1.1: Node I is trip type; Node J is ending depot
                                             *  */
                                            // here special for time window need to think whether this label is null or not
                                            if (nodeWithLabelsI.getLabels().isEmpty()) {// add this new check for time attributes
                                                /**
                                                 *  here is the Case 1.1.1: Node I is trip type; Node J is a ending depot; Node I has NO any labels on it
                                                 *  */
                                                // 这里也没进行dominance check受不管这里受不受，因为J是最后一个Tirp
                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ei = (int) Math.round(earliestDepartureTimeI / timeUnit);
                                                int nbU_Li = (int) Math.round(latestDepartureTimeI / timeUnit);

                                                for (int n_i = nbU_Ei; n_i <= nbU_Li; n_i++) {
                                                    int t_i = n_i * timeUnit;
                                                    Label labelI1 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i);//使用extend from
                                                    Label labelI2 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i);////使用extend from
                                                    nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                    if (labelI1 != null && labelI2 != null) {
                                                        double reducedCostI1 = labelI1.getTotalReducedCost();
                                                        double reducedCostI2 = labelI2.getTotalReducedCost();
                                                        //Case1 :if there is no any label yet, we put the two label by the reduced cost increasing way
                                                        if (reducedCostI1 < reducedCostI2) {
                                                            nodeWithLabelsI.addLabel(labelI1);
                                                            nodeWithLabelsI.addLabel(labelI2);
                                                            termination = false;
                                                        } else {
                                                            nodeWithLabelsI.addLabel(labelI2);
                                                            nodeWithLabelsI.addLabel(labelI1);
                                                            termination = false;
                                                        }
                                                    } else if (labelI1 != null && labelI2 == null) {
                                                        nodeWithLabelsI.addLabel(labelI1);
                                                        termination = false;
                                                    } else if (labelI1 == null && labelI2 != null) {
                                                        nodeWithLabelsI.addLabel(labelI2);
                                                        termination = false;
                                                    }
                                                }


                                            } else {
                                                /**
                                                 *  here is the Case 1.1.2: Node I is trip type; Node J is a ending depot; Node I has already some labels on it
                                                 *  */
                                                // 这里不管这里受不受时间窗影响的话，暂时也考虑所有的开始时间窗，因为A是最后一个Trip
                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ei = (int) Math.round(earliestDepartureTimeI / timeUnit);
                                                int nbU_Li = (int) Math.round(latestDepartureTimeI / timeUnit);
                                                for (int n_i = nbU_Ei; n_i <= nbU_Li; n_i++) {//part2: change to unit folder 2025.4.18
                                                    int t_i = n_i * timeUnit;
                                                    Label labelI1 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i);
                                                    Label labelI2 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i);
                                                    nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
//                                                    //********************** below here is the dominance rule 1.1 ****************************************
                                                    if (labelI1 != null && labelI2 != null) {
                                                        double reducedCostI1 = labelI1.getTotalReducedCost();
                                                        double reducedCostI2 = labelI2.getTotalReducedCost();
                                                        if (reducedCostI1 < reducedCostI2) {
                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);// 带修改根据backwarddominance rule来添加的
                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                            if (isLabelAdded1 || isLabelAdded2) {
                                                                termination = false;
                                                            }
                                                        } else {
                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                            if (isLabelAdded1 || isLabelAdded2) {
                                                                termination = false;
                                                            }
                                                        }
                                                    } else if (labelI1 != null && labelI2 == null) {
                                                        boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                        if (isLabelAdded1) {
                                                            termination = false;
                                                        }
                                                    } else if (labelI1 == null && labelI2 != null) {
                                                        boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                        if (isLabelAdded2) {
                                                            termination = false;
                                                        }
                                                    }
                                                }
                                                //System.out.println("nb Label generated"+nbGenerateLabelsWithGivenDepot);
                                            }
                                        } else {
                                            /**
                                             *  here is the Case 1.2: Node J is trip type; Node I is trip type
                                             *  */
                                            Trip tripJ = instance.getTrip(idOfNodeOfLabelJ);
                                            int durationTripI = tripI.getDuration();
                                            if (nodeWithLabelsI.getLabels().isEmpty()) {// add this new check for time attributes
                                                /**
                                                 *  here is the Case 1.2.1: Node J is trip type; Node I is trip type;There is no label on node I before
                                                 *  */

                                                boolean whetherDelta_i_t_IsStrictNegative = false;
                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ei = (int) Math.round(earliestDepartureTimeI / timeUnit);
                                                int nbU_Li = (int) Math.round(latestDepartureTimeI / timeUnit);
                                                double sumDelta = 0;
                                                for (int n_i = nbU_Ei; n_i <= nbU_Li; n_i++) {
                                                    int t_i = n_i * timeUnit;
                                                    double delta_i_t = this.masterProblem.getDualValueFromLinkSelectScheduleAndTripOneDepTime(tripI.getIdOfTrip(), t_i);//对偶检查变量也变成前面哪个要拓展的点了
                                                    sumDelta = sumDelta + delta_i_t;
                                                    if (sumDelta < -epsilon) {
                                                        System.out.println("the sum of Delta is not zero");
                                                    }
                                                    if (delta_i_t < -epsilon) {
                                                        System.out.println("there is negative delta_" + delta_i_t);
                                                        whetherDelta_i_t_IsStrictNegative = true;
                                                        break;
                                                    }
                                                }
                                                boolean whetherZeta_ij_IsStrictNegative = false;
                                                double zeta_ij = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripI.getIdOfTrip(), tripJ.getIdOfTrip());
                                                if (zeta_ij < -epsilon) {
                                                    whetherZeta_ij_IsStrictNegative = true;
                                                    System.out.println("there is zeta_ij is strict negative" + zeta_ij);
                                                }
                                                //***********根据dual 拓展
                                                if (whetherDelta_i_t_IsStrictNegative) {
                                                    System.out.println("there is there is extend Case 1: Exist t s.t. Delta is strict negative");
                                                    //Case1: extend to all possible choice
                                                    for (int n_i = nbU_Ei; n_i <= nbU_Li; n_i++) {
                                                        int t_i = n_i * timeUnit;
                                                        Label labelI1 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i);
                                                        Label labelI2 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                        if (labelI1 != null && labelI2 != null) {
                                                            double reducedCost1 = labelI1.getTotalReducedCost();
                                                            double reducedCost2 = labelI2.getTotalReducedCost();
                                                            if (reducedCost1 < reducedCost2) {
                                                                nodeWithLabelsI.addLabel(labelI1);
                                                                nodeWithLabelsI.addLabel(labelI2);
                                                                termination = false;
                                                            } else {
                                                                nodeWithLabelsI.addLabel(labelI2);
                                                                nodeWithLabelsI.addLabel(labelI1);
                                                                termination = false;
                                                            }

                                                        } else if (labelI1 != null && labelI2 == null) {
                                                            nodeWithLabelsI.addLabel(labelI1);
                                                            termination = false;

                                                        } else if (labelI1 == null && labelI2 != null) {
                                                            nodeWithLabelsI.addLabel(labelI2);
                                                            termination = false;

                                                        }

                                                    }

                                                } else {
                                                    if (whetherZeta_ij_IsStrictNegative) {
                                                        System.out.println("there is extend Case 2 zeta_ij is strict negative" + zeta_ij);
                                                        // Case2: extend to minPlan or short connection depend on zeta_ij
                                                        int t_i_1 = latestDepartureTimeI;
                                                        int t_ii = depTimeOfLabelJ - instance.getMinPlanTurnTime() - durationTripI;// latest feasible dep time for a driver and passenger
                                                        if (t_ii < t_i_1 && t_ii >= earliestDepartureTimeI) {
                                                            t_i_1 = t_ii;
                                                        }
                                                        int t_i_2 = latestDepartureTimeI;

                                                        int t_iii = depTimeOfLabelJ - instance.getShortConnectionTimeForDriver() - durationTripI; // latest feasible dep time for avoid short connection time 2024.4.18 for driver

                                                        if (t_iii < t_i_2 && t_iii >= earliestDepartureTimeI) {
                                                            t_i_2 = t_iii;
                                                        }

                                                        Label labelI1 = labelOfJ.extendToNextNode(nodeWithLabelsI.getNewNode(), 1, t_i_1);
                                                        Label labelI2 = labelOfJ.extendToNextNode(nodeWithLabelsI.getNewNode(), 0, t_i_1);
                                                        Label labelI3 = labelOfJ.extendToNextNode(nodeWithLabelsI.getNewNode(), 1, t_i_2);
                                                        Label labelI4 = labelOfJ.extendToNextNode(nodeWithLabelsI.getNewNode(), 0, t_i_2);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 4;
                                                        if (labelI1 != null) {
                                                            nodeWithLabelsI.addLabel(labelI1);
                                                            termination = false;
                                                        }
                                                        if (labelI2 != null) {
                                                            nodeWithLabelsI.addLabel(labelI2);
                                                            termination = false;
                                                        }
                                                        if (labelI3 != null) {
                                                            nodeWithLabelsI.addLabel(labelI3);
                                                            termination = false;
                                                        }
                                                        if (labelI4 != null) {
                                                            nodeWithLabelsI.addLabel(labelI4);
                                                            termination = false;
                                                        }
                                                    } else {
                                                        //System.out.println("dela_t; zij is all zero");
                                                        // Case3: only extend to minPlan
                                                        int t_i_1 = latestDepartureTimeI;
                                                        int t_ii = depTimeOfLabelJ - instance.getMinPlanTurnTime() - durationTripI;// latest feasible dep time for a driver and passenger
                                                        if (t_ii < t_i_1 && t_ii >= earliestDepartureTimeI) {
                                                            t_i_1 = t_ii;
                                                        }

                                                        Label labelI1 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i_1);
                                                        Label labelI2 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i_1);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                        if (labelI1 != null) {
                                                            nodeWithLabelsI.addLabel(labelI1);
                                                            termination = false;
                                                        }
                                                        if (labelI2 != null) {
                                                            nodeWithLabelsI.addLabel(labelI2);
                                                            termination = false;
                                                        }
                                                    }
                                                    //初始列少，RMP解差，dual value也很偏，这时候不能只盯着 strict dual，应该多尝试，aggressive多扩展一些可能有潜力的出发时间，防止陷入局部最优。
                                                    // System.out.println("nbLabel Genetated " + nbGenerateLabelsWithGivenDepot);
                                                }
                                                //****************************************************************************************above code according to the dual value to extend into three different case**********************
                                            } else {
                                                /**
                                                 *  here is the Case 1.2.2: Node J is trip type; Node I is trip type;There are already some labels on J before
                                                 *  */
                                                // 不管受不受时间窗影响，可以统一写为min{latestDepartureTimeJ,depTimeOfLabelZ- durationA - instance.getMinPlanTurnTime()}
                                                // 这里受时间窗影响的话，从上一次的出发时间，找到feasible的时间 开始拓展

                                                int timeUnit = instance.getTimeSlotUnit();
                                                int nbU_Ei = (int) Math.round(earliestDepartureTimeI / timeUnit);
                                                int nbU_Li = (int) Math.round(latestDepartureTimeI / timeUnit);

                                                boolean whetherDelta_i_t_IsStrictNegative = false;

                                                for (int n_i = nbU_Ei; n_i <= nbU_Li; n_i++) {
                                                    int t_i = n_i * timeUnit;
                                                    double delta_i_t = this.masterProblem.getDualValueFromLinkSelectScheduleAndTripOneDepTime(tripI.getIdOfTrip(), t_i);
                                                    if (delta_i_t < -epsilon) {
                                                        System.out.println("Pay attention delta_" + delta_i_t);
                                                        whetherDelta_i_t_IsStrictNegative = true;
                                                        break;
                                                    }
                                                }

                                                boolean whetherZeta_ij_IsStrictNegative = false;
                                                double zeta_ij = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripI.getIdOfTrip(), tripJ.getIdOfTrip());
                                                if (zeta_ij < -epsilon) {
                                                    whetherZeta_ij_IsStrictNegative = true;
                                                    //System.out.println("there is zeta_ij is strict negative" + zeta_ij);
                                                }
                                                //change extend according to the dual information
                                                if (whetherDelta_i_t_IsStrictNegative) {
                                                    for (int n_i = nbU_Ei; n_i <= nbU_Li; n_i++) {//part 4: change to consider each time unite instead of only one extension
                                                        int t_i = n_i * timeUnit;
                                                        Label labelI1 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i);
                                                        Label labelI2 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;
                                                        //********************** below here is the dominance rule  ****************************************
                                                        if (labelI1 != null && labelI2 != null) {
                                                            double reducedCost1 = labelI1.getTotalReducedCost();
                                                            double reducedCost2 = labelI2.getTotalReducedCost();
                                                            //there are some existing labels which need to try to compare the earlier labels
                                                            //check the dominance before add the new label
                                                            if (reducedCost1 < reducedCost2) {
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            } else {
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            }
                                                        } else if (labelI1 != null && labelI2 == null) {
                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                            if (isLabelAdded1) {
                                                                termination = false;
                                                            }

                                                        } else if (labelI1 == null && labelI2 != null) {
                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                            if (isLabelAdded2) {
                                                                termination = false;
                                                            }
                                                        }

                                                    }
                                                } else {
                                                    // delta_t are all zero, now we check the value of zeta
                                                    if (whetherZeta_ij_IsStrictNegative) {
                                                        // extend to minPlan and short connection
                                                        int t_i_1 = latestDepartureTimeI;
                                                        int t_ii = depTimeOfLabelJ - durationTripI - instance.getMinPlanTurnTime();// latest  feasible time for driver and passenger
                                                        if (t_ii < t_i_1 && t_ii >= earliestDepartureTimeI) {
                                                            t_i_1 = t_ii;
                                                        }
                                                        int t_i_2 = latestDepartureTimeI;

                                                        int t_iii = depTimeOfLabelJ - durationTripI - instance.getShortConnectionTimeForDriver(); // earliest avoid short connection time 2024.4.18 for driver

                                                        if (t_iii < t_i_2 && t_iii >= earliestDepartureTimeI) {
                                                            t_i_2 = t_iii;
                                                        }

                                                        //int t_j_3=latestDepartureTimeJ-1;
                                                        Label labelI1 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i_1);
                                                        Label labelI2 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i_1);
                                                        Label labelI3 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i_2);
                                                        Label labelI4 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i_2);
//                                                Label labelJ5 = labelOfI.extendTo(nodeWithLabelsJ.getNewNode(), 1, t_j_3);
//                                                Label labelJ6 = labelOfI.extendTo(nodeWithLabelsJ.getNewNode(), 0, t_j_3);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 4;
                                                        // 收集非空标签
                                                        List<Label> candidateLabels = new ArrayList<>();
                                                        if (labelI1 != null) candidateLabels.add(labelI1);
                                                        if (labelI2 != null) candidateLabels.add(labelI2);
                                                        if (labelI3 != null) candidateLabels.add(labelI3);
                                                        if (labelI4 != null) candidateLabels.add(labelI4);
//
// 按 reduced cost 升序排序
                                                        candidateLabels.sort(Comparator.comparingDouble(Label::getTotalReducedCost));
// 依次检查并插入（按 reduced cost 顺序）
                                                        for (Label label : candidateLabels) {
                                                            boolean isAdded = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, label);
                                                            if (isAdded) {
                                                                termination = false;
                                                            }
                                                        }

                                                    } else {
                                                        // only extend to the minPlan
                                                        int t_i_1 = latestDepartureTimeI;
                                                        int t_ii = depTimeOfLabelJ - durationTripI - instance.getMinPlanTurnTime();// earliest  feasible time for driver and passenger
                                                        if (t_ii > t_i_1 && t_ii <= latestDepartureTimeI) {
                                                            t_i_1 = t_ii;
                                                        }
                                                        Label labelI1 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 1, t_i_1);
                                                        Label labelI2 = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), 0, t_i_1);
                                                        nbGenerateLabelsWithGivenDepot = nbGenerateLabelsWithGivenDepot + 2;

//                                                        //********************** below here is the dominance rule  ****************************************
                                                        if (labelI1 != null && labelI2 != null) {
                                                            double reducedCost1 = labelI1.getTotalReducedCost();
                                                            double reducedCost2 = labelI2.getTotalReducedCost();
                                                            //there are some existing labels which need to try to compare the earlier labels
                                                            // check the dominance before add the new label
                                                            if (reducedCost1 < reducedCost2) {
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            } else {
                                                                boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                                boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                                if (isLabelAdded1 || isLabelAdded2) {
                                                                    termination = false;
                                                                }
                                                            }

                                                        } else if (labelI1 != null && labelI2 == null) {
                                                            boolean isLabelAdded1 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI1);
                                                            if (isLabelAdded1) {
                                                                nodeWithLabelsI.addLabel(labelI1);
                                                                termination = false;
                                                            }

                                                        } else if (labelI1 == null && labelI2 != null) {
                                                            boolean isLabelAdded2 = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI2);
                                                            if (isLabelAdded2) {
                                                                termination = false;
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        //****************************************************************************************************************************
                                    } else if (nodeWithLabelsI.getNewNode().getNodeType().equals(startingDepotType)) {

                                        //case 3: from a trip to a ending depot
                                        // Here is Situation 2: the neighbor is ending depot, then we will extend to non-defined driving status
                                        Label labelI = labelOfJ.extendFromPreviousNode(nodeWithLabelsI.getNewNode(), -1, -1);// TODO: check the secondDepTime whether influence
                                        //nodeWithLabelsJ.addLabel(labelJ);
                                        boolean isLabelAdded = checkDominanceAddNonDominatedLabel_BackwardLabeling(nodeWithLabelsI, labelI);
                                        if (isLabelAdded) {
                                            termination = false;
                                        }
                                    }
                                }
                            }
                        }
                        labelOfJ.setWhetherBeExtendedFromPreNode(true);//防止死循环，应该上了就设置J被扩展了
                    }
                }
            }
        }
//        List<Label> minReducedCostLabels = new ArrayList<>(); // Initialize an empty list  because there maybe more than one mini-Reduced cost labels
        List<Label> negativeReducedCostLabels = new ArrayList<>();

        for (NodeWithLabels nodeWithLabels : listOfNodeWithLabels) {
            if (nodeWithLabels.getNewNode().getNodeType().equals(startingDepotType) && !nodeWithLabels.getLabels().isEmpty()) {
                for (int l = 0; l < nodeWithLabels.getLabels().size(); l++) {
                    Label label = nodeWithLabels.getLabels().get(l);
                    if (label != null && label.checkAtLeastOneIsDriving()
                            //here is to ensure the feasible path has at least one drip
                            && label.getTotalReducedCost() <= 0 + epsilon) {
                        double negReducedCost = label.getTotalReducedCost();
                        negativeReducedCostLabels.add(label);
                    }
                }
            }
        }
        //System.out.println("check backward negative reduced cost labels size "+negativeReducedCostLabels.size());

        if (!negativeReducedCostLabels.isEmpty()) {
            //step1: sort label by the reduced cost(the most negative first)
            //System.out.println(negativeReducedCostLabels.get(0).getTotalReducedCost());

            negativeReducedCostLabels.sort(Comparator.comparingDouble(Label::getTotalReducedCost));

            System.out.println("Here is the negative"+negativeReducedCostLabels.get(0).getTotalReducedCost());

            //step2: get the first GivenNumberNegLabels, and keep the minReduced cost
            List<Label> givenNegRedLabels = negativeReducedCostLabels.subList(0, Math.min(nbNegSchedulesToGenEachDepot, negativeReducedCostLabels.size()));
            minReducedCost_BackwardLabel = givenNegRedLabels.get(0).getTotalReducedCost();
            System.out.println("Here we updated the backward minReducedCost"+minReducedCost_BackwardLabel);

            if (minReducedCost == Double.MAX_VALUE) {
                System.out.println("WARNING: Backward labeling failed to find any schedule for depot_" + this.graphRelatedToGivenDepot.getGivenDepot().getIdOfDepot());
                minReducedCost = Double.POSITIVE_INFINITY; // or ignore backward result
            }
            //Step3: firstNeg labels
            for (Label givenNegReducedCostLabel : givenNegRedLabels) {
                DriverSchedule driverSchedule = new DriverSchedule(this.graphRelatedToGivenDepot.getInstance());
                driverSchedule.setIdOfDepot(this.graphRelatedToGivenDepot.getGivenDepot().getIdOfDepot());
                driverSchedule.setIndexDepotAsStartingPoint(this.graphRelatedToGivenDepot.getGivenDepot().getIndexOfDepotAsStartingPoint());
                driverSchedule.setIndexDepotAsEndingPoint(this.graphRelatedToGivenDepot.getGivenDepot().getIndexOfDepotAsEndingPoint());


                List<TripWithWorkingStatusAndDepartureTime> tripList = new ArrayList<>(givenNegReducedCostLabel.getSequenceOfTripsWithWorkingStatusAndDepartureTime());

                Collections.reverse(tripList); // 反转顺序 不同点

                for (TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime : tripList) {
                    driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTime);
                }


                driverSchedules.add(driverSchedule);
                realNbGeneratedNegReducedCostSchedule++;
                System.out.println("Add driver schedule by backward labeling in the with negative reduced cost: " + driverSchedule + " with reduced cost" + givenNegReducedCostLabel.getTotalReducedCost());
                System.out.println("Check the driver schedule by backward labeling  reduced cost calculated by hand" + driverSchedule
                        + " with reduced cost " + driverSchedule.getReducedCost(masterProblem.getDualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect(),
                        masterProblem.getDualFromConstraintOneDriving(),
                        masterProblem.getDualFromConstraintLinkShortConnection(),
                        masterProblem.getDualFromConstraintAvailableDriver()));

            }


        }
        long endLabelingTime = System.currentTimeMillis();
        durationOfLabelingInMilliSec = endLabelingTime - startLabeling;
        //System.out.println("The number of schedules we generated  negative reduced cost is: "+driverSchedules.size());
        return driverSchedules;
    }

    private boolean checkDominanceAddNonDominatedLabel_ForwardLabeling(NodeWithLabels nodeWithLabelsJ, Label newLabel) {
        double reducedCostOfNewLabel = newLabel.getTotalReducedCost();
        LinkedList<Label> existingLabelsInNodeJ = nodeWithLabelsJ.getLabels();
        // Create a ListIterator for the labels within nodeWithLabelsJ
        ListIterator<Label> existingLabelIterator = existingLabelsInNodeJ.listIterator();
        // Iterate through the labels in nodeWithLabelsJ using a ListIterator
        int counter = 0;
        boolean newLabelIsDominated = false;
        boolean newLabelInserted = false;

        while (existingLabelIterator.hasNext()) {
            Label labelJ = existingLabelIterator.next();
            double reducedCostJ = labelJ.getTotalReducedCost();
            int departureTimeOfExistingLabelJ = labelJ.getCurrentNodeDepTime();
            int departureTimeOfNewLabel = newLabel.getCurrentNodeDepTime();
            int idOfCurrentTrip = labelJ.getCurrentNode().getId();

            if (reducedCostJ < reducedCostOfNewLabel - epsilon) {
                if (this.isLabel_FDominatesLabel_S_ForwardLabeling(labelJ, newLabel)) {
                    return false;
                }
            } else if (Math.abs(reducedCostJ - reducedCostOfNewLabel) <= epsilon) {
                if (this.isLabel_FDominatesLabel_S_ForwardLabeling(labelJ, newLabel)) {
                    return false;
                }
                if (isLabel_FDominatesLabel_S_ForwardLabeling(newLabel, labelJ)) {
                    // remove oldLabel J from the existingList
                    existingLabelIterator.remove();
                }
            } else {
                counter++;
                if (counter == 1) {
                    /**
                     * here is to insert the new label and ensure it will be put by increasing reduced cost order*/
                    existingLabelIterator.previous();
                    // here we want to add the label before the current one to keep the list in the reduced cost increasing order,
                    // so we first change the adverse iteration direction, by using previous() to put the current label with smaller reduced cost
                    existingLabelIterator.add(newLabel);// then add the elements
                    existingLabelIterator.next();// then change back to the direction next() to move forward,
                }
                if (isLabel_FDominatesLabel_S_ForwardLabeling(newLabel, labelJ)) {
                    existingLabelIterator.remove();
                }
            }
        }
        if (counter == 0) { //if all the while loop run, and there the new label never comes to the third part compare
            // and never be dominated in the first two steps, so we need to add this non-dominated label
            existingLabelsInNodeJ.add(newLabel);
        }
        return true;
    }


    private boolean isLabel_FDominatesLabel_S_ForwardLabeling(Label labelF, Label labelS) {
        boolean isLabel_FDominatesLabel_S = false;
        //Comment:  boolean type check at least one driving, when we check we need check each element  of the list
        boolean atLeastOneIsDrivingLabelF = labelF.checkAtLeastOneIsDriving();
        boolean atLeastOneIsDrivingLabelS = labelS.checkAtLeastOneIsDriving();
        if (atLeastOneIsDrivingLabelF && atLeastOneIsDrivingLabelS) {
            if (labelF.checkWhetherDrivingStatusIsSame(labelS)) {
                if (labelF.whetherThisDominatesUnderSameDriveStatusForwardLabelingAlgorithm(labelS)) {
                    //Dominance 1
                    isLabel_FDominatesLabel_S = true;
                }
            } else {
                if (labelF.whetherThisDominatesUnderDifferentDrivingStatusForwardLabelingAlgorithm(labelS)) {
                    //Dominance 2
                    isLabel_FDominatesLabel_S = true;
                }
            }
        } else if ((!atLeastOneIsDrivingLabelF) && (!atLeastOneIsDrivingLabelS)) {
            //Dominance 1
            if (labelF.whetherThisDominatesUnderSameDriveStatusForwardLabelingAlgorithm(labelS)) {
                isLabel_FDominatesLabel_S = true;
            }
        }
        return isLabel_FDominatesLabel_S;
    }


    //Here are the method for the backward labeling algorithm


    private boolean isLabel_FDominatesLabel_S_BackwardLabeling(Label labelF, Label labelS) {
        boolean isLabel_FDominatesLabel_S = false;
        //Comment:  boolean type check at least one driving, when we check we need check each element  of the list
        boolean atLeastOneIsDrivingLabelF = labelF.checkAtLeastOneIsDriving();
        boolean atLeastOneIsDrivingLabelS = labelS.checkAtLeastOneIsDriving();
        if (atLeastOneIsDrivingLabelF && atLeastOneIsDrivingLabelS) {
            if (labelF.checkWhetherDrivingStatusIsSame(labelS)) {
                if (labelF.whetherThisDominatesUnderSameDriveStatusBackwardLabeling(labelS)) {
                    //Dominance 1
                    isLabel_FDominatesLabel_S = true;
                }
            } else {
                if (labelF.whetherThisDominatesUnderDifferentDriveStatusBackwardLabeling(labelS)) {
                    //Dominance 2
                    isLabel_FDominatesLabel_S = true;
                }
            }
        } else if ((!atLeastOneIsDrivingLabelF) && (!atLeastOneIsDrivingLabelS)) {
            //Dominance 1
            if (labelF.whetherThisDominatesUnderSameDriveStatusBackwardLabeling(labelS)) {
                isLabel_FDominatesLabel_S = true;
            }
        }
        return isLabel_FDominatesLabel_S;
    }

    private boolean checkDominanceAddNonDominatedLabel_BackwardLabeling(NodeWithLabels nodeWithLabelsJ, Label newLabel) {
        double reducedCostOfNewLabel = newLabel.getTotalReducedCost();
        LinkedList<Label> existingLabelsInNodeJ = nodeWithLabelsJ.getLabels();
        // Create a ListIterator for the labels within nodeWithLabelsJ
        ListIterator<Label> existingLabelIterator = existingLabelsInNodeJ.listIterator();
        // Iterate through the labels in nodeWithLabelsJ using a ListIterator
        int counter = 0;
        boolean newLabelIsDominated = false;
        boolean newLabelInserted = false;

        while (existingLabelIterator.hasNext()) {
            Label labelJ = existingLabelIterator.next();
            double reducedCostJ = labelJ.getTotalReducedCost();
            int departureTimeOfExistingLabelJ = labelJ.getCurrentNodeDepTime();
            int departureTimeOfNewLabel = newLabel.getCurrentNodeDepTime();
            int idOfCurrentTrip = labelJ.getCurrentNode().getId();

            if (reducedCostJ < reducedCostOfNewLabel - epsilon) {
                if (this.isLabel_FDominatesLabel_S_BackwardLabeling(labelJ, newLabel)) {
                    return false;
                }
            } else if (Math.abs(reducedCostJ - reducedCostOfNewLabel) <= epsilon) {
                if (this.isLabel_FDominatesLabel_S_BackwardLabeling(labelJ, newLabel)) {
                    return false;
                }
                if (isLabel_FDominatesLabel_S_BackwardLabeling(newLabel, labelJ)) {
                    // remove oldLabel J from the existingList
                    existingLabelIterator.remove();
                }
            } else {
                counter++;
                if (counter == 1) {
                    /**
                     * here is to insert the new label and ensure it will be put by increasing reduced cost order*/
                    existingLabelIterator.previous();
                    // here we want to add the label before the current one to keep the list in the reduced cost increasing order,
                    // so we first change the adverse iteration direction, by using previous() to put the current label with smaller reduced cost
                    existingLabelIterator.add(newLabel);// then add the elements
                    existingLabelIterator.next();// then change back to the direction next() to move forward,
                }
                if (isLabel_FDominatesLabel_S_BackwardLabeling(newLabel, labelJ)) {
                    existingLabelIterator.remove();
                }
            }
        }
        if (counter == 0) { //if all the while loop run, and there the new label never comes to the third part compare
            // and never be dominated in the first two steps, so we need to add this non-dominated label
            existingLabelsInNodeJ.add(newLabel);
        }
        return true;
    }

    public double getMinReducedCost_ForwardLabel() {
        return minReducedCost_ForwardLabel;
    }

    public double getMinReducedCost_BackwardLabel() {
        return minReducedCost_BackwardLabel;
    }

    public double getMinReducedCost() {
        return minReducedCost=Math.min(this.getMinReducedCost_ForwardLabel(),this.getMinReducedCost_BackwardLabel());
    }

    public int getNbGenerateLabelsWithGivenDepot() {
        return nbGenerateLabelsWithGivenDepot;
    }

    public double getDurationOfDominanceInMilliSecondTime() {
        return durationOfDominanceInMilliSec;
    }

    public double getDurationOfLabelingInMilliSec() {
        return durationOfLabelingInMilliSec;
    }


    @Override
    public String toString() {
        return "PathsGeneratorBasedOnGivenDepot{" +
                '}';
    }

    public static void main(String[] args) throws IOException {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips050_combPer0.25_TW6.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);

        SchedulesReader schedulesReader = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips050_combPer0.25_TW6.txt", instance);
        Solution initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);
        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());

        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
        masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况

        GraphRelatedToGivenDepot graphRelatedToGivenDepot1 = new GraphRelatedToGivenDepot(instance, instance.getDepot(1));

        PathsGeneratorBasedOnGivenDepot pathsGeneratorBasedOnGivenDepot = new PathsGeneratorBasedOnGivenDepot(graphRelatedToGivenDepot1, masterProblem);
        //System.out.println("schedule: " + pathsGeneratorBasedOnGivenDepot.generateNegReducedCostPathsBasedOnAGivenStartingDepotForardLabeling());
        System.out.println("reduced cost 1: " + pathsGeneratorBasedOnGivenDepot.getMinReducedCost());
        System.out.println("reduced cost 2:" + pathsGeneratorBasedOnGivenDepot.minReducedCost);
    }
}
