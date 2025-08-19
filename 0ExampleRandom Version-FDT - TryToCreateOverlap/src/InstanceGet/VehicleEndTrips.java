package InstanceGet;

import java.util.LinkedList;

public class VehicleEndTrips {
    private int idOfVehicle;
    private LinkedList<Trip> vehicleEndTrips;
    public VehicleEndTrips (int idOfVehicle){
        this.idOfVehicle=idOfVehicle;
        this.vehicleEndTrips=new LinkedList<>();
    }

    public void addEndTrip(Trip trip){
        this.vehicleEndTrips.add(trip);
    }

    @Override
    public String toString() {
        String s = idOfVehicle + " ";
        for (int i = 0; i < vehicleEndTrips.size(); i++) {
            if (i < vehicleEndTrips.size() - 1) {
                s += vehicleEndTrips.get(i).getIdOfTrip() + "\n" + idOfVehicle + " ";
            }
            else s+=vehicleEndTrips.get(vehicleEndTrips.size()-1).getIdOfTrip();
        }
        return s;
    }
}
