package SolutionOfInstance;
import Instance.Trip;

public class TripWithDepartureTime extends Trip{
    private int  startingTime;
    public TripWithDepartureTime(Trip trip, int startingTime){
        super(trip.getIdOfTrip(),trip.getIdOfStartCity(),trip.getIdOfEndCity(),trip.getEarliestDepartureTime(),
                trip.getLatestDepartureTime(),trip.getNbVehicleNeed(),trip.getDuration());
        this.startingTime = startingTime;
    }

    public  int getStartingTime(){
        return this.startingTime;
    }
    @Override
    public String toString() {
        return
                " " + idOfTrip +
                " " + startingTime ;
    }

}
