package Generator;
import NewGraph.GraphRelatedToGivenDepot;

import ColumnGe.MasterProblem;
import Instance.Instance;
import Instance.InstanceReader;
import Solution.DriverSchedule;
import Solution.SchedulesReader;
import Solution.Solution;
import Instance.Depot;

import java.io.IOException;
import java.util.ArrayList;


/**
 * This class considering all the depots
 * try to  find those most minimum-reduced cost driver schedules
 * among all the generated mini-reduced cost driver schedules
 * */

public class SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots {
    private Instance instance;
    private MasterProblem masterProblem;
    private double mostMinReducedCost;
    private int nbTotalGenerateLabels;
    private double totalDominanceTime;
    private long generaPathsTime = 0;


    public SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots(Instance instance, MasterProblem masterProblem) {
        this.instance = instance;
        this.masterProblem = masterProblem;
        this.mostMinReducedCost = Double.MAX_VALUE;
    }


    //here we only generate the most min reduced cost trips among all minReducedTrips based on a given StartingTrip
    public ArrayList<DriverSchedule> generateSchedulesWithMostMinReducedCostAmongAllStartingTrips() {
        this.mostMinReducedCost = 0; // this need to be updated each iteration
        long startTime = System.currentTimeMillis();
        ArrayList<DriverSchedule> driverScheduleArrayList = new ArrayList<DriverSchedule>();
        for (int p = 0; p < instance.getNbDepots(); p++) {
            Depot givenDepot = instance.getDepot(p);
            GraphRelatedToGivenDepot graphRelatedToGivenDepot = new GraphRelatedToGivenDepot(this.instance, givenDepot);
            PathsGeneratorBasedOnGivenDepot pathsGeneratorBasedOnGivenDepot = new PathsGeneratorBasedOnGivenDepot(graphRelatedToGivenDepot, this.masterProblem);
            ArrayList<DriverSchedule> driverSchedules = pathsGeneratorBasedOnGivenDepot.
                    generateMiniReducedCostPathsBasedOnAGivenStartingDepot();
            nbTotalGenerateLabels=nbTotalGenerateLabels+pathsGeneratorBasedOnGivenDepot.getNbGenerateLabelsWithGivenDepot();
            totalDominanceTime=totalDominanceTime+pathsGeneratorBasedOnGivenDepot.getDurationOfDominanceInNanoSecondTime();
            double minReducedCostBasedOnGivenDepot = pathsGeneratorBasedOnGivenDepot.getMinReducedCost();
            if (!driverSchedules.isEmpty()) {
                if (minReducedCostBasedOnGivenDepot <=mostMinReducedCost
                        && driverSchedules.size() != 0) {
                    mostMinReducedCost = minReducedCostBasedOnGivenDepot;
                    for (int s = 0; s < driverSchedules.size(); s++) {
                        DriverSchedule driverSchedule = driverSchedules.get(s);
                        driverScheduleArrayList.add(driverSchedule);
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        generaPathsTime = (endTime - startTime) / 1000;
        System.out.println("consider all the depot the mini-reduced cost "+mostMinReducedCost);
        System.out.println("Most reduced cost schedules size:"+driverScheduleArrayList.size());
        System.out.println("Best Driver schedules:"+driverScheduleArrayList);

        return driverScheduleArrayList;
    }

    public double getGenerateTime() {
        return generaPathsTime;
    }

    public double getMostMinReducedCost() {
        return mostMinReducedCost;
    }

    public int getNbTotalGenerateLabels(){
        return nbTotalGenerateLabels;
    }

    public double getTotalDominanceTime(){
        return totalDominanceTime;
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
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips030_combPer0.0_TW0.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        SchedulesReader schedulesReader = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips030_combPer0.0_TW0.txt", instance);

        Solution initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);
        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());

        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
        masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况
        SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots subPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots = new SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots(instance, masterProblem);
//        Schedules schedules = subPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots.generateSchedulesWithMostMinReducedCostAmongAllStartingTrips();
        ArrayList<DriverSchedule> driverScheduleArrayList = subPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots.generateSchedulesWithMostMinReducedCostAmongAllStartingTrips();
        System.out.println("all paths with the most miniReducedCost : " + driverScheduleArrayList);
        System.out.println(" the most mini reduced Cost is " + subPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots.getMostMinReducedCost());// answer:-7972.0
        System.out.println(subPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots.getGenerateTime() + "sec");
    }
}
