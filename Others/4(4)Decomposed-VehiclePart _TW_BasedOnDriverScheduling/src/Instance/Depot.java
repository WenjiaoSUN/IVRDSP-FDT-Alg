package Instance;

public class Depot {
    private int idOfDepot;
    private int indexOfDepotAsStartingPoint;
    private int indexOfDepotAsEndingPoint;
    private int idOfCityAsDepot;



    public Depot(int idOfDepot, int indexOfDepotAsStartingPoint, int indexOfDepotAsEndingPoint, int idOfCityAsDepot){
        this.idOfDepot=idOfDepot;
        this.indexOfDepotAsStartingPoint = indexOfDepotAsStartingPoint;
        this.indexOfDepotAsEndingPoint = indexOfDepotAsEndingPoint;
        this.idOfCityAsDepot=idOfCityAsDepot;

    }

    public int getIdOfDepot(){return idOfDepot;}

    public int getIndexOfDepotAsStartingPoint() {
        return indexOfDepotAsStartingPoint;
    }

    public int getIndexOfDepotAsEndingPoint() {
        return indexOfDepotAsEndingPoint;
    }

    public int getIdOfCityAsDepot(){return  idOfCityAsDepot;}

    @Override
    public String toString() {
        return "Depot{" +
                "idOfDepot=" + idOfDepot +
                ", indexOfDepotAsStartingPoint=" + indexOfDepotAsStartingPoint +
                ", indexOfDepotAsEndingPoint=" + indexOfDepotAsEndingPoint+
                ", idOfCityAsDepot=" + idOfCityAsDepot +
                '}';
    }

    public static void main(String[] args) {
        Depot depot = new Depot(0,27,28,2);

    }

}
