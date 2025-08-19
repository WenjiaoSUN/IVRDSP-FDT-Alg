package InstanceGet;

import java.util.LinkedList;

public class VehicleStartTrips {
    private int idOfVehicle;
    private LinkedList<Trip> vehicleStartTrips;

    public VehicleStartTrips(int idOfVehicle) {
        this.idOfVehicle = idOfVehicle;
        this.vehicleStartTrips = new LinkedList<>();
    }

    public void addStartTrip(Trip trip) {
        this.vehicleStartTrips.add(trip);
    }

    @Override
    public String toString() {
        String s = idOfVehicle +" ";
        for (int i = 0; i < vehicleStartTrips.size(); i++) {
            if (i < vehicleStartTrips.size() - 1) {
                s += vehicleStartTrips.get(i).getIdOfTrip() + "\n" + idOfVehicle+" ";
            }
            else s+=vehicleStartTrips.get(vehicleStartTrips.size()-1).getIdOfTrip();
        }
        return s;
    }

    public static void main(String[] args) {
        Trip trip = new Trip(0, 1, 2,6, 7, 8,1,1);
        LinkedList<Trip> trips = new LinkedList<>();
        VehicleStartTrips vehicleStartTrips1 = new VehicleStartTrips(0);
        vehicleStartTrips1.addStartTrip(trip);
        System.out.println(vehicleStartTrips1);
    }
}
