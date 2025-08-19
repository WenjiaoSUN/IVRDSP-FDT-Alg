package Solution;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.TripWithWorkingStatusAndDepartureTime;

import java.util.ArrayList;
import java.util.BitSet;

public class DriverSchedule {
    //    private int idOfSchedule;//k
    private int idOfDepot;
    private int indexDepotAsStartingPoint;
    private int indexDepotAsEndingPoint;
    private ArrayList<TripWithWorkingStatusAndDepartureTime> tripWithWorkingStatusAndDepartureTimeArrayList;
//    private int nbDriverSchedules;

    private Instance instance;
    private final double eps=1e-7;;

    public DriverSchedule(Instance instance) {
        this.idOfDepot = Integer.MAX_VALUE;
        this.indexDepotAsStartingPoint = Integer.MAX_VALUE;
        this.indexDepotAsEndingPoint = Integer.MAX_VALUE;
        this.tripWithWorkingStatusAndDepartureTimeArrayList = new ArrayList<>();
//        this.nbDriverSchedules = 1;
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


    public int getCoefficientF(int i, int t) {
        int f_k_i_t = 0;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip = tripWithWorkingStatusAndDepartureTime.getIdOfTrip();
            int departureTime = tripWithWorkingStatusAndDepartureTime.getDepartureTime();
            if (idOfTrip == i && departureTime == t) {
                f_k_i_t = 1;
                break;
            }
        }
        return f_k_i_t;

    }

    public int getNewCoefficientF(int i) {
        int f_k_i= 0;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip = tripWithWorkingStatusAndDepartureTime.getIdOfTrip();
            if (idOfTrip == i ) {
                f_k_i = 1;
                break;
            }
        }
        return f_k_i;

    }

    public int getCoefficientA(int i) {
        int a_k_i = 0;// whether trip i showed up in the selected schedules;
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip = tripWithWorkingStatus.getIdOfTrip();
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
            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatus = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
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
            TripWithWorkingStatusAndDepartureTime trip1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            TripWithWorkingStatusAndDepartureTime trip2 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1);
            int firstId = trip1.getIdOfTrip();
            int secondId = trip2.getIdOfTrip();
            int departureF = trip1.getDepartureTime();
            int departureS = trip2.getDepartureTime();
            int conTime = departureS - (departureF + trip1.getDuration());
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
        int c_DI = instance.getIdleTimeCostForDriverPerUnit();
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; l++) {
            int firstId = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getIdOfTrip();
            int secondId = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1).getIdOfTrip();
            boolean whetherDrivingInFirstTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDrivingStatus();
            boolean whetherDrivingInSecondTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1).getDrivingStatus();
            TripWithWorkingStatusAndDepartureTime tripWitStatusAndDepTime1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            TripWithWorkingStatusAndDepartureTime tripWithStatusAndDepTime2 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1);

            if (instance.whetherHavePossibleArcAfterCleaning(firstId, secondId)) {
                int conTime = tripWithStatusAndDepTime2.getDepartureTime()
                        - tripWitStatusAndDepTime1.getDepartureTime() - tripWitStatusAndDepTime1.getDuration();

                if (conTime > instance.getMaxWorkingTime()) {
//                    System.out.println("Check the trip1:"+tripWitStatusAndDepTime1);
//                    System.out.println("Check the trip2: "+ tripWithStatusAndDepTime2);
                }
                c_k = (int) (c_k + c_DI * conTime);
            }
            if (whetherDrivingInFirstTrip != whetherDrivingInSecondTrip) {
                c_k = (int) (c_k + instance.getCostForChangeOver());
            }

        }
        if (c_k < 0) {
            System.out.println("Error neg cost in driver schedule " + c_k);
        }
        return c_k;
    }

    public int getPartialCostC() {
        int c_k = (int) instance.getFixedCostForDriver();//fixed + changeover// change 2025.6.8
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; l++) {
            boolean whetherDrivingInFirstTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDrivingStatus();
            boolean whetherDrivingInSecondTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l + 1).getDrivingStatus();
            if (whetherDrivingInFirstTrip != whetherDrivingInSecondTrip) {
                c_k = (int) (c_k + instance.getCostForChangeOver());
            }
        }
        if (c_k < 0) {
            System.out.println("Error neg cost in driver schedule " + c_k);
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

//    public int getNbDriverSchedules() {
//        return nbDriverSchedules;
//    }
//
//    public void addNbSchedule(int nbDriverScheduleToAdd) {
//        this.nbDriverSchedules = this.nbDriverSchedules + nbDriverScheduleToAdd;
//    }

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

    public int getCoefficientHWhetherPerformArc(int i, int j) {
        int h_ij_k = 0; // 默认不执行这条弧

        // 检查列表是否为空
        if (this.tripWithWorkingStatusAndDepartureTimeArrayList == null ||
                this.tripWithWorkingStatusAndDepartureTimeArrayList.size() < 2) {
            return h_ij_k;
        }

        // 查找任务i和任务j在列表中的位置
        int positionI = -1, positionJ = -1;

        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            TripWithWorkingStatusAndDepartureTime trip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip = trip.getIdOfTrip();

            if (idOfTrip == i) {
                positionI = l;
            } else if (idOfTrip == j) {
                positionJ = l;
            }
        }

        // 检查是否找到了两个任务，并且任务j紧跟在任务i之后
        if (positionI != -1 && positionJ != -1 && positionJ == positionI + 1) {
            h_ij_k = 1; // 执行从任务i到任务j的弧
        }

        return h_ij_k;
    }

    public int getCoefficientYWhetherChangeStatusWhenPerformArc(int i, int j) {
        int y_ij = 0; // 默认不改变状态

        // 检查列表是否为空
        if (this.tripWithWorkingStatusAndDepartureTimeArrayList == null ||
                this.tripWithWorkingStatusAndDepartureTimeArrayList.isEmpty()) {
            return y_ij;
        }

        // 找到任务i和任务j的driving状态
        boolean foundI = false, foundJ = false;
        boolean statusI = false, statusJ = false;

        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            TripWithWorkingStatusAndDepartureTime trip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l);
            int idOfTrip = trip.getIdOfTrip();

            if (idOfTrip == i) {
                statusI = trip.getDrivingStatus();
                foundI = true;
            } else if (idOfTrip == j) {
                statusJ = trip.getDrivingStatus();
                foundJ = true;
            }

            // 如果两个任务都找到了，可以提前退出
            if (foundI && foundJ) {
                break;
            }
        }

        // 如果两个任务都找到了，比较它们的driving状态
        if (foundI && foundJ) {
            if (statusI != statusJ) {
                y_ij = 1; // 从任务i到任务j时状态发生了改变
            }
        }

        return y_ij;
    }

    public int getCoefficientEWhetherLastTask(int i) {
        int e_i = 0; // 默认不是最后一个任务

        // 检查列表是否为空
        if (this.tripWithWorkingStatusAndDepartureTimeArrayList == null ||
                this.tripWithWorkingStatusAndDepartureTimeArrayList.isEmpty()) {
            return e_i;
        }

        // 获取列表中最后一个任务
        int lastIndex = this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1;
        TripWithWorkingStatusAndDepartureTime lastTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(lastIndex);

        // 检查最后一个任务是否就是任务i
        if (lastTrip.getIdOfTrip() == i) {
            e_i = 1; // 任务i是schedule中最后一个被执行的任务
        }

        return e_i;
    }

    public int getCoefficientSWhetherFirstTask(int i) {
        int s_i = 0; // 默认不是第一个任务

        // 检查列表是否为空
        if (this.tripWithWorkingStatusAndDepartureTimeArrayList == null ||
                this.tripWithWorkingStatusAndDepartureTimeArrayList.isEmpty()) {
            return s_i;
        }

        // 检查第一个任务是否就是任务i
        TripWithWorkingStatusAndDepartureTime firstTrip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(0);
        if (firstTrip.getIdOfTrip() == i) {
            s_i = 1; // 任务i是schedule中第一个被执行的任务
        }

        return s_i;
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

    public double getReducedCost(double[][] dualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect, double[] dualForConstraintOneDriving, double[][] dualForLinkShortConnection,
                                 double dualForAvailableNbDriver) {
        double reducedCost = this.getCostC() - dualForAvailableNbDriver;

        for (int f = 0; f < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); f++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingAndDepTime = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f);
            int idTrip=tripWithWorkingAndDepTime.getIdOfTrip();
            int depTimeIdOfTrip = tripWithWorkingAndDepTime.getDepartureTime();
            reducedCost = reducedCost - this.getCoefficientF(idTrip,depTimeIdOfTrip) * dualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect[idTrip][depTimeIdOfTrip];

        }

        for (int f = 0; f < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); f++) {
            TripWithWorkingStatusAndDepartureTime tripWithWorkingAndDepTime = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f);
            int idTrip = tripWithWorkingAndDepTime.getIdOfTrip();
            reducedCost = reducedCost - this.getCoefficientG(idTrip) * dualForConstraintOneDriving[idTrip];

//            System.out.println("Trip " + idTrip +
//                    " status=" + (tripWithWorkingAndDepTime.getDrivingStatus() ? "driving" : "passenger") +
//                    ", coefficientG=" + this.getCoefficientG(idTrip) +
//                    ", beta=" + dualForConstraintOneDriving[idTrip]);


        }


        for (int f = 0; f < this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1; f++) {
            int idFirst = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f).getIdOfTrip();
            int idSecond = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f + 1).getIdOfTrip();
            TripWithWorkingStatusAndDepartureTime tripWithWorkingAndDepTime1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f);
            TripWithWorkingStatusAndDepartureTime tripWithWorkingAndDepTime2 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(f + 1);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (idFirst == i && idSecond == j) {
                        if (whetherArcExist(i, j)) {
                            if (tripWithWorkingAndDepTime2.getDepartureTime()
                                    - (tripWithWorkingAndDepTime1.getDepartureTime() + tripWithWorkingAndDepTime1.getDuration())
                                    < instance.getShortConnectionTimeForDriver()) {
                                reducedCost = reducedCost - this.getCoefficientForShortConnectionB(i, j) * dualForLinkShortConnection[i][j];
                            }
                        }
                    }
                }
            }
        }
        return reducedCost;
    }



//    public void setNbDriverSchedules(int nbDriverSchedules) {
//        this.nbDriverSchedules = nbDriverSchedules;
//    }


    public boolean whetherFeasible(boolean ver) {
        //1 check start end same depot
        //2 check whether connect well
        //3 check work limitation
        //4 check driving limitation
        boolean whetherFeasible = true;
        int nbTrips = this.tripWithWorkingStatusAndDepartureTimeArrayList.size();

        //1 check start end same depot
        boolean whetherStartEndSameDepot=true;
        int idCityStart = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(0).getIdOfStartCity();
        int idCityEnd = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(nbTrips - 1).getIdOfEndCity();
        if (idCityStart != idCityEnd) {
            whetherStartEndSameDepot = false;
            if (ver) {
                System.out.println("This schedule doesn't satisfy the same start and end city");
            }
        }

        //2 check whether connection well
        boolean whetherConnectionWell = true;
        for (int i = 0; i < nbTrips - 1; i++) {
            TripWithWorkingStatusAndDepartureTime trip1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(i);
            TripWithWorkingStatusAndDepartureTime trip2 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(i + 1);
            int connectionTime = trip2.getDepartureTime() - trip1.getDepartureTime() - trip1.getDuration();
            if (connectionTime < instance.getMinPlanTurnTime()) {
                whetherConnectionWell = false;
                if (ver) {
                    System.out.println("This schedule doesn't connection well when perform task_"+ trip1.getIdOfTrip()+" to trip_"+trip2.getIdOfTrip()+ " as the connection time is "+connectionTime);
                }
                break;
            }

        }

        //3 check work limitation
        boolean whetherWorkWithinLimitation = true;
        TripWithWorkingStatusAndDepartureTime trip1 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(0);
        TripWithWorkingStatusAndDepartureTime trip2 = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(nbTrips-1);
        int workTime = trip2.getDepartureTime() + trip2.getDuration() - trip1.getDepartureTime();
        if (workTime > instance.getMaxWorkingTime()) {
            whetherWorkWithinLimitation = false;
            if (ver) {
                System.out.println("This schedule doesn't work within limitation");
            }
        }


        //4 check driving limitation
        boolean whetherDriveWithinLimitation = true;
        int drivingTime = 0;
        for (int i = 0; i < nbTrips ; i++) {
            TripWithWorkingStatusAndDepartureTime trip = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(i);
            if (trip.getDrivingStatus()) {
               drivingTime=drivingTime+trip.getDuration();
            }
        }
        if(drivingTime>this.instance.getMaxDrivingTime()){
            whetherDriveWithinLimitation=false;
            if(ver){
                System.out.println("This schedule doesn't drive within limitation");
            }
        }
        if(whetherStartEndSameDepot==false||whetherConnectionWell==false||whetherWorkWithinLimitation==false||whetherDriveWithinLimitation==false){
            whetherFeasible=false;
        }

        return whetherFeasible;

    }




    public DriverSchedule deepCopy() {
        DriverSchedule copy = new DriverSchedule(this.instance); // 保留原实例
        // 拷贝 depot 起止信息和数量
        copy.setIdOfDepot(this.idOfDepot);
        copy.setIndexDepotAsStartingPoint(this.indexDepotAsStartingPoint);
        copy.setIndexDepotAsEndingPoint(this.indexDepotAsEndingPoint);
//        copy.setNbDriverSchedules(this.nbDriverSchedules);

        // 拷贝 trip 列表
        ArrayList<TripWithWorkingStatusAndDepartureTime> newTripList = new ArrayList<>();
        for (TripWithWorkingStatusAndDepartureTime trip : this.tripWithWorkingStatusAndDepartureTimeArrayList) {
            TripWithWorkingStatusAndDepartureTime copiedTrip = new TripWithWorkingStatusAndDepartureTime(
                    this.instance.getTrip(trip.getIdOfTrip()), // Trip 实例（推荐从 instance 获取）
                    trip.getDrivingStatus(),
                    trip.getDepartureTime()
            );
            newTripList.add(copiedTrip);
        }

        copy.setTripWithWorkingStatusAndDepartureTimeArrayList(newTripList);
        return copy;
    }

    public boolean isTheSame(DriverSchedule other) {
        if (other == null) {
            System.out.println("compared with null!");
            return false;
        }

        ArrayList<TripWithWorkingStatusAndDepartureTime> list1 = this.tripWithWorkingStatusAndDepartureTimeArrayList;
        ArrayList<TripWithWorkingStatusAndDepartureTime> list2 = other.getTripWithWorkingStatusAndDepartureTimeArrayList();

        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            TripWithWorkingStatusAndDepartureTime t1 = list1.get(i);
            TripWithWorkingStatusAndDepartureTime t2 = list2.get(i);

            if (t1.getIdOfTrip() != t2.getIdOfTrip() ||
                    t1.getDepartureTime() != t2.getDepartureTime() ||
                    t1.getDrivingStatus() != t2.getDrivingStatus()) {
                return false;
            }
        }
        return true;
    }

    public int getIdOfStartDepot() {
        int idStartCity = this.tripWithWorkingStatusAndDepartureTimeArrayList.get(0).getIdOfStartCity();

        for (int k = 0; k < instance.getNbDepots(); k++) {
            int idCityAsDepot = instance.getDepot(k).getIdOfCityAsDepot();
            if (idCityAsDepot == idStartCity) {
                return k; // 找到直接返回
            }
        }

        // 如果没有找到匹配的 depot
        throw new IllegalStateException("Start city of the trip does not match any depot's city.");
    }

    // 新构造函数
    public DriverSchedule(int indexDepotAsStartingPoint,
                          int indexDepotAsEndingPoint,
                          ArrayList<TripWithWorkingStatusAndDepartureTime> tripList,
                          Instance instance) {
        this.indexDepotAsStartingPoint = indexDepotAsStartingPoint;
        this.indexDepotAsEndingPoint = indexDepotAsEndingPoint;
        this.tripWithWorkingStatusAndDepartureTimeArrayList = new ArrayList<>(tripList);
//        this.nbDriverSchedules = 1;
        this.instance = instance;
        this.idOfDepot = this.getIdOfStartDepot(); // 自动设置 depot
    }


    public int getEndTime() {
        if (this.tripWithWorkingStatusAndDepartureTimeArrayList.isEmpty()) {
            throw new IllegalStateException("Cannot get end time: driver schedule is empty.");
        }

        TripWithWorkingStatusAndDepartureTime lastTrip =
                this.tripWithWorkingStatusAndDepartureTimeArrayList.get(
                        this.tripWithWorkingStatusAndDepartureTimeArrayList.size() - 1
                );

        return lastTrip.getDepartureTime() + lastTrip.getDuration();
    }

    public int getStartTime() {
        if (this.tripWithWorkingStatusAndDepartureTimeArrayList.isEmpty()) {
            throw new IllegalStateException("Cannot get start time: driver schedule is empty.");
        }

        return this.tripWithWorkingStatusAndDepartureTimeArrayList.get(0).getDepartureTime();
    }

    public BitSet getTripBitSet(int nbTrips) {
        BitSet tripSet = new BitSet(nbTrips);
        for (TripWithWorkingStatusAndDepartureTime trip : this.tripWithWorkingStatusAndDepartureTimeArrayList) {
            tripSet.set(trip.getIdOfTrip());
        }
        return tripSet;
    }

    // compare whether the same structure
    public boolean isSameStructureIgnoreDepartureTime(DriverSchedule other) {
        if (other == null) {
            return false;
        }

        ArrayList<TripWithWorkingStatusAndDepartureTime> list1 = this.tripWithWorkingStatusAndDepartureTimeArrayList;
        ArrayList<TripWithWorkingStatusAndDepartureTime> list2 = other.getTripWithWorkingStatusAndDepartureTimeArrayList();

        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            TripWithWorkingStatusAndDepartureTime t1 = list1.get(i);
            TripWithWorkingStatusAndDepartureTime t2 = list2.get(i);

            if (t1.getIdOfTrip() != t2.getIdOfTrip() ||
                    t1.getDrivingStatus() != t2.getDrivingStatus()) {
                return false;
            }
        }

        return true;
    }


    public boolean isExactlySameAs(DriverSchedule other) {
        if (other == null) {
            //System.out.println("[COMPARE FAIL] Other schedule is null.");
            return false;
        }

        ArrayList<TripWithWorkingStatusAndDepartureTime> list1 = this.tripWithWorkingStatusAndDepartureTimeArrayList;
        ArrayList<TripWithWorkingStatusAndDepartureTime> list2 = other.getTripWithWorkingStatusAndDepartureTimeArrayList();

        if (list1.size() != list2.size()) {
//            System.out.println("[COMPARE FAIL] Trip list size mismatch: " + list1.size() + " vs " + list2.size());
            return false;
        }

        final double EPS = 1e-4;

        for (int i = 0; i < list1.size(); i++) {
            TripWithWorkingStatusAndDepartureTime t1 = list1.get(i);
            TripWithWorkingStatusAndDepartureTime t2 = list2.get(i);

            if (t1.getIdOfTrip() != t2.getIdOfTrip()) {
//                System.out.println(" Trip ID mismatch at index " + i + ": " +
//                        t1.getIdOfTrip() + " vs " + t2.getIdOfTrip());
                return false;
            }

            if (t1.getDrivingStatus() != t2.getDrivingStatus()) {
//                System.out.println(" Driving status mismatch at index " + i + ": " +
//                        t1.getDrivingStatus() + " vs " + t2.getDrivingStatus());
                return false;
            }

            double dep1 = t1.getDepartureTime();
            double dep2 = t2.getDepartureTime();
            double diff = Math.abs(dep1 - dep2);
            if (diff > EPS) {
//                System.out.println(" Departure time mismatch at index " + i + ": " +
//                        dep1 + " vs " + dep2 + " (diff = " + diff + ")");
                return false;
            }
        }

        System.out.println(" Schedules are exactly the same.");
        return true;
    }













    @Override
    public String toString() {
        String s = idOfDepot + " " + indexDepotAsStartingPoint + " ";
        for (int l = 0; l < this.tripWithWorkingStatusAndDepartureTimeArrayList.size(); l++) {
            s += this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getIdOfTrip() + " "
                    + this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDrivingStatus() + " "
                    + this.tripWithWorkingStatusAndDepartureTimeArrayList.get(l).getDepartureTime() + " ";
        }
        s += indexDepotAsEndingPoint + "\n";
//        s+="Is feasible: " +this.whetherFeasible(true)+" ";
//        s += " cost: "+ this.getCostC();
        return s;
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


        DriverSchedule driverSchedule = new DriverSchedule(instance);
        driverSchedule.setIdOfDepot(idOfDepot);
        driverSchedule.setIndexDepotAsStartingPoint(indexOfStartingDepot);
        driverSchedule.setIndexDepotAsEndingPoint(indexOfEndingDepot);
//        driverSchedule.setNbDriverSchedules(nbDriverSchedules);
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
