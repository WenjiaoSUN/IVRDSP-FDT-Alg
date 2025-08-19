package benchmark;

import Instance.Instance;
import Instance.InstanceReader;
import Solution.Solution;
//import Solver.Solver;
import Solver.SolverWithFormulation_1;
import ilog.concert.IloException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Benchmark {
    private static String directory = "instances";
    private static String warmStartDirectory = "warmStartFiles";
    private static String outputFile = "results.csv";
    private static PrintWriter writer;

    private static boolean whetherSolveRelaxationProblem = false;

    public static void main(String[] args) throws IloException {
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

        if (options.containsKey("-warm")) {
            warmStartDirectory = options.get("-warm");//*here add the warmStartFile Key
        }

        if (options.containsKey("-outF")) {
            outputFile = options.get("-outF");
        }

    }

    public static void solveFromFiles() throws IloException {
        try {
            writer = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        writer.println("name,minPlanTime,maxPlanTime,shortConTimeDriver,shortConTimeVehicle,maxDrivingTime,maxWorkingTime,nbCities,nbTrips,nbNodes," +
                "maxNbPossibleArc,maxPercentCombine,maxFolderTimeSlotAsTimeWindow,maxNbDriverAvailable,maxNbVehicleAvailable," +
                "lb,ub,gap(%),minNbDrivers,elapsedTime(s),");
        writer.flush();// write it into the csv result file

        File f = new File(directory);
        if (f.isDirectory()) {
            File[] listOfFiles = f.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                File fileI = listOfFiles[i];
                String fileName = fileI.getName();
                String warmStartFileName = fileName;
                warmStartFileName = warmStartFileName.replace("inst", "feaSol");
                String warmStartFilePath = warmStartDirectory + "\\" + warmStartFileName;
                System.out.println(warmStartFilePath);

                if (fileName.startsWith("inst_") && fileName.endsWith(".txt")) {
                    InstanceReader instanceReader = new InstanceReader(fileI.getAbsolutePath());
                    Instance instance = instanceReader.readFile();

                    SolverWithFormulation_1 solverWithWarmStart = new SolverWithFormulation_1(instance, warmStartFilePath, whetherSolveRelaxationProblem);
                    Solution sol = solverWithWarmStart.solveWithCplex();

//                    Solver solver = new Solver(instance);
//                    Solution sol = solver.solveWithCplex();
                    writer.print(instance.getNameOfInstance() + ",");
                    writer.print("15" + ",");
                    writer.print(instance.getMaxPlanTime() + ",");
                    writer.print(instance.getShortConnectionTimeForDriver() + ",");
                    writer.print(instance.getShortConnectionTimeForVehicle() + ",");
                    writer.print(instance.getMaxDrivingTime() + ",");
                    writer.print(instance.getMaxWorkingTime() + ",");
                    writer.print(instance.getNbCities() + ",");
                    writer.print(instance.getNbTrips() + ",");
                    writer.print(instance.getNbNodes() + ",");
                    writer.print(instance.getMaxNbPossibleArc()+",");
                    writer.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                    writer.print(instance.getMaxFolderOfTimeSlotAsTimeWindow()+",");
                    writer.print(instance.getMaxNbDriverAvailable() + ",");
                    writer.print(instance.getMaxNbVehicleAvailable() + ",");

                    if (whetherSolveRelaxationProblem == true) {
                        int intValue = (int) Math.floor(solverWithWarmStart.getUpperBound());
                        System.out.println(intValue);
                        writer.print(solverWithWarmStart.getUpperBound()+ ",");
                    } else {
                        writer.print(solverWithWarmStart.getLowerBound() + ",");
                        writer.print(solverWithWarmStart.getUpperBound() + ",");
                        writer.print(solverWithWarmStart.getGap() + ",");
                        if (sol != null) {
//                            writer.print(sol.getTotalFixedCost() + ",");
//                            writer.print(sol.getTotalIdleTimeCost() + ",");
//                            writer.print(sol.getTotalChangeoverCostForAllDriver() + ",");
                            writer.print(sol.getNbDriversInSolution() + ",");
//                            writer.print(sol.getNbVehiclesInSolution() + ",");
                        } else {
                            writer.print(0 + ",");
//                            writer.print(0 + ",");
//                            writer.print(0 + ",");
//                            writer.print(0 + ",");
//                            writer.print(0 + ",");
                        }
                    }
//                    writer.print(solver.getTimeInMilliSec() + ",");
                    writer.print(solverWithWarmStart.getTimeInSec() + ",");
                    writer.println();
                    writer.flush();
                }
            }
        }
        writer.close();
    }
}

