package benchmark;

import Generator.AllPathsGenerator;
import Generator.PathsGeneratorBasedOnGivenTrip;
import Instance.Instance;
import Instance.InstanceReader;
import Solution.Schedules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.PrintWriter;
import java.util.HashMap;


public class benchmark {
    private static String directory="instances";
    private static String outputFileDirectory="resultOfAllPathsFromLabelingAlgorithm";
    private static PrintWriter writer;

    //here is to record the time of generate paths
    private static String timeCostOfGeneratingDirectory ="timeCostOfGenerating.csv";
    private static PrintWriter timeCostWriter;

    public static void main(String[] args){
        read(args);
        solveFromFiles();
    }

    public static void read(String[] args) {
        HashMap<String,String> options=new HashMap<>();
        for(int i=0;i< args.length;i++){
            String arg=args[i];
            i++;
            String val=args[i];
            options.put(arg,val);
        }
        if(options.containsKey("-d")){
            directory=options.get("-d");
        }

        if(options.containsKey("-result")){
            outputFileDirectory=options.get("-result");
        }

        if(options.containsKey("-time")){
            timeCostOfGeneratingDirectory=options.get("-time");
        }
    }

    public static void solveFromFiles(){
        try {
            timeCostWriter=new PrintWriter(timeCostOfGeneratingDirectory);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        timeCostWriter.println("name,minPlanTime,maxPlanTime,shortConTimeDriver,shortConTimeVehicle,maxDrivingTime,maxWorkingTime,"
                        +"nbCities,nbTrips,nbNodes,nbArcs,maxPercentCombine,maxNbDriverAvailable,time(sec)");
        timeCostWriter.flush();


        File f= new File(directory);
        if(f.isDirectory()){
            File[] listOfFiles=f.listFiles();
            for(int i=0;i< listOfFiles.length;i++){
                File fileI=listOfFiles[i];
                String fileName=fileI.getName();
                System.out.println("instanceFileName"+fileName);
                String outputFileName= "iniPathsLabelAlg_"+fileName;
                File outputFile= new File(outputFileDirectory, outputFileName);
                try{
                    writer= new PrintWriter(outputFile);
                    if(fileName.startsWith("inst")&&fileName.endsWith(".txt")){
                        InstanceReader instanceReader= new InstanceReader(fileI.getAbsolutePath());
                        Instance instance=instanceReader.readFile();
                        System.out.println(instance);

                        AllPathsGenerator allPathsGenerator= new AllPathsGenerator(instance);
                        int nbMaxPathsGenerateForEachTrip=10;
                        int maxNbTripInSchedule=5;
                        Schedules schedules=allPathsGenerator.generateSchedules(nbMaxPathsGenerateForEachTrip,maxNbTripInSchedule);
                        schedules.printDriverSchedulesSolutionInFile(outputFileName);
                        System.out.println(schedules);
                        timeCostWriter.print(instance.getNameOfInstance() + ",");
                        timeCostWriter.print("15" + ",");
                        timeCostWriter.print(instance.getShortConnectionTimeForDriver() + ",");
                        timeCostWriter.print(instance.getShortConnectionTimeForVehicle() + ",");
                        timeCostWriter.print(instance.getMaxPlanTime() + ",");
                        timeCostWriter.print(instance.getMaxDrivingTime() + ",");
                        timeCostWriter.print(instance.getMaxWorkingTime() + ",");
                        timeCostWriter.print(instance.getNbCities() + ",");
                        timeCostWriter.print(instance.getNbTrips() + ",");
                        timeCostWriter.print(instance.getNbNodes() + ",");
                        timeCostWriter.print(instance.getMaxNbPossibleArc() + ",");
                        timeCostWriter.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                        timeCostWriter.print(instance.getMaxNbDriverAvailable() + ",");


                        //writer.println("//instance "+instance.getNameOfInstance()+";");
                        if(schedules!=null){
                            writer.println(schedules);
                            timeCostWriter.print(allPathsGenerator.getGenerateTime());
                        }
                        else {
                            writer.print(0+",");
                            timeCostWriter.print(0+",");
                        }
                        writer.println();
                        writer.flush();
                        timeCostWriter.println();
                        timeCostWriter.flush();
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        writer.close();

    }
}
