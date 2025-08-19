package Benchmark;

import ColumnGe.MasterProblem;
import Instance.Instance;
import Instance.InstanceReader;
import Solution.SchedulesReader;
import Solution.Solution;
import ilog.concert.IloException;

import java.io.*;
import java.util.HashMap;

public class BenchmarkForDriverScheduleCG {
    private static String directory = "instances";
    private static String initialColumnDirectory = "initialSchedules";
    private static String outputFileDirectory = "resultsOfSchedulesFromCG";

    private static PrintWriter writer;// use for loop and local writer

    private static String outputExcelDirectory = "outPutFileCG.csv";

    private static PrintWriter writerResultInformation;


    public static void main(String[] args) throws IloException, IOException {
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

    public static void solveFromFiles() throws IloException, IOException {
        File f = new File(directory);
        writerResultInformation = new PrintWriter(outputExcelDirectory);
//        FileOutputStream fos = new FileOutputStream(outputExcelDirectory);
//        fos.write(0xEF);
//        fos.write(0xBB);
//        fos.write(0xBF); // 写入 UTF-8 BOM，防乱码
//        writerResultInformation = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));

        writerResultInformation.write("sep=,\n");//我的中文电脑默认分号分隔，但是国际及英文系统的是逗号分隔的，为了保证我电脑上跟学校电脑都能查看csv添加了这样一样强制我电脑按着逗号分隔写csv的话，这样就可以继续使用原来的instance了
        writerResultInformation.println("name,minPlanTime(min),maxPlanTime(min),shortConTimeDriver(min),shortConTimeVehicle(min),maxDrivingTime(min),maxWorkingTime(min),"
                + "nbCities,nbTrips,nbTripStartFromDepot,nbNodes,nbArcs,maxPercentCombine,timeWindowRange(min),maxNbDriverAvailable," +
                "initialNbColumns,nbColumnsGeneratedByIterations,nbColumnsGeneratedByPostProcess,finalNbColumnsInModel,nbIteration," +
                "initialMasterObjectiveValue,finalMasterObjectiveValue,finalLowerBoundMP,finalIntegerObjectiveValue,gap(%),elapsedTime(sec)," +
                "timeOfSolvingSubPByLabelingAlgorithm(milli sec),timeOfSolRMPByCplex(milli sec),timeOfSolveFinalIntegerProblemByCplex(sec)," +
                "timePostProcessForRichSchedules(sec),"+"timeForCreatingModelInCG(sec),"+
                "nbLabelsInTotal,nbLabelsFinalIteration,nbLabelsPerIteration");//timeOfTotalDominance(milli sec),timeOfTotalDominance(milli sec),,timeOfSolvingSubPByLabelingAlgorithm(milli sec)
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
                System.out.println("initial column name"+initialColumnName);
                String initialColumnPath = initialColumnDirectory + "\\" + initialColumnName;
                File initialColumnPathFile = new File(initialColumnPath, initialColumnName);
                System.out.println(initialColumnPath);
                System.out.println(initialColumnName);

                try {
                    writer = new PrintWriter(outputFile);
                    if (fileName.startsWith("inst") && fileName.endsWith(".txt")) {
                        InstanceReader instanceReader = new InstanceReader(fileI.getAbsolutePath());
                        Instance instance = instanceReader.readFile();
                        writerResultInformation.print(instance.getNameOfInstance() + ",");
                        writerResultInformation.print("15" + ",");
                        writerResultInformation.print(instance.getShortConnectionTimeForDriver() + ",");
                        writerResultInformation.print(instance.getShortConnectionTimeForVehicle() + ",");
                        writerResultInformation.print(instance.getMaxPlanTime() + ",");
                        writerResultInformation.print(instance.getMaxDrivingTime() + ",");
                        writerResultInformation.print(instance.getMaxWorkingTime() + ",");
                        writerResultInformation.print(instance.getNbCities() + ",");
                        writerResultInformation.print(instance.getNbTrips() + ",");
                        writerResultInformation.print(instance.getNbTripCouldStartFromDepot()+",");
                        writerResultInformation.print(instance.getNbNodes() + ",");
                        writerResultInformation.print(instance.getMaxNbPossibleArc() + ",");
                        writerResultInformation.print(instance.getMaxPercentageOfCombinedTrip() + ",");
                        writerResultInformation.print(instance.getTimeWindowRange()+",");
                        writerResultInformation.print(instance.getMaxNbDriverAvailable() + ",");

                        SchedulesReader schedulesReader = new SchedulesReader(initialColumnPath, instance);
                        //in this schedulesReader instead of using initialColumName, I use the absolute Path, then it can find the folderContent
                        Solution initialSchedules = schedulesReader.readFile();

                        //MasterProblem masterProblemSolver = new MasterProblem(instance, initialSchedules);
                        MasterProblem  masterProblemSolver = new MasterProblem(instance, initialSchedules, true, false, false,false, 20);
                        Solution solution1 = masterProblemSolver.solve();

                        writer.println("//instance " + i + " " + instance.getNameOfInstance() + ",");
                        writer.print("//nbNodes " + instance.getNbNodes() + ",");
                        writer.print(" nbArcs: " + instance.getMaxNbPossibleArc() + ",");
                        writer.print(" maxPerCombined: " + instance.getMaxPercentageOfCombinedTrip() + ",");
                        writer.print(" timeWindow"+ instance.getTimeWindowRange()+",");
                        writer.print(" nbDriverAvailable: " + instance.getMaxNbDriverAvailable() + ",");
                        writer.print(" solution gap: " + masterProblemSolver.getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() + ", \n");

                        if (solution1 != null) {
                            writer.println("//The final integer value is: " + masterProblemSolver.getFinalIntegerValueOfMasterObjective());
                            writer.println("//The elapsed time is: " + masterProblemSolver.getFinalTimeCostInSec() + " sec");
                            writer.println("//Total time in milli second of cplex solving pricing problem: " +masterProblemSolver.getTotalTimeCostForSolvingSubProblemByCplex() + " milli sec");
//                            writer.println("//Total time in milli second of labeling algorithm solving pricing problem: " +masterProblemSolver.getTotalTimeCostForSolvingSubProblemByLabeling()+ " milli sec");
                            writer.println(solution1);
                            writerResultInformation.print(initialSchedules.getDriverSchedules().size() + ",");
                            //System.out.println("Total columns added: " + (masterProblemSolver.getFinalColumns() - initialSchedules.getDriverSchedules().size()));
                            int nbColumnsGeneratedByIterations=masterProblemSolver.getFinalColumns()-masterProblemSolver.getNbColumnsGeneratedByPostProcess()-initialSchedules.getDriverSchedules().size();
                            writerResultInformation.print(nbColumnsGeneratedByIterations+",");
                            writerResultInformation.print(masterProblemSolver.getNbColumnsGeneratedByPostProcess()+",");
                            writerResultInformation.print(masterProblemSolver.getFinalColumns() + ",");

                            writerResultInformation.print(masterProblemSolver.getFinalNbIterations() + ",");
                            writerResultInformation.print(masterProblemSolver.getInitialRMPValue() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalMasterObjectiveValue() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalLowerBoundOfMP() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalIntegerValueOfMasterObjective() + ",");
                            writerResultInformation.print(masterProblemSolver.getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() + ",");
                            writerResultInformation.print(masterProblemSolver.getFinalTimeCostInSec() + ",");
                            writerResultInformation.print(masterProblemSolver.getTotalTimeCostForSolvingSubProblemByLabeling()+",");
                            writerResultInformation.print(masterProblemSolver.getTotalTimeOfSolRMPByCplex()+",");// add 2025.4.3
                            writerResultInformation.print(masterProblemSolver.getTimeForSolvingLastIntegerProblemInSec()+",");
                            writerResultInformation.print(masterProblemSolver.getTimeForPostProcessOfRichScheduleByDepartureTimeInSec()+",");
                            writerResultInformation.print(masterProblemSolver.getTimeForCreateModelInSec()+",");
                            // writerResultInformation.print(masterProblemSolver.getTotalTimeCostForSolvingSubProblemByCplex()+ ";");
                            writerResultInformation.print(masterProblemSolver.getTotalLabelsGenerateForAllIterations()+",");//4.3
                            writerResultInformation.print(masterProblemSolver.getFinalNbLabelsLastIteration()+",");//4/4
                            writerResultInformation.print(masterProblemSolver.getAverageLabelsByIteration()+",");//4.3



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
                            writer.print(0 + ";");
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
       // writer.close();
        if (writer != null) {
            writer.close();
        }
    }

}
