package InstanceGet;

import java.util.LinkedList;

public class Shift {
    private int idOfShift;
    private LinkedList<Trip> trips;
    private LinkedList<Integer> departureTimes;//2024.9.25
    public Shift(int idOfShift){
        this.idOfShift=idOfShift;
        this.trips=new LinkedList<>();
    }

    public LinkedList<Trip> getTrips() {
        return trips;
    }

    public LinkedList<Integer> getDepartureTimes(){return this.departureTimes;}//2024.9.25

    public int getIdOfShift() {
        return idOfShift;
    }
    public void addTrip(Trip trip){
        this.trips.add(trip);
    }

    public int getTotalWorkingTime(){
        int startingTime= this.trips.getFirst().getDepartureTime();
        int endingTime=this.trips.getLast().getArrivalTime();
        int totalWorkingTime =endingTime-startingTime;
        return totalWorkingTime;
    }


    public boolean whetherTripPresentInTheShift(int idTrip){
       boolean whetherTripPresent= false;
       for(int i=0;i<this.trips.size();i++){
           int idOfTripInShift=this.trips.get(i).getIdOfTrip();
           if(idTrip==idOfTripInShift){
               whetherTripPresent=true;
           }
       }
       return whetherTripPresent;
    }


    @Override
    public String toString() {
        return "Shift{" +
                "idOfShift=" + idOfShift +
                ", trips=" + trips +
                ",departureTime"+departureTimes+//2024.9.25
                '}';
    }

}
