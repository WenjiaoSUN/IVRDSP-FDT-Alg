package InstanceCreate;

import InstanceGet.*;
import InstanceInput.Input;
import InstanceInput.InputReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;

/**
 * This file is the final file, which will DO two things: firstly write  all the instance information in to a instanceGenerate.txt
 * secondly, write one feasible solution to the FeasibleSolution.txt
 * when create trips, we need to add trip in the instance, and add trip in the shift,
 * the different is that the shift is the list of trips, would cover the combined trip,
 * all the list of shift would be put in the feasible solutions
 * And the number of shift is equal to the maxNbVehicle and maxNbDriver
 */

public class RandomInstanceCreate {
    private Random rnd;
    private Input input;
    private Instance instance;// very import, here this to write create what?  is to create random city,depot, trip and others, sooooo it actually create the instance

    private FeasibleSolution feasibleSolution;
    private int sumCombinedTrip;


    public RandomInstanceCreate(Input input) {
        this.rnd = new Random(0);
        this.input = input;
        this.instance = new Instance(input);
        this.feasibleSolution = new FeasibleSolution(input);
        this.sumCombinedTrip = 0;
    }

    private void createCityAndDepotBothByHand() {
        int nbTrips = this.input.getNbTrips();
        int nbDepots = this.input.getNbDepots();
        int nbCities = this.input.getNbCities();


        if (nbCities == 3 && nbDepots == 2) {//size of 180
            int X0 = 80;//131-135
            int Y0 = 25;//25-30
            String name0 = Character.toString((char) 67) + 0;
            CoordinateOfCity coordinateOfCity0 = new CoordinateOfCity(X0, Y0);
            City city0 = new City(0, name0, coordinateOfCity0);
            this.instance.addCity(city0);

            int idOfCityAsDepot0 = nbCities - nbDepots;
            int DepotX1 = 78;//70
            int DepotY1 = 86;//85
            CoordinateOfCity coordinateOfCityAsDepot0 = new CoordinateOfCity(DepotX1, DepotY1);
            String name1 = Character.toString((char) 67) + idOfCityAsDepot0;
            City city1 = new City(idOfCityAsDepot0, name1, coordinateOfCityAsDepot0);
            this.instance.addCity(city1);
            Depot depot0 = new Depot(0, nbTrips + 0,
                    nbTrips + nbDepots + 0, idOfCityAsDepot0);
            this.instance.addDepot(depot0);

            int idOfCityAsDepot1 = input.getNbCities() - nbDepots + 1;
            int DepotX2 = 45;//120-125
            int DepotY2 = 57;//10-15
            CoordinateOfCity coordinateOfCityAsDepot1 = new CoordinateOfCity(DepotX2, DepotY2);
            String name2 = Character.toString((char) 67) + idOfCityAsDepot1;
            City city2 = new City(idOfCityAsDepot1, name2, coordinateOfCityAsDepot1);
            this.instance.addCity(city2);
            Depot depot1 = new Depot(1, nbTrips + 1,
                    nbTrips + nbDepots + 1, idOfCityAsDepot1);
            this.instance.addDepot(depot1);
        }


        if (nbCities == 5 && nbDepots == 2) {//size of 180
            int X0 = 132;//131-135
            int Y0 = 25;//25-30
            String name0 = Character.toString((char) 67) + 0;
            CoordinateOfCity coordinateOfCity0 = new CoordinateOfCity(X0, Y0);
            City city0 = new City(0, name0, coordinateOfCity0);
            this.instance.addCity(city0);

            int X1 = 88;//75-85
            int Y1 = 121;//105-119
            String name1 = Character.toString((char) 67) + 1;
            CoordinateOfCity coordinateOfCity1 = new CoordinateOfCity(X1, Y1);
            City city1 = new City(1, name1, coordinateOfCity1);
            this.instance.addCity(city1);

            int X2 = 95;
            int Y2 = 55;//21-30
            String name2 = Character.toString((char) 67) + 2;
            CoordinateOfCity coordinateOfCity2 = new CoordinateOfCity(X2, Y2);
            City city2 = new City(2, name2, coordinateOfCity2);
            this.instance.addCity(city2);


            int idOfCityAsDepot0 = nbCities - nbDepots;
            int DepotX3 = 78;//70
            int DepotY3 = 86;//85
            CoordinateOfCity coordinateOfCityAsDepot0 = new CoordinateOfCity(DepotX3, DepotY3);
            String name3 = Character.toString((char) 67) + idOfCityAsDepot0;
            City city3 = new City(idOfCityAsDepot0, name3, coordinateOfCityAsDepot0);
            this.instance.addCity(city3);
            Depot depot0 = new Depot(0, nbTrips + 0,
                    nbTrips + nbDepots + 0, idOfCityAsDepot0);
            this.instance.addDepot(depot0);

            int idOfCityAsDepot1 = input.getNbCities() - nbDepots + 1;
            int DepotX4 = 113;//120-125
            int DepotY4 = 57;//10-15
            CoordinateOfCity coordinateOfCityAsDepot1 = new CoordinateOfCity(DepotX4, DepotY4);
            String name4 = Character.toString((char) 67) + idOfCityAsDepot1;
            City city4 = new City(idOfCityAsDepot1, name4, coordinateOfCityAsDepot1);
            this.instance.addCity(city4);
            Depot depot1 = new Depot(1, nbTrips + 1,
                    nbTrips + nbDepots + 1, idOfCityAsDepot1);
            this.instance.addDepot(depot1);
        }

        if (nbCities == 10 && nbDepots == 3) {//size of 240
            int X0 = 121;//121-132 121-122
            int Y0 = 102;//65-79  65-71
            String name0 = Character.toString((char) 67) + 0;
            CoordinateOfCity coordinateOfCity0 = new CoordinateOfCity(X0, Y0);
            City city0 = new City(0, name0, coordinateOfCity0);
            this.instance.addCity(city0);

            int X1 = 69;
            int Y1 = 225;
            String name1 = Character.toString((char) 67) + 1;
            CoordinateOfCity coordinateOfCity1 = new CoordinateOfCity(X1, Y1);
            City city1 = new City(1, name1, coordinateOfCity1);
            this.instance.addCity(city1);

            int X2 = 145;//136-140  139-140
            int Y2 = 144;//104-124  124
            String name2 = Character.toString((char) 67) + 2;
            CoordinateOfCity coordinateOfCity2 = new CoordinateOfCity(X2, Y2);
            City city2 = new City(2, name2, coordinateOfCity2);
            this.instance.addCity(city2);

            int X3 = 95;
            int Y3 = 150;
            String name3 = Character.toString((char) 67) + 3;
            CoordinateOfCity coordinateOfCity3 = new CoordinateOfCity(X3, Y3);
            City city3 = new City(3, name3, coordinateOfCity3);
            this.instance.addCity(city3);

            int X4 = 60;
            int Y4 = 100;//100
            String name4 = Character.toString((char) 67) + 4;
            CoordinateOfCity coordinateOfCity4 = new CoordinateOfCity(X4, Y4);
            City city4 = new City(4, name4, coordinateOfCity4);
            this.instance.addCity(city4);

            int X5 = 185;
            int Y5 = 130;//123-134  130
            String name5 = Character.toString((char) 67) + 5;
            CoordinateOfCity coordinateOfCity5 = new CoordinateOfCity(X5, Y5);
            City city5 = new City(5, name5, coordinateOfCity5);
            this.instance.addCity(city5);

            int X6 = 165;//172-192
            int Y6 = 74;
            String name6 = Character.toString((char) 67) + 6;
            CoordinateOfCity coordinateOfCity6 = new CoordinateOfCity(X6, Y6);
            City city6 = new City(6, name6, coordinateOfCity6);
            this.instance.addCity(city6);


            int idOfCityAsDepot0 = nbCities - nbDepots;
            int DepotX7 = 11;//199-202  199
            int DepotY7 = 200;
            CoordinateOfCity coordinateOfCityAsDepot0 = new CoordinateOfCity(DepotX7, DepotY7);
            String name7 = Character.toString((char) 67) + idOfCityAsDepot0;
            City city7 = new City(idOfCityAsDepot0, name7, coordinateOfCityAsDepot0);
            this.instance.addCity(city7);
            Depot depot0 = new Depot(0, nbTrips + 0,
                    nbTrips + nbDepots + 0, idOfCityAsDepot0);
            this.instance.addDepot(depot0);

            int idOfCityAsDepot1 = input.getNbCities() - nbDepots + 1;
            int DepotX8 = 165;//105
            int DepotY8 = 125;//110
            CoordinateOfCity coordinateOfCityAsDepot1 = new CoordinateOfCity(DepotX8, DepotY8);
            String name8 = Character.toString((char) 67) + idOfCityAsDepot1;
            City city8 = new City(idOfCityAsDepot1, name8, coordinateOfCityAsDepot1);
            this.instance.addCity(city8);
            Depot depot1 = new Depot(1, nbTrips + 1,
                    nbTrips + nbDepots + 1, idOfCityAsDepot1);
            this.instance.addDepot(depot1);

            int idOfCityAsDepot2 = input.getNbCities() - nbDepots + 2;
            int DepotX9 = 60;//65
            int DepotY9 = 150;//152
            CoordinateOfCity coordinateOfCityAsDepot2 = new CoordinateOfCity(DepotX9, DepotY9);
            String name9 = Character.toString((char) 67) + idOfCityAsDepot2;
            City city9 = new City(idOfCityAsDepot2, name9, coordinateOfCityAsDepot2);
            this.instance.addCity(city9);
            Depot depot2 = new Depot(2,
                    nbTrips + 2, nbTrips + nbDepots + 2, idOfCityAsDepot2);
            this.instance.addDepot(depot2);
        }

        if (nbCities == 20 && nbDepots == 4) {//size of 300
            int X0 = 175;//
            int Y0 = 115;
            String name0 = Character.toString((char) 67) + 0;
            CoordinateOfCity coordinateOfCity0 = new CoordinateOfCity(X0, Y0);
            City city0 = new City(0, name0, coordinateOfCity0);
            this.instance.addCity(city0);

            int X1 = 125;
            int Y1 = 150;
            String name1 = Character.toString((char) 67) + 1;
            CoordinateOfCity coordinateOfCity1 = new CoordinateOfCity(X1, Y1);
            City city1 = new City(1, name1, coordinateOfCity1);
            this.instance.addCity(city1);

            int X2 = 115;
            int Y2 = 125;
            String name2 = Character.toString((char) 67) + 2;
            CoordinateOfCity coordinateOfCity2 = new CoordinateOfCity(X2, Y2);
            City city2 = new City(2, name2, coordinateOfCity2);
            this.instance.addCity(city2);

            int X3 = 85;
            int Y3 = 189;
            String name3 = Character.toString((char) 67) + 3;
            CoordinateOfCity coordinateOfCity3 = new CoordinateOfCity(X3, Y3);
            City city3 = new City(3, name3, coordinateOfCity3);
            this.instance.addCity(city3);

            int X4 = 200;
            int Y4 = 150;
            String name4 = Character.toString((char) 67) + 4;
            CoordinateOfCity coordinateOfCity4 = new CoordinateOfCity(X4, Y4);
            City city4 = new City(4, name4, coordinateOfCity4);
            this.instance.addCity(city4);

            int X5 = 176;
            int Y5 = 250;
            String name5 = Character.toString((char) 67) + 5;
            CoordinateOfCity coordinateOfCity5 = new CoordinateOfCity(X5, Y5);
            City city5 = new City(5, name5, coordinateOfCity5);
            this.instance.addCity(city5);

            int X6 = 150;
            int Y6 = 275;
            String name6 = Character.toString((char) 67) + 6;
            CoordinateOfCity coordinateOfCity6 = new CoordinateOfCity(X6, Y6);
            City city6 = new City(6, name6, coordinateOfCity6);
            this.instance.addCity(city6);

            int X7 = 124;
            int Y7 = 200;
            String name7 = Character.toString((char) 67) + 7;
            CoordinateOfCity coordinateOfCity7 = new CoordinateOfCity(X7, Y7);
            City city7 = new City(7, name7, coordinateOfCity7);
            this.instance.addCity(city7);

            int X8 = 150;
            int Y8 = 168;
            String name8 = Character.toString((char) 67) + 8;
            CoordinateOfCity coordinateOfCity8 = new CoordinateOfCity(X8, Y8);
            City city8 = new City(8, name8, coordinateOfCity8);
            this.instance.addCity(city8);

            int X9 = 200;
            int Y9 = 280;
            String name9 = Character.toString((char) 67) + 9;
            CoordinateOfCity coordinateOfCity9 = new CoordinateOfCity(X9, Y9);
            City city9 = new City(9, name9, coordinateOfCity9);
            this.instance.addCity(city9);

            int X10 = 100;//150;
            int Y10 = 220;//198;
            String name10 = Character.toString((char) 67) + 10;
            CoordinateOfCity coordinateOfCity10 = new CoordinateOfCity(X10, Y10);
            City city10 = new City(10, name10, coordinateOfCity10);
            this.instance.addCity(city10);

            int X11 = 75;
            int Y11 = 130;//100-160
            String name11 = Character.toString((char) 67) + 11;
            CoordinateOfCity coordinateOfCity11 = new CoordinateOfCity(X11, Y11);
            City city11 = new City(11, name11, coordinateOfCity11);
            this.instance.addCity(city11);

            int X12 = 218;
            int Y12 = 240;//270-290
            String name12 = Character.toString((char) 67) + 12;
            CoordinateOfCity coordinateOfCity12 = new CoordinateOfCity(X12, Y12);
            City city12 = new City(12, name12, coordinateOfCity12);
            this.instance.addCity(city12);

            int X13 = 235;
            int Y13 = 243;//243-255
            String name13 = Character.toString((char) 67) + 13;
            CoordinateOfCity coordinateOfCity13 = new CoordinateOfCity(X13, Y13);
            City city13 = new City(13, name13, coordinateOfCity13);
            this.instance.addCity(city13);

            int X14 = 218;
            int Y14 = 220;
            String name14 = Character.toString((char) 67) + 14;
            CoordinateOfCity coordinateOfCity14 = new CoordinateOfCity(X14, Y14);
            City city14 = new City(14, name14, coordinateOfCity14);
            this.instance.addCity(city14);

            int X15 = 175;
            int Y15 = 200;
            String name15 = Character.toString((char) 67) + 15;
            CoordinateOfCity coordinateOfCity15 = new CoordinateOfCity(X15, Y15);
            City city15 = new City(15, name15, coordinateOfCity15);
            this.instance.addCity(city15);


            int idOfCityAsDepot0 = input.getNbCities() - nbDepots + 0;
            int DepotX16 = 100;//
            int DepotY16 = 146;//116-146
            CoordinateOfCity coordinateOfCityAsDepot0 = new CoordinateOfCity(DepotX16, DepotY16);
            String name16 = Character.toString((char) 67) + idOfCityAsDepot0;
            City city16 = new City(idOfCityAsDepot0, name16, coordinateOfCityAsDepot0);
            this.instance.addCity(city16);
            Depot depot0 = new Depot(0, nbTrips + 0,
                    nbTrips + nbDepots + 0, idOfCityAsDepot0);
            this.instance.addDepot(depot0);

            int idOfCityAsDepot1 = input.getNbCities() - nbDepots + 1;
            int DepotX17 = 165;//135-195
            int DepotY17 = 115;//75-127
            CoordinateOfCity coordinateOfCityAsDepot1 = new CoordinateOfCity(DepotX17, DepotY17);
            String name17 = Character.toString((char) 67) + idOfCityAsDepot1;
            City city17 = new City(idOfCityAsDepot1, name17, coordinateOfCityAsDepot1);
            this.instance.addCity(city17);
            Depot depot1 = new Depot(1, nbTrips + 1,
                    nbTrips + nbDepots + 1, idOfCityAsDepot1);
            this.instance.addDepot(depot1);

            int idOfCityAsDepot2 = input.getNbCities() - nbDepots + 2;
            int DepotX18 = 150;
            int DepotY18 = 241;
            CoordinateOfCity coordinateOfCityAsDepot2 = new CoordinateOfCity(DepotX18, DepotY18);
            String name18 = Character.toString((char) 67) + idOfCityAsDepot2;
            City city18 = new City(idOfCityAsDepot2, name18, coordinateOfCityAsDepot2);
            this.instance.addCity(city18);
            Depot depot2 = new Depot(2,
                    nbTrips + 2, nbTrips + nbDepots + 2, idOfCityAsDepot2);
            this.instance.addDepot(depot2);


            int idOfCityAsDepot3 = input.getNbCities() - nbDepots + 3;
            int DepotX19 = 200;//250
            int DepotY19 = 220;//130-225
            CoordinateOfCity coordinateOfCityAsDepot3 = new CoordinateOfCity(DepotX19, DepotY19);
            String name19 = Character.toString((char) 67) + idOfCityAsDepot3;
            City city19 = new City(idOfCityAsDepot3, name19, coordinateOfCityAsDepot3);
            this.instance.addCity(city19);
            Depot depot3 = new Depot(3,
                    nbTrips + 3, nbTrips + nbDepots + 3, idOfCityAsDepot3);
            this.instance.addDepot(depot3);
        }

    }


    private void createRandomTrip() {

        //+ 2 * this.input.getTimeSlotUnit()
        int timeSlotUnit = this.input.getTimeSlotUnit();//2024.9.25
        int folderForTW = this.input.getFolderOfTimeSlotUnitAsTimeWindow();//2024.9.25
        int maxFolerForTW=6;//最大时间窗目前是加减半个小时 11.26

        int startHorizon = this.input.getStartHorizon();
        int adjustedStartHorizonForTW=startHorizon+maxFolerForTW*timeSlotUnit;//11.26保证开始时间左侧有重组的时间段可以用来构造时间窗

        //Following here is we add some controller for only the combine
        double folderOfMaximumWaitingTime = 2.5;//folderOfMaximumWaitingTime (n*maxWaitingTime)
        int maxFolderOfDiscountTime = 1;//not be included
        int m = 1 + this.rnd.nextInt(maxFolderOfDiscountTime);// control the shift period time (m*percentageOfDiscount )
        // above here is the new thing we add to control the shift period


        // define as attributes
        double percentageOfDiscount = this.input.getKeyPercentTimeBackToDepot();//24%
        // System.out.println(percentageOfDiscount);

        int requiredNbTrip = this.input.getNbTrips();
        int maxNbCombinedTrip = (int) (this.input.getNbTrips() * this.input.getMaxPercentageCombinedTrip());

        int nbDepots = this.input.getNbDepots();
        int nbCities = this.input.getNbCities();

        int minPlanTime = this.input.getMinPlanTime();//15 minutes
        int maxWaitingTime = this.input.getMaxWaitingTime();//60 minutes-change to 40 2024.10.23
        int maxWorkingTime = this.input.getMaxWorkingTime();//12 hours
        int discountWorkingTime = (int) (percentageOfDiscount * maxWorkingTime);
        int maxDrivingTime = this.input.getMaxDrivingTime();//9 hours
        int discountDrivingTime = (int) (percentageOfDiscount * maxDrivingTime);
        int t_eHorizon = input.getEndHorizon();
        int latestDepartureTime = t_eHorizon -adjustedStartHorizonForTW- maxWorkingTime;//11.26


        //*********modify 2024.9.30
        latestDepartureTime = (int) (Math.floor(latestDepartureTime / timeSlotUnit) * timeSlotUnit);

        int adjustLatestDepartureTimeForTW=latestDepartureTime-maxFolerForTW*timeSlotUnit;//11.26 保证最晚时间段的后面也有足够长的时间段去保证时间窗的调节
        //********modify 2024.9.30
        int shortConnectionForVehicle = input.getShortTimeForVehicle();
        int shortConnectionForDriver = input.getShortTimeForDriver();

        int maxShortConnection = shortConnectionForVehicle;

        if (shortConnectionForVehicle > shortConnectionForDriver) {
            maxShortConnection = shortConnectionForVehicle + 1;
        } else {
            maxShortConnection = shortConnectionForDriver + 1;
        }

        /**
         * // Since this line define the base time and unit In order to consider some overlap in the whole Instance
         * */
        int t_minPlanT = this.input.getMinPlanTime(); // Example value for t^{minPlanT}
        int u = this.input.getTimeSlotUnit(); // Example value for u
        // Create the connection time periods using your formula related to the overlap
//                                        int[] timePeriodSetRelatedToOverlapConnection = new int[]{t_minPlanT - 2*folderForTW * u, t_minPlanT -2*(folderForTW-1)*u - 1}; (folderForTw-1!=0)
//                                        System.out.println("Time Period related to the over lap connection time value is: ["+timePeriodSetRelatedToOverlapConnection[0] + ", " + timePeriodSetRelatedToOverlapConnection[1] + "]");
        // Create a list to hold each set of time periods
        ArrayList<int[]> timePeriodSets = new ArrayList<>();
        // Define time periods for 6 different sets using your formula
        timePeriodSets.add(new int[]{t_minPlanT - 2 * u, t_minPlanT - 1});
        timePeriodSets.add(new int[]{t_minPlanT - 4 * u, t_minPlanT - 2 * u - 1});
        timePeriodSets.add(new int[]{t_minPlanT - 6 * u, t_minPlanT - 4 * u - 1});
        timePeriodSets.add(new int[]{t_minPlanT - 8 * u, t_minPlanT - 6 * u - 1});
        timePeriodSets.add(new int[]{t_minPlanT - 10 * u, t_minPlanT - 8 * u - 1});
        timePeriodSets.add(new int[]{t_minPlanT - 12 * u, t_minPlanT - 10 * u - 1});

        // Print all sets for verification
        System.out.println("All time period sets:");
        for (int i = 0; i < timePeriodSets.size(); i++) {
            int[] period = timePeriodSets.get(i);
            System.out.println("S_" + (i + 1) + " = [" + period[0] + ", " + period[1] + "]");
        }
        /**
         * above this line is to define the base time and unit In order to consider some overlap in the whole Instance
         * */

        //System.out.println(maxShortConnection);
        //this is the latest departure time, which guarantee the driver can finish the shift
        int workingTime = 0;
        int drivingTime = 0;
        sumCombinedTrip = 0;
        int sumTrip = 0;
        int sumShift = 0;
        int idOfShift = sumShift;

        boolean whetherFirstTrip = false;

        Shift shift = new Shift(idOfShift);//create a new shift
        whetherFirstTrip = true;

        while (sumTrip < requiredNbTrip - 3) {
            //add an initial trip from a random depot in the empty shift
            /**
             *this is the preliminary part, check whether we need to create an initial trip
             * */
            int idOfTrip = sumTrip;
            int connectionTimeRandom = 0;

            //start from a random Depot and create an initial random trip
            int idOfRandomDepot = this.rnd.nextInt(nbDepots);
            int idOfRandomStartCity = this.instance.getDepots().get(idOfRandomDepot).getIdOfCityAsDepot();
            City startCity = this.instance.getCities().get(idOfRandomStartCity);

            int idOfRandomEndCity = this.rnd.nextInt(nbCities);
            while (idOfRandomEndCity == idOfRandomStartCity) {
                idOfRandomEndCity = this.rnd.nextInt(nbCities);
            }
            City endCity = this.instance.getCities().get(idOfRandomEndCity);


            //***************新添加下面的代码 2024.9.25 向下取整，将所有城市之间的duration 作为原理距离中最近timeSlotUnit的倍数的值； departureTime 成为 timeSlotUnit 的倍数
            int duration = this.instance.getDuration(startCity, endCity);
            int departureTime = (int) ( adjustedStartHorizonForTW + (adjustLatestDepartureTimeForTW - adjustedStartHorizonForTW)
                    * this.rnd.nextDouble()); //start from a random time from 6 am

            //为了保证TW=+-30时仍旧可以成立，修改startHorizon为更正后的11.26

            departureTime = (departureTime / timeSlotUnit) * timeSlotUnit;
            // 确保 departureTime 不超过 latestDepartureTime
            if (departureTime > latestDepartureTime) {
                System.out.println("Error line 496");
                departureTime = (latestDepartureTime / timeSlotUnit) * timeSlotUnit;
            }
            int arrivalTime = departureTime + duration;
            int nbRandomNbVehiclesInTrip = 1;// start with one vehicle  in the trip


            //********modify below 2024.10.08
            int eRandDeparture = departureTime - folderForTW * timeSlotUnit;
            int lRandDeparture = departureTime + folderForTW * timeSlotUnit;
            int TW = lRandDeparture - eRandDeparture;
            if(Math.abs((lRandDeparture-eRandDeparture)-2*timeSlotUnit*this.input.getFolderOfTimeSlotUnitAsTimeWindow())>0.1){
                System.out.println("now line 509 has error, current the time window is " + TW+" not as required "+2*timeSlotUnit*this.input.getFolderOfTimeSlotUnitAsTimeWindow());
            }

            //********modify above 2024.10.08
            Trip trip = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity, departureTime,
                    eRandDeparture, lRandDeparture, nbRandomNbVehiclesInTrip, duration);
            //***************新添加上面的代码，2024.9.25 向下取整，使 departureTime 成为 timeSlotUnit 的倍数

            this.instance.addTrip(trip);
            //System.out.println("first trip" + trip);
            shift.addTrip(trip);
            whetherFirstTrip = false;

            workingTime = duration;
            drivingTime = duration;

            sumTrip = sumTrip + 1;
            idOfTrip = sumTrip;

            /**
             *this is the first part, check whether we need to combine a former trip
             * */
            boolean addTrip = true;
            while (addTrip && sumTrip < requiredNbTrip - 3) {
                boolean addComb = false;
                LinkedList<Trip> combCandidate = new LinkedList<Trip>();
                if (sumCombinedTrip < maxNbCombinedTrip) {
                    if (sumShift > 0) {
                        Trip preTrip = instance.getTrips().get(idOfTrip - 1);
                        //choose all the trips in the former shift compared with this previous trip
                        int idOfCityAsDepot = instance.getDepots().get(idOfRandomDepot).getIdOfCityAsDepot();
                        City depot = instance.getCities().get(idOfCityAsDepot);
                        if (feasibleSolution.getShifts().size() > 0) {
                            for (int s = 0; s < feasibleSolution.getShifts().size(); s++) {
                                Shift formerShift = feasibleSolution.getShifts().get(s);
//                                //case1: former first trip
                                Trip firstFormerTrip = formerShift.getTrips().getFirst();
                                City endCityOfFirsFormerTrip = instance.getCities().get(firstFormerTrip.getIdOfEndCity());

                                int timeFromPreToFirstFormerTrip = firstFormerTrip.getDepartureTime() - preTrip.getArrivalTime();

                                timeFromPreToFirstFormerTrip = (int) (Math.floor(timeFromPreToFirstFormerTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26

                                Trip followerOfFirstFormerTrip = formerShift.getTrips().get(1);
                                int timeFromFirstFormerTripToFollower = followerOfFirstFormerTrip.getDepartureTime() - firstFormerTrip.getArrivalTime();

                                timeFromFirstFormerTripToFollower = (int) (Math.floor(timeFromFirstFormerTripToFollower / timeSlotUnit) * timeSlotUnit);//2024.9.26

                                if (firstFormerTrip.getIdOfStartCity() == preTrip.getIdOfEndCity() && firstFormerTrip.getNbCombinedVehicle() == 1
                                        && timeFromPreToFirstFormerTrip >= maxShortConnection
                                        && timeFromPreToFirstFormerTrip <= folderOfMaximumWaitingTime * maxWaitingTime
                                        && timeFromFirstFormerTripToFollower >= maxShortConnection
                                ) {

                                    int durationOfFirstFormerTripToDepot = instance.getDuration(endCityOfFirsFormerTrip, depot);//2024.9.25

                                    int arrTimeLefToDepotCombFormTrip = t_eHorizon - firstFormerTrip.getArrivalTime() - durationOfFirstFormerTripToDepot;
                                    arrTimeLefToDepotCombFormTrip = (int) (Math.floor(arrTimeLefToDepotCombFormTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26
                                    int worTimeLefToDepotCombFormTrip = discountWorkingTime - workingTime - (firstFormerTrip.getArrivalTime() - arrivalTime) - durationOfFirstFormerTripToDepot;
                                    worTimeLefToDepotCombFormTrip = (int) (Math.floor(worTimeLefToDepotCombFormTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26
                                    if (firstFormerTrip.getIdOfEndCity() != idOfCityAsDepot) {
                                        if (arrTimeLefToDepotCombFormTrip >= maxShortConnection && worTimeLefToDepotCombFormTrip >= maxShortConnection) {
                                            addComb = true;
                                            combCandidate.add(firstFormerTrip);
                                        }

                                    } else {
                                        if (arrTimeLefToDepotCombFormTrip >= maxShortConnection && worTimeLefToDepotCombFormTrip >= maxShortConnection) {
                                            addComb = true;
                                            combCandidate.add(firstFormerTrip);
                                        }

                                    }
                                }
////
//                                //case 2: former trip is the last trip in the shift
                                Trip lastFormerTrip = formerShift.getTrips().getLast();
                                City endCityOfLastFormerTrip = instance.getCities().get(lastFormerTrip.getIdOfEndCity());
                                int timeFromPreToLastFormerTrip = lastFormerTrip.getDepartureTime() - preTrip.getArrivalTime();
                                timeFromPreToLastFormerTrip = (int) (Math.floor(timeFromPreToLastFormerTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26
                                Trip leaderOfLastFormerTrip = formerShift.getTrips().get(formerShift.getTrips().size() - 2);//这个里面的值一定是>=0

                                int timeFromLeaderToLastFormerTrip = lastFormerTrip.getDepartureTime() - leaderOfLastFormerTrip.getArrivalTime();
                                timeFromLeaderToLastFormerTrip = (int) (Math.floor(timeFromLeaderToLastFormerTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26

                                if (lastFormerTrip.getIdOfStartCity() == preTrip.getIdOfEndCity() && lastFormerTrip.getNbCombinedVehicle() == 1
                                        && timeFromPreToLastFormerTrip >= maxShortConnection
                                        && timeFromPreToLastFormerTrip <= folderOfMaximumWaitingTime * maxWaitingTime
                                        && timeFromLeaderToLastFormerTrip >= maxShortConnection
                                ) {

                                    int durationOfLastFormerTripToDepot = instance.getDuration(endCityOfLastFormerTrip, depot);//2024.9.25
                                    int arrTimeLefToDepotCombFormTrip = t_eHorizon - lastFormerTrip.getArrivalTime() - durationOfLastFormerTripToDepot;
                                    arrTimeLefToDepotCombFormTrip = (int) (Math.floor(arrTimeLefToDepotCombFormTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26

                                    int worTimeLefToDepotCombFormTrip = discountWorkingTime - workingTime - (lastFormerTrip.getArrivalTime() - arrivalTime) - durationOfLastFormerTripToDepot;
                                    worTimeLefToDepotCombFormTrip = (int) (Math.floor(worTimeLefToDepotCombFormTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26
                                    if (lastFormerTrip.getIdOfEndCity() == idOfCityAsDepot) {
                                        if (arrTimeLefToDepotCombFormTrip >= maxShortConnection && worTimeLefToDepotCombFormTrip >= maxShortConnection) {
                                            addComb = true;
                                            combCandidate.add(lastFormerTrip);
                                        }

                                    } else {
                                        if (arrTimeLefToDepotCombFormTrip >= maxShortConnection && worTimeLefToDepotCombFormTrip >= maxShortConnection) {
                                            addComb = true;
                                            combCandidate.add(lastFormerTrip);
                                        }

                                    }
                                }

                                //case3: general case of the former trip
                                for (int t = 1; t < formerShift.getTrips().size() - 1; t++) {
                                    // here we check all the trips in the former shift
                                    Trip formerTrip = formerShift.getTrips().get(t);
                                    City forEnCity = instance.getCities().get(formerTrip.getIdOfEndCity());
                                    int timeOfForArrival = (int) formerTrip.getArrivalTime();
                                    int durBetweenForECityAndDepot = instance.getDuration(forEnCity, depot);//2024.9.25
                                    int timeBetweenPreTripArrivalAndFormerDeparture = formerTrip.getDepartureTime() - preTrip.getArrivalTime();
/**
 * this following two line is very important part for the combined, because we also need consider after combining a former trip, they need to have enough time to separate
 *
 * */
                                    Trip formerLeader = formerShift.getTrips().get(t - 1);
                                    int timeBetweenLeaderFormer = formerTrip.getDepartureTime() - formerLeader.getArrivalTime();// this also should be guartte the time greater than the maxShort connction
                                    Trip formerFollower = formerShift.getTrips().get(t + 1);
                                    int timeBetweenForFollowDepAndForArrival = formerFollower.getDepartureTime() - formerTrip.getArrivalTime();//this is to guarantee after combine, it has enough time to separate
/**
 * this upper two line is very important part for the combined, because we also need consider after combining , they need to have enough time to separate
 * */
                                    if (formerTrip.getIdOfStartCity() == preTrip.getIdOfEndCity() && formerTrip.getNbCombinedVehicle() == 1
                                            && timeBetweenPreTripArrivalAndFormerDeparture >= maxShortConnection
                                            && timeBetweenPreTripArrivalAndFormerDeparture <= folderOfMaximumWaitingTime * maxWaitingTime
                                            && timeBetweenForFollowDepAndForArrival >= maxShortConnection
                                            && timeBetweenLeaderFormer >= maxShortConnection
                                    ) {
                                        //System.out.println("some possible formerTrip id_"+formerTrip.getIdOfTrip()+" and  corresponding  preTrip id_"+preTrip.getIdOfTrip());
                                        int arrTimeLefToDepotCombFormTrip = Integer.MAX_VALUE;
                                        int worTimeLefToDepotCombFormTrip = Integer.MAX_VALUE;


                                        if (preTrip.getIdOfEndCity() != idOfCityAsDepot) {
                                            //pay attention of idOfDepot is not the idOfCityAsDepot
                                            // such that the two vehicle can be used continue we consider he always neefd to sperate each other s
                                            arrTimeLefToDepotCombFormTrip = t_eHorizon - timeOfForArrival - durBetweenForECityAndDepot;
                                            arrTimeLefToDepotCombFormTrip = (int) (Math.floor(arrTimeLefToDepotCombFormTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26

                                            worTimeLefToDepotCombFormTrip = discountWorkingTime - workingTime - (timeOfForArrival - arrivalTime) - durBetweenForECityAndDepot;
                                            worTimeLefToDepotCombFormTrip = (int) (Math.floor(worTimeLefToDepotCombFormTrip / timeSlotUnit) * timeSlotUnit);//2024.9.26

                                            if (formerTrip.getIdOfEndCity() != idOfCityAsDepot) {
                                                // System.out.println("some possible formerTrip id_"+formerTrip.getIdOfTrip()+" and  corresponding  preTrip id_"+preTrip.getIdOfTrip());
                                                //pay attention of idOfDepot is not the idOfCityAs depot
                                                if (arrTimeLefToDepotCombFormTrip >= maxShortConnection && worTimeLefToDepotCombFormTrip >= maxShortConnection) {
                                                    addComb = true;
                                                    combCandidate.add(formerTrip);
                                                }
                                            } else//formerTrip.getIdOfEndCity()==idOfRandomDepot
                                            {
                                                if (arrTimeLefToDepotCombFormTrip >= maxShortConnection && worTimeLefToDepotCombFormTrip >= maxShortConnection) {
                                                    //this when combined trip and end at depot we should guaruantee not exceed maxWorking Time
                                                    addComb = true;
                                                    combCandidate.add(formerTrip);
                                                }
                                            }
                                        } else//preTrip.getIdOfEndCity == idOfRandomDepot, then for sure the fomer endCity is not the depot
                                        {
                                            arrTimeLefToDepotCombFormTrip = t_eHorizon - timeOfForArrival - durBetweenForECityAndDepot;
                                            worTimeLefToDepotCombFormTrip = discountWorkingTime - workingTime - (timeOfForArrival - arrivalTime)
                                                    - durBetweenForECityAndDepot;
                                            if (arrTimeLefToDepotCombFormTrip >= maxShortConnection && worTimeLefToDepotCombFormTrip >= maxShortConnection) {
                                                addComb = true;
                                                combCandidate.add(formerTrip);
                                            }
                                        }
                                    }

                                }
                            }

                        }
                        if (combCandidate.size() > 0) {
                            int indexOfCombTrip = this.rnd.nextInt(combCandidate.size());
                            int idOfCombTrip = combCandidate.get(indexOfCombTrip).getIdOfTrip();
                            Trip combTrip = instance.getTrips().get(idOfCombTrip);
                            combTrip.setNbCombinedVehicle(2);
                            //System.out.println("combine Trip" + combTrip);
                            shift.addTrip(combTrip);
                            addComb = false;
                            whetherFirstTrip = false;
                            //System.out.println("the id of shift with combine=" + idOfShift);
                            //System.out.println("formerTrip id_" + combTrip.getIdOfTrip() + " and  corresponding  preTrip id_" + preTrip.getIdOfTrip() + "\n");
                            sumCombinedTrip = sumCombinedTrip + 1;
                            //this following part just update the local variable, but didn't change anything to the previous trip
                            idOfRandomEndCity = combTrip.getIdOfEndCity();
                            endCity = instance.getCities().get(idOfRandomEndCity);

                            workingTime = workingTime + combTrip.getArrivalTime() - arrivalTime;
                            arrivalTime = combTrip.getArrivalTime();

                        }
                    }
                }
                /**
                 * this is the second step to create the new trip after combining the former trip
                 * create a new trip when preEnCity is not good depot ------------------------P3
                 * */
                if (addComb == false) {
                    int idOfCityAsDepot = instance.getDepots().get(idOfRandomDepot).getIdOfCityAsDepot();
                    City depot = instance.getCities().get(idOfCityAsDepot);
                    LinkedList<City> candidateTemEnCity = new LinkedList<>();
                    //the downside is prepared for the trip create when after a combined trip
                    int idOfCurrentTripInShift = shift.getTrips().size() - 1;
                    Trip currentTripInShift = shift.getTrips().get(idOfCurrentTripInShift);
                    // the upside is prepared for the trip create when after a combined trip
                    int nbCandidates = 0;
                    if (idOfRandomEndCity != depot.getIdOfCity()) {
                        for (int i = 0; i < nbCities; i++) {
                            City temEnCity = instance.getCities().get(i);
                            int durBetweenPreECityAndTemEnCity = instance.getDuration(temEnCity, endCity);//2024.9.25
                            int durBetweenTemEnCityAndDepot = instance.getDuration(temEnCity, depot);//2024.9.25
                            int idOfTemEnCity = temEnCity.getIdOfCity();
                            if (idOfTemEnCity != idOfRandomEndCity) {
                                int arrTimeLefToDepot = Integer.MAX_VALUE;
                                int worTimeLefToDepot = Integer.MAX_VALUE;
                                int drTimeLefToDepot = Integer.MAX_VALUE;
                                if (idOfTemEnCity != idOfCityAsDepot) {
//                                    System.out.println("the tem city is not the good depot");
                                    // calculate the connection time left for the preEndCity AND temEndCity
                                    // based on assuming by minPlanTime arrive depot
                                    arrTimeLefToDepot = t_eHorizon - durBetweenPreECityAndTemEnCity - arrivalTime - durBetweenTemEnCityAndDepot - minPlanTime;
                                    worTimeLefToDepot = discountWorkingTime - durBetweenPreECityAndTemEnCity - workingTime
                                            - durBetweenTemEnCityAndDepot - minPlanTime;
                                    drTimeLefToDepot = discountDrivingTime - durBetweenPreECityAndTemEnCity - drivingTime - durBetweenTemEnCityAndDepot;
//                                    System.out.println("arrTimeLeft" + arrTimeLefToDepot);
//                                    System.out.println("worTimeLeft" + worTimeLefToDepot);
//                                    System.out.println("driTimeLeft" + drTimeLefToDepot);
                                } else {
                                    arrTimeLefToDepot = t_eHorizon - durBetweenPreECityAndTemEnCity - arrivalTime;
                                    //System.out.println("arTimeLeft"+arrTimeLefToDepot);
                                    worTimeLefToDepot = discountWorkingTime - durBetweenPreECityAndTemEnCity - workingTime;
                                    //System.out.println("woTimeLeft"+worTimeLefToDepot);
                                    drTimeLefToDepot = discountDrivingTime - durBetweenPreECityAndTemEnCity - drivingTime;
                                    //System.out.println("drivingTimeLeft="+drTimeLefToDepot);

                                }
                                /**
                                 * here the outside if--consider the normal case, else is the special case
                                 * which  consider after combined trip how can we create a new trip
                                 * */

                                if (idOfShift == 0 || (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 1)) {
//                                    System.out.println("now we come to case 1");
//                                    System.out.println("check the time condition in case 1" + (arrTimeLefToDepot >= minPlanTime && worTimeLefToDepot >= minPlanTime && drTimeLefToDepot >= 0));
                                    if (arrTimeLefToDepot >= minPlanTime && worTimeLefToDepot >= minPlanTime && drTimeLefToDepot >= 0) {
                                        candidateTemEnCity.add(temEnCity);
                                        nbCandidates++;
//                                        System.out.println("nbCandidates:" + nbCandidates);
//                                        System.out.println("No problem, and the candidates in case1 are :" + candidateTemEnCity);
                                    }
                                } else {
//                                    System.out.println("check case 2" + (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 2));
                                    if (arrTimeLefToDepot >= maxShortConnection && worTimeLefToDepot >= maxShortConnection && drTimeLefToDepot >= 0) {
                                        candidateTemEnCity.add(temEnCity);
                                        nbCandidates++;
//                                        System.out.println("nbCandidates:" + nbCandidates);
//                                        System.out.println("candidate in case 2 :" + candidateTemEnCity);
                                    }

                                }
                                /**
                                 *  the upside of else is the special case
                                 * which is considered after combined trip how can we create a new trip
                                 * */
                            }

                        }

                        //if(candidateTemEnCity.size()>0) {
//                        System.out.println(candidateTemEnCity.size() > 0); //this is for sure is true, otherwise there is a problem
                        startCity = endCity;
                        idOfRandomStartCity = startCity.getIdOfCity();
                        //System.out.println(arrivalTime);

                        if (workingTime <= discountWorkingTime && drivingTime <= discountDrivingTime) {
                            //  System.out.println("preEndCity is not a  good city");
                            int indexOfTemEnCity = this.rnd.nextInt(candidateTemEnCity.size());
                            idOfRandomEndCity = candidateTemEnCity.get(indexOfTemEnCity).getIdOfCity();
                            endCity = instance.getCities().get(idOfRandomEndCity);
                            duration = instance.getDuration(startCity, endCity);//2024.9.25
                            int arTimeLefToDepot = Integer.MAX_VALUE;
                            int woTimeLefToDepot = Integer.MAX_VALUE;
                            int maxConTime = Integer.MAX_VALUE;

                            if (endCity.getIdOfCity() != idOfCityAsDepot) {//check whether the idOfCityAsDepot is the same idOfRandomDepot
                                int durationBetweenEnCityAndDepot = instance.getDuration(endCity, depot);//2024.9.25
                                arTimeLefToDepot = t_eHorizon - duration - arrivalTime - durationBetweenEnCityAndDepot - minPlanTime;//避免cplex出差错
                                woTimeLefToDepot = discountWorkingTime - duration - workingTime - durationBetweenEnCityAndDepot - minPlanTime;

                            } else {//endCity.getIdOfCity ==idOfCityAsDepot
                                arTimeLefToDepot = t_eHorizon - duration - arrivalTime;
                                woTimeLefToDepot = discountWorkingTime - duration - workingTime;
                            }
                            //this part is to calculate the maxConnectionTime = the min of these three
                            int[] conTime = new int[]{maxWaitingTime, arTimeLefToDepot, woTimeLefToDepot};

                            maxConTime = conTime[0];
                            for (int i = 0; i < conTime.length; i++) {
                                //chose  maxConnectionTime is the min of these three element
                                if (conTime[i] < maxConTime) {
                                    maxConTime = conTime[i];
                                }
                            }
//                            /**
//                             * Here I start to modify  to consider the overlap cases 1104 for those preEndCity is not the depot case
//                             * */
//                            // Step1:check whether there is former trip could cause overlap
//                            boolean whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 = false;
//                            boolean whetherOverlapCouldFeasiConnectFromTimeView_1 = false;
//                            int conTimeConsiderCreateOverlap_1 = 0;
//                            if (idOfShift > 0) {
//                                LinkedList<Trip> formerTripMayOverLapWithCurrentTrip = new LinkedList<>();
//                                for (int s = 0; s < feasibleSolution.getShifts().size(); s++) {
//                                    Shift shiftFormer = feasibleSolution.getShifts().get(s);
//                                    for (int t = 0; t < shiftFormer.getTrips().size(); t++) {
//                                        Trip tripInforShift = shiftFormer.getTrips().get(t);
//                                        int idSCityFor = tripInforShift.getIdOfStartCity();
//                                        if (idSCityFor == idOfRandomEndCity) {
//                                            formerTripMayOverLapWithCurrentTrip.add(tripInforShift);
//                                        }
//
//                                    }
//                                }
//                                // here is to define the connection when it could be considered to have overlap with the former trip in the former shift
//                                if (formerTripMayOverLapWithCurrentTrip.size() > 0) {
//                                    whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 = true;
//                                    //Step2: choose a random formerTrip
//                                    int randomIndex = this.rnd.nextInt(formerTripMayOverLapWithCurrentTrip.size());
//                                    Trip tripConsideredOverlapWithCurrent = formerTripMayOverLapWithCurrentTrip.get(randomIndex);
//                                    //Step3: chose randomSet, and check whether could cause overlap in time
//                                    // Choose a random set
//                                    int randomIndexOfSet = this.rnd.nextInt(timePeriodSets.size());
//                                    int[] randomSetRelatedToOverlapConnection = timePeriodSets.get(randomIndexOfSet);
//
//                                    // Iterate through each time value in the chosen set
//                                    System.out.println("Iterating through each time in the chosen set:");
//                                    for (int time = randomSetRelatedToOverlapConnection[0]; time <= randomSetRelatedToOverlapConnection[1]; time++) {
//                                        System.out.println("Time for overlap is chosen from set:" + randomIndexOfSet + " time is :" + time);
//                                    }
//
//                                    //step4: Iterate through each time value in the chosen set calculate the connection time to the preTrip
//                                    for (int time = randomSetRelatedToOverlapConnection[0]; time < randomSetRelatedToOverlapConnection[1]; time++) {
////                                        System.out.println("Time: " + time);
//                                        //step3.3: calculte the connecitonTimeGiven by the formerOverlapTrip with the connection time of formerTrip belong to randomSet
//                                        int conTimeConsideringOverlap = tripConsideredOverlapWithCurrent.getDepartureTime() - time - duration - arrivalTime;
//                                        //Modify for create the overlap 2024.11.04
//                                        // Before this part we check whether could create some overlap; then decide the connection time is
//                                        // case1: the random value within [minPlan, maxConnection] for nbV==1 case;
//                                        // ([minShortConnectionForCombine, maxConnection]) for nbV==2 case;
//                                        // or case2: given by former trip to create overlap which belong to the [minPlan, maxConnection] for nbV==1 case
//                                        // ( or belong to the [minShortConnectionForCombine, maxConnection] for nbV==2 case)
//                                        if (currentTripInShift.getNbCombinedVehicle() == 1 && tripConsideredOverlapWithCurrent.getNbCombinedVehicle() == 1) {
//                                            if (conTimeConsideringOverlap >= minPlanTime && conTimeConsideringOverlap <= maxConTime) {
//                                                whetherOverlapCouldFeasiConnectFromTimeView_1 = true;
//                                                conTimeConsiderCreateOverlap_1 = conTimeConsideringOverlap;
//                                                break;
//                                            }
//                                        } else {// the current trip or the trip to overlap is combined trip
//                                            if (conTimeConsideringOverlap >= maxShortConnection && conTimeConsideringOverlap <= maxConTime) {
//                                                whetherOverlapCouldFeasiConnectFromTimeView_1 = true;
//                                                conTimeConsiderCreateOverlap_1 = conTimeConsideringOverlap;
//                                                break;
//                                            }
//
//                                        }
//                                    }
//                                }
//
//                            }
//                            if ((whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == true
//                                    && whetherOverlapCouldFeasiConnectFromTimeView_1 == true)) {
//                                // the connection value is given decide by the former overlap trip  if it belong to the good connection
//                                connectionTimeRandom = conTimeConsiderCreateOverlap_1;
//                                System.out.println("the connection after modify the overlap ");
//                                //not belong to good connection, then still use the normal random connection
//                            } else {
//                                //( case1: whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == false ||
//                                //  case2: whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == true && whetherOverlapCouldFeasiConnectFromTimeView_1 == false))
//                                //then this is normal random connection
//                                if (idOfShift == 0 || (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 1)) {
//                                    if (minPlanTime > maxConTime) {
//                                        System.out.println("Here maxConTime should greater than minPlanTime, but now there is an error" + maxConTime + " " + (maxConTime >= minPlanTime));
//                                    }
//                                    connectionTimeRandom = (int) (minPlanTime + (maxConTime - minPlanTime) * this.rnd.nextDouble());
//                                    //************2024.9.26 保证connection time 是timeSlot的倍数
//                                    connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
//                                    // ****************2024.9.26 保证connection time is timeSlot的倍数
//
//                                    //System.out.println("line706" + "connectionTimeRandom: " + connectionTimeRandom + " " + (connectionTimeRandom >= 15));
//                                } else {
//                                    //here is for the trip which is after the combined trip
//                                    connectionTimeRandom = (int) (maxShortConnection + (maxConTime - maxShortConnection) * this.rnd.nextDouble());
//                                    //************2024.9.26 保证connection time 是timeSlot的倍数
//                                    connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
//                                    // ****************2024.9.26 保证conection time is timeSlot的倍数
//                                }
//                            }
//                            /**
//                             * until now the case of whether create connection could cause overlap CASE_1 with the former trip is  finished, which contains in the value of connectionTimeRandom
//                             * */


                            /**
                             * Here I start to modify  to consider the overlap cases 11.15 for those preEndCity is not the depot case
                             * */
                            // Step1:check whether there is former trip could cause overlap
                            boolean whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 = false;
                            boolean whetherOverlapCouldFeasiConnectFromTimeView_1 = false;
                            int conTimeConsiderCreateOverlap_1 = 0;
                            if (idOfShift > 0) {
                                LinkedList<Trip> formerTripMayOverLapWithCurrentTrip = new LinkedList<>();
                                for (int s = 0; s < feasibleSolution.getShifts().size(); s++) {
                                    Shift shiftFormer = feasibleSolution.getShifts().get(s);
                                    for (int t = 0; t < shiftFormer.getTrips().size(); t++) {
                                        Trip tripInforShift = shiftFormer.getTrips().get(t);
                                        // this following three line is the different part 11.15
                                        int idECityFor = tripInforShift.getIdOfEndCity();
                                        if (idECityFor == startCity.getIdOfCity()) {
                                            formerTripMayOverLapWithCurrentTrip.add(tripInforShift);
                                        }
                                        // this above three line is the different  part 11.15

                                    }
                                }
                                // here is to define the connection when it could be considered to have overlap with the former trip in the former shift
                                if (formerTripMayOverLapWithCurrentTrip.size() > 0) {
                                    whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 = true;
                                    //Step2: choose a random formerTrip
                                    int randomIndex = this.rnd.nextInt(formerTripMayOverLapWithCurrentTrip.size());
                                    Trip tripConsideredOverlapWithCurrent = formerTripMayOverLapWithCurrentTrip.get(randomIndex);
                                    //Step3: chose randomSet, and check whether could cause overlap in time
                                    // Choose a random set
                                    int randomIndexOfSet = this.rnd.nextInt(timePeriodSets.size());
                                    int[] randomSetRelatedToOverlapConnection = timePeriodSets.get(randomIndexOfSet);

                                    // Iterate through each time value in the chosen set
                                    //System.out.println("Iterating through each time in the chosen set:");
                                    for (int time = randomSetRelatedToOverlapConnection[0]; time <= randomSetRelatedToOverlapConnection[1]; time++) {
                                        //System.out.println("Time for overlap is chosen from set:" + randomIndexOfSet + " time is :" + time);
                                    }

                                    //step4: Iterate through each time value in the chosen set calculate the connection time to the preTrip
                                    for (int time = randomSetRelatedToOverlapConnection[0]; time < randomSetRelatedToOverlapConnection[1]; time++) {
//                                        System.out.println("Time: " + time);
                                        //step3.3: calculte the connecitonTimeGiven by the formerOverlapTrip with the connection time of formerTrip belong to randomSet
                                        int conTimeConsideringOverlap = tripConsideredOverlapWithCurrent.getArrivalTime() - arrivalTime - time ;
                                        //Modify for create the overlap 2024.11.04
                                        // Before this part we check whether could create some overlap; then decide the connection time is
                                        // case1: the random value within [minPlan, maxConnection] for nbV==1 case;
                                        // ([minShortConnectionForCombine, maxConnection]) for nbV==2 case;
                                        // or case2: given by former trip to create overlap which belong to the [minPlan, maxConnection] for nbV==1 case
                                        // ( or belong to the [minShortConnectionForCombine, maxConnection] for nbV==2 case)
                                        if (currentTripInShift.getNbCombinedVehicle() == 1 && tripConsideredOverlapWithCurrent.getNbCombinedVehicle() == 1) {
                                            if (conTimeConsideringOverlap >= minPlanTime && conTimeConsideringOverlap <= maxConTime) {
                                                whetherOverlapCouldFeasiConnectFromTimeView_1 = true;
                                                conTimeConsiderCreateOverlap_1 = conTimeConsideringOverlap;
                                                break;
                                            }
                                        } else {// the current trip or the trip to overlap is combined trip
                                            if (conTimeConsideringOverlap >= maxShortConnection && conTimeConsideringOverlap <= maxConTime) {
                                                whetherOverlapCouldFeasiConnectFromTimeView_1 = true;
                                                conTimeConsiderCreateOverlap_1 = conTimeConsideringOverlap;
                                                break;
                                            }

                                        }
                                    }
                                }

                            }
                            if ((whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == true
                                    && whetherOverlapCouldFeasiConnectFromTimeView_1 == true)) {
                                // the connection value is given decide by the former overlap trip  if it belong to the good connection
                                connectionTimeRandom = conTimeConsiderCreateOverlap_1;
                                System.out.println("the connection after modify the overlap ");
                                //not belong to good connection, then still use the normal random connection
                            } else {
                                //( case1: whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == false ||
                                //  case2: whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == true && whetherOverlapCouldFeasiConnectFromTimeView_1 == false))
                                //then this is normal random connection
                                if (idOfShift == 0 || (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 1)) {
                                    if (minPlanTime > maxConTime) {
                                        System.out.println("Here maxConTime should greater than minPlanTime, but now there is an error" + maxConTime + " " + (maxConTime >= minPlanTime));
                                    }
                                    connectionTimeRandom = (int) (minPlanTime + (maxConTime - minPlanTime) * this.rnd.nextDouble());
                                    //************2024.9.26 保证connection time 是timeSlot的倍数
                                    connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
                                    // ****************2024.9.26 保证connection time is timeSlot的倍数

                                    //System.out.println("line706" + "connectionTimeRandom: " + connectionTimeRandom + " " + (connectionTimeRandom >= 15));
                                } else {
                                    //here is for the trip which is after the combined trip
                                    connectionTimeRandom = (int) (maxShortConnection + (maxConTime - maxShortConnection) * this.rnd.nextDouble());
                                    //************2024.9.26 保证connection time 是timeSlot的倍数
                                    connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
                                    // ****************2024.9.26 保证conection time is timeSlot的倍数
                                }
                            }
                            /**
                             * until now the case of whether create connection could cause overlap CASE_1 with the former trip is  finished, which contains in the value of connectionTimeRandom 11.15
                             * */

                            departureTime = arrivalTime + connectionTimeRandom;
                            //departureTime = Math.round(100.0 * departureTime) / 100.0;
                            arrivalTime = departureTime + duration;
                            //arrivalTime = Math.round(100.0 * arrivalTime) / 100.0;
                            workingTime = workingTime + connectionTimeRandom + duration;
                            drivingTime = drivingTime + duration;
                            nbRandomNbVehiclesInTrip = 1;
                            //****************below 2024.10.08

                            //********modify below 2024.10.08
                            int earliestDeparture = departureTime - folderForTW * timeSlotUnit;
                            int latestDeparture = departureTime + folderForTW * timeSlotUnit;
                            int curTW = latestDeparture - earliestDeparture;
                            System.out.println("now the earliest departure 1045"+earliestDeparture);
                            System.out.println("now the latest departure 1045:"+latestDepartureTime);
                            if(Math.abs(curTW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1){
                                System.out.println("now the there is an error on line 1048 for time window is :" + curTW+" which is not as required as "+2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
                            }

                            //********modify above 2024.10.08
                            Trip tripCreateWhenPreEnCityIsNotDepot = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity,
                                    departureTime, earliestDeparture, latestDeparture, nbRandomNbVehiclesInTrip, duration);
                            //****************above 2024.9.25
                            instance.addTrip(tripCreateWhenPreEnCityIsNotDepot);
                            shift.addTrip(tripCreateWhenPreEnCityIsNotDepot);

                            sumTrip = sumTrip + 1;
                            idOfTrip = sumTrip;
                            //这里我有个疑问，假如选择了candidate 是depot, 那这个shift是不是应该+1呢？还是应该判断他是能继续，不能继续就应该加1，可以进去下一次trip来判断
                        } else {
                            //working time already greater than discounted time, then directly go back to the depot , and start a new shift
                            idOfRandomEndCity = idOfCityAsDepot;
                            endCity = depot;
                            duration = instance.getDuration(startCity, endCity);//2024.9.25

                            //this part is to calculate the maxConnectionTime = the min of these three

                            if (idOfShift == 0 || (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 1)) {
                                //System.out.println("line704" + "maxConTime" + maxConTime + " " + (maxConTime >= minPlanTime));
                                connectionTimeRandom = minPlanTime;
                                //System.out.println("line706" + "connectionTimeRandom: " + connectionTimeRandom + " " + (connectionTimeRandom >= 15));
                            } else {
                                //here is for the trip which is after the combined trip
                                connectionTimeRandom = maxShortConnection;


                                //************2024.9.26 保证connection time 是timeSlot的倍数
                                connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
                                // ****************2024.9.26 保证conection time is timeSlot的倍数

                            }
                            departureTime = arrivalTime + connectionTimeRandom;
                            //departureTime = Math.round(100.0 * departureTime) / 100.0;
                            arrivalTime = departureTime + duration;
                            //arrivalTime = Math.round(100.0 * arrivalTime) / 100.0;
                            workingTime = workingTime + connectionTimeRandom + duration;
                            drivingTime = drivingTime + duration;
                            nbRandomNbVehiclesInTrip = 1;

                            //******************************** below 2024.10.08 ********************************
                            //********modify below 2024.10.08
                            int earliestDeparture = departureTime - folderForTW * timeSlotUnit;

                            int latestDeparture = departureTime + folderForTW * timeSlotUnit;

                            int curTW = latestDeparture - earliestDeparture;
                            if(Math.abs(curTW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1) {
                                System.out.println("now there is an error on line 1099 as the current time window is"+curTW +" which is not as required "+2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
                            }
                            //********modify above 2024.10.08
                            Trip tripCreateWhenPreEnCityIsNotDepot = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity,
                                    departureTime, earliestDeparture, latestDeparture, nbRandomNbVehiclesInTrip, duration);
                            //******************************** above 2024.10.08 ********************************

                            instance.addTrip(tripCreateWhenPreEnCityIsNotDepot);

                            sumTrip = sumTrip + 1;
                            idOfTrip = sumTrip;

                            shift.addTrip(tripCreateWhenPreEnCityIsNotDepot);
                            whetherFirstTrip = false;

                            feasibleSolution.addShift(shift);
                            //System.out.println("shift at line 769 : "+shift);
                            addTrip = false;
                            sumShift = sumShift + 1;
                            idOfShift = sumShift;



                            //**********************************below code is add to modify the start time of new shift 2024.11.18
                            ArrayList<Integer> modifyStartHorizonList=new ArrayList<>();
                            for(int s=0;s<feasibleSolution.getShifts().size();s++){
                                Shift shift1=this.feasibleSolution.getShifts().get(s);
                                int endTimeOfFormerShift=shift1.getTrips().getLast().getArrivalTime();
                                if(endTimeOfFormerShift<latestDepartureTime) {
                                    modifyStartHorizonList.add(endTimeOfFormerShift);
                                }
                            }
                            if(modifyStartHorizonList.size()>0){
                                // Generate a random index
                                int randomIndex = this.rnd.nextInt(modifyStartHorizonList.size());
                                System.out.println("1 size of modifyHorzonList "+modifyStartHorizonList.size());
                                startHorizon=modifyStartHorizonList.get(randomIndex)-minPlanTime;
                                latestDepartureTime=startHorizon+minPlanTime;
                                System.out.println("1 New shift_"+idOfShift+" modified the start horizon: " + startHorizon);
                            }else {
                                startHorizon=adjustedStartHorizonForTW;
                                latestDepartureTime= adjustLatestDepartureTimeForTW;
                                latestDepartureTime = (int) (Math.floor(latestDepartureTime / timeSlotUnit) * timeSlotUnit);
                            }
                            //**********************************above modify 2024.11.18




                            shift = new Shift(idOfShift);
                            whetherFirstTrip = true;

                            // Following  modify 2-1 (4) here is to make the new time limit for the  new shift is random number from (1,4)*24% *total max: 24%-96%
                            m = 1 + this.rnd.nextInt(maxFolderOfDiscountTime);
                            discountDrivingTime = (int) (m * percentageOfDiscount * this.input.getMaxDrivingTime());
                            discountWorkingTime = (int) (m * percentageOfDiscount * this.input.getMaxWorkingTime());
                            // Above modification  2-1 is to make the shift have shorter and longer

                        }

                    } else {
                        //when preEndCity is good Depot
                        /**
                         * create a new trip when preEnCity is a good depot ------------------------P4
                         * here is the third part
                         */
                        for (int i = 0; i < nbCities; i++) {
                            City temEnCity = instance.getCities().get(i);
                            int durBetweenPreECityAndTemEnCity = instance.getDuration(temEnCity, endCity);//2024.9.25
                            int durBetweenTemEnCityAndDepot = instance.getDuration(temEnCity, depot);//2024.9.25
                            int idOfTemEnCity = temEnCity.getIdOfCity();
                            if (idOfTemEnCity != idOfRandomEndCity) {//here is to guarantee that this temCity is not the preEnCity
                                int arrTimeLefToDepot = t_eHorizon - durBetweenPreECityAndTemEnCity - arrivalTime - durBetweenTemEnCityAndDepot - minPlanTime;
                                int worTimeLefToDepot = discountWorkingTime - durBetweenPreECityAndTemEnCity - workingTime
                                        - durBetweenTemEnCityAndDepot - minPlanTime;
                                int drTimeLefToDepot = discountDrivingTime - durBetweenPreECityAndTemEnCity - drivingTime - durBetweenTemEnCityAndDepot;
                                if (idOfShift == 0 || (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 1)) {
                                    if (arrTimeLefToDepot >= minPlanTime && worTimeLefToDepot >= minPlanTime && drTimeLefToDepot >= 0) {
                                        candidateTemEnCity.add(temEnCity);
                                    }
                                } else {
                                    //here is for the following trip after combined
                                    if (arrTimeLefToDepot >= maxShortConnection && worTimeLefToDepot >= maxShortConnection && drTimeLefToDepot >= 0) {
                                        candidateTemEnCity.add(temEnCity);
                                    }

                                }
                            }
                        }
                        if (candidateTemEnCity.size() > 0 && workingTime <= discountWorkingTime && drivingTime <= discountDrivingTime) {
                            startCity = endCity;
                            idOfRandomStartCity = startCity.getIdOfCity();
                            int indexOfTemEnCity = this.rnd.nextInt(candidateTemEnCity.size());
                            idOfRandomEndCity = candidateTemEnCity.get(indexOfTemEnCity).getIdOfCity();
                            endCity = instance.getCities().get(idOfRandomEndCity);
                            duration = instance.getDuration(startCity, endCity);//2024.9.25

                            //check whether the idOfCityAsDepot is the same idOfRandomDepot
                            int durationBetweenEnCityAndDepot = instance.getDuration(endCity, depot);//2024.9.25
                            int arTimeLefToDepot = t_eHorizon - duration - arrivalTime - durationBetweenEnCityAndDepot - minPlanTime;
                            int woTimeLefToDepot = discountWorkingTime - duration - workingTime - durationBetweenEnCityAndDepot - minPlanTime;

                            //this part is to calculate the maxConnectionTime
                            int[] conTime = new int[]{maxWaitingTime, arTimeLefToDepot, woTimeLefToDepot};
                            int maxConTime = conTime[0];
                            for (int i = 0; i < conTime.length; i++) {
                                //chose  maxConnectionTime is the min of these three element
                                if (conTime[i] < maxConTime) {
                                    maxConTime = conTime[i];
                                }
                            }
//                            /**
//                             * Here I start to modify  to consider the overlap cases case2 11.15 for those preEndCity is not the depot case
//                             * */
//                            // Step1:check whether there is former trip could cause overlap
//                            boolean whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_2 = false;
//                            boolean whetherOverlapCouldFeasiConnectFromTimeView_2 = false;
//                            int conTimeConsiderCreateOverlap_2 = 0;
//                            if (idOfShift > 0) {
//                                LinkedList<Trip> formerTripMayOverLapWithCurrentTrip = new LinkedList<>();
//                                for (int s = 0; s < feasibleSolution.getShifts().size(); s++) {
//                                    Shift shiftFormer = feasibleSolution.getShifts().get(s);
//                                    for (int t = 0; t < shiftFormer.getTrips().size(); t++) {
//                                        Trip tripInforShift = shiftFormer.getTrips().get(t);
//
//                                        // this following three line is the different part 11.15
//                                        int idECityFor = tripInforShift.getIdOfEndCity();
//                                        if (idECityFor == startCity.getIdOfCity()) {
//                                            formerTripMayOverLapWithCurrentTrip.add(tripInforShift);
//                                        }
//                                        // this above three line is the different  part 11.15
//
//                                    }
//                                }
//                                // here is to define the connection when it could be considered to have overlap with the former trip in the former shift
//                                if (formerTripMayOverLapWithCurrentTrip.size() > 0) {
//                                    whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_2 = true;
//                                    //Step2: choose a random formerTrip
//                                    int randomIndex = this.rnd.nextInt(formerTripMayOverLapWithCurrentTrip.size());
//                                    Trip tripConsideredOverlapWithCurrent = formerTripMayOverLapWithCurrentTrip.get(randomIndex);
//                                    //Step3: chose randomSet, and check whether could cause overlap in time
//                                    // Choose a random set
//                                    int randomIndexOfSet = this.rnd.nextInt(timePeriodSets.size());
//                                    int[] randomSetRelatedToOverlapConnection = timePeriodSets.get(randomIndexOfSet);
//
//                                    // Iterate through each time value in the chosen set
//                                    System.out.println("Iterating through each time in the chosen set:");
//                                    for (int time = randomSetRelatedToOverlapConnection[0]; time <= randomSetRelatedToOverlapConnection[1]; time++) {
//                                        System.out.println("Time for overlap is chosen from set:" + randomIndexOfSet + " time is :" + time);
//                                    }
//
//                                    //step4: Iterate through each time value in the chosen set calculate the connection time to the preTrip
//                                    for (int time = randomSetRelatedToOverlapConnection[0]; time < randomSetRelatedToOverlapConnection[1]; time++) {
////                                        System.out.println("Time: " + time);
//                                        //step3.3: calculte the connecitonTimeGiven by the formerOverlapTrip with the connection time of formerTrip belong to randomSet
//                                        int conTimeConsideringOverlap = tripConsideredOverlapWithCurrent.getArrivalTime() - arrivalTime - time ;
//                                        //Modify for create the overlap 2024.11.04
//                                        // Before this part we check whether could create some overlap; then decide the connection time is
//                                        // case1: the random value within [minPlan, maxConnection] for nbV==1 case;
//                                        // ([minShortConnectionForCombine, maxConnection]) for nbV==2 case;
//                                        // or case2: given by former trip to create overlap which belong to the [minPlan, maxConnection] for nbV==1 case
//                                        // ( or belong to the [minShortConnectionForCombine, maxConnection] for nbV==2 case)
//                                        if (currentTripInShift.getNbCombinedVehicle() == 1 && tripConsideredOverlapWithCurrent.getNbCombinedVehicle() == 1) {
//                                            if (conTimeConsideringOverlap >= minPlanTime && conTimeConsideringOverlap <= maxConTime) {
//                                                whetherOverlapCouldFeasiConnectFromTimeView_2 = true;
//                                                conTimeConsiderCreateOverlap_2 = conTimeConsideringOverlap;
//                                                break;
//                                            }
//                                        } else {// the current trip or the trip to overlap is combined trip
//                                            if (conTimeConsideringOverlap >= maxShortConnection && conTimeConsideringOverlap <= maxConTime) {
//                                                whetherOverlapCouldFeasiConnectFromTimeView_2 = true;
//                                                conTimeConsiderCreateOverlap_2 = conTimeConsideringOverlap;
//                                                break;
//                                            }
//
//                                        }
//                                    }
//                                }
//
//                            }
//                            if ((whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_2 == true
//                                    && whetherOverlapCouldFeasiConnectFromTimeView_2 == true)) {
//                                // the connection value is given decide by the former overlap trip  if it belong to the good connection
//                                connectionTimeRandom = conTimeConsiderCreateOverlap_2;
//                                System.out.println("the connection after modify the overlap ");
//                                //not belong to good connection, then still use the normal random connection
//                            } else {
//                                //( case1: whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == false ||
//                                //  case2: whetherForShiftsHaveCandiTripForOverlapOnlyFromLocationView_1 == true && whetherOverlapCouldFeasiConnectFromTimeView_1 == false))
//                                //then this is normal random connection
//                                if (idOfShift == 0 || (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 1)) {
//                                    connectionTimeRandom = (int) (minPlanTime + (maxConTime - minPlanTime) * this.rnd.nextDouble());
//                                    //************2024.9.26 保证connection time 是timeSlot的倍数
//                                    connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
//                                    // ****************2024.9.26 保证conection time is timeSlot的倍数
//
//                                } else {
//                                    //here is for the following trip after combined
//                                    connectionTimeRandom = (int) (maxShortConnection + (maxConTime - maxShortConnection) * this.rnd.nextDouble());
//                                    //************2024.9.26 保证connection time 是timeSlot的倍数
//                                    connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
//                                    // ****************2024.9.26 保证conection time is timeSlot的倍数
//
//                                }
//                            }
//                            /**
//                             * until now the case of whether create connection could cause overlap CASE_2 with the former trip is  finished, which contains in the value of connectionTimeRandom 11.15
//                             * */

                            if (idOfShift == 0 || (idOfShift > 0 && currentTripInShift.getNbCombinedVehicle() == 1)) {
                                connectionTimeRandom = (int) (minPlanTime + (maxConTime - minPlanTime) * this.rnd.nextDouble());
                                //************2024.9.26 保证connection time 是timeSlot的倍数
                                connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
                                // ****************2024.9.26 保证conection time is timeSlot的倍数

                            } else {
                                //here is for the following trip after combined
                                connectionTimeRandom = (int) (maxShortConnection + (maxConTime - maxShortConnection) * this.rnd.nextDouble());
                                //************2024.9.26 保证connection time 是timeSlot的倍数
                                connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
                                // ****************2024.9.26 保证conection time is timeSlot的倍数

                            }

                            departureTime = arrivalTime + connectionTimeRandom;
                            // departureTime = Math.round(100.0 * departureTime) / 100.0;
                            arrivalTime = departureTime + duration;
                            //arrivalTime = Math.round(100.0 * arrivalTime) / 100.0;

                            workingTime = workingTime + connectionTimeRandom + duration;
                            drivingTime = drivingTime + duration;

                            nbRandomNbVehiclesInTrip = 1;
                            //****************below***********2024.10.08
                            //********modify below 2024.10.08
                            int earliestDeparture = departureTime - folderForTW * timeSlotUnit;
                            int latestDeparture = departureTime + folderForTW * timeSlotUnit;
                            int curTW = latestDeparture - earliestDeparture;
                            if(Math.abs(curTW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1){
                            System.out.println("there is an error on line 1344 now the time window is " + curTW+" which is not as required "+2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
                            }
                            //********modify above 2024.10.08

                            Trip tripCreateWhenPreEnCityIsDepot = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity,
                                    departureTime, earliestDeparture, latestDeparture, nbRandomNbVehiclesInTrip, duration);
                            instance.addTrip(tripCreateWhenPreEnCityIsDepot);
                            //****************above***********2024.9.25
                            shift.addTrip(tripCreateWhenPreEnCityIsDepot);
                            whetherFirstTrip = false;

                            sumTrip = sumTrip + 1;
                            idOfTrip = sumTrip;

                        } else {
                            feasibleSolution.addShift(shift);
                            addTrip = false;
                            sumShift = sumShift + 1;
                            idOfShift = sumShift;


                            //******* when it starts a new shift, then Add following code to create more over lap between different shift since here modify startHorizon to make the new shift continue to the preShift******************** 2024.11.18
                            if(idOfShift>0) {

                                ArrayList<Integer> modifyStartHorizonList=new ArrayList<>();
                                for(int s=0;s<feasibleSolution.getShifts().size();s++){
                                    Shift shift1=this.feasibleSolution.getShifts().get(s);
                                    int endTimeOfFormerShift=shift1.getTrips().getLast().getArrivalTime();
                                    if(endTimeOfFormerShift<latestDepartureTime) {
                                        modifyStartHorizonList.add(endTimeOfFormerShift);
                                    }
                                }
                                if(modifyStartHorizonList.size()>0){
                                    // Generate a random index
                                    int randomIndex = this.rnd.nextInt(modifyStartHorizonList.size());
                                    System.out.println("2 size of modifyHorzonList "+modifyStartHorizonList.size());
                                    startHorizon=modifyStartHorizonList.get(randomIndex)-minPlanTime;
                                    latestDepartureTime=startHorizon+minPlanTime;
                                    System.out.println("2 New shift_"+idOfShift+" modified the start horizon: " + startHorizon);
                                }else {
                                    startHorizon=adjustedStartHorizonForTW;
                                    latestDepartureTime= adjustLatestDepartureTimeForTW;
                                    latestDepartureTime = (int) (Math.floor(latestDepartureTime / timeSlotUnit) * timeSlotUnit);
                                }
                                //**********************************above modify 2024.11.18
                            }
                            //**********************************above modify 2024.11.18
                            shift = new Shift(idOfShift);
                            whetherFirstTrip = true;

                            // Following  modify 2-2(4) here is to make the new time limit for the  new shift is random number from (1,4)*24% *total max: 24%-96%
                            m = 1 + this.rnd.nextInt(maxFolderOfDiscountTime);
                            discountDrivingTime = (int) (m * percentageOfDiscount * this.input.getMaxDrivingTime());
                            discountWorkingTime = (int) (m * percentageOfDiscount * this.input.getMaxWorkingTime());
//                            // Above modification  2-2 is to make the shift have shorter and longer
                            System.out.println("New shift startHorizon:"+startHorizon);
                        }

                    }

                }
            }

        }

        /**
         * Now here is the last part to create the last three trip------------------------------------------P5 and P6
         * first problem is that I need to know which is the depot in the last shift
         * I need collect all trip in the last shift, find the mini id of that trip
         * then the start City of that trip  is the depot, then we need continue according to that city
         * */

//        // after this preparing work, we start to create a new trip-----------------P5
        if (shift.getTrips().size() != 0 && (shift.getTrips().getLast().getIdOfEndCity() == shift.getTrips().getFirst().getIdOfStartCity())) {
            feasibleSolution.addShift(shift);
            sumShift = sumShift + 1;
            idOfShift = sumShift;

            //******* when it starts a new shift, then Add following code to create more over lap between different shift since here modify startHorizon to make the new shift continue to the preShift******************** 2024.11.18

            ArrayList<Integer> modifyStartHorizonList=new ArrayList<>();
            for(int s=0;s<feasibleSolution.getShifts().size();s++){
                Shift shift1=this.feasibleSolution.getShifts().get(s);
                int endTimeOfFormerShift=shift1.getTrips().getLast().getArrivalTime();
                if(endTimeOfFormerShift<latestDepartureTime) {
                    modifyStartHorizonList.add(endTimeOfFormerShift);
                }
            }
            if(modifyStartHorizonList.size()>0){
                // Generate a random index
                int randomIndex = this.rnd.nextInt(modifyStartHorizonList.size());
                System.out.println("3 size of modifyHorzonList "+modifyStartHorizonList.size());
                startHorizon=modifyStartHorizonList.get(randomIndex)-minPlanTime;
                latestDepartureTime=startHorizon+minPlanTime;
                System.out.println("3 New shift_"+idOfShift+" modified the start horizon: " + startHorizon);
            }else {
                startHorizon=adjustedStartHorizonForTW;
                latestDepartureTime= adjustLatestDepartureTimeForTW;
                latestDepartureTime = (int) (Math.floor(latestDepartureTime / timeSlotUnit) * timeSlotUnit);
            }
            //**********************************above modify 2024.11.18

            shift = new Shift(idOfShift);
            // Following  modify 2-3(4) here is to make the new time limit for the  new shift is random number from (1,4)*24% *total max: 24%-96%
            m = 1 + this.rnd.nextInt(maxFolderOfDiscountTime);
            discountDrivingTime = (int) (m * percentageOfDiscount * this.input.getMaxDrivingTime());
            discountWorkingTime = (int) (m * percentageOfDiscount * this.input.getMaxWorkingTime());
            // Above modification  2-3 is to make the shift have shorter and longer
        }

        if (shift.getTrips().size() != 0 && (shift.getTrips().getLast().getIdOfEndCity() != shift.getTrips().getFirst().getIdOfStartCity())) {
            //This is the first case, when preEnCity is not a good depot in the last three trips
            // we just continue the first trip end at good depot, then restart  a shift with two trips
            int idOfCityAsDepot = shift.getTrips().getFirst().getIdOfStartCity();
            int idOfRandomEndCity = shift.getTrips().getLast().getIdOfEndCity();
            int arrivalTime = shift.getTrips().getLast().getArrivalTime();
            //**********************************
            City startCity = instance.getCities().get(idOfRandomEndCity);
            int idOfRandomStartCity = startCity.getIdOfCity();
            City endCity = instance.getCities().get(idOfCityAsDepot);
            idOfRandomEndCity = idOfCityAsDepot;
            int duration = instance.getDuration(startCity, endCity);//2024.9.25
            int arTimeLefToDepot = (int) (t_eHorizon - arrivalTime - duration);
            arTimeLefToDepot = (int) (Math.floor(arTimeLefToDepot / timeSlotUnit) * timeSlotUnit);//2024.9.26 is timeSlot的倍数


            int woTimeLefToDepot = discountWorkingTime - workingTime - duration; // how I know this working time is just what I want
            woTimeLefToDepot = (int) (Math.floor(woTimeLefToDepot / timeSlotUnit) * timeSlotUnit);//2024.9.26 is timeSlot的倍数

            int drTimeLefToDepot = discountDrivingTime - drivingTime - duration;// how I know this  driving time is just what I want
//            System.out.println("test left arT=" + arTimeLefToDepot);
//            System.out.println("test left woT=" + woTimeLefToDepot);
//            System.out.println("test drT=" + drTimeLefToDepot);

            //this part is to calculate the maxConnectionTime
            int[] conTime = new int[]{maxWaitingTime, arTimeLefToDepot, woTimeLefToDepot};

            int maxConTime = conTime[0];
            for (int i = 0; i < conTime.length; i++) {
                //chose  maxConnectionTime is the min of these three element
                if (conTime[i] < maxConTime) {
                    maxConTime = conTime[i];
                }
            }
            int connectionTimeRandom = (int) (minPlanTime + (maxConTime - minPlanTime) * this.rnd.nextDouble());

            //************2024.9.26 保证connection time 是timeSlot的倍数
            connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
            // ****************2024.9.26 保证conection time is timeSlot的倍数

            int departureTime = (int) (arrivalTime + connectionTimeRandom);
            departureTime = (int) (Math.floor(departureTime / timeSlotUnit) * timeSlotUnit);// ****************2024.9.27 保证 departure time is timeSlot的倍数
            arrivalTime = (int) (departureTime + duration);
            workingTime = (int) (workingTime + connectionTimeRandom + duration);
            drivingTime = (int) (drivingTime + duration);
            int nbRandomNbVehiclesInTrip = 1;

            int idOfTrip = sumTrip;
            // System.out.println(" idShift=" + idOfShift + ", idOfTrip=" + idOfTrip);

            //**********below*************2024.10.08********************

            int earliestDeparture = departureTime - folderForTW * timeSlotUnit;
            int latestDeparture = departureTime + folderForTW * timeSlotUnit;
            int curTW = latestDeparture - earliestDeparture;
            if(Math.abs(curTW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1) {
                System.out.println("there is an error on line 1519: now the time window is "  + curTW+" which is not as required "+2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
            }
            //********modify above 2024.10.08

            Trip trip1 = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity,
                    departureTime, earliestDeparture, latestDeparture, nbRandomNbVehiclesInTrip, duration);
            //**********above************2024.9.25*********************
            instance.addTrip(trip1);
            shift.addTrip(trip1);
            feasibleSolution.addShift(shift);
            sumTrip = sumTrip + 1;
            sumShift = sumShift + 1;

            idOfShift = sumShift;

            //******* when it starts a new shift, then Add following code to create more over lap between different shift since here modify startHorizon to make the new shift continue to the preShift******************** 2024.11.18
            ArrayList<Integer> modifyStartHorizonList=new ArrayList<>();
            for(int s=0;s<feasibleSolution.getShifts().size();s++){
                Shift shift1=this.feasibleSolution.getShifts().get(s);
                int endTimeOfFormerShift=shift1.getTrips().getLast().getArrivalTime();
                if(endTimeOfFormerShift<latestDepartureTime) {
                    modifyStartHorizonList.add(endTimeOfFormerShift);
                }
            }
            if(modifyStartHorizonList.size()>0){
                // Generate a random index
                int randomIndex = this.rnd.nextInt(modifyStartHorizonList.size());
                System.out.println("4 ize of modifyHorzonList "+modifyStartHorizonList.size());
                startHorizon=modifyStartHorizonList.get(randomIndex)-minPlanTime;
                latestDepartureTime=startHorizon+minPlanTime;
                System.out.println("4 New shift_"+idOfShift+" modified the start horizon: " + startHorizon);
            }else {
                startHorizon=adjustedStartHorizonForTW;
                latestDepartureTime= adjustLatestDepartureTimeForTW;
                latestDepartureTime = (int) (Math.floor(latestDepartureTime / timeSlotUnit) * timeSlotUnit);
            }
            //**********************************above modify 2024.11.18


            // Then restart a shift
            shift = new Shift(idOfShift);
            idOfTrip = sumTrip;
            connectionTimeRandom = 0;
            int idOfRandomDepot = this.rnd.nextInt(nbDepots);

            //System.out.println("restart a depot =" + idOfRandomDepot);
            idOfRandomStartCity = this.instance.getDepots().get(idOfRandomDepot).getIdOfCityAsDepot();// start from a  random Depot
            startCity = this.instance.getCities().get(idOfRandomStartCity);
            // System.out.println("idOfRandomStartCity" + idOfRandomStartCity);

            idOfRandomEndCity = this.rnd.nextInt(nbCities);
            //To avoid the arrival city == start city  we can use "while"
            while (idOfRandomEndCity == idOfRandomStartCity) {
                idOfRandomEndCity = this.rnd.nextInt(nbCities);
            }
            // System.out.println("idOfRandomEndCity" + idOfRandomEndCity);
            endCity = this.instance.getCities().get(idOfRandomEndCity);
            // System.out.println("midCity1=" + startCity + " ,midCity2" + endCity);
            duration = this.instance.getDuration(startCity, endCity);//2024.9.25


            departureTime = (int) (adjustedStartHorizonForTW + (adjustLatestDepartureTimeForTW- adjustedStartHorizonForTW) * this.rnd.nextDouble()); //start from a random time from 6 am
            departureTime = (int) (Math.floor(departureTime / timeSlotUnit) * timeSlotUnit);// ****************2024.9.27 保证 departure time is timeSlot的倍数

            arrivalTime = departureTime + duration;
            //arrivalTime = Math.round(100.0 * arrivalTime) / 100.0;
            workingTime = duration;
            drivingTime = duration;
            nbRandomNbVehiclesInTrip = 1;// start with one vehicle  in the trip

            // below is modified by**************************2024.10.08****************
            int earliestDepartureT = departureTime - folderForTW * timeSlotUnit;
            int latestDepartureT = departureTime + folderForTW * timeSlotUnit;
            //已经通过adjustValue 调解了，保证+30 -30都没问题
            int currTW = latestDepartureT - earliestDepartureT;

            if(Math.abs(currTW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1) {

                System.out.println("now there is an error on line 1571 time window is " + currTW+" which is not as required "+ 2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
            }

            //********modify above 2024.10.08

            Trip trip2 = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity, departureTime, earliestDepartureT, latestDepartureT, nbRandomNbVehiclesInTrip, duration);
            //***********************above modified 2024.10.08********************************

            this.instance.addTrip(trip2);
            shift.addTrip(trip2);
            sumTrip = sumTrip + 1;

            //continue the third trip based on the second one
            idOfTrip = sumTrip;
            startCity = endCity;
            idOfRandomStartCity = startCity.getIdOfCity();
            endCity = instance.getCities().get(instance.getDepots().get(idOfRandomDepot).getIdOfCityAsDepot());//modify 2
            idOfRandomEndCity = endCity.getIdOfCity();
            duration = instance.getDuration(startCity, endCity);//2024.9.25
            arTimeLefToDepot = t_eHorizon - arrivalTime;
            woTimeLefToDepot = discountWorkingTime - workingTime;

            conTime = new int[]{maxWaitingTime, arTimeLefToDepot, woTimeLefToDepot};
            maxConTime = conTime[0];
            for (int i = 0; i < conTime.length; i++) {
                //chose  maxConnectionTime is the min of these three element
                if (conTime[i] < maxConTime) {
                    maxConTime = conTime[i];
                }
            }

            connectionTimeRandom = (int) (minPlanTime + (maxConTime - minPlanTime) * this.rnd.nextDouble());
            //************2024.9.26 保证connection time 是timeSlot的倍数
            connectionTimeRandom = (int) (Math.floor(connectionTimeRandom / timeSlotUnit) * timeSlotUnit);
            // ****************2024.9.26 保证conection time is timeSlot的倍数

            departureTime = arrivalTime + connectionTimeRandom;
            departureTime = (int) (Math.floor(departureTime / timeSlotUnit) * timeSlotUnit);// ****************2024.9.27 保证 departure time is timeSlot的倍数

            arrivalTime = departureTime + duration;
            //arrivalTime = Math.round(100.0 * arrivalTime) / 100.0;
            workingTime = workingTime + connectionTimeRandom + duration;
            drivingTime = drivingTime + duration;
            nbRandomNbVehiclesInTrip = 1;


            //****************modify 2024.10.08 *********************
            int earliest = departureTime - folderForTW * timeSlotUnit;
            int latest = departureTime + folderForTW * timeSlotUnit;
            int curRTW = latest - earliest;
            if(Math.abs(curRTW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1) {
                System.out.println("there is an error on line 1660 : now the time window is " + curRTW+" which is not as required "+2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit );
            }

            //********modify above 2024.10.08
            Trip trip3 = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity,
                    departureTime, earliest, latest, nbRandomNbVehiclesInTrip, duration);
            //********************** 2024.10.08 *******************

            instance.addTrip(trip3);
            shift.addTrip(trip3);
            feasibleSolution.addShift(shift);
            sumTrip = sumTrip + 1;
            sumShift = sumShift + 1;


            //******* when it starts a new shift, then Add following code to create more over lap between different shift since here modify startHorizon to make the new shift continue to the preShift******************** 2024.11.18

            ArrayList<Integer> modifyStartHorizonList_1=new ArrayList<>();
            for(int s=0;s<feasibleSolution.getShifts().size();s++){
                Shift shift1=this.feasibleSolution.getShifts().get(s);
                int endTimeOfFormerShift=shift1.getTrips().getLast().getArrivalTime();
                if(endTimeOfFormerShift<latestDepartureTime) {
                    modifyStartHorizonList_1.add(endTimeOfFormerShift);
                }
            }
            if(modifyStartHorizonList_1.size()>0){
                // Generate a random index
                int randomIndex= this.rnd.nextInt(modifyStartHorizonList_1.size());
                System.out.println("5 ize of modifyHorzonList "+modifyStartHorizonList_1.size());
                startHorizon=modifyStartHorizonList_1.get(randomIndex)-minPlanTime;
                latestDepartureTime=startHorizon+minPlanTime;
                System.out.println("5 New shift_"+idOfShift+" modified the start horizon: " + startHorizon);
            }else {
                startHorizon=adjustedStartHorizonForTW;
                latestDepartureTime= adjustLatestDepartureTimeForTW;
                latestDepartureTime = (int) (Math.floor(latestDepartureTime / timeSlotUnit) * timeSlotUnit);
            }
            //**********************************above modify 2024.11.18

            //System.out.println("sum Shift is" + sumShift + "sum trip is" + sumTrip +"total working time is "+workingTime + "total driving time is " + drivingTime);

        } else {
            //----------------------------------P6
            //now there is totally a new shift with 3 trips

            // which guarantee the connection time is feasible for the final depot
            //After preparing work,we start to continue the three trips in the one shift
            //feasibleSolution.addShift(shift);
            //sumShift = sumShift + 1;// this is the difference from the upper situation


            // now we start the first one
            int idOfTrip = sumTrip;
            idOfShift = sumShift;


            //******* when it starts a new shift, then Add following code to create more over lap between different shift since here modify startHorizon to make the new shift continue to the preShift******************** 2024.11.18

            ArrayList<Integer> modifyStartHorizonList=new ArrayList<>();
            for(int s=0;s<feasibleSolution.getShifts().size();s++){
                Shift shift1=this.feasibleSolution.getShifts().get(s);
                int endTimeOfFormerShift=shift1.getTrips().getLast().getArrivalTime();
                if(endTimeOfFormerShift<latestDepartureTime) {
                    modifyStartHorizonList.add(endTimeOfFormerShift);
                }
            }
            if(modifyStartHorizonList.size()>0){
                // Generate a random index
                int randomIndex = this.rnd.nextInt(modifyStartHorizonList.size());
                System.out.println("6 New shift_"+idOfShift+" modified starting horizon list size: " + modifyStartHorizonList.size());
                startHorizon=modifyStartHorizonList.get(randomIndex)-minPlanTime;
                latestDepartureTime=startHorizon+minPlanTime;
                System.out.println("6 New shift_"+idOfShift+" modified the start horizon: " + startHorizon);
            }else {
                startHorizon=adjustedStartHorizonForTW;
                latestDepartureTime= adjustLatestDepartureTimeForTW;
                latestDepartureTime = (int) (Math.floor(latestDepartureTime / timeSlotUnit) * timeSlotUnit);
            }
            //**********************************above modify 2024.11.18


            shift = new Shift(idOfShift);

//            // Following  modify 2-4(4) here is to make the new time limit for the  new shift is random number from (1,4)*24% *total max: 24%-96%
            m = 1 + this.rnd.nextInt(maxFolderOfDiscountTime);
            discountDrivingTime = (int) (m * percentageOfDiscount * this.input.getMaxDrivingTime());
            discountWorkingTime = (int) (m * percentageOfDiscount * this.input.getMaxWorkingTime());

//            // Above modification  2-4 is to make the shift have shorter and longer

            int idOfRandomDepot = this.rnd.nextInt(nbDepots);
            City depot = instance.getCities().get(this.instance.getDepots().get(idOfRandomDepot).getIdOfCityAsDepot());
            //idOfCityAsDepot=depot.getIdOfCity();
            int idOfRandomStartCity = depot.getIdOfCity();// start from a  random Depot

            int idMidCity1 = -1;
            int idMidCity2 = -1;

            for (int i = 0; i < nbCities; i++) {
                int idOfMidCity1 = instance.getCities().get(i).getIdOfCity();
                City midCity1 = instance.getCities().get(idOfMidCity1);
                for (int j = 0; j < nbCities; j++) {
                    int idOfMidCity2 = instance.getCities().get(j).getIdOfCity();
                    City midCity2 = instance.getCities().get(idOfMidCity2);
                    if (idOfMidCity1 != idOfRandomStartCity && idOfMidCity2 != idOfRandomStartCity && idOfMidCity1 != idOfMidCity2) {
                        int duration1 = instance.getDuration(depot, midCity1);//2024.9.25
                        int duration2 = instance.getDuration(midCity1, midCity2);//2024.9.25
                        int duration3 = instance.getDuration(midCity2, depot);//2024.9.25
                        int totalDuration = duration1 + duration2 + duration3;
                        if (2 * minPlanTime + totalDuration < discountWorkingTime && totalDuration < discountDrivingTime) {
                            idMidCity1 = idOfMidCity1;
                            idMidCity2 = idOfMidCity2;
                            break;

                        }
                    }
                }
            }

            // System.out.println("idMid1: "+idMidCity1 +", idMid2: "+idMidCity2);
            if (idMidCity1 >= 0 && idMidCity2 >= 0) {
                int idOfRandomEndCity = idMidCity1;

                //From here, we try to add arc from shift to shift modify 2024.11.15
//                ArrayList<Integer> listEndingTimeShift= new ArrayList<>();
//                for(int s=0;s<feasibleSolution.getShifts().size();s++){
//                   Shift formerShift = this.feasibleSolution.getShifts().get(s);
//                   int nbTripInShift=formerShift.getTrips().size();
//                   int idOfEndingCityOfAShift=formerShift.getTrips().get(nbTripInShift-1).getIdOfEndCity();
//                   int idOfEndingTimeOfShift=formerShift.getTrips().get(nbTripInShift-1).getArrivalTime();
//                   if(idOfEndingCityOfAShift==idOfRandomStartCity&&idOfEndingTimeOfShift-minPlanTime<0.85*(percentageOfDiscount*t_eHorizon) ){
//                       listEndingTimeShift.add(idOfEndingTimeOfShift);
//                   }
//                }
                // above here we try to add arc from shift to shift modify 2024.11.15

                int departureTime = adjustedStartHorizonForTW + this.rnd.nextInt(adjustLatestDepartureTimeForTW - adjustedStartHorizonForTW + 1);//2024.11.26


                //From here, we try to add arc from shift to shift modify 2024.11.15
//                if(listEndingTimeShift.size()>0){
//                  int  idRandomDepartureSameAsFormerShiftEnding=this.rnd.nextInt(listEndingTimeShift.size());
//                      departureTime=listEndingTimeShift.get(idRandomDepartureSameAsFormerShiftEnding)-minPlanTime;
//                }
                // above here we try to add arc from shift to shift modify 2024.11.15

                departureTime = (int) (Math.floor(departureTime / timeSlotUnit) * timeSlotUnit);// ****************2024.9.27 保证 departure time is timeSlot的倍数
                int duration = instance.getDuration(depot, instance.getCities().get(idMidCity1));//2024.9.25
                int arrivalTime = departureTime + duration;
                int nbRandomNbVehiclesInTrip = 1;

                //****************modified below 2024.10.08 ***************************
                int earliestD = departureTime - folderForTW * timeSlotUnit;
                int latestD = departureTime + folderForTW * timeSlotUnit;
                int timeWindow=latestD-earliestD;
                if(Math.abs(timeWindow-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1){
                    System.out.println(" now there is an error on line 1778 because the current time window is "+timeWindow
                            +" which is not as required "+2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
                }
                Trip trip31 = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity, departureTime, earliestD, latestD, nbRandomNbVehiclesInTrip, duration);
                //************ modified above 2024.10.08************************
                this.instance.addTrip(trip31);
                sumTrip = sumTrip + 1;
                shift.addTrip(trip31);
                idOfTrip = sumTrip;

                idOfRandomStartCity = idMidCity1;
                departureTime = arrivalTime + minPlanTime;
                idOfRandomEndCity = idMidCity2;
                duration = instance.getDuration(instance.getCities().get(idMidCity1), instance.getCities().get(idMidCity2));// 2024.9.25

                arrivalTime = departureTime + duration;
                nbRandomNbVehiclesInTrip = 1;

                //*************modify below 2024.10.08***********************************
                int earliestDep = departureTime - folderForTW * timeSlotUnit;
                int latestDep = departureTime + folderForTW * timeSlotUnit;
                int currentTW = latestDep - earliestDep;

                if(Math.abs(currentTW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1) {
                    System.out.println("there is an error ont line 1802, because the current time window " + currentTW+
                            " is not as required "+2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
                }
                //********modify above 2024.9.30
                Trip trip32 = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity, departureTime, earliestDep, latestDep, nbRandomNbVehiclesInTrip, duration);
                //********************2024.9.25*********************************
                this.instance.addTrip(trip32);
                sumTrip = sumTrip + 1;
                shift.addTrip(trip32);

                idOfTrip = sumTrip;

                idOfRandomStartCity = idMidCity2;
                departureTime = arrivalTime + minPlanTime;
                idOfRandomEndCity = depot.getIdOfCity();
                duration = instance.getDuration(instance.getCities().get(idMidCity2), depot);//2024.9.25
                arrivalTime = departureTime + duration;
                nbRandomNbVehiclesInTrip = 1;


                //******************* below **********2024.10.08************************
                int earliestDept = departureTime - folderForTW * timeSlotUnit;
                int latestDept = departureTime + folderForTW * timeSlotUnit;
                int currentTimeW = latestDept - earliestDept;
                if (Math.abs(currentTimeW-2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit)>0.1) {
                    System.out.println("there is an error on line 1873 " +"as the current time window " + currentTimeW+" which is not the same as required "
                            +2*this.input.getFolderOfTimeSlotUnitAsTimeWindow()*timeSlotUnit);
                }

                //********modify above 2024.10.08
                Trip trip33 = new Trip(idOfTrip, idOfRandomStartCity, idOfRandomEndCity, departureTime, earliestDept, latestDept, nbRandomNbVehiclesInTrip, duration);
                //******************* above **********2024.10.08************************

                this.instance.addTrip(trip33);
                sumTrip = sumTrip + 1;

                shift.addTrip(trip33);
                feasibleSolution.addShift(shift);
                idOfTrip = sumTrip;

            } else {
                System.out.println("Last shift start at city" + idOfRandomStartCity + " which couldn't find two midCity make the lase shift " +
                        "be finished int the current maxWorking and Driving Time requirement");
            }

        }
    }


    //here I need to think what should the vehicle class carry, and how to get the nb shift?


    private void createVehicle() {
        int nbVehicles = this.feasibleSolution.getShifts().size() + (int) (this.feasibleSolution.getShifts().size() * 0.0);

        for (int v = 0; v < nbVehicles; v++) {

            Vehicle vehicle = new Vehicle(v, -1, input.getNbTrips());
            this.instance.addVehicle(vehicle);
            //System.out.println("vehicle id, depot, nbTrip: " + vehicle);
        }


    }

    private void createDriver() {
        //here we need guarantee in total we have enough drivers==nbShift; they will choose which depot by model?
//        int nbTrips = input.getNbTrips();
//        for (int i = 1; i <= input.getNbDepots(); i++) {
//            for (int t = 0 * i; t < i * this.input.getNbTrips(); t++) {
//                int idOfStartDepotForDriver = i - 1;
//                int idOfEndDepotForDriver = idOfStartDepotForDriver;
//                Driver driver = new Driver(t, idOfStartDepotForDriver, idOfEndDepotForDriver, nbTrips);
//                this.instance.addDriver(driver);
//            }
//        }
        for (int d = 0; d < feasibleSolution.getShifts().size(); d++) {
            Driver driver = new Driver(d, -1, -1, input.getNbTrips());
            this.instance.addDriver(driver);
            //System.out.println("Driver:  id, depot, nbTrip " + driver);
        }
    }

    private void createArcForTwoFeasibleSuccessiveTrips() {
        for (int t = 0; t < this.input.getNbTrips(); t++) {
            for (int tt = 0; tt < this.input.getNbTrips(); tt++) {
                Trip firstTrip = this.instance.getTrips().get(t);
                Trip secondTrip = this.instance.getTrips().get(tt);

                int idOfFirstTrip = firstTrip.getIdOfTrip();
                int idOfSecondTrip = secondTrip.getIdOfTrip();
                int idOfEndCityOfFirstTrip = firstTrip.getIdOfEndCity();
                int idOfStartCityOfSecondTrip = secondTrip.getIdOfStartCity();
                int idOfEndCityOfSecondTrip = secondTrip.getIdOfEndCity();

                int arrivalTimeOfFirstTrip = firstTrip.getArrivalTime();

                int departureTimeOfSecondTrip = secondTrip.getDepartureTime();

                int connectionTime = departureTimeOfSecondTrip - arrivalTimeOfFirstTrip;
//                int maxStayDurationLimitForVehicle = (int) (input.getCostUseVehicle() / input.getCostOfIdlePerUnitForVehicle());
                if ((idOfEndCityOfFirstTrip == idOfStartCityOfSecondTrip) && (connectionTime >= this.input.getMinPlanTime())
                        && (connectionTime <= this.input.getMaxPlanTime())) {
                    int costForDriver = (int) (connectionTime * input.getCostOfIdlePerUnitForDriver());
                    //costForDriver = Math.round(100.0 * costForDriver) / 100.0;


                    /////*****here i modify about the vehicle cost
                    int costForVehicle = Integer.MAX_VALUE;
                    boolean whetherSecondTripEndCityIsADepot = false;

                    for (int k = 0; k < this.instance.getInput().getNbDepots(); k++) {
                        int idOfCityAsDepot = instance.getDepots().get(k).getIdOfCityAsDepot();
                        if (idOfEndCityOfSecondTrip == idOfCityAsDepot) {
                            whetherSecondTripEndCityIsADepot = true;
                        }
                    }

                    if (whetherSecondTripEndCityIsADepot == true && (connectionTime >= this.input.getMaxChargingTimeForVehicleInDepot())) {
                        costForVehicle = (int) (this.input.getMaxChargingTimeForVehicleInDepot() * input.getCostOfIdlePerUnitForVehicle());
                    } else {
                        costForVehicle = (int) (connectionTime * input.getCostOfIdlePerUnitForVehicle());
                    }

                    //*****until here
                    //costForVehicle = Math.round(100.0 * costForVehicle) / 100.0;
                    ArcForTwoFeasibleSuccessiveTrip arcForTwoFeasibleSuccessiveTrip = new ArcForTwoFeasibleSuccessiveTrip(idOfFirstTrip, idOfSecondTrip, costForVehicle,
                            costForDriver);
                    this.instance.addArcForTripsFeasible(arcForTwoFeasibleSuccessiveTrip);
                }

            }
        }
    }


    public Instance getInstanceFromRandomCreate() { //这个是最枢纽步骤链接这个解决方法到最后的结果中去
        /**
         * Question 3: how can I put the city and depot and trip into Instance?-====through create city method
         * 因为里面已经有将新创建的City根据循环不停的放进Instance里了
         */
        this.createCityAndDepotBothByHand();
        // this.createSomeCityWithRandomCoordinateAndSomeDepotCoordinateGivenByHand();
        // this.createDesignedDepot();
        //this.createRandomDepot();// how to make sure each depot has at least one vehicle? 怎么能保证每个车站至少一个车呢？

        this.createRandomTrip(); // here need to rethink about how to make Start City and End City are not the same!
//        this.createArcForTwoConflictTripsBySameDriver();
        this.createVehicle(); //how to make sure the vehicle has different depot？每个地方都有始发的车呢？
        this.createDriver();// how to make sure that each depot also exactly have vehicle恰好也是有车的呢？
        this.createArcForTwoFeasibleSuccessiveTrips();//Should we give the number of trips? 有多少不应该给定的? 考虑从input中把它删除
        return this.instance;
    }

    /**
     * Here should I add some constraints to judge whether the instance is good? for example the depot or the trip?
     */
    //here I need to define  how to print the instance in a file, thus we can check the results in file
    public void printInstanceInfile(String fileName) {
        int nbVehicles = this.feasibleSolution.getShifts().size() + (int) (this.feasibleSolution.getShifts().size() * 0.0);
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName);
            writer.println("// The instance basic information \n" + "nbCities:" + this.input.getNbCities() + "; nbDepots:" + this.input.getNbDepots()
                    + "; size:" + this.input.getDistanceX() + " " + this.input.getDistanceY() + ";"
                    + "nbTrips:" + this.input.getNbTrips() + ";"
                    + "nbCombinedTrips" + this.sumCombinedTrip + ";"
                    + "maxFolderTimeSlotAsTimeWindow" + this.input.getFolderOfTimeSlotUnitAsTimeWindow() + ";"
                    + "planningHorizon:" + this.input.getStartHorizon() + "-" + this.input.getEndHorizon() + ";"
                    + "maxDriving:" + this.input.getMaxDrivingTime() + ";"
                    + "maxWorking:" + this.input.getMaxWorkingTime() + "\n"
                    + "// The number of cities is \n" + this.input.getNbCities() + "\n"
                    + "// The number of depots is \n" + this.input.getNbDepots() + "\n"
                    + "// The max number of vehicles is \n" + nbVehicles + "\n"
                    + "// The max number of drivers is \n" + this.feasibleSolution.getShifts().size() + "\n"
                    + "// The number of trips is \n" + this.input.getNbTrips() + "\n"
                    + "// The minPlanTime is \n" + this.input.getMinPlanTime() + "\n"
                    + "// The short Time for driver is \n" + this.input.getShortTimeForDriver() + "\n"
                    + "// The short Time for vehicle is \n" + this.input.getShortTimeForVehicle() + "\n"
                    + "// The maximum driving time is \n" + this.input.getMaxDrivingTime() + "\n"
                    + "// The maximum working time is \n" + this.input.getMaxWorkingTime() + "\n"
                    + "// The cost of using driver is \n" + this.input.getCostUseDriver() + "\n"
                    + "// The cost of using vehicle is \n" + this.input.getCostUseVehicle() + "\n"
                    + "// The idle time cost for driver per unit is \n" + this.input.getCostOfIdlePerUnitForDriver() + "\n"
                    + "// The idle time cost for vehicle per unit is \n" + this.input.getCostOfIdlePerUnitForVehicle() + "\n"
                    + "// The cost of driver changeover penalty per change is \n" + this.input.getCostOfChangeOverPerChange() + "\n"
                    + "// The max percentage of combined trips is  \n" + this.input.getMaxPercentageCombinedTrip() + "\n"
                    + "//The maxFolderOfTimeSlotAsTimeWindow is \n" + this.input.getFolderOfTimeSlotUnitAsTimeWindow() + "\n"
                    + "// The maxPlanTime is \n" + this.input.getMaxPlanTime() + "\n"
                    + "//The time slot Unit is \n" + this.input.getTimeSlotUnit() + "\n"
                    + "//The scale of distance and duration is \n" + this.input.getScale_DisAndDur() + "\n"
                    + "//The start time of planning horizon" + "\n" + this.input.getStartHorizon() + "\n"
                    + "//The ending time of planning horizon" + "\n" + this.input.getEndHorizon() + "\n"

            );

            writer.println("//Here are the cities: idOfCity \t nameOfCity \t coordinateX \t coordinateY ");
            for (City city : this.instance.getCities()) {
                writer.println(city);
            }

            writer.println("//Depots:  \n// idOfDepot  \t indexOfDepotAsStartingPoint \t indexOfDepotAsEndingPoint \t idCityAsDepot");
            for (Depot depot : this.instance.getDepots()) {
                writer.println(depot);
            }

//            writer.println("//nbDriverAvailable ");
//            writer.println(this.feasibleSolution.getShifts().size());

            writer.println("//Trips: idOfTrip \t idOfStartCity \t idEndCity"
                    + "\t earliestDepartureTime \t latestDepartureTime \t nbCombinedVehicleInThisTrip \t duration");
            for (Trip trip : this.instance.getTrips()) {
                writer.println(trip);
            }

            writer.println("//Vehicles: idOfVehicle idOfStartingDepot nbTrips");
            for (Vehicle vehicle : this.instance.getVehicles()) {
                writer.println(vehicle);
            }

            writer.println("//Drivers :idOfDriver idOfStartingDepot idOfEndingDepot nbTrips");
            for (Driver driver : this.instance.getDrivers()) {
                writer.println(driver);
            }

//            writer.println("//Graph arcs for trips can be performed successively: \n// idStartTrip idEndTrip costForVehicle costForDriver");
//            for (ArcForTwoFeasibleSuccessiveTrip arcForTwoFeasibleSuccessiveTrips : this.instance.getArcForTwoSuccessiveTrips()) {
//                writer.println(arcForTwoFeasibleSuccessiveTrips);
//            }

//            writer.println("//Graph arcs for trips cannot be performed by same driver: \n// idStartTrip idEndTrip");
//            writer.println(this.input.getNbTrips());
//            for (ArcForTwoConflictTripsBySameDriver arcForTwoConflictTripsBySameDriver : this.instance.getArcForConflictTrips()) {
//                writer.println(arcForTwoConflictTripsBySameDriver);
//            }

            writer.close();
        } catch (IOException exception) {
            System.out.println("Write file error happened");
            System.out.println(exception);
        }
    }

    public void printFeasibleSolutionInfile(String feasibleSolutionFileName) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(feasibleSolutionFileName);

            int totalCost = 0;
            int costForUseResource = (int) (this.feasibleSolution.getShifts().size() * (input.getCostUseVehicle() + input.getCostUseDriver()));
            int costForWaitingBetweenTrips = 0;
            int costForChangeOver = 0;


            //**************since here is for the warm-start of integrated model********************
//            writer.println("//Here is the changeover for driver idOfDriver " + "idOfShift" + " " + "idOfFirst(or idOfSecond)Trip");
//            double totalIdleTime = 0;
//
//            for (int s = 0; s < this.feasibleSolution.getShifts().size(); s++) {
//                Shift shift = this.feasibleSolution.getShifts().get(s);
//                int totalIdleTimeInShift = 0;
//                //System.out.println("shift" + shift);
//                for (int t = 0; t < shift.getTrips().size() - 1; t++) {
//                    int tt = t + 1;
//                    Trip trip1 = shift.getTrips().get(t);
//                    int idOfTrip1 = trip1.getIdOfTrip();
//                    //System.out.println(idOfTrip1);
//                    Trip trip2 = shift.getTrips().get(tt);
//                    int idOfTrip2 = trip2.getIdOfTrip();
//                    //System.out.println(idOfTrip2);
//                    for (int a = 0; a < instance.getArcForTwoSuccessiveTrips().size(); a++) {
//                        ArcForTwoFeasibleSuccessiveTrip arcForTwoFeasibleSuccessiveTrip = instance.getArcForTwoSuccessiveTrips().get(a);
//                        if (arcForTwoFeasibleSuccessiveTrip.getIdOfFirstTrip() == idOfTrip1 && arcForTwoFeasibleSuccessiveTrip.getIdOfSecondTrip() == idOfTrip2) {
//                            costForWaitingBetweenTrips = (int) (costForWaitingBetweenTrips + arcForTwoFeasibleSuccessiveTrip.getCostForVehicle() + arcForTwoFeasibleSuccessiveTrip.getCostForDriver());
//                            totalIdleTimeInShift = totalIdleTimeInShift + trip2.getDepartureTime() - trip1.getArrivalTime();
//                        }
//                    }
//                    if (trip1.getIdOfTrip() + 1 != trip2.getIdOfTrip()) {
//                        // System.out.println("Now costForChangOver");
//                        for (int a = 0; a < instance.getArcForTwoSuccessiveTrips().size(); a++) {
//                            ArcForTwoFeasibleSuccessiveTrip arcForTwoFeasibleSuccessiveTrip = instance.getArcForTwoSuccessiveTrips().get(a);
//                            if (arcForTwoFeasibleSuccessiveTrip.getIdOfFirstTrip() == idOfTrip1 && arcForTwoFeasibleSuccessiveTrip.getIdOfSecondTrip() == idOfTrip2) {
//                                costForChangeOver = (int) (costForChangeOver + input.getCostOfChangeOverPerChange());
//                            }
//                        }
//                        //System.out.println("changeOver:" + shift.getIdOfShift() + " " + trip1.getIdOfTrip() + " " + trip2.getIdOfTrip());
//
//                        // writer.println("changeover: " + shift.getIdOfShift() + " " + trip1.getIdOfTrip() + " " + trip2.getIdOfTrip());
//                        if (trip2.getNbCombinedVehicle() == 2) {
//                            //writer.println("passengerAndNonLeading:" + " " + shift.getIdOfShift() + " " + trip2.getIdOfTrip());
//                            //System.out.println("idOfDriver_"+shift.getIdOfShift()+"go from trip_"+trip1.getIdOfTrip()+" combine with trip_"+trip2.getIdOfTrip());
//                        }
//                        if (trip1.getNbCombinedVehicle() == 2) {
//                            //writer.println("passengerAndNonLeading:" + " " + shift.getIdOfShift() + " " + trip1.getIdOfTrip());
//                            //System.out.println("idOfDriver_"+shift.getIdOfShift()+"leave the combined trip_"+trip1.getIdOfTrip()+" to the next trip_"+trip2.getIdOfTrip());
//                        }
//
//                    }
//
//                }
//                totalIdleTime = totalIdleTime + totalIdleTimeInShift;
//            }
//            double totalIdleTimePerDriver = totalIdleTime / this.feasibleSolution.getShifts().size();
//            totalCost = costForUseResource + costForWaitingBetweenTrips + costForChangeOver;
//            writer.println("//Here are the total idle time per driver: " + totalIdleTimePerDriver);
//            writer.println("//Here are the cost ");
//            writer.println("The total cost is " + totalCost);
//            writer.println("The cost for idle time is " + costForWaitingBetweenTrips);
//            writer.println("The cost for driver ChangOver is " + costForChangeOver);
//            System.out.println("costForChangOver : " + costForChangeOver);
//            writer.println("//One feasible solution vehicle (Driver): idVehicle(idDriver)  idOfDepot  indexOfDepotAsStartingPoint  idOfTrip startTime indexOfDepotAsEndingPoint");
//            //System.out.println("totalCost is : " + totalCost);
//
//            for (int v = 0; v < feasibleSolution.getShifts().size(); v++) {
//                Shift shift = feasibleSolution.getShifts().get(v);
//                //writer.print("FirstDepartureTime"+(shift.getTrips().getFirst().getDepartureTime())+" ");
//                // writer.print("LastArrivalTime"+shift.getTrips().getLast().getArrivalTime()+" ");
//                int vehicleStartCityId = Integer.MAX_VALUE;
//                int vehicleEndCityId = Integer.MAX_VALUE;
//                int vehicleStartDepotIndex = Integer.MAX_VALUE;
//                int vehicleEndDepotIndex = Integer.MAX_VALUE;
//                writer.print("vehicle " + v + " ");
//
//                //below this is part for the first trip to the depot index
//                Trip firstTrip = shift.getTrips().getFirst();
//                vehicleStartCityId = firstTrip.getIdOfStartCity();
//                for (int k = 0; k < input.getNbDepots(); k++) {
//                    Depot depot = instance.getDepots().get(k);
//                    if (depot.getIdOfCityAsDepot() == vehicleStartCityId) {
//                        vehicleStartDepotIndex = depot.getIndexOfDepotAsStarting();
//                        //System.out.println(startIndex);
//                        int depotIdForVehicle = depot.getIdOfDepot();
//                        writer.print(depotIdForVehicle + " "); // here is the depot id
//                        writer.print(vehicleStartDepotIndex + " ");
//                        break;
//                    }
//                }
//                //above this is part for the first trip to the depot index
//                for (int vi = 0; vi < shift.getTrips().size(); vi++) {
//                    Trip trip = shift.getTrips().get(vi);
//                    writer.print(trip.getIdOfTrip() + " ");
//                    writer.print(trip.getDepartureTime() + " ");
//                }
//
//                //below this is part for the last trip to the depot index
//                Trip lastTrip = shift.getTrips().getLast();
//                vehicleEndCityId = lastTrip.getIdOfEndCity();
//                for (int k = 0; k < input.getNbDepots(); k++) {
//                    Depot depot = instance.getDepots().get(k);
//                    if (depot.getIdOfCityAsDepot() == vehicleEndCityId) {
//                        vehicleEndDepotIndex = depot.getIndexOfDepotAsEnding();
//                    }
//                }
//                writer.println(vehicleEndDepotIndex);
//                //above this is part for the last trip to the depot index
//
//            }
//
//            int totalDurationTrip=0;
//            int minNbDriver=Integer.MAX_VALUE;
//
//            for (int d = 0; d < feasibleSolution.getShifts().size(); d++) {
//                Shift shift = feasibleSolution.getShifts().get(d);
//                int totalWorkingTime = shift.getTotalWorkingTime();
//                int totalMaxDrivingTimeEstimate = 0;
//
//                int driverStartCityId = Integer.MAX_VALUE;
//                int driverEndCityId = Integer.MAX_VALUE;
//                int driverStartDepotIndex = Integer.MAX_VALUE;
//                int driverEndDepotIndex = Integer.MAX_VALUE;
//
//                writer.print("driver " + d + " ");
//                //below this is part for the first trip to the depot index
//                Trip firstTrip = shift.getTrips().getFirst();
////                int firstStaringTime=shift.getDepartureTimes().getFirst();
//                driverStartCityId = firstTrip.getIdOfStartCity();
//                for (int k = 0; k < input.getNbDepots(); k++) {
//                    Depot depot = instance.getDepots().get(k);
//                    if (depot.getIdOfCityAsDepot() == driverStartCityId) {
//                        driverStartDepotIndex = depot.getIndexOfDepotAsStarting();
//                        int depotIdForDriver = depot.getIdOfDepot();
//                        writer.print(depotIdForDriver + " "); // here is the depot id
//                        writer.print(driverStartDepotIndex + " ");// here is the depot index in the real solve
//                        break;
//                    }
//                }
//                //above this is part for the first trip to the depot index
//                for (int di = 0; di < shift.getTrips().size(); di++) {
//                    Trip trip = shift.getTrips().get(di);
//                    writer.print(trip.getIdOfTrip() + " ");//this for print the trip
//                    //add 2024.10.13 decret by unit
////                    int depTByUnit=trip.getDepartureTime()/input.getTimeSlotUnit();
////                    writer.print( depTByUnit+ " ");
//                    writer.print(trip.getDepartureTime() + " ");
//
//                    if (di < shift.getTrips().size() - 1) {
//                        Trip secondTrip = shift.getTrips().get(di + 1);
//                        int dep_Second = secondTrip.getDepartureTime();
//                        int connection = dep_Second - trip.getDepartureTime() - trip.getDuration();
//                        if (connection < this.input.getMinPlanTime()) {
//                            System.out.println("check connection has error" + connection);
//                        } else if (connection > this.input.getMaxWorkingTime()) {
//                            System.out.println("check connection has error" + connection);
//                        }
//                    }
//
//
//                    //below this is part for calculate the driving duration
//                    City city1 = instance.getCities().get(trip.getIdOfStartCity());
//                    City city2 = instance.getCities().get(trip.getIdOfEndCity());
//                    totalMaxDrivingTimeEstimate = totalMaxDrivingTimeEstimate + instance.getDuration(city1, city2);//2024.9.25
//                    //above this is the part for calculate the driving duration
//                }
//
//                //below this is part for the last trip to the depot index
//                Trip lastTrip = shift.getTrips().getLast();
//                driverEndCityId = lastTrip.getIdOfEndCity();
//                for (int k = 0; k < input.getNbDepots(); k++) {
//                    Depot depot = instance.getDepots().get(k);
//                    if (depot.getIdOfCityAsDepot() == driverEndCityId) {
//                        driverEndDepotIndex = depot.getIndexOfDepotAsEnding();
//                    }
//                }
//                writer.println(driverEndDepotIndex);
//                //above this is part for the last trip to the depot index
//                // writer.println("startIndex:" + driverStartDepotIndex + " endIndex:" + driverEndDepotIndex);
//                //writer.println("driverStartCityId:"+driverStartCityId+"\t"+"endCityId:"+driverEndCityId);
//
//                // define as attributes
//                double percentageOfDiscount = this.input.getKeyPercentTimeBackToDepot();//24%
//                // System.out.println(percentageOfDiscount);
//
//
//                int discountWorkingTime = (int) (percentageOfDiscount * this.input.getMaxWorkingTime());
////                System.out.println("check the discountWorking time"+discountWorkingTime);
//                int discountDrivingTime = (int) (percentageOfDiscount * this.input.getMaxDrivingTime());
////                System.out.println("check the discoutDriving time"+discountDrivingTime);
//                boolean whetherWorkingTimeSatisfied = (totalWorkingTime <= discountWorkingTime);
//                boolean whetherDrivingTimeSatisfied = (totalMaxDrivingTimeEstimate <= discountDrivingTime);
//                int minNbDriverConsideringTotalDrivingTime=(int) Math.ceil((double) totalMaxDrivingTimeEstimate / this.input.getMaxDrivingTime());
//                int minNbDriverConsideringTotalWorkingTime=(int) Math.ceil((double) totalWorkingTime / this.input.getMaxWorkingTime());
//                if(minNbDriverConsideringTotalDrivingTime>minNbDriverConsideringTotalWorkingTime){
//                    minNbDriver=minNbDriverConsideringTotalDrivingTime;
//                }else {
//                    minNbDriver=minNbDriverConsideringTotalWorkingTime;
//                }
////                System.out.println("minNbDrivers to finish all the trips is:" +minNbDriver);
//                System.out.println("totalWorkingTimeForThisShift: " + totalWorkingTime + " " + whetherWorkingTimeSatisfied);
//                System.out.println("totalDrivingTimeMustLessThan:" + totalMaxDrivingTimeEstimate + " " + whetherDrivingTimeSatisfied);
////                writer.println();
//
//            }

            //***************** above there is the warm start for the integrated model ************
//
            // now the following is to write the feasible solution for the decomposed
            writer.println("//Here for varDriving  idOfDriver idOfDepot indexDepotAsStartPoint [idOfTrip whetherDrive startingTime]  indexDepotAsEndPoint");
            for (int dd = 0; dd < feasibleSolution.getShifts().size(); dd++) {
                Shift shift = feasibleSolution.getShifts().get(dd);
                writer.print("driver " + shift.getIdOfShift() + " ");
                //below this is part for the first trip to the depot index
                int driverStartDepotIndex = Integer.MAX_VALUE;
                Trip firstTrip = shift.getTrips().getFirst();
                int driverStartCityId = firstTrip.getIdOfStartCity();
                for (int k = 0; k < input.getNbDepots(); k++) {
                    Depot depot = instance.getDepots().get(k);
                    if (depot.getIdOfCityAsDepot() == driverStartCityId) {
                        driverStartDepotIndex = depot.getIndexOfDepotAsStarting();
                        int depotIdForDriver = depot.getIdOfDepot();
                        writer.print(depotIdForDriver + " "); // here is the depot id
                        writer.print(driverStartDepotIndex + " ");// here is the depot index in the real solve
                        break;
                    }
                }
                //above this is part for the first trip to the depot index

                for (int i = 0; i < shift.getTrips().size(); i++) {
                    Trip trip = shift.getTrips().get(i);
                    if (trip.getNbCombinedVehicle() == 1) {
                        writer.print(trip.getIdOfTrip() + " ");
                        writer.print("true "+ trip.getDepartureTime()+" ");
                    }
                    if (trip.getNbCombinedVehicle() == 2) {
                        int idTrip = trip.getIdOfTrip();
                        boolean whetherShowUpBefore = false;
                        for (int idFormShift = 0; idFormShift < dd; idFormShift++) {
                            Shift shiftFormer = feasibleSolution.getShifts().get(idFormShift);
                            if (shiftFormer.whetherTripPresentInTheShift(idTrip)) {
                                whetherShowUpBefore = true;
                                break;
                            }
                        }
                        if (!whetherShowUpBefore) {
                            writer.print(idTrip + " true ");
                        } else {
                            writer.print(idTrip + " false ");
                        }
                        writer.print(trip.getDepartureTime()+" ");
                    }

                }
                Trip lastTrip = shift.getTrips().getLast();
                int driverEndCityId = lastTrip.getIdOfEndCity();
                int driverEndDepotIndex = Integer.MAX_VALUE;
                for (int k = 0; k < input.getNbDepots(); k++) {
                    Depot depot = instance.getDepots().get(k);
                    if (depot.getIdOfCityAsDepot() == driverEndCityId) {
                        driverEndDepotIndex = depot.getIndexOfDepotAsEnding();
                    }
                }
                writer.println(driverEndDepotIndex);

            }

            //***************** until now there is the feasible solution for the decomposed model ************

            //here is just to check the connection time
            for (int s = 0; s < this.feasibleSolution.getShifts().size(); s++) {
                Shift shift = this.feasibleSolution.getShifts().get(s);
                for (int t = 0; t < shift.getTrips().size() - 1; t++) {
                    Trip trip1 = shift.getTrips().get(t);
                    Trip trip2 = shift.getTrips().get(t + 1);
                    int connectionTime = trip2.getDepartureTime() - trip1.getArrivalTime();
                    if (connectionTime < input.getMinPlanTime()) {
                        System.out.println("the connection time has problem" + connectionTime);
                    }
                }
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
        for (City city : this.instance.getCities()) {
            s += "\n\t " + city;
        }
        s += "\n" + "Here are the depots: ";

        for (Depot depot : this.instance.getDepots()) {
            s += "\n\t " + depot;
        }

        s += "\n" + "Here are the trips:";
        for (Trip trip : this.instance.getTrips()) {

            s += "\n\t" + trip;
        }

        s += "\n" + "Here are the vehicle start trips:";
        for (VehicleStartTrips vehicleStartTrips1 : this.instance.getVehicleStartTrips()) {

            s += "\n\t" + vehicleStartTrips1;
        }

        s += "\n" + "Here are the vehicle end trips:";
        for (VehicleEndTrips vehicleEndTrips1 : this.instance.getVehicleEndTrips()) {

            s += "\n\t" + vehicleEndTrips1;
        }

        s += "\n" + "Here are the driver start trips:";
        for (DriverStartTrips driverStartTrips1 : this.instance.getDriverStartTrips()) {
            s += "\n\t" + driverStartTrips1;
        }

        s += "\n" + "Here are the driver end trips:";
        for (DriverEndTrips driverEndTrips1 : this.instance.getDriverEndTrips()) {
            s += "\n\t" + driverEndTrips1;
        }

        return s;
    }


    public static void main(String[] args) {
        InputReader reader = new InputReader("5city200TripExample.txt");
        Input input = reader.readfile();
        System.out.println(input);
        RandomInstanceCreate randomInstanceCreate = new RandomInstanceCreate(input);

        Instance instance = randomInstanceCreate.getInstanceFromRandomCreate();

        randomInstanceCreate.printInstanceInfile("Instance Generate.txt");
        randomInstanceCreate.printFeasibleSolutionInfile("feasibleSolution.txt");


    }


}
