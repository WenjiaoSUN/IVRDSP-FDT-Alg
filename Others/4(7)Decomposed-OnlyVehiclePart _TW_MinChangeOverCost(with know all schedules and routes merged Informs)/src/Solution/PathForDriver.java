package Solution;

import Instance.Instance;
import Instance.Trip;

import java.util.ArrayList;

//This file describes one example path for the one crew

public class PathForDriver {
    private Instance instance;
    private int idOfDriver;

    private ArrayList<TripWithDriveInfos> tripWithDriveInfosArrayList;

    public PathForDriver(Instance instance, int idOfDriver) {
        this.instance = instance;
        this.idOfDriver = idOfDriver;
        this.tripWithDriveInfosArrayList = new ArrayList<TripWithDriveInfos>();

    }
    public int getIdOfDriver() {
        return idOfDriver;
    }

    public ArrayList<TripWithDriveInfos> getTripWithDriveInfosArrayList() {
        return tripWithDriveInfosArrayList;
    }

    public void addTripInDriverPath(TripWithDriveInfos tripWithDriveInfos) {
        this.tripWithDriveInfosArrayList.add(tripWithDriveInfos);
    }

    public double getIdleTimeCostOfDriverPath() {
        double idleTimeCost = 0;
        for (int i = 0; i < this.tripWithDriveInfosArrayList.size() - 1; i++) {
            int idOfFormerTrip = this.tripWithDriveInfosArrayList.get(i).getIdOfTrip();
            int idOfLatterTrip = this.tripWithDriveInfosArrayList.get(i + 1).getIdOfTrip();
            TripWithDriveInfos formerTripWithStartingInfos=this.tripWithDriveInfosArrayList.get(i);
            TripWithDriveInfos latterTripWithStartingInfos=this.tripWithDriveInfosArrayList.get(i+1);

            int formerStartingTime=formerTripWithStartingInfos.getStartingTime();
            int latterStartingTime=latterTripWithStartingInfos.getStartingTime();

            Trip formerTrip=instance.getTrip(idOfFormerTrip);
            int durationOfFormerTrip=formerTrip.getDuration();

            int connectionTime=latterStartingTime-formerStartingTime-durationOfFormerTrip;

            if (this.instance.whetherHavePossibleArcAfterCleaning(idOfFormerTrip, idOfLatterTrip)&&connectionTime>=this.instance.getMinPlanTurnTime()) {
//
                    idleTimeCost = idleTimeCost + this.instance.getIdleTimeCostForDriverPerUnit()*connectionTime;
                    System.out.println("now the idle time in driver path "+idOfDriver+" between trip "+idOfFormerTrip+" to trip " +idOfLatterTrip+" is"+connectionTime);
                   System.out.println("now the idle time cost until trip " +idOfLatterTrip+" is"+idleTimeCost);
                    idleTimeCost = Math.round(100.0 * idleTimeCost) / 100.0;
//
            } else {
                System.out.println("ERROR !!! The method of getIdleTimeCostForDriver");
                System.out.println("idOfFormerTrip = " + idOfFormerTrip);
                System.out.println("idOfLatterTrip = " + idOfLatterTrip);
            }
        }
        System.out.println("idle cost for this driver path is "+idleTimeCost);
        return idleTimeCost;
    }
//    public double getTotalWorkingTime(){
//        TripWithDriveInfos tripWithDriveInfosFirst= this.tripWithDriveInfosArrayList.get(0);
//        TripWithDriveInfos tripWithDriveInfosLast= this.tripWithDriveInfosArrayList.get(this.tripWithDriveInfosArrayList.size()-1);
//        double firstWorkingTimeUnit=tripWithDriveInfosFirst.getStartingTime();
//        double lastWorkingTimeUnit=tripWithDriveInfosLast.getStartingTime();
//        int durationOfLastTrip= instance.getTrip(tripWithDriveInfosLast.getIdOfTrip()).getDuration();
//        double totalWorkingTimeInThisShift=(lastWorkingTimeUnit-firstWorkingTimeUnit)*instance.getTimeSlotUnit()+durationOfLastTrip;
//        return totalWorkingTimeInThisShift;
//    }

    public double getTotalDrivingTime(){
        double totalDrivingTimeInThisShift =0;
        for(int i = 0; i<this.tripWithDriveInfosArrayList.size(); i++){
            TripWithDriveInfos tripWithDriveInfos = this.tripWithDriveInfosArrayList.get(i);
            if(tripWithDriveInfos.getDrivingStatus()==true){
                totalDrivingTimeInThisShift=totalDrivingTimeInThisShift+instance.getTrip(i).getDuration();
            }
        }

        return totalDrivingTimeInThisShift;
    }







    public int positionInPathOfCrew(Trip trip) {
        for (int i = 0; i < this.tripWithDriveInfosArrayList.size(); i++)
            if (this.tripWithDriveInfosArrayList.get(i).equals(trip)) {
                return i;
            }
        return Integer.MAX_VALUE;
    }



    //here is the comment part
    public boolean isPresentInPath(Trip trip) {
        for (int i = 0; i < this.tripWithDriveInfosArrayList.size(); i++) {
            if (this.tripWithDriveInfosArrayList.get(i).equals(trip)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        String s="driver "+idOfDriver+ " "+ tripWithDriveInfosArrayList.size();
        for(int i = 0; i< tripWithDriveInfosArrayList.size(); i++){
            s+=" "+ tripWithDriveInfosArrayList.get(i).getIdOfTrip()+" "+
//                    tripWithDriveInfosArrayList.get(i).getDuration()+" "+
                    tripWithDriveInfosArrayList.get(i).getIdOfVehicle()+" "+
                    tripWithDriveInfosArrayList.get(i).getDrivingStatus()+" "+ tripWithDriveInfosArrayList.get(i).getStartingTime();
        }
//        s+=" \n timeInMinute"+ " working: "+this.getTotalWorkingTime()+" driving: "+this.getTotalDrivingTime();
        return s;
    }

}
