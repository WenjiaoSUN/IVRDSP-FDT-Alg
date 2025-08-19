package Instance;

//import Solution.TripWithDriveInfos;

public class Instance {
    private String nameOfInstance;
    private int nbCities;
    private int nbDepot;
    private int nbVehicleAvailable;
    private int nbDriverAvailable;
    private int nbTrips;
    private int  minPlanTurnTime;
    private int shortConnectionTimeForDriver;
    private int shortConnectionTimeForVehicle;
    private int maxDrivingTime;
    private int maxWorkingTime;
    private int fixedCostForDriver;
    private int fixedCostForVehicle;
    private double maxPercentageOfCombinedTrip;

    private int maxFolderOfTimeSlotAsTimeWindow;
    private  int maxPlanTime;
    private Vehicle[] vehicles;
    private Driver[] drivers;
    private Trip[] trips;
//    private TripWithDriveInfos[] tripWithDriveInfos;
    private City[] cities;
    private Depot[] depots;
    private int[][] idleTimeCostForVehicle;
    private int[][] idleTimeCostForDriver;
    private int[][] connectionTime;
    private int changeOverCostForDriverPerChange;

    // these to deal with the time window
    private int idleTimeCostForDriverPerUnit;
    private int idleTimeCostForVehiclePerUnit;
    private int timeSlotUnit;
    private double scale_DisAndDur;
    private int startingPlanningHorizon;
    private int endingPlaningHorizon;


    public Instance(String nameOfInstance, int nbCities, int nbDepot, int nbVehicleAvailable, int nbDriverAvailable, int nbTrips,
                    int minPlanTurnTime, int shortConnectionTimeForDriver, int shortConnectionTimeForVehicle, int maxDrivingTime,
                    int maxWorkingTime, int fixedCostForDriver, int fixedCostForVehicle, int idleTimeCostForDriverPerUnit,
                    int idleTimeCostForVehiclePerUnit,int changeOverCostForDriverPerChange,
                    double maxPercentageOfCombinedTrip,int maxFolderOfTimeSlotAsTimeWindow, int maxPlanTime,int timeSlotUnit,
                    double scale_DisAndDur,int startingPlanningHorizon,int endingPlaningHorizon) {
        this.nameOfInstance=nameOfInstance;
        this.nbCities=nbCities;
        this.nbDepot=nbDepot;
        this.nbVehicleAvailable = nbVehicleAvailable;
        this.nbDriverAvailable = nbDriverAvailable;
        this.nbTrips = nbTrips;
        this.minPlanTurnTime=minPlanTurnTime;
        this.shortConnectionTimeForDriver = shortConnectionTimeForDriver;
        this.shortConnectionTimeForVehicle=shortConnectionTimeForVehicle;
        this.maxDrivingTime=maxDrivingTime;
        this.maxWorkingTime=maxWorkingTime;
        this.fixedCostForDriver = fixedCostForDriver;
        this.fixedCostForVehicle = fixedCostForVehicle;
        this.idleTimeCostForDriverPerUnit=idleTimeCostForDriverPerUnit;
        this.idleTimeCostForVehiclePerUnit=idleTimeCostForVehiclePerUnit;
        this.vehicles = new Vehicle[nbVehicleAvailable];
        this.drivers = new Driver[nbDriverAvailable];
        this.trips = new Trip[nbTrips];
//        this.tripWithDriveInfos =new TripWithDriveInfos[nbTrips];
        this.depots=new Depot[nbDepot];
        this.cities= new City[nbCities];
        this.idleTimeCostForVehicle = new int[nbTrips][nbTrips];
        this.idleTimeCostForDriver = new int[nbTrips][nbTrips];
        this.connectionTime = new int[nbTrips][nbTrips];
        this.changeOverCostForDriverPerChange = changeOverCostForDriverPerChange;
        this.maxPercentageOfCombinedTrip=maxPercentageOfCombinedTrip;
        this.maxFolderOfTimeSlotAsTimeWindow=maxFolderOfTimeSlotAsTimeWindow;
        this.maxPlanTime = maxPlanTime;
        this.timeSlotUnit=timeSlotUnit;
        this.scale_DisAndDur=scale_DisAndDur;
        this.startingPlanningHorizon=startingPlanningHorizon;
        this.endingPlaningHorizon=endingPlaningHorizon;
        for (int i = 0; i < nbTrips; i++) {
            for (int j = 0; j < nbTrips; j++) {
                this.idleTimeCostForVehicle[i][j] = Integer.MAX_VALUE;
                this.idleTimeCostForDriver[i][j] = Integer.MAX_VALUE;
                this.connectionTime[i][j] = Integer.MAX_VALUE;
            }
        }
    }

    //the basic instance information

    public String  getNameOfInstance() {return nameOfInstance;}

    public void setNameOfInstance(String nameOfInstance) {
        this.nameOfInstance = nameOfInstance;
    }

    public int getNbCities() {
        return nbCities;
    }

    public int getMaxNbVehicleAvailable() {
        return this.nbVehicleAvailable;
    }

    public int getMaxNbDriverAvailable() {
        return this.nbDriverAvailable;
    }

    public int getNbTrips() {
        return this.nbTrips;
    }

    public int getNbDepots() { return this.nbDepot; }
    public int getNbNodes(){return 2+2*nbDepot+nbTrips;}
    public double getMaxPercentageOfCombinedTrip(){return this.maxPercentageOfCombinedTrip;}

    public Depot getDepot(int i){return this.depots[i];}
    public Depot[] getDepots(){return depots;}
    public City getCity(int i){return  this.cities[i];}
    public City[] getCities(){return cities;}

    public void setCity (int i, City city){this.cities[i]=city;}
    public void setDepot(int i, Depot depot){this.depots[i]= depot;}

    public void setVehicle(int i, Vehicle vehicle) {
        this.vehicles[i] = vehicle;
    }

    public Vehicle getVehicle(int i) {
        return this.vehicles[i];
    }

    public Vehicle[] getVehicles() {
        return vehicles;
    }

//    public int getIdleTimeCostForVehicle(int idOfFirstTrip, int idOfSecondTrip) {
//        return this.idleTimeCostForVehicle[idOfFirstTrip][idOfSecondTrip];
//    }
//
//    public void setIdleTimeCostForVehicle(int idOfFirstTrip, int idOfSecondTrip, int idleTimeCostForVehicle) {
//        this.idleTimeCostForVehicle[idOfFirstTrip][idOfSecondTrip] = idleTimeCostForVehicle;
//    }

    //some information about crews
    public void setDriver(int i, Driver driver) {
        this.drivers[i] = driver;
    }

    public Driver getDriver(int i) {
        return this.drivers[i];
    }

    public Driver[] getDrivers() {
        return drivers;
    }

    public int getMaxPlanTime() {
        return maxPlanTime;
    }

//    public void setIdleTimeCostForDriver(int idOfFirstTrip, int idOfSecondTrip, int idleTimeCostForDriver) {
//        this.idleTimeCostForDriver[idOfFirstTrip][idOfSecondTrip] = idleTimeCostForDriver;
//    }
//    public int getIdleTimeCostForDriver(int idOfFirstTrip, int idOfSecondTrip) {
//        return this.idleTimeCostForDriver[idOfFirstTrip][idOfSecondTrip];
//    }
    // some information about trips
    public void setTrip(int i, Trip trip) {
        this.trips[i] = trip;
    }

    public Trip getTrip(int i) {
        return this.trips[i];
    }

    public Trip[] getTrips() {
        return trips;
    }

//    public TripWithDriveInfos getExtendTrip (int i){return  this.tripWithDriveInfos[i];}
    public int getFixedCostForDriver() {
        return fixedCostForDriver;
    }
    public int getFixedCostForVehicle() {
        return fixedCostForVehicle;
    }
    public int getShortConnectionTimeForDriver() {
        return shortConnectionTimeForDriver;
    }

    public int getShortConnectionTimeForVehicle(){ return  shortConnectionTimeForVehicle;}


    //here we give some information for the ChangeOver
    //here is we want to get the cost of ChangeOver
    public int getCostForChangeOver(){
        return this.changeOverCostForDriverPerChange;
    }

    public int getMaxDrivingTime(){return maxDrivingTime;}

    public int getMaxWorkingTime(){return maxWorkingTime;}

    public boolean whetherVehicleCanStartWithTrip(int idOfVehicle, int idOfTrip) {
        // here we change make the vehicle can start from any depot, so as long as the trip start from a depot city
        // then the vehicle can start with that trip
        boolean whetherVehicleCanStartWithTrip = false;
        Trip trip = this.getTrip(idOfTrip);
        for(int k=0; k<nbDepot;k++){
            Depot depot = this.getDepot(k);
            if (trip.getIdOfStartCity()==depot.getIdOfCityAsDepot()) {
                whetherVehicleCanStartWithTrip = true;
            }

        }
        return whetherVehicleCanStartWithTrip;
    }

    public boolean whetherVehicleCanEndAtTrip(int idOfVehicle, int idOfTrip) {
        boolean whetherVehicleCanEndAtTrip = false;
        Vehicle vehicle = this.getVehicle(idOfVehicle);
        Trip trip = this.getTrip(idOfTrip);
        for (int k = 0; k < nbDepot; k++) {
            Depot depot = this.getDepot(k);
            if (trip.getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                whetherVehicleCanEndAtTrip = true;
            }
        }
        return whetherVehicleCanEndAtTrip;
    }

    public boolean whetherDriverCanStartWithTrip(int idOfTrip) {
        boolean whetherDriverCanStartWithTrip = false;
        for(int k =0; k<nbDepot; k++){
            Depot depot = this.getDepot(k);
            Trip trip = this.getTrip(idOfTrip);
            if (trip.getIdOfStartCity()==depot.getIdOfCityAsDepot()) {
                whetherDriverCanStartWithTrip = true;
            }

        }
        return whetherDriverCanStartWithTrip;
    }

    public boolean whetherDriverCanEndAtTrip( int idOfTrip) {
        boolean whetherDriverCanEndAtTrip = false;

        Trip trip = this.getTrip(idOfTrip);
        for(int k=0;k<nbDepot; k++){
            Depot depot = this.getDepot(k);
            if (trip.getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                whetherDriverCanEndAtTrip = true;
            }

        }
        return whetherDriverCanEndAtTrip;
    }

    public int getDistance(int idOfCity1, int idOfCity2) {
        //Here use distance between the cities stands for the duration of the trip
        int x1 = this.cities[idOfCity1].getCoordinate().getX();
        int x2 = this.cities[idOfCity2].getCoordinate().getX();
        int y1 = this.cities[idOfCity1].getCoordinate().getY();
        int y2 = this.cities[idOfCity2].getCoordinate().getY();
        int duration = (int)  (Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2)));
        return duration;
    }

    public double getScale_DisAndDur() {
        return scale_DisAndDur;
    }

    // 计算两城市间的持续时间，并且为 TimeSlot 的整数倍,这个跟当时 generate Instance一样；
    public int getDuration(int idOfCity1, int idOfCity2) {
        // Step 1: 获取两城市之间的距离
        int distance = getDistance(idOfCity1, idOfCity2);
        // Step 2: 使用比例因子将距离转换为时间
        double rawDuration = distance *this.getScale_DisAndDur() ;
        // Step 3: 四舍五入到最接近的 TimeSlot 倍数
        int duration = (int) (Math.round(rawDuration / this.getTimeSlotUnit()) *this.getTimeSlotUnit());
        return duration;
    }

//    public int getMaxWaitingTime(int idFirstTrip, int idSecondTrip){
//
//            if (!this.whetherHavePossibleArcAfterCleaning(idFirstTrip, idSecondTrip)) {
//                return 0;
//            }
//
//            int maxWaitingTime = this.getTrip(idSecondTrip).getLatestDepartureTime()
//                    - this.getTrip(idFirstTrip).getEarliestDepartureTime()
//                    - this.getTrip(idFirstTrip).getDuration();
//
//            return Math.max(0, maxWaitingTime);
//
//    }

    public int getMaxWaitingTime(int idFirstTrip, int idSecondTrip){
        int maxWaitingTime=0;
        if (this.whetherHavePossibleArcAfterCleaning(idFirstTrip, idSecondTrip)) {

            int maxConTime = this.getTrip(idSecondTrip).getLatestDepartureTime()
                    - this.getTrip(idFirstTrip).getEarliestDepartureTime()
                    - this.getTrip(idFirstTrip).getDuration();
            maxWaitingTime=maxConTime;
        }
        return maxWaitingTime;
    }

    public int getMinWaitingTime(int idFirstTrip, int idSecondTrip){
        int minWaitingTime=Integer.MIN_VALUE;
        if(whetherHavePossibleArcAfterCleaning(idFirstTrip,idSecondTrip)){
            int minConTime=this.getTrip(idSecondTrip).getEarliestDepartureTime()-this.getTrip(idFirstTrip).getLatestDepartureTime()- this.getTrip(idFirstTrip).getDuration();
            if (minConTime<minPlanTurnTime){
                minWaitingTime=minPlanTurnTime;
            }else {
                minWaitingTime=minConTime;
            }
        }
        return minWaitingTime;
    }



    // these are some things add for dealing with time window 2024.9.12
    public int getIdleTimeCostForDriverPerUnit(){return  this.idleTimeCostForDriverPerUnit;}
    public int getIdleTimeCostForVehiclePerUnit(){return this.idleTimeCostForVehiclePerUnit;}
    public int getTimeSlotUnit(){return  this.timeSlotUnit;}
    public int getStartingPlanningHorizon(){return this.startingPlanningHorizon;}
    public int getEndingPlaningHorizon(){return this.endingPlaningHorizon;}

    public boolean whetherHavePossibleArcAfterCleaning(int idOfFirstTrip, int idOfSecondTrip){
        // 初始化结果标志
        boolean whetherHavePossibleArc = false;
        // 提取关键参数
        int maxWorkingTime = this.maxWorkingTime;
        int minPlanTime = this.minPlanTurnTime;
        // 获取第一个行程的起点和终点城市编号
        int idFStaCity = this.trips[idOfFirstTrip].getIdOfStartCity();
        int idFEndCity = this.trips[idOfFirstTrip].getIdOfEndCity();
        // 获取第二个行程的起点和终点城市编号
        int idSStaCity = this.trips[idOfSecondTrip].getIdOfStartCity();
        int idSEndCity = this.trips[idOfSecondTrip].getIdOfEndCity();
        int conTime = this.trips[idOfSecondTrip].earliestDepartureTime - this.trips[idOfFirstTrip].getLatestDepartureTime() - this.trips[idOfFirstTrip].getDuration();

        // 检查两个行程是否直接相连（第一个行程的终点是第二个行程的起点）
        if (idFEndCity == idSStaCity) {
            // 计算两个行程之间的最大连接时间
            int largestConTime = this.trips[idOfSecondTrip].getLatestDepartureTime()
                    - this.trips[idOfFirstTrip].getEarliestDepartureTime()
                    - this.trips[idOfFirstTrip].getDuration();  // 应该使用第一个行程的持续时间
            int durFirstTrip = this.trips[idOfFirstTrip].getDuration();
            int durSecondTrip = this.trips[idOfSecondTrip].getDuration();
            // 如果满足最小调度间隔，则进行进一步检查
            if (largestConTime >= minPlanTime) {
                // 遍历所有仓库，判断是否存在满足条件的路径
//                for (int p = 0; p < nbDepot; p++) {
//                    Depot depot = this.getDepot(p);
//                    int idCityAsDepot = depot.getIdOfCityAsDepot();
//                    // 初始化总行程时间变量
//                    int totalDuration = 0;
//                    // 计算每种情况下的总行程时间
//                    if (idFStaCity == idCityAsDepot && idSEndCity == idCityAsDepot) {
//                        // Case 1: 起点和终点都在仓库城市
//                        if (conTime >= minPlanTime) {
//                            totalDuration = durFirstTrip + durSecondTrip + conTime;
////                            System.out.println("check" + totalDuration);
//                        } else {
//                            totalDuration = durFirstTrip + durSecondTrip + minPlanTime;
//                        }
//
//
//                    } else if (idFStaCity != idCityAsDepot && idSEndCity != idCityAsDepot) {
//                        // Case 2: 起点和终点都不在仓库城市
//                        if (conTime >= minPlanTime) {
//                            totalDuration = this.getDuration(idCityAsDepot, idFStaCity)
//                                    + minPlanTime + durFirstTrip
//                                    + conTime + durSecondTrip
//                                    + minPlanTime
//                                    + this.getDuration(idSEndCity, idCityAsDepot);
//
//                        } else {
//                            totalDuration = this.getDuration(idCityAsDepot, idFStaCity)
//                                    + minPlanTime + durFirstTrip
//                                    + minPlanTime + durSecondTrip
//                                    + minPlanTime
//                                    + this.getDuration(idSEndCity, idCityAsDepot);
//                        }
//
//                    } else if (idFStaCity != idCityAsDepot && idSEndCity == idCityAsDepot) {
//                        // Case 3: 起点不在仓库，终点在仓库城市
//                        if (conTime >= minPlanTime) {
//                            totalDuration = this.getDuration(idCityAsDepot, idFStaCity)
//                                    +conTime + durFirstTrip
//                                    + minPlanTime + durSecondTrip;
//                        }else {
//                            totalDuration = this.getDuration(idCityAsDepot, idFStaCity)
//                                    + minPlanTime + durFirstTrip
//                                    + minPlanTime + durSecondTrip;
//
//                        }
//
//                    } else if (idFStaCity == idCityAsDepot && idSEndCity != idCityAsDepot) {
//                        // Case 4: 起点在仓库，终点不在仓库城市
//                        if (conTime >= minPlanTime) {
//                            totalDuration = durFirstTrip +conTime
//                                    + durSecondTrip + minPlanTime
//                                    + this.getDuration(idSEndCity, idCityAsDepot);
//                        }else {
//                            totalDuration = durFirstTrip + minPlanTime
//                                    + durSecondTrip + minPlanTime
//                                    + this.getDuration(idSEndCity, idCityAsDepot);
//                        }
//                    }
//                    // 检查总行程时间是否在最大工作时间限制内
//                    if (totalDuration <= maxWorkingTime) {
//                        whetherHavePossibleArc = true;
//                        break;  // 只要找到一个满足条件的路径，就提前退出
//                    }
//                }
                whetherHavePossibleArc=true;
            }
        }
        return whetherHavePossibleArc;
//        boolean whetherHavePossibleArc=false;
//        int largeCon=this.trips[idOfSecondTrip].getLatestDepartureTime()-this.trips[idOfFirstTrip].getEarliestDepartureTime()
//                -this.getDuration(this.trips[idOfFirstTrip].getIdOfStartCity(),this.trips[idOfFirstTrip].getIdOfEndCity());
//        if(this.trips[idOfFirstTrip].getIdOfEndCity()==this.trips[idOfSecondTrip].getIdOfStartCity()){
//            if(largeCon>=this.minPlanTurnTime&&largeCon<=this.maxWorkingTime){
//                whetherHavePossibleArc=true;
//            }
//        }
//        return whetherHavePossibleArc;
    }



    public int getMaxNbPossibleArc(){
        int maxNbArc=0;
        for(int i=0;i<nbTrips;i++){
            for(int j=0;j<nbTrips;j++){
                boolean whetherPossibleArc=this.whetherHavePossibleArcAfterCleaning(i,j);
                if(whetherPossibleArc){
                    maxNbArc++;

                }
            }
        }
        for(int p=0;p<nbDepot;p++){
            int idCityAsDepot=this.getDepot(p).getIdOfCityAsDepot();
            for(int i=0;i<nbTrips;i++){
                if(this.getTrip(i).getIdOfStartCity()==idCityAsDepot||this.getTrip(i).getIdOfEndCity()==idCityAsDepot){
                    maxNbArc++;
                }
            }
        }
        return maxNbArc;
    }

    public int getMinPlanTurnTime(){
        return this.minPlanTurnTime;
    }


    public int getMaxFolderOfTimeSlotAsTimeWindow() {
        return maxFolderOfTimeSlotAsTimeWindow;
    }

    public int getTimeWindowRange(){
        return  2*maxFolderOfTimeSlotAsTimeWindow*timeSlotUnit;
    }

    @Override
    public String toString() {
        String s = "Instance{" + "\n"+"\t"+ "instanceName: "+nameOfInstance+
                "\n"+"\t"+"nbCity:"+nbCities +"\t"+"nbDepot:" + nbDepot + "\t" + "maxNbVehicle:" + nbVehicleAvailable + "\t" + "maxNbDriver:" + nbDriverAvailable + "\t" + "nbTrip:" + nbTrips
                + "\n \t" + "minPlanTurnTime:" + minPlanTurnTime + "\t" + "shortConnectionTimeForDriver:" + shortConnectionTimeForDriver + "\t" + "shortConnectionTimeForVehicle:" + shortConnectionTimeForVehicle
                + "\n \t" +"maxDrivingTime:" + maxDrivingTime + "\t" +"maxWorkingTime:" + maxWorkingTime +  "\t" +"maxPercentageOfCombinedTrip:" + maxPercentageOfCombinedTrip+ "\t" +"maxWaitingTime:" + maxPlanTime
                +"\n\t"+"timeSlotUnit"+ timeSlotUnit;
//                + "\t" + "minWorkingTime: " + minWorkingTime + "\t"+ "maxWorkingTime: " + maxWorkingTime;



        return s;

        /**
         * Attention: Here we use Array (not List), for the reader when we want print out something, it will fist get the meme adrress
         * so we need to construct a for loop to modify the toString
         * But List has its own toString, and it will show us things in the same line
         * for the list type, in order easy to read it is also need to use for loop to modify the output
         */
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);
   }


}
