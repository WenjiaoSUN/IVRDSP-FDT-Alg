package benchmark;

import Instance.Instance;
import Instance.InstanceReader;
import Solution.Solution;
import PathsForDriver.DriverSchedule;
import PathsForDriver.Schedules;
import PathsForDriver.SchedulesReader;

import Solution.PathForDriver;

import Solver.SolverWithFormulationBasedOnDriverSchedule;
import ilog.concert.IloException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Benchmark {
    private static String directory = "instances"; // in order to read the instances

    private static String scheduleDirectory = "schedules"; //in order to read the schedules obtained from the first phase
    //C:\Users\wsun\IdeaProjects\4(3)Decomposed-VehiclePart\

    //Different1(in total 4)---dont forger comment the extra constriant which am to gain the warmFiles
    private static String warmSecondPhaseDirectory="warmFiles";// this is to read the warmSolution getFrom extra constraints

    private static String outputFile = "results.csv";

//    private static String statisticsFile="statistics.csv";// here is to give a statistics result on the solution

    private static String solutionDirectory = "solutions";// in order to put the solutions in this direction
    private static PrintWriter writer;//this is for the output csv file
    private static boolean whetherSolveRelaxationProblem =false;
    private static  boolean whetherConsiderChangeOverCost=true ;
    private static PrintWriter writerSolution;

    public static void main(String[] args) throws IloException {
        read(args);
        try {
            solveFromFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        if (options.containsKey("-schedules")) {
            scheduleDirectory= options.get("-schedules");
        }
        //Different2
        if(options.containsKey("-warmFiles")){
            warmSecondPhaseDirectory=options.get("-warmFiles");
        }

        if (options.containsKey("-outF")) {
            outputFile = options.get("-outF");
        }


        if(options.containsKey("-solutions")){
            solutionDirectory=options.get("-solutions");
        }

    }

    public static void solveFromFiles() throws IloException, IOException {
        try {
            writer = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        writer.println("name,minPlanTime,maxPlanTime,shortConTimeDriver,shortConTimeVehicle,maxDrivingTime,maxWorkingTime," +
                "nbCities,nbTrips,maxPercentCombine,timeWindowRange,nbNodes,nbArcs,maxNbDriverAvailable,maxNbVehicleAvailable,lb,ub,gap(%)," +
                "totalCost,totalFixedCost,fixedCostForDrivers,fixedCostForVehicles,totalIdleTimeCost,idleTimeCostForDrivers,idleTimeCostForVehicles,changeOverCost," +
                "nbDrivers,nbVehicles,elapsedTime(sec),");
        writer.flush();// write it into the csv result file


        File f = new File(directory);

        if (f.isDirectory()) {
            File[] listOfFiles = f.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                File fileI = listOfFiles[i];
                String fileName = fileI.getName();// this is the instance file name

                String scheduleFileName = "scheduleSolution_" + fileName;// this is for read the schedules obtained from CG
                String scheduleFilePath = scheduleDirectory + "\\" + scheduleFileName;
                System.out.println(scheduleFilePath);
                System.out.println(scheduleFileName);

                //Different3
                //now is to add the new warm files
                String warmFileName="warmSPhase_"+fileName;// this is get from this code but add extra constraints to get a feasible solution
                String warmFilePath=warmSecondPhaseDirectory+"\\"+warmFileName;
                System.out.println(warmFilePath);
                System.out.println(warmFileName);

                //this part is  in order to output the standard solution in a designed path
//                String solutionFileName = "solution_" + fileName;
                String solutionFileName = "solution_" + fileName;// this is before we want to fixed the varibles to have solution as warmFile
                File solutionFile = new File(solutionDirectory, solutionFileName);

                try {
                    writerSolution = new PrintWriter(solutionFile);
                    if (fileName.startsWith("inst_") && fileName.endsWith(".txt")) {
                        InstanceReader instanceReader = new InstanceReader(fileI.getAbsolutePath());
                        Instance instance = instanceReader.readFile();

                        SchedulesReader schedulesReader = new SchedulesReader(scheduleFilePath, instance);
                        Schedules driverSchedules = schedulesReader.readFile();

//                        //the following two line is when we solve with no warm file
//                        SolverBasedOnDriverSchedule solverBasedOnDriverSchedule = new SolverBasedOnDriverSchedule(instance, driverSchedules, whetherSolveRelaxationProblem);
//                        Solution sol = solverBasedOnDriverSchedule.solveWithCplex();

////                        //the following two line is when we solve with no warm file and also no consider changeoverCost
                        SolverWithFormulationBasedOnDriverSchedule solverBasedOnDriverSchedule = new SolverWithFormulationBasedOnDriverSchedule(instance, driverSchedules, whetherSolveRelaxationProblem);
//                        Solution sol = solverBasedOnDriverSchedule.solveWithCplex();


                        //Different4 change
                         //now these two lines are solved problem when there are two warm files, whether consider solve relaxation, whether consider changeover
                        //SolverBasedOnDriverSchedule solverBasedOnDriverSchedule = new SolverBasedOnDriverSchedule(instance, driverSchedules, warmFilePath, whetherSolveRelaxationProblem,whetherConsiderChangeOverCost);
                        Solution sol = solverBasedOnDriverSchedule.solveWithCplexBasedOnGivenSchedules();

                        //This part is to write the solution in a standard format in a text
                        writerSolution.println("//instance");
                        writerSolution.println(instance.getNameOfInstance());
//                    Solver solver = new Solver(instance);
//                    Solution sol = solver.solveWithCplex();
                        //This follow part is for the Excel part
                        writer.print(instance.getNameOfInstance() + ",");
                        writer.print("15" + ",");
                        writer.print(instance.getMaxPlanTime() + ",");
                        writer.print(instance.getShortConnectionTimeForDriver() + ",");
                        writer.print(instance.getShortConnectionTimeForVehicle() + ",");
                        writer.print(instance.getMaxDrivingTime() + ",");
                        writer.print(instance.getMaxWorkingTime() + ",");
                        writer.print(instance.getNbCities() + ",");
                        writer.print(instance.getNbTrips() + ",");
                        writer.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                        writer.print(instance.getTimeWindowRange()+",");
                        writer.print(instance.getNbNodes() + ",");
                        writer.print(instance.getMaxNbPossibleArc() + ",");
//                    writer.print(solver.getLowerBound() + ",");
//                    writer.print(solver.getUpperBound() + ",");
//                    writer.print(solver.getGap() + ",");
                        writer.print(instance.getMaxNbDriverAvailable() + ",");
                        writer.print(instance.getMaxNbVehicleAvailable() + ",");
                        if (whetherSolveRelaxationProblem == true) {
                            int intValue = (int) Math.floor(solverBasedOnDriverSchedule.getUpperBound());
                            System.out.println(intValue);
                            writer.print(solverBasedOnDriverSchedule.getUpperBound()+ ",");
                        } else {
                            writer.print(solverBasedOnDriverSchedule.getLowerBound() + ",");
                            writer.print(solverBasedOnDriverSchedule.getUpperBound() + ",");
                            writer.print(solverBasedOnDriverSchedule.getGap() + ",");
                            if (sol != null) {
                                // this following part is for the output of Excel
                                writer.print(sol.getTotalCost() + ",");
                                writer.print(sol.getTotalFixedCost() + ",");
                                writer.print(sol.getFixedCostForDriver() + ",");
                                writer.print(sol.getFixedCostForVehicle() + ",");
                                writer.print(sol.getTotalIdleTimeCost() + ",");
                                writer.print(sol.getTotalIdleTimeCostOfAllDriver() + ",");
                                writer.print(sol.getTotalIdleTimeCostOfAllVehicle() + ",");

                                writer.print(sol.getTotalChangeoverCostForAllDriver() + ",");

                                writer.print(sol.getNbDriversInSolution() + ",");
                                writer.print(sol.getNbVehiclesInSolution() + ",");

                                //here is for the solution files
                                writerSolution.println("//nbVehicles in the solution");
                                writerSolution.println(sol.getNbVehiclesInSolution());
                                writerSolution.println("//nbDrivers in the solution");
                                writerSolution.println(sol.getNbDriversInSolution());
                                writerSolution.println("//total cost of the solution");
                                writerSolution.println(sol.getTotalCost());
                                writerSolution.println("//vehicle routes in the solution: idVehicle, nbTrips, idStDepot, [idTrips], idEnDepot");
                                for(int r=0;r<sol.getNbVehiclesInSolution();r++){
                                    writerSolution.println("vehicle "+sol.getPathForVehicles().get(r));
                                }
                                writerSolution.println("//driver schedules in the solution: idDriver, nbTrips, idStDepot, [idTrips]");
                                for (int s=0; s<sol.getNbDriversInSolution();s++){
                                    writerSolution.println("driver "+sol.getPathForDrivers().get(s));
                                }

                                if(instance.getMaxPercentageOfCombinedTrip()>0.00001){
                                    writerSolution.println("//combine parts: idTrip  idDrivingDriver idLeadingVehicle");
                                    for(int t=0;t<instance.getNbTrips();t++){
                                        if(instance.getTrip(t).getNbVehicleNeed()==2){
                                            int idCombineTrip=instance.getTrip(t).getIdOfTrip();
                                            for(int s=0; s<sol.getPathForDrivers().size(); s++){
                                               PathForDriver pathForDriver = sol.getPathForDrivers().get(s);
                                               for(int p=0;p<pathForDriver.getDriverPathWithInfos().size();p++){
                                                  if(pathForDriver.getDriverPathWithInfos().get(p).getIdOfTrip()==idCombineTrip&&pathForDriver.getDriverPathWithInfos().get(p).getDrivingStatus()==true){
                                                      int idOfLeadingVehicle=pathForDriver.getDriverPathWithInfos().get(p).getIdOfVehicle();//in combined trip driving driver using leading vehicle
                                                      writerSolution.println("combine "+idCombineTrip+" "+pathForDriver.getIdOfDriver()+" "+idOfLeadingVehicle);
                                                  }
                                               }
                                            }
                                        }
                                    }
                                }
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
                            }
                        }
                        writer.print(solverBasedOnDriverSchedule.getTimeInSec() + ",");
                        writer.println();
                        writer.flush();
                        writerSolution.println();
                        writerSolution.flush();

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (IloException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        writer.close();

    }
}
