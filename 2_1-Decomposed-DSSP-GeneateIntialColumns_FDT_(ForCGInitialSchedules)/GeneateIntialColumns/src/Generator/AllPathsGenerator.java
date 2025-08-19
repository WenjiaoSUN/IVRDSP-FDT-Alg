package Generator;

import Instance.Instance;
import Instance.Trip;
import Instance.InstanceReader;
import Solution.DriverSchedule;
import Instance.TripWithWorkingStatusAndDepartureTime;
import Solution.Schedules;

import java.util.ArrayList;

public class AllPathsGenerator {
    private Instance instance;

    public AllPathsGenerator(Instance instance) {
        this.instance = instance;
    }


    long generaPathsTime=0;
    public Schedules generateSchedules(int nbMaxPathForEachStartingTrip,int maxLengthPreviousTrip) {
        long startTime=System.currentTimeMillis();
        Schedules schedules = new Schedules(this.instance);
            for (int i = 0; i < this.instance.getNbTrips(); i++) {
                PathsGeneratorBasedOnGivenTrip pathsGeneratorBasedOnGivenTrip= new PathsGeneratorBasedOnGivenTrip(this.instance);
                Trip trip = this.instance.getTrip(i);
                if (this.instance.whetherDriverCanStartWithTrip(trip.getIdOfTrip())) {
                    System.out.println("check here is the trip"+trip);
                    ArrayList<DriverSchedule> driverSchedules = pathsGeneratorBasedOnGivenTrip.generatePaths(trip,nbMaxPathForEachStartingTrip,maxLengthPreviousTrip);
                    System.out.println("check the final driver schedule"+driverSchedules);
                    if (driverSchedules.size()!= 0) {
                        for (int s = 0; s < driverSchedules.size(); s++) {
                            DriverSchedule driverSchedule=driverSchedules.get(s);
                            schedules.addSchedule(driverSchedule);
                            System.out.println("here are the schedules until trip"+trip.getIdOfTrip()+schedules);

                            for(int j=0;j<driverSchedule.getSchedule().size();j++){//this for loop if for each combined trip, we add the working status is false
                                Trip tripJ=driverSchedule.getSchedule().get(j);
                                if(tripJ.getNbVehicleNeed()==2){
                                    DriverSchedule newDriverSchedule=new DriverSchedule(instance);
                                    newDriverSchedule.setIdOfDepot(driverSchedule.getIdOfDepot());
                                    newDriverSchedule.setIndexDepotAsStartingPoint(driverSchedule.getIndexDepotAsStartingPoint());
                                    newDriverSchedule.setIndexDepotAsEndingPoint(driverSchedule.getIndexDepotAsEndingPoint());
                                    for(int k=0;k<driverSchedule.getSchedule().size();k++){
                                        Trip tripK=driverSchedule.getSchedule().get(k);
                                        if(tripK.getNbVehicleNeed()==1){
                                            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus= new TripWithWorkingStatusAndDepartureTime(tripK,true,tripK.getMiddleDepartureTime() );
                                            newDriverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus);

                                        }else {
                                            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus= new TripWithWorkingStatusAndDepartureTime(tripK,false,tripK.getMiddleDepartureTime());
                                            newDriverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus);
                                        }
                                    }
                                    schedules.addSchedule(newDriverSchedule);
                                }
                            }

                        }
                    }
                }

            }
            long endTime=System.currentTimeMillis();
            generaPathsTime=(endTime-startTime)/1000;

        return schedules;
    }

    public double getGenerateTime(){
        return generaPathsTime;
    }

    @Override
    public String toString() {
        return "AllPathsGenerator{" +
                "instance=" + instance +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        //System.out.println(instance);
        PathsGeneratorBasedOnGivenTrip pathsGeneratorBasedOnGivenTrip = new PathsGeneratorBasedOnGivenTrip(instance);
        //System.out.println(pathsGeneratorBasedOnGivenTrip.generatePaths(instance.getTrip(0)));
        AllPathsGenerator allPathsGenerator = new AllPathsGenerator(instance);
        Schedules schedules= allPathsGenerator.generateSchedules(10,8);
        schedules.printDriverSchedulesSolutionInFile("schedule_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1.txt");
        System.out.println(schedules);
        System.out.println(allPathsGenerator.getGenerateTime()+"sec");
    }
}
