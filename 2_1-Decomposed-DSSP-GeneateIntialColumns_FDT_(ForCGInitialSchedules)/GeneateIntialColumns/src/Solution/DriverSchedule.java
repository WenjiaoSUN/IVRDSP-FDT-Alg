package Solution;

import Instance.Instance;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.util.ArrayList;
import java.util.Arrays;

public class DriverSchedule {
    //    private int idOfSchedule;//k
    private int idOfDepot;
    private int indexDepotAsStartingPoint;
    private int indexDepotAsEndingPoint;
    private ArrayList<TripWithWorkingStatusAndDepartureTime> schedule;
    private int nbDriverSchedules;
    private Instance instance;

    public DriverSchedule(Instance instance) {
        this.idOfDepot = Integer.MAX_VALUE;
        this.indexDepotAsStartingPoint = Integer.MAX_VALUE;
        this.indexDepotAsEndingPoint = Integer.MAX_VALUE;
        this.schedule = new ArrayList<>();
        this.nbDriverSchedules = 1;
        this.instance = instance;
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

    public void setIndexDepotAsStartingPoint(int indexDepotAsStartingPoint) {
        this.indexDepotAsStartingPoint = indexDepotAsStartingPoint;
    }

    public int getIndexDepotAsEndingPoint() {
        return indexDepotAsEndingPoint;
    }

    public void setIndexDepotAsEndingPoint(int indexDepotAsEndingPoint) {
        this.indexDepotAsEndingPoint = indexDepotAsEndingPoint;
    }

    public ArrayList<TripWithWorkingStatusAndDepartureTime> getSchedule() {
        return schedule;
    }

    public void setSchedule(ArrayList<TripWithWorkingStatusAndDepartureTime> schedule) {
        this.schedule = schedule;
    }

    public int getCoefficientA(int i) {
        int a_k_i = 0;// whether trip i showed up in the selected schedules;
        for (int l = 0; l < this.schedule.size(); l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus= this.schedule.get(l);
            int idOfTrip =tripWithWorkingStatus.getIdOfTrip();
            if (idOfTrip == i) {
                a_k_i = 1;
                break;
            }
        }
        return a_k_i;
    }

    public int getCoefficientG(int i) {
        int g_k_i = 0;// whether trip i is driving in this schedule;
        for (int l = 0; l < this.schedule.size(); l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus=this.schedule.get(l);
            int idOfTrip = tripWithWorkingStatus.getIdOfTrip();
            if (idOfTrip == i) {
                boolean whetherDriving = tripWithWorkingStatus.getDrivingStatus();
                if (whetherDriving == true) {
                    g_k_i = 1;
                } else {
                    g_k_i = 0;
                }
            }
        }
        return g_k_i;
    }

    public int getCoefficientForShortConnectionB(int i, int j) {//Pay attention here (i,j)only for short Connection
        int b_k_i_j = 0;
        for (int l = 0; l < this.schedule.size() - 1; l++) {
            int firstId = this.schedule.get(l).getIdOfTrip();
            int secondId = this.schedule.get(l + 1).getIdOfTrip();
            if (firstId == i && secondId == j) {
                b_k_i_j = 1;
            }
        }
        return b_k_i_j;
    }

    public int getCostC() {

        int c_k = (int) instance.getFixedCostForDriver();//fixed + idleTime cost+ changeover(only changeStatus)

        for (int l = 0; l < this.getSchedule().size() - 1; l++) {
            int firstId = this.getSchedule().get(l).getIdOfTrip();
            int secondId = this.getSchedule().get(l + 1).getIdOfTrip();
            boolean whetherDrivingInFirstTrip = this.getSchedule().get(l).getDrivingStatus();
            boolean whetherDrivingInSecondTrip = this.getSchedule().get(l + 1).getDrivingStatus();

            if ((this.getSchedule().get(l+1) .getMiddleDepartureTime() -(this.getSchedule().get(l).getMiddleDepartureTime()+this.getSchedule().get(l).getDuration()))
                    <Integer.MAX_VALUE) {
                 int idleTime=this.getSchedule().get(l+1) .getMiddleDepartureTime() -(this.getSchedule().get(l).getMiddleDepartureTime()+this.getSchedule().get(l).getDuration());

                c_k=(int)(c_k+instance.getIdleTimeCostForDriverPerUnit()*idleTime);
            }

            if (whetherDrivingInFirstTrip != whetherDrivingInSecondTrip) {
                c_k = (int) (c_k + instance.getCostForChangeOver(firstId, secondId));
            }

        }
        return c_k;
    }


    public void addTripWithWorkingStatusInSchedule(TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus) {
        this.schedule.add(tripWithWorkingStatus);
    }

    public int getNbDriverSchedules() {
        return nbDriverSchedules;
    }

    public void addNbSchedule(int nbDriverScheduleToAdd) {
        this.nbDriverSchedules = this.nbDriverSchedules + nbDriverScheduleToAdd;
    }

    public boolean isTheSame(DriverSchedule driverSchedule) {
        if (driverSchedule == null) return false;
        if (this.schedule.size() != driverSchedule.getSchedule().size()) {
            return false;
        }
        for (int i = 0; i < driverSchedule.getSchedule().size(); i++) {
            int idOfTripInThisSchedule = this.schedule.get(i).getIdOfTrip();
            boolean whetherDriving1 = this.schedule.get(i).getDrivingStatus();
            int idOfTripInParameterSchedule = driverSchedule.getSchedule().get(i).getIdOfTrip();
            boolean whetherDriving2 = driverSchedule.getSchedule().get(i).getDrivingStatus();
            if (idOfTripInThisSchedule != idOfTripInParameterSchedule || whetherDriving1 != whetherDriving2) {
                return false;
            }
        }
        return true;
    }

    public boolean whetherTripPresent(int idOfTrip) {
        boolean whetherTripPresent = false;
        for (int l = 0; l < this.schedule.size(); l++) {
            int id = this.schedule.get(l).getIdOfTrip();
            if (id == idOfTrip) {
                whetherTripPresent = true;
            }
        }
        return whetherTripPresent;
    }

    public boolean whetherStatusDriving(int idOfTrip) {//
        boolean whetherTripDriving = false;
        if (this.whetherTripPresent(idOfTrip)) {
            for (int l = 0; l < this.schedule.size(); l++) {
                if (this.schedule.get(l).getIdOfTrip() == idOfTrip) {
                    whetherTripDriving = this.schedule.get(l).getDrivingStatus();
                }
            }

        }
        return whetherTripDriving;

    }

    public boolean whetherArcExist(int idFormerTrip, int idLatterTrip) {
        boolean whetherTripExist = false;
        for (int l = 0; l < this.schedule.size() - 1; l++) {
            int idFirsTrip = this.schedule.get(l).getIdOfTrip();
            int idSecondTrip = this.schedule.get(l + 1).getIdOfTrip();
            if (idFirsTrip == idFormerTrip && idSecondTrip == idLatterTrip) {
                whetherTripExist = true;
            }
        }
        return whetherTripExist;
    }

//    public double getReducedCost(double[] dualForConstraintNbDriver, double[] dualForConstraintOneDriving, double[][] dualForLinkShortConnection,
//                                 double dualForAvailableNbDriver) {
//        double reducedCost = this.getCostC() - dualForAvailableNbDriver;
//
//        for (int l = 0; l < this.schedule.size(); l++) {
//            int idOfTrip = this.schedule.get(l).getIdOfTrip();
//            for (int i = 0; i < instance.getNbTrips(); i++) {
//                if (idOfTrip == i) {
//                    if (whetherTripPresent(i)) {
//                        reducedCost = reducedCost - dualForConstraintNbDriver[i];
//
//                        if (whetherStatusDriving(i)) {
//                            reducedCost = reducedCost - dualForConstraintOneDriving[i];
//
//                        }
//                    }
//                }
//            }
//
//        }
//
//        for (int f = 0; f < this.schedule.size() - 1; f++) {
//            int idFirst = this.schedule.get(f).getIdOfTrip();
//            int idSecond = this.schedule.get(f + 1).getIdOfTrip();
//            for (int i = 0; i < instance.getNbTrips(); i++) {
//                for (int j = 0; j < instance.getNbTrips(); j++) {
//                    if (idFirst == i && idSecond == j) {
//                        if (whetherArcExist(i, j)) {
//                            if (instance.getConnectionTime(i, j) == instance.getShortConnectionTimeForDriver()) {
//                                reducedCost = reducedCost - dualForLinkShortConnection[i][j];
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return reducedCost;
//    }


    @Override
    public String toString() {
        String s = idOfDepot + " " + indexDepotAsStartingPoint + " ";
        for (int l = 0; l < this.schedule.size(); l++) {
            s += this.schedule.get(l).getIdOfTrip() + " " + this.schedule.get(l).getDrivingStatus() + " "+this.schedule.get(l).getMiddleDepartureTime()+" ";
        }
        s += indexDepotAsEndingPoint + "\n";
        return s;
    }

    public static void main(String[] args) {

    }
}
