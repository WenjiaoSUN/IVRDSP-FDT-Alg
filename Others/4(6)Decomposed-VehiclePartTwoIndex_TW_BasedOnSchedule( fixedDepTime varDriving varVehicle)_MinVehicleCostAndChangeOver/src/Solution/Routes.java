package Solution;

import Instance.Instance;
import PathsForDriver.DriverSchedule;
import PathsForDriver.Schedules;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Routes {
    private Instance instance;
    private ArrayList<Route> routesForVehicles;

    public Routes(Instance instance){
        this.instance=instance;
        this.routesForVehicles =new ArrayList<Route>();
    }

    public ArrayList<Route> getRoutes() {
        return this.routesForVehicles;
    }
    public void addRoute(Route route){
        this.routesForVehicles.add(route);
    }

    public double getFixedCostForAllVehiclesInPlanning() {
        return  this.instance.getFixedCostForVehicle()*this.routesForVehicles.size();
    }

    public double getIdleTimeCostOfAllVehiclesInPlanning(){
        double cost=0;
        for(int r=0;r<this.routesForVehicles.size();r++){
            cost=cost+this.routesForVehicles.get(r).getIdleTimeCostOfVehicleRoute();
        }
        return cost;
    }

    public double getTotalCost(){
        return this.getFixedCostForAllVehiclesInPlanning()+this.getIdleTimeCostOfAllVehiclesInPlanning();
    }

    public int getNbVehicleInSolution(){
        return this.getRoutes().size();
    }

    public void addAllRoutes(Routes routes){
        for(Route route:routes.getRoutes()){
            this.routesForVehicles.add(route);
        }
    }


    public void printInfile(String fileName) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName);
            writer.println("// The mini cost:" + this.getTotalCost() + " The fixed cost for vehicle:" + this.getFixedCostForAllVehiclesInPlanning());
            writer.println("//The idle cost for vehicle:" + this.getIdleTimeCostOfAllVehiclesInPlanning());
            writer.println("//vehicle idVehicle nbTrips idDepotStart [idTrips] idDepotEnd");
            for (int r=0;r< this.routesForVehicles.size();r++) {
                writer.println("vehicle " +r +" "+ this.routesForVehicles.get(r));
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    @Override
    public String toString() {

        String s="Route"+"\n";

        for (Route route : this.routesForVehicles) {
            s+=route+"\n";

            }
        return s;
    }
}
