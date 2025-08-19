package Solution;
import Instance.Trip;

public class TripWithStartingInfos extends Trip{
    private int startingTimeUnit;
    public TripWithStartingInfos(Trip trip,int startingTimeUnit){
        super(trip.getIdOfTrip(),trip.getIdOfStartCity(),trip.getIdOfEndCity(),trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(),trip.getNbVehicleNeed(),trip.getDuration());
        this.startingTimeUnit =startingTimeUnit;
    }

    public int getStartingTimeUnit(){
        return this.startingTimeUnit;
    }
    @Override
    public String toString() {
        return "TripWithStartInfos{" +
                "trip_" + idOfTrip +
                ", departureTimeUnit=" + startingTimeUnit +
                '}';
    }

}
