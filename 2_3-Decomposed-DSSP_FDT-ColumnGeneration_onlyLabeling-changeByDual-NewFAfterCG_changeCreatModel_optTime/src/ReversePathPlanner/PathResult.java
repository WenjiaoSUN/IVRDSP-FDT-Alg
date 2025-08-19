package ReversePathPlanner;

import Instance.TripWithDepartureTime;

import java.util.ArrayList;
import java.util.List;

public class PathResult {
    public int depTime;
    public ArrayList<TripWithDepartureTime> path;

    public PathResult(int depTime, ArrayList<TripWithDepartureTime> path) {
        this.depTime = depTime;
        this.path = (ArrayList<TripWithDepartureTime>) path;
    }
}
