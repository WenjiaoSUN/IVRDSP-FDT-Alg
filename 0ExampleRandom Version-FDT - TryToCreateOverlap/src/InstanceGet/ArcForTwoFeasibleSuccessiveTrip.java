package InstanceGet;

public class ArcForTwoFeasibleSuccessiveTrip {
    private int idOfFirstTrip;
    private int idOfSecondTrip;
    private int costForVehicle;
    private int costForDriver;
//    private double costForChangeover;
    public ArcForTwoFeasibleSuccessiveTrip(int idOfFirstTrip, int idOfSecondTrip, int costForVehicle, int costForDriver){
        this.idOfFirstTrip =idOfFirstTrip;
        this.idOfSecondTrip =idOfSecondTrip;
        this.costForVehicle= costForVehicle;
        this.costForDriver= costForDriver;

    }

    public void setIdOfFirstTrip(int idOfFirstTrip) {
        this.idOfFirstTrip = idOfFirstTrip;
    }

    public void setIdOfSecondTrip(int idOfSecondTrip) {
        this.idOfSecondTrip = idOfSecondTrip;
    }

    public void setCostForVehicle(int costForVehicle) {
        this.costForVehicle = costForVehicle;
    }

    public void setCostForDriver(int costForDriver) {
        this.costForDriver = costForDriver;
    }

//    public void setCostForChangeover(double costForChangeover) {
//        this.costForChangeover = costForChangeover;
//    }

    public int getIdOfFirstTrip() {
        return idOfFirstTrip;
    }

    public int getIdOfSecondTrip() {
        return idOfSecondTrip;
    }

    public int getCostForVehicle() {
        return costForVehicle;
    }

    public int getCostForDriver() {
        return costForDriver;
    }

//    public double getCostForChangeover() {
//        return costForChangeover;
//    }

    @Override
    public String toString() {
        return   idOfFirstTrip+" "+idOfSecondTrip+" "+ costForVehicle +
                " "+ costForDriver;
    }

    public static void main(String[] args) {
        ArcForTwoFeasibleSuccessiveTrip arcForTwoFeasibleSuccessiveTrip = new ArcForTwoFeasibleSuccessiveTrip(0,1,1,2);
        System.out.println(arcForTwoFeasibleSuccessiveTrip);
    }
}
