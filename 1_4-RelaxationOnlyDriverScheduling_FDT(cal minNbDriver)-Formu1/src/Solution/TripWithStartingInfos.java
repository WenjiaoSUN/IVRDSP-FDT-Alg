package Solution;
import Instance.Trip;

public class TripWithStartingInfos extends Trip{
    private int  startingTime;
    public TripWithStartingInfos(Trip trip,int startingTime){
        super(trip.getIdOfTrip(),trip.getIdOfStartCity(),trip.getIdOfEndCity(),trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(),trip.getNbVehicleNeed(),trip.getDuration());
        this.startingTime = startingTime;
    }

    public  int getStartingTime(){
        return this.startingTime;
    }
    @Override
    public String toString() {
        return "TripWithStartInfos{" +
                "trip_" + idOfTrip +
                ", departureTimeUnit=" + startingTime +
                '}';
    }

}
