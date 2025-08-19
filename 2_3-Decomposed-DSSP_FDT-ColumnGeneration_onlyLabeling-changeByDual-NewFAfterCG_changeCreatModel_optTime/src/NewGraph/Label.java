package NewGraph;

import ColumnGe.MasterProblem;
import Instance.*;
//import ShortestTimeCalculate.Calculator;
import ShortestTimeCalculate.CalculatorRelatedToGivenDepot;
import ShortestTimeCalculate.SolutionOfShortestTimeToDepot;
import Solution.SchedulesReader;
import Solution.Solution;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;

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
    private boolean whetherBeExtendedToNextNode;

    private boolean whetherBeExtendedFromPreNode;
    private CalculatorRelatedToGivenDepot calculatorRelatedToGivenDepot;
    private SolutionOfShortestTimeToDepot solutionOfShortestTimeToDepot;
    private int[] shortestTimes;
    private static final double EPS = 1e-5;

    public Label(GraphRelatedToGivenDepot graphRelatedToGivenDepot, MasterProblem masterProblem, boolean whetherFromStartingDepotDirection) {//
        this.graphRelatedToGivenDepot = graphRelatedToGivenDepot;
        this.masterProblem = masterProblem;
        NewNode initialNode = new NewNode(graphRelatedToGivenDepot.getGivenDepot(), whetherFromStartingDepotDirection);
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
        this.whetherBeExtendedToNextNode = false;
        this.whetherBeExtendedFromPreNode = false;
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


    public double getTotalReducedCost() {
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

    public void setWhetherBeExtendedToNextNode(boolean whetherBeExtendedToNextNode) {
        this.whetherBeExtendedToNextNode = whetherBeExtendedToNextNode;
    }

    public void setWhetherBeExtendedFromPreNode(boolean whetherBeExtendedFromPreNode) {
        this.whetherBeExtendedFromPreNode = whetherBeExtendedFromPreNode;
    }

    public boolean getWhetherBeExtendedToNextNode() {
        return whetherBeExtendedToNextNode;
    }

    public boolean getWhetherBeExtendedFromPreNode() {
        return whetherBeExtendedFromPreNode;
    }

    public NewNode getCurrentNode() {
        return currentNode;
    }

    public Label extendToNextNode(NewNode nextNode, int nextNodeWhetherDriving, int nextNodeDepTime) {
        String startingDepotType = "startingDepot";
        String endingDepotType = "endingDepot";
        String tripType = "trip";

        Instance instance = this.graphRelatedToGivenDepot.getInstance();
        boolean whetherDrive = false;

        Label newLabel = new Label(this);// first copy the common attribute, then in the following need to set value to the  attribute belong to different part

        //case 1: from starting depot to the first starting trip
        if (this.currentNode.getNodeType().equals(startingDepotType)) {
            Depot depot = instance.getDepot(this.currentNode.getId());
            if (!nextNode.getNodeType().equals(tripType)) {
                System.out.println("couldn't extend to the first trip because it is not a trip type");
                return null;
            } else if (nextNode.getNodeType().equals(tripType) && instance.getTrip(nextNode.getId()).getIdOfStartCity() != depot.getIdOfCityAsDepot()) {//
                System.out.println("couldn't extend to the first trip because the starting city is not the depot");
                return null;
            } else if (nextNode.getNodeType().equals(tripType) && (instance.getTrip(nextNode.getId()).getIdOfStartCity() == depot.getIdOfCityAsDepot())
                    && (nextNodeDepTime < instance.getTrip(nextNode.getId()).getEarliestDepartureTime() || nextNodeDepTime > instance.getTrip(nextNode.getId()).getLatestDepartureTime())) {//
                System.out.println("couldn't extend to the first trip because the starting time is not in the given time window");
                if (nextNodeDepTime < instance.getTrip(nextNode.getId()).getEarliestDepartureTime()) {
                    System.out.println("couldn't extend to the first trip because the starting time is less than earliest time");
                }
                if (nextNodeDepTime > instance.getTrip(nextNode.getId()).getLatestDepartureTime()) {
                    System.out.println("couldn't extend to the first trip because the starting time is greater than latest time");
                }
                return null;
            } else {
                Trip nextNodeAsStartingTrip = instance.getTrip(nextNode.getId());
                double gamma = this.masterProblem.getDualValueFromNbAvailableDriver();
                double beta = this.masterProblem.getDualValueFromOneDriving(nextNodeAsStartingTrip.getIdOfTrip());
                newLabel.currentNode = nextNode;
                newLabel.whetherDriving = nextNodeWhetherDriving;
                newLabel.currentNodeDepTime = nextNodeDepTime;// NEW ADD 2025.3.3
//                if(startingTrip.getIdOfTrip()==0) {
//                    System.out.println("if starting trip=" + startingTrip.getIdOfTrip()+" departure time"+nextNodeDepTime);
//                }
                if (nextNodeWhetherDriving == 1) {
                    whetherDrive = true;
                    newLabel.totalDrivingTime = nextNodeAsStartingTrip.getDuration();
                    newLabel.totalReducedCost = instance.getFixedCostForDriver() - gamma - beta;// modify 2025.3.3
                    // System.out.println("now the  reduced cost is "+newLabel.getTotalReducedCost()+" includes: fixed cost_" + instance.getFixedCostForDriver() + "dual value gamma_" + gamma
                    // + " dual value beta_" + beta);

                } else if (nextNodeWhetherDriving == 0) {
                    whetherDrive = false;
                    newLabel.totalDrivingTime = 0;
                    newLabel.totalReducedCost = instance.getFixedCostForDriver() - gamma;// modify 2025.3.4
                } else {
                    return null;
                }
                newLabel.totalWorkingTime = nextNodeAsStartingTrip.getDuration();
                TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(nextNodeAsStartingTrip, whetherDrive, nextNodeDepTime);

                /**
                 *  here I need to initialize again because it is not in the common attributes
                 * */
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>();
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime.add(tripWithWorkingStatusAndDepartureTime);
                newLabel.visitedTripId = new BitSet(instance.getNbTrips());// why we use bitSet here
                newLabel.visitedTripId.set(nextNodeAsStartingTrip.getIdOfTrip());

                //the following for loop is to check other trips maybe unreachable in second NodeWithLabels
                int arrivalTimeOfStartingTrip = nextNodeDepTime + nextNode.getTrip().getDuration();
                newLabel.unreachableTripId = new BitSet(instance.getNbTrips());
                newLabel.unreachableTripId = (BitSet) newLabel.visitedTripId.clone();
                newLabel.currentNodeDepTime = nextNodeDepTime;

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
                        if (!newLabel.unreachableTripId.get(idOfFollowerTrip)) {
                            if (shortTime_Fol_Depot_AccordingToCalculator > instance.getMaxWorkingTime()) {
                                //  Debug 打印方便分析问题
                                // System.out.println("Trip " + idOfFollowerTrip + " marked unreachable (can't return to depot from it)" + depot);
                                newLabel.unreachableTripId.set(idOfFollowerTrip);

                            } else {
                                int minConTimeFromSecondNodeToFollNode = instance.getMinPlanTurnTime();
                                int conT = followerTrip.getEarliestDepartureTime() - nextNodeDepTime - nextNode.getTrip().getDuration();
                                if (conT > minConTimeFromSecondNodeToFollNode) {
                                    minConTimeFromSecondNodeToFollNode = conT;
                                }
                                int totalWorkingIfAddFollower = (int) Math.round(nextNode.getTrip().getDuration()
                                        + minConTimeFromSecondNodeToFollNode
                                        + followerTrip.getDuration()
                                        + shortTime_Fol_Depot_AccordingToCalculator
                                );
                                if (totalWorkingIfAddFollower > instance.getMaxWorkingTime()) {
//                                    System.out.println("add trip couldn't be followed after node " + nextNode);
//                                    System.out.println("Duration of starting trip" + startingTrip.getDuration());
//                                    System.out.println("follower trip " + idOfFollowerTrip + "min connection to node" + nextNode + " time is:" + minConTimeFromSecondNodeToFollNode);
//                                    System.out.println("follower trip " + idOfFollowerTrip + " duration " + followerTrip.getDuration());
//                                    System.out.println("shortest time from follower to depot" + idOfFollowerTrip + " time is: " + shortTime_Fol_Depot_AccordingToCalculator);
                                    newLabel.unreachableTripId.set(idOfFollowerTrip);//check0421
                                    // System.out.println("Trip " + idOfFollowerTrip + " marked unreachable");
                                }

                            }
                        }

                    }
                }
                /**
                 * this is for the first trip, we add this attribute value which is not be shown up in constructor
                 * */
                newLabel.whetherBeExtendedToNextNode = false;
                newLabel.endId = -1;
                return newLabel;
            }
        }

        //case 2: from the last trip to the ending depot
        if (nextNode.getNodeType().equals(endingDepotType)) {
            Depot depot = instance.getDepot(nextNode.getId());
            if (!this.currentNode.getNodeType().equals(tripType)) {
                return null;
            } else if (this.getTotalDrivingTime() > instance.getMaxDrivingTime() || instance.getTrip(this.currentNode.getId()).getIdOfEndCity() != depot.getIdOfCityAsDepot()) {
                //if the current trip driving time exceed the limitation, it should not be extended, or it does not end at a good depot
                return null;
            } else {
                newLabel.currentNode = nextNode;
                newLabel.whetherDriving = this.whetherDriving;//here should equal to -1
                newLabel.totalReducedCost = this.getTotalReducedCost();
                newLabel.totalDrivingTime = this.getTotalDrivingTime();
                newLabel.totalWorkingTime = this.getTotalWorkingTime();
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>(this.sequenceOfTripsWithWorkingStatusAndDepartureTime);
                newLabel.visitedTripId = (BitSet) this.visitedTripId.clone();
                newLabel.unreachableTripId = (BitSet) this.unreachableTripId.clone();
                newLabel.endId = depot.getIndexOfDepotAsEndingPoint();
                return newLabel;

            }

        }
        //case3: general case, add trip after the last predecessor
        //judge whether the current trip can be extended to the parameter trip, which means the two trips can be connected,
        // Pay Attention: and the current driving time is not exceed the maximum limitation
        //if it could extend, update the label
        //otherwise, it will return null;
        if (this.currentNode.getNodeType().equals(tripType) && nextNode.getNodeType().equals(tripType)) {
            if (!instance.whetherHavePossibleArcAfterCleaning(currentNode.getTrip().getIdOfTrip(), nextNode.getTrip().getIdOfTrip())
                    || this.getTotalDrivingTime() > instance.getMaxDrivingTime()) {
                // if the two trip are not connected or the current trip driving time already exceed the limitation,
                // then it should not be extended
                return null;
            } else {
                int earliestSecondNodeFeasibleDep = nextNode.getTrip().getEarliestDepartureTime();
                int latestSecondNodeFeasibleDep = nextNode.getTrip().getLatestDepartureTime();
                int depCalAccordingToFirst = this.currentNodeDepTime + this.currentNode.getTrip().getDuration() + instance.getMinPlanTurnTime();
                if (depCalAccordingToFirst > earliestSecondNodeFeasibleDep) {
                    earliestSecondNodeFeasibleDep = depCalAccordingToFirst;
                }
                if (nextNodeDepTime < earliestSecondNodeFeasibleDep || nextNodeDepTime > latestSecondNodeFeasibleDep || nextNodeDepTime - this.currentNodeDepTime - instance.getTrip(this.currentNode.getId()).getDuration() < instance.getMinPlanTurnTime()) {
                    return null;
                } else {
                    Trip firstTrip = instance.getTrip(this.currentNode.getId());
                    Trip tripInSecondNode = instance.getTrip(nextNode.getId());
                    int conTime = nextNodeDepTime - this.currentNodeDepTime - firstTrip.getDuration();
                    //System.out.println("conTime_"+firstTrip.getIdOfTrip()+"with _"+tripInSecondNode.getIdOfTrip()+"is :"+conTime);
                    double beta = this.masterProblem.getDualValueFromOneDriving(tripInSecondNode.getIdOfTrip());
                    // System.out.println("Check labeling beta_" + tripInSecondNode.getIdOfTrip() + "is: " + beta);

                    double delta = this.masterProblem.getDualValueFromLinkSelectScheduleAndTripOneDepTime(tripInSecondNode.getIdOfTrip(), nextNodeDepTime);
                    //System.out.println("Check labeling delta_"+tripInSecondNode.getIdOfTrip()+" "+nextNodeDepTime+" is "+delta);
                    newLabel.currentNode = nextNode;
                    newLabel.whetherDriving = nextNodeWhetherDriving;

                    double whetherDrivingFormer = this.whetherDriving;
                    if (this.whetherDriving == 1 && nextNodeWhetherDriving == 1) {//case 1: both trip i and j driving
                        whetherDrive = true;
//                    newLabel.totalPathCost = this.totalPathCost + instance.getIdleTimeCostForDriver(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                        newLabel.totalDrivingTime = this.getTotalDrivingTime() + tripInSecondNode.getDuration();

                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            //  System.out.println("Check labeling zeta "+firstTrip.getIdOfTrip()+"_"+tripInSecondNode.getIdOfTrip()+" is "+zeta);

                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - beta - delta - zeta;
                            // System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual value beta: "+beta+" delta value "+delta +" zeta value "+zeta);

                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - beta - delta;
                            //System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual value beta: "+beta+" delta value "+delta);
                        }

                    } else if (this.whetherDriving == 1 && nextNodeWhetherDriving == 0) {//case 2: i driving j passenger
                        whetherDrive = false;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime();
                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            //System.out.println("Check labeling zeta "+firstTrip.getIdOfTrip()+"_"+tripInSecondNode.getIdOfTrip()+" is "+zeta);
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - delta - zeta;
                            //System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual delta value "+delta +" zeta value "+zeta);
                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - delta;
                            //System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual delta value "+delta );
                        }
                    } else if (this.whetherDriving == 0 && nextNodeWhetherDriving == 1) {//case3: i passenger j driving
                        whetherDrive = true;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime() + tripInSecondNode.getDuration();

                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            //System.out.println("Check labeling zeta "+firstTrip.getIdOfTrip()+"_"+tripInSecondNode.getIdOfTrip()+" is "+zeta);
                            //driver will drive in the following trip
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver()
                                    - beta - delta - zeta;
                            //System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual value beta: "+beta+" delta value "+delta+" zeta value "+zeta);
                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver()
                                    - beta - delta;
                            // System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual value beta: "+beta+" delta value "+delta);
                        }

                    } else if (this.whetherDriving == 0 && nextNodeWhetherDriving == 0) {//case 4
                        whetherDrive = false;

                        newLabel.totalDrivingTime = this.getTotalDrivingTime();
                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(firstTrip.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            // System.out.println("Check labeling zeta "+firstTrip.getIdOfTrip()+"_"+tripInSecondNode.getIdOfTrip()+" is "+zeta);
                            //driver will drive in the following trip
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - delta - zeta;
                            //System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual delta value "+delta+" zeta value "+zeta);
                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - delta;
                            //System.out.println("now the reduced cost is "+newLabel.getTotalReducedCost()+" with dual delta value "+delta);
                        }
                    } else {
                        return null;
                    }
                    newLabel.totalWorkingTime = this.getTotalWorkingTime() + conTime + tripInSecondNode.getDuration();
                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(tripInSecondNode, whetherDrive, nextNodeDepTime);
                    newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>(this.sequenceOfTripsWithWorkingStatusAndDepartureTime);//得先复制原来的，然后再添加
                    newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime.add(tripWithWorkingStatusAndDepartureTime);

                    newLabel.visitedTripId = (BitSet) (this.visitedTripId.clone());//here we should copy the previous trip
                    newLabel.visitedTripId.set(tripInSecondNode.getIdOfTrip());//then add the new trip
                    newLabel.currentNodeDepTime = nextNodeDepTime;

                    /**
                     * this is the part which is not belong to the comment attribute value
                     * */
                    newLabel.endId = -1;

                    //here is to get the untouchable trip id set
                    //newLabel.unreachableTripId = (BitSet) newLabel.visitedTripId.clone();
                    // change 0427 copy the previous trip and add the new visited: the following two lines
                    newLabel.unreachableTripId = (BitSet) this.unreachableTripId.clone();
                    newLabel.unreachableTripId.set(nextNode.getId());
                    //************
                    Trip tripInFirstNode = firstTrip;
                    int arrivalTimeOfSecondTip = nextNodeDepTime + tripInSecondNode.getDuration();
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
                            if (!newLabel.unreachableTripId.get(idOfFollowerTrip)) {
                                if (shortTime_Fol_Depot_AccordingToCalculator > instance.getMaxWorkingTime()) {
                                    // Debug 打印方便分析问题
                                    //System.out.println("No path from trip " + idOfFollowerTrip + " to depot, add it directly the  working time check.");
                                    newLabel.unreachableTripId.set(idOfFollowerTrip);
                                } else {
                                    int minTimeSecondNodeToFollower = instance.getMinPlanTurnTime();
                                    int earliestDepOfFollowerTrip = followerTrip.getEarliestDepartureTime();
                                    if ((earliestDepOfFollowerTrip - arrivalTimeOfSecondTip) > minTimeSecondNodeToFollower) {
                                        minTimeSecondNodeToFollower = earliestDepOfFollowerTrip - arrivalTimeOfSecondTip;
                                    }
                                    int estimatedTotalWork = (int) Math.round(this.getTotalWorkingTime()
                                            + conTime
                                            + tripInSecondNode.getDuration()
                                            + followerTrip.getDuration()
                                            + minTimeSecondNodeToFollower
                                            + shortTime_Fol_Depot_AccordingToCalculator
                                    );
                                    if (estimatedTotalWork > instance.getMaxWorkingTime()) {
                                        //  newLabel.unreachableTripId.set(idOfFollowerTrip);
                                    }
                                    if (estimatedTotalWork > instance.getMaxWorkingTime()) {
                                        newLabel.unreachableTripId.set(idOfFollowerTrip);//check0421
//                                    System.out.println("check the total work"+this.getTotalWorkingTime());
//                                    System.out.println("check the min connection"+minTimeSecondNodeToFollower);
//                                    System.out.println("check the short time to depot"+shortTime_Fol_Depot_AccordingToCalculator);
//                                        System.out.println("current connection time: "+ conTime);
                                    }
                                }
                            }
                        }

                    }
                    return newLabel;
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
            //  System.out.println(" We cannot compare the current label with " + label);
            return false;
        }
    }

    public boolean whetherThisDominatesUnderSameDriveStatusForwardLabelingAlgorithm(Label other) {
        boolean whetherDominates = false;
        int departureTimeOfThisLabel = this.getCurrentNodeDepTime();
        int departureTimeOfOtherLabel = other.getCurrentNodeDepTime();

        double reducedCost_this = this.getTotalReducedCost();
        double reducedCost_label = other.getTotalReducedCost();

        double totalWorkingTime_this = this.getTotalWorkingTime();
        double totalWorkingTime_label = other.getTotalWorkingTime();

        double totalDrivingTime_this = this.getTotalDrivingTime();
        double totalDrivingTime_label = other.getTotalDrivingTime();
        double idleCost = this.graphRelatedToGivenDepot.getInstance().getIdleTimeCostForDriverPerUnit();


        //针对第一个node希望越晚越好
        boolean whetherCurrentNodeIsTrip = (this.currentNode.getNodeType() == "trip");
        int idTrip = this.currentNode.getId();
        Trip trip = this.graphRelatedToGivenDepot.getInstance().getTrip(idTrip);
        int nbValid = 0;
        int nbStrict = 0;

        int keyDepartureFromCurrentTrip = this.graphRelatedToGivenDepot.getInstance().getKeyDeparturePointForExtendFrom(trip);
        boolean whetherCouldHavePreNode = this.graphRelatedToGivenDepot.getInstance().whetherCouldHavePreTripInGraph(trip);
        boolean whetherCouldHaveSucNode = this.graphRelatedToGivenDepot.getInstance().whetherCouldHaveSuccessorTripInGraph(trip);
        boolean whetherEffectByTimeWindow = this.graphRelatedToGivenDepot.getInstance().whetherConArcEffectByTW(trip);

        //接下来开始进行比较，只要有一个严格不等式或者真子集成立，那么这个标签就是更好的
        //case 15: reducedCost_this= ;total working =; total driving=; proper subset
        BitSet visited_This = this.visitedTripId;
        BitSet visited_Other = other.visitedTripId;
        //if(departureTimeOfThisLabel==departureTimeOfLabel) {
        int idCity = trip.getIdOfStartCity();
        int moreWaitThis = 0;
        if (this.currentNodeDepTime <= other.getCurrentNodeDepTime()) {
            moreWaitThis = other.getCurrentNodeDepTime() - this.currentNodeDepTime;// pay more but it would earned in the future
        }
        moreWaitThis = Math.max(0, other.getCurrentNodeDepTime() - this.getCurrentNodeDepTime());
        double moreWaitCost = moreWaitThis * idleCost;
//        if (whetherCurrentNodeIsTrip) {

            if (reducedCost_this + moreWaitCost <= reducedCost_label + EPS) {//分两种情况讨论
                nbValid++;
                if (reducedCost_this + moreWaitCost < reducedCost_label + EPS) {
                    nbStrict++;
                }
                // case 1.1: reducedCost_this< reducedCost_label
                if (totalWorkingTime_this + moreWaitThis <= totalWorkingTime_label + EPS) {//+ moreWaitThis
                    nbValid++;
                    if (totalWorkingTime_this + moreWaitThis < totalWorkingTime_label + EPS) {//+ moreWaitThis
                        nbStrict++;
                    }
                    //case1.1.1  reducedCost_this< ;total working <
                    if (totalDrivingTime_this <= totalDrivingTime_label) {
                        nbValid++;
                        if (totalDrivingTime_this < totalDrivingTime_label) {
                            //case1.1.1.1 reducedCost_this< ;total working <; total driving<
                            nbStrict++;
                        }
                        BitSet unreachableSet_this = this.getUnreachableTripId();
                        BitSet unreachableSet_label = other.getUnreachableTripId();
                        //求这俩的交集，看看是不是交集是this,说明 this 不能扩展的点更少，可扩展的可能性是更多的，因此，this的label更好
                        //首先克隆下来或者说给他一个第三个水杯放交集
// 正确版子集判断
                        BitSet temp = (BitSet) unreachableSet_this.clone();
                        temp.andNot(unreachableSet_label);
                        boolean isSubSet = temp.isEmpty();
                        if (isSubSet) {
                            nbValid++;
                            if (unreachableSet_this.cardinality() < unreachableSet_label.cardinality()) {
                                nbStrict++;
                            }
                            if (visited_This.cardinality() > 1 && visited_Other.cardinality() > 1) {
//                                if (this.currentNode.getTrip().getNbVehicleNeed() < 2) {
                                    if (departureTimeOfThisLabel <= departureTimeOfOtherLabel) {
                                        nbValid++;
                                        if (departureTimeOfThisLabel < departureTimeOfOtherLabel) {
                                            nbStrict++;
                                        }
                                    }
//                                }
                            }

                        }
                    }
                }
            }
//            if (this.currentNode.getTrip().getNbVehicleNeed() < 2) {

                if (nbStrict >= 1 && nbValid == 5) {
                    whetherDominates = true;
                    // System.out.println("this label "+this+ "dominates \n"+label+" in the same status");
                }
//            }
//        }
        //}
        return whetherDominates;
    }

    public boolean whetherThisDominatesUnderDifferentDrivingStatusForwardLabelingAlgorithm(Label other) {
        int departureTimeOfThisLabel = this.getCurrentNodeDepTime();
        int departureTimeOfOtherLabel = other.getCurrentNodeDepTime();
        int moreWaitThis = 0;
        if (this.getCurrentNodeDepTime() < other.getCurrentNodeDepTime()) {
            moreWaitThis = other.getCurrentNodeDepTime() - this.getCurrentNodeDepTime();
        }
        double idleCost = this.graphRelatedToGivenDepot.getInstance().getIdleTimeCostForDriverPerUnit();
        double moreWaitCost = moreWaitThis * idleCost;

        double changeOverCost = this.graphRelatedToGivenDepot.getInstance().getCostForChangeOver();
        boolean whetherDominates = false;
        double reducedCost_this = this.getTotalReducedCost();
        double reducedCost_label = other.getTotalReducedCost();

        double totalWorkingTime_this = this.getTotalWorkingTime();
        double totalWorkingTime_label = other.getTotalWorkingTime();
        double totalDrivingTime_this = this.getTotalDrivingTime();
        double totalDrivingTime_label = other.getTotalDrivingTime();


        //针对第一个node希望越晚越好
        boolean whetherCurrentNodeIsTrip = (this.currentNode.getNodeType() == "trip");
        int idTrip = this.currentNode.getId();
        Trip trip = this.graphRelatedToGivenDepot.getInstance().getTrip(idTrip);
        int nbStrict = 0;
        int nbValid = 0;

        //接下来开始进行比较，只要有一个严格不等式或者真子集成立，那么这个标签就是更好的
        BitSet visited_This = this.visitedTripId;
        BitSet visited_Other = other.visitedTripId;


        int duration = trip.getDuration();
//        if (whetherCurrentNodeIsTrip) {
            if (reducedCost_this + moreWaitCost + changeOverCost + EPS <= reducedCost_label) {
                nbValid++;
                if (reducedCost_this + moreWaitCost + changeOverCost + EPS < reducedCost_label) {
                    nbStrict++;
                }
                //1.1 reduced cost+ changeover <
                if (totalWorkingTime_this + moreWaitThis + EPS <= totalWorkingTime_label) {//+ moreWaitThis
                    nbValid++;
                    if (totalWorkingTime_this + moreWaitThis + EPS < totalWorkingTime_label) {//+ moreWaitThis
                        nbStrict++;
                    }
                    //1.1.1 reduced cost+ changeover <; totalWorking<
                    if (totalDrivingTime_this <= totalDrivingTime_label) {
                        nbValid++;
                        if (totalDrivingTime_this < totalDrivingTime_label) {
                            nbStrict++;
                        }
                        BitSet unreachableSet_this = this.getUnreachableTripId();
                        BitSet unreachableSet_label = other.getUnreachableTripId();
                        BitSet temp = (BitSet) unreachableSet_this.clone();
                        temp.andNot(unreachableSet_label);
                        boolean isSubSet = temp.isEmpty();
                        if (isSubSet) {
                            nbValid++;
                            if (unreachableSet_this.cardinality() < unreachableSet_label.cardinality()) {
                                nbStrict++;
                            }
                            //case: reducedCost_this= ;total working =; total driving=; proper subset
                            if (visited_This.cardinality() > 1 && visited_Other.cardinality() > 1) {//只考虑除了第一个 trip以外的
//                                if (this.currentNode.getTrip().getNbVehicleNeed() < 2) {
                                    if (departureTimeOfThisLabel <= departureTimeOfOtherLabel) {
                                        nbValid++;
                                        if (departureTimeOfThisLabel < departureTimeOfOtherLabel) {
                                            nbStrict++;
                                        }
                                    }
//                                }
                            }
                        }
                    }
                }
            }
//            if (this.currentNode.getTrip().getNbVehicleNeed() < 2) {
                if (nbStrict >= 1 && nbValid == 5) {
                    whetherDominates = true;
                    // System.out.println("this label"+this+ "dominates\n"+label+" in different status");
                }
//            }
//        }

        return whetherDominates;
    }

    // now here is for the backward labeling prepare
    public Label extendFromPreviousNode(NewNode preNode, int preNodeWhetherDriving, int preNodeDepTime) {
        String startingDepotType = "startingDepot";
        String endingDepotType = "endingDepot";
        String tripType = "trip";

        Instance instance = this.graphRelatedToGivenDepot.getInstance();
        boolean whetherDrive = false;
        Label newLabel = new Label(this);// first copy the common attribute, then in the following need to set value to the  attribute belong to different part

        // 反向标记的核心方法：从前一个节点扩展到当前节点
        // case 1: 从ending depot扩展到最后一个trip (与前向标记中的case 2逻辑相反)
        if (this.currentNode.getNodeType().equals(endingDepotType)) {
            // pre: last trip  current: depot
            Depot depot = instance.getDepot(this.currentNode.getId());
            if (!preNode.getNodeType().equals(tripType)) {
                System.out.println("couldn't extend from the pre trip because it is not a trip type");
                return null;
            } else if (preNode.getNodeType().equals(tripType) && instance.getTrip(preNode.getId()).getIdOfEndCity() != depot.getIdOfCityAsDepot()) {//
                System.out.println("couldn't extend from the trip because the ending city is not the depot");
                return null;
            } else if (preNode.getNodeType().equals(tripType) && (instance.getTrip(preNode.getId()).getIdOfEndCity() == depot.getIdOfCityAsDepot())
                    && (preNodeDepTime < instance.getTrip(preNode.getId()).getEarliestDepartureTime() || preNodeDepTime > instance.getTrip(preNode.getId()).getLatestDepartureTime())) {//
                System.out.println("couldn't extend to the first trip because the starting time is not in the given time window");
                if (preNodeDepTime < instance.getTrip(preNode.getId()).getEarliestDepartureTime()) {
                    System.out.println("couldn't extend to the first trip because the starting time is less than earliest time");
                }
                if (preNodeDepTime > instance.getTrip(preNode.getId()).getLatestDepartureTime()) {
                    System.out.println("couldn't extend to the first trip because the starting time is greater than latest time");
                }
                return null;
            } else {
                Trip preTripAsLastTrip = instance.getTrip(preNode.getId());
                newLabel.currentNode = preNode;
                newLabel.whetherDriving = preNodeWhetherDriving;
                newLabel.currentNodeDepTime = preNodeDepTime;//

                double gamma = this.masterProblem.getDualValueFromNbAvailableDriver();
                double beta = this.masterProblem.getDualValueFromOneDriving(preNode.getTrip().getIdOfTrip());

                if (preNodeWhetherDriving == 1) {
                    whetherDrive = true;
                    newLabel.totalDrivingTime = preTripAsLastTrip.getDuration();
                    newLabel.totalReducedCost = this.getTotalReducedCost() + instance.getFixedCostForDriver() - gamma - beta;//
                    // System.out.println("now the  reduced cost is "+newLabel.getTotalReducedCost()+" includes: fixed cost  + instance.getFixedCostForDriver() + "dual value gamma_" + gamma
                    // + " dual value beta_" + beta_i);;//

                } else if (preNodeWhetherDriving == 0) {
                    whetherDrive = false;
                    newLabel.totalDrivingTime = 0;
                    newLabel.totalReducedCost = this.getTotalReducedCost() + instance.getFixedCostForDriver() - gamma;// fixed cost +gamma
                } else {
                    return null;
                }
                newLabel.totalWorkingTime = preTripAsLastTrip.getDuration();
                TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(preTripAsLastTrip, whetherDrive, preNodeDepTime);

                /**
                 *  here I need to initialize again because it is not in the common attributes
                 * */
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>();
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime.add(tripWithWorkingStatusAndDepartureTime);
                newLabel.visitedTripId = new BitSet(instance.getNbTrips());// why we use bitSet here
                newLabel.visitedTripId.set(preTripAsLastTrip.getIdOfTrip());

                //the following for loop is to check other trips maybe unreachable in preNode NodeWithLabels
                newLabel.unreachableTripId = new BitSet(instance.getNbTrips());
                newLabel.unreachableTripId = (BitSet) newLabel.visitedTripId.clone();
                newLabel.currentNodeDepTime = preNodeDepTime;

                for (int k = 0; k < instance.getNbTrips(); k++) {
                    Trip formerTrip = instance.getTrip(k);
                    int idOfFormerTrip = instance.getTrip(k).getIdOfTrip();
                    // because maybe there are some nodes could become reachable
                    // add a middle node
                    int earliestArrivalTimeOfFormerTrip = formerTrip.getEarliestDepartureTime() + formerTrip.getDuration();
                    if (earliestArrivalTimeOfFormerTrip + instance.getMinPlanTurnTime() > preNodeDepTime) {
                        //case1:   formerTrip ealiest arrival time is too late, the second trip already start
                        newLabel.unreachableTripId.set(idOfFormerTrip);
                    } else {
                        //case2: add the formerTrip then it will exceed the total working time
                        int minAddWorkingTime = instance.getMinPlanTurnTime() + formerTrip.getDuration();

                        int latestDepOfFormerTrip = formerTrip.getLatestDepartureTime();
                        int minWorkingTimeByLargeConnection = preNodeDepTime - latestDepOfFormerTrip;
                        if (minWorkingTimeByLargeConnection > minAddWorkingTime) {
                            minAddWorkingTime = minWorkingTimeByLargeConnection;
                        }
                        int totalWorkingAddFormerTrip = (int) (newLabel.totalWorkingTime + minAddWorkingTime);
                        if (totalWorkingAddFormerTrip > instance.getMaxWorkingTime()) {
                            newLabel.unreachableTripId.set(idOfFormerTrip);//Check0523
                        }
                    }
                }
                /**
                 * this is for the first trip, we add this attribute value which is not be shown up in constructor
                 * */
                newLabel.whetherBeExtendedFromPreNode = false;
                newLabel.endId = -1;
                return newLabel;
            }
        }
        //case 2: 从schedule第一个trip反向扩展到starting depot (label扩展于schedule arc相反)
        if (preNode.getNodeType().equals(startingDepotType)) {
            Depot depot = instance.getDepot(preNode.getId());
            if (!this.currentNode.getNodeType().equals(tripType)) {
                return null;
            } else if (this.getTotalDrivingTime() > instance.getMaxDrivingTime() || instance.getTrip(this.currentNode.getId()).getIdOfStartCity() != depot.getIdOfCityAsDepot()) {
                //if the current trip driving time exceed the limitation, it should not be extended, or it does not end at a good depot
                return null;
            } else {
                newLabel.currentNode = preNode;
                newLabel.whetherDriving = this.whetherDriving;//here should equal to -1
                newLabel.totalDrivingTime = this.getTotalDrivingTime();
                newLabel.totalWorkingTime = this.getTotalWorkingTime();
                newLabel.visitedTripId = (BitSet) this.visitedTripId.clone();
                newLabel.unreachableTripId = (BitSet) this.unreachableTripId.clone();
                newLabel.endId = depot.getIndexOfDepotAsStartingPoint();
                newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>(this.sequenceOfTripsWithWorkingStatusAndDepartureTime);

                if (this.whetherDriving == 1) {
                    newLabel.totalReducedCost = this.getTotalReducedCost();
                } else if (this.whetherDriving == 0) {
                    newLabel.totalReducedCost = this.getTotalReducedCost();
                } else {
                    return null;
                }

            }

        }
        //case3: general case, add trip before the
        //judge whether the current trip can be extended to the parameter trip, which means the two trips can be connected,
        // Pay Attention: and the current driving time is not exceed the maximum limitation
        //if it could extend, update the label
        //otherwise, it will return null;
        if (this.currentNode.getNodeType().equals(tripType) && preNode.getNodeType().equals(tripType)) {
            if (!instance.whetherHavePossibleArcAfterCleaning(preNode.getTrip().getIdOfTrip(), currentNode.getTrip().getIdOfTrip())
                    || this.getTotalDrivingTime() > instance.getMaxDrivingTime()) {
                // if the two trip are not connected or the current trip driving time already exceed the limitation,
                // then it should not be extended
                newLabel = null;
            } else {
                int latestDepOfPreNodeCalAccordingToTheCurrent = this.currentNodeDepTime - instance.getMinPlanTurnTime() - preNode.getTrip().getDuration();
                int latestDepartureTimeOfPreNode = preNode.getTrip().getLatestDepartureTime();
                int earliestDepartureTimeOfPreNode = preNode.getTrip().getEarliestDepartureTime();
                if (preNodeDepTime > latestDepartureTimeOfPreNode || preNodeDepTime < earliestDepartureTimeOfPreNode || preNodeDepTime > latestDepOfPreNodeCalAccordingToTheCurrent) {
                    newLabel = null;
                } else {
                    Trip tripInFirstNode = instance.getTrip(preNode.getTrip().getIdOfTrip());
                    Trip tripInSecondNode = instance.getTrip(this.currentNode.getId());
                    int conTime = this.currentNodeDepTime - (preNodeDepTime + tripInFirstNode.getDuration());
                    double beta = this.masterProblem.getDualValueFromOneDriving(tripInFirstNode.getIdOfTrip());
                    double delta = this.masterProblem.getDualValueFromLinkSelectScheduleAndTripOneDepTime(tripInFirstNode.getIdOfTrip(), preNodeDepTime);
                    newLabel.currentNode = preNode;
                    newLabel.whetherDriving = preNodeWhetherDriving;

                    if (preNodeWhetherDriving == 1 && this.whetherDriving == 1) {//case 1: both trip i and j driving
                        whetherDrive = true;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime() + tripInFirstNode.getDuration();
                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripInFirstNode.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - beta - delta
                                    - zeta;

                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - beta - delta;
                        }

                    } else if (preNodeWhetherDriving == 0 && this.whetherDriving == 1) {//case 2: i driving j passenger
                        whetherDrive = false;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime();
                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripInFirstNode.getIdOfTrip(), tripInSecondNode.getIdOfTrip());

                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - delta
                                    - zeta;

                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - delta;

                        }
                    } else if (preNodeWhetherDriving == 1 && this.whetherDriving == 0) {//case3: i passenger j driving
                        whetherDrive = true;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime() + tripInFirstNode.getDuration();

                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripInFirstNode.getIdOfTrip(), tripInSecondNode.getIdOfTrip());

                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - beta
                                    - delta - zeta;

                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    + instance.getCostForChangeOver() - beta
                                    - delta;
                        }

                    } else if (preNodeWhetherDriving == 0 && this.whetherDriving == 0) {
                        whetherDrive = false;
                        newLabel.totalDrivingTime = this.getTotalDrivingTime();
                        if (conTime < instance.getShortConnectionTimeForDriver()) {//the current trip and try to extended one is short connection
                            double zeta = this.masterProblem.getDualValueFromLinkDriverShortConTime(tripInFirstNode.getIdOfTrip(), tripInSecondNode.getIdOfTrip());
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - delta
                                    - zeta;
                        } else {//not short connection
                            newLabel.totalReducedCost = totalReducedCost + instance.getIdleTimeCostForDriverPerUnit() * conTime
                                    - delta;
                        }
                    } else {
                        newLabel = null;
                    }
                    newLabel.totalWorkingTime = this.getTotalWorkingTime() + conTime + tripInFirstNode.getDuration();
                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(tripInFirstNode, whetherDrive, preNodeDepTime);
                    newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime = new ArrayList<>(this.sequenceOfTripsWithWorkingStatusAndDepartureTime);//得先复制原来的，然后再添加
                    newLabel.sequenceOfTripsWithWorkingStatusAndDepartureTime.add(tripWithWorkingStatusAndDepartureTime);
                    newLabel.visitedTripId = (BitSet) (this.visitedTripId.clone());//here we should copy the previous trip
                    newLabel.visitedTripId.set(tripInFirstNode.getIdOfTrip());//then add the new trip
                    newLabel.currentNodeDepTime = preNodeDepTime;

                    /**
                     * this is the part which is not belong to the comment attribute value
                     * */
                    newLabel.endId = -1;

                    //here is to get the untouchable trip id set
                    newLabel.unreachableTripId = (BitSet) this.unreachableTripId.clone();
                    newLabel.unreachableTripId.set(preNode.getId());
                    //************
                    for (int k = 0; k < instance.getNbTrips(); k++) {
                        Trip formerTrip = instance.getTrip(k);
                        double earliestArrivalOfFormerTrip = formerTrip.getEarliestDepartureTime() + formerTrip.getDuration();
                        int idOfFormerTrip = instance.getTrip(k).getIdOfTrip();
                        if (earliestArrivalOfFormerTrip + instance.getMinPlanTurnTime() > preNodeDepTime) {
                            //case1:  former trip earliest arrival time is too late with minConnection, behind the preNode start time
                            newLabel.unreachableTripId.set(idOfFormerTrip);
                        } else {
                            //case2: add the minimum formerTrip then it will exceed the total working time
                            int latestDepOfFormerTrip = formerTrip.getLatestDepartureTime();
                            int minAddToWorking = instance.getMinPlanTurnTime() + formerTrip.getDuration();
                            int minAddToWorkingByLargeConnection = preNodeDepTime - latestDepOfFormerTrip;
                            if (minAddToWorkingByLargeConnection > minAddToWorking) {
                                minAddToWorking = minAddToWorkingByLargeConnection;
                            }
                            int totalWorkingAddFormerTrip = (int) (newLabel.totalWorkingTime + minAddToWorking);
                            if (totalWorkingAddFormerTrip > instance.getMaxWorkingTime()) {
                                newLabel.unreachableTripId.set(idOfFormerTrip);//check 0523
                            }
                        }
                    }
                }

            }
        }
        return newLabel;
    }


    public boolean whetherThisDominatesUnderSameDriveStatusBackwardLabeling(Label other) {
        boolean whetherDominates = false;
        BitSet visited_This = this.visitedTripId;
        BitSet visited_Other = other.visitedTripId;
        if (!this.currentNode.equals(other.currentNode)) return false;

        int nbValid = 0;
        int nbStrict = 0;
        int depTime_This = this.currentNodeDepTime;
        int depTime_Other = other.currentNodeDepTime;
        int timeDifference = Math.max(0, this.currentNodeDepTime - other.currentNodeDepTime);
        double idleCost = this.graphRelatedToGivenDepot.getInstance().getIdleTimeCostForDriverPerUnit();
        int idleCostDifference = (int) (timeDifference * idleCost);


        // 1. reduced cost 越小越好
        if (this.totalReducedCost + idleCostDifference + EPS <= other.totalReducedCost) {
            nbValid++;
            if (this.totalReducedCost + idleCostDifference + EPS < other.totalReducedCost) {
                nbStrict++;
            }

            // 2. total working time 越少越好（不用做 waitTime差异的考量）
            if (this.totalWorkingTime + timeDifference <= other.totalWorkingTime) {
                nbValid++;
                if (this.totalWorkingTime + timeDifference < other.totalWorkingTime) {
                    nbStrict++;
                }

                // 3. total driving time 越少越好
                if (this.totalDrivingTime <= other.totalDrivingTime) {
                    nbValid++;
                    if (this.totalDrivingTime < other.totalDrivingTime) {
                        nbStrict++;
                    }
                    // 4. unreachableTrip 子集判断
                    BitSet unreachableThis = this.unreachableTripId;
                    BitSet unreachableOther = other.unreachableTripId;
                    BitSet temp = (BitSet) unreachableThis.clone();
                    temp.andNot(unreachableOther);
                    if (temp.isEmpty()) {
                        nbValid++;
                        if (unreachableThis.cardinality() < unreachableOther.cardinality()) {
                            nbStrict++;
                        }
                        // 5. 出发时间越晚越好（backward 逻辑--与forward 相反）
                        if (visited_This.cardinality() > 2 && visited_Other.cardinality() > 2) {
                            if (this.currentNodeDepTime >= other.currentNodeDepTime) {
                                nbValid++;
                                if (this.currentNodeDepTime > other.currentNodeDepTime) {
                                    nbStrict++;
                                }
                            }
                        } else if (visited_This.cardinality() <= 1 && visited_Other.cardinality() <= 1) {
                            if (this.currentNodeDepTime <= other.currentNodeDepTime) {
                                nbValid++;
                                if (this.currentNodeDepTime < other.currentNodeDepTime) {
                                    nbStrict++;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nbStrict >= 1 && nbValid == 5) {
            whetherDominates = true;
            // System.out.println("this label "+this+ "dominates \n"+label+" in the same status");
        }
        return whetherDominates;
    }


    public boolean whetherThisDominatesUnderDifferentDriveStatusBackwardLabeling(Label other) {
        boolean whetherDominates = false;
        BitSet visited_This = this.visitedTripId;
        BitSet visited_Other = other.visitedTripId;
        int timeDifference = Math.max(0, this.currentNodeDepTime - other.currentNodeDepTime);
        double idleCost = this.graphRelatedToGivenDepot.getInstance().getIdleTimeCostForDriverPerUnit();
        int idleCostDifference = (int) (timeDifference * idleCost);
        double changeOverCost = this.graphRelatedToGivenDepot.getInstance().getCostForChangeOver();
        int nbValid = 0;
        int nbStrict = 0;
        // 1. reduced cost 越小越好
        if (this.totalReducedCost + idleCostDifference + changeOverCost + EPS <= other.totalReducedCost) {
            nbValid++;
            if (this.totalReducedCost + idleCostDifference + changeOverCost + EPS < other.totalReducedCost) {
                nbStrict++;
            }
            // 2. total working time 越少越好
            if (this.totalWorkingTime + timeDifference <= other.totalWorkingTime) {
                nbValid++;
                if (this.totalWorkingTime + timeDifference < other.totalWorkingTime) {
                    nbStrict++;
                }
                // 3. total driving time 越少越好
                if (this.totalDrivingTime <= other.totalDrivingTime) {
                    nbValid++;
                    if (this.totalDrivingTime < other.totalDrivingTime) {
                        nbStrict++;
                    }
                    // 4. unreachableTripId ⊆
                    BitSet unreachableThis = this.unreachableTripId;
                    BitSet unreachableOther = other.unreachableTripId;
                    BitSet temp = (BitSet) unreachableThis.clone();
                    temp.andNot(unreachableOther);
                    if (temp.isEmpty()) {
                        nbValid++;
                        if (unreachableThis.cardinality() < unreachableOther.cardinality()) {
                            nbStrict++;
                        }
                        if (visited_This.cardinality() > 2 && visited_Other.cardinality() > 2) {
                            // 5. departure time 越晚越好
                            if (this.currentNodeDepTime >= other.currentNodeDepTime) {
                                nbValid++;
                                if (this.currentNodeDepTime > other.currentNodeDepTime) {
                                    nbStrict++;
                                }
                            }
                        } else if (visited_This.cardinality() <= 1 && visited_Other.cardinality() <= 1) {
//                            if (this.currentNodeDepTime <= other.currentNodeDepTime) {
//                                nbValid++;
//                                if (this.currentNodeDepTime < other.currentNodeDepTime) {
//                                    nbStrict++;
//                                }
//                            }
                        }
                    }
                }
            }
        }
        if (nbStrict >= 1 && nbValid == 5) {
            whetherDominates = true;
            // System.out.println("this label " + this + " dominates \n" + other + " under different driving status (backward)");
        }
        return whetherDominates;
    }


    Comparator<Label> labelComparator = new Comparator<Label>() {
        @Override
        public int compare(Label label1, Label label2) {
            // 按 reduced cost 比较两个 Label 对象
            double reducedCost1 = label1.getTotalReducedCost();
            double reducedCost2 = label2.getTotalReducedCost();

            // 返回比较结果（按 reduced cost 升序排列）
            return Double.compare(reducedCost1, reducedCost2);
        }
    };


    @Override
    public String toString() {
        return
//                "Label{" +
                "currentNode=" + currentNode +
                        "departureTime" + currentNodeDepTime +
                        ", totalReducedCost=" + totalReducedCost +
                        ", totalDrivingTime=" + totalDrivingTime +
//                ", totalWorkingTime=" + totalWorkingTime +
                        ", visitedTripId=" + visitedTripId +
                        ", unreachableTrip" + unreachableTripId +
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
        NewNode node6 = new NewNode(instance.getTrip(5));
        NewNode node7 = new NewNode(instance.getTrip(6));
        NewNode node8 = new NewNode(instance.getDepot(1), false);//Depot0: 20 22 3
//        NewNode node3 = new NewNode(instance.getDepot(1), false);
//        NewNode node4 = new NewNode(instance.getTrip(2));//Trip2: 2 0 322 369 1
        GraphRelatedToGivenDepot graphRelatedToGivenDepot = new GraphRelatedToGivenDepot(instance, instance.getDepot(1));

        Label label1 = new Label(graphRelatedToGivenDepot, masterProblem, true);
        System.out.println("the default label" + label1);
////        Label label2 = label1.extendTo(node1, -1,-1);//495
////        System.out.println("check the label2 " + label2);
//        Label label3 = label1.extendToNextNode(node2, 1, 495);
//        System.out.println("check label 3" + label3);
//        Label label4 = label3.extendToNextNode(node3, 1, 600);
//        System.out.println("check label 4" + label4);
//        Label label5 = label4.extendToNextNode(node4, 1, 675);
//        System.out.println("check label 5" + label5);
//        Label label6 = label5.extendToNextNode(node5, 1, 775);
//        System.out.println("check label 6 " + label6);
//        Label label7 = label6.extendToNextNode(node6, 1, 840);
//        System.out.println("check label 7" + label7);
//        Label label8 = label7.extendToNextNode(node7, 1, 900);
//        System.out.println("check label 8" + label8);
//        Label lastLabel = label8.extendToNextNode(node8, 1, -1);
//        System.out.println("check last label" + lastLabel);


        Label label1_1 = new Label(graphRelatedToGivenDepot, masterProblem, false);
        System.out.println("default label" + label1_1);
        NewNode node9 = new NewNode(instance.getTrip(22));
//        NewNode node10 = new NewNode(instance.getTrip(3));
//        NewNode node11 = new NewNode(instance.getTrip(4));

//        Label label2_2 = label1_1.extendFromPreviousNode(node9, 1,375);//495
//        System.out.println("the from label1_1 to the last trip end at depot" + label2_2);


        //System.out.println("from the default label to Trip2" + label1.extendTo(node2, 1,495));
    }
}
