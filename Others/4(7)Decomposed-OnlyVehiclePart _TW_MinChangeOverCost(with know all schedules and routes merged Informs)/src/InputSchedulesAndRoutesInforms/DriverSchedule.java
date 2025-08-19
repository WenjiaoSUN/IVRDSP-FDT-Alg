package InputSchedulesAndRoutesInforms;

import Instance.Instance;
import  Instance.InstanceReader;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.util.ArrayList;

public class DriverSchedule {
    private int idOfDriver;
    private int idOfDepot;
    private int indexDepotAsStartingPoint;
    private int indexDepotAsEndingPoint;
    private ArrayList<TripWithWorkingStatusAndDepartureTime> tripWithWorkingStatusAndDepartureTimeArrayList;
    private int nbDriverSchedules;

    private Instance instance;

    public DriverSchedule(Instance instance) {
        this.idOfDriver=Integer.MAX_VALUE;
        this.idOfDepot = Integer.MAX_VALUE;
        this.indexDepotAsStartingPoint = Integer.MAX_VALUE;
        this.indexDepotAsEndingPoint = Integer.MAX_VALUE;
        this.tripWithWorkingStatusAndDepartureTimeArrayList = new ArrayList<>();
        this.nbDriverSchedules = 1;
        this.instance = instance;
    }

    public int getIdOfDriver() {
        return idOfDriver;
    }

    public int getIdOfDepot() {
        return idOfDepot;
    }

    public void setIdOfDepot(int idOfDepot) {
        this.idOfDepot = idOfDepot;
    }

    public int getIndexDepotAsStartingPoint() {
        return indexDepotAsStartingPoint;
    }

    public void setIndexDepotAsStartingPoint() {
        this.indexDepotAsStartingPoint =instance.getDepot(idOfDepot).getIndexOfDepotAsStartingPoint();
    }

    public int getIndexDepotAsEndingPoint() {
        return indexDepotAsEndingPoint;
    }

    public void setIndexDepotAsEndingPoint() {
        this.indexDepotAsEndingPoint =instance.getDepot(idOfDepot).getIndexOfDepotAsEndingPoint();
    }

    public void addTripWithWorkingStatusInSchedule(TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime) {
        this.tripWithWorkingStatusAndDepartureTimeArrayList.add(tripWithWorkingStatusAndDepartureTime);
    }

    public ArrayList<TripWithWorkingStatusAndDepartureTime> getTripWithWorkingStatusAndDepartureTimeArrayList() {
        return tripWithWorkingStatusAndDepartureTimeArrayList;
    }

    public void setTripWithWorkingStatusAndDepartureTimeArrayList(ArrayList<TripWithWorkingStatusAndDepartureTime> tripWithWorkingStatusAndDepartureTimeArrayList) {
        this.tripWithWorkingStatusAndDepartureTimeArrayList = tripWithWorkingStatusAndDepartureTimeArrayList;
    }


    //TRY TO SEE HERE for the values
    public int getCostC() {
        int c_k = (int) instance.getFixedCostForDriver();//fixed + idleTime cost+ changeover// change 2025.1.9
        int c_DI = instance.getIdleTimeCostForDriverPerUnit();
        for (int l = 0; l < this.getTripWithWorkingStatusAndDepartureTimeArrayList().size() - 1; l++) {
            int firstId = this.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getIdOfTrip();
            int secondId = this.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l + 1).getIdOfTrip();
            boolean whetherDrivingInFirstTrip = this.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getDrivingStatus();
            boolean whetherDrivingInSecondTrip = this.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l + 1).getDrivingStatus();
            TripWithWorkingStatusAndDepartureTime tripWitStatusAndDepTime1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            TripWithWorkingStatusAndDepartureTime tripWithStatusAndDepTime2 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1);

            if (instance.whetherHavePossibleArcAfterCleaning(firstId, secondId)) {
                int conTime = tripWithStatusAndDepTime2.getDepartureTime()
                        - tripWitStatusAndDepTime1.getDepartureTime() - tripWitStatusAndDepTime1.getDuration();
                c_k = (int) (c_k + c_DI * conTime);
            }
            if (whetherDrivingInFirstTrip != whetherDrivingInSecondTrip) {
                c_k = (int) (c_k + instance.getCostForChangeOver());
            }

        }
        if (c_k < 0) {
            System.out.println(" error neg cost in driver schedule " + c_k);
        }
        return c_k;
    }


    public boolean whetherIsShortConnectionInDriverSchedule(int idTrip1, int idTrip2) {
        boolean whetherShortConnection = false;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime2 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1);
            int conTime = tripWithWorkingStatusAndDepartureTime2.getDepartureTime()
                    - tripWithWorkingStatusAndDepartureTime1.getDepartureTime() - tripWithWorkingStatusAndDepartureTime1.getDuration();
            if (tripWithWorkingStatusAndDepartureTime1.getIdOfTrip() == idTrip1 &&
                    tripWithWorkingStatusAndDepartureTime2.getIdOfTrip() == idTrip2
                    && conTime <= instance.getShortConnectionTimeForDriver()
            ) {
                whetherShortConnection = true;
            }

        }
        return whetherShortConnection;
    }

    public int getNbDriverSchedules() {
        return nbDriverSchedules;
    }

    public void addNbSchedule(int nbDriverScheduleToAdd) {
        this.nbDriverSchedules = this.nbDriverSchedules + nbDriverScheduleToAdd;
    }



    public boolean whetherTripPresent(int idOfTrip) {
        boolean whetherTripPresent = false;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            int id = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getIdOfTrip();
            if (id == idOfTrip) {
                whetherTripPresent = true;
            }
        }
        return whetherTripPresent;
    }

    public boolean whetherStatusDriving(int idOfTrip) {//
        boolean whetherTripDriving = false;
        if (this.whetherTripPresent(idOfTrip)) {
            for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
                if (this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getIdOfTrip() == idOfTrip) {
                    whetherTripDriving = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDrivingStatus();
                }
            }

        }
        return whetherTripDriving;

    }

    public boolean whetherArcExist(int idFormerTrip, int idLatterTrip) {
        boolean whetherTripExist = false;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; l++) {
            int idFirsTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getIdOfTrip();
            int idSecondTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1).getIdOfTrip();
            if (idFirsTrip == idFormerTrip && idSecondTrip == idLatterTrip) {
                whetherTripExist = true;
            }
        }
        return whetherTripExist;
    }

    public void setIdOfDriver(int idOfDriver) {
        this.idOfDriver = idOfDriver;
    }

    @Override
    public String toString() {
        return "DriverSchedule{" +
                "idOfDriver=" + idOfDriver +
                ", idOfDepot=" + idOfDepot +
                ", tripWithWorkingStatusAndDepartureTimeArrayList=" + tripWithWorkingStatusAndDepartureTimeArrayList +
                ", nbDriverSchedules=" + nbDriverSchedules +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity05_Size180_Day1_nbTrips020_combPer0.1.txt");//test vehicle example
        //inst_nbCity20_Size300_Day3_nbTrips300_combPer0.1
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus0 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(0), true, 1);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus8 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(8), false, 20);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus9 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(9), true, 15);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus5 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(5), true, 19);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus14 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(14), true, 20);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus15 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(15), true, 45);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus18 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(18), true, 55);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus19 = new TripWithWorkingStatusAndDepartureTime(instance.getTrip(19), true, 68);

        int idOfDepot = 1;
        int indexOfStartingDepot = 21;
        int indexOfEndingDepot = 23;
        int nbDriverSchedules = 1;


        // System.out.println("driver tripWithWorkingStatusAndDepartureTimeArrayList check:"+driverSchedule.getCostC());

    }

}
