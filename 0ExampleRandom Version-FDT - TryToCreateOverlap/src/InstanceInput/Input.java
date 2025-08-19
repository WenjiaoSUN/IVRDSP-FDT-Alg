package InstanceInput;

public class Input {

    private int distanceX;

    private int distanceY;
    private int nbCities;
    private int nbTrips;
    private int nbDepots;

    private double maxPercentageCombinedTrip;

    private int maxFolderOfTimeSlotUnitAsTimeWindow;

    //private int nbVehicles;
    //private int nbDrivers;

    private int startHorizon;

    private int endHorizon;
    private int minPlanTime; //decide the minTime exist an arc in the graph
    private int maxPlanTime; //decide the maxTime exist an arc in the graph
    private int maxWaitingTime;//decide the maxConnection time in our shift when consider two trips connectionTime
    private int shortTimeForDriver;
    private int shortTimeForVehicle;
    private int maxDrivingTime;
    private int maxWorkingTime;

    private int costUseVehicle;

    private int costUseDriver;

    private int costOfIdlePerUnitForVehicle;

    private int costOfIdlePerUnitForDriver;

    private int costOfChangeOverPerChange;

    private double keyPercentTimeBackToDepot;

    private int maxChargingTimeForVehicleInDepot;
    private int timeSlotUnit;

    private double scale_DisAndDur;


    public Input(int distanceX, int distanceY, int nbCities, int nbTrips, double maxPercentageCombinedTrip,int maxFolderOfTimeSlotUnitAsTimeWindow, int nbDepots,
                 int startHorizon, int endHorizon, int minPlanTime, int maxPlanTime, int maxWaitingTime, int shortTimeForDriver,
                 int shortTimeForVehicle, int maxDrivingTime, int maxWorkingTime, int costUseVehicle, int costUseDriver,
                 int costOfIdlePerUnitForVehicle, int costOfIdlePerUnitForDriver, int costOfChangeOverPerChange,
                 double keyPercentTimeBackToDepot, int maxChargingTimeForVehicleInDepot, int timeSlotUnit, double scale_DisAndDur) {
        this.distanceX = distanceX;
        this.distanceY = distanceY;
        this.nbCities = nbCities;
        this.nbTrips = nbTrips;
        this.maxPercentageCombinedTrip = maxPercentageCombinedTrip;
        this.maxFolderOfTimeSlotUnitAsTimeWindow=maxFolderOfTimeSlotUnitAsTimeWindow;
        this.nbDepots = nbDepots;
        this.startHorizon=startHorizon;
        this.endHorizon=endHorizon;
        this.minPlanTime = minPlanTime;//decide the minTime exist an arc in the graph
        this.maxPlanTime=maxPlanTime;
        this.maxWaitingTime= maxWaitingTime;
        this.shortTimeForDriver = shortTimeForDriver;
        this.shortTimeForVehicle = shortTimeForVehicle;
        this.maxDrivingTime = maxDrivingTime;
        this.maxWorkingTime = maxWorkingTime;
        this.costUseVehicle=costUseVehicle;
        this.costUseDriver=costUseDriver;
        this.costOfIdlePerUnitForVehicle=costOfIdlePerUnitForVehicle;
        this.costOfIdlePerUnitForDriver=costOfIdlePerUnitForDriver;
        this.costOfChangeOverPerChange=costOfChangeOverPerChange;
        this.keyPercentTimeBackToDepot = keyPercentTimeBackToDepot;
        this.maxChargingTimeForVehicleInDepot=maxChargingTimeForVehicleInDepot;
        this.timeSlotUnit=timeSlotUnit;
        this.scale_DisAndDur=scale_DisAndDur;
    }

    public int getDistanceX() {
        return distanceX;
    }
    public int getDistanceY() {
        return distanceY;
    }

    public int getNbCities() {
        return nbCities;
    }
    public int getNbTrips() {
        return nbTrips;
    }
    public int getNbDepots() {
        return nbDepots;
    }

    public int getMinPlanTime() {
        return minPlanTime;
    }

    public int getMaxPlanTime() {return maxPlanTime;}

    public int getMaxWaitingTime(){return maxWaitingTime;}
    public int getShortTimeForDriver() {
        return shortTimeForDriver;
    }
    public int getShortTimeForVehicle() {
        return shortTimeForVehicle;
    }

    public int getMaxDrivingTime() {
        return maxDrivingTime;
    }

    public int getMaxWorkingTime() {
        return maxWorkingTime;
    }

    public double getKeyPercentTimeBackToDepot() {
        return keyPercentTimeBackToDepot;
    }

    public int getMaxChargingTimeForVehicleInDepot() {
        return maxChargingTimeForVehicleInDepot;
    }

    public int getStartHorizon() {
        return startHorizon;
    }

    public int getEndHorizon() {
        return endHorizon;
    }

    public double getMaxPercentageCombinedTrip() {
        return maxPercentageCombinedTrip;
    }

    public int getCostUseVehicle() {
        return costUseVehicle;
    }

    public int getCostUseDriver() {
        return costUseDriver;
    }

    public int getCostOfIdlePerUnitForVehicle() {
        return costOfIdlePerUnitForVehicle;
    }

    public int getCostOfIdlePerUnitForDriver() {
        return costOfIdlePerUnitForDriver;
    }

    public int getCostOfChangeOverPerChange() {
        return costOfChangeOverPerChange;
    }

    public int getTimeSlotUnit(){return timeSlotUnit;}

    public int getFolderOfTimeSlotUnitAsTimeWindow(){return maxFolderOfTimeSlotUnitAsTimeWindow;}

    public double getScale_DisAndDur(){return scale_DisAndDur;}



    @Override
    public String toString() {
        return "Input{" +
                "distanceX=" + distanceX +
                ", distanceY=" + distanceY +
                ", nbCities=" + nbCities +
                ", nbTrips=" + nbTrips +
                ", nbDepots=" + nbDepots +
                ", maxPercentageCombinedTrip=" + maxPercentageCombinedTrip +
                ", startHorizon=" + startHorizon +
                ", endHorizon=" + endHorizon +
                ", minPlanTime=" + minPlanTime +
                ", maxPlanTime=" + maxPlanTime +
                ", maxWaitingTime=" + maxWaitingTime +
                ", shortTimeForDriver=" + shortTimeForDriver +
                ", shortTimeForVehicle=" + shortTimeForVehicle +
                ", maxDrivingTime=" + maxDrivingTime +
                ", maxWorkingTime=" + maxWorkingTime +
                ",kePercentageGoBackToDepot =" + keyPercentTimeBackToDepot +
                ", costUseVehicle=" + costUseVehicle +
                ", costUseDriver=" + costUseDriver +
                ", costOfIdlePerUnitForVehicle=" + costOfIdlePerUnitForVehicle +
                ", costOfIdlePerUnitForDriver=" + costOfIdlePerUnitForDriver +
                ", costOfChangeOverPerChange=" + costOfChangeOverPerChange +
                ", timeSlotUnit="+timeSlotUnit+
                '}';
    }


    public static void main(String[] args) {
        Input input = new Input(1,1,5, 7,  0.1,1,2, 5,23,1,
                720,5,1, 1, 5, 7,10,8,1,
                2,3,0.1,120,5,0.95);
        System.out.println(input);
    }
}

