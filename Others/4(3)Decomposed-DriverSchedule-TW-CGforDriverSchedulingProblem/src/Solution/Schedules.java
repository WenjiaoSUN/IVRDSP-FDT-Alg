package Solution;
//
//import Generator.PathsGeneratorBasedOnGivenDepot;
import Instance.Instance;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Schedules {
    private Instance instance;
    private ArrayList<DriverSchedule> driverSchedules;

    public Schedules(Instance instance) {
        this.instance = instance;
        this.driverSchedules = new ArrayList<>();
    }

    public int getCostC(int idOfSchedule_k) {
        int c_k = (int) instance.getFixedCostForDriver();//fixed + idleTime cost+ changeover
        int c_DI=instance.getIdleTimeCostForDriverPerUnit();
        DriverSchedule driverSchedule = this.driverSchedules.get(idOfSchedule_k);
        for (int l = 0; l < driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size() - 1; l++) {
            int firstId = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getIdOfTrip();
            int secondId = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l + 1).getIdOfTrip();
            boolean whetherDrivingInFirstTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getDrivingStatus();
            boolean whetherDrivingInSecondTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l + 1).getDrivingStatus();
            TripWithWorkingStatusAndDepartureTime tripWitStatusAndDepTime1= driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l);
            TripWithWorkingStatusAndDepartureTime tripWithStatusAndDepTime2=driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l+1);
            if (instance.whetherHavePossibleArcAfterCleaning(firstId, secondId) ) {
                int conTime=tripWithStatusAndDepTime2.getDepartureTime()
                        -tripWitStatusAndDepTime1.getDepartureTime()-tripWitStatusAndDepTime1.getDuration();
                c_k = (int) (c_k + c_DI*conTime);
            }

            if (whetherDrivingInFirstTrip != whetherDrivingInSecondTrip) {
                c_k = (int) (c_k + instance.getCostForChangeOver());
            }

        }
        return c_k;
    }

//    public double getReducedCost(int idOfSchedule){
//
//    }

    public ArrayList<DriverSchedule> getDriverSchedules() {
        return driverSchedules;
    }

    public void addSchedule(DriverSchedule driverSchedule) {
        this.driverSchedules.add(driverSchedule);
    }

    public void printDriverSchedulesSolutionInFile(String fileName) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName);
            writer.println("//DriverScheduling solution from column generation: idOfDriver idOfDepot indexStartingDepot List<TripWithWorkingStatus> indexEndingDepot");
            for (int s = 0; s < this.driverSchedules.size(); s++) {
                DriverSchedule driverSchedule = this.driverSchedules.get(s);
                writer.print("driver " + s + " " + driverSchedule);
            }
            writer.close();

        } catch (FileNotFoundException e) {
            System.out.println("Write file error happened");
            System.out.println(e);
        }
    }

    public boolean whetherScheduleIsFeasible() {
        boolean isFeasible = true; // Assuming the solution is feasible by default

        // Check whether all the trips are performed
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int idOfTrip = i;
            boolean whetherTripFeasibleInSolution = false;

            // Check whether at least show up q_i times
            // Check whether at least one is in driving status
            int q_i = instance.getTrip(i).getNbVehicleNeed();
            int countTotalShowUpTime = 0;
            int countDrivingTime = 0;

            for (int s = 0; s < this.driverSchedules.size(); s++) {
                DriverSchedule driverSchedule = this.driverSchedules.get(s);
                if (driverSchedule.whetherTripPresent(i)) {
                    countTotalShowUpTime++;
                    if (driverSchedule.whetherStatusDriving(i)) {
                        countDrivingTime++;
                    }
                }
            }

            if (countTotalShowUpTime < q_i || countDrivingTime != 1) {
                whetherTripFeasibleInSolution = false;
                System.out.println("trip " + i + " is not feasible in the solution");
                isFeasible = false; // Update the overall feasibility
                break; // No need to check further trips if the current trip is infeasible
            } else {
                whetherTripFeasibleInSolution = true;
            }
        }
        return isFeasible;
    }

    @Override
    public String toString() {
        String string = "//Schedules: driver idDriver idDepot indexStarting tripWithWorkingStatus indexEnding \n";
        for (int s = 0; s < this.driverSchedules.size(); s++) {
            string += "driver " + s + " ";
            string += this.driverSchedules.get(s);
        }
//        for(DriverSchedule driverSchedules1: driverSchedules){
//            s+=driverSchedules1+"\n";
//        }
        return string;
    }

//    public static void main(String[] args) {
//        InstanceReader reader = new InstanceReader("inst_nbCity10_Size240_Day1_nbTrips050_combPer0.0.txt");//test vehicle example
//        Instance instance = reader.readFile(); //this will  read the file
//        //System.out.println(instance);
//        PathsGeneratorBasedOnGivenDepot pathsGeneratorBasedOnGivenTrip = new PathsGeneratorBasedOnGivenDepot(instance);
//        System.out.println(pathsGeneratorBasedOnGivenTrip.generatePaths(instance.getTrip(0)));
//        ArrayList<DriverSchedule> schedules= pathsGeneratorBasedOnGivenTrip.generatePaths(instance.getTrip(0));
//        System.out.println(schedules);
//        // schedules.printDriverSchedulesSolutionInFile("sol.txt");
//    }


}
