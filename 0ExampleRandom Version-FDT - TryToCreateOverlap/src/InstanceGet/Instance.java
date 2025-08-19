package InstanceGet;

import InstanceInput.Input;
import InstanceInput.InputReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public class Instance {
    private Input input;

    private int nbDrivers;
    private int nbVehicles;
    private LinkedList<City> cities;
    private LinkedList<Depot> depots;
    private LinkedList<Trip> trips;

    private LinkedList<Vehicle> vehicles;

    private LinkedList<Driver> drivers;

    private LinkedList<ArcForTwoFeasibleSuccessiveTrip> arcForTwoFeasibleSuccessiveTrips;

    private LinkedList<ArcForTwoConflictTripsBySameDriver> arcForTwoConflictTripsBySameDrivers;

    private LinkedList<VehicleStartTrips> vehicleStartTrips;

    private LinkedList<VehicleEndTrips> vehicleEndTrips;

    private LinkedList<DriverStartTrips> driverStartTrips;

    private LinkedList<DriverEndTrips> driverEndTrips;

    public Instance(Input input) {
        this.input = input;
        //this.nbVehicles = input.getNbTrips() * input.getNbDepots();//this maxime nbVehicle we modified to the nb shift when we create the trips
        //this.nbDrivers = input.getNbTrips() * input.getNbDepots();//the maxime is nbVehicles = nbDrivers
        this.cities = new LinkedList<>();
        this.depots = new LinkedList<>();
        this.trips = new LinkedList<>();
        this.vehicles = new LinkedList<>();
        this.drivers = new LinkedList<>();
        this.arcForTwoFeasibleSuccessiveTrips = new LinkedList<>();
        this.vehicleStartTrips = new LinkedList<>();
        this.vehicleEndTrips = new LinkedList<>();
        this.driverStartTrips = new LinkedList<>();
        this.driverEndTrips = new LinkedList<>();
        this.arcForTwoConflictTripsBySameDrivers= new LinkedList<>();
    }

//    public int getNbVehicles() {
//        return nbVehicles;
//    }

//    public int getNbDrivers() {
//        return nbDrivers;
//    }


    public void addCity(City city) {
        this.cities.add(city);
    }

    public void addDepot(Depot depot) {
        this.depots.add(depot);
    }

    public void addTrip(Trip trip) {
        this.trips.add(trip);
    }

    public void addVehicle(Vehicle vehicle) {
        this.vehicles.add(vehicle);
    }

    public void addDriver(Driver driver) {
        this.drivers.add(driver);
    }

    public LinkedList<Driver> getDrivers() {
        return drivers;
    }

    public void addArcForTripsFeasible(ArcForTwoFeasibleSuccessiveTrip arcForTwoFeasibleSuccessiveTrip) {
        this.arcForTwoFeasibleSuccessiveTrips.add(arcForTwoFeasibleSuccessiveTrip);
    }

    public void addArcForTripsConflict(ArcForTwoConflictTripsBySameDriver arcForTwoConflictTripsBySameDriver){
        this.arcForTwoConflictTripsBySameDrivers.add(arcForTwoConflictTripsBySameDriver);
    }

    public void addVehicleStartTrip(VehicleStartTrips vehicleStartTrips) {
        this.vehicleStartTrips.add(vehicleStartTrips);
    }

    public void addVehicleEndTrip(VehicleEndTrips vehicleEndTrips) {
        this.vehicleEndTrips.add(vehicleEndTrips);
    }

    public void addDriverStartTrip(DriverStartTrips driverStartTrips) {
        this.driverStartTrips.add(driverStartTrips);
    }

    public void addDriverEndTrip(DriverEndTrips driverEndTrips) {
        this.driverEndTrips.add(driverEndTrips);
    }

    public Input getInput() {
        return input;
    }

    public LinkedList<Vehicle> getVehicles() {
        return vehicles;
    }

    public LinkedList<City> getCities() {
        return cities;
    }

    public LinkedList<Depot> getDepots() {
        return depots;
    }

    public LinkedList<Trip> getTrips() {
        return trips;
    }

    public void setNbDrivers(int nbDrivers) {
        this.nbDrivers = nbDrivers;
    }

    public void setNbVehicles(int nbVehicles) {
        this.nbVehicles = nbVehicles;
    }

    public int getDistance(City city1, City city2) {
        //Here use distance between the cities stands for the distance of the trip
        int x1 = city1.getCoordinate().getX();
        int x2 = city2.getCoordinate().getX();
        int y1 = city1.getCoordinate().getY();
        int y2 = city2.getCoordinate().getY();
        int distance = (int) (Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2)));
        return distance;
    }

    // 计算两城市间的持续时间，并且为 TimeSlot 的整数倍
    public int getDuration(City city1, City city2) {
        // Step 1: 获取两城市之间的距离
        int distance = getDistance(city1, city2);

        // Step 2: 使用比例因子将距离转换为时间
        double rawDuration = distance *input.getScale_DisAndDur() ;

        // Step 3: 四舍五入到最接近的 TimeSlot 倍数
        int duration = (int) (Math.round(rawDuration / input.getTimeSlotUnit()) *input.getTimeSlotUnit());

        return duration;
    }

    public int getMaxDuration(LinkedList<City> cities) {
        int maxDuration = 0;
        for (int i = 0; i < cities.size(); i++) {
            for (int j = 1; j < cities.size(); j++) {
                City city1 = cities.get(i);
                City city2 = cities.get(j);
                if (this.getDuration(city1, city2) > maxDuration) {
                    maxDuration = this.getDistance(city1, city2);
                }

            }
        }
        return maxDuration;
    }


    public LinkedList<ArcForTwoFeasibleSuccessiveTrip> getArcForTwoSuccessiveTrips() {
        return arcForTwoFeasibleSuccessiveTrips;
    }

    public LinkedList<ArcForTwoConflictTripsBySameDriver> getArcForConflictTrips() {
        return arcForTwoConflictTripsBySameDrivers;
    }

    public LinkedList<VehicleStartTrips> getVehicleStartTrips() {
        return vehicleStartTrips;
    }

    public LinkedList<VehicleEndTrips> getVehicleEndTrips() {
        return vehicleEndTrips;
    }

    public LinkedList<DriverStartTrips> getDriverStartTrips() {
        return driverStartTrips;
    }

    public LinkedList<DriverEndTrips> getDriverEndTrips() {
        return driverEndTrips;
    }


    public int getNbNormalTrip(){
        int nbNormalTrip=0;
        for(int t=0;t<this.getTrips().size();t++){
            Trip trip=this.trips.get(t);
            int nbVehicleNeed=trip.getNbCombinedVehicle();
            if(nbVehicleNeed==1){
                nbNormalTrip++;
            }
        }
        return nbNormalTrip;
    }


    public int getNbNormalTripStartFromDepot(){
        int nbNormalTrip_StartDepot=0;
        for(int t=0;t<this.getTrips().size();t++){
            Trip trip=this.trips.get(t);
            int nbVehicleNeed=trip.getNbCombinedVehicle();
            if(nbVehicleNeed>1){
                for(int d=0;d<this.getDepots().size();d++){
                    Depot depot=this.getDepots().get(d);
                    if(trip.getIdOfStartCity()==depot.getIdOfCityAsDepot()){
                        nbNormalTrip_StartDepot++;
                    }
                }
            }
        }
        return nbNormalTrip_StartDepot;
    }

    public int getNbNormalTripFromNonDepot(){
        int nbNormalTrip_StartNonDepot=0;
        for(int t=0;t<this.getTrips().size();t++){
            Trip trip=this.trips.get(t);
            int nbVehicleNeed=trip.getNbCombinedVehicle();
            if(nbVehicleNeed>1){
                for(int d=0;d<this.getDepots().size();d++){
                    Depot depot=this.getDepots().get(d);
                    if(trip.getIdOfStartCity()!=depot.getIdOfCityAsDepot()){
                        nbNormalTrip_StartNonDepot++;
                    }
                }
            }
        }
        return nbNormalTrip_StartNonDepot;
    }



    public int getNbCombineTrip(){
        int nbCombineTrip=0;
        for(int t=0;t<this.getTrips().size();t++){
            Trip trip=this.trips.get(t);
            int nbVehicleNeed=trip.getNbCombinedVehicle();
            if(nbVehicleNeed>1){
                nbCombineTrip++;
            }
        }
        return nbCombineTrip;
    }

    public int getNbCombineTripStartFromDepot(){
        int nbCombineTrip_StartDepot=0;
        for(int t=0;t<this.getTrips().size();t++){
            Trip trip=this.trips.get(t);
            int nbVehicleNeed=trip.getNbCombinedVehicle();
            if(nbVehicleNeed>1){
              for(int d=0;d<this.getDepots().size();d++){
                  Depot depot=this.getDepots().get(d);
                  if(trip.getIdOfStartCity()==depot.getIdOfCityAsDepot()){
                      nbCombineTrip_StartDepot++;
                  }
              }
            }
        }
        return nbCombineTrip_StartDepot;
    }

    public int getNbCombineTripStartFromNonDepot(){
        int nbCombineTrip_StartNonDepot=0;
        for(int t=0;t<this.getTrips().size();t++){
            Trip trip=this.trips.get(t);
            int nbVehicleNeed=trip.getNbCombinedVehicle();
            if(nbVehicleNeed>1){
                for(int d=0;d<this.getDepots().size();d++){
                    Depot depot=this.getDepots().get(d);
                    if(trip.getIdOfStartCity()!=depot.getIdOfCityAsDepot()){
                        nbCombineTrip_StartNonDepot++;
                    }
                }
            }
        }
        return nbCombineTrip_StartNonDepot;

    }



    /**
     * Here should Is add some constraint to judge whether the instance is good? for example the depot or the trip?
     */
    //here I need to define  how to print the instance in a file, thus we can check the results in file
    public void printInfile(String fileName) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName);
            writer.println("// The number of cities is \n" + this.input.getNbCities() + "\n"
                    + "// The size of city is \n" +"("+ this.input.getDistanceX()+","+this.input.getDistanceY() +")"+ "\n"
                            + "// The number of trips is  \n" + this.input.getNbTrips() + "\n"
                            + "// The percentage of combined trips is  \n" + this.input.getMaxPercentageCombinedTrip() + "\n"
                            + "// The number of depots is \n" + this.input.getNbDepots() + "\n"
                            + "// The minPlanTime is \n" + this.input.getMinPlanTime() + "\n"
                            + "// The maxWaitingTime is \n" + this.input.getMaxWaitingTime() + "\n"
                            + "// The short Time for driver is \n" + this.input.getShortTimeForDriver() + "\n"
                            + "// The short Time for vehicle is \n" + this.input.getShortTimeForVehicle() + "\n"
                            + "// The maximum driving time is \n" + this.input.getMaxDrivingTime() + "\n"
                            + "// The maximum working time is \n" + this.input.getMaxWorkingTime() + "\n"
            );

            writer.println("//Here are the cities: idOfCity  \t nameOfCity \t coordinateX \t coordinateY ");
            for (City city : this.cities) {
                writer.println(city);
            }


            writer.println("//Depots:  \n// idOfDepot \t idCityAsDepot");
            for (Depot depot : this.depots) {
                writer.println(depot);
            }


            writer.println("//Trips: idOfTrip \t idOfShift \t idOfStartCity \t idEndCity"
                    + "\t departureTime \t arrivalTime \t nbCombinedVehicleInThisTrip");
            for (Trip trip : this.trips) {
                writer.println(trip);
            }


            writer.println("//Vehicles: idOfVehicle idOfStartingDepot nbTrips");
            for (Vehicle vehicle : this.vehicles) {
                writer.println(vehicle);
            }


            writer.println("//Drivers :idOfDriver idOfStartingDepot idOfEndingDepot nbTrips");
            for (Driver driver : this.drivers) {
                writer.println(driver);
            }


            writer.println("//Graph arcs for trips could be performed successively: \n// idStartTrip idEndTrip costForVehicle costForDriver costForChangOver");
            for (ArcForTwoFeasibleSuccessiveTrip arcForTwoFeasibleSuccessiveTrips : this.arcForTwoFeasibleSuccessiveTrips) {
                writer.println(arcForTwoFeasibleSuccessiveTrips);
            }

            writer.println("//Graph arcs for trips can not be performed by the same driver: \n// idFirstTrip idSecondTrip ");
            for (ArcForTwoConflictTripsBySameDriver arcForTwoConflictTripsBySameDriver : this.arcForTwoConflictTripsBySameDrivers) {
                writer.println(arcForTwoConflictTripsBySameDriver);
            }

            writer.println("//Graph for vehicle: \n//Vehicle start trips:  idVehicle  \t idTrip");
            for (VehicleStartTrips vehicleStartTrips1 : this.vehicleStartTrips) {
                writer.println(vehicleStartTrips1);
            }


            writer.println("//Vehicle end trips:  idVehicle  \t idTrip");
            for (VehicleEndTrips vehicleEndTrips1 : this.vehicleEndTrips) {
                writer.println(vehicleEndTrips1);
            }


            writer.println("//Graph for driver: \n//Driver start trips:  idDriver  \t idTrip ");
            for (DriverStartTrips driverStartTrips1 : this.driverStartTrips) {
                writer.println(driverStartTrips1);
            }

            writer.println("//Driver end trips: idDriver  \t idTrip ");
            for (DriverEndTrips driverEndTrips1 : this.driverEndTrips) {
                writer.println(driverEndTrips1);
            }

            writer.close();
        } catch (IOException exception) {
            System.out.println("Write file error happened");
            System.out.println(exception);
        }
    }


    @Override
    public String toString() {
        String s = "The instance is : ";
        s += "\n" + "Here are the cities: ";
        for (City city : this.cities) {
            s += "\n\t " + city;
        }
        s += "\n" + "Here are the depots: ";

        for (Depot depot : this.depots) {
            s += "\n\t " + depot;
        }

        s += "\n" + "Here are the trips:";
        for (Trip trip : this.trips) {

            s += "\n\t" + trip;
        }

        s += "\n" + "Here are the vehicle start trips:";
        for (VehicleStartTrips vehicleStartTrips1 : this.vehicleStartTrips) {

            s += "\n\t" + vehicleStartTrips1;
        }

        s += "\n" + "Here are the vehicle end trips:";
        for (VehicleEndTrips vehicleEndTrips1 : this.vehicleEndTrips) {

            s += "\n\t" + vehicleEndTrips1;
        }

        s += "\n" + "Here are the driver start trips:";
        for (DriverStartTrips driverStartTrips1 : this.driverStartTrips) {
            s += "\n\t" + driverStartTrips1;
        }

        s += "\n" + "Here are the driver end trips:";
        for (DriverEndTrips driverEndTrips1 : this.driverEndTrips) {
            s += "\n\t" + driverEndTrips1;
        }

        return s;
    }

    public static void main(String[] args) {
        InputReader reader = new InputReader("Small random example_data.txt");
        Input input = reader.readfile();
        Instance instance = new Instance(input);
        System.out.println(instance.getMaxDuration(instance.getCities()));

        Depot depot = new Depot(0, 10,12,0);
        Depot depot1 = new Depot(1, 11,13,1);
        instance.addDepot(depot);
        instance.addDepot(depot1);

        Trip trip = new Trip(0, 0, 1, 7, 8, 8,1,1);
        instance.addTrip(trip);

        System.out.println(instance);
        System.out.println(instance.getMaxDuration(instance.getCities()));
    }
}
