package Benchmark;

import ColumnGe.MasterProblem;
import Instance.Instance;
import Instance.InstanceReader;
import Solution.SchedulesReader;
import Solution.Solution;
import ilog.concert.IloException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class BenchmarkForDriverScheduleCG {
    private static String directory = "instances";
    private static String initialColumnDirectory = "initialSchedules";
    private static String outputFileDirectory = "resultsOfSchedulesFromCG";


    private static PrintWriter writer;// use for loop and local writer

    private static String outputExcelDirectory = "outPutFileCG.csv";

    private static PrintWriter writerResultInformation;


    public static void main(String[] args) throws IloException, FileNotFoundException {
        read(args);
        solveFromFiles();
    }

    public static void read(String[] args) {
        HashMap<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            i++;
            String val = args[i];
            options.put(arg, val);
        }

        if (options.containsKey("-d")) {
            directory = options.get("-d");
        }

        if (options.containsKey("-initial")) {
            initialColumnDirectory = options.get("-initial");
        }

        if (options.containsKey("-result")) {
            outputFileDirectory = options.get("-result");
        }

        if (options.containsKey("-out")) {
            outputExcelDirectory = options.get("-out");
        }
    }

    public static void solveFromFiles() throws IloException, FileNotFoundException {
        File f = new File(directory);
        writerResultInformation = new PrintWriter(outputExcelDirectory);
        writerResultInformation.println("name,minPlanTime,maxPlanTime,shortConTimeDriver,shortConTimeVehicle,maxDrivingTime,maxWorkingTime,"
                + "nbCities,nbTrips,nbNodes,nbArcs,maxPercentCombine,timeWindowRange(min),maxNbDriverAvailable," +
                "initialNbColumns,finalNbColumns,nbIteration," +
                "initialMasterObjectiveValue,finalMasterObjectiveValue,finalLowerBoundMP,finalIntegerObjectiveValue,gap(%),elapsedTime(sec)," +
                "timeOfSolvingSubPByCplex(milli sec)");//,timeOfSolvingSubPByLabelingAlgorithm(milli sec)
        writerResultInformation.flush();

        if (f.isDirectory()) {
            File[] listOfFiles = f.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                File fileI = listOfFiles[i];
                String fileName = fileI.getName();
                System.out.println("instanceFileName" + fileName);
                String outputFileName = "scheduleSolution_" + fileName;
                File outputFile = new File(outputFileDirectory, outputFileName);

                String initialColumnName = fileName;
                initialColumnName = initialColumnName.replace("inst", "feaSol");
                String initialColumnPath = initialColumnDirectory + "\\" + initialColumnName;
                File initialColumnPathFile = new File(initialColumnPath, initialColumnName);
                System.out.println(initialColumnPath);
                System.out.println(initialColumnName);

                try {
                    writer = new PrintWriter(outputFile);

                    if (fileName.startsWith("inst") && fileName.endsWith(".txt")) {
                        InstanceReader instanceReader = new InstanceReader(fileI.getAbsolutePath());
                        Instance instance = instanceReader.readFile();
                        System.out.println(instance);
                        writerResultInformation.print(instance.getNameOfInstance() + ",");
                        writerResultInformation.print("15" + ",");
                        writerResultInformation.print(instance.getShortConnectionTimeForDriver() + ",");
                        writerResultInformation.print(instance.getShortConnectionTimeForVehicle() + ",");
                        writerResultInformation.print(instance.getMaxPlanTime() + ",");
                        writerResultInformation.print(instance.getMaxDrivingTime() + ",");
                        writerResultInformation.print(instance.getMaxWorkingTime() + ",");
                        writerResultInformation.print(instance.getNbCities() + ",");
                        writerResultInformation.print(instance.getNbTrips() + ",");
                        writerResultInformation.print(instance.getNbNodes() + ",");
                        writerResultInformation.print(instance.getMaxNbPossibleArc() + ",");
                        writerResultInformation.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                        writerResultInformation.print(instance.getTimeWindowRange()+",");
                        writerResultInformation.print(instance.getMaxNbDriverAvailable() + ",");

                        SchedulesReader schedulesReader = new SchedulesReader(initialColumnPath, instance);
                        //in this schedulesReader instead of using initialColumName, I use the absolute Path, then it can find the folderContent

                        Solution initialSchedules = schedulesReader.readFile();

                        MasterProblem masterProblemSolver = new MasterProblem(instance, initialSchedules);
                        Solution solution1 = masterProblemSolver.solve();
                        System.out.println(solution1);
                        writer.println("//instance " + i + " " + instance.getNameOfInstance() + ";");
                        writer.print("//nbNodes " + instance.getNbNodes() + ",");
                        writer.print(" nbArcs: " + instance.getMaxNbPossibleArc() + ",");
                        writer.print(" maxPerCombined: " + instance.getMaxPercentageOfCombinedTrip() + ",");
                        writer.print(" timeWindow"+ instance.getTimeWindowRange()+",");
                        writer.print(" nbDriverAvailable: " + instance.getMaxNbDriverAvailable() + ",");
                        writer.print(" solution gap: " + masterProblemSolver.getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() + "; \n");

                        if (solution1 != null) {
                            writer.println("//The final integer value is: " + masterProblemSolver.getFinalIntegerValueOfMasterObjective());
                            writer.println("//The elapsed time is: " + masterProblemSolver.getFinalTimeCostInSec() + " sec");
                            writer.println("//Total time in milli second of cplex solving pricing problem: " +masterProblemSolver.getTotalTimeCostForSolvingSubProblemByCplex() + " milli sec");
//                            writer.println("//Total time in milli second of labeling algorithm solving pricing problem: " +masterProblemSolver.getTotalTimeCostForSolvingSubProblemByLabeling()+ " milli sec");
                            writer.println(solution1);
                            writerResultInformation.print(initialSchedules.getDriverSchedules().size() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalColumns() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalNbIterations() + ",");
                            writerResultInformation.print(masterProblemSolver.getInitialRMPValue() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalMasterObjectiveValue() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalLowerBoundOfMP() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalIntegerValueOfMasterObjective() + ",");
                            writerResultInformation.print(masterProblemSolver.getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalTimeCostInSec() + ",");
                            writerResultInformation.print(masterProblemSolver.getTotalTimeCostForSolvingSubProblemByCplex()+ ",");
//                            writerResultInformation.print(masterProblemSolver.getTotalTimeCostForSolvingSubProblemByLabeling()+",");
                        } else {
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writer.print(0 + ",");
//                            writer.print(0 + ",");
                        }
                        writer.println();
                        writer.flush();
                        writerResultInformation.println();
                        writerResultInformation.flush();
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
        writer.close();
    }
}
