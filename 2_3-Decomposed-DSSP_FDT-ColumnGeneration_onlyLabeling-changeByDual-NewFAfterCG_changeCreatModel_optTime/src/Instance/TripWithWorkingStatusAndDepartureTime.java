package Instance;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public class TripWithWorkingStatusAndDepartureTime extends Trip {
    private  boolean drivingStatus;
    private int departureTime;
    public TripWithWorkingStatusAndDepartureTime(Trip trip, boolean drivingStatus,int departureTime) {
        super(trip.getIdOfTrip(), trip.getIdOfStartCity(),
                trip.getIdOfEndCity(), trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(), trip.getNbVehicleNeed(),trip.getDuration());

        this.drivingStatus = drivingStatus;
        this.departureTime=departureTime;
    }


    public boolean getDrivingStatus() {
        return drivingStatus;
    }

    public int getDepartureTime(){
        return departureTime;
    }

    public void setDepartureTime(int departureTime){this.departureTime=departureTime;}



    @Override
    public String toString() {
        return  idOfTrip +
                " " + drivingStatus+" "+departureTime+" ";
    }

    public static void main(String[] args) {
        Trip l1= new Trip(0,0,1,10,11,1,10);
        Trip l2= new Trip(1,1,0,13,13, 1,11);
        Trip l3= new Trip(2,2,3,9,10, 1,2);
        TripWithWorkingStatusAndDepartureTime ll1= new TripWithWorkingStatusAndDepartureTime(l1,false,1);
        TripWithWorkingStatusAndDepartureTime ll2= new TripWithWorkingStatusAndDepartureTime(l2,true,2);
        TripWithWorkingStatusAndDepartureTime ll3= new TripWithWorkingStatusAndDepartureTime(l3,false,3);
        //System.out.println(l1);
        ArrayList<TripWithWorkingStatusAndDepartureTime> tripWithDriveInfos =new ArrayList<>();//initialize a list of type Trip
        tripWithDriveInfos.add(ll1);
        tripWithDriveInfos.add(ll2);
        tripWithDriveInfos.add(ll3);
        Collections.sort(tripWithDriveInfos);//sort the trips

        for(TripWithWorkingStatusAndDepartureTime tr : tripWithDriveInfos){
            System.out.println(tr);//print out the trips after sort
        }
    }
}
