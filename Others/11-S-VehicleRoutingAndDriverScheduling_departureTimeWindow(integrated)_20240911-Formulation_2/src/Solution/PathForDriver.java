package Solution;

import Instance.Instance;
import Instance.Trip;

import java.util.ArrayList;

//This file describes one example path for the one crew

public class PathForDriver {
    private Instance instance;
    private int idOfDriver;

    private ArrayList<TripWithDriveInfos> driverPath;

    public PathForDriver(Instance instance, int idOfDriver) {
        this.instance = instance;
        this.idOfDriver = idOfDriver;
        this.driverPath = new ArrayList<TripWithDriveInfos>();

    }
    public int getIdOfDriver() {
        return idOfDriver;
    }
    public ArrayList<TripWithDriveInfos> getDriverPath() {
        return driverPath;
    }


    public void addTripInDriverPath(TripWithDriveInfos tripWithDriveInfos) {
        this.driverPath.add(tripWithDriveInfos);
    }

    public TripWithDriveInfos getTripFromDriverPath(int idOfTrip){
        return this.driverPath.get(idOfTrip);
    }
    public double getIdleTimeCostOfDriverPath() {
        double idleTimeCost = 0;
        for (int i = 0; i < this.driverPath.size() - 1; i++) {
            int idOfFormerTrip = this.driverPath.get(i).getIdOfTrip();
            int idOfLatterTrip = this.driverPath.get(i + 1).getIdOfTrip();
            TripWithDriveInfos formerTripWithStartingInfos=this.driverPath.get(i);
            TripWithDriveInfos latterTripWithStartingInfos=this.driverPath.get(i+1);
            int formerStartingUnit=formerTripWithStartingInfos.getStartingTimeUnit();

            int latterStartingUnit=latterTripWithStartingInfos.getStartingTimeUnit();

            Trip formerTrip=instance.getTrip(idOfFormerTrip);
            int durationOfFormerTrip=formerTrip.getDuration();
            int connectionTime=(latterStartingUnit-formerStartingUnit)*instance.getTimeSlotUnit()-durationOfFormerTrip;

            if (this.instance.whetherHavePossibleArcAfterCleaning(idOfFormerTrip, idOfLatterTrip)) {
                if(connectionTime>=this.instance.getMinPlanTurnTime()) {
                    idleTimeCost = idleTimeCost + this.instance.getIdleTimeCostForDriverPerUnit()*connectionTime;
//                    System.out.println("now the idle time in driver path between trip "+idOfFormerTrip+" to trip " +idOfLatterTrip+" is"+connectionTime);
//                    System.out.println("now the idle time cost in driver path until trip " +idOfLatterTrip+" is"+idleTimeCost);
                    idleTimeCost = Math.round(100.0 * idleTimeCost) / 100.0;
                }
            } else {
                System.out.println("ERROR !!! The method of getIdleTimeCostForDriver");
                System.out.println("idOfFormerTrip = " + idOfFormerTrip);
                System.out.println("idOfLatterTrip = " + idOfLatterTrip);
            }
        }
        return idleTimeCost;
    }
    public double getTotalWorkingTime(){
        TripWithDriveInfos tripWithDriveInfosFirst= this.driverPath.get(0);
        TripWithDriveInfos tripWithDriveInfosLast= this.driverPath.get(this.driverPath.size()-1);
        double firstWorkingTimeUnit=tripWithDriveInfosFirst.getStartingTimeUnit();
        double lastWorkingTimeUnit=tripWithDriveInfosLast.getStartingTimeUnit();
        int durationOfLastTrip= instance.getTrip(tripWithDriveInfosLast.getIdOfTrip()).getDuration();
        double totalWorkingTimeInThisShift=(lastWorkingTimeUnit-firstWorkingTimeUnit)*instance.getTimeSlotUnit()+durationOfLastTrip;
        return totalWorkingTimeInThisShift;
    }

    public double getTotalDrivingTime(){
        double totalDrivingTimeInThisShift =0;
        for(int i=0;i<this.driverPath.size();i++){
            TripWithDriveInfos tripWithDriveInfos = this.driverPath.get(i);
            if(tripWithDriveInfos.getDrivingStatus()==true){
                totalDrivingTimeInThisShift=totalDrivingTimeInThisShift+instance.getTrip(i).getDuration();
            }
        }

        return totalDrivingTimeInThisShift;
    }



    public double getConnectionTimeInPathForDriver(int idOfFormerTrip) {
        int idOfCurrentTrip = idOfFormerTrip + 1;
        Trip formerTrip = this.driverPath.get(idOfFormerTrip);
        Trip currentTrip = this.driverPath.get(idOfCurrentTrip);
        if (formerTrip.getIdOfEndCity() == (currentTrip.getIdOfStartCity()))
            return currentTrip.getEarliestDepartureTime() - formerTrip.getLatestDepartureTime();
        else
            return Double.MAX_VALUE;
    }

    /**
     * This is the last part, which is help check the path is feasible 1,2,3 steps
     * 0.when we set a cost, the arc has exist, so we already consider the min-plan time, it don't need to check?
     * 1.check if the path, the every former leg destination == the latter origin?
     * if(this.instance.getLeg(i).getDestination().equals(this.instance.getLeg(j).getOrigin()))
     * 2.check the start and the end are satified for each the aircraft and crew?
     * 3.cost
     */

    public boolean isPathFeasible(boolean verbose) {
        // verbose is to remind us whether to give a hint of the result of Feasible
        boolean feas = true;
        //1.judge whether the start and end is satisfied for the Driver

        if (this.driverPath.get(0).getIdOfStartCity() != this.instance.getDepot(this.instance.getDriver(idOfDriver).getIdOfStartDepot()).getIdOfCityAsDepot()) {
            feas = false;
            if (verbose)
                System.out.println("The origin of this path is not satisfied the driver pre-designed starting depot:" + this.instance.getDriver(idOfDriver).getIdOfStartDepot());
        }
        if (this.driverPath.get(this.driverPath.size() - 1).getIdOfEndCity() != (this.instance.getDepot(this.instance.getDriver(idOfDriver).getIdOfEndDepot()).getIdOfCityAsDepot())) {
            feas = false;
            if (verbose) {
                System.out.println("The destination of this path is not satisfied the driver pre-designed ending depot: " + this.instance.getDriver(idOfDriver).getIdOfEndDepot());
            }
        }
        //2.judge whether a trip can be connected as a path for the crew
        for (int i = 0; i < this.driverPath.size() - 1; i++) {
            int idOfEndCityOfFormerTrip = this.driverPath.get(i).getIdOfEndCity();
            int idOfStartCityOfCurrentTrip = this.driverPath.get(i).getIdOfStartCity();
            if (idOfEndCityOfFormerTrip != idOfStartCityOfCurrentTrip) {
                feas = false;
                if (verbose) {
                    System.out.println("For the driver " + this.instance.getDriver(idOfDriver) + ", the destination city of trip " + this.driverPath.get(i).getIdOfEndCity()
                            + " isn't the origin city of the trip " + driverPath.get(i + 1).getIdOfStartCity());
                }
            }
        }

        //3.cost is correct be compute?
        if (this.getIdleTimeCostOfDriverPath() == Double.MAX_VALUE) {
            feas = false;
            if (verbose) {
                System.out.println("The cost is greater than the normal value.");
            }
        }
        return feas;
    }

    public boolean areTripsConsecutiveInPath(Trip first, Trip second) {
        int posFirst = this.positionInPathOfCrew(first);
        int posSecond = this.positionInPathOfCrew(second);
        if (posFirst == Integer.MAX_VALUE
                || posSecond == Integer.MAX_VALUE) {
            return false;
        }
        if (posFirst + 1 == posSecond) {
            return true;
        }
        return false;
    }


    public int positionInPathOfCrew(Trip trip) {
        for (int i = 0; i < this.driverPath.size(); i++)
            if (this.driverPath.get(i).equals(trip)) {
                return i;
            }
        return Integer.MAX_VALUE;
    }



    //here is the comment part
    public boolean isPresentInPath(Trip trip) {
        for (int i = 0; i < this.driverPath.size(); i++) {
            if (this.driverPath.get(i).equals(trip)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "PathForDriver{" +
                " idOfDriver=" + idOfDriver +
                ", driverPath=" + driverPath +
                " };"+ "\n"+
                "\t workingTimeInThisShift: " +this.getTotalWorkingTime()+ " minutes"+
               ", drivingTimeInThisShift: "+ this.getTotalDrivingTime()+" minutes";
    }


//    public static void main(String[] args) {
//        InstanceReader reader = new InstanceReader("instanceOfRoutingAndScheduling.txt");
//        Instance instance = reader.readFile();
//        //String s=instance.getCrew(0).getEndPoint();
//
//        PathForDriver pathForTheFistCrew = new PathForDriver(instance, 0);
//        pathForTheFistCrew.addTrip(instance.getTrip(6));
//        pathForTheFistCrew.addTrip(instance.getTrip(1));
//        pathForTheFistCrew.addTrip(instance.getTrip(2));
//
//        PathForDriver pathForTheSecondCrew = new PathForDriver(instance, 1);
//        pathForTheSecondCrew.addTrip(instance.getTrip(0));
//        pathForTheSecondCrew.addTrip(instance.getTrip(5));
//        pathForTheSecondCrew.addTrip(instance.getTrip(4));
//        pathForTheSecondCrew.addTrip(instance.getTrip(3));
//
//
//        System.out.println(pathForTheFistCrew);
//        System.out.println(pathForTheSecondCrew);
//
//        boolean feasible = pathForTheFistCrew.isPathFeasible(true);
//        boolean fea = pathForTheSecondCrew.isPathFeasible(true);
//
//        System.out.println("This path for the first crew is feasible? " + feasible);
//        System.out.println("This path for the second crew is feasible? " + fea);
//
//        System.out.println("The cost for crew in the first path is: " + pathForTheFistCrew.getCostOfPath());
//        System.out.println("The cost for crew in the second path is: " + pathForTheSecondCrew.getCostOfPath());
//
////        boolean isPresent = pathForTheFistCrew.isPresentInPathOfCrew(instance.getLeg(3));
////        System.out.println("This leg has shown in this path of crew? " + isPresent);
//    }
}
