package Instance;

public class Driver {
    private int idOfDriver;
    private int idOfStartDepot;
    private int idOfEndDepot;



    public Driver(int idOfDriver, int idOfStartDepot,int nbTrips) {
        this.idOfDriver = idOfDriver;
        this.idOfStartDepot = idOfStartDepot;
        this.idOfEndDepot = idOfStartDepot;
    }


    public int getIdOfDriver() {
        return idOfDriver;
    }

    public int getIdOfStartDepot() {
        return idOfStartDepot;
    }

    public int getIdOfEndDepot() {
        return idOfEndDepot;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "idOfDriver=" + idOfDriver +
                ", idOfStartDepot=" + idOfStartDepot +
                ", idOfEndDepot=" + idOfEndDepot +
                '}';
    }
}
