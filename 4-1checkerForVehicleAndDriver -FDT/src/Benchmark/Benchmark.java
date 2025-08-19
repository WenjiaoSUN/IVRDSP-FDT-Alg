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
    private static String outputFile="checkResult.csv";
    private static PrintWriter writer;
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
        if (options.containsKey("-d")) {
            directoryOfInstances = options.get("-d");
        }

        if(options.containsKey("-solutions")){
            solutionsDirectory =options.get("-solutions");
        }
        if(options.containsKey("-outF")){
            outputFile=options.get("-outF");
        }
    }

    public static void checkFromFiles() throws IOException{
        File f = new File(directoryOfInstances);
        writer= new PrintWriter(outputFile);
        writer.println("name,wheSolFeasible,costInfile,costCalculate,wheCostSame");
        writer.flush();
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
                    SolutionReader solutionReader= new SolutionReader(solutionFile.getAbsolutePath(),instance);
                    Solution solution= solutionReader.readFile();
                    if(solution!=null){
                    writer.print(instance.getNameOfInstance()+",");
                    writer.print(solution.isSolutionFeasible(true)+",");
                    writer.print(solution.getTotalCostInput()+",");
                    writer.print(solution.getTotalCost()+",");
                    writer.print(solution.isCostSame()+",");
                    }
                    else {
                        writer.print("Error"+",");
                        writer.print("Error"+",");
                        writer.print("Error"+",");
                        writer.print("Error"+",");
                        writer.print("Error"+",");

                    }
                    writer.println();
                    writer.flush();

                }
            }
        }
        writer.close();
    }
}
