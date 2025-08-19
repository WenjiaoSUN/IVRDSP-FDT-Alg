package Solution;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.util.ArrayList;

public class DriverSchedule {
    //    private int idOfSchedule;//k
    private int idOfDepot;
    private int indexDepotAsStartingPoint;
    private int indexDepotAsEndingPoint;
    private ArrayList<TripWithWorkingStatusAndDepartureTime> tripWithWorkingStatusAndDepartureTimeArrayList;
    private int nbDriverSchedules;

    private Instance instance;

    public DriverSchedule(Instance instance) {
        this.idOfDepot = Integer.MAX_VALUE;
        this.indexDepotAsStartingPoint = Integer.MAX_VALUE;
        this.indexDepotAsEndingPoint = Integer.MAX_VALUE;
        this.tripWithWorkingStatusAndDepartureTimeArrayList = new ArrayList<>();
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

    public void addTripWithWorkingStatusInSchedule(TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime) {
        this.tripWithWorkingStatusAndDepartureTimeArrayList.add(tripWithWorkingStatusAndDepartureTime);
    }

    public ArrayList<TripWithWorkingStatusAndDepartureTime> getTripWithWorkingStatusAndDepartureTimeArrayList() {
        return tripWithWorkingStatusAndDepartureTimeArrayList;
    }

    public void setTripWithWorkingStatusAndDepartureTimeArrayList(ArrayList<TripWithWorkingStatusAndDepartureTime> tripWithWorkingStatusAndDepartureTimeArrayList) {
        this.tripWithWorkingStatusAndDepartureTimeArrayList = tripWithWorkingStatusAndDepartureTimeArrayList;
    }




    public int getCoefficientF(int i, int t){
        int f_k_i_t=0;
        for(int l = 0; l<this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++){
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime= this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip=tripWithWorkingStatusAndDepartureTime.getIdOfTrip();
            int departureTime=tripWithWorkingStatusAndDepartureTime.getDepartureTime();
            if(idOfTrip==i&&departureTime==t){
                f_k_i_t=1;
                break;
            }
        }
        return f_k_i_t;

    }

    public int getCoefficientA(int i) {
        int a_k_i = 0;// whether trip i showed up in the selected schedules;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus= this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip =tripWithWorkingStatus.getIdOfTrip();
            if (idOfTrip == i) {
                a_k_i = 1;
                break;
            }
        }
        return a_k_i;
    }

    public int getCoefficientG(int i) {
        int g_k_i = 0;// whether trip i is driving in this tripWithWorkingStatusAndDepartureTimeArrayList;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus=this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip = tripWithWorkingStatus.getIdOfTrip();
            if (idOfTrip == i) {
                boolean whetherDriving = tripWithWorkingStatus.getDrivingStatus();
                if (whetherDriving == true) {
                    g_k_i = 1;
                } else {
                    g_k_i = 0;
                }
                break;
            }
        }
        return g_k_i;
    }

    public int getCoefficientForShortConnectionB(int i, int j) {//Pay attention here (i,j)only for short Connection
        int b_k_i_j = 0;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; l++) {
            TripWithWorkingStatusAndDepartureTime trip1=this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            TripWithWorkingStatusAndDepartureTime trip2=this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l+1);
            int firstId =trip1.getIdOfTrip();
            int secondId = trip2.getIdOfTrip();
            int departureF=trip1.getDepartureTime();
            int departureS=trip2.getDepartureTime();
            int conTime=departureS-(departureF+trip1.getDuration());
            if (firstId == i && secondId == j && conTime <= instance.getShortConnectionTimeForDriver()) {
                b_k_i_j = 1;
                break;
            }
        }
        return b_k_i_j;
    }

    //TRY TO SEE HERE for the values
    public int getCostC() {
        int c_k = (int) instance.getFixedCostForDriver();//fixed + idleTime cost+ changeover// change 2025.1.9
        int c_DI=instance.getIdleTimeCostForDriverPerUnit();
        for (int l = 0; l <  this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; l++) {
            int firstId = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getIdOfTrip();
            int secondId =  this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1).getIdOfTrip();
            boolean whetherDrivingInFirstTrip =this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDrivingStatus();
            boolean whetherDrivingInSecondTrip =  this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1).getDrivingStatus();
            TripWithWorkingStatusAndDepartureTime tripWitStatusAndDepTime1= this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            TripWithWorkingStatusAndDepartureTime tripWithStatusAndDepTime2=this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l+1);

            if (instance.whetherHavePossibleArcAfterCleaning(firstId, secondId) ) {
                int conTime=tripWithStatusAndDepTime2.getDepartureTime()
                        -tripWitStatusAndDepTime1.getDepartureTime()-tripWitStatusAndDepTime1.getDuration();

                if(conTime>instance.getMaxWorkingTime()){
//                    System.out.println("Check the trip1:"+tripWitStatusAndDepTime1);
//                    System.out.println("Check the trip2: "+ tripWithStatusAndDepTime2);
                }
                c_k = (int) (c_k + c_DI*conTime);
            }
            if (whetherDrivingInFirstTrip != whetherDrivingInSecondTrip) {
                c_k = (int) (c_k + instance.getCostForChangeOver());
            }

        }
        if(c_k<0){
            System.out.println("Error neg cost in driver schedule "+c_k);
        }
        return c_k;
    }


    public boolean whetherIsShortConnectionInDriverSchedule(int idTrip1, int idTrip2){
        boolean whetherShortConnection=false;
        for(int l = 0; l<this.tripWithWorkingStatusAndDepartureTimeArrayList.size()-1; l++){
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime1= this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime2= this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l+1);
            int conTime=tripWithWorkingStatusAndDepartureTime2.getDepartureTime()
                    - tripWithWorkingStatusAndDepartureTime1.getDepartureTime()-tripWithWorkingStatusAndDepartureTime1.getDuration();
            if(tripWithWorkingStatusAndDepartureTime1.getIdOfTrip()==idTrip1&&
            tripWithWorkingStatusAndDepartureTime2.getIdOfTrip()==idTrip2
                    &&conTime<=instance.getShortConnectionTimeForDriver()
            ){
                whetherShortConnection=true;
            }

        }
        return  whetherShortConnection;
    }

    public int getNbDriverSchedules() {
        return nbDriverSchedules;
    }

    public void addNbSchedule(int nbDriverScheduleToAdd) {
        this.nbDriverSchedules = this.nbDriverSchedules + nbDriverScheduleToAdd;
    }

    public boolean isTheSame(DriverSchedule driverSchedule) {
        if (driverSchedule == null) return false;
        if (this.tripWithWorkingStatusAndDepartureTimeArrayList.size() != driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size()) {
            return false;
        }
        for (int i = 0; i < driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); i++) {
            int idOfTripInThisSchedule = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(i).getIdOfTrip();
            boolean whetherDriving1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(i).getDrivingStatus();
            int idOfTripInParameterSchedule = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(i).getIdOfTrip();
            boolean whetherDriving2 = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(i).getDrivingStatus();
            if (idOfTripInThisSchedule != idOfTripInParameterSchedule || whetherDriving1 != whetherDriving2) {
                return false;
            }
        }
        return true;
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

    public double getReducedCost(double[][] DualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect, double[] dualForConstraintOneDriving, double[][] dualForLinkShortConnection,
                                 double dualForAvailableNbDriver) {
        double reducedCost = this.getCostC() - dualForAvailableNbDriver;

        for(int f = 0; f<this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); f++){
            TripWithWorkingStatusAndDepartureTime tripWithWorkingAndDepTime=this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f);
            int idTrip=tripWithWorkingAndDepTime.getIdOfTrip();
            reducedCost=reducedCost-this.getCoefficientG(idTrip)*dualForConstraintOneDriving[idTrip];

        }


        for (int f = 0; f < this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; f++) {
            int idFirst = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f).getIdOfTrip();
            int idSecond = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f + 1).getIdOfTrip();
            TripWithWorkingStatusAndDepartureTime tripWithWorkingAndDepTime1=this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f);
            TripWithWorkingStatusAndDepartureTime tripWithWorkingAndDepTime2=this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f+1);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (idFirst == i && idSecond == j) {
                        if (whetherArcExist(i, j)) {
                            if (tripWithWorkingAndDepTime2.getDepartureTime()
                                    -(tripWithWorkingAndDepTime1.getDepartureTime() +tripWithWorkingAndDepTime1.getDuration())
                                    <= instance.getShortConnectionTimeForDriver()) {
                                reducedCost = reducedCost - this.getCoefficientForShortConnectionB(i,j)*dualForLinkShortConnection[i][j];
                            }
                        }
                    }
                }
            }
        }
        return reducedCost;
    }

    public void setNbDriverSchedules(int nbDriverSchedules) {
        this.nbDriverSchedules = nbDriverSchedules;
    }

    @Override
    public String toString() {
        String s = idOfDepot + " " + indexDepotAsStartingPoint + " ";
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            s += this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getIdOfTrip() + " "
                    + this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDrivingStatus() + " "
            +this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDepartureTime()+" ";
        }
        s += indexDepotAsEndingPoint + "\n";
        return s;
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity05_Size180_Day1_nbTrips020_combPer0.1.txt");//test vehicle example
        //inst_nbCity20_Size300_Day3_nbTrips300_combPer0.1
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus0 = new TripWithWorkingStatusAndDepartureTime( instance.getTrip(0), true,1);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus8 = new TripWithWorkingStatusAndDepartureTime( instance.getTrip(8), false,20);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus9 = new TripWithWorkingStatusAndDepartureTime( instance.getTrip(9), true,15);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus5 = new TripWithWorkingStatusAndDepartureTime( instance.getTrip(5), true,19);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus14 = new TripWithWorkingStatusAndDepartureTime( instance.getTrip(14), true,20);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus15= new TripWithWorkingStatusAndDepartureTime( instance.getTrip(15), true,45);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus18 = new TripWithWorkingStatusAndDepartureTime( instance.getTrip(18), true,55);
        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus19= new TripWithWorkingStatusAndDepartureTime( instance.getTrip(19), true,68);

        int idOfDepot=1;
        int indexOfStartingDepot=21;
        int indexOfEndingDepot=23;
        int nbDriverSchedules=1;


        DriverSchedule driverSchedule =new DriverSchedule(instance);
        driverSchedule.setIdOfDepot(idOfDepot);
        driverSchedule.setIndexDepotAsStartingPoint(indexOfStartingDepot);
        driverSchedule.setIndexDepotAsEndingPoint(indexOfEndingDepot);
        driverSchedule.setNbDriverSchedules(nbDriverSchedules);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus0);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus8);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus9);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus5);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus14);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus15);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus18);
        driverSchedule.addTripWithWorkingStatusInSchedule(tripWithWorkingStatus19);

       // System.out.println("driver tripWithWorkingStatusAndDepartureTimeArrayList check:"+driverSchedule.getCostC());

    }
}
