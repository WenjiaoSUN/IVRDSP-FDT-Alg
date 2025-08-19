package Instance;
import java.util.Objects;

import java.util.ArrayList;
import java.util.Collections;

public class TripWithWorkingStatus {
    private Trip trip;
    private  boolean drivingStatus;

    public TripWithWorkingStatus(Trip trip, boolean drivingStatus) {
      this.trip=trip;
        this.drivingStatus = drivingStatus;
    }

    public int getIdOfTrip(){
        return this.trip.getIdOfTrip();
    }

    public boolean equal(TripWithWorkingStatus tripWithWorkingStatus){
        boolean whetherEqual=false;
        if(this.getIdOfTrip()==tripWithWorkingStatus.getIdOfTrip()
                &&this.getDrivingStatus()==tripWithWorkingStatus.getDrivingStatus()){
            whetherEqual=true;

        }
        return whetherEqual;
    }


    public boolean getDrivingStatus() {
        return drivingStatus;
    }

    @Override
    public String toString() {
        return  trip.getIdOfTrip() +
                " " + drivingStatus;
    }


    //The hashCode() method has also been overridden to ensure that objects that are considered equal have the same hash code.
    // This is important for consistent behavior when using objects in hash-based collections like HashSet or HashMap.
    //With these changes, the intersection operation (retainAll()) will consider the combination of ID and driving status for determining intersection

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripWithWorkingStatus)) return false;
        TripWithWorkingStatus that = (TripWithWorkingStatus) o;
        return getIdOfTrip() == that.getIdOfTrip() && drivingStatus == that.drivingStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trip.idOfTrip, drivingStatus);
    }

    public static void main(String[] args) {
        Trip l1= new Trip(0,0,1,10,11,1,10);
        Trip l2= new Trip(1,1,0,13,13, 1,12);
        Trip l3= new Trip(2,2,3,9,10, 1,15);
        TripWithWorkingStatus ll1= new TripWithWorkingStatus(l1,false);
        TripWithWorkingStatus ll2= new TripWithWorkingStatus(l1,true);
        TripWithWorkingStatus ll3= new TripWithWorkingStatus(l2,false);
        Trip l4= new Trip(3,0,1,10,11, 1,56);
        Trip l5= new Trip(4,1,0,13,13,1,6);
        //System.out.println(l1);
        ArrayList<TripWithWorkingStatus> tripWithDriveInfos =new ArrayList<>();//initialize a list of type Trip
        tripWithDriveInfos.add(ll1);
        tripWithDriveInfos.add(ll2);
        tripWithDriveInfos.add(ll3);
//        Collections.sort(tripWithDriveInfos);//sort the trips

        for(TripWithWorkingStatus tr : tripWithDriveInfos){
            System.out.println(tr);//print out the trips after sort
        }
    }
}
