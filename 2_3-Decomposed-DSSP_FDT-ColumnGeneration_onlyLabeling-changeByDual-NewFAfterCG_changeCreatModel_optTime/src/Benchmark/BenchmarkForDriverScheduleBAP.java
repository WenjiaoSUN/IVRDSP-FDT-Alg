//package Benchmark;
//
//import BranchAndPrice.BranchAndPriceSolver;
//import ColumnGe.MasterProblem;
//import Instance.Instance;
//import Instance.InstanceReader;
//import Solution.SchedulesReader;
//import Solution.Solution;
//import ilog.concert.IloException;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.HashMap;
//
//public class BenchmarkForDriverScheduleBAP {
//    private static String directory = "instances";
//    private static String initialColumnDirectory = "initialSchedules";
//    private static String outputFileDirectory = "resultsOfSchedulesFromBAP";
//
//    private static String outputExcelDirectory = "outPutFileBAP.csv";
//
//    private static PrintWriter writerResultInformation;
//
//    public static void main(String[] args) throws IloException, FileNotFoundException {
//        read(args);
//        solveFromFiles();
//    }
//
//    public static void read(String[] args) {
//        HashMap<String, String> options = new HashMap<>();
//        for (int i = 0; i < args.length; i++) {
//            String arg = args[i];
//            i++;
//            String val = args[i];
//            options.put(arg, val);
//        }
//
//        if (options.containsKey("-d")) directory = options.get("-d");
//        if (options.containsKey("-initial")) initialColumnDirectory = options.get("-initial");
//        if (options.containsKey("-result")) outputFileDirectory = options.get("-result");
//        if (options.containsKey("-out")) outputExcelDirectory = options.get("-out");
//    }
//
//    public static void solveFromFiles() throws IloException, FileNotFoundException {
//        File f = new File(directory);
//        writerResultInformation = new PrintWriter(outputExcelDirectory);
//        writerResultInformation.write("sep=,\n");
//        writerResultInformation.println("name,minPlanTime(min),maxPlanTime(min),shortConTimeDriver(min),shortConTimeVehicle(min),maxDrivingTime(min),maxWorkingTime(min),"
//                + "nbCities,nbTrips,nbTripStartFromDepot,nbNodes,nbArcs,maxPercentCombine,timeWindowRange(min),maxNbDriverAvailable,"
//                + "initialNbColumns,finalNbColumnsInModel,nbIteration,"
//                + "initialMasterObjectiveValue,finalMasterObjectiveValue,finalLowerBoundMP,finalIntegerObjectiveValue,gap(%),elapsedTime(sec),"
//                + "timeOfSolvingSubPByLabelingAlgorithm(milli sec),timeOfSolRMPByCplex(milli sec),timeOfSolveFinalIntegerProblemByCplex(milli sec),"
//                + "nbLabelsInTotal,nbLabelsFinalIteration,nbLabelsPerIteration");
//        writerResultInformation.flush();
//
//        if (f.isDirectory()) {
//            File[] listOfFiles = f.listFiles();
//            for (File fileI : listOfFiles) {
//                String fileName = fileI.getName();
//                if (!fileName.startsWith("inst") || !fileName.endsWith(".txt")) continue;
//
//                System.out.println("instanceFileName: " + fileName);
//
//                String initialColumnName = fileName.replace("inst", "feaSol");
//                File initialColumnPathFile = new File(initialColumnDirectory, initialColumnName);
//
//                try {
//                    InstanceReader instanceReader = new InstanceReader(fileI.getAbsolutePath());
//                    Instance instance = instanceReader.readFile();
//                    writerResultInformation.print(instance.getNameOfInstance() + ",");
//                    writerResultInformation.print("15" + ",");
//                    writerResultInformation.print(instance.getShortConnectionTimeForDriver() + ",");
//                    writerResultInformation.print(instance.getShortConnectionTimeForVehicle() + ",");
//                    writerResultInformation.print(instance.getMaxPlanTime() + ",");
//                    writerResultInformation.print(instance.getMaxDrivingTime() + ",");
//                    writerResultInformation.print(instance.getMaxWorkingTime() + ",");
//                    writerResultInformation.print(instance.getNbCities() + ",");
//                    writerResultInformation.print(instance.getNbTrips() + ",");
//                    writerResultInformation.print(instance.getNbTripCouldStartFromDepot() + ",");
//                    writerResultInformation.print(instance.getNbNodes() + ",");
//                    writerResultInformation.print(instance.getMaxNbPossibleArc() + ",");
//                    writerResultInformation.print(instance.getMaxPercentageOfCombinedTrip() + ",");
//                    writerResultInformation.print(instance.getTimeWindowRange() + ",");
//                    writerResultInformation.print(instance.getMaxNbDriverAvailable() + ",");
//
//                    SchedulesReader schedulesReader = new SchedulesReader(initialColumnPathFile.getAbsolutePath(), instance);
//                    Solution initialSchedules = schedulesReader.readFile();
//                    writerResultInformation.print(initialSchedules.getDriverSchedules().size() + ",");
//
//                    // ✨ Branch and Price Solver
//                    BranchAndPriceSolver solver = new BranchAndPriceSolver(instance, initialSchedules);
//                    solver.runBranchAndPrice();
//
//                    MasterProblem finalMP = solver.getBestMasterProblem();
//
//                    writerResultInformation.print(finalMP.getFinalColumns() + ",");
//                    writerResultInformation.print(finalMP.getFinalNbIterations() + ",");
//                    writerResultInformation.print(finalMP.getInitialRMPValue() + ",");
//                    writerResultInformation.print(finalMP.getFinalMasterObjectiveValue() + ",");
//                    writerResultInformation.print(finalMP.getFinalLowerBoundOfMP() + ",");
//                    writerResultInformation.print(finalMP.getFinalIntegerValueOfMasterObjective() + ",");
//                    writerResultInformation.print(finalMP.getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() + ",");
//                    writerResultInformation.print(finalMP.getFinalTimeCostInSec() + ",");
//                    writerResultInformation.print(finalMP.getTotalTimeCostForSolvingSubProblemByLabeling() + ",");
//                    writerResultInformation.print(finalMP.getTotalTimeOfSolRMPByCplex() + ",");
//                    writerResultInformation.print(finalMP.getTimeForSolvingLastIntegerProblem() + ",");
//                    writerResultInformation.print(finalMP.getTotalLabelsGenerateForAllIterations() + ",");
//                    writerResultInformation.print(finalMP.getFinalNbLabelsLastIteration() + ",");
//                    writerResultInformation.print(finalMP.getAverageLabelsByIteration() + ",");
//
//                    writerResultInformation.println();
//                    writerResultInformation.flush();
//
//                } catch (IOException | RuntimeException e) {
//                    System.err.println("❌ Error on instance: " + fileName);
//                    e.printStackTrace();
//                    writerResultInformation.println("ERROR");
//                    writerResultInformation.flush();
//                }
//            }
//        }
//        writerResultInformation.close();
//    }
//}
