package Solution;

import Instance.Trip;

public class TripWithDriveInfos extends Trip {
    private  int idOfVehicle;
    private  boolean drivingStatus;
    private int startingTime;

    public TripWithDriveInfos(Trip trip, int idOfVehicle, boolean drivingStatus,int startingTime) {
        super(trip.getIdOfTrip(), trip.getIdOfStartCity(),
                trip.getIdOfEndCity(), trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(), trip.getNbVehicleNeed(),trip.getDuration());
        this.idOfVehicle = idOfVehicle;
        this.drivingStatus = drivingStatus;
        this.startingTime = startingTime;
    }

    public int getIdOfVehicle() {
        return idOfVehicle;
    }

    public boolean getDrivingStatus() {
        return drivingStatus;
    }

    public int getStartingTime(){return startingTime;}

    @Override
    public String toString() {
        return "TripWithDriveInfos{" +
                "trip_" + idOfTrip +
                ", v_" + idOfVehicle +
                ", driving=" + drivingStatus +
                ", departureTimeUnit=" + startingTime +
//                ", arrivalTime=" + arrivalTime +
//                ", nbVehicleNeedInThisTrip=" + nbVehicleNeed +
                '}';
    }

    public static void main(String[] args) {
    }
}
