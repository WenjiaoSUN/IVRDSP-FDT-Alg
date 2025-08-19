package Solution;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;

import java.util.ArrayList;

//This file describes one example path for one aircraft

public class PathForVehicle {
    private Instance instance;
    private int idOfVehicle;
    private ArrayList<TripWithStartingInfos> vehiclePath;

    public PathForVehicle(Instance instance, int idOfVehicle) {
        this.instance = instance;
        this.idOfVehicle = idOfVehicle;
        this.vehiclePath = new ArrayList<>();
    }

    public ArrayList<TripWithStartingInfos> getTripsInPath() {
        return vehiclePath;
    }

    public TripWithStartingInfos getTripWithStartingInfos(int idOfTrip) {
        return this.vehiclePath.get(idOfTrip);
    }

    public void addTripInVehiclePath(TripWithStartingInfos tripWithStartingInfos) {
        this.vehiclePath.add(tripWithStartingInfos);
    }

    public int getIdOfVehicle() {
        return idOfVehicle;
    }

    private TripWithStartingInfos getLastTripWithStartingInfos() {
        return this.vehiclePath.get(this.vehiclePath.size() - 1);
    }

    private TripWithStartingInfos getFirstTripWithStartingInfos() {
        return this.vehiclePath.get(0);
    }

    public double getIdleTimeCostOfVehiclePath() {
        double cost = 0;
        for (int i = 0; i < this.vehiclePath.size() - 1; i++) {

            TripWithStartingInfos formerTripWithStartingInfos = this.vehiclePath.get(i);
            int idOfFormerTrip = formerTripWithStartingInfos.getIdOfTrip();
            TripWithStartingInfos latterTripWithStartingInfos = this.vehiclePath.get(i + 1);
            int idOfLatterTrip = latterTripWithStartingInfos.getIdOfTrip();

            int formerStartingTime = formerTripWithStartingInfos.getStartingTimeUnit();
            int latterStartingTime = latterTripWithStartingInfos.getStartingTimeUnit();

            Trip formerTrip = instance.getTrip(idOfFormerTrip);
            int durationOfFormerTrip = formerTrip.getDuration();
            int connectionTime = (latterStartingTime - formerStartingTime) * instance.getTimeSlotUnit() - durationOfFormerTrip;

            if (this.instance.whetherHavePossibleArcAfterCleaning(idOfFormerTrip, idOfLatterTrip)) {
                if (connectionTime >= this.instance.getMinPlanTurnTime()) {
                    cost = cost + this.instance.getIdleTimeCostForVehiclePerUnit() * connectionTime;
                    cost = Math.round(100.0 * cost) / 100.0;
                }
            } else {
                System.out.println("ERROR !!!! method getCostOfPathForVehicle in class PathForVehicle");
                System.out.println("vehicle id: " + this.idOfVehicle + "seems no possible arc between them");
                System.out.println("idOfFormerTrip = " + idOfFormerTrip);
                System.out.println("idOfLatterTrip = " + idOfLatterTrip);
            }
        }
        return cost;
    }


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
    //Here is different
    public boolean isFeasible(boolean verbose) {
        // verbose is to remind us whether to give a hint of the result of Feasible
        boolean feas = true;
        //2.judge whether leg can be connected as a path for the aircraft
        for (int i = 0; i < this.vehiclePath.size() - 1; i++) {
            int idOfEndCityCurrent = this.vehiclePath.get(i).getIdOfEndCity();
            int idOfStartCityNext = this.vehiclePath.get(i + 1).getIdOfStartCity();
            if (idOfEndCityCurrent != (idOfStartCityNext)) {
                feas = false;
                // if unfeasible, then whether it output the reason?-------------------it decide on verbose
                if (verbose) {
                    System.out.println(" For the vehicle " + idOfVehicle
                            + " the destination city of trip_ " + i + "is " + this.vehiclePath.get(i).getIdOfEndCity()
                            + " isn't the origin city of the trip" + (i + 1) + "is " + this.vehiclePath.get(i + 1).getIdOfStartCity());
                }
            }

        }
        //3.cost is correct be compute?
        if (this.getIdleTimeCostOfVehiclePath() == Double.MAX_VALUE) {
            feas = false;
            if (verbose) {
                System.out.println("The cost of this vehicle" + this.idOfVehicle + " trip is greater than the normal value.");
            }
        }
        return feas;
    }

    /**
     * ps: preparing for the class "solution"
     * this is to help to judge whether the solution is feasible considering the driver short connection time case
     */
    public boolean areConsecutiveInPathOfVehicle(Trip lFirst, Trip lSecond) {
        for (int i = 0; i < this.vehiclePath.size() - 1; i++) {
            if (this.vehiclePath.get(i).equals(lFirst)
                    && this.vehiclePath.get(i + 1).equals(lSecond)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ps:In order to check all the trip has been visited in the class "solution"
     * we need a method to tell us whether it has been showed in this path
     */

    public boolean isPresentInPath(Trip trip) {
        for (int i = 0; i < this.vehiclePath.size(); i++) {
            if (this.vehiclePath.get(i).equals(trip)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "PathForVehicle{" +
                " idOfVehicle=" + idOfVehicle +
//                ", costOfPathForVehicle=" + this.getCostOfPath() +
                ", vehiclePath=" + vehiclePath +
                " };" ;
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity05_Size180_Day2_nbTrips200_combPer0.1.txt");
        Instance instance = reader.readFile();


        // boolean isPresent = pathForAircraft.isPresentInPathOfAircraft(instance.getLeg(2));

        //System.out.println("The path for the second vehicle is feasible: true? " + feasible7+"id"+pathForVehicle7.getIdOfVehicle());

    }


}
