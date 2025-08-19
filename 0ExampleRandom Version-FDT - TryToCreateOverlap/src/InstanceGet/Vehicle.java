package InstanceGet;

public class Vehicle {
    private int idOfVehicle;
    private int idOfStartingDepot;

    private int nbTrips;


    public Vehicle(int idOfVehicle, int idOfStartingDepot,int nbTrips) {
        this.idOfVehicle = idOfVehicle;
        this.idOfStartingDepot = idOfStartingDepot;
        this.nbTrips=nbTrips;
    }

    public int getIdOfVehicle() {
        return idOfVehicle;
    }

    public void setIdOfStartingDepot(int idOfStartingDepot) {
        this.idOfStartingDepot = idOfStartingDepot;
    }

    public int getIdOfStartingDepot() {
        return idOfStartingDepot;
    }



    @Override
    public String toString() {
        return  idOfVehicle+" "+idOfStartingDepot+" "+nbTrips;
    }

    public static void main(String[] args) {
        Vehicle vehicle = new Vehicle(0,0,2);
        System.out.println(vehicle);
    }
}
