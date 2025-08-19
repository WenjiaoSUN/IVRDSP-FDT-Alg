package Solution;

import Instance.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * the solution file should contain several paths of aircraft and several paths of crew
 * we need use Array or List
 */
public class Solution {
    private Instance instance;
    private LinkedList<PathForVehicle> pathForVehicles; //there are many vehicle's path
    private LinkedList<PathForDriver> pathForDrivers; //there are many crews' path


    public Solution(Instance instance) {
        this.instance = instance;
        this.pathForVehicles = new LinkedList<>();
        this.pathForDrivers = new LinkedList<>();

    }

    public LinkedList<PathForVehicle> getPathForVehicles() {
        return pathForVehicles;
    }

    public LinkedList<PathForDriver> getPathForDrivers() {
        return pathForDrivers;
    }

    public void addPathInSetForVehicle(PathForVehicle pathForVehicle) {
        this.pathForVehicles.add(pathForVehicle);
    }

    public void addPathInSetForDriver(PathForDriver pathForDriver) {
        this.pathForDrivers.add(pathForDriver);
    }

    public double getTotalChangeoverCostForAllDriver() {
        double changeoverCost = 0;
        for (int d = 0; d < this.pathForDrivers.size(); d++) {
            // 2024.9.24 correct the error of < this.pathForDrivers.size()-1; it should be < this.pathForDrives.size()
            ArrayList<TripWithDriveInfos> pathForDriver = this.pathForDrivers.get(d).getDriverPathWithInfos();
            for (int i = 0; i < pathForDriver.size() - 1; i++) {
                int idOfFormerTrip = pathForDriver.get(i).getIdOfTrip();
                int idOfLatterTrip = pathForDriver.get(i + 1).getIdOfTrip();
                boolean workingStatusInFormerTrip = pathForDriver.get(i).getDrivingStatus();
                boolean workingStatusInLatterTrip = pathForDriver.get(i + 1).getDrivingStatus();
                int idOfVehicleInFormerTrip = pathForDriver.get(i).getIdOfVehicle();
                int idOfVehicleInLatterTrip = pathForDriver.get(i + 1).getIdOfVehicle();

                if ((idOfVehicleInFormerTrip != idOfVehicleInLatterTrip) || (workingStatusInFormerTrip != workingStatusInLatterTrip)) {
                    // System.out.println("changOverHappen for driver_"+d+" with trip_"+idOfFormerTrip+" to trip_"+idOfLatterTrip);
                    changeoverCost = changeoverCost + this.instance.getCostForChangeOver();
                    changeoverCost = Math.round(100.0 * changeoverCost) / 100.0;
                    System.out.println("Until now total changOver Cost is " + changeoverCost + " because of " + "idOfDriver_" + d + " formerTrip_" + idOfFormerTrip + " latterTrip_" + idOfLatterTrip);
                }
                if (workingStatusInFormerTrip == false && workingStatusInLatterTrip == false && (idOfVehicleInFormerTrip != idOfVehicleInLatterTrip)) {
                    changeoverCost = changeoverCost - this.instance.getCostForChangeOver();
                    System.out.println("Until now total changOver Cost is " + changeoverCost + " because of remove penalty for driver" + "idOfDriver_" + d + " formerTrip_" + idOfFormerTrip + " latterTrip_" + idOfLatterTrip);
                }
                //even the same status and same vehicle, but the vehicle doesnt perform successive then it should have change over cost accoring to the model constraints 27
                if (workingStatusInFormerTrip == true && workingStatusInLatterTrip == true && (idOfVehicleInFormerTrip == idOfVehicleInLatterTrip)) {
                    boolean whetherNeedAddPenalty = false;
                    for (int v = 0; v < this.pathForVehicles.size(); v++) {
                        PathForVehicle pathForVehicle = this.pathForVehicles.get(v);
                        if (pathForVehicle.getIdOfVehicle() == idOfVehicleInLatterTrip && pathForVehicle.getIdOfVehicle() == idOfVehicleInLatterTrip) {
                            boolean whetherVehiclePerformSuccessive = pathForVehicle.areConsecutiveInPathOfVehicle(instance.getTrip(idOfFormerTrip), instance.getTrip(idOfLatterTrip));
                            if (whetherVehiclePerformSuccessive == false) {
                                whetherNeedAddPenalty = true;
                            }
                        }
                    }
                    if (whetherNeedAddPenalty) {
                        changeoverCost = changeoverCost + this.instance.getCostForChangeOver();
                        System.out.println("Until now total changOver Cost is " + changeoverCost + " because of add penalty for driver" + "idOfDriver_" + d + " formerTrip_" + idOfFormerTrip + " latterTrip_" + idOfLatterTrip);
                    }
                }
            }
        }
        return changeoverCost;
    }

    public double getFixedCostForVehicle() {
        return this.pathForVehicles.size() * instance.getFixedCostForVehicle();
    }

    public double getFixedCostForDriver() {
        return this.pathForDrivers.size() * instance.getFixedCostForDriver();
    }

    public double getTotalFixedCost() {
        double totalFixedCost = 0;
        return this.getFixedCostForVehicle() + this.getFixedCostForDriver();
    }

    public double getTotalIdleTimeCostOfAllVehicle() {
        double cost = 0;
        for (int i = 0; i < this.pathForVehicles.size(); i++)
            cost = cost + this.pathForVehicles.get(i).getIdleTimeCostOfVehiclePath();
        return cost;
    }

    public double getTotalIdleTimeCostOfAllDriver() {
        double cost = 0;
        for (int i = 0; i < this.pathForDrivers.size(); i++)
            cost = cost + this.pathForDrivers.get(i).getIdleTimeCostOfDriverPath();
        return cost;
    }

    public double getTotalIdleTimeCost() {
        return this.getTotalIdleTimeCostOfAllVehicle() + this.getTotalIdleTimeCostOfAllDriver();
    }

    public double getTotalCost() {
        return this.getTotalFixedCost() + this.getTotalIdleTimeCost() + this.getTotalChangeoverCostForAllDriver();
    }

    public int getNbDriversInSolution() {
        return this.pathForDrivers.size();
    }

    public int getNbVehiclesInSolution() {
        return this.pathForVehicles.size();
    }

    /**
     * This is the second part, which is help two check whether the solution is feasible it can be decomposed to  1,2,3,4 steps
     * 1 check whether all paths are feasible--which means the path is connected well; it can just call the methods  "isPathForAircraftFeasible" and "isPathForCrewFeasible"
     * in class PathForVehicle and class PathForDriver
     * 2 check every node is visited by one crew and aircraft; we can examine all the legs whether they have been found  at least once in the path of aircraft and once in the path of the crew,
     * it can call the methods "isPresentInPathOfAircraft"&"isPresentInPathOfCrew"  in class PathForVehicle and class PathForDriver
     * 3 in the one specific solution, for one crew and one aircraft there is no possible to have more than one path_(avoid the situation which more than one paths for a specific aircraft or  a specific crew)
     * think adverse,it turns out to count how many the id of aircraft (or id of the crew) shows up throughout all the Paths
     * 4.check when the connection time is less than 1h, whether the path and crew not change like the former leg
     */
    public boolean isSolutionFeasible(boolean verbose) {
        // verbose is to remind us whether to give a hint of the result of Feasible
        boolean feas = true;

        //1.1 judge all the paths for the vehicles are feasible
        for (int i = 0; i < this.pathForVehicles.size(); i++) {
            if (!this.pathForVehicles.get(i).isFeasible(false)) {
                feas = false;
                if (verbose)
                    System.out.println("The path of vehicle" + this.pathForVehicles.get(i).getIdOfVehicle() + " is not feasible: ");
            }
        }
        //1.2 judge all the paths for drivers are feasible
        for (int i = 0; i < this.pathForDrivers.size(); i++) {
            if (!this.pathForDrivers.get(i).isPathFeasible(false)) {
                feas = false;
                if (verbose) {
                    System.out.println("The path of Driver " + this.pathForDrivers.get(i).getIdOfDriver() + " is not feasible.");
                }
            }
        }

        //2.1 check all the leg has been visited at least once in the path of vehicle
        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            boolean isPresentInPathOfVehicle = false;
            for (int j = 0; j < this.pathForVehicles.size(); j++) {
                if (this.pathForVehicles.get(j).isPresentInPath(this.instance.getTrip(i))) {
                    isPresentInPathOfVehicle = true;
                    break;
                }
            }
            if (isPresentInPathOfVehicle == false) {
                feas = false;
                if (verbose) {
                    System.out.println("The leg " + i + " has not been visited in a path of the aircraft.");
                }
            }
        }

        //2.2 check all the trips have been visited  at least once in the path of the driver
        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            boolean isPresentedInPathOfDriver = false;
            for (int j = 0; j < this.pathForDrivers.size(); j++) {
                if (this.pathForDrivers.get(j).isPresentInPath(this.instance.getTrip(i))) {
                    isPresentedInPathOfDriver = true;
                    break;
                }
            }
            if (isPresentedInPathOfDriver == false) {
                feas = false;
                if (verbose) {
                    System.out.println("The leg " + i + " has not been visited in a path of the crew.");
                }
            }
        }

        /**3.1 check for every vehicle here is only one path in the solution
         * count the id in the  all paths
         */

        for (Vehicle a : this.instance.getVehicles()) {
            int id = a.getIdOfVehicle();
            int countTheNumberOfPathContainThisIdOfAircraft = 0;
            for (PathForVehicle p : pathForVehicles) {
                if (p.getIdOfVehicle() == id) {
                    countTheNumberOfPathContainThisIdOfAircraft++;
                }
            }
            if (countTheNumberOfPathContainThisIdOfAircraft > 1) {
                feas = false;
                if (verbose) {
                    System.out.println("There are more than one path for this aircraft " + id + ".");
                }

            }
        }

        //3.2 check for every driver there is only one path in the solution
        //count the number of drivers in the path, it should show up only once for all paths
        for (Driver c : this.instance.getDrivers()) {
            int id = c.getIdOfDriver();
            int countTheNumberOfPathContainThisIdOfCrew = 0;
            for (PathForDriver p : pathForDrivers) {
                if (p.getIdOfDriver() == id) {
                    countTheNumberOfPathContainThisIdOfCrew++;
                }
            }
            if (countTheNumberOfPathContainThisIdOfCrew > 1) {
                feas = false;
                if (verbose) {
                    System.out.println("There are more than one path for this crew " + id + ".");
                }

            }
        }

        //4. check when the connection time is less than driver's short connection time, whether the Driver and Aircraft not change
        // find two trips in driver's short connection time, then find the vehicle for these two trips judge whether it continuously appears in the path of vehicle
        for (PathForDriver p : pathForDrivers) {//check all paths & find for this driver which is labeled as p
            for (int i = 0; i < p.getDriverPathWithInfos().size() - 1; i++) {
                if (p.getConnectionTimeInPathForDriver(i) <= this.instance.getShortConnectionTimeForDriver()) { //then find all the short connection time
                    boolean areInSameVehicle = false;
                    TripWithDriveInfos lFirst = p.getDriverPathWithInfos().get(i);
                    TripWithDriveInfos lSecond = p.getDriverPathWithInfos().get(i+1);

                    for (PathForVehicle pathForVehicle : pathForVehicles) {//check the vehicle  path to find which contains the trips which related to the short connection time
                        if (pathForVehicle.areConsecutiveInPathOfVehicle(lFirst, lSecond)) {//if the path contains trip i and trip j
                            areInSameVehicle = true;
                            break;
                        }
                    }

                    if (!areInSameVehicle) {
                        feas = false;
                        if (verbose) {
                            System.out.println("There exist a bad exchange for crews between the leg " + i + " and the leg " + (i + 1));
                        }
                    }
                }
            }
        }
        return feas;
    }


    /**
     * This is the last part, which is about the printInfile,in order to give the solution into a file
     */

    public void printInfile(String fileName) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName);
            writer.println("//The mini cost is including :" + this.getTotalCost());
            writer.println("//The fixed cost for vehicle and driver :" + this.getTotalFixedCost());
            writer.println("//The idle cost for vehicle and driver :" + this.getTotalIdleTimeCost());
            writer.println("//The idle cost for all drivers :" + this.getTotalIdleTimeCostOfAllDriver());
            writer.println("//THe idle cost for all vehicles " + this.getTotalIdleTimeCostOfAllVehicle());

            writer.println("//Routes for all vehicles: idVehicle nbTripsInRoutes  [idTrip departureTime]" );

            for (PathForVehicle pathForVehicle : this.pathForVehicles) {
                writer.println(pathForVehicle);
            }
            writer.println("//Schedules for all drivers: idDriver nbTripsInRoutes  [idTrip idVehicle status departureTime]" );

            for (PathForDriver pathForDriver : this.pathForDrivers) {
                writer.println(pathForDriver);
            }

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        //InstanceReader reader = new InstanceReader("vehicleLargerExample.txt");
        //InstanceReader reader = new InstanceReader("instanceOfRoutingAndScheduling.txt");
        //InstanceReader reader = new InstanceReader("largerInstance.txt");
        //InstanceReader reader = new InstanceReader("vehicleRoutingSmallExample.txt");//假如换新文件了，记得下面需要给出人的路径也要修改，每个例子中的人数又不一样
        //Instance instance = reader.readFile();
        //Solution solution = new Solution(instance);


        InstanceReader reader = new InstanceReader("instance Generate.txt");
        Instance instance = reader.readFile(); //这个语句将文本的内容就读出来了
        System.out.println(instance);
        String warmStartFileName = "feaSol_nbCity05_Size180_Day1_nbTrips050_combPer0.1.txt";
        // solve

        // print solution


    }
}
