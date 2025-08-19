package InputSchedulesAndRoutesInforms;

import CombineInforms.CombineTripInform;
import Instance.Instance;
import Solution.TripWithStartingInfos;
import ilog.concert.IloException;
import Instance.TripWithWorkingStatusAndDepartureTime;
import Instance.InstanceReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class InputInformsReader {
    private String fileName;
    private Instance instance;

    public InputInformsReader(String fileName, Instance instance) throws FileNotFoundException, IloException {
        this.fileName = fileName;
        this.instance = instance;
    }

    public InputInforms readFile() {
        try {
            InputInforms inputInforms = new InputInforms(instance);
            FileReader reader = new FileReader(fileName);
            BufferedReader buffer = new BufferedReader(reader);
            String line = buffer.readLine();
            while (line.startsWith("//") || line.isEmpty()) {
                line = buffer.readLine();
            }
            int idVehicle=0;
            while (line != null && line.startsWith("vehicle")) {
                String[] values;
                values = line.split(" ");
                VehicleRoute vehicleRoute = new VehicleRoute(instance);
                vehicleRoute.setIdOfStartDepot(Integer.valueOf(values[3]));
                vehicleRoute.setIdOfEndDepot(Integer.valueOf(values[values.length - 1]));
                for (int i = 4; i < values.length - 1; i = i + 2) {
                    int idOfTrip = Integer.valueOf(values[i]);
                    int depTime = Integer.valueOf(values[i + 1]);
                    TripWithStartingInfos tripWithStartingInfos = new TripWithStartingInfos(instance.getTrip(idOfTrip), depTime);
                    vehicleRoute.addTripWithStartInforms(tripWithStartingInfos);
                }
                vehicleRoute.setIdVehicle(idVehicle);
                vehicleRoute.setIndexDepotAsStartingPoint();
                vehicleRoute.setIndexDepotAsEndingPoint();
                inputInforms.addVehicleRouteInput(vehicleRoute);
                idVehicle++;
                //System.out.println("check vehicle route"+vehicleRoute);
                line= buffer.readLine();
            }
            System.out.println("unitil now input"+inputInforms);
            if (line!=null&&line.startsWith("//")){
                line= buffer.readLine();
            }

            int idDriver=0;

            while (line != null && line.startsWith("driver")) {
                String[] values;
                values = line.split(" ");
                DriverSchedule driverSchedule = new DriverSchedule(instance);
                driverSchedule.setIdOfDriver(idDriver);
                driverSchedule.setIdOfDepot(Integer.valueOf(values[3]));
                driverSchedule.setIndexDepotAsStartingPoint();;
                driverSchedule.setIndexDepotAsEndingPoint();
                for (int j = 4; j < values.length; j = j + 4) {
                    int idTrip = Integer.valueOf(values[j]);
                    int idVehiclePerform = Integer.valueOf(values[j + 1]);
                    boolean drivingStatus = Boolean.parseBoolean(values[j + 2]);
                    int startTime = Integer.valueOf(values[j + 3]);

                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(idTrip), drivingStatus, startTime);
                    driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTime);

                }
                idDriver++;
                inputInforms.addDriverScheduleInput(driverSchedule);
                line= buffer.readLine();
                System.out.println("check driver "+driverSchedule);
            }

            if (line!=null&&line.startsWith("//")){
                line= buffer.readLine();
            }

            while (line != null && line.startsWith("combine")) {
                String[] values;
                values = line.split(" ");
                CombineTripInform combineTripInform = new CombineTripInform(instance);
                int idTrip = Integer.valueOf(values[1]);
                int idDriverInCombine= Integer.valueOf(values[2]);
                int idVehicleLeading= Integer.valueOf(values[3]);
                combineTripInform.setIdTrip(idTrip);
                combineTripInform.setIdDrivingDriver(idDriverInCombine);
                combineTripInform.setIdLeadingVehicle(idVehicleLeading);
                inputInforms.addCombinedInput(combineTripInform);
                System.out.println("check combine"+ combineTripInform);
                line= buffer.readLine();
            }
            buffer.close();
            reader.close();
            return inputInforms;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) throws FileNotFoundException, IloException {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips035_combPer0.25_TW6.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file

        InputInformsReader inputInformsReader= new InputInformsReader("FirstSolutionGivenByTwoIndex_inst_nbCity03_Size90_Day1_nbTrips030_combPer0.25_TW6.txt",instance);
        InputInforms inputInforms=inputInformsReader.readFile();
        System.out.println(inputInforms);

    }
}
