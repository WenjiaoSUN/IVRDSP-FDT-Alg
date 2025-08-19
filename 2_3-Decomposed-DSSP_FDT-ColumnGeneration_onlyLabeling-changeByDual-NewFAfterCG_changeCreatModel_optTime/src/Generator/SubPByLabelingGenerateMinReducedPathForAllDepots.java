//package Generator;
//
//import ColumnGe.MasterProblem;
//import Instance.Instance;
//import NewGraph.GraphRelatedToGivenDepot;
//import Solution.DriverSchedule;
//import Solution.SchedulesReader;
//import Solution.Solution;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import Instance.InstanceReader;
//import Instance.Depot;
//
//public class SubPByLabelingGenerateMinReducedPathForAllDepots {
//
//    /**
//     * This class considering all the depots
//     * try to  find those most minimum-reduced cost driver schedules， TO better solve the problem, we now consider
//     * among all the generated mini-reduced cost driver schedules
//     * */
//
//        private Instance instance;
//        private MasterProblem masterProblem;
//        private double mostMinReducedCost;
//        private int nbTotalGenerateLabels=0;
//        private double totalDominanceTime=0;
//        private  double totalLabelingTime=0;
//        private long generaPathsTime = 0;
//        private static final double epsilon = 1e-6;
//
//
//
//        public SubPByLabelingGenerateMinReducedPathForAllDepots(Instance instance, MasterProblem masterProblem) {
//            this.instance = instance;
//            this.masterProblem = masterProblem;
//            this.mostMinReducedCost = Double.MAX_VALUE;
//            this.totalDominanceTime=0;
//            this.nbTotalGenerateLabels=0;
//        }
//
//
//        //here we only generate the most min reduced cost trips among all minReducedTrips based on a given StartingTrip
//        public ArrayList<DriverSchedule> generateSchedulesWithMostMinReducedCostAmongAllStartingTrips() {
//            this.mostMinReducedCost = 0; // this need to be updated each iteration
//            long startTime = System.currentTimeMillis();
//            ArrayList<DriverSchedule> driverScheduleArrayList = new ArrayList<DriverSchedule>();
//            for (int p = 0; p < instance.getNbDepots(); p++) {
//                Depot givenDepot = instance.getDepot(p);
//
//
//                GraphRelatedToGivenDepot graphRelatedToGivenDepot = new GraphRelatedToGivenDepot(this.instance, givenDepot);
//                PathsGeneratorBasedOnGivenDepot pathsGeneratorBasedOnGivenDepot = new PathsGeneratorBasedOnGivenDepot(graphRelatedToGivenDepot, this.masterProblem);
////            long start = System.nanoTime();
//                ArrayList<DriverSchedule> driverSchedules = pathsGeneratorBasedOnGivenDepot.
//                        generateMiniReducedCostPathsBasedOnAGivenStartingDepot();
////            long end=System.nanoTime();
////            long labeling=end-start;
////            System.out.println("labeling time in milli sec: "+labeling/1e6);
//                int nbLabelInDepotP=pathsGeneratorBasedOnGivenDepot.getNbGenerateLabelsWithGivenDepot();
//                System.out.println("nbLabelsGenerated in depot_"+p +" is "+nbLabelInDepotP);
//                nbTotalGenerateLabels=nbTotalGenerateLabels+nbLabelInDepotP;
//                totalDominanceTime=totalDominanceTime+pathsGeneratorBasedOnGivenDepot.getDurationOfDominanceInMilliSecondTime();
//                totalLabelingTime=totalLabelingTime+pathsGeneratorBasedOnGivenDepot.getDurationOfLabelingInMilliSec();
//
//
//
//
//                double minReducedCostBasedOnGivenDepot = pathsGeneratorBasedOnGivenDepot.getMinReducedCost();
//                if (!driverSchedules.isEmpty()) {
////                if (minReducedCostBasedOnGivenDepot <=mostMinReducedCost
////                        && driverSchedules.size() != 0) {
////                    mostMinReducedCost = minReducedCostBasedOnGivenDepot;
////                    for (int s = 0; s < driverSchedules.size(); s++) {
////                        DriverSchedule driverSchedule = driverSchedules.get(s);
////                        driverScheduleArrayList.add(driverSchedule);
////                    }
////                }
//
//                    if (minReducedCostBasedOnGivenDepot < mostMinReducedCost - epsilon) {
//                        // 更优的 reduced cost，清空原来的，替换
//                        mostMinReducedCost = minReducedCostBasedOnGivenDepot;
//                        driverScheduleArrayList.clear(); // 💥 这里必须 clear
//                        driverScheduleArrayList.addAll(driverSchedules);
//                    } else if (Math.abs(minReducedCostBasedOnGivenDepot - mostMinReducedCost) < epsilon) {
//                        // reduced cost 一样，说明是等价的另一批 schedule，追加
//                        driverScheduleArrayList.addAll(driverSchedules);
//                    }
//                }
//            }
//            long endTime = System.currentTimeMillis();
//            generaPathsTime = (endTime - startTime) / 1000;
//            System.out.println("consider all the depot the mini-reduced cost "+mostMinReducedCost);
//            System.out.println("Most reduced cost schedules size:"+driverScheduleArrayList.size());
//            System.out.println("Best Driver schedules:"+driverScheduleArrayList);
//
//            System.out.println("labeling time considering all depots in milli sec: "+totalLabelingTime);
//            System.out.println("dominance time considering all depots in milli sec: "+totalDominanceTime);
//
//            return driverScheduleArrayList;
//        }
//
//        public double getGenerateTime() {
//            return generaPathsTime;
//        }
//
//        public double getMostMinReducedCost() {
//            return mostMinReducedCost;
//        }
//
//        public int getNbTotalGenerateLabels(){
//            return nbTotalGenerateLabels;
//        }
//
//        public double getTotalDominanceTime(){
//            return totalDominanceTime;
//        }
//
//        public double getTotalLabelingTime(){return  totalLabelingTime;}
//
//        @Override
//        public String toString() {
//            return "SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots{" +
//                    "instance=" + instance +
//                    ", masterProblem=" + masterProblem +
//                    ", mostMinReducedCost=" + mostMinReducedCost +
//                    ", generaPathsTime=" + generaPathsTime +
//                    '}';
//        }
//
//        public static void main(String[] args) throws IOException {
//            InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips030_combPer0.0_TW0.txt");//test vehicle example
//            Instance instance = reader.readFile(); //this will  read the file
//            System.out.println(instance);
//            SchedulesReader schedulesReader = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips030_combPer0.0_TW0.txt", instance);
//
//            Solution initialSchedules = schedulesReader.readFile();
//            System.out.println(initialSchedules);
//            System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());
//
//            MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
//            masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况
//            SubPByLabelingGenerateMinReducedPathForAllDepots subPSolByLabelAlgGeneMinReducedCostPathsForAllDepots = new SubPByLabelingGenerateMinReducedPathForAllDepots(instance, masterProblem);
////        Schedules schedules = subPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots.generateSchedulesWithMostMinReducedCostAmongAllStartingTrips();
//            ArrayList<DriverSchedule> driverScheduleArrayList = subPSolByLabelAlgGeneMinReducedCostPathsForAllDepots.generateSchedulesWithMostMinReducedCostAmongAllStartingTrips();
//            System.out.println("all paths with the most miniReducedCost : " + driverScheduleArrayList);
//            System.out.println(" the most mini reduced Cost is " +subPSolByLabelAlgGeneMinReducedCostPathsForAllDepots.getMostMinReducedCost());// answer:-7972.0
//            System.out.println(subPSolByLabelAlgGeneMinReducedCostPathsForAllDepots.getGenerateTime() + "sec");
//        }
//
//
//}
