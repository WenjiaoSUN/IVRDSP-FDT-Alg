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
    private static PrintWriter writer;// writer csv file;

    private static String solutionDirectory="cplexSolResults";

    private static PrintWriter writerSolution;

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

        if(options.containsKey("-cplexSol")){
            solutionDirectory=options.get("-cplexSol");
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
                "lb,ub,gap(%),fixedCost,idleTimeCost,changeOverCost," +
                "nbDrivers,nbVehicles,elapsedTime(s),");
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

                String solutionFileName = "result_" + fileName;// this is before we want to fixed the varibles to have solution as warmFile
                String solutionFilePath=solutionDirectory+"\\"+fileName;
                File solutionFile=new File(solutionDirectory,solutionFileName);
                System.out.println("solution file path " +solutionFilePath);

                if (fileName.startsWith("inst_") && fileName.endsWith(".txt")) {
                    InstanceReader instanceReader = new InstanceReader(fileI.getAbsolutePath());
                    Instance instance = instanceReader.readFile();

                    SolverWithFormulation_1 solverWithWarmStart = new SolverWithFormulation_1(instance, warmStartFilePath, whetherSolveRelaxationProblem);
                    Solution sol = solverWithWarmStart.solveWithCplex();
                    //The following lines  are to write the solution in a standard format in a text
                    try {
                        writerSolution = new PrintWriter(solutionFile);
                        writerSolution.println("//instance");
                        writerSolution.println("//"+instance.getNameOfInstance());
                        // the above lines are to write solution to the standard format in a text

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
                        writer.print(instance.getMaxNbPossibleArc() + ",");
                        writer.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                        writer.print(instance.getMaxFolderOfTimeSlotAsTimeWindow() + ",");
                        writer.print(instance.getMaxNbDriverAvailable() + ",");
                        writer.print(instance.getMaxNbVehicleAvailable() + ",");

                        if (whetherSolveRelaxationProblem == true) {
                            int intValue = (int) Math.floor(solverWithWarmStart.getUpperBound());
                            System.out.println(intValue);
                            writer.print(solverWithWarmStart.getUpperBound() + ",");
                        } else {
                            writer.print(solverWithWarmStart.getLowerBound() + ",");
                            writer.print(solverWithWarmStart.getUpperBound() + ",");
                            writer.print(solverWithWarmStart.getGap() + ",");
                            if (sol != null) {
                                writer.print(sol.getTotalFixedCost() + ",");
                                writer.print(sol.getTotalIdleTimeCost() + ",");
                                writer.print(sol.getTotalChangeoverCostForAllDriver() + ",");
                                writer.print(sol.getNbDriversInSolution() + ",");
                                writer.print(sol.getNbVehiclesInSolution() + ",");


                                //The following lines  are to write the solution in a standard format in a text
                                writerSolution.println("//nbVehicles in the solution");
                                writerSolution.println("//"+sol.getNbVehiclesInSolution());
                                writerSolution.println("//nbDrivers in the solution");
                                writerSolution.println("//"+sol.getNbDriversInSolution());
                                writerSolution.println("//total cost of the solution");
                                writerSolution.println("//"+sol.getTotalCost());
                                writerSolution.println("//vehicle routes in the solution: idVehicle, nbTripsInPath [idTrip departureTime] ");
                                for (int r = 0; r < sol.getNbVehiclesInSolution(); r++) {
                                    writerSolution.println("vehicle " + sol.getPathForVehicles().get(r));
                                }
                                writerSolution.println("//driver schedules in the solution: idDriver, nbTripsInPath [idTrip idVehicleStay, status, departureTime]");
                                for (int s = 0; s < sol.getNbDriversInSolution(); s++) {
                                    writerSolution.println("driver " + sol.getPathForDrivers().get(s));
                                }
                                // the above lines are to write solution to the standard format in a text

                            } else {
                                writer.print(0 + ",");
                                writer.print(0 + ",");
                                writer.print(0 + ",");
                                writer.print(0 + ",");
                                writer.print(0 + ",");
                            }
                        }
//                    writer.print(solver.getTimeInMilliSec() + ",");
                        writer.print(solverWithWarmStart.getTimeInSec() + ",");
                        writer.println();
                        writer.flush();

                        writerSolution.println();
                        writerSolution.flush();

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    writerSolution.close();
                 }
                }
        }
        writer.close();
    }
}

