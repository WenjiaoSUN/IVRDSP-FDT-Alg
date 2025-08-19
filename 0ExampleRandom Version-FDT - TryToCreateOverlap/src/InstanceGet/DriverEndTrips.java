package InstanceGet;

import java.util.LinkedList;

public class DriverEndTrips {
    private int idOfDriver;
    private LinkedList<Trip> driverEndTrips;

    public DriverEndTrips(int idOfDriver) {
        this.idOfDriver = idOfDriver;
        this.driverEndTrips = new LinkedList<>();
    }

    public void addEndTrip(Trip trip) {
        this.driverEndTrips.add(trip);
    }

    @Override
    public String toString() {
        String s = idOfDriver+" ";
        for (int i = 0; i <driverEndTrips.size(); i++) {
            if (i < driverEndTrips.size() - 1) {
                s += driverEndTrips.get(i).getIdOfTrip() + "\n" + idOfDriver + " ";
            }
            else s+=driverEndTrips.get(driverEndTrips.size()-1).getIdOfTrip();
        }
        return s;
    }
}
