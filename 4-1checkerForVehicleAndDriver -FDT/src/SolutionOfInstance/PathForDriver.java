
package SolutionOfInstance;
import Instance.Instance;
import Instance.Depot;

import java.util.ArrayList;

public class PathForDriver {
    private Instance instance;
    private int idOfDriver;
    private ArrayList<TripWithWorkingStatusAndDepartureTimeAndVehicle> tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList;

    public PathForDriver(Instance instance, int idOfDriver) {
        this.instance = instance;
        this.idOfDriver = idOfDriver;
        this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList = new ArrayList<TripWithWorkingStatusAndDepartureTimeAndVehicle>();

    }
    public int getIdOfDriver() {
        return idOfDriver;
    }

    public ArrayList<TripWithWorkingStatusAndDepartureTimeAndVehicle> getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList() {
        return tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList;
    }

    public void addTripInDriverPath(TripWithWorkingStatusAndDepartureTimeAndVehicle TripWithWorkingStatusAndDepartureTimeAndVehicle) {
        this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.add(TripWithWorkingStatusAndDepartureTimeAndVehicle);
    }

    public double getIdleTimeCostOfDriverPath() {
        double idleTimeCost = 0;
        for(int i = 0; i<this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size()-1; i++){
            TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatusAndDepartureTimeAndVehicle1=this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i);
            TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatusAndDepartureTimeAndVehicle2=this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i+1);
            int idleTime=tripWithWorkingStatusAndDepartureTimeAndVehicle2.getDepartureTime()-tripWithWorkingStatusAndDepartureTimeAndVehicle1.getDepartureTime()-tripWithWorkingStatusAndDepartureTimeAndVehicle1.getDuration();
            idleTimeCost=idleTimeCost+idleTime*instance.getIdleTimeCostForDriverPerUnit();
        }
        return idleTimeCost;
    }

    public int getWorkingTime(){
        TripWithWorkingStatusAndDepartureTimeAndVehicle first=this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(0);
        TripWithWorkingStatusAndDepartureTimeAndVehicle last=this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size()-1);
        int workingTime=last.getDepartureTime()+last.getDuration()-first.getDepartureTime();
        return workingTime;
    }

    public int getDrivingTime(){
        int drivingTime=0;
        for(int i=0;i<this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size();i++){
            boolean whetherDriving=this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i).getDrivingStatus();
            int duration=this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i).getDuration();
            if(whetherDriving){
                drivingTime=drivingTime+duration;
            }

        }
        return drivingTime;
    }

    public TripWithWorkingStatusAndDepartureTimeAndVehicle getFirstTripWithStatusAndDepartureTimeAndIdVehicle(){
        return this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(0);
    }

    public TripWithWorkingStatusAndDepartureTimeAndVehicle getLastTripWithStatusAndDepartureTimeAndIdVehicle(){
        return this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size()-1);
    }

    public boolean whetherPresent(int idTrip){
        boolean whetherPresent=false;
        for(int i=0;i<this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size();i++){
            TripWithWorkingStatusAndDepartureTimeAndVehicle trip=this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i);
            if(trip.getIdOfTrip()==idTrip){
                whetherPresent=true;
            }
        }
        return whetherPresent;
    }

    public boolean whetherFeasible(boolean whetherSayReason){
        //1 working time limitation
        // 2 driving time limitation
        // 3 same departure and ending depot
        // 4connection time greater than minPlan
        // 5 connection time less than maxPlan
        // 6 one trip is within time window
        boolean whetherFeasible=true;

        //1 working time limitation
        int workingTime=this.getWorkingTime();
        if(workingTime>this.instance.getMaxWorkingTime()){
            whetherFeasible=false;
            if(whetherSayReason){
                System.out.println("Working time of driver_"+this.idOfDriver+" is: "+workingTime+" greater than limitation "+instance.getMaxWorkingTime());
            }
        }

        // 2 driving time limitation
        int drivingTime=this.getDrivingTime();
        if(drivingTime>this.instance.getMaxDrivingTime()){
            whetherFeasible=false;
            if(whetherSayReason){
                System.out.println("Driving time of driver_"+this.idOfDriver+" is: "+drivingTime+ "  greater than limitation "+instance.getMaxDrivingTime());
            }
        }

        // 3 same departure and ending depot
        int idStartDepot=Integer.MAX_VALUE;
        int idEndDepot=Integer.MAX_VALUE;
        for(int k=0;k<instance.getNbDepots();k++){
            int idFirstStartCity=this.getFirstTripWithStatusAndDepartureTimeAndIdVehicle().getIdOfStartCity();
            int idLastEndCity=this.getLastTripWithStatusAndDepartureTimeAndIdVehicle().getIdOfEndCity();
            Depot depot=instance.getDepot(k);
            if(depot.getIdOfCityAsDepot()==idFirstStartCity){
                idStartDepot=k;
            }
            if(depot.getIdOfCityAsDepot()==idLastEndCity){
                idEndDepot=k;
            }

        }
        if(idStartDepot!=idEndDepot){
            whetherFeasible=false;
            if(whetherSayReason){
                System.out.println("Start depot_"+idStartDepot+" of driver _"+this.idOfDriver+" is not the same as End depot_"+idEndDepot);
            }
        }

        // 4 connection time greater than minPlan
        for(int i=0; i<this.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size()-1;i++){
            TripWithWorkingStatusAndDepartureTimeAndVehicle formerTrip=this.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(i);
            TripWithWorkingStatusAndDepartureTimeAndVehicle latterTrip=this.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(i+1);
            int conTime=latterTrip.getDepartureTime()- formerTrip.getDepartureTime()- formerTrip.getDuration();
            if(conTime<instance.getMinPlanTurnTime()){
                whetherFeasible=false;
                if(whetherSayReason){
                    System.out.println("In driver schedule_"+this.idOfDriver+" the connection time of trip_"+formerTrip.getIdOfTrip()
                    +" with latter trip_"+latterTrip.getIdOfTrip()+" is "+conTime+" which is less than the limitation "+instance.getMinPlanTurnTime());
                }
            }
        }

        // 5 connection time less than maxPlan=working time
        for(int i=0; i<this.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().size()-1;i++){
            TripWithWorkingStatusAndDepartureTimeAndVehicle formerTrip=this.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(i);
            TripWithWorkingStatusAndDepartureTimeAndVehicle latterTrip=this.getTripWithWorkingStatusAndDepartureTimeAndVehicleArrayList().get(i+1);
            int conTime=latterTrip.getDepartureTime()- formerTrip.getDepartureTime()- formerTrip.getDuration();
            if(conTime>instance.getMaxWorkingTime()){
                whetherFeasible=false;
                if(whetherSayReason){
                    System.out.println("In driver schedule_"+this.idOfDriver+" the connection time of trip_"+formerTrip.getIdOfTrip()
                            +" with latter trip_"+latterTrip.getIdOfTrip()+" is "+conTime+" which is greater than the limitation "+instance.getMaxWorkingTime());
                }
            }
        }

        // 6 one trip is within time window

        for(int i=0;i<this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size();i++){
            TripWithWorkingStatusAndDepartureTimeAndVehicle tripWithWorkingStatusAndDepartureTimeAndVehicle = this.tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i);
            int idTrip=tripWithWorkingStatusAndDepartureTimeAndVehicle.getIdOfTrip();
            int depTime=tripWithWorkingStatusAndDepartureTimeAndVehicle.getDepartureTime();

            if(depTime<instance.getTrip(idTrip).getEarliestDepartureTime()||depTime>instance.getTrip(idTrip).getLatestDepartureTime()){
                whetherFeasible=false;
                if(whetherSayReason){
                    System.out.println("In driver schedule_"+this.idOfDriver+" trip_"+idTrip+" departure time is "+depTime+" not as required time window from "
                            +instance.getTrip(idTrip).getEarliestDepartureTime()+" to "+instance.getTrip(idTrip).getLatestDepartureTime());
                }
            }
        }

        return  whetherFeasible;

    }


    @Override
    public String toString() {
        String s=idOfDriver+ " "+ tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size();
        for(int i = 0; i< tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.size(); i++){
            s+=" "+ tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i).getIdOfTrip()+" "+
                    tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i).getIdVehicle()+" "+
                    tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i).getDrivingStatus()+" "+ tripWithWorkingStatusAndDepartureTimeAndVehicleArrayList.get(i).getDepartureTime();
        }
//        s+=" \n timeInMinute"+ " working: "+this.getTotalWorkingTime()+" driving: "+this.getTotalDrivingTime();
        return s;
    }

}
