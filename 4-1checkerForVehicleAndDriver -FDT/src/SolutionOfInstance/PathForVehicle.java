package SolutionOfInstance;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;

import java.util.ArrayList;

public class PathForVehicle {
    private Instance instance;
    private int idOfVehicle;
    private ArrayList<TripWithDepartureTime> tripWithDepartureTimeArrayList;

    public PathForVehicle(Instance instance, int idOfVehicle) {
        this.instance = instance;
        this.idOfVehicle = idOfVehicle;
        this.tripWithDepartureTimeArrayList = new ArrayList<>();
    }

    public void addTripInVehiclePath(TripWithDepartureTime tripWithDepartureTime) {
        this.tripWithDepartureTimeArrayList.add(tripWithDepartureTime);
    }

    public int getIdOfVehicle() {
        return idOfVehicle;
    }

    public ArrayList<TripWithDepartureTime> getTripWithDepartureTimeArrayList() {
        return tripWithDepartureTimeArrayList;
    }

    public double getIdleTimeCostOfVehiclePath() {
        double idleTimeCost = 0;
        for (int i = 0; i < this.tripWithDepartureTimeArrayList.size() - 1; i++) {
            TripWithDepartureTime formerTripWithDepartureTime = this.tripWithDepartureTimeArrayList.get(i);
            TripWithDepartureTime latterTripWithDepartureTime = this.tripWithDepartureTimeArrayList.get(i + 1);
            int formerStartingTime = formerTripWithDepartureTime.getStartingTime();
            int durationOfFormerTrip=formerTripWithDepartureTime.getDuration();
            int latterStartingTime = latterTripWithDepartureTime.getStartingTime();
            int connectionTime = latterStartingTime - formerStartingTime - durationOfFormerTrip;
            idleTimeCost=idleTimeCost+connectionTime* instance.getIdleTimeCostForVehiclePerUnit();
        }
        return idleTimeCost;
    }

    public boolean whetherConsecutive(int idFirstTrip, int idSecondTrip){
        boolean whetherConsecutive=false;
        for(int i=0;i<this.tripWithDepartureTimeArrayList.size()-1;i++){
            int idFirst=this.tripWithDepartureTimeArrayList.get(i).getIdOfTrip();
            int idSecond=this.tripWithDepartureTimeArrayList.get(i+1).getIdOfTrip();
            if(idFirst==idFirstTrip&&idSecond==idSecondTrip){
                whetherConsecutive=true;
                break;
            }
        }

        return  whetherConsecutive;
    }

    public boolean whetherFeasible(boolean whetherSayReason){
        //1 check the connection time
        //2 check the trip depature within time window
        boolean whetherFeasible=true;

        for(int i=0; i<this.tripWithDepartureTimeArrayList.size()-1;i++){
            TripWithDepartureTime tripWithDepartureTime1=this.tripWithDepartureTimeArrayList.get(i);
            TripWithDepartureTime tripWithDepartureTime2=this.tripWithDepartureTimeArrayList.get(i+1);
            int conTime= tripWithDepartureTime2.getStartingTime()-tripWithDepartureTime1.getStartingTime()-tripWithDepartureTime1.getDuration();
            if(conTime<instance.getMinPlanTurnTime()){
                whetherFeasible=false;
                if(whetherSayReason){
                    System.out.println("In vehicle route_"+this.idOfVehicle+"connection time between trip_"+tripWithDepartureTime1.getIdOfTrip()
                            +" to trip_"+tripWithDepartureTime2.getIdOfTrip()+" is "+conTime+" which is less than minPlan time"+instance.getMinPlanTurnTime());
                }
            }
            if(conTime>instance.getMaxWorkingTime()){
                whetherFeasible=false;
                if(whetherSayReason){
                    System.out.println("In vehicle route_"+this.idOfVehicle+"connection time between trip_"+tripWithDepartureTime1.getIdOfTrip()
                            +" to trip_"+tripWithDepartureTime2.getIdOfTrip()+" is "+conTime+" which greater than the max limitation");
                }
            }

        }

        for(int t=0;t<this.tripWithDepartureTimeArrayList.size();t++){
            TripWithDepartureTime tripWithDepartureTime=this.tripWithDepartureTimeArrayList.get(t);
            int idOfTrip=tripWithDepartureTime.getIdOfTrip();
            int depTime=tripWithDepartureTime.getStartingTime();
            if(depTime<instance.getTrip(idOfTrip).getEarliestDepartureTime()||depTime>instance.getTrip(idOfTrip).getLatestDepartureTime()){
                whetherFeasible=false;
                if(whetherSayReason){
                    System.out.println("In vehicle route_"+this.idOfVehicle+"departure time of trip_"+idOfTrip+
                            " is not within time window from "+instance.getTrip(idOfTrip).getEarliestDepartureTime()+" to "+instance.getTrip(idOfTrip).getLatestDepartureTime());
                }
            }
        }
        return whetherFeasible;
    }

    public boolean whetherPresent(int idTrip){
        boolean whetherPresent=false;
        for(int t=0;t<this.tripWithDepartureTimeArrayList.size();t++){
            Trip trip=this.tripWithDepartureTimeArrayList.get(t);
            int id=trip.getIdOfTrip();
            if(id==idTrip){
                whetherPresent=true;
            }
        }
        return  whetherPresent;
    }

    public int getStartCityForPath(){
        return  this.tripWithDepartureTimeArrayList.get(0).getIdOfStartCity();
    }

    public int getEndCityForPath(){
        return  this.tripWithDepartureTimeArrayList.get(this.tripWithDepartureTimeArrayList.size()-1).getIdOfEndCity();
    }


    @Override
    public String toString() {

        String s=idOfVehicle+ " "+ tripWithDepartureTimeArrayList.size();
        for(int i = 0; i< tripWithDepartureTimeArrayList.size(); i++){
            s+=" "+ tripWithDepartureTimeArrayList.get(i).getIdOfTrip()+" "+ tripWithDepartureTimeArrayList.get(i).getStartingTime();
        }
        return s;
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity05_Size180_Day2_nbTrips200_combPer0.1.txt");
        Instance instance = reader.readFile();

    }


}
