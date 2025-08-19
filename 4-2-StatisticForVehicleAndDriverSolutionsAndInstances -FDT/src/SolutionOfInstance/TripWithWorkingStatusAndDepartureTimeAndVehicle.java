package SolutionOfInstance;

import Instance.Trip;
import java.util.ArrayList;
import java.util.Collections;

public class TripWithWorkingStatusAndDepartureTimeAndVehicle extends Trip {
    private  boolean drivingStatus;
    private int departureTime;

    private int idVehicle;
    public TripWithWorkingStatusAndDepartureTimeAndVehicle(Trip trip,  int idVehicle, boolean drivingStatus, int departureTime) {
        super(trip.getIdOfTrip(), trip.getIdOfStartCity(),
                trip.getIdOfEndCity(), trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(), trip.getNbVehicleNeed(),trip.getDuration());
        this.idVehicle=idVehicle;

        this.drivingStatus = drivingStatus;
        this.departureTime=departureTime;

    }


    public boolean getDrivingStatus() {
        return drivingStatus;
    }

    public int getDepartureTime(){
        return departureTime;
    }

    public int getIdVehicle() {
        return idVehicle;
    }

    @Override
    public String toString() {
        return  idOfTrip + " " + idVehicle+" "+drivingStatus+" "+departureTime+" ";
    }

    public static void main(String[] args) {
        Trip l1= new Trip(0,0,1,10,11,1,10);
        Trip l2= new Trip(1,1,0,13,13, 1,11);
        Trip l3= new Trip(2,2,3,9,10, 1,2);
        TripWithWorkingStatusAndDepartureTimeAndVehicle ll1= new TripWithWorkingStatusAndDepartureTimeAndVehicle(l1,1,false,1);
        TripWithWorkingStatusAndDepartureTimeAndVehicle ll2= new TripWithWorkingStatusAndDepartureTimeAndVehicle(l2,2,true,2);
        TripWithWorkingStatusAndDepartureTimeAndVehicle ll3= new TripWithWorkingStatusAndDepartureTimeAndVehicle(l3,3,false,3);
        //System.out.println(l1);
//        ArrayList<TripWithWorkingStatusAndDepartureTimeAndVehicle> tripWithDriveInfos =new ArrayList<>();//initialize a list of type Trip
//        tripWithDriveInfos.add(ll1);
//        tripWithDriveInfos.add(ll2);
//        tripWithDriveInfos.add(ll3);
//        Collections.sort(tripWithDriveInfos);//sort the trips

//        for(TripWithWorkingStatusAndDepartureTimeAndVehicle tr : tripWithDriveInfos){
//            System.out.println(tr);//print out the trips after sort
//        }
    }
}
