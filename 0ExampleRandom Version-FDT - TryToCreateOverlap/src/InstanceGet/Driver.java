package InstanceGet;

public class Driver {
    private int idOfDriver;
    private int idOfStartDepot;
    private int idOfEndDepot;

    private int nbTrips;
    public Driver(int idOfDriver, int idOfStartDepot, int idOfEndDepot,int nbTrips){
        this.idOfDriver= idOfDriver;
        this.idOfStartDepot= idOfStartDepot;
        this.idOfEndDepot=idOfEndDepot;
        this.nbTrips=nbTrips;
    }

    public int getIdOfDriver() {
        return idOfDriver;
    }

    public void setIdOfStartDepot(int idOfStartDepot) {
        this.idOfStartDepot = idOfStartDepot;
    }

    public int getIdOfStartDepot() {
        return idOfStartDepot;
    }


    public int getIdOfEndDepot() {
        return idOfEndDepot;
    }



    @Override
    public String toString() {
        return  idOfDriver+" " + idOfStartDepot+" "+nbTrips;
    }

    public static void main(String[] args) {
        Driver driver = new Driver(0,0,0,2);
        System.out.println(driver);
    }
}
