package Generator;

import NewGraph.GraphRelatedToGivenDepot;

import ColumnGe.MasterProblem;
import Instance.Instance;
import Instance.TripWithWorkingStatusAndDepartureTime;
import Instance.InstanceReader;
import Solution.DriverSchedule;
import Solution.SchedulesReader;
import Solution.Solution;
import Instance.Depot;

import java.io.IOException;
import java.util.ArrayList;


/**
 * This class considering all the depots
 * try to  find those the most given number of driver schedules, with minimum-reduced cost ;
 * we don't need the most reduced, which is only interest in the objective function, but not very help for the whole column generation
 * among all the generated mini-reduced cost driver schedules
 */

public class SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots {
    private Instance instance;
    private MasterProblem masterProblem;
    private double mostMinReducedCost;
    private int nbTotalGenerateLabels = 0;
    private double totalDominanceTime = 0;
    private double totalLabelingTime = 0;
    private long generaPathsTime = 0;

    private static final double epsilon = 1e-6;


    public SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots(Instance instance, MasterProblem masterProblem) {
        this.instance = instance;
        this.masterProblem = masterProblem;
        this.mostMinReducedCost = Double.MAX_VALUE;
        this.totalDominanceTime = 0;
        this.nbTotalGenerateLabels = 0;
    }


    //here we only generate the most min reduced cost trips among all minReducedTrips based on a given StartingTrip
    public ArrayList<DriverSchedule> generateSchedulesWithGivenNegReducedCostAmongAllStartingTrips() {
        this.mostMinReducedCost = Double.MAX_VALUE; // this need to be updated each iteration
        long startTime = System.currentTimeMillis();
        ArrayList<DriverSchedule> driverScheduleArrayList = new ArrayList<DriverSchedule>();
        for (int p = 0; p < instance.getNbDepots(); p++) {
            Depot givenDepot = instance.getDepot(p);
            GraphRelatedToGivenDepot graphRelatedToGivenDepot = new GraphRelatedToGivenDepot(this.instance, givenDepot);
            PathsGeneratorBasedOnGivenDepot pathsGeneratorBasedOnGivenDepot = new PathsGeneratorBasedOnGivenDepot(graphRelatedToGivenDepot, this.masterProblem);

            long start = System.nanoTime();
            ArrayList<DriverSchedule> driverSchedulesGeneratedByForwardLabeling = pathsGeneratorBasedOnGivenDepot.generateNegReducedCostPathsBasedOnAGivenStartingDepotForwardLabeling();
            int nbLabelInDepotP = pathsGeneratorBasedOnGivenDepot.getNbGenerateLabelsWithGivenDepot();
            nbTotalGenerateLabels = nbTotalGenerateLabels + nbLabelInDepotP;
            totalDominanceTime = totalDominanceTime + pathsGeneratorBasedOnGivenDepot.getDurationOfDominanceInMilliSecondTime();
            totalLabelingTime = totalLabelingTime + pathsGeneratorBasedOnGivenDepot.getDurationOfLabelingInMilliSec();

            double minReducedCostBasedOnGivenDepot_forward = pathsGeneratorBasedOnGivenDepot.getMinReducedCost_ForwardLabel();
            System.out.println("reduced cost given by forward labeling for depot_" + p + " is " + minReducedCostBasedOnGivenDepot_forward);
            if (!driverSchedulesGeneratedByForwardLabeling.isEmpty()) {
                if (minReducedCostBasedOnGivenDepot_forward <=0 && minReducedCostBasedOnGivenDepot_forward< mostMinReducedCost) {
                    mostMinReducedCost = minReducedCostBasedOnGivenDepot_forward;
                }
                driverScheduleArrayList.addAll(driverSchedulesGeneratedByForwardLabeling);
            }

//            ArrayList<DriverSchedule> driverSchedulesGeneratedByBackwardLabeling = pathsGeneratorBasedOnGivenDepot.generateNegReducedCostPathsBasedOnAGivenEndingDepotBackwardLabeling();
//
//            double minReducedCostBasedOnGivenDepot_Backward = pathsGeneratorBasedOnGivenDepot.getMinReducedCost_BackwardLabel();
//            System.out.println("reduced cost given by backward labeling for depot_" + p + " is " + minReducedCostBasedOnGivenDepot_Backward);
//            if (!driverSchedulesGeneratedByBackwardLabeling.isEmpty()) {
//                if (minReducedCostBasedOnGivenDepot_Backward<=0 && minReducedCostBasedOnGivenDepot_Backward < mostMinReducedCost) {
//                    mostMinReducedCost = minReducedCostBasedOnGivenDepot_Backward;
//                }
//                driverScheduleArrayList.addAll(driverSchedulesGeneratedByBackwardLabeling);
//            }

            //consider two method which the most
//
//            //Post-process: Add some schedule to make it have useful diverse departure times and push these dynamic in the CG process
//            // add those trip with passenger status time according to the driving status
//
//            int nbAddScheduleChangePassengerTimeAccordingToDriverTime = 0;
//            //1.针对所有的schedule, 收集每个 trip 的 driver 是 driving 出发时间
//            ArrayList<DriverSchedule> originalSchedules  = new ArrayList<>(driverScheduleArrayList);
//            // given depot the schedules we generated by labeling
//
//            for (int i = 0; i < instance.getNbTrips(); i++) {
//                int idTrip = instance.getTrip(i).getIdOfTrip();
//                BitSet departureTimesUnderDrivingStatus_I = new BitSet();
//                // 第一步：收集所有 driving 状态下该 trip 的出发时间
//                for (DriverSchedule schedule : originalSchedules) {
//                    for (TripWithWorkingStatusAndDepartureTime trip : schedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
//                        if (trip.getIdOfTrip() == idTrip && trip.getDrivingStatus()) {
//                            departureTimesUnderDrivingStatus_I.set(trip.getDepartureTime());
//                        }
//                    }
//                }
//                //System.out.println("dep time under driving status related to trip_" + i + "includes: " + departureTimesUnderDrivingStatus_I);
//
//
//                // 第二步：尝试把 passenger 状态的出发时间改成 driving 状态下的时间
//                for (int depTimeDriving = departureTimesUnderDrivingStatus_I.nextSetBit(0);
//                     depTimeDriving >= 0;
//                     depTimeDriving = departureTimesUnderDrivingStatus_I.nextSetBit(depTimeDriving + 1)) {
//                    // your logic
//                    for (DriverSchedule schedule : originalSchedules) {
//                        //System.out.println("original schedule: "+schedule);
//                        if (schedule.whetherTripPresent(idTrip)) {
//                            for (int j = 0; j < schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); j++) {
//                                TripWithWorkingStatusAndDepartureTime trip = schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j);
//                                if (trip.getIdOfTrip() == idTrip && !trip.getDrivingStatus()) {
//                                    int currentTimeAsPassenger = trip.getDepartureTime();
//                                    if (currentTimeAsPassenger != depTimeDriving) {
//                                        boolean feasible = whetherCouldChangeConsiderFeasibilityConnection(depTimeDriving, schedule, j, trip.getDuration());
//                                        if (feasible) {
//                                            DriverSchedule newSchedule = schedule.deepCopy();
//                                            // System.out.println("copy schedule: "+ newSchedule);
//                                            newSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j).setDepartureTime(depTimeDriving);
//                                            if (newSchedule.whetherFeasible(false)) {//also need to check check the total working time,connection time
//                                                driverScheduleArrayList.add(newSchedule);
//                                                nbAddScheduleChangePassengerTimeAccordingToDriverTime = nbAddScheduleChangePassengerTimeAccordingToDriverTime + 1;
//                                                System.out.println("new schedule is added in labeling post-process:" + newSchedule);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            System.out.println("add new schedules change driver time to passenger time in labeling post-process:" + nbAddScheduleChangePassengerTimeAccordingToDriverTime);
//
//
//
//            //对称的做法；
//            int nbAddScheduleChangeDriverTimeAccordingToPassenger = 0;
//            for (int i = 0; i < instance.getNbTrips(); i++) {
//                int idTrip = instance.getTrip(i).getIdOfTrip();
//                BitSet departureTimesUnderPassengerStatus_I = new BitSet();
//
//                // 第一步：收集所有 passenger 状态下该 trip 的出发时间
//                for (DriverSchedule schedule : originalSchedules) {
//                    for (TripWithWorkingStatusAndDepartureTime trip : schedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
//                        if (trip.getIdOfTrip() == idTrip && !trip.getDrivingStatus()) {
//                            departureTimesUnderPassengerStatus_I.set(trip.getDepartureTime());
//                        }
//                    }
//                }
//                //System.out.println("dep time under passenger status related to trip_" + i + "includes: " + departureTimesUnderPassengerStatus_I);
//
//                // 第二步：尝试把 driving 状态的出发时间改成 passenger 状态下的时间
//                for (int depTimePassenger = departureTimesUnderPassengerStatus_I.nextSetBit(0);
//                     depTimePassenger >= 0;
//                     depTimePassenger = departureTimesUnderPassengerStatus_I.nextSetBit(depTimePassenger + 1)) {
//                    for (DriverSchedule schedule : originalSchedules) {
//                        if (schedule.whetherTripPresent(idTrip)) {
//                            for (int j = 0; j < schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); j++) {
//                                TripWithWorkingStatusAndDepartureTime trip = schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j);
//                                if (trip.getIdOfTrip() == idTrip && trip.getDrivingStatus()) {
//                                    int currentTimeAsDriving = trip.getDepartureTime();
//                                    if (currentTimeAsDriving != depTimePassenger) {
//                                        boolean feasible = whetherCouldChangeConsiderFeasibilityConnection(depTimePassenger, schedule, j, trip.getDuration());
//                                        if (feasible) {
//                                            DriverSchedule newSchedule = schedule.deepCopy();
//                                            TripWithWorkingStatusAndDepartureTime newTrip = newSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j);
//                                            newTrip.setDepartureTime(depTimePassenger);
//
//                                                if (newSchedule.whetherFeasible(false)) {//also need to check the total working time,connection time
//                                                    driverScheduleArrayList.add(newSchedule);
//                                                    nbAddScheduleChangeDriverTimeAccordingToPassenger = nbAddScheduleChangeDriverTimeAccordingToPassenger + 1;
//                                                    // System.out.println(" new schedule is added！ " + newSchedule);
//
//                                                }
//
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            System.out.println("add new schedules change driver time to passenger time" + nbAddScheduleChangeDriverTimeAccordingToPassenger);
        }
        long endTime = System.currentTimeMillis();
        generaPathsTime = (endTime - startTime) / 1000;
        System.out.println("consider all the depot the mini-reduced cost " + mostMinReducedCost);
        System.out.println("Negative reduced cost schedules size:" + driverScheduleArrayList.size());
       // System.out.println("Driver schedules reported:" + driverScheduleArrayList);
        System.out.println("labeling time considering all depots in milli sec: " + totalLabelingTime);
        // System.out.println("dominance time considering all depots in milli sec: "+totalDominanceTime);
        return driverScheduleArrayList;
    }

    public double getGenerateTime() {
        return generaPathsTime;
    }

    public double getMostMinReducedCost() {
        return mostMinReducedCost;
    }

    public int getNbTotalGenerateLabels() {
        return nbTotalGenerateLabels;
    }

    public double getTotalDominanceTime() {
        return totalDominanceTime;
    }

    public double getTotalLabelingTime() {
        return totalLabelingTime;
    }


    private boolean whetherCouldChangeConsiderFeasibilityConnection(int depTime_new, DriverSchedule driverSchedule, int j, int durationCurrentTrip) {
        boolean whetherCouldChange = false;
        if (j == 0) {
            TripWithWorkingStatusAndDepartureTime tripLatter = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j + 1);
            int depTripLatter = tripLatter.getDepartureTime();
            int conTime_ToNext = depTripLatter - (depTime_new + durationCurrentTrip);
            if (conTime_ToNext >= instance.getMinPlanTurnTime()) {
                whetherCouldChange = true;
            }
        } else if (j == driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size() - 1) {
            TripWithWorkingStatusAndDepartureTime tripFormer = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j - 1);
            int depTripFormer = tripFormer.getDepartureTime();
            int durationFormer = tripFormer.getDuration();
            int conTime_FromFormer = depTime_new - (depTripFormer + durationFormer);
            if (conTime_FromFormer >= instance.getMinPlanTurnTime()) {
                whetherCouldChange = true;
            }
        } else {
            TripWithWorkingStatusAndDepartureTime tripLatter = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j + 1);
            int depTripLatter = tripLatter.getDepartureTime();
            int conTime_ToNext = depTripLatter - (depTime_new + durationCurrentTrip);
            TripWithWorkingStatusAndDepartureTime tripFormer = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j - 1);
            int depTripFormer = tripFormer.getDepartureTime();
            int durationFormer = tripFormer.getDuration();
            int conTime_FromFormer = depTime_new - (depTripFormer + durationFormer);
            if (conTime_ToNext >= instance.getMinPlanTurnTime() && conTime_FromFormer >= instance.getMinPlanTurnTime()) {
                whetherCouldChange = true;
            }
        }
        return whetherCouldChange;
    }

    @Override
    public String toString() {
        return "SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots{" +
                "instance=" + instance +
                ", masterProblem=" + masterProblem +
                ", mostMinReducedCost=" + mostMinReducedCost +
                ", generaPathsTime=" + generaPathsTime +
                '}';
    }

    public static void main(String[] args) throws IOException {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips030_combPer0.25_TW4.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        SchedulesReader schedulesReader = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips030_combPer0.25_TW4.txt", instance);

        Solution initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);
        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());

        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
        masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况
        SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots subPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots = new SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots(instance, masterProblem);
//        Schedules schedules = subPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots.generateSchedulesWithMostMinReducedCostAmongAllStartingTrips();
        ArrayList<DriverSchedule> driverScheduleArrayList = subPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots.generateSchedulesWithGivenNegReducedCostAmongAllStartingTrips();
        System.out.println("all paths with the most miniReducedCost : " + driverScheduleArrayList);
        System.out.println(" the most mini reduced Cost is " + subPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots.getMostMinReducedCost());// answer:-7972.0
        System.out.println(subPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots.getGenerateTime() + "sec");
    }
}
