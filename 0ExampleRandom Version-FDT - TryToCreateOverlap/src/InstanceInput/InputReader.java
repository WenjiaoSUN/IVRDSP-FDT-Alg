package InstanceInput;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InputReader {
    private String fileName;

    public InputReader(String fileName) {
        this.fileName = fileName;
    }

    public Input readfile() {
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader buffer = new BufferedReader(reader);
            //read the file
            int coordinateX = readNextInteger(buffer);
            int coordinateY = readNextInteger(buffer);
            int nbCities = readNextInteger(buffer);
            int nbTrips = readNextInteger(buffer);
            double percTripCombined = readNextDouble(buffer);
            int maxFolderOfTimeSlotUnitAsTimeWindow=readNextInteger(buffer);
            int nbDepots = readNextInteger(buffer);
            int startHorizon = readNextInteger(buffer);
            int endHorizon = readNextInteger(buffer);
            int minPlanTime = readNextInteger(buffer);
            int maxPlanTime = readNextInteger(buffer);
            int maxWaitingTime = readNextInteger(buffer);
            int shortTimeForDriver = readNextInteger(buffer);
            int shortTimeForVehicle = readNextInteger(buffer);
            int maxDrivingTime = readNextInteger(buffer);
            int maxWorkingTime = readNextInteger(buffer);
            int costUseVehicle = readNextInteger(buffer);
            int costUseDriver = readNextInteger(buffer);
            int costOfIdlePerUnitForVehicle = readNextInteger(buffer);
            int costOfIdlePerUnitForDriver = readNextInteger(buffer);
            int costOfChangeOverPerChange = readNextInteger(buffer);
            double keyWorkAndDriveTimePercentageGoBackToDepot=readNextDouble(buffer);
            int maxChargingTimeForVehicleInDepot=readNextInteger(buffer);
            int timeSlotUnit=readNextInteger(buffer);
            double scale_DisAndDur=readNextDouble(buffer);

            //According to the first line we can build our input
            Input input = new Input(coordinateX, coordinateY, nbCities, nbTrips, percTripCombined, maxFolderOfTimeSlotUnitAsTimeWindow,nbDepots, startHorizon, endHorizon,
                    minPlanTime,maxPlanTime, maxWaitingTime, shortTimeForDriver, shortTimeForVehicle, maxDrivingTime, maxWorkingTime,
                    costUseVehicle, costUseDriver, costOfIdlePerUnitForVehicle, costOfIdlePerUnitForDriver,
                    costOfChangeOverPerChange,keyWorkAndDriveTimePercentageGoBackToDepot,maxChargingTimeForVehicleInDepot,timeSlotUnit,scale_DisAndDur);

            //close the buffered reader and the file reader

            buffer.close();
            reader.close();
            return input;


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private int readNextInteger(BufferedReader buffer) throws IOException {
        String line = buffer.readLine();
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        line = line.trim();
        return Integer.valueOf(line);

    }


    private double readNextDouble(BufferedReader buffer) throws IOException {
        String line = buffer.readLine();
        while (line.startsWith("//") || line.isEmpty()) {
            line = buffer.readLine();
        }
        line = line.trim();
        return Double.valueOf(line);
    }


    public static void main(String[] args) {
        InputReader reader = new InputReader("Small random example_inputData.txt");
        Input input = reader.readfile();
        System.out.println(input);
    }
}
