package Benchmark;
import  Instance.Instance;
import Instance.InstanceReader;
import  SolutionOfInstance.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;


public class Benchmark {
    private static String directoryOfInstances="instances";
    private static String solutionsDirectory ="solutions";
    private static String outputFileForSolution ="statisticsResultForSolution.csv";
    private static String outputFileForInstance ="statisticsResultForInstance.csv";
    private static PrintWriter writerForStatisticsSolution;

    private static PrintWriter writerForStatisticsInstance;


    public static void main (String[] args) throws IOException{
        read(args);
        checkFromFiles();
    }

    public static void read(String[] args){
        HashMap<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            i++;
            String val = args[i];
            options.put(arg, val);
        }
        if (options.containsKey("-instances")) {
            directoryOfInstances = options.get("-instances");
        }

        if(options.containsKey("-solutions")){
            solutionsDirectory =options.get("-solutions");
        }
        if(options.containsKey("-statisticsResultForSolution")){
            outputFileForSolution =options.get("-statisticsResultForSolution");
        }

        if(options.containsKey("-statisticsResultForInstance")){
            outputFileForInstance =options.get("-statisticsResultForInstance");
        }
        if(options.containsKey("-statisticsResultForInstance")){
            outputFileForInstance =options.get("-statisticsResultForInstance");
        }

    }

    public static void checkFromFiles() throws IOException{
        File f = new File(directoryOfInstances);


        writerForStatisticsSolution = new PrintWriter(outputFileForSolution);
        writerForStatisticsSolution.println("name,nbTasks,maxPercentCombine,nbDrivers(Vehicles)Available,lengthOfDepartureTimeFlexibility," +
                "nbDriversInSolution,nbVehiclesInSolution," +
                "nbTripsHavePassenger,nbTripsHavePassenger(NormalTrip),nbTripsHavePassenger(CombineTrip)," +
                "nbTripsPerformedPerDriver," +
                "totalWorkTimePerDriver(minute),totalDriveTimePerDriver(minute),totalPassengerTimePerDriver(minute),totalIdleTimePerDriver(minute)," +
                "shortestIdleTime(minute),longestIdleTime(minute)," + "shortestWorkingTime(minute),longestWorkingTime(minute),"+
                "nbChangeoversPerDriver,nbChangeoverPerDriver_ChangeStatus,nbChangeoverPerDriver_ChangeVehicle,");
        writerForStatisticsSolution.flush();


        writerForStatisticsInstance = new PrintWriter(outputFileForInstance);
        writerForStatisticsInstance.println("name,nbCombineTrips,nbCombineTripsDepotToDepot,nbCombineTripsDepotToNonDepot,nbCombineTripsNonDepotToDepot,nbCombineTripsNonDepotToNonDepot," +
                "nbNormalTrips,nbNormalTripsDepotToDepot,nbNormalTripDepotToNonDepot,nbNormalTripsNonDepotToDepot,nbNormalTripsNonDepotToNonDepot,lengthOfDepartureTimeFlexibility");
        if(f.isDirectory()){
            File[] listOfFiles= f.listFiles();

            for(int i=0;i<listOfFiles.length;i++){
                File fileI= listOfFiles[i];
                String fileName= fileI.getName();
                String solutionFileName="solution_"+fileName;
                String solutionFilePath= solutionsDirectory;
                File solutionFile=new File(solutionFilePath,solutionFileName);
                System.out.println(solutionFileName);
                if(fileName.startsWith("inst_")&&fileName.endsWith(".txt")){
                    InstanceReader instanceReader= new InstanceReader(fileI.getAbsolutePath());
                    Instance instance= instanceReader.readFile();
                    writerForStatisticsInstance.print(instance.getNameOfInstance() + ",");
                    writerForStatisticsInstance.print(instance.getNbCombinedTrips() +",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromDepotAndEndAtDepot() +",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromDepotAndEndAtNonDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromNonDepotEndAtDepot() +",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromNonDepotEndAtNonDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTrip()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromDepotEndAtDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromDepotEndAtNonDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromNonDepotEndAtDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromNonDepotEndAtNonDepot()+",");
                    writerForStatisticsInstance.print(instance.getDepartureTimeLength()+",");
                    writerForStatisticsInstance.println();
                    writerForStatisticsInstance.flush();

                    SolutionReader solutionReader= new SolutionReader(solutionFile.getAbsolutePath(),instance);
                    Solution solution= solutionReader.readFile();
                    if(solution!=null){
                        writerForStatisticsSolution.print(instance.getNameOfInstance() + ",");
                        writerForStatisticsSolution.print(instance.getNbTrips() + ",");
                        writerForStatisticsSolution.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                        writerForStatisticsSolution.print(instance.getMaxNbDriverAvailable() + ",");
                        writerForStatisticsSolution.print(instance.getDepartureTimeLength()+",");

                        writerForStatisticsSolution.print(solution.getPathForDrivers().size()+",");
                        writerForStatisticsSolution.print(solution.getPathForVehicles().size()+",");

                        writerForStatisticsSolution.print(solution.getNbTripsWithPassenger()+",");
                        writerForStatisticsSolution.print(solution.getNbTripsWithPassenger_Normal()+",");
                        writerForStatisticsSolution.print(solution.getNbTripsWithPassenger_Combine()+",");
                        writerForStatisticsSolution.print(solution.getNbTripsPerDriver()+",");

                        writerForStatisticsSolution.print(solution.getWorkingTimePerDriver()+",");
                        writerForStatisticsSolution.print(solution.getDrivingTimePerDriver()+",");
                        writerForStatisticsSolution.print(solution.getPassengerTimePerDriver()+",");
                        writerForStatisticsSolution.print(solution.getIdleTimePerDriver()+",");
                        writerForStatisticsSolution.print(solution.getShortestIdleTime()+",");
                        writerForStatisticsSolution.print(solution.getLongestIdleTime()+",");
                        writerForStatisticsSolution.print(solution.getShortestWorkingTime()+",");
                        writerForStatisticsSolution.print(solution.getLongestWorkingTime()+",");
                        writerForStatisticsSolution.print(solution.getNbChangeOversPerDriver()+",");
                        writerForStatisticsSolution.print(solution.getNbChangeOversPerDriver_ChangeStatus()+",");
                        writerForStatisticsSolution.print(solution.getNbChangeOversPerDriver_ChangeVehicle()+",");

                    }
                    else {
                        writerForStatisticsSolution.print("Error"+",");
                        writerForStatisticsSolution.print("Error"+",");
                        writerForStatisticsSolution.print("Error"+",");
                        writerForStatisticsSolution.print("Error"+",");
                        writerForStatisticsSolution.print("Error"+",");
                        writerForStatisticsSolution.print("Error"+",");

                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");

                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                        writerForStatisticsSolution.print(0+",");
                    }
                    writerForStatisticsSolution.println();
                    writerForStatisticsSolution.flush();

                }
            }
        }
        writerForStatisticsInstance.close();
        writerForStatisticsSolution.close();

    }
}
