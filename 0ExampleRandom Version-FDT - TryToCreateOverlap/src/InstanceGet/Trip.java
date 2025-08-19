package InstanceGet;
import InstanceInput.Input;

public class Trip {
    private Input input;
    private int idOfTrip;
//    private int idOfShift;
    private int idOfStartCity;
    private int idOfEndCity;
    private int departureTime;
//    private int arrivalTime;
    private int nbCombinedVehicle;

    private int earlestDeparture;
    private int latestDeparture;
    private int duration;
    public Trip(int idOfTrip, int idOfStartCity, int idOfEndCity, int departureTime, int earliestDeparture,int latestDeparture, int nbCombinedVehicle,int duration){
        this.idOfTrip=idOfTrip;
//        this.idOfShift =idOfShift;
        this.idOfStartCity = idOfStartCity;
        this.idOfEndCity = idOfEndCity;
//        this.duration=0;
        this.departureTime=departureTime;
        this.earlestDeparture=earliestDeparture;
        this.latestDeparture=latestDeparture;
        this.nbCombinedVehicle=nbCombinedVehicle;
        this.duration=duration;
    }

    public int getIdOfTrip() {
        return idOfTrip;
    }

//    public int getIdOfShift( ){return  idOfShift; }

    public int getIdOfStartCity() {
        return idOfStartCity;
    }

    public int getIdOfEndCity() {
        return idOfEndCity;
    }

    public int getDepartureTime(){
        return departureTime;
    }

    public int getArrivalTime(){
        return this.departureTime+this.duration;
    }

    public int getNbCombinedVehicle() {
        return nbCombinedVehicle;
    }

    public int getDuration(){return duration;}

    public void setNbCombinedVehicle(int nbCombinedVehicle) {
        this.nbCombinedVehicle = nbCombinedVehicle;
    }
    public void setDepartureTime(int departureTime) {
        this.departureTime =  departureTime;
    }

    public void setEarliestDeparture(int earliestDeparture){this.earlestDeparture=earliestDeparture;}
    public void setLatestDeparture(int latestDeparture){this.latestDeparture=latestDeparture;}







    @Override
    public String toString() {
        return  idOfTrip+" "+idOfStartCity+" "+idOfEndCity
                +" "+ earlestDeparture+" "+latestDeparture+" "+nbCombinedVehicle+" "+duration;
        //"+ idOfShift +"
    }

    public static void main(String[] args) {
        Trip trip = new Trip(0, 0, 1, 7,6,8, 1,1);
        System.out.println(trip);
    }
}
