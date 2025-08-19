package NewGraph;

import ColumnGe.MasterProblem;
import Instance.*;
//import ShortestTimeCalculate.Calculator;
import ShortestTimeCalculate.CalculatorRelatedToGivenDepot;
import ShortestTimeCalculate.SolutionOfShortestTimeToDepot;
import Solution.SchedulesReader;
import Solution.Solution;
import Instance.TripWithWorkingStatusAndDepartureTime;
import jdk.swing.interop.SwingInterOpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

public class Label {
    private GraphRelatedToGivenDepot graphRelatedToGivenDepot;
    private MasterProblem masterProblem;

    private NewNode currentNode;// new node including depot and trip two type of class, we can check type to get what we need

    private int currentNodeDepTime;
    private int whetherDriving;//current working status of this trip
    private ArrayList<TripWithWorkingStatusAndDepartureTime> sequenceOfTripsWithWorkingStatusAndDepartureTime;

    private double totalReducedCost;
    private double totalDrivingTime;
    private double totalWorkingTime;
    private BitSet visitedTripId;
    private BitSet unreachableTripId;
    private int endId;
    private boolean whetherBeExtended;
    private CalculatorRelatedToGivenDepot calculatorRelatedToGivenDepot;
    private SolutionOfShortestTimeToDepot solutionOfShortestTimeToDepot;
    private int[] shortestTimes;

    public Label(GraphRelatedToGivenDepot graphRelatedToGivenDepot, MasterProblem masterProblem) {//
        this.graphRelatedToGivenDepot = graphRelatedToGivenDepot;
        this.masterProblem = masterProblem;
        NewNode initialNode = new NewNode(graphRelatedToGivenDepot.getGivenDepot(), true);
        this.currentNode = initialNode;
        this.currentNodeDepTime = -1;//stand for the un-defined
        this.whetherDriving = -1;//stand for un-defined
        this.totalReducedCost = 0;
        this.totalDrivingTime = 0;
        this.totalWorkingTime = 0;
        this.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>();
        this.endId = -1;// means there is no good ends for this starting Trip
        this.visitedTripId = new BitSet(this.graphRelatedToGivenDepot.getInstance().getNbTrips());
        this.unreachableTripId = new BitSet(this.graphRelatedToGivenDepot.getInstance().getNbTrips());
        this.whetherBeExtended = false;
        this.calculatorRelatedToGivenDepot = new CalculatorRelatedToGivenDepot(graphRelatedToGivenDepot);
        this.solutionOfShortestTimeToDepot = calculatorRelatedToGivenDepot.solveWithDijkstra();
        this.shortestTimes = calculatorRelatedToGivenDepot.getDistances();
    }

    public Label(Label label) {// here is for the common attributes which will never change during the
        //当下的 node中包含的时间。当下node所在的计算最短回depot的整个网络不变
        // other attributes will change according to each iteration
        this.currentNodeDepTime = label.currentNodeDepTime;
        this.graphRelatedToGivenDepot = label.graphRelatedToGivenDepot;
        this.masterProblem = label.masterProblem;
        this.calculatorRelatedToGivenDepot = label.calculatorRelatedToGivenDepot;
        this.solutionOfShortestTimeToDepot = label.solutionOfShortestTimeToDepot;
        this.shortestTimes = label.shortestTimes;
    }


    public double getTotalReducedCostCost() {
        return totalReducedCost;
    }

    public double getTotalDrivingTime() {
        return totalDrivingTime;
    }

    public double getTotalWorkingTime() {
        return totalWorkingTime;
    }


    public BitSet getVisitedTripId() {
        return visitedTripId;
    }

    public BitSet getUnreachableTripId() {
        return unreachableTripId;
    }

    public ArrayList<TripWithWorkingStatusAndDepartureTime> getSequenceOfTripsWithWorkingStatusAndDepartureTime() {
        return sequenceOfTripsWithWorkingStatusAndDepartureTime;
    }

    public int getEndId() {
        return endId;
    }

    public int getCurrentNodeDepTime() {
        return this.currentNodeDepTime;
    }

    public boolean getCurrentWorkingStatus() {
        int indexOfLastWorkingStatus = this.sequenceOfTripsWithWorkingStatusAndDepartureTime.size() - 1;
        TripWithWorkingStatusAndDepartureTime lastTripWithWorkingStatusAndDepartureTime = this.sequenceOfTripsWithWorkingStatusAndDepartureTime.get(indexOfLastWorkingStatus);
        boolean workingStatus = lastTripWithWorkingStatusAndDepartureTime.getDrivingStatus();
        return workingStatus;
    }

    public void setWhetherBeExtended(boolean whetherBeExtended) {
        this.whetherBeExtended = whetherBeExtended;
    }

    public boolean getWhetherBeExtended() {
        return whetherBeExtended;
    }

    public NewNode getCurrentNode() {
        return currentNode;
    }

    public Label extendTo(NewNode secondNode, int secondNodeWhetherDriving, int secondNodeDepTime) {
        String startingDepotType = "startingDepot";
        String endingDepotType = "endingDepot";
        String tripType = "trip";

        Instance instance = this.graphRelatedToGivenDepot.getInstance();
        boolean whetherDrive = false;

        Label newLabel = new Label(this);// first copy the common attribute, then in the following need to set value to the  attribute belong to different part

        //case 1: from starting depot to the first starting trip
        if (this.currentNode.getNodeType().equals(startingDepotType)) {
            Depot depot = instance.getDepot(this.currentNode.getId());
            if (!secondNode.getNodeType().equals(tripType)) {
                System.out.println("couldn't extend to the first trip because it is not a trip type");
                return null;
            } else if (secondNode.getNodeType().equals(tripType) && instance.getTrip(secondNode.getId()).getIdOfStartCity() != depot.getIdOfCityAsDepot()) {//
                System.out.println("couldn't extend to the first trip because the starting city is not the depot");
                return null;
            } else if (secondNode.getNodeType().equals(tripType) && (instance.getTrip(secondNode.getId()).getIdOfStartCity() == depot.getIdOfCityAsDepot())
                    && (secondNodeDepTime < instance.getTrip(secondNode.getId()).getEarliestDepartureTime() || secondNodeDepTime > instance.getTrip(secondNode.getId()).getLatestDepartureTime())) {//
                System.out.println("couldn't extend to the first trip because the starting time is not in the given time window");
                return null;
            } else {
                Trip startingTrip = instance.getTrip(secondNode.getId());
                double gamma = this.masterProblem.getDualValueFromNbAvailableDriver();
                double beta = this.masterProblem.getDualValueFromOneDriving(startingTrip.getIdOfTrip());
                newLabel.currentNode = secondNode;
                newLabel.whetherDriving = secondNodeWhetherDriving;

                newLabel.currentNodeDepTime = secondNodeDepTime;// NEW ADD 2025.3.3
                if (secondNodeWhetherDriving == 1) {
                    whetherDrive = true;
                    newLabel.totalDrivingTime = startingTrip.getDuration();
                    newLabel.totalReducedCost = instance.getFixedCostForDriver() - gamma - beta;// modify 2025.3.3
                    //System.out.println("now the  reduced cost includes: fixed cost_" + instance.getFixedCostForDriver() + "dual value gamma_" + gamma
                       //     + " dual value beta_" + beta);
                } else if (secondNodeWhetherDriving == 0) {
                    whetherDrive = false;
                    newLabel.totalDrivingTime = 0;
                    newLabel.totalReducedCost = instance.getFixedCostForDriver() - gamma;// modify 2025.3.4
                } else {
                    return null;
                }
                newLabel.totalWorkingTime = startingTrip.getDuration();
                TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(startingTrip, whetherDrive, secondNodeDepTime);

                /**
                 *  here I need to initialize again because it is not in the common attributes
                 * */
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>();
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime.add(tripWithWorkingStatusAndDepartureTime);
                newLabel.visitedTripId = new BitSet(instance.getNbTrips());// why we use bitSet here
                newLabel.visitedTripId.set(startingTrip.getIdOfTrip());

                //the following for loop is to check other trips maybe unreachable in second NodeWithLabels
                int arrivalTimeOfStartingTrip = secondNodeDepTime + secondNode.getTrip().getDuration();
                newLabel.unreachableTripId = new BitSet(instance.getNbTrips());
                newLabel.unreachableTripId = (BitSet) newLabel.visitedTripId.clone();
                newLabel.currentNodeDepTime=secondNodeDepTime;

                for (int k = 0; k < instance.getNbTrips(); k++) {
                    Trip followerTrip = instance.getTrip(k);
                    int idOfFollowerTrip = instance.getTrip(k).getIdOfTrip();
                    //Pay attention: here should find all the unreachable node，not only the neighbor,
                    // because maybe there are some nodes could become reachable
                    // add a middle node
                    int latestDepTimeOfFollowerTrip = followerTrip.getLatestDepartureTime();// 2025.3.3 here i Changed to int
                    //int  arrivalTimeOfFollowerTrip = followerTrip.getArrivalTime();// 2025.3.3 here i Changed to int
                    double shortTime_Fol_Depot_AccordingToCalculator = shortestTimes[idOfFollowerTrip];

                    if (latestDepTimeOfFollowerTrip < arrivalTimeOfStartingTrip + instance.getMinPlanTurnTime()) {
                        //case1:  follower  departure time is too early, the second trip still not finish arrival
                        newLabel.unreachableTripId.set(idOfFollowerTrip);
                    }
                    //case2:  follower trip can not go back to depot in time
                    else if (latestDepTimeOfFollowerTrip >= arrivalTimeOfStartingTrip + instance.getMinPlanTurnTime()) {
                        int minConTimeFromSecondNodeToFollNode = instance.getMinPlanTurnTime();
                        int conT = followerTrip.getEarliestDepartureTime() - secondNodeDepTime - secondNode.getTrip().getDuration();
                        if (conT > minConTimeFromSecondNodeToFollNode) {
                            minConTimeFromSecondNodeToFollNode = conT;
                        }
                        if (this.getTotalWorkingTime() + secondNode.getTrip().getDuration()//modify 2025.3.3
                                + minConTimeFromSecondNodeToFollNode + followerTrip.getDuration() + shortTime_Fol_Depot_AccordingToCalculator > instance.getMaxWorkingTime()) {

                            newLabel.unreachableTripId.set(idOfFollowerTrip);
                        }
                    }
                }
                /**
                 * this is for the first trip, we add this attribute value which is not be shown up in constructor
                 * */
                newLabel.whetherBeExtended = false;
                newLabel.endId = -1;
                return newLabel;
            }

        }

        //case 2: from the last trip to the ending depot
        if (secondNode.getNodeType().equals(endingDepotType)) {
            Depot depot = instance.getDepot(secondNode.getId());
            if (!this.currentNode.getNodeType().equals(tripType)) {
                return null;
            } else if (this.getTotalDrivingTime() > instance.getMaxDrivingTime() || instance.getTrip(this.currentNode.getId()).getIdOfEndCity() != depot.getIdOfCityAsDepot()) {
                //if the current trip driving time exceed the limitation, it should not be extended, or it does not end at a good depot
                return null;
            } else {
                newLabel.currentNode = secondNode;
                newLabel.whetherDriving = whetherDriving;//here should equal to -1
                newLabel.totalReducedCost = this.getTotalReducedCostCost();
                newLabel.totalDrivingTime = this.getTotalDrivingTime();
                newLabel.totalWorkingTime = this.getTotalWorkingTime();
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>(this.sequenceOfTripsWithWorkingStatusAndDepartureTime);
                newLabel.visitedTripId = (BitSet) this.visitedTripId.clone();
                newLabel.unreachableTripId = (BitSet) this.unreachableTripId.clone();
                newLabel.endId = depot.getIndexOfDepotAsEndingPoint();

            }

        }


        //case3: general case, add trip after the last predecessor
        //judge whether the current trip can be extended to the parameter trip, which means the two trips can be connected,
        // Pay Attention: and the current driving time is not exceed the maximum limitation
        //if it could extend, update the label
        //otherwise, it will return null;
        if (this.currentNode.getNodeType().equals(tripType) && secondNode.getNodeType().equals(tripType)) {
            if (!instance.whetherHavePossibleArcAfterCleaning(currentNode.getTrip().getIdOfTrip(), secondNode.getTrip().getIdOfTrip())
                    || this.getTotalDrivingTime() > instance.getMaxDrivingTime()) {
                // if the two trip are not connected or the current trip driving time already exceed the limitation,
                // then it should not be extended
                newLabel = null;
            } else {
                int earliestSecondNodeFeasibleDep = secondNode.getTrip().getEarliestDepartureTime();
                int latestSecondNodeFeasibleDep = secondNode.getTrip().getLatestDepartureTime();
                int depCalAccordingToFirst = this.currentNodeDepTime + this.currentNode.getTrip().getDuration() + instance.getMinPlanTurnTime();
                if (depCalAccordingToFirst > earliestSecondNodeFeasibleDep) {
                    earliestSecondNodeFeasibleDep = depCalAccordingToFirst;
                }
                if (secondNodeDepTime < earliestSecondNodeFeasibleDep || secondNodeDepTime > latestSecondNodeFeasibleDep) {
                    newLabel = null;
                } else {
                    Trip firstTrip = instance.getTrip(this.currentNode.getId());
                    Trip tripInSecondNode = instance.getTrip(secondNode.getId());
                    int conTime = secondNodeDepTime - this.currentNodeDepTime - firstTrip.getDuration();
                    //System.out.println("conTime_"+firstTrip.getIdOfTrip()+"with _"+tripInSecondNode.getIdOfTrip()+"is :"+conTime);

                    double beta = this.masterProblem.getDualValueFromOneDriving(tripInSecondNode.getIdOfTrip());
                   // System.out.println("beta_" + tripInSecondNode.getIdOfTrip() + "is: " + beta);
                    double delta = this.masterProblem.getDualValueFromLinkSelectScheduleAndTripOneDepTime(tripInSecondNode.getIdOfTrip(), secondNodeDepTime);
                   // System.out.println("delta is"+delta);
                    newLabel.currentNode = secondNode;
                    newLabel.whetherDriving = secondNodeWhetherDriving;

                    double whetherDrivingFormer = this.whetherDriving;
                    if (whetherDrivingFormer == 1 && whetherDriving == 1) {//case 1: both trip i and j driving
                        whetherDrive = true;
//                    newLabel.totalPathCost = this.totalPathCost + instance.getIdleTimeCostForDriver(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                        newLabel.totalDrivingTime = this.getTotalDrivingTime() + tripInSecondNode.getDuration();

                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            //System.out.println("zeta  is"+zeta);

                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - beta - delta - zeta;

                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - beta - delta;
                        }

                    } else if (whetherDrivingFormer == 1 && whetherDriving == 0) {//case 2: i driving j passenger
                        whetherDrive = false;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime();
                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - delta - zeta;
                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - delta;
                        }
                    } else if (whetherDrivingFormer == 0 && whetherDriving == 1) {//case3: i passenger j driving
                        whetherDrive = true;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime() + tripInSecondNode.getDuration();

                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            //driver will drive in the following trip
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver()
                                    - beta - delta - zeta;
                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver()
                                    - beta - delta;
                        }

                    } else if (whetherDrivingFormer == 0 && whetherDriving == 0) {
                        whetherDrive = false;

                        newLabel.totalDrivingTime = this.getTotalDrivingTime();
                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            //driver will drive in the following trip
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - delta - zeta;
                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - delta;
                        }
                    } else {
                        newLabel = null;
                    }
                    newLabel.totalWorkingTime = this.getTotalWorkingTime() + conTime + tripInSecondNode.getDuration();

                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(tripInSecondNode, whetherDrive, secondNodeDepTime);
                    newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>(this.sequenceOfTripsWithWorkingStatusAndDepartureTime);//得先复制原来的，然后再添加
                    newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime.add(tripWithWorkingStatusAndDepartureTime);

                    newLabel.visitedTripId = (BitSet) (this.visitedTripId.clone());//here we should copy the previous trip
                    newLabel.visitedTripId.set(tripInSecondNode.getIdOfTrip());//then add the new trip
                    newLabel.currentNodeDepTime=secondNodeDepTime;

                    /**
                     * this is the part which is not belong to the comment attribute value
                     * */
                    newLabel.endId = -1;

                    //here is to get the untouchable trip id set
                    newLabel.unreachableTripId = (BitSet) newLabel.visitedTripId.clone();
                    Trip tripInFirstNode = firstTrip;
                    int arrivalTimeOfSecondTip = secondNodeDepTime + tripInSecondNode.getDuration();
                    for (int k = 0; k < instance.getNbTrips(); k++) {
                        Trip followerTrip = instance.getTrip(k);
                        double latestDepartureTimeOfFollowerTrip = followerTrip.getLatestDepartureTime();
                        int idOfFollowerTrip = instance.getTrip(k).getIdOfTrip();
                        double shortTime_Fol_Depot_AccordingToCalculator = shortestTimes[idOfFollowerTrip];

                        if (latestDepartureTimeOfFollowerTrip < arrivalTimeOfSecondTip + instance.getMinPlanTurnTime()) {
                            //case1:  follower trip departure time is too early before the second trip finished
                            newLabel.unreachableTripId.set(idOfFollowerTrip);
                        } else if (latestDepartureTimeOfFollowerTrip >= arrivalTimeOfSecondTip + instance.getMinPlanTurnTime()) {
                            //case2:  follower trip arrival time is behind the second trip (we don't need to consider whether they are neighboor or not)
                            // but it can not go back to the good depot in time

                            int minTimeSecondNodeToFollower = instance.getMinPlanTurnTime();
                            int earliestDepOfFollowerTrip = followerTrip.getEarliestDepartureTime();
                            if ((earliestDepOfFollowerTrip - arrivalTimeOfSecondTip) > minTimeSecondNodeToFollower) {
                                minTimeSecondNodeToFollower = earliestDepOfFollowerTrip - arrivalTimeOfSecondTip;
                            }

                            if (this.getTotalWorkingTime() + conTime + tripInSecondNode.getDuration() +
                                    minTimeSecondNodeToFollower + followerTrip.getDuration() + shortTime_Fol_Depot_AccordingToCalculator > instance.getMaxWorkingTime()) {
                                newLabel.unreachableTripId.set(idOfFollowerTrip);
                               // System.out.println("check the label class the untouchable trip is " +idOfFollowerTrip+" because of the limited working time");
                            }
                        }

                    }
                }

            }
        }
        return newLabel;
    }

    public boolean checkAtLeastOneIsDriving() {
        boolean atLeastOneDriving = false;
        for (TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime : this.sequenceOfTripsWithWorkingStatusAndDepartureTime) {
            if (tripWithWorkingStatusAndDepartureTime.getDrivingStatus()) {
                atLeastOneDriving = true;
                break;
            }
        }
        return atLeastOneDriving;
    }


    public boolean checkWhetherDrivingStatusIsSame(Label label) {
        if (this.currentNode == label.currentNode && this.getCurrentWorkingStatus() == label.getCurrentWorkingStatus()) {
            return true;
        } else if (this.currentNode == label.currentNode && this.getCurrentWorkingStatus() != label.getCurrentWorkingStatus()) {
            return false;

        } else {
            System.out.println(" We cannot compare the current label with " + label);
            return false;
        }
    }


    @Override
    public String toString() {
        return
//                "Label{" +

                "currentNode=" + currentNode +

                ", totalReducedCost=" + totalReducedCost +
                ", totalDrivingTime=" + totalDrivingTime +
//                ", totalWorkingTime=" + totalWorkingTime +
//                ", visitedTripId=" + visitedTripId +
//
//                ", endId=" + endId +
                        '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips030_combPer0.0_TW0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);

        SchedulesReader schedulesReader = null;
        try {
            schedulesReader = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips030_combPer0.0_TW0.txt", instance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //scheduleSolution_inst_nbCity05_Size180_Day1_nbTrips020_combPer0.0
        Solution initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);
        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());

        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
        masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况


        NewNode node1 = new NewNode(instance.getDepot(1), true);//Depot0: 20 22 3
        NewNode node2 = new NewNode(instance.getTrip(18));//Trip1: 3 2 246 281 1
        NewNode node3 = new NewNode(instance.getTrip(2));
        NewNode node4 = new NewNode(instance.getTrip(3));
        NewNode node5 = new NewNode(instance.getTrip(4));
        NewNode node6=new NewNode(instance.getTrip(5));
        NewNode node7=new NewNode(instance.getTrip(6));
        NewNode node8 = new NewNode(instance.getDepot(1), false);//Depot0: 20 22 3
//        NewNode node3 = new NewNode(instance.getDepot(1), false);
//        NewNode node4 = new NewNode(instance.getTrip(2));//Trip2: 2 0 322 369 1
        GraphRelatedToGivenDepot graphRelatedToGivenDepot = new GraphRelatedToGivenDepot(instance, instance.getDepot(1));

        Label label1 = new Label(graphRelatedToGivenDepot, masterProblem);
        System.out.println("the default label" + label1);
//        Label label2 = label1.extendTo(node1, -1,-1);//495
//        System.out.println("check the label2 " + label2);
        Label label3 = label1.extendTo(node2, 1, 495);
        System.out.println("check label 3" + label3);
        Label label4 = label3.extendTo(node3, 1, 600);
        System.out.println("check label 4" + label4);
        Label label5 = label4.extendTo(node4, 1, 675);
        System.out.println("check label 5" + label5);
        Label label6=label5.extendTo(node5,1,775);
        System.out.println("check label 6 "+label6);
        Label label7= label6.extendTo(node6,1,840);
        System.out.println("check label 7"+label7);
        Label label8=label7.extendTo(node7,1,900);
        System.out.println("check label 8"+label8);
        Label lastLabel=label8.extendTo(node8,1,-1);
        System.out.println("check last label"+lastLabel);



        //System.out.println("from the default label to Trip2" + label1.extendTo(node2, 1,495));
    }
}
