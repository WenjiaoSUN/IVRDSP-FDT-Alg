package benchmark;

import Instance.Instance;
import Instance.InstanceReader;
import Solution.Schedules;
import Solution.SchedulesReader;

import java.io.*;
import java.util.HashMap;

public class TwoSchedulesCombiner {
    private static String directory = "instances";
    private static String initialFeasibleSchedulesDirectory = "initialFeasibleSchedules";//file1
    private static String resultOfAllPathsFromLabelingAlgorithmDirectory = "resultOfAllPathsFromLabelingAlgorithm";//file2
    private static String outputFileDirectory = "twoFilesCombinedSchedules";
    private static String nbPathsFileDirectory ="nbPathsFile.csv";
    private static PrintWriter writer;

    private static PrintWriter writerNbPaths;

    public static void main(String[] args) {
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
            initialFeasibleSchedulesDirectory = options.get("-initial");
        }

        if (options.containsKey("-result")) {
            resultOfAllPathsFromLabelingAlgorithmDirectory = options.get("-result");
        }

        if (options.containsKey("-two")) {
            outputFileDirectory = options.get("-two");
        }

        if(options.containsKey("-nb")){
            nbPathsFileDirectory = options.get("nb");
        }
    }

    public static void solveFromFiles() {
        try{
            writerNbPaths=new PrintWriter(nbPathsFileDirectory);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        writerNbPaths.println("name,minPlanTime,maxPlanTime,shortConTimeDriver,shortConTimeVehicle,maxDrivingTime,maxWorkingTime,"
                        +"nbCities,nbTrips,nbNodes,nbArcs,maxPercentCombine,maxNbDriverAvailable,nbPaths");
        writerNbPaths.flush();

        File f = new File(directory);
        if (f.isDirectory()) {
            File[] listOfFiles = f.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                File fileI = listOfFiles[i];
                String fileName = fileI.getName();
                System.out.println("instanceFileName" + fileName);

                String outputFileName =  fileName.replace("inst","feaSol");
                File outputFile = new File(outputFileDirectory, outputFileName);

                try {
                    writer = new PrintWriter(outputFile);
                    if (fileName.startsWith("inst") && fileName.endsWith(".txt")) {
                        InstanceReader instanceReader = new InstanceReader(fileI.getAbsolutePath());
                        Instance instance = instanceReader.readFile();
                        System.out.println(instance);

                        // Create a FileReader to read the contents of file1
                        String file1Name = fileName;
                        file1Name = file1Name.replace("inst", "feaSol");
                        File file1 = new File(initialFeasibleSchedulesDirectory, file1Name);


                        SchedulesReader fileReader1 = new SchedulesReader(file1.getAbsolutePath(), instance);
                        Schedules schedules1 = fileReader1.readFile();
                        System.out.println("check size1="+schedules1.getDriverSchedules().size());

                        String file2Name = fileName;
                        file2Name = "iniPathsLabelAlg_" + file2Name;
                        File file2= new File(resultOfAllPathsFromLabelingAlgorithmDirectory,file2Name);

                        SchedulesReader fileReader2 = new SchedulesReader(file2.getAbsolutePath(),instance);
                        Schedules schedules2= fileReader2.readFile();
                        System.out.println("check size2="+schedules2.getDriverSchedules().size());

                        int nbPaths=schedules1.getDriverSchedules().size()+schedules2.getDriverSchedules().size();
                        System.out.println("check total size="+nbPaths);

                        writerNbPaths.print(instance.getNameOfInstance() + ",");
                        writerNbPaths.print("15" + ",");
                        writerNbPaths.print(instance.getShortConnectionTimeForDriver() + ",");
                        writerNbPaths.print(instance.getShortConnectionTimeForVehicle() + ",");
                        writerNbPaths.print(instance.getMaxPlanTime() + ",");
                        writerNbPaths.print(instance.getMaxDrivingTime() + ",");
                        writerNbPaths.print(instance.getMaxWorkingTime() + ",");
                        writerNbPaths.print(instance.getNbCities() + ",");
                        writerNbPaths.print(instance.getNbTrips() + ",");
                        writerNbPaths.print(instance.getNbNodes() + ",");
                        writerNbPaths.print(instance.getMaxNbPossibleArc() + ",");
                        writerNbPaths.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                        writerNbPaths.print(instance.getMaxNbDriverAvailable() + ",");

                        if (instance != null && schedules1 != null&& schedules2 != null) {
                            writer.print(schedules1);
                            writer.println(schedules2);
                            writerNbPaths.print(nbPaths);
                        } else {
                            writer.print(0 + ",");
                            writer.print(0 + ",");
                            writerNbPaths.print(0 + ",");
                        }
                        writer.println();
                        writer.flush();
                        writerNbPaths.println();
                        writerNbPaths.flush();
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
