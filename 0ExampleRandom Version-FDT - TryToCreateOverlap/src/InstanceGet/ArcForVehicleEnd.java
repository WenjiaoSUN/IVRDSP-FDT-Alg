package InstanceGet;

public class ArcForVehicleEnd {
    private int idOfVehicle;
    private int idOfTripVehicleEnd;//open route, vehicle can end at any depot
    public ArcForVehicleEnd(int idOfVehicle, int idOfTripVehicleEnd){
        this.idOfVehicle = idOfVehicle;
        this.idOfTripVehicleEnd = idOfTripVehicleEnd;
    }

    @Override
    public String toString() {
        return "ArcForVehicleEnd{" +
                "idOfVehicle=" + idOfVehicle +
                ", idOfTripVehicleEnd=" + idOfTripVehicleEnd +
                '}';
    }

    public static void main(String[] args) {
        ArcForVehicleEnd arcForVehicleEnd = new ArcForVehicleEnd(0,0);
        System.out.println(arcForVehicleEnd);
    }
}
