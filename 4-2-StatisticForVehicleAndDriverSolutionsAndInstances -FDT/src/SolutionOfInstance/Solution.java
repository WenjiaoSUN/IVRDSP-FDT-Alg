package SolutionOfInstance;

import Instance.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class Solution {
    private Instance instance;
    private double totalCostInput;
    private LinkedList<PathForVehicle> pathForVehicles;
    private LinkedList<PathForDriver> pathForDrivers;

    private ArrayList<CombineTripInform> combineTripInformLinkedList;

    public Solution(Instance instance, double totalCostInput) {
        this.instance = instance;
        this.pathForVehicles = new LinkedList<>();
        this.pathForDrivers = new LinkedList<>();
        this.combineTripInformLinkedList = new ArrayList<>();
        this.totalCostInput = totalCostInput;

    }

    public double getTotalCostInput() {
        return totalCostInput;
    }

    public void setTotalCostInput(double totalCostInput) {
        this.totalCostInput = totalCostInput;
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

    public void addCombineTripInform(CombineTripInform combineTripInform) {
        this.combineTripInformLinkedList.add(combineTripInform);
    }

    public double getTotalChangeoverCostForAllDriver() {
        double changeoverCost = 0;
        for (int d = 0; d < this.pathForDrivers.size(); d++) {
            ArrayList<TripWithWorkingStatusAndDepartureTimeAndVehicle> pathForDriver = this.pathForDrivers.get(d).getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList();
            for (int i = 0; i < pathForDriver.size() - 1; i++) {
                int idOfFormerTrip = pathForDriver.get(i).getIdOfTrip();
                int idOfLatterTrip = pathForDriver.get(i + 1).getIdOfTrip();

                boolean workingStatusInFormerTrip = pathForDriver.get(i).getDrivingStatus();
                boolean workingStatusInLatterTrip = pathForDriver.get(i + 1).getDrivingStatus();

                int idOfVehicleInFormerTrip = pathForDriver.get(i).getIdVehicle();
                int idOfVehicleInLatterTrip = pathForDriver.get(i + 1).getIdVehicle();

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
                            boolean whetherVehiclePerformSuccessive = pathForVehicle.whetherConsecutive(idOfFormerTrip, idOfLatterTrip);
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

        return this.getFixedCostForVehicle() + this.getFixedCostForDriver();
    }

    public double getTotalIdleTimeCostOfAllVehicle() {
        double cost = 0;
        for (int v = 0; v < this.pathForVehicles.size(); v++)
            cost = cost + this.pathForVehicles.get(v).getIdleTimeCostOfVehiclePath();
        return cost;
    }

    public double getTotalIdleTimeCostOfAllDriver() {
        double cost = 0;
        for (int d = 0; d < this.pathForDrivers.size(); d++)
            cost = cost + this.pathForDrivers.get(d).getIdleTimeCostOfDriverPath();
        return cost;
    }

    public double getTotalIdleTimeCost() {
        return this.getTotalIdleTimeCostOfAllVehicle() + this.getTotalIdleTimeCostOfAllDriver();
    }

    public double getTotalCost() {
        return this.getTotalFixedCost() + this.getTotalIdleTimeCost() + this.getTotalChangeoverCostForAllDriver();
    }

    public boolean isCostSame(){
        boolean isCostSame=true;
        // 1 check the total cost
        double totalCostInput = this.getTotalCostInput();
        double costByCalculate = this.getTotalCost();
        if (totalCostInput != costByCalculate) {
            isCostSame=false;
        }
        return isCostSame;
    }


    public boolean isSolutionFeasible(boolean verbose) {
        // verbose is to remind us whether to give a hint of the result of Feasible
        boolean feas = true;
        // 1 check the total cost
        // 2 check each path is feasible
        // 3 check each trip at least one driver
        //4 check each trip is visited as required nbVehicles
        //5 check exactly one driving driver
        //6 check leading vehicle
        //7 each depot have balance number of vehicles
        // 8 short connection for driver not change vehicle (driving status-only for the normal task, not for the combined tasks)
//        8.1 normal-normal (keep driving status)
//        8.2 normal-combine  or combine to normal (keep driving status)
//        8.3 combine-combine (keep driving status)


        // 1 check the total cost
        double totalCostInput = this.getTotalCostInput();
        double costByCalculate = this.getTotalCost();
        if (totalCostInput != costByCalculate) {
            feas = false;
            if (verbose) {
                System.out.println(" the is cost by calculating is " + costByCalculate + " which is not the same as input " + totalCostInput);
            }
        }


        //2.1 judge all the paths for the vehicles are feasible
        for (int i = 0; i < this.pathForVehicles.size(); i++) {
            if (!this.pathForVehicles.get(i).whetherFeasible(false)) {
                feas = false;
                if (verbose)
                    System.out.println("The path of vehicle" + this.pathForVehicles.get(i).getIdOfVehicle() + " is not feasible: ");
            }
        }
        // 2.2 check each path is feasible
        for (int i = 0; i < this.pathForDrivers.size(); i++) {
            if (!this.pathForDrivers.get(i).whetherFeasible(false)) {
                feas = false;
                if (verbose) {
                    System.out.println("The path of Driver " + this.pathForDrivers.get(i).getIdOfDriver() + " is not feasible.");
                }
            }
        }

        //3 check all the task has been visited at least once in driver path
        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            boolean isPresentedInPathOfDriver = false;
            for (int d = 0; d < this.pathForDrivers.size(); d++) {
                if (this.pathForDrivers.get(d).whetherPresent(i)) {
                    isPresentedInPathOfDriver = true;
                    break;
                }
            }
            if (isPresentedInPathOfDriver == false) {
                feas = false;
                if (verbose) {
                    System.out.println("The task " + i + " has not been visited in any driver schedule");
                }
            }
        }

        //4 check each trip is visited as required nbVehicles
        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            int nbVehicleNeed = instance.getTrip(i).getNbVehicleNeed();
            int nbPresentInVehicleRoute = 0;
            for (int v = 0; v < this.pathForVehicles.size(); v++) {
                if (this.pathForVehicles.get(v).whetherPresent(i)) {
                    nbPresentInVehicleRoute++;

                }
            }
            if (nbVehicleNeed != nbPresentInVehicleRoute) {
                feas = false;
                if (verbose) {
                    System.out.println("The task " + i + " has not been visited as need. ");
                }
            }
        }


        // 5 exactly one driving driver
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int nbVehicleNeed = trip.getNbVehicleNeed();
            if (nbVehicleNeed > 1.99) {
                int idDrivingDriverInput=Integer.MAX_VALUE;
                int idLeadingVehicleInput=Integer.MAX_VALUE;

                for(int c=0; c<this.combineTripInformLinkedList.size();c++){
                    CombineTripInform combineTrip=this.combineTripInformLinkedList.get(c);
                    if(combineTrip.getIdTrip()==trip.getIdOfTrip()){
                        idDrivingDriverInput=combineTrip.getIdDrivingDriver();
                        idLeadingVehicleInput=combineTrip.getIdLeadingVehicle();
                    }
                }

                int nbDriving = 0;
                int idDriverDrivingByCheckPath=Integer.MAX_VALUE;
                int idVehicleLeadingByCheckPath=Integer.MAX_VALUE;

                int nbVehicleRelated=0;
                for (int d = 0; d < this.pathForDrivers.size(); d++) {
                    PathForDriver pathForDriver = this.pathForDrivers.get(d);
                    boolean whetherPresentCombine = pathForDriver.whetherPresent(trip.getIdOfTrip());
                    if (whetherPresentCombine) {
                        for (int t = 0; t < pathForDriver.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size(); t++) {
                            TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatusAndDepartureTimeAndVehicle
                                    = pathForDriver.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                            if (tripWithWorkingStatusAndDepartureTimeAndVehicle.getIdOfTrip() == trip.getIdOfTrip() && tripWithWorkingStatusAndDepartureTimeAndVehicle.getDrivingStatus() == true) {
                                nbDriving++;
                                idDriverDrivingByCheckPath=pathForDriver.getIdOfDriver();
                            }
                        }
                    }
                }
                if (nbDriving!= 1) {
                    feas = false;
                    if(verbose){
                        System.out.println("combine trip_"+trip.getIdOfTrip()+" is not have exactly one driving driver, as the nbDriving is "+nbDriving);
                    }
                }
                if(idDriverDrivingByCheckPath!=idDrivingDriverInput){
                    System.out.println("combine trip_"+trip.getIdOfTrip()+" of driving driver by checking is "+idDriverDrivingByCheckPath
                            +" which is not the same as input "+idDrivingDriverInput);
                }

                //6 check leading vehicle showed up in the vehicle path
                for(int v=0;v<this.pathForVehicles.size();v++){
                    PathForVehicle pathForVehicle=this.pathForVehicles.get(v);
                    int idVehicle=pathForVehicle.getIdOfVehicle();
                    if(idVehicle==idLeadingVehicleInput) {
                        boolean whetherPresent = pathForVehicle.whetherPresent(trip.getIdOfTrip());
                        if (!whetherPresent) {
                            feas = false;
                            if(verbose){
                                System.out.println("combine trip_"+trip.getIdOfTrip()+" is not show up the one leading vehicle path_ "+idLeadingVehicleInput);
                            }
                        }
                    }
                }



            }
        }

        //7 check the nbVehicle in each depot
        for(int k=0;k<instance.getNbDepots();k++){
            Depot depot=instance.getDepot(k);
            int idCityAsDepot=depot.getIdOfCityAsDepot();
            int nbVehicleStartInDepot=0;
            int nbVehicleEndInDepot=0;
            for(int v=0;v<this.pathForVehicles.size();v++){
                PathForVehicle pathForVehicle= this.pathForVehicles.get(v);
                int idStartCity=pathForVehicle.getStartCityForPath();
                if(idStartCity==idCityAsDepot){
                    nbVehicleStartInDepot++;

                }
                int idEndCity=pathForVehicle.getEndCityForPath();
                if(idEndCity==idCityAsDepot){
                    nbVehicleEndInDepot++;
                }
            }
            if(nbVehicleEndInDepot!=nbVehicleStartInDepot){
                feas=false;
                if(verbose){
                    System.out.println("nbVehicle start in depot_"+depot.getIdOfDepot()+" is "+nbVehicleStartInDepot+" which is not the same number of vehicles "+nbVehicleEndInDepot);
                }
            }
        }


        //8 no change vehicle when the time is limited for the driving driver
        //  check 1 normal to normal
        //  check 2 normal to combine or combine to normal check as long as the lead vehicle show up is enough
        //  combine only check those in two trip is driving (we dont care how passenger change vehicle, because T^DS define allow passener to enter or exsit vehicle)
        for(int d=0;d<this.pathForDrivers.size();d++){
            PathForDriver pathForDriver =this.pathForDrivers.get(d);
            for(int t=0;t<pathForDriver.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size()-1;t++){
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatusAndDepartureTimeAndVehicle1= pathForDriver.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                int Q1=instance.getTrip(tripWithWorkingStatusAndDepartureTimeAndVehicle1.getIdOfTrip()).getNbVehicleNeed();
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatusAndDepartureTimeAndVehicle2=pathForDriver.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t+1);
                int Q2=instance.getTrip(tripWithWorkingStatusAndDepartureTimeAndVehicle2.getIdOfTrip()).getNbVehicleNeed();
                int conTime=tripWithWorkingStatusAndDepartureTimeAndVehicle2.getDepartureTime()-tripWithWorkingStatusAndDepartureTimeAndVehicle1.getDepartureTime()-tripWithWorkingStatusAndDepartureTimeAndVehicle1.getDuration();
                if(conTime<instance.getShortConnectionTimeForDriver()){
                    System.out.println("In driver schedule_"+pathForDriver.getIdOfDriver()+" shortConnection trip_"+tripWithWorkingStatusAndDepartureTimeAndVehicle1.getIdOfTrip()+" with trip_"
                            +tripWithWorkingStatusAndDepartureTimeAndVehicle2.getIdOfTrip()+"with connection time "+conTime);
                    int idVehicle1=tripWithWorkingStatusAndDepartureTimeAndVehicle1.getIdVehicle();
                    int idVehicle2=tripWithWorkingStatusAndDepartureTimeAndVehicle2.getIdVehicle();
                    if(Q1==1&&Q2==1) {
                        if (idVehicle1 != idVehicle2) {
                            feas = false;
                            if (verbose) {
                                System.out.println("In driver schedule_" + pathForDriver.getIdOfDriver() + " two normal shortConnection trip_" + tripWithWorkingStatusAndDepartureTimeAndVehicle1.getIdOfTrip() + " with trip_"
                                        + tripWithWorkingStatusAndDepartureTimeAndVehicle2.getIdOfTrip() + "with connection time " + conTime + " is performed by different vehicle_" + idVehicle1
                                        + " and vehicle_" + idVehicle2);
                            }
                        }
                    }
//                    else if((Q1==1&&Q2==2)){
//                        boolean whetherDriving1= tripWithWorkingStatusAndDepartureTimeAndVehicle1.getDrivingStatus();
//                        boolean whetherDriving2=tripWithWorkingStatusAndDepartureTimeAndVehicle2.getDrivingStatus();
//                        if (idVehicle1 != idVehicle2&&whetherDriving1==true&&whetherDriving2==true){
//                            feas = false;
//                            if (verbose) {
//                                System.out.println("In driver schedule_" + pathForDriver.getIdOfDriver() + "combined task related shortConnection trip_" + tripWithWorkingStatusAndDepartureTimeAndVehicle1.getIdOfTrip() + " with trip_"
//                                        + tripWithWorkingStatusAndDepartureTimeAndVehicle2.getIdOfTrip() + "with connection time " + conTime + " is performed by different vehicle_" + idVehicle1
//                                        + " and vehicle_" + idVehicle2);
//                            }
//                        }
//                    }
//                    else if(Q1==2&&Q2==1){
//                        boolean whetherDriving1= tripWithWorkingStatusAndDepartureTimeAndVehicle1.getDrivingStatus();
//                        boolean whetherDriving2=tripWithWorkingStatusAndDepartureTimeAndVehicle2.getDrivingStatus();
//                        if (idVehicle1 != idVehicle2&&whetherDriving1==true&&whetherDriving2==true){
//                            feas = false;
//                            if (verbose) {
//                                System.out.println("In driver schedule_" + pathForDriver.getIdOfDriver() + "combined task related shortConnection trip_" + tripWithWorkingStatusAndDepartureTimeAndVehicle1.getIdOfTrip() + " with trip_"
//                                        + tripWithWorkingStatusAndDepartureTimeAndVehicle2.getIdOfTrip() + "with connection time " + conTime + " is performed by different vehicle_" + idVehicle1
//                                        + " and vehicle_" + idVehicle2);
//                            }
//                        }
//                    }
//                    else {
//
//                    }
                    //combine to combine require same two vehicle
//                    else {
//                        boolean whetherDriving1= tripWithWorkingStatusAndDepartureTimeAndVehicle1.getDrivingStatus();
//                        boolean whetherDriving2=tripWithWorkingStatusAndDepartureTimeAndVehicle2.getDrivingStatus();
//                        if (idVehicle1 != idVehicle2&&whetherDriving1==true&&whetherDriving2==true){
//                            feas = false;
//                            if (verbose) {
//                                System.out.println("In driver schedule_" + pathForDriver.getIdOfDriver() + "combined task related shortConnection trip_" + tripWithWorkingStatusAndDepartureTimeAndVehicle1.getIdOfTrip() + " with trip_"
//                                        + tripWithWorkingStatusAndDepartureTimeAndVehicle2.getIdOfTrip() + "with connection time " + conTime + " is performed by different vehicle_" + idVehicle1
//                                        + " and vehicle_" + idVehicle2);
//                            }
//                        }
//
//                    }

                }


            }
        }

        //9 same departure for vehicle and driver
        for(int i=0;i<instance.getNbTrips();i++){
            int idTrip=instance.getTrip(i).getIdOfTrip();
            int depTimeInDriverSchedule=Integer.MAX_VALUE;
            int depTimeInVehicleRoute=Integer.MAX_VALUE;
            for(int d=0;d<this.pathForDrivers.size();d++){
                PathForDriver pathForDriver=this.pathForDrivers.get(d);
                if(pathForDriver.whetherPresent(idTrip)){
                    for(int t=0;t<pathForDriver.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size();t++) {
                        TripWithWorkingStatusAndDepartureTimeAndVehicle tripInDriverPath =pathForDriver.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                        if(tripInDriverPath.getIdOfTrip()==idTrip){
                            depTimeInDriverSchedule=tripInDriverPath.getDepartureTime();
                        }
                    }
                }
            }


            for(int v=0;v<this.pathForVehicles.size();v++){
                PathForVehicle pathForVehicle=this.pathForVehicles.get(v);
                if(pathForVehicle.whetherPresent(idTrip)){
                    for(int t=0;t<pathForVehicle.getTripWithDepartureTimeArrayList().size();t++) {
                        TripWithDepartureTime tripWithDepartureTime =pathForVehicle.getTripWithDepartureTimeArrayList().get(t);
                        if(tripWithDepartureTime.getIdOfTrip()==idTrip){
                            depTimeInVehicleRoute=tripWithDepartureTime.getStartingTime();
                        }
                    }
                }
            }

            if(depTimeInDriverSchedule!=depTimeInVehicleRoute){
                System.out.println("departure time for trip_"+idTrip+" which is "+depTimeInDriverSchedule+" in driver schedule "+ " is different in vehicle route which is "+depTimeInVehicleRoute);
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
            writer.println("The mini cost is including :" + this.getTotalCost());
            writer.println("The fixed cost for vehicle and driver :" + this.getTotalFixedCost());
            writer.println("The idle cost for vehicle and driver :" + this.getTotalIdleTimeCost());
            writer.println("The idle cost for all drivers :" + this.getTotalIdleTimeCostOfAllDriver());
            writer.println("THe idle cost for all vehicles " + this.getTotalIdleTimeCostOfAllVehicle());
            writer.println("The changover cost for all drivers:" + this.getTotalChangeoverCostForAllDriver());

            for (PathForVehicle pathForVehicle : this.pathForVehicles) {
                writer.println(pathForVehicle);
            }

            for (PathForDriver pathForDriver : this.pathForDrivers) {
                writer.println(pathForDriver);
            }

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }





    //********************following here are for statistics
    //statistics 1
    public int getNbTripsWithPassenger() {
        int nbPassengers = 0;
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
           PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size(); t++) {
                boolean whetherDrive = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t).getDrivingStatus();
                if (!whetherDrive) {
                    nbPassengers++;

                }
            }

        }
        return nbPassengers;
    }

    //statistics 2
    public int getNbTripsWithPassenger_Normal() {
        int nbPassengersInNormalTrip = 0;
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size(); t++) {
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                boolean whetherDrive = tripWithWorkingStatus.getDrivingStatus();
                int nbVehicleNeed = tripWithWorkingStatus.getNbVehicleNeed();
                if (!whetherDrive && nbVehicleNeed == 1) {
                    nbPassengersInNormalTrip++;

                }
            }
        }
        return nbPassengersInNormalTrip;
    }

    //statistics 3
    public int getNbTripsWithPassenger_Combine() {
        int nbPassengersInCombineTrip = 0;
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size(); t++) {
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                boolean whetherDrive = tripWithWorkingStatus.getDrivingStatus();
                int nbVehicleNeed = tripWithWorkingStatus.getNbVehicleNeed();
                if (!whetherDrive && nbVehicleNeed > 1) {
                    nbPassengersInCombineTrip++;

                }
            }
        }
        return nbPassengersInCombineTrip;
    }

    //statistics 4 on average  nbTrips performed per driver
    public double getNbTripsPerDriver() {
        int nbDrivers = this.pathForDrivers.size();
        double nbTotalTrips = instance.getNbTrips();
        double nbTripsPerDriver = (double) (nbTotalTrips / nbDrivers);
        return nbTripsPerDriver;
    }

    //statistics 5 total working time per driver
    public double getWorkingTimePerDriver() {
        double totalWorkingTime = 0;
        double workingTimePerDriver = 0;
        int nbDrivers = this.pathForDrivers.size();
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);

            int nbTripsForDriverS = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size();
            TripWithWorkingStatusAndDepartureTimeAndVehicle trip_1=driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(0);
            TripWithWorkingStatusAndDepartureTimeAndVehicle trip_n=driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(nbTripsForDriverS-1);
            double duration_n=trip_n.getDuration();
            double endingTime= trip_n.getDepartureTime()+ duration_n;
            double startingTime=trip_1.getDepartureTime();

            totalWorkingTime=totalWorkingTime+(endingTime-startingTime);

        }
        workingTimePerDriver = (double) (totalWorkingTime / nbDrivers);
        return workingTimePerDriver;
    }

    //statistics 6 driving time perDriver
    public double getDrivingTimePerDriver() {
        double totalDriverTime = 0;
        double drivingTimePerDriver = 0;
        int nbDrivers = this.pathForDrivers.size();
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
           PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size(); t++) {
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithDriveInfos = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                boolean whetherDrive = tripWithDriveInfos.getDrivingStatus();
                if (whetherDrive == true) {
                    double driveTimeTForS = tripWithDriveInfos.getDuration();
                    totalDriverTime = totalDriverTime + driveTimeTForS;
                }

            }
        }
        drivingTimePerDriver = (double) (totalDriverTime / nbDrivers);
        return drivingTimePerDriver;
    }

    //statistics 7 passengerTimePerDriver
    public double getPassengerTimePerDriver() {
        double totalPassengerTime = 0;
        double passengerTimePerDriver = 0;
        int nbDrivers = this.pathForDrivers.size();
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size(); t++) {
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithDriveInfos =driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                boolean whetherDrive = tripWithDriveInfos.getDrivingStatus();
                if (whetherDrive == false) {
                    double passengerTimeTForS = tripWithDriveInfos.getDuration();
                    totalPassengerTime = totalPassengerTime + passengerTimeTForS;
                }

            }
        }
        passengerTimePerDriver = (double) (totalPassengerTime / nbDrivers);
        return passengerTimePerDriver;
    }

    //statistics 8 idleTimePerDriver
    public double getIdleTimePerDriver() {
        double totalIdleTime = 0;
        double idleTimePerDriver = 0;
        int nbDrivers = this.pathForDrivers.size();
        for (int s = 0; s <  this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size() - 1; t++) {
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithDriveInfos_F =driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                int j = t + 1;
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithDriveInfos_S = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(j);
                double previousTripEndTime = tripWithDriveInfos_F.getDepartureTime()+tripWithDriveInfos_F.getDuration();
                double latterTripStartTime = tripWithDriveInfos_S.getDepartureTime();
                double idleTime = latterTripStartTime - previousTripEndTime;
                totalIdleTime = totalIdleTime + idleTime;
            }
        }
        idleTimePerDriver = (double) (totalIdleTime / nbDrivers);
        return idleTimePerDriver;
    }

    //statistics 9 nbChangeOver
    public double getNbChangeOversPerDriver() {
        double nbChangeOverPerDriver = 0;
        double nbTotalChangeOver = 0;
        int nbDrivers = this.pathForDrivers.size();
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size()- 1; t++) {
                //formerTripInformation
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus_former = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                int idTripInDriverSchedule_former = tripWithWorkingStatus_former.getIdOfTrip();
                Trip tripInDriverSchedule_former = instance.getTrip(idTripInDriverSchedule_former);
                boolean whetherDrive_former = tripWithWorkingStatus_former.getDrivingStatus();
                int idVehiclePerformDriverSchedule_former = Integer.MAX_VALUE;

                //latterTripInformation
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus_latter = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t + 1);
                int idTripInDriverSchedule_latter = tripWithWorkingStatus_latter.getIdOfTrip();
                Trip tripInDriverSchedule_latter = instance.getTrip(idTripInDriverSchedule_latter);
                boolean whetherDriver_latter = tripWithWorkingStatus_latter.getDrivingStatus();
                int idVehiclePerformDriverSchedule_latter = Integer.MAX_VALUE;
// here I stopped
                for (int r = 0; r < this.pathForVehicles.size(); r++) {
                    PathForVehicle vehicleRoute = this.pathForVehicles.get(r);
                    boolean isTripInVehicleRoute_former = vehicleRoute.whetherPresent(tripInDriverSchedule_former.getIdOfTrip());
                    boolean isTripInVehicleRoute_latter = vehicleRoute.whetherPresent(tripInDriverSchedule_latter.getIdOfTrip());

                    //Case1: normal trip to normal trip
                    if (tripInDriverSchedule_former.getNbVehicleNeed() == 1 && tripInDriverSchedule_latter.getNbVehicleNeed() == 1) {
                        if (isTripInVehicleRoute_former) {
                            idVehiclePerformDriverSchedule_former = r;
                        }
                        if (isTripInVehicleRoute_latter) {
                            idVehiclePerformDriverSchedule_latter = r;
                        }
                    }
                    //Case2: combine trip to normal trip
                    else if (tripInDriverSchedule_former.getNbVehicleNeed() > 1 && tripInDriverSchedule_latter.getNbVehicleNeed() == 1) {
                        //check the combine list the idvehicle in combined trip should be the leading vehicle (combine trip should be the current trip we care)
                        for (int c = 0; c < this.combineTripInformLinkedList.size(); c++) {
                            CombineTripInform combinedInformation = this.combineTripInformLinkedList.get(c);

                            int idCombineTrip = combinedInformation.getIdTrip();
                            int idLeadingVehicle = combinedInformation.getIdLeadingVehicle();
                            if (idCombineTrip == idTripInDriverSchedule_former) {
                                idVehiclePerformDriverSchedule_former = idLeadingVehicle;
                            }
                        }
                        if (isTripInVehicleRoute_latter) {
                            idVehiclePerformDriverSchedule_latter = r;
                        }

                    }
                    // Case3: normal trip to combine trip
                    else if (tripInDriverSchedule_former.getNbVehicleNeed() == 1 && tripInDriverSchedule_latter.getNbVehicleNeed() > 1) {
                        //check the combine list the idvehicle in combined trip should be the leading vehicle (combine trip should be the current trip we care)
                        for (int c = 0; c < this.combineTripInformLinkedList.size(); c++) {
                            CombineTripInform combinedInformation = this.combineTripInformLinkedList.get(c);
                            int idCombineTrip = combinedInformation.getIdTrip();
                            int idLeadingVehicle = combinedInformation.getIdLeadingVehicle();
                            if (idCombineTrip == idTripInDriverSchedule_latter) {
                                idVehiclePerformDriverSchedule_former = idLeadingVehicle;
                            }
                        }
                        if (isTripInVehicleRoute_former) {
                            idVehiclePerformDriverSchedule_latter = r;
                        }

                    }
                    // Case4: combine trip to combine trip
                    else if (tripInDriverSchedule_former.getNbVehicleNeed() > 1 && tripInDriverSchedule_latter.getNbVehicleNeed() > 1) {
                        for (int c = 0; c <this.combineTripInformLinkedList.size(); c++) {
                            CombineTripInform combinedInformation = this.combineTripInformLinkedList.get(c);
                            int idCombineTrip = combinedInformation.getIdTrip();
                            int idLeadingVehicle = combinedInformation.getIdLeadingVehicle();
                            if (idCombineTrip == idTripInDriverSchedule_former) {
                                idVehiclePerformDriverSchedule_former = idLeadingVehicle;
                            }
                            if (idCombineTrip == idTripInDriverSchedule_latter) {
                                idVehiclePerformDriverSchedule_latter = idLeadingVehicle;
                            }
                        }
                    }
                }

                if (whetherDriver_latter != whetherDrive_former || idVehiclePerformDriverSchedule_latter != idVehiclePerformDriverSchedule_former) {
                    nbTotalChangeOver = nbTotalChangeOver + 1;
                }
                if (whetherDriver_latter == false && whetherDrive_former == false && idVehiclePerformDriverSchedule_latter != idVehiclePerformDriverSchedule_former) {
                    nbTotalChangeOver = nbTotalChangeOver - 1;
                }

            }
        }
        nbChangeOverPerDriver = (nbTotalChangeOver / nbDrivers);

        return nbChangeOverPerDriver;
    }

    //statistics 10 nbChangeOver
    public double getNbChangeOversPerDriver_ChangeStatus() {
        double nbChangeOverPerDriver_ChangeStatus = 0;
        double nbTotalChangeOver_ChangeStatus = 0;
        int nbDrivers = this.pathForDrivers.size();
        for (int s = 0; s <this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size()- 1; t++) {
                //formerTripInformation
               TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus_former = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                boolean whetherDrive_former = tripWithWorkingStatus_former.getDrivingStatus();

                //latterTripInformation
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus_latter = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t + 1);
                boolean whetherDriver_latter = tripWithWorkingStatus_latter.getDrivingStatus();

                if (whetherDriver_latter != whetherDrive_former) {
                    nbTotalChangeOver_ChangeStatus = nbTotalChangeOver_ChangeStatus + 1;
                }

            }
        }
        nbChangeOverPerDriver_ChangeStatus = (nbTotalChangeOver_ChangeStatus / nbDrivers);
        return nbChangeOverPerDriver_ChangeStatus;
    }

    //statistics 11 nbChangeOver
    public double getNbChangeOversPerDriver_ChangeVehicle() {
        double nbChangeOverPerDriver_ChangeVehicle = 0;
        double nbTotalChangeOver_ChangeVehicle = 0;
        double nbDrivers = this.pathForDrivers.size();
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size() - 1; t++) {
                //formerTripInformation
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus_former = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                int idTripInDriverSchedule_former = tripWithWorkingStatus_former.getIdOfTrip();
                Trip tripInDriverSchedule_former = instance.getTrip(idTripInDriverSchedule_former);
                boolean whetherDrive_former = tripWithWorkingStatus_former.getDrivingStatus();
                int idVehiclePerformDriverSchedule_former = Integer.MAX_VALUE;

                //latterTripInformation
                TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatus_latter = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t + 1);
                int idTripInDriverSchedule_latter = tripWithWorkingStatus_latter.getIdOfTrip();
                Trip tripInDriverSchedule_latter = instance.getTrip(idTripInDriverSchedule_latter);
                boolean whetherDriver_latter = tripWithWorkingStatus_latter.getDrivingStatus();
                int idVehiclePerformDriverSchedule_latter = Integer.MAX_VALUE;

                for (int r = 0; r < this.pathForVehicles.size(); r++) {
                    PathForVehicle vehicleRoute = this.pathForVehicles.get(r);
                    boolean isTripInVehicleRoute_former = vehicleRoute.whetherPresent(tripInDriverSchedule_former.getIdOfTrip());
                    boolean isTripInVehicleRoute_latter = vehicleRoute.whetherPresent(tripInDriverSchedule_latter.getIdOfTrip());

                    //Case1: normal trip to normal trip

                    if (tripInDriverSchedule_former.getNbVehicleNeed() == 1 && tripInDriverSchedule_latter.getNbVehicleNeed() == 1) {
                        if (isTripInVehicleRoute_former) {
                            idVehiclePerformDriverSchedule_former = r;
                        }
                        if (isTripInVehicleRoute_latter) {
                            idVehiclePerformDriverSchedule_latter = r;
                        }
                    }
                    //Case2: combine trip to normal trip
                    else if (tripInDriverSchedule_former.getNbVehicleNeed() > 1 && tripInDriverSchedule_latter.getNbVehicleNeed() == 1) {
                        //check the combine list the idvehicle in combined trip should be the leading vehicle (combine trip should be the current trip we care)
                        for (int c = 0; c < this.combineTripInformLinkedList.size(); c++) {
                            CombineTripInform combinedInformation = this.combineTripInformLinkedList.get(c);

                            int idCombineTrip = combinedInformation.getIdTrip();
                            int idLeadingVehicle = combinedInformation.getIdLeadingVehicle();
                            if (idCombineTrip == idTripInDriverSchedule_former) {
                                idVehiclePerformDriverSchedule_former = idLeadingVehicle;
                            }
                        }
                        if (isTripInVehicleRoute_latter) {
                            idVehiclePerformDriverSchedule_latter = r;
                        }

                    }
                    // Case3: normal trip to combine trip
                    else if (tripInDriverSchedule_former.getNbVehicleNeed() == 1 && tripInDriverSchedule_latter.getNbVehicleNeed() > 1) {
                        //check the combine list the idvehicle in combined trip should be the leading vehicle (combine trip should be the current trip we care)
                        for (int c = 0; c <  this.combineTripInformLinkedList.size(); c++) {
                            CombineTripInform combinedInformation = this.combineTripInformLinkedList.get(c);

                            int idCombineTrip = combinedInformation.getIdTrip();
                            int idLeadingVehicle = combinedInformation.getIdLeadingVehicle();
                            if (idCombineTrip == idTripInDriverSchedule_latter) {
                                idVehiclePerformDriverSchedule_former = idLeadingVehicle;
                            }
                        }
                        if (isTripInVehicleRoute_former) {
                            idVehiclePerformDriverSchedule_latter = r;
                        }

                    }
                    // Case4: combine trip to combine trip
                    else if (tripInDriverSchedule_former.getNbVehicleNeed() > 1 && tripInDriverSchedule_latter.getNbVehicleNeed() > 1) {
                        for (int c = 0; c < this.combineTripInformLinkedList.size(); c++) {
                            CombineTripInform combinedInformation = this.combineTripInformLinkedList.get(c);

                            int idCombineTrip = combinedInformation.getIdTrip();

                            int idLeadingVehicle = combinedInformation.getIdLeadingVehicle();
                            if (idCombineTrip == idTripInDriverSchedule_former) {
                                idVehiclePerformDriverSchedule_former = idLeadingVehicle;
                            }
                            if (idCombineTrip == idTripInDriverSchedule_latter) {
                                idVehiclePerformDriverSchedule_latter = idLeadingVehicle;
                            }
                        }
                    }
                }


                if (idVehiclePerformDriverSchedule_latter != idVehiclePerformDriverSchedule_former) {
                    nbTotalChangeOver_ChangeVehicle = nbTotalChangeOver_ChangeVehicle + 1;
                }
                if (whetherDriver_latter == false && whetherDrive_former == false && idVehiclePerformDriverSchedule_latter != idVehiclePerformDriverSchedule_former) {
                    nbTotalChangeOver_ChangeVehicle = nbTotalChangeOver_ChangeVehicle - 1;
                }

            }
        }
        nbChangeOverPerDriver_ChangeVehicle = (nbTotalChangeOver_ChangeVehicle / nbDrivers);

        return nbChangeOverPerDriver_ChangeVehicle;
    }

    public double getShortestIdleTime() {
        double shortestIdleTime = Double.MAX_VALUE;
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size() - 1; t++) {
              TripWithWorkingStatusAndDepartureTimeAndVehicle fistTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                TripWithWorkingStatusAndDepartureTimeAndVehicle secondTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t + 1);
                double idleTime = secondTrip.getDepartureTime() - (fistTrip.getDepartureTime()+fistTrip.getDuration());
                if (idleTime <=shortestIdleTime) {
                    shortestIdleTime = idleTime;
                }
            }

        }
        return shortestIdleTime;
    }

    public double getLongestIdleTime() {
        double longestIdleTime = 0;
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            for (int t = 0; t < driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size() - 1; t++) {
                TripWithWorkingStatusAndDepartureTimeAndVehicle fistTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t);
                TripWithWorkingStatusAndDepartureTimeAndVehicle secondTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(t + 1);
                double idleTime = secondTrip.getDepartureTime() - (fistTrip.getDepartureTime()+fistTrip.getDuration());
                if (idleTime >= longestIdleTime) {
                    longestIdleTime = idleTime;
                }
            }

        }
        return longestIdleTime;
    }


    public double getShortestWorkingTime() {
        double shortestWorkingTime = Double.MAX_VALUE;
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            double workingTime = driverSchedule.getWorkingTime();
            if (workingTime <= shortestWorkingTime) {
                shortestWorkingTime = workingTime;
            }
        }
        return shortestWorkingTime;
    }

    public double getLongestWorkingTime() {
        double longestWorkingTime = 0;
        for (int s = 0; s < this.pathForDrivers.size(); s++) {
            PathForDriver driverSchedule = this.pathForDrivers.get(s);
            double workingTime = driverSchedule.getWorkingTime();
            System.out.println("Schedule_" + s + " workingTime" + workingTime);
            if (workingTime >= longestWorkingTime) {
                longestWorkingTime = workingTime;
            }
        }
        return longestWorkingTime;
    }


    @Override
    public String toString() {
        return "Solution{" +
                "totalCostInput=" + totalCostInput +
                ", pathForVehicles=" + pathForVehicles +
                ", pathForDrivers=" + pathForDrivers +
                ", combineTripInformLinkedList=" + combineTripInformLinkedList +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW1.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println("instance: "+instance);

        SolutionReader solutionReader= new SolutionReader("solution_inst_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW1.txt",instance);
        Solution solution=solutionReader.readFile();
        System.out.println(solution);
        System.out.println(" whether solution is feasible "+solution.isSolutionFeasible(true));
    }
}
