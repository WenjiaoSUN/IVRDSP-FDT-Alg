package InstanceGet;

import java.util.LinkedList;

public class DriverStartTrips {
    private int idOfDriver;
    private LinkedList<Trip> driverStartTrips;
    public DriverStartTrips(int idOfDriver){
        this.idOfDriver=idOfDriver;
        this.driverStartTrips=new LinkedList<>();
    }
    public void addStartTrip(Trip trip){
        this.driverStartTrips.add(trip);
    }

    @Override
    public String toString() {
        String s = idOfDriver + " ";
        for (int i = 0; i <driverStartTrips.size(); i++) {
            if (i < driverStartTrips.size() - 1) {
                s += driverStartTrips.get(i).getIdOfTrip() + "\n" + idOfDriver + " ";
            }
            else s+=driverStartTrips.get(driverStartTrips.size()-1).getIdOfTrip();
        }
        return s;
    }

}
