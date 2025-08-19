package Instance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

// here should add the cost for the aircraft and crews

public class InstanceReader {
    private String fileName;

    public InstanceReader(String fileName) {
        this.fileName = fileName;
    }

    public Instance readFile() {
        //readFile() and build a Instance记得把这个void改成Instance
        try {
            FileReader reader = new FileReader(fileName);//create an object FileReader with the name of the file to read as parameter
            BufferedReader buffer = new BufferedReader(reader);//create a buffered reader with the file reader

            //read the file
            String nameOfInstance= readNextString(buffer);
            int nbCities=readNextInteger(buffer);
            int nbDepots = readNextInteger(buffer);
            int nbVehicle = readNextInteger(buffer);
            int nbDriver = readNextInteger(buffer);
            int nbTrips = readNextInteger(buffer);
            int miniPlanTurnTime = readNextInteger(buffer);
            int shortConnectionTimeForDriver = readNextInteger(buffer);
            int  shortConnectionTimeForVehicle =readNextInteger(buffer);
            int maxDrivingTime =readNextInteger(buffer);
            int maxWorkingTime = readNextInteger(buffer);
            int costForUsingDriver= readNextInteger(buffer);
            int costForUsingVehicle= readNextInteger(buffer);
            int costForDriverIdleTimePerUnit=readNextInteger(buffer);
            int costForVehicleIdleTimePerUnit=readNextInteger(buffer);
            int costForChangeOverCostPerChange= readNextInteger(buffer);
            double maxPercentageOfCombineTrip =readNextDouble(buffer);
            int maxFolderOfTimeSlotAsTimeWindow=readNextInteger(buffer);
            int maxWaitingTime= readNextInteger(buffer);
            int timeUnit=readNextInteger(buffer);
            double scale_DisAndDur=readNextDouble(buffer);
            int startingTimePlanning=readNextInteger(buffer);
            int endingTimePlanning=readNextInteger(buffer);

            //According to the first line we can build a new instance
            Instance instance = new Instance(nameOfInstance,nbCities,nbDepots, nbVehicle, nbDriver, nbTrips, miniPlanTurnTime,
                    shortConnectionTimeForDriver, shortConnectionTimeForVehicle, maxDrivingTime, maxWorkingTime,
                    costForUsingDriver, costForUsingVehicle,costForDriverIdleTimePerUnit,costForVehicleIdleTimePerUnit,
                    costForChangeOverCostPerChange,maxPercentageOfCombineTrip,maxFolderOfTimeSlotAsTimeWindow,maxWaitingTime,
                    timeUnit,scale_DisAndDur,startingTimePlanning,endingTimePlanning);

            // Then read the Legs and Aicrafts and Crews and GraphLegs_contains_the_origin_and_destination
            readCities(buffer,instance);
            readDepot(buffer, instance);
            readTrips(buffer, instance);
            readVehicles(buffer, instance);
            readDrivers(buffer, instance);
////            readGraphArcForTrips(buffer, instance);
///*            readGraphForVehicleStart(buffer, instance);
//            readGraphForVehicleEnd(buffer, instance);*/
////            readTripsInSetH(buffer,instance);
//            readTripsInSetR(buffer,instance);
            //close the buffered reader and the file reader
            buffer.close();
            reader.close();
            return instance;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("ex");
        }

        return null;
    }



    private void readCities(BufferedReader buffer, Instance instance) throws IOException {
        String line = buffer.readLine();
        String[] values;
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        for (int i = 0; i < instance.getNbCities(); i++) {
            values = line.split(" ");
            CoordinateOfCity coordinateOfCity = new CoordinateOfCity(Integer.valueOf(values[2]), Integer.valueOf(values[3]));
            City city = new City(
                    Integer.valueOf(values[0]),
                    String.valueOf(values[1]),coordinateOfCity);
            instance.setCity(i, city);
            line = buffer.readLine();
        }
    }

    private void readTrips(BufferedReader buffer, Instance instance) throws IOException {
        String line = buffer.readLine();
        String[] values;
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        for (int i = 0; i < instance.getNbTrips(); i++) {
            //do the treatment of the current line
            values = line.split(" ");
            Trip trip = new Trip(
                    Integer.valueOf(values[0]),
                    Integer.valueOf(values[1]),
                    Integer.valueOf(values[2]),
                    Integer.valueOf(values[3]),
                    Integer.valueOf(values[4]),
                    Integer.valueOf(values[5]),
                    Integer.valueOf(values[6]));
            instance.setTrip(i, trip);
            //update current line to the next line
            line = buffer.readLine();
        }
    }

    private void readVehicles(BufferedReader buffer, Instance instance) throws IOException {
        String line = buffer.readLine();
        String[] values;
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        for (int i = 0; i < instance.getMaxNbVehicleAvailable(); i++) {
            //do the treatment of the current line
            values = line.split(" ");
            Vehicle a = new Vehicle(
                    Integer.valueOf(values[0]),// the first is the idOfVehicle
                    Integer.valueOf(values[1]),// third is the id of starting ID of the Depot
                    Integer.valueOf(values[2]));//the fourth is for the number of legs in the whole graph
            instance.setVehicle(i, a);
            //update current line to the next line
            line = buffer.readLine();
        }
    }

    private void readDrivers(BufferedReader buffer, Instance instance) throws IOException {
        String line = buffer.readLine();
        String[] values;
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        for (int i = 0; i < instance.getMaxNbDriverAvailable(); i++) {
            //do the treatment of the current line
            values = line.split(" ");
            Driver c = new Driver(
                    Integer.valueOf(values[0]),
                    Integer.valueOf(values[1]),
                    Integer.valueOf(values[2]));
            instance.setDriver(i, c);
            //update current line to the next line
            line = buffer.readLine();
        }
    }
//
//    private void readGraphArcForTrips(BufferedReader buffer, Instance instance) throws IOException {
//        String line = buffer.readLine();
//        String[] values;
//        while (line.startsWith("//") || line.isEmpty()) {
//            line = buffer.readLine();
//        }
//        while (line != null && (!line.isEmpty())) {
//            values = line.split(" ");
//            if (values.length == 4) { //here we change the length of values because it adds the cost of changeover, if we dont change it will be the default value maximum
//                instance.setIdleTimeCostForVehicle(Integer.valueOf(values[0]), Integer.valueOf(values[1]),  Integer.valueOf(values[2]));
//                instance.setIdleTimeCostForDriver(Integer.valueOf(values[0]), Integer.valueOf(values[1]),  Integer.valueOf(values[3]));
//            }
//            line = buffer.readLine();
//        }
//    }

    public void readDepot(BufferedReader buffer, Instance instance) throws IOException {
        String line = buffer.readLine();
        String[] values;
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }

        for (int i = 0; i < instance.getNbDepots(); i++) {
            values = line.split(" ");
            Depot depot = new Depot(Integer.valueOf(values[0]),Integer.valueOf(values[1]), Integer.valueOf(values[2]), Integer.valueOf(values[3]));
            instance.setDepot(i, depot);
            line = buffer.readLine();
        }

    }

    private int readNextInteger(BufferedReader buffer) throws IOException {
        String line = buffer.readLine();// read a line in the file and put it in the variable "line"
        while (line.startsWith("//") || line.isEmpty()) {//skill:line.startsWith("//") allow us to add the comment in the read file
            line = buffer.readLine();
        }
        line = line.trim();
        return Integer.valueOf(line);
    }

    private double readNextDouble(BufferedReader buffer) throws IOException {
        String line = buffer.readLine();// read a line in the file and put it in the variable "line"
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        line = line.trim(); // skill: line.trim()  could make us to delete the  unexpected space in the line
        return Double.valueOf(line);
    }


    private String readNextString(BufferedReader buffer) throws IOException {
        String line = buffer.readLine();// read a line in the file and put it in the variable "line"
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        line = line.trim(); // skill: line.trim()  could make us to delete the  unexpected space in the line
        return String.valueOf(line);
    }

    public static void main(String[] args) {
        //test an instance
        //InstanceReader reader = new InstanceReader("instanceOfRoutingAndScheduling.txt");
        //InstanceReader reader = new InstanceReader("vehicleLargerExample.txt");
        //InstanceReader reader = new InstanceReader("vehicleRoutingSmallExample.txt");//test vehicle example
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        System.out.println(instance.getTrip(1));
        System.out.println(instance.getCity(1));
    }
}

