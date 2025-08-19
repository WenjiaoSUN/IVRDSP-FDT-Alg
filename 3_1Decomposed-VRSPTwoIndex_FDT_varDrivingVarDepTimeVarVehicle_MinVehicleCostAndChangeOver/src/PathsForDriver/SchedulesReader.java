package PathsForDriver;

import Instance.Instance;
import Instance.Trip;
import Instance.TripWithWorkingStatusAndDepartureTime;
import Instance.InstanceReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SchedulesReader {
    private String fileName;
    private Instance instance;

    public SchedulesReader(String fileName,Instance instance) throws FileNotFoundException, IOException {
        this.fileName=fileName;
        this.instance=instance;
    }
    public Schedules readFile(){
        try{
            Schedules driverSchedules= new Schedules(instance);
            FileReader reader= new FileReader(fileName);
            BufferedReader buffer = new BufferedReader(reader);
            //read the file
            String line = buffer.readLine();//read the first line
            while (line.startsWith("//")||line.isEmpty()){
                line= buffer.readLine();
            }
            while(line!=null&&line.startsWith("driver")){
                String[] values;
                values=line.split(" ");
                int index= 1;
                DriverSchedule driverSchedule = new DriverSchedule(instance);
//                driverSchedule.setIdOfSchedule(Integer.valueOf(values[1]));
                driverSchedule.setIdOfDepot(Integer.valueOf(values[2]));
                driverSchedule.setIndexDepotAsStartingPoint(Integer.valueOf(values[3]));
                driverSchedule.setIndexDepotAsEndingPoint(Integer.valueOf(values[values.length-1]));
                int i=0;
                for(int j= 4;j<values.length-1;j=j+3){
                    int idOfTrip= Integer.valueOf(values[j]);
                    Trip trip =instance.getTrip(idOfTrip);
                    boolean whetherOperating = Boolean.parseBoolean(String.valueOf(values[j+1]));
                    int departureTime=Integer.valueOf(values[j+2]);
                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus = new TripWithWorkingStatusAndDepartureTime(trip,whetherOperating,departureTime);
                    driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus);
                    //driverSchedule.setTripWithWorkingStatusAndDepartureTimeArrayList(i,Integer.valueOf(values[j]));
                    i++;
                }
                driverSchedules.addSchedule(driverSchedule);
                line= buffer.readLine();

            }
            /*
             *
             * here is because we combined two files, so I need to read the second part of schedules
             *
             */

            if (line!=null&&line.startsWith("//")){
                line= buffer.readLine();
            }
            while(line!=null&&line.startsWith("driver")){
                String[] values;
                values=line.split(" ");
                int index= 1;
                DriverSchedule driverSchedule = new DriverSchedule(instance);
//                driverSchedule.setIdOfSchedule(Integer.valueOf(values[1]));
                driverSchedule.setIdOfDepot(Integer.valueOf(values[2]));
                driverSchedule.setIndexDepotAsStartingPoint(Integer.valueOf(values[3]));
                driverSchedule.setIndexDepotAsEndingPoint(Integer.valueOf(values[values.length-1]));
                int i=0;
                for(int j= 4;j<values.length-1;j=j+3){
                    int idOfTrip= Integer.valueOf(values[j]);
                    Trip trip =instance.getTrip(idOfTrip);
                    boolean whetherOperating = Boolean.parseBoolean(String.valueOf(values[j+1]));
                    int departureTime=Integer.valueOf(values[j+2]);
                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(trip,whetherOperating,departureTime);
                    driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTime);
                    //driverSchedule.setTripWithWorkingStatusAndDepartureTimeArrayList(i,Integer.valueOf(values[j]));
                    i++;
                }
                driverSchedules.addSchedule(driverSchedule);
                line= buffer.readLine();

            }
            //according to the line we build ]
            buffer.close();
            reader.close();
            return driverSchedules;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        //Instance instance1= new Instance(instance);
        SchedulesReader schedulesReader = new SchedulesReader("scheduleSolution_inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt",instance);
        Schedules initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);

    }

}
