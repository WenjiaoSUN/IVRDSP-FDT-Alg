package SolutionOfInstance;

import Instance.Instance;

public class CombineTripInform {

    private Instance instance;

    private int idTrip;
    private int idDrivingDriver;
    private int idLeadingVehicle;

    public CombineTripInform(Instance instance, int idTrip, int idDrivingDriver, int idLeadingVehicle){
        this.instance=instance;
        this.idTrip=idTrip;
        this.idDrivingDriver=idDrivingDriver;
        this.idLeadingVehicle=idLeadingVehicle;
    }

    public int getIdTrip() {
        return idTrip;
    }

    public void setIdTrip(int idTrip) {
        this.idTrip = idTrip;
    }

    public int getIdDrivingDriver() {
        return idDrivingDriver;
    }

    public void setIdDrivingDriver(int idDrivingDriver) {
        this.idDrivingDriver = idDrivingDriver;
    }

    public int getIdLeadingVehicle() {
        return idLeadingVehicle;
    }

    public void setIdLeadingVehicle(int idLeadingVehicle) {
        this.idLeadingVehicle = idLeadingVehicle;
    }

    @Override
    public String toString() {
        return "CombineTripInform{" +
                "idTrip=" + idTrip +
                ", idDrivingDriver=" + idDrivingDriver +
                ", idLeadingVehicle=" + idLeadingVehicle +
                '}';
    }
}
