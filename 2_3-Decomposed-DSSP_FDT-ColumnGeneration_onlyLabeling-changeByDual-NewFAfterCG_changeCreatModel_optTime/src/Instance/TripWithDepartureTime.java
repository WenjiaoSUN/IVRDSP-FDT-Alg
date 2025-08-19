package Instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Collections;

public class TripWithDepartureTime extends Trip {

    private int departureTime;

    public TripWithDepartureTime(Trip trip, int departureTime) {
        super(trip.getIdOfTrip(), trip.getIdOfStartCity(),
                trip.getIdOfEndCity(), trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(), trip.getNbVehicleNeed(), trip.getDuration());


        this.departureTime = departureTime;
    }


    public int getDepartureTime() {
        return departureTime;
    }

    @Override
    public String toString() {
        return idOfTrip +
                " " + departureTime + " ";
    }

    public static void main(String[] args) {
        Trip l1 = new Trip(0, 0, 1, 10, 11, 1, 10);
        Trip l2 = new Trip(1, 1, 0, 13, 13, 1, 11);
        Trip l3 = new Trip(2, 2, 3, 9, 10, 1, 2);
        TripWithDepartureTime ll1 = new TripWithDepartureTime(l1, 1);
        TripWithDepartureTime ll2 = new TripWithDepartureTime(l2, 2);

        //System.out.println(l1);
        ArrayList<TripWithDepartureTime> tripWithDriveInfos = new ArrayList<>();//initialize a list of type Trip
        tripWithDriveInfos.add(ll1);
        tripWithDriveInfos.add(ll2);

        Collections.sort(tripWithDriveInfos);//sort the trips

        for (TripWithDepartureTime tr : tripWithDriveInfos) {
            System.out.println(tr);//print out the trips after sort
        }
    }

}
