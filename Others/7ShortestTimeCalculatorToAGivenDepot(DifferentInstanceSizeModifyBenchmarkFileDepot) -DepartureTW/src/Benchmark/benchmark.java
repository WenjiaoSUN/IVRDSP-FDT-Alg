package Benchmark;

//
//import Calculator.Calculator;

import Calculator.Calculator;
import Instance.Instance;
import Instance.InstanceReader;
import Instance.NewGraph;
import Solution.SolutionOfShortestTimeToDepot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class benchmark {
    private static String directory = "instances";
    private static String outputFileDirectory = "resultOfShortestTime";

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

        if (options.containsKey("-result")) {
            outputFileDirectory = options.get("-result");
        }
    }

    public static void solveFromFiles() {
        File f = new File(directory);
        System.out.println("Check " + (f.isDirectory()));

        if (f.isDirectory()) {
            File[] listOfFiles = f.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                File fileI = listOfFiles[i];
                String fileName = fileI.getName();
                System.out.println("instanceFileName " + fileName);
                String outputFileName = "shortestTimeToDepot_1_" + fileName;//第一处修改文件名(因为这个跟这个例子一共有几个depot有关,共有4处待修改)
                File outputFile = new File(outputFileDirectory, outputFileName);

                try (PrintWriter writer = new PrintWriter(outputFile)) {
                    if (fileName.startsWith("inst") && fileName.endsWith(".txt")) {
                        InstanceReader instanceReader = new InstanceReader(fileI.getPath());
                        Instance instance = instanceReader.readFile();
                        System.out.println(instance);
                        NewGraph newGraph = new NewGraph(instance,instance.getDepot(1));//第二处修改depot in new graph,
                        // then it will effect the calculator
                        Calculator calculator1 = new Calculator(newGraph);
                        SolutionOfShortestTimeToDepot solution1 = calculator1.solveWithDijkstra();
                        if (solution1 != null) {
                            writer.println("//instance " + instance.getNameOfInstance() + ";");
                            writer.println("//Each trip minTime to arrive idDepot:" + 1 + ", idTrip, shortestTime");//第三次修改文件内容中的depot id
                            for (int t = 0; t < instance.getNbTrips(); t++) {
                                writer.println("1 " + t + " " + (int) calculator1.getDistances()[t]);//第四次修改数据中的depot id
                            }
                        } else {
                            writer.print(0 + ",");
                        }
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

