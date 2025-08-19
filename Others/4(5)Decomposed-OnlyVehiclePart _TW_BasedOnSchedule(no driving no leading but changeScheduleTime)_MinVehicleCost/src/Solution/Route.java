package Solution;

import Instance.Instance;

import java.util.ArrayList;

public class Route {
    private Instance instance;
    private ArrayList<Node> nodes;

    public Route(Instance instance) {
        this.instance = instance;
//        this.vehiclePath = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addTripWithStartInfosInNodes(Node node){
        this.nodes.add(node);
    }

    public Node getNode(int index){
       return this.nodes.get(index);
    }

    public int getIdOfStartDepot() {

        return this.nodes.get(0).getId();
    }

    public int getIdOfEndingDepot() {
        return this.nodes.get(this.nodes.size()-1).getId();
    }

    public int getIdleTimeCostOfVehicleRoute(){
        int idleTimeCost=0;
        for (int i=1;i<this.nodes.size()-2;i++){
            Node node1=this.nodes.get(1);
            int startTime1=node1.getTripWithStartInfos().getStartingTime();
            int duartion1=node1.getTripWithStartInfos().getDuration();
            Node node2=this.nodes.get(2);
            int startTime2=node2.getTripWithStartInfos().getStartingTime();
            int idleTime=startTime2-startTime1-duartion1;
            idleTimeCost=idleTimeCost+instance.getIdleTimeCostForVehiclePerUnit()*idleTime;

        }
        return idleTimeCost;
    }

    public int getNbTripsPerformedByVehicle(){
        return this.nodes.size()-2;
    }
//

    /**
     * This is the last part, which is to check whether the path is feasible 1,2,3 steps ; In the meantime to create the basic some
     * when we set a cost, the arc has exist, so we already consider the min-plan time, it don't need to check? Yes because we give the arc
     * 1.check the leg is  start and the end are satified for each the aircraft and crew?// We dont need now I think
     * 2.check if the path, the every former leg destination == the latter origin?
     * if(this.instance.getLeg(i).getDestination().equals(this.instance.getLeg(j).getOrigin()))
     * 3.cost is correct be compute?
     * <p>
     * ps:In order to check all the leg has been visited, we need a method to tell us whether it has been showed in this path
     */

    @Override
    public String toString() {
        //iDVehicle; nbTrips; idStartDepot; Arraylist of [trip id]; idEndDepot
//        String s = vehiclePath.size() + " " + this.getIdOfStartDepot();

//        for (int i = 0; i < vehiclePath.size(); i++) {
//            s += " " + vehiclePath.get(i).getIdOfTrip();
//        }
//        s += " " + this.getIdOfEndingDepot();
//        return s;

        String s = this.nodes.size()-2 + " " + this.nodes.get(0).getId();
        for (int i = 1; i < this.nodes.size()-1;i++) {
            s += " " + this.nodes.get(i).getId();
        }
        s += " " + this.nodes.get(this.nodes.size()-1).getId();
        return s;

    }


}
