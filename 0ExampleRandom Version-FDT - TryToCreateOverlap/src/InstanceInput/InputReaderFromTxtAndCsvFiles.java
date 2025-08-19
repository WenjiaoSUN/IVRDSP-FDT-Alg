package InstanceInput;

import InstanceCreate.RandomInstanceCreate;
import InstanceGet.Instance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputReaderFromTxtAndCsvFiles {
    private String textFileName;
    private String csvFileName;

    public InputReaderFromTxtAndCsvFiles(String textFileName, String csvFileName) {
        this.textFileName = textFileName;
        this.csvFileName = csvFileName;
    }

    public List<Input> readfiles() {
        List<Input> inputs = new ArrayList<>();
        try {
            FileReader textReader = new FileReader(textFileName);
            BufferedReader textBuffer = new BufferedReader(textReader);
            //read the file
            int minPlanTime = readNextInteger(textBuffer);
            int maxPlanTime = readNextInteger(textBuffer);
            int maxWaitingTime = readNextInteger(textBuffer);
            int shortTimeForDriver = readNextInteger(textBuffer);
            int shortTimeForVehicle = readNextInteger(textBuffer);
            double keyWorkAndDriveTimePercentageGoBackToDepot=readNextDouble(textBuffer);
            int maxChargingTimeForVehicleInDepot=readNextInteger(textBuffer);
            int timeSlotUnit=readNextInteger(textBuffer);
            double scale_DisAndDur=readNextDouble(textBuffer);
            //after read text file
            FileReader csvReader = new FileReader(csvFileName);
            BufferedReader csvBuffer = new BufferedReader(csvReader);
            csvBuffer.readLine(); // read first csvLine with columns names
            String csvLine = csvBuffer.readLine();//read the second csvLine with parameters inside of it
            while (csvLine != null && !csvLine.isEmpty()) {
               //System.out.println(csvLine);
                String[] values = csvLine.split(",");
                int distanceX = Integer.valueOf(values[0]);
                int distanceY = Integer.valueOf(values[1]);
                int nbCities = Integer.valueOf(values[2]);
                int nbTrips = Integer.valueOf(values[4]);
                double percTripCombined = Double.valueOf(values[6]);
                int maxFolderOfTimeSlotAsTimeWindow=Integer.valueOf(values[7]);
                int nbDepots = Integer.valueOf(values[3]);
                int startHorizon = Integer.valueOf(values[8]);
                int endHorizon = Integer.valueOf(values[9]);
                int maxDrivingTime = Integer.valueOf(values[10]);
                int maxWorkingTime = Integer.valueOf(values[11]);
                int costUseVehicle = Integer.valueOf(values[12]);
                int costUseDriver = Integer.valueOf(values[13]);
                int costOfIdlePerUnitForVehicle = Integer.valueOf(values[14]);
                int costOfIdlePerUnitForDriver = Integer.valueOf(values[15]);
                int costOfChangeOverPerChange = Integer.valueOf(values[16]);


                Input input = new Input(distanceX, distanceY, nbCities, nbTrips, percTripCombined, maxFolderOfTimeSlotAsTimeWindow, nbDepots, startHorizon, endHorizon,
                        minPlanTime,maxPlanTime, maxWaitingTime, shortTimeForDriver, shortTimeForVehicle, maxDrivingTime, maxWorkingTime,
                        costUseVehicle, costUseDriver, costOfIdlePerUnitForVehicle, costOfIdlePerUnitForDriver, costOfChangeOverPerChange,
                        keyWorkAndDriveTimePercentageGoBackToDepot,maxChargingTimeForVehicleInDepot,timeSlotUnit,scale_DisAndDur);
                inputs.add(input);

                csvLine = csvBuffer.readLine();// be updated to next line

            }
            textBuffer.close();
            textReader.close();
            csvBuffer.close();
            csvReader.close();
            return inputs;
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
        InputReaderFromTxtAndCsvFiles reader = new InputReaderFromTxtAndCsvFiles(
                "FixedParameter.txt", "ChangingParameter.csv");
        List<Input> inputs = reader.readfiles();
       // System.out.println(inputs.get(0).getNbCities());

        for (int i = 0; i < inputs.size(); i++) {
            Input input = inputs.get(i);
            int nbCities = input.getNbCities();
            int distance = input.getDistanceX();
            int nbTrips = input.getNbTrips();
            int planningHorizon = input.getEndHorizon();
            double percTripCombined = input.getMaxPercentageCombinedTrip();
            //System.out.println(percTripCombined);
            int maxFolderOfTimeSlotUnitAsTW=input.getFolderOfTimeSlotUnitAsTimeWindow();


            //int indexOfCSVline =i+2;
            RandomInstanceCreate randomInstanceCreate = new RandomInstanceCreate(input);
            Instance instance = randomInstanceCreate.getInstanceFromRandomCreate();
            if (nbCities < 5) {
                if (nbTrips < 100) {
                    if (planningHorizon == 1440) {
                        if (percTripCombined < 1) {
                            if(maxFolderOfTimeSlotUnitAsTW<=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity0" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity0" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined +"_TW"+ (maxFolderOfTimeSlotUnitAsTW)+ ".txt");
                            }
                        }

                    }
                    if (planningHorizon == 2880) {
                        if (percTripCombined < 1) {
                            if(maxFolderOfTimeSlotUnitAsTW<=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity0" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity0" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt");
                            }

                        }

                    }

                    if (planningHorizon == 4320) {
                        if (percTripCombined < 1) {
                            if(maxFolderOfTimeSlotUnitAsTW<=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity0" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity0" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt");
                            }
                        }

                    }

                }
                if (nbTrips >= 100) {
                    if (planningHorizon == 1440) {
                        if (percTripCombined < 1) {
                            if(maxFolderOfTimeSlotUnitAsTW<=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity0" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity0" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt");
                            }
                        }
                    }


                    if (planningHorizon == 2880) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity0" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity0" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW)  + ".txt");
                            }
                        }

                    }
                    if (planningHorizon == 4320) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <= 60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity0" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity0" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt");
                            }
                        }
                    }

                }

            }

            if (nbCities >= 5) {
                if (nbTrips < 100) {
                    if (planningHorizon == 1440) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW)  + ".txt");
                            }
                        }

                    }
                    if (planningHorizon == 2880) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW)  + ".txt");
                            }
                        }
                    }
                    if (planningHorizon == 4320) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips0" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW)  + ".txt");

                            }
                        }
                    }

                }
                if (nbTrips >= 100) {
                    if (planningHorizon == 1440) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity" + nbCities + "_Size" + distance + "_Day1" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW)  + ".txt");
                            }
                        }
                    }
                    if (planningHorizon == 2880) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity" + nbCities + "_Size" + distance + "_Day2" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt");
                            }
                        }
                    }
                    if (planningHorizon == 4320) {
                        if (percTripCombined < 1) {
                            if (maxFolderOfTimeSlotUnitAsTW <=60) {
                                randomInstanceCreate.printInstanceInfile("inst_nbCity" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined +"_TW"+(maxFolderOfTimeSlotUnitAsTW)+ ".txt"); //********* the names should be based on the instance or input
                                randomInstanceCreate.printFeasibleSolutionInfile("feaSol_nbCity" + nbCities + "_Size" + distance + "_Day3" + "_nbTrips" + nbTrips + "_combPer" + percTripCombined+"_TW"+(maxFolderOfTimeSlotUnitAsTW) + ".txt");
                            }
                        }
                    }

                }
            }

        }
    }
}
