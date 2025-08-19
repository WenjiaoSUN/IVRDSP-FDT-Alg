package Instance;

public class Vehicle {
    private int idOfVehicle;
    private int idOfStartDepot;
    private int nbTrips;

//    // the starting point for the vehicle is given ahead
//    private boolean [] canStartWithTrip;
//    private boolean [] canEndWithTrip;

    public Vehicle(int vehicleId, int idOfStartDepot, int nbTrips){
        this.idOfVehicle =vehicleId;
        this.idOfStartDepot=idOfStartDepot;
        this.nbTrips = nbTrips;
//        //this part is to judge whether the vehicle could start with that leg
//        this.canStartWithTrip = new boolean[nbTrips];
//        this.canEndWithTrip= new boolean[nbTrips];
//
//        for (int i = 0; i< nbTrips; i++){
//            this.canStartWithTrip[i]=false;
//            this.canEndWithTrip[i]=false;
//        }
    }

    public int getIdOfVehicle() {
        return idOfVehicle;
    }

    public int getIdOfStartDepot() {
        return idOfStartDepot;
    }

    public int getNbTrips() {
        return nbTrips;
    }

//    //except the basic information of vehicle, when considering the legs it also add more information for the vehicle
//    public void setCanStartWithTrip(int idOfStartLeg){this.canStartWithTrip[idOfStartLeg]=true;}
//    public void setCanEndWithTrip(int idOfEndTrip){this.canEndWithTrip[idOfEndTrip]=true;}
//    public boolean canStartTrip(int idOfStartLeg){return canStartWithTrip[idOfStartLeg];}
//    public boolean canEndTrip(int idOfEndTrip){return canEndWithTrip[idOfEndTrip];}


    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + idOfVehicle +
                ", idOfStartDepot=" + idOfStartDepot +
                ", nbTrips=" + nbTrips +
                '}';
    }

    public static void main(String[] args) {
        Vehicle v0=new Vehicle(0,19,19);
    }
}
