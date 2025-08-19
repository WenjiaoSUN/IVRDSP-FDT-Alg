package InputSchedulesAndRoutesInforms;
import java.util.ArrayList;
import Solution.TripWithStartingInfos;
import Instance.Instance;

public class VehicleRoute {
    private  int idVehicle;
    private int idOfStartDepot;
    private int idOfEndDepot;
    private int indexDepotAsStartingPoint;
    private int indexDepotAsEndingPoint;
    private Instance instance;

    private ArrayList<TripWithStartingInfos> tripWithStartingInfosArrayList;

    public VehicleRoute(Instance instance){
        this.idVehicle=Integer.MAX_VALUE;
        this.instance=instance;
        this.idOfStartDepot = Integer.MAX_VALUE;
        this.idOfEndDepot=Integer.MAX_VALUE;
        this.indexDepotAsStartingPoint = Integer.MAX_VALUE;
        this.indexDepotAsEndingPoint =Integer.MAX_VALUE;
        this.tripWithStartingInfosArrayList = new ArrayList<>();


    }

    public int getIdOfStartDepot() {
        return idOfStartDepot;
    }

    public void setIdVehicle(int idVehicle){this.idVehicle=idVehicle;}

    public void setIdOfStartDepot(int idOfDepot) {
        this.idOfStartDepot = idOfDepot;
    }

    public void setIdOfEndDepot(int idOfEndDepot){this.idOfEndDepot=idOfEndDepot;}

    public void setIndexDepotAsStartingPoint() {
        this.indexDepotAsStartingPoint = instance.getDepot(idOfStartDepot).getIndexOfDepotAsStartingPoint();
    }

    public void setIndexDepotAsEndingPoint() {
        this.indexDepotAsEndingPoint = instance.getDepot(idOfEndDepot).getIndexOfDepotAsEndingPoint();
    }

    public void addTripWithStartInforms(TripWithStartingInfos tripWithStartingInfos){this.tripWithStartingInfosArrayList.add(tripWithStartingInfos);}

    public boolean whetherTripPresent(int idOfTrip) {
        boolean whetherTripPresent = false;
        for (int l = 0; l < this.tripWithStartingInfosArrayList.size(); l++) {
            int id = this.tripWithStartingInfosArrayList.get(l).getIdOfTrip();
            if (id == idOfTrip) {
                whetherTripPresent = true;
            }
        }
        return whetherTripPresent;
    }

    public boolean whetherPerformArc(int idFTrip,int idSTrip){
        boolean whetherPerformArc=false;
        for(int i=0;i<this.tripWithStartingInfosArrayList.size()-1;i++){
            int idFTripInRoute=this.tripWithStartingInfosArrayList.get(i).getIdOfTrip();
            int idSTripInRoute=this.tripWithStartingInfosArrayList.get(i+1).getIdOfTrip();
            if(idFTrip ==idFTripInRoute&&idSTrip==idSTripInRoute){
                whetherPerformArc=true;
            }
        }
        return whetherPerformArc;
    }

    public boolean whetherStartDepot(int idOfStartDepot){
        if(idOfStartDepot==this.idOfStartDepot){
            return true;
        }else {
            return false;
        }
    }

    public ArrayList<TripWithStartingInfos> getTripWithStartingInfosArrayList() {
        return tripWithStartingInfosArrayList;
    }

    @Override
    public String toString() {
        return "VehicleRoute{" +
                "idVehicle=" + idVehicle +
                ", idOfStartDepot=" + idOfStartDepot +
                ", idOfEndDepot=" + idOfEndDepot +
                ", indexDepotAsStartingPoint=" + indexDepotAsStartingPoint +
                ", indexDepotAsEndingPoint=" + indexDepotAsEndingPoint +
                ", tripWithStartingInfosArrayList=" + tripWithStartingInfosArrayList +
                '}';
    }
}
