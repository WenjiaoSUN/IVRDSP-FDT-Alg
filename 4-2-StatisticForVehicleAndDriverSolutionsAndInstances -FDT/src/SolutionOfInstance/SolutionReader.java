package SolutionOfInstance;

import Instance.Instance;
import Instance.InstanceReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SolutionReader {

    private String fileName;
    private Instance instance;

    public SolutionReader(String fileName,  Instance instance){
        this.fileName=fileName;
        this.instance=instance;
    }

    public Solution readFile(){
        Solution solution=null;
        double totalCostInput=Double.MAX_VALUE;
        try{
            FileReader reader = new FileReader(fileName);
            BufferedReader bufferedReader= new BufferedReader(reader);
            String line=bufferedReader.readLine();

            while (line.startsWith("//") || line.isEmpty()) {
                line = bufferedReader.readLine();
            }
            int idVehicle=0;

            while(line!=null&&line.startsWith("cost")){
                String[] values;
                values= line.split(" ");
                totalCostInput =Double.valueOf(values[1]);
//                System.out.println("now we aleady read the cost"+ totalCostInput);
                line = bufferedReader.readLine();
            }
            solution=new Solution(instance,totalCostInput);



            readVehiclePath(bufferedReader,solution);
            readDriverPath(bufferedReader,solution);

            if(instance.getMaxPercentageOfCombinedTrip()>0.001){
                readCombineList(bufferedReader,solution);
            }


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return solution;
    }

    public void readVehiclePath(BufferedReader bufferedReader,Solution solution) throws IOException {
        String line= bufferedReader.readLine();
        while(line.startsWith("//")||line.isEmpty()){
            line=bufferedReader.readLine();
        }
        while (line!=null&& line.startsWith("vehicle")){
            String[] values;
            values=line.split(" ");
            int idOfVehicle=Integer.valueOf(values[1]);
            PathForVehicle pathForVehicle= new PathForVehicle(instance,idOfVehicle);

            for(int i=3;i< values.length-1;i=i+2){
                int idTrip=instance.getTrip(Integer.valueOf(values[i])).getIdOfTrip();
                int depTime=Integer.valueOf(values[i+1]);

                TripWithDepartureTime tripWithDepartureTime = new TripWithDepartureTime(instance.getTrip(idTrip),depTime);
                pathForVehicle.addTripInVehiclePath(tripWithDepartureTime);
            }
            solution.addPathInSetForVehicle(pathForVehicle);// waiting for check
            line= bufferedReader.readLine();

        }

    }


    public void readDriverPath(BufferedReader bufferedReader,Solution solution) throws IOException {
        String line= bufferedReader.readLine();
        while(line.startsWith("//")||line.isEmpty()){
            line=bufferedReader.readLine();
        }
        while (line!=null&& line.startsWith("driver")){
            String[] values;
            values=line.split(" ");
            int idOfDriver=Integer.valueOf(values[1]);
            int nbTripPerformedByVehicle=Integer.valueOf(values[2]);
            PathForDriver pathForDriver= new PathForDriver(instance,idOfDriver);

            for(int i=3;i< values.length-1;i=i+4){
                int idTrip=instance.getTrip(Integer.valueOf(values[i])).getIdOfTrip();
                int idVehicle=Integer.valueOf(values[i+1]);
                boolean whetherDrive= Boolean.parseBoolean(values[i+2]);
                int depTime=Integer.valueOf(values[i+3]);

                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatusAndDepartureTimeAndVehicle = new TripWithWorkingStatusAndDepartureTimeAndVehicle(instance.getTrip(idTrip),idVehicle,whetherDrive,depTime);
                pathForDriver.addTripInDriverPath(tripWithWorkingStatusAndDepartureTimeAndVehicle);
            }
            solution.addPathInSetForDriver(pathForDriver);// waiting for check
            line= bufferedReader.readLine();

        }

    }

    public void readCombineList(BufferedReader bufferedReader,Solution solution) throws IOException{
        String line= bufferedReader.readLine();
        while(line.startsWith("//")||line.isEmpty()){
            line=bufferedReader.readLine();
        }
        while (line!=null&& line.startsWith("combine")){

            String[] values;
            values=line.split(" ");
            int idOfTrip=Integer.valueOf(values[1]);
            int idOfDrivingDriver=Integer.valueOf(values[2]);
            int idOfLeadingVehicle=Integer.valueOf(values[3]);
            CombineTripInform combineTripInform = new CombineTripInform(instance,idOfTrip,idOfDrivingDriver,idOfLeadingVehicle);
            solution.addCombineTripInform(combineTripInform);
            line= bufferedReader.readLine();
        }

    }

    public static void main(String[] args) {

        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW1.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println("instance: "+instance);

        SolutionReader solutionReader= new SolutionReader("solution_inst_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW1.txt",instance);
        Solution solution=solutionReader.readFile();
        System.out.println(solution);
    }
}