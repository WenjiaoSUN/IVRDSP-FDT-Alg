package Solution;

import Instance.Trip;

public class TripWithDriveInfos extends Trip {
    private  int idOfVehicle;
    private  boolean drivingStatus;

    private int startingTimeUnit;

    public TripWithDriveInfos(Trip trip, int idOfVehicle, boolean drivingStatus,int startingTimeUnit) {
        super(trip.getIdOfTrip(), trip.getIdOfStartCity(),
                trip.getIdOfEndCity(), trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(), trip.getNbVehicleNeed(),trip.getDuration());
        this.idOfVehicle = idOfVehicle;
        this.drivingStatus = drivingStatus;
        this.startingTimeUnit=startingTimeUnit;
    }

    public int getIdOfVehicle() {
        return idOfVehicle;
    }

    public boolean getDrivingStatus() {
        return drivingStatus;
    }

    public int getStartingTimeUnit(){return startingTimeUnit;}

    @Override
    public String toString() {
        return "TripWithDriveInfos{" +
                "trip_" + idOfTrip +
                ", v_" + idOfVehicle +
                ", driving=" + drivingStatus +
                ", departureTimeUnit=" + startingTimeUnit +
//                ", arrivalTime=" + arrivalTime +
//                ", nbVehicleNeedInThisTrip=" + nbVehicleNeed +
                '}';
    }

    public static void main(String[] args) {
    }
}
