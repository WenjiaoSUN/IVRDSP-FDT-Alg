package Generator;

import Instance.Instance;
import Instance.Depot;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.util.ArrayList;
import java.util.List;

public class Label {
    private Instance instance;
    private Trip startingTrip;//original trip
    private Trip trip;//current trip

//    private int departureTime;// add attributes
//
//    private boolean workingStatus;// add attributes

    private ArrayList<Trip> sequenceOfTrips;// all the trips in front
    private double cost; // sum of cost
    private double totalDrivingTime;// sum of driving time
    private double totalWorkingTime;// sum of working time
    private int endId;

    public Label(Instance instance, Trip startingTrip) {
        this.instance = instance;
        this.startingTrip = startingTrip;
        this.trip = null;
//        this.departureTime = Integer.MAX_VALUE;//default value
//        this.workingStatus = false;//default value
        this.cost = 0;
        this.totalDrivingTime = 0;
        this.totalWorkingTime = 0;
        this.sequenceOfTrips = new ArrayList<>();
        this.endId = -1;// means there is no good ends for this starting Trip
    }

    public Trip getTrip() {
        return trip;
    }

    public double getCost() {
        return cost;
    }

    public double getTotalDrivingTime() {
        return totalDrivingTime;
    }

    public double getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public int getEndId() {
        return endId;
    }

    //following add these two new attributes consider time window
//
//    public int getDepartureTime() {
//        return departureTime;
//    }
//
//    public boolean getWorkingStatus() {
//        return workingStatus;
//    }
    //above add these two new attributes consider time window


    public ArrayList<Trip> getSequenceOfTrips() {
        return sequenceOfTrips;
    }

    public boolean isDominated(Label label1, Label label2) {
        boolean whetherLabel1IsDominatedLabel2 = false;//non-dominated
        if (label1.getCost() <= label2.getCost()
                && label1.getTotalWorkingTime() >= label2.getTotalWorkingTime()
                && label1.totalDrivingTime >= label2.getTotalWorkingTime()) {
            whetherLabel1IsDominatedLabel2 = true;
        }
        return whetherLabel1IsDominatedLabel2;
    }

    public List<Label> getNonDominatedLabels(List<Label> labels) {
        List<Label> nonDominatedLabels = new ArrayList<>();

        for (Label label : labels) {
            boolean isDominated = false;
            List<Label> dominatedLabels = new ArrayList<>();

            for (Label existingLabel : nonDominatedLabels) {
                if (isDominated(label, existingLabel)) {
                    isDominated = true;
                    break;
                } else if (isDominated(existingLabel, label)) {
                    dominatedLabels.add(existingLabel);
                }
            }

            if (!isDominated) {
                nonDominatedLabels.removeAll(dominatedLabels);
                nonDominatedLabels.add(label);
            }
        }

        return nonDominatedLabels;
    }

    public Label extend(Trip extendTrip) {
        Label newLabel = new Label(this.instance, this.startingTrip);
        //case1: consider extend to the end point
        if (extendTrip == null) {
            if (this.trip.getIdOfEndCity() != this.startingTrip.getIdOfStartCity()) {
                return null;
            }
            newLabel.cost = this.getCost();
            newLabel.totalDrivingTime = this.getTotalDrivingTime();
            newLabel.totalWorkingTime = this.getTotalWorkingTime();
            newLabel.sequenceOfTrips = new ArrayList<>(this.sequenceOfTrips);
            for (int p = 0; p < instance.getNbDepots(); p++) {
                Depot depot = instance.getDepot(p);
                if (depot.getIdOfCityAsDepot() == this.startingTrip.getIdOfStartCity()) {
                    newLabel.endId = depot.getIndexOfDepotAsEndingPoint();
                }

            }
            return newLabel;
        }
        //case2: consider this is the starting point
        if (this.sequenceOfTrips.isEmpty()) {
            if (!extendTrip.equals(this.startingTrip)) {
                return null;
            }
            newLabel.cost = 0;

            newLabel.totalDrivingTime = extendTrip.getDuration();
            newLabel.totalWorkingTime = extendTrip.getDuration();

            newLabel.sequenceOfTrips.add(extendTrip);
            newLabel.trip = extendTrip;

            return newLabel;
        }
        //case3: general case, add trip after the last predecessor
        //judge whether the current trip can be extended to the parameter trip, which means the two trips can be connected
        //if it could extend, update the label
        //otherwise, it will return null;
        if (instance.whetherHavePossibleArcAfterCleaning(this.trip.getIdOfTrip(), extendTrip.getIdOfTrip()) == false) {
            return null;
        }else if ((extendTrip.getMiddleDepartureTime()) - (this.trip.getMiddleDepartureTime() + this.trip.getDuration())<instance.getMinPlanTurnTime())
        {
            return  null;
        }
        else {
            int idleTime = (extendTrip.getMiddleDepartureTime()) - (this.trip.getMiddleDepartureTime() + this.trip.getDuration());
            newLabel.cost = this.getCost() + instance.getIdleTimeCostForDriverPerUnit() * idleTime;//sum cost
            newLabel.totalDrivingTime = this.getTotalDrivingTime() + extendTrip.getDuration();
            newLabel.totalWorkingTime = this.getTotalWorkingTime() + idleTime + extendTrip.getDuration();// sumWorkingTime

            newLabel.sequenceOfTrips = new ArrayList<>(this.sequenceOfTrips);
            newLabel.sequenceOfTrips.add(extendTrip);
            newLabel.trip = extendTrip;

            return newLabel;
        }
    }

    @Override
    public String toString() {
        return "Label{" +
                "trip=" + trip +
                ", sequenceOfTrips=" + sequenceOfTrips +
                ", cost=" + cost +
                ", totalDrivingTime=" + totalDrivingTime +
                ", totalWorkingTime=" + totalWorkingTime +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");//test vehicle example
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        Label label = new Label(instance, instance.getTrip(0));
        Label label1 = label.extend(instance.getTrip(0));

        Label label2 = label1.extend(instance.getTrip(1));
        System.out.println(label1);
        System.out.println(label2);

    }

}
