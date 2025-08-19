package InstanceGet;

public class Depot {
    private int idOfDepot;

    private int indexOfDepotAsStarting;

    private int indexOfDepotAsEnding;
    private int idOfCityAsDepot;

    public Depot(int idOfDepot, int indexOfDepotAsStarting, int indexOfDepotAsEnding, int idOfCityAsDepot) {
        this.idOfDepot = idOfDepot;
        this.indexOfDepotAsStarting = indexOfDepotAsStarting;
        this.indexOfDepotAsEnding=indexOfDepotAsEnding;
        this.idOfCityAsDepot = idOfCityAsDepot;
    }
    public int getIdOfDepot(){return idOfDepot;}

    public int getIndexOfDepotAsStarting() {
        return indexOfDepotAsStarting;
    }

    public int getIndexOfDepotAsEnding() {
        return indexOfDepotAsEnding;
    }

    public int getIdOfCityAsDepot(){return idOfCityAsDepot;}


    public void setIdOfDepot(int idOfDepot) {
        this.idOfDepot = idOfDepot;
    }


    @Override
    public String toString() {
        return  idOfDepot+" "+ indexOfDepotAsStarting+" "+ indexOfDepotAsEnding+" "+idOfCityAsDepot
        ;
    }

    public static void main(String[] args) {
        Depot depot = new Depot(19, 10,11,0);
        System.out.println(depot);

    }
}
