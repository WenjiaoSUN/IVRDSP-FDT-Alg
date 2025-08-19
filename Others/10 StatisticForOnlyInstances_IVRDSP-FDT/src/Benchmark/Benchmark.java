package Benchmark;
import  Instance.Instance;
import Instance.InstanceReader;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;


public class Benchmark {
    private static String directoryOfInstances="instances";

    private static String outputFileForInstance ="statisticsResultForInstance.csv";


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



        if(options.containsKey("-statisticsResultForInstance")){
            outputFileForInstance =options.get("-statisticsResultForInstance");
        }
    }

    public static void checkFromFiles() throws IOException{
        File f = new File(directoryOfInstances);

        writerForStatisticsInstance = new PrintWriter(outputFileForInstance);
        writerForStatisticsInstance.println("name,nbTrips,nbCombineTrips,percentageOfCombine(%),nbCombineTripsDepotToDepot,nbCombineTripsDepotToNonDepot,nbCombineTripsNonDepotToDepot,nbCombineTripsNonDepotToNonDepot," +
                "nbNormalTrips,nbNormalTripsDepotToDepot,nbNormalTripDepotToNonDepot,nbNormalTripsNonDepotToDepot,nbNormalTripsNonDepotToNonDepot,lengthOfDepartureTimeFlexibility");
        if(f.isDirectory()){
            File[] listOfFiles= f.listFiles();

            for(int i=0;i<listOfFiles.length;i++){
                File fileI= listOfFiles[i];
                String fileName= fileI.getName();

                if(fileName.startsWith("inst_")&&fileName.endsWith(".txt")){
                    InstanceReader instanceReader= new InstanceReader(fileI.getAbsolutePath());
                    Instance instance= instanceReader.readFile();

                    writerForStatisticsInstance.print(instance.getNameOfInstance() + ",");
                    writerForStatisticsInstance.print(instance.getNbTrips()+",");
                    writerForStatisticsInstance.print(instance.getNbCombinedTrips() +",");
                    writerForStatisticsInstance.print(instance.getActualPercentageOfCombine()+",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromDepotAndEndAtDepot() +",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromDepotAndEndAtNonDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromNonDepotEndAtDepot() +",");
                    writerForStatisticsInstance.print(instance.getNbCombineTripStartFromNonDepotEndAtNonDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTrip()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromDepotEndAtDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromDepotEndAtNonDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromNonDepotEndAtDepot()+",");
                    writerForStatisticsInstance.print(instance.getNbNormalTripStartFromNonDepotEndAtNonDepot()+",");
                    // new for departure time flexibility
                    writerForStatisticsInstance.print(instance.getDepartureTimeLength()+",");
                    writerForStatisticsInstance.println();
                    writerForStatisticsInstance.flush();


                }
            }
        }
        writerForStatisticsInstance.close();

    }
}
