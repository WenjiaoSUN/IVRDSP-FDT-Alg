package Solver;

import PathsForDriver.DriverSchedule;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.Depot;
import Instance.Driver;
import Solution.Solution;
import PathsForDriver.Schedules;
import PathsForDriver.SchedulesReader;
import Solution.Node;
import Solution.Route;
import Solution.Routes;
import Solution.TripWithStartingInfos;
import Solution.PathForVehicle;
import Solution.PathForDriver;
import Solution.TripWithDriveInfos;

import java.io.*;

public class SolverTwoIndexBasedOnDriverScheduleWithNoChangeOver {
    private Instance instance;
    private IloCplex cplex;

    //-------------------------------------------------------
    private IloNumVar[] varVehicleSourceDepotArc;//change 2025.1.19

    private IloNumVar[] varVehicleDepotSinkArc;//change 2025.1.19

    private IloNumVar[][] varDriverSourceDepotArc;

    private IloNumVar[][] varDriverDepotSinkArc;
    //-------------------------------------------------------
    private IloNumVar[][] varVehicleArc; //here is the vehicle variable//change 2025.1.19
    private IloNumVar[][][] varDriverArc; //here is the variable x d i j


    // new variables to deal with the time window
    private IloNumVar[][] varWhetherDriverShortArc; //b_ij^DS
    private IloNumVar[][] varWhetherVehicleShortArc;//b_ij^VS
    private IloNumVar[] varStartingTimeOfDriverSchedule;//s^d
    private IloNumVar[] varEndingTimeOfDriverSchedule;//e^d

    // Here are the new variables to deal with the time window 2024.9.18
    private IloNumVar[] varTripStartTime;//t_i
    private IloNumVar[][] varVehicleWaitingTime;

    private IloNumVar[][] varVehicleWaitingTimeForNotAllComb;//t^V_ij

    private IloNumVar[][] varOneVehicleWaitingTimeForAllComb;//t^V1_ij

    private IloNumVar[][] varTwoVehicleWaitingTimeForAllComb;//t^V2_ij

    private IloNumVar[][] varWhetherOneVehiclePerformArcForAllComb;//u_ij^{V1}
    private IloNumVar[][] varWhetherTwoVehiclePerformArcForAllComb;//u_ij^{V2}

    //here is the vehicle variable//change 2025.1.19

    //t_ij 2025.1.17 new (varVehicleWaitingTimeForNotAllComb;//t^V_ijm varVehicleWaitingTimeForAllComb;//t^v_ij)

    private IloNumVar[][][] varDriverWaitingTime;//t^d_ij

    // above are the new variables to deal with the time window 2024.9.18
    private int nbVehicle;
    private int nbDriver;
    private int nbTrip;
    private int nbNodes;  // what is this nbNodes For? give all the vehicle a starting and ending node.....if not use this, what would happen?
    private int nbDepot;
    private double timeInMilliSec;
    private double lowerBound;
    private double upperBound;
    private String warmStartFilePath;
    //warmStartFilePath;
    private boolean whetherSolveRelaxationProblem;

    // Here are the new attributes to deal with the time window 2024.9.12
    private int timeUnitSlot;
    private int startHorizon;
    private int endHorizon;

    //    private int nbMaxWaitingUnits;
//
    private int minPlanTime;
    // Above are the attributes added to  deal with the time window 2024.9.12


    private int M;

    private Schedules schedules;
    private int nbDriverValidInSchedules;

    public SolverTwoIndexBasedOnDriverScheduleWithNoChangeOver(Instance instance, Schedules schedules) throws IloException {
        this.instance = instance;
        this.schedules = schedules;//Z2025.1.15
        this.nbVehicle = instance.getMaxNbVehicleAvailable();
        this.nbDriver = instance.getMaxNbDriverAvailable();
        this.nbDriverValidInSchedules = this.schedules.getDriverSchedules().size();
        // change to schedules size 2025.1.15
        this.nbTrip = instance.getNbTrips();
        this.nbDepot = instance.getNbDepots();
        this.nbNodes = nbTrip + 2 * nbDepot;  //this is to avoid the path become a circle
        this.warmStartFilePath = null;
        this.whetherSolveRelaxationProblem = false;
        // here are the attributes to deal with the time window
        this.timeUnitSlot = instance.getTimeSlotUnit();
        this.startHorizon = instance.getStartingPlanningHorizon();
        this.endHorizon = instance.getEndingPlaningHorizon();
        this.minPlanTime = instance.getMinPlanTurnTime();
        this.M = instance.getEndingPlaningHorizon();
    }

    public SolverTwoIndexBasedOnDriverScheduleWithNoChangeOver(Instance instance, Schedules schedules, Boolean whetherSolveRelaxationProblem) throws IloException {
        this(instance, schedules);
        this.whetherSolveRelaxationProblem = whetherSolveRelaxationProblem;
    }


    public Solution solveWithCplexBasedOnGivenSchedules() {
        try {
            cplex = new IloCplex();
            //cplex.setParam(IloCplex.Param.MIP.Strategy.File, 2);//give a warm start value from an outside file
            defineDecisionVariables();
            defineObjectiveFunction();

            // for vehicle
            cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotSinkArc(); //（3）****
            cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotTripArc(); //（4）
            cnstRelationshipOfVehicleDepotSinkArcAndVehicleDepotTripArc(); //（5）*****
            cnstVehicleFlowConstraintForTrip();//(6) Vehicle flow constraint for trip
            cnstnbVehicleInTrip(); //constraint (7)

            //=============================================================================2025.1.16 Formulation Extra Constraints TW cause====================
            cnstStartingTimeRange1();//(33)-1
            cnstStartingTimeRange2();//(33)-1

            cnstDetectDriverShortConnectionArc();//(42)
            cnstStartingTimeOfSchedule();//(44)
            cnstEndingTimeOfSchedule();//(45)
            cnstWorkingTimeLimitation();//(46)

            cnstDriverWaitingTime_M1();//(38)-1
            cnstDriverWaitingTime_M2();//(38)-2
            cnstDriverWaitingTime_1();//(39)-1
            cnstDriverWaitingTime_2();//(39)-2


            cnstVehicleWaitingTimeForNotAllCombinedArc_M1();//(34)-1//
            cnstVehicleWaitingTimeForNotAllCombinedArc_M2();//(34)-2//

            cnstOneVehicleWaitingTimeForAllCombinedArc_M1();//(36)-1// change 205.1.22
            cnstOneVehicleWaitingTimeForAllCombinedArc_M2();//(36)-2// change 205.1.22
//
            cnstTwoVehicleWaitingTimeForAllCombinedArc_M1();//new(36)-1// change 205.1.22
            cnstTwoVehicleWaitingTimeForAllCombinedArc_M2();//new(36)-1// change 205.1.22
//
            cnstVehicleWaitingTimeForNotAllCombinedArc_1();//(35)-1//
            cnstVehicleWaitingTimeForNotAllCombinedArc_2();//(35)-2//

            cnstOneVehicleWaitingTimeForAllCombinedArc_1();//(37)-1 // change 205.1.22
            cnstOneVehicleWaitingTimeForAllCombinedArc_2();//(37)-2// change 205.1.22

            cnstTwoVehicleWaitingTimeForAllCombinedArc_1();// new(37)-1// change 205.1.22
            cnstTwoVehicleWaitingTimeForAllCombinedArc_2();// new(37)-2// change 205.1.22

//            cnstVehicleWaitingTimeForArc_M1();//(new)-1 //wrong forget there maybe only one arc perform combine to combine
//            cnstVehicleWaitingTimeForArc_M2();//(new)-2//wrong forget there maybe only one arc perform combine to combine
            //cnstVehicleWaitingTimeForArc_1();//(new)-1 2025.1.20//wrong forget there maybe only one arc perform combine to combine
//           //cnstVehicleWaitingTimeForArc_2();//(new)-22025.1.20//wrong forget there maybe only one arc perform combine to combine

            cnstDetectOneVehicleShortConnectionArcForAllCombined();//(40)//change 2025.1.20// change 205.1.22
            cnstDetectTwoVehicleShortConnectionArcForAllCombined();//(40)//change 2025.1.20// change 205.1.22

            cnstKeepSameVehiclesDuringShortConnectionInCombinedArc();//(41)// change2025.1.20
            cnstProhibitChangeVehicleDuringDriverShortConnection();//(43)// change 2025.1.20

            // extra constraints for the new variables
            cnstLinkVehicleArcAndWhetherOneOrTwoPerfomArc();


            // 5. export the model
            cplex.exportModel("modelOfRoutingAndScheduling.lp");

            cplex.setParam(IloCplex.Param.TimeLimit, 3600);// limit the time is two hour
            cplex.setParam(IloCplex.Param.Threads, 1);
            //here parameter for gap is only for the small instance which gap is 0.01 but in a very short time
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1e-06);
            //cplex.setParam(IloCplex.Param.Parallel, 1);
            //cplex.setParam(IloCplex.Param.MIP.Strategy.PresolveNode,0);//Node presolve strategy.
            // Set the HeurFreq parameter to -1 to turn off the node heuristic
            // cplex.setParam(IloCplex.Param.MIP.Strategy.HeuristicFreq, -1);
            // cplex.setParam(IloCplex.Param.MIP.Strategy.VariableSelect, 4);//try a less expensive variable selection strategy pseudo reduced costs.
            // cplex.setParam(IloCplex.Param.Emphasis.MIP, 1);//this emphasize feasibility over optimality
            // ----------------------------------------------here is for the warm start
            // Create a warm start according to a solution file
            if (this.warmStartFilePath != null && this.whetherSolveRelaxationProblem == false) {
//                addWarmStartBeforeSolve(warmStartFilePath);
            }
            // ----------------------------------------------here is after the warm start

            int numVars = cplex.getNcols();  // Total variables
            int numBinaryVars = cplex.getNbinVars();  // Binary variables
            int numIntegerVars = cplex.getNintVars();  // Integer variables (excluding binary)
            int numContinuousVars = numVars - numBinaryVars - numIntegerVars;  // Continuous variables
            int numConstraints = cplex.getNrows(); // Count constraints before solving

            System.out.println("nbTotal variables " + numVars);
            System.out.println("nbBinaryVars " + numBinaryVars);
            System.out.println("nbIntegerVars " + numIntegerVars);
            System.out.println("nbConstraints " + numConstraints);
            System.out.println("nbNodes " + instance.getNbNodes());
            System.out.println("nbPossibleArcs " + instance.getMaxNbPossibleArc());

            // 6. solve
            long startTime = System.currentTimeMillis();
            if (cplex.solve()) {
                this.timeInMilliSec = (int) (System.currentTimeMillis() - startTime);
                this.lowerBound = this.cplex.getBestObjValue();  //getBestObjValue will give the low bound
                this.upperBound = this.cplex.getObjValue();
                System.out.println("problem solved !");
                if (this.whetherSolveRelaxationProblem == false) {
                    printSolutionAfterSolve();
                    return getSolutionObtainedByMergeRoutesAndScheduleInformation();
                } else {
                    return null;
                }
            } else {
                System.out.println("lp problem not be solved");
                System.out.println(cplex.getStatus());
                this.timeInMilliSec = (int) (System.currentTimeMillis() - startTime);
                this.lowerBound = 0;
                this.upperBound = Integer.MAX_VALUE;
                return null;
            }
        } catch (IloException e) {
            System.out.println("exception");
            e.printStackTrace();
            System.exit(-1);
        }

        cplex.close();
        cplex.end();

        return null;

    }


    //need to be modified the method printSolution 2025.1.15
    private void printSolutionAfterSolve() throws IloException {
        // 7. print
        System.out.println("The mini cost of routing and scheduling is " + cplex.getObjValue());
        for (int k = 0; k < instance.getNbDepots(); k++) {
            Depot depot = instance.getDepot(k);
            if (cplex.getValue(varVehicleSourceDepotArc[k]) > 0.99) {
                // System.out.println("vehicle" + v + "_source_" + depot.getIndexOfDepotAsStartingPoint() + "=1.0");
            }
            if (cplex.getValue(varVehicleDepotSinkArc[k]) > 0.9999) {
                //System.out.println("vehicle" + v + "_" + depot.getIndexOfDepotAsEndingPoint() + "_sink=1.0");
            }
        }
        for (int i = 0; i < nbTrip; i++) {
            if (instance.whetherVehicleCanStartWithTrip(i)) {
                for (int k = 0; k < nbDepot; k++) {
                    Depot depot = instance.getDepot(k);
                    if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                        if (cplex.getValue(varVehicleArc[depot.getIndexOfDepotAsStartingPoint()][i]) > 0.9999) {
                            // System.out.println("vehicle" + v + "_" + depot.getIndexOfDepotAsStartingPoint() + "_" + i + "=1.0");
                        }
                    }
                }
            }

            if (instance.whetherVehicleCanEndAtTrip(i)) {
                for (int k = 0; k < nbDepot; k++) {
                    Depot depot = instance.getDepot(k);
                    if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                        if (cplex.getValue(varVehicleArc[i][depot.getIndexOfDepotAsEndingPoint()]) > 0.999) {
                            //System.out.println("vehicle" + v + "_" + i + "_" + depot.getIndexOfDepotAsEndingPoint() + "=1.0");
                        }
                    }
                }
            }

            for (int j = 0; j < nbTrip; j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    //check whether there is arc before let cplex get the value
                    //here pay attention: if the value is not less than infinitive, then we couldn't get the value of this variable, because it is not show up in the model
                    // System.out.println("trip_"+i+"trip_"+j);
                    if (cplex.getValue(varVehicleArc[i][j]) > 0.999) {
                        // System.out.println("vehicle_" + v + "perform arc " + i + "_" + j + "=" + cplex.getValue(varVehicleArc[i][j]));
                    }

                }
            }
        }
//

        // here is to print out the solution of the driver solution
//        for (int d = 0; d < nbDriverValidInSchedules; d++) {
//            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
//            for (int i = 0; i < nbTrip; i++) {
//                if (instance.whetherDriverCanStartWithTrip(i)) {
//                    for (int k = 0; k < nbDepot; k++) {
//                        Depot depot = instance.getDepot(k);
//                        if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
//                            if (cplex.getValue(varDriverSourceDepotArc[d][k]) > 0.9999) {
//                                // System.out.println("driver" + d + "_source_" + depot.getIndexOfDepotAsStartingPoint() + "=1.0");
//                            }
//                            if (cplex.getValue(varDriverArc[d][depot.getIndexOfDepotAsStartingPoint()][i]) > 0.9999) {
//                                //System.out.println("driver" + d + "_" + depot.getIndexOfDepotAsStartingPoint() + "_" + i + "=1.0");
//                            }
//                        }
//                    }
//                }
//
//                for (int k = 0; k < nbDepot; k++) {
//                    Depot depot = instance.getDepot(k);
//                    if (instance.whetherDriverCanEndAtTrip( i)) {
//                        for (int k1 = 0; k1 < nbDepot; k1++) {
//                            Depot depot1 = instance.getDepot(k1);
//                            if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
//                                if (cplex.getValue(varDriverDepotSinkArc[d][k]) > 0.9999) {
//                                    // System.out.println("driver" + d + "_" + depot.getIndexOfDepotAsEndingPoint() + "_sink=1.0");
//                                }
//                                if (cplex.getValue(varDriverArc[d][i][depot.getIndexOfDepotAsEndingPoint()]) > 0.9999) {
//                                    //System.out.println("driver" + d + "_"+i+ "_"+ depot.getIndexOfDepotAsEndingPoint()  + "=1.0");
//                                }
//                            }
//                        }
//
//                    }
//
//                }
//            }
//        }


        for (int i = 0; i < nbTrip; i++) {
            int qi = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int qj = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    if (qi == qj && qi == 2) {
                        if (cplex.getValue(varWhetherTwoVehiclePerformArcForAllComb[i][j]) > 0.99) {
                            System.out.println("there are two vehicle perform the combined arc_" + i + "_" + j);
                            if (cplex.getValue(varTwoVehicleWaitingTimeForAllComb[i][j]) > 0.9) {
                                System.out.println("Two vehicles perform the combined arc together with total waiting time" + cplex.getValue(varTwoVehicleWaitingTimeForAllComb[i][j]));
                                System.out.println("nb vehicles check " + cplex.getValue(varVehicleArc[i][j]));
                            }
                        } else if (cplex.getValue(varWhetherOneVehiclePerformArcForAllComb[i][j]) > 0.99) {
                            System.out.println("there is one vehicle perform the arc_" + i + "_" + j);
                            System.out.println("nb vehicles check " + cplex.getValue(varVehicleArc[i][j]));

                        }
                    }
                }

            }
        }


        //2024.9.17
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            if (cplex.getValue(varStartingTimeOfDriverSchedule[d]) > 0.99) {
                System.out.println("driver schedule_" + d + " start time " + varStartingTimeOfDriverSchedule[d]);
            }
        }

        for (int i = 0; i < nbTrip; i++) {
            if (cplex.getValue(varTripStartTime[i]) >= 0) {
                System.out.println("trip_" + i + " start time unit_" + cplex.getValue(varTripStartTime[i]));
            }
        }
//

        for (int i = 0; i < nbTrip; i++) {
            int duration_i = instance.getDuration(instance.getTrip(i).getIdOfStartCity(), instance.getTrip(i).getIdOfEndCity());
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {

                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int maxWaitingTime = Math.round(instance.getTrip(j).getLatestDepartureTime()
                            - instance.getTrip(i).getEarliestDepartureTime() - duration_i);
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if (maxWaitingTime >= minPlanTime) {
                        if (cplex.getValue(varVehicleArc[i][j]) > 0.99) {
                            if (nbVehicleNeed_i == 1 || nbVehicleNeed_j == 1) {
                                if (cplex.getValue(varVehicleWaitingTimeForNotAllComb[i][j]) > 0.99) {
                                    System.out.println("vehicle_" + " waiting _" + cplex.getValue(varVehicleWaitingTimeForNotAllComb[i][j]) + "time between trip_" + i + "_" + j);
                                }
                            }

                        }
                    }
                }
            }
        }

//
//
//
//
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                int duration_i = instance.getDuration(instance.getTrip(i).getIdOfStartCity(), instance.getTrip(i).getIdOfEndCity());
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (this.schedules.whetherIsContinuousArc(i, j)) {//2025.
                            int maxWaitingTime = (instance.getTrip(j).getLatestDepartureTime()
                                    - instance.getTrip(i).getEarliestDepartureTime() - duration_i);
                            if (driverSchedule.whetherArcExist(i, j)) {
                                if (cplex.getValue(varDriverArc[d][i][j]) > 0.99) {
                                    if (maxWaitingTime >= minPlanTime) {
                                        if (cplex.getValue(varDriverWaitingTime[d][i][j]) > 0.99) {
//                                        System.out.println("Driver_" + d + " waiting _" +cplex.getValue(varDriverWaitingTime[d][i][j])+ "between trip_"+i+ "_" + j);
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    // this constraint is about at least one person in each trip, which is (17) in the paper
    private void cnstPassenger() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int d = 0; d < nbDriver; d++) {
                Driver driver = instance.getDriver(d);
                if (instance.whetherDriverCanEndAtTrip(i)) {
                    // end at trip i
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                            expr.addTerm(varDriverArc[d][i][depot.getIndexOfDepotAsEndingPoint()], 1);
                        }
                    }
                }

                for (int j = 0; j < nbTrip; j++) {
                    //third case: traverse by trip i
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        expr.addTerm(varDriverArc[d][i][j], 1);
                    }
                }
            }
            IloRange constraint = cplex.addGe(expr, 1, "Pass" + "Trip" + i);
        }
    }

    private void cnstDriverFlowConstraintForTrip() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            Driver driver = instance.getDriver(d);
            for (int i = 0; i < nbTrip; i++) {
                IloLinearNumExpr exprFlowEnter = cplex.linearNumExpr();
                IloLinearNumExpr exprFlowOut = cplex.linearNumExpr();
                //first case: Start with trip i
                if (instance.whetherDriverCanStartWithTrip(i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                            exprFlowEnter.addTerm(varDriverArc[d][depot.getIndexOfDepotAsStartingPoint()][i], 1);
                        }
                    }
                }
                //second case: End at trip i
                if (instance.whetherDriverCanEndAtTrip(i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                            exprFlowOut.addTerm(varDriverArc[d][i][depot.getIndexOfDepotAsEndingPoint()], 1);
                        }
                    }
                }
                //third case: traverse by trip i
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(j, i))
                        exprFlowEnter.addTerm(varDriverArc[d][j][i], 1);
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j))
                        exprFlowOut.addTerm(varDriverArc[d][i][j], 1);
                }
                IloConstraint constraint = cplex.addEq(exprFlowOut, exprFlowEnter, "dr" + d + "Trip" + i + "Flow");
            }
        }
    }


    // this constraint is for combine number of vehicle in each trip, which is (7) in paper
    private void cnstnbVehicleInTrip() throws IloException {
        // count the number of vehicles go out from trip i,
        // in each trip the total number of vehicle arcs equals required number of vehicles
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            //first case: some vehicle can leave i enter the depot
            // then the vehicle go out to depot, then we need to know where is i end to obtain the out arc
            if (instance.whetherVehicleCanEndAtTrip(i)) {
                for (int k = 0; k < nbDepot; k++) {
                    Depot depot = instance.getDepot(k);
                    int indexOfDepotAsEndingPoint = depot.getIndexOfDepotAsEndingPoint();
                    if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                        expr.addTerm(varVehicleArc[i][indexOfDepotAsEndingPoint], 1);
                    }
                }

            }
            // second case some vehicle leave i to another trip j behind
            for (int j = 0; j < nbTrip; j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j))
                    expr.addTerm(varVehicleArc[i][j], 1);//------here we need assign the number of vehicles in each trip
            }
            IloRange constraint = cplex.addEq(expr, instance.getTrip(i).getNbVehicleNeed(), "Trip" + i + "nbV");
        }
    }


    // this FlowConstraint about the trip, which is constraint (6) in paper
    private void cnstVehicleFlowConstraintForTrip() throws IloException {

        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr exprFlowEnter = cplex.linearNumExpr();
            IloLinearNumExpr exprFlowOut = cplex.linearNumExpr();
            //Starting with trip i
            if (instance.whetherVehicleCanStartWithTrip(i)) {
                for (int k = 0; k < nbDepot; k++) {
                    Depot depot = instance.getDepot(k);
                    if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                        exprFlowEnter.addTerm(varVehicleArc[depot.getIndexOfDepotAsStartingPoint()][i], 1);
                    }
                }
            }
            //Ending with trip i
            if (instance.whetherVehicleCanEndAtTrip(i)) {
                for (int k = 0; k < nbDepot; k++) {
                    Depot depot = instance.getDepot(k);
                    if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                        exprFlowOut.addTerm(varVehicleArc[i][depot.getIndexOfDepotAsEndingPoint()], 1);
                    }
                }
            }
            //general case
            for (int j = 0; j < nbTrip; j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(j, i))
                    exprFlowEnter.addTerm(varVehicleArc[j][i], 1);
                if (instance.whetherHavePossibleArcAfterCleaning(i, j))
                    exprFlowOut.addTerm(varVehicleArc[i][j], 1);

            }
            IloConstraint constraint = cplex.addEq(exprFlowOut, exprFlowEnter, "Vehicle" + "Trip" + i + "Flow");
        }
    }


    /**
     * new  part about the vehicle and driver use and depot flow constraint
     */
    //__________________________________________________________________________________________________________________


    //constraint (3) for the vehicle source depot and depot sink arc
    private void cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotSinkArc() throws IloException {
        for (int k = 0; k < nbDepot; k++) {

            IloLinearNumExpr exprVehicleSourceDepot = cplex.linearNumExpr();
            IloLinearNumExpr exprVehicleDepotSink = cplex.linearNumExpr();

            exprVehicleSourceDepot.addTerm(varVehicleSourceDepotArc[k], 1);
            exprVehicleDepotSink.addTerm(varVehicleDepotSinkArc[k], 1);

            IloConstraint constraint = cplex.addEq(exprVehicleSourceDepot, exprVehicleDepotSink, "Vehicle Dp" + k + "So&Si");

        }
    }

    //constraint (4) flow constraint for the depot and the source
    private void cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotTripArc() throws IloException {
        for (int k = 0; k < instance.getNbDepots(); k++) {
            IloLinearNumExpr exprVehicleSourceDepot = cplex.linearNumExpr();
            IloLinearNumExpr exprVehicleDepotTrip = cplex.linearNumExpr();

            exprVehicleSourceDepot.addTerm(varVehicleSourceDepotArc[k], 1);

            Depot depot = instance.getDepot(k);
            int indexOfDepotAsStartingPoint = depot.getIndexOfDepotAsStartingPoint();
            for (int j = 0; j < instance.getNbTrips(); j++) {

                Trip trip = instance.getTrip(j);
                if (trip.getIdOfStartCity() == depot.getIdOfCityAsDepot()) {//only when the trip start from the depot
                    exprVehicleDepotTrip.addTerm(varVehicleArc[indexOfDepotAsStartingPoint][j], 1);
                }
            }

            IloConstraint constraint = cplex.addEq(exprVehicleSourceDepot, exprVehicleDepotTrip, "V" + "Dp" + k + "SoFlow");

        }

    }

    //constraint (5) flow constraint for the depot and sink
    private void cnstRelationshipOfVehicleDepotSinkArcAndVehicleDepotTripArc() throws IloException {
        for (int k = 0; k < instance.getNbDepots(); k++) {
            IloLinearNumExpr exprVehicleDepotSink = cplex.linearNumExpr();
            IloLinearNumExpr exprVehicleTripDepot = cplex.linearNumExpr();
            exprVehicleDepotSink.addTerm(varVehicleDepotSinkArc[k], 1);
            Depot depot = instance.getDepot(k);
            int indexOfDepotAsEndingPoint = depot.getIndexOfDepotAsEndingPoint();
            for (int i = 0; i < instance.getNbTrips(); i++) {
                Trip trip = instance.getTrip(i);
                if (trip.getIdOfEndCity() == depot.getIdOfCityAsDepot()) {//only when the trip start from the depot
                    exprVehicleTripDepot.addTerm(varVehicleArc[i][indexOfDepotAsEndingPoint], 1);
                }
            }

            IloConstraint constraint = cplex.addEq(exprVehicleDepotSink, exprVehicleTripDepot, "V" + "Dp" + k + "SiFlow");

        }


    }
    //_______________________________________________________________add constraint of driver part

    //constraint (13) for driver use and driver source and depot arc
//    private void cnstRelationshipOfDriverUseAndSourceDepot() throws IloException {
//        for (int d = 0; d < nbDriver; d++) {
//            IloLinearNumExpr exprDriverUse = cplex.linearNumExpr();
//            IloLinearNumExpr exprDriverSourceDepot = cplex.linearNumExpr();
//            exprDriverUse.addTerm(varDriverUse[d], 1);
//            for (int k = 0; k < nbDepot; k++) {
//                exprDriverSourceDepot.addTerm(varDriverSourceDepotArc[d][k], 1);
//            }
//            IloConstraint constraint = cplex.addEq(exprDriverUse, exprDriverSourceDepot, "D" + d + "Use&SoDp");
//        }
//    }

    //constraint (14) for the driver source depot and depot sink arc
    private void cnstRelationshipOfDriverSourceDepotArcAndDriverDepotSinkArc() throws IloException {
        for (int k = 0; k < nbDepot; k++) {
            Depot depot = instance.getDepot(k);
            for (int d = 0; d < instance.getMaxNbDriverAvailable(); d++) {
                IloLinearNumExpr exprDriverSourceDepot = cplex.linearNumExpr();
                IloLinearNumExpr exprDriverDepotSink = cplex.linearNumExpr();
                exprDriverSourceDepot.addTerm(varDriverSourceDepotArc[d][k], 1);
                exprDriverDepotSink.addTerm(varDriverDepotSinkArc[d][k], 1);
                IloConstraint constraint = cplex.addEq(exprDriverSourceDepot, exprDriverDepotSink, "Dp" + k + "So&Si");
            }
        }
    }

    //constraint (15) flow constraint for the depot and the source(source depot  and depot trip arc)
    private void cnstRelationshipOfDriverSourceDepotArcAndDriverDepotTripArc() throws IloException {
        for (int d = 0; d < instance.getMaxNbDriverAvailable(); d++) {
            for (int k = 0; k < instance.getNbDepots(); k++) {
                IloLinearNumExpr exprDriverSourceDepot = cplex.linearNumExpr();
                IloLinearNumExpr exprDriverDepotTrip = cplex.linearNumExpr();

                exprDriverSourceDepot.addTerm(varDriverSourceDepotArc[d][k], 1);

                Depot depot = instance.getDepot(k);
                int indexOfDepotAsStartingPoint = depot.getIndexOfDepotAsStartingPoint();
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    Trip trip = instance.getTrip(j);
                    if (trip.getIdOfStartCity() == depot.getIdOfCityAsDepot()) {//only when the trip start from the depot
                        exprDriverDepotTrip.addTerm(varDriverArc[d][indexOfDepotAsStartingPoint][j], 1);
                    }
                }

                IloConstraint constraint = cplex.addEq(exprDriverSourceDepot, exprDriverDepotTrip, "D" + d + "Dp" + k + "SoFlow");

            }

        }

    }

    //constraint (16) flow constraint for the depot and sink
    private void cnstRelationshipOfDriverDepotSinkAndDepotTrip() throws IloException {
        for (int d = 0; d < instance.getMaxNbDriverAvailable(); d++) {
            for (int k = 0; k < instance.getNbDepots(); k++) {
                IloLinearNumExpr exprDriverDepotSink = cplex.linearNumExpr();
                IloLinearNumExpr exprDriverTripDepot = cplex.linearNumExpr();

                exprDriverDepotSink.addTerm(varDriverDepotSinkArc[d][k], 1);

                Depot depot = instance.getDepot(k);
                int indexOfDepotAsEndingPoint = depot.getIndexOfDepotAsEndingPoint();
                for (int i = 0; i < instance.getNbTrips(); i++) {
                    Trip trip = instance.getTrip(i);
                    if (trip.getIdOfEndCity() == depot.getIdOfCityAsDepot()) {//only when the trip start from the depot
                        exprDriverTripDepot.addTerm(varDriverArc[d][i][indexOfDepotAsEndingPoint], 1);
                    }
                }

                IloConstraint constraint = cplex.addEq(exprDriverDepotSink, exprDriverTripDepot, "D" + d + "Dp" + k + "SiFlow");

            }

        }

    }


    //cnst (33)-1
    private void cnstStartingTimeRange1() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr exprStartingTime = cplex.linearNumExpr();
            Trip trip_i = instance.getTrip(i);
            int a_i = trip_i.getEarliestDepartureTime();
            exprStartingTime.addTerm(varTripStartTime[i], 1);
            IloConstraint constraint = cplex.addGe(exprStartingTime, a_i, "StartTime_trip_" + i);
        }
    }

    //cnst (33)-2
    private void cnstStartingTimeRange2() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr exprStartingTime = cplex.linearNumExpr();
            Trip trip_i = instance.getTrip(i);
            int b_i = trip_i.getLatestDepartureTime();
            exprStartingTime.addTerm(varTripStartTime[i], 1);
            IloConstraint constraint = cplex.addLe(exprStartingTime, b_i, "StartTime_trip_" + i);
        }
    }


    //(34)-1
    private void cnstVehicleWaitingTimeForNotAllCombinedArc_M1() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && (nbVehicleNeed_i == 1 || nbVehicleNeed_j == 1)) {
                    int duration_i = instance.getTrip(i).getDuration();
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        int M_Waiting = maxWaitingTime;
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varVehicleWaitingTimeForNotAllComb[i][j], 1);
                        expr.addTerm(varTripStartTime[j], -1);
                        expr.addTerm(varTripStartTime[i], 1);

                        expr.addTerm(varVehicleArc[i][j], -M_Waiting);

                        IloConstraint constraint = cplex.addGe(expr, -duration_i - M_Waiting, "cnstVehicleWaitingTimeForNotAllCombinedArc_M1_" + i + "_" + j);

                    }
                }
            }
        }
    }


    //(34)-2
    private void cnstVehicleWaitingTimeForNotAllCombinedArc_M2() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && (nbVehicleNeed_i == 1 || nbVehicleNeed_j == 1)) {
                    int duration_i = instance.getTrip(i).getDuration();
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        int M_Waiting = duration_i + instance.getTrip(i).getLatestDepartureTime() - instance.getTrip(j).getEarliestDepartureTime() + 1;
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varVehicleWaitingTimeForNotAllComb[i][j], 1);
                        expr.addTerm(varTripStartTime[j], -1);
                        expr.addTerm(varTripStartTime[i], 1);

                        expr.addTerm(varVehicleArc[i][j], M_Waiting);

                        IloConstraint constraint = cplex.addLe(expr, -duration_i + M_Waiting, "cnstVehicleWaitingTimeForNotAllCombinedArc_M2_" + i + "_" + j);

                    }
                }
            }
        }
    }


    //(35)-1
    private void cnstVehicleWaitingTimeForNotAllCombinedArc_1() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && (nbVehicleNeed_i == 1 || nbVehicleNeed_j == 1)) {
                    int duration_i = instance.getTrip(i).getDuration();
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varVehicleWaitingTimeForNotAllComb[i][j], 1);
                        expr.addTerm(varVehicleArc[i][j], -minPlanTime);
                        IloConstraint constraint = cplex.addGe(expr, 0, "cnstVehicleWaitingTimeForNotAllCombinedArc_1_" + i + "_" + j);
                    }
                }
            }
        }
    }

    //(35)-2
    private void cnstVehicleWaitingTimeForNotAllCombinedArc_2() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && (nbVehicleNeed_i == 1 || nbVehicleNeed_j == 1)) {
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varVehicleWaitingTimeForNotAllComb[i][j], 1);
                        expr.addTerm(varVehicleArc[i][j], -maxWaitingTime);
                        IloConstraint constraint = cplex.addLe(expr, 0, "cnstVehicleWaitingTimeForNotAllCombinedArc_1_" + i + "_" + j);
                    }
                }
            }
        }
    }


    //(36)-1
    private void cnstOneVehicleWaitingTimeForAllCombinedArc_M1() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int duration_i = instance.getTrip(i).getDuration();
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            int M_Waiting = maxWaitingTime;
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varOneVehicleWaitingTimeForAllComb[i][j], 1);
                            expr.addTerm(varTripStartTime[j], -1);
                            expr.addTerm(varTripStartTime[i], 1);
                            expr.addTerm(varWhetherOneVehiclePerformArcForAllComb[i][j], -M_Waiting);
                            IloConstraint constraint = cplex.addGe(expr, -duration_i - M_Waiting, "cnstVehicleWaitingTimeForAllCombinedArc_M1_" + i + "_" + j);

                        }
                    }
                }
            }
        }
    }


    //(36)-2

    private void cnstOneVehicleWaitingTimeForAllCombinedArc_M2() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int duration_i = instance.getTrip(i).getDuration();
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            int M_Waiting = duration_i + instance.getTrip(i).getLatestDepartureTime() - instance.getTrip(j).getEarliestDepartureTime() + 1;
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varOneVehicleWaitingTimeForAllComb[i][j], 1);
                            expr.addTerm(varTripStartTime[j], -1);
                            expr.addTerm(varTripStartTime[i], 1);
                            expr.addTerm(varWhetherOneVehiclePerformArcForAllComb[i][j], M_Waiting);
                            IloConstraint constraint = cplex.addLe(expr, -duration_i + M_Waiting, "cnstVehicleWaitingTimeForAllCombinedArc_M2_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }


    //new for two vehicle in combined(36)-1
    private void cnstTwoVehicleWaitingTimeForAllCombinedArc_M1() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int duration_i = instance.getTrip(i).getDuration();
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        int M_Waiting = 2 * maxWaitingTime;
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varTwoVehicleWaitingTimeForAllComb[i][j], 1);
                        expr.addTerm(varTripStartTime[j], -nbVehicleNeed_i);
                        expr.addTerm(varTripStartTime[i], nbVehicleNeed_i);
                        expr.addTerm(varWhetherTwoVehiclePerformArcForAllComb[i][j], -M_Waiting);
                        IloConstraint constraint = cplex.addGe(expr, -nbVehicleNeed_i * duration_i - M_Waiting, "cnstVehicleWaitingTimeForAllCombinedArc_M1_" + i + "_" + j);

                    }
                }
            }
        }

    }


    //new for two vehicle in combined (36)-2

    private void cnstTwoVehicleWaitingTimeForAllCombinedArc_M2() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int duration_i = instance.getTrip(i).getDuration();
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        int M_Waiting = 2 * (duration_i + instance.getTrip(i).getLatestDepartureTime() - instance.getTrip(j).getEarliestDepartureTime() + 1);
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varTwoVehicleWaitingTimeForAllComb[i][j], 1);
                        expr.addTerm(varTripStartTime[j], -nbVehicleNeed_i);
                        expr.addTerm(varTripStartTime[i], nbVehicleNeed_i);
                        expr.addTerm(varWhetherTwoVehiclePerformArcForAllComb[i][j], M_Waiting);
                        IloConstraint constraint = cplex.addLe(expr, -nbVehicleNeed_i * duration_i + M_Waiting, "cnstVehicleWaitingTimeForAllCombinedArc_M2_" + i + "_" + j);
                    }
                }
            }
        }

    }

    //
    //(37)-1
    private void cnstOneVehicleWaitingTimeForAllCombinedArc_1() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int duration_i = instance.getTrip(i).getDuration();
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varOneVehicleWaitingTimeForAllComb[i][j], 1);
                            expr.addTerm(varWhetherOneVehiclePerformArcForAllComb[i][j], -minPlanTime);
                            IloConstraint constraint = cplex.addGe(expr, 0, "cnstVehicleWaitingTimeForAllCombinedArc_1_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }


    //(37)-2
    private void cnstOneVehicleWaitingTimeForAllCombinedArc_2() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int duration_i = instance.getTrip(i).getDuration();
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varOneVehicleWaitingTimeForAllComb[i][j], 1);
                        expr.addTerm(varWhetherOneVehiclePerformArcForAllComb[i][j], -maxWaitingTime);
                        IloConstraint constraint = cplex.addLe(expr, 0, "cnstVehicleWaitingTimeForAllCombinedArc_2_" + i + "_" + j);
                    }
                }
            }
        }
    }


    //(37)- new for two vehicles perform combine to combine-1
    private void cnstTwoVehicleWaitingTimeForAllCombinedArc_1() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int duration_i = instance.getTrip(i).getDuration();
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varTwoVehicleWaitingTimeForAllComb[i][j], 1);
                            expr.addTerm(varWhetherTwoVehiclePerformArcForAllComb[i][j], -nbVehicleNeed_i * minPlanTime);
                            IloConstraint constraint = cplex.addGe(expr, 0, "cnstTWOVehicleWaitingTimeForAllCombinedArc_1_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }


    //(37) new for two vehicle perform combine to combine-2
    private void cnstTwoVehicleWaitingTimeForAllCombinedArc_2() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int duration_i = instance.getTrip(i).getDuration();
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (maxWaitingTime >= minPlanTime) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varTwoVehicleWaitingTimeForAllComb[i][j], 1);
                        expr.addTerm(varWhetherTwoVehiclePerformArcForAllComb[i][j], -nbVehicleNeed_i * maxWaitingTime);
                        IloConstraint constraint = cplex.addLe(expr, 0, "cnstTWOVehicleWaitingTimeForAllCombinedArc_2_" + i + "_" + j);
                    }
                }
            }
        }
    }


    // here is after copy
    //(38)-1
    private void cnstDriverWaitingTime_M1() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                            cplex.addEq(varDriverArc[d][i][j], 1);// force the arc==1 2025.1.15
                            int duration_i = instance.getTrip(i).getDuration();
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                int M_Waiting = maxWaitingTime;
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(varDriverWaitingTime[d][i][j], 1);
                                expr.addTerm(varTripStartTime[j], -1);
                                expr.addTerm(varTripStartTime[i], 1);
                                expr.addTerm(varDriverArc[d][i][j], -M_Waiting);
                                IloConstraint constraint = cplex.addGe(expr, -duration_i - M_Waiting, "cnstDriverWaitingTime_M1_" + i + "_" + j);

                            }
                        }
                    }
                }
            }
        }
    }


    //(38)-2
    private void cnstDriverWaitingTime_M2() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                            cplex.addEq(varDriverArc[d][i][j], 1);// force the arc==1 2025.1.15
                            int duration_i = instance.getTrip(i).getDuration();
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                int M_Waiting = instance.getTrip(i).getLatestDepartureTime() + duration_i - instance.getTrip(j).getEarliestDepartureTime() + 1;
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(varDriverWaitingTime[d][i][j], 1);
                                expr.addTerm(varTripStartTime[j], -1);
                                expr.addTerm(varTripStartTime[i], 1);
                                expr.addTerm(varDriverArc[d][i][j], M_Waiting);
                                IloConstraint constraint = cplex.addLe(expr, -duration_i + M_Waiting, "cnstDriverWaitingTime_M2_" + i + "_" + j);
                            }
                        }
                    }
                }
            }
        }
    }


    //(39)-1
    private void cnstDriverWaitingTime_1() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                            cplex.addEq(varDriverArc[d][i][j], 1);// force the arc==1 2025.1.15
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(varDriverWaitingTime[d][i][j], 1);
                                expr.addTerm(varDriverArc[d][i][j], -minPlanTime);
                                IloConstraint constraint = cplex.addGe(expr, 0, "cnstDriverWaitingTime_1_" + i + "_" + j);
                            }
                        }
                    }
                }
            }
        }
    }

    //(39)-2
    private void cnstDriverWaitingTime_2() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(varDriverWaitingTime[d][i][j], 1);
                                expr.addTerm(varDriverArc[d][i][j], -maxWaitingTime);
                                IloConstraint constraint = cplex.addLe(expr, 0, "cnstDriverWaitingTime_2_" + i + "_" + j);
                            }
                        }
                    }
                }
            }
        }
    }

    //(40)
    private void cnstDetectOneVehicleShortConnectionArcForAllCombined() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                    int vehicleShortTime = instance.getShortConnectionTimeForVehicle();
                    int diffValue = instance.getMinPlanTurnTime() - vehicleShortTime;
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(varOneVehicleWaitingTimeForAllComb[i][j], 1);
                    expr.addTerm(varWhetherOneVehiclePerformArcForAllComb[i][j], -vehicleShortTime);
                    expr.addTerm(varWhetherVehicleShortArc[i][j], -diffValue);
                    IloConstraint constraint = cplex.addGe(expr, 0, "DetectVehicleShortConnection_" + "_" + i + "_" + j);
                }
            }
        }
    }


    // new for two vehicles perform the combiend arcs (40)
    private void cnstDetectTwoVehicleShortConnectionArcForAllCombined() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                    int vehicleShortTime = instance.getShortConnectionTimeForVehicle();
                    int diffValue = instance.getMinPlanTurnTime() - vehicleShortTime;
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(varTwoVehicleWaitingTimeForAllComb[i][j], 1);
                    expr.addTerm(varWhetherTwoVehiclePerformArcForAllComb[i][j], -nbVehicleNeed_i * vehicleShortTime);
                    expr.addTerm(varWhetherVehicleShortArc[i][j], -nbVehicleNeed_i * diffValue);
                    IloConstraint constraint = cplex.addGe(expr, 0, "DetectVehicleShortConnection_" + "_" + i + "_" + j);
                }
            }
        }
    }

    private void cnstLinkVehicleArcAndWhetherOneOrTwoPerfomArc() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(varVehicleArc[i][j], 1);
                    expr.addTerm(varWhetherOneVehiclePerformArcForAllComb[i][j], -1);
                    expr.addTerm(varWhetherTwoVehiclePerformArcForAllComb[i][j], -nbVehicleNeed_i);

                    IloConstraint constraint = cplex.addEq(expr, 0, "link vehicleArc Between combined task_" + i + "_" + j);

                }
            }
        }
    }


    //(41)
    private void cnstKeepSameVehiclesDuringShortConnectionInCombinedArc() throws IloException {
//        for (int v = 0; v < nbVehicle; v++) {
        for (int i = 0; i < nbTrip; i++) {
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            int minQ = nbVehicleNeed_i;
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (nbVehicleNeed_j < nbVehicleNeed_i) {
                    minQ = nbVehicleNeed_j;
                }
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(varVehicleArc[i][j], 1);
                    expr.addTerm(varWhetherVehicleShortArc[i][j], -minQ);
                    IloConstraint constraint = cplex.addLe(expr, 0);
                }
            }
        }
//        }
    }


    //(42)
    private void cnstDetectDriverShortConnectionArc() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                            cplex.addEq(varDriverArc[d][i][j], 1);// force the arc==1 2025.1.15
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            int driverShortConnectionTime = instance.getShortConnectionTimeForDriver();
                            int diffValue = instance.getMinPlanTurnTime() - driverShortConnectionTime;
                            if (varWhetherDriverShortArc[i][j] == null) {
                                throw new IloException("varWhetherDriverShortArc[" + i + "][" + j + "] is null.");
                            }
                            if (varDriverWaitingTime[d][i][j] == null) {
                                throw new IloException("varDriverWaitingTime[" + d + "][" + i + "][" + j + "] is null.");
                            }
                            if (varDriverArc[d][i][j] == null) {
                                throw new IloException("varDriverArc[" + d + "][" + i + "][" + j + "] is null.");
                            }

                            expr.addTerm(varWhetherDriverShortArc[i][j], -diffValue);
                            expr.addTerm(varDriverWaitingTime[d][i][j], 1);
                            expr.addTerm(varDriverArc[d][i][j], -driverShortConnectionTime);
                            IloConstraint constraint = cplex.addGe(expr, 0, "DetectDriverShortConnectionTime_" + d + "_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }

    //(43)
    private void cnstProhibitChangeVehicleDuringDriverShortConnection() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                            cplex.addEq(varDriverArc[d][i][j], 1);// force the arc==1 2025.1.15
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varWhetherDriverShortArc[i][j], 1);
                            expr.addTerm(varDriverArc[d][i][j], 1);
                            expr.addTerm(varVehicleArc[i][j], -1);
                            IloConstraint constraint = cplex.addLe(expr, 1);
                        }
                    }
                }
            }
        }
    }

    //(44)
    private void cnstStartingTimeOfSchedule() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            for (int p = 0; p < nbDepot; p++) {
                int idCityAsDepot = instance.getDepot(p).getIdOfCityAsDepot();
                for (int i = 0; i < nbTrip; i++) {
                    int idStartCityOfTrip = instance.getTrip(i).getIdOfStartCity();
                    if (idCityAsDepot == idStartCityOfTrip) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varStartingTimeOfDriverSchedule[d], 1);
                        expr.addTerm(varTripStartTime[i], -1);
                        expr.addTerm(varDriverArc[d][instance.getDepot(p).getIndexOfDepotAsStartingPoint()][i], M);
                        IloConstraint constraint = cplex.addLe(expr, M);
                    }
                }
            }
        }
    }

    //(45)
    private void cnstEndingTimeOfSchedule() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            for (int p = 0; p < nbDepot; p++) {
                int idCityAsDepot = instance.getDepot(p).getIdOfCityAsDepot();
                for (int i = 0; i < nbTrip; i++) {
                    int idEndingCityOfTrip = instance.getTrip(i).getIdOfEndCity();
                    int duration_i = instance.getTrip(i).getDuration();
                    if (idCityAsDepot == idEndingCityOfTrip) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varEndingTimeOfDriverSchedule[d], 1);
                        expr.addTerm(varTripStartTime[i], -1);
                        expr.addTerm(varDriverArc[d][i][instance.getDepot(p).getIndexOfDepotAsEndingPoint()], -M);
                        IloConstraint constraint = cplex.addGe(expr, duration_i - M);
                    }
                }
            }
        }
    }

    //(71)
    public void cnstWorkingTimeLimitation() throws IloException {
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            expr.addTerm(varEndingTimeOfDriverSchedule[d], 1);
            expr.addTerm(varStartingTimeOfDriverSchedule[d], -1);
            IloConstraint constraint = cplex.addLe(expr, instance.getMaxWorkingTime());
        }
    }


    //____________________________________
    private void defineObjectiveFunction() throws IloException {
        //3. 定义目标函数 cost for the aircraft part in the objective function
        IloLinearNumExpr obj = cplex.linearNumExpr();


        //cost for using vehicle
//        for (int v = 0; v < nbVehicle; v++) {
//
//            obj.addTerm(varVehicleUse[v], instance.getFixedCostForVehicle());
//        }

        //cost for using driver
//        for (int d = 0; d < nbDriverValidInSchedules; d++) {
//
//            obj.addTerm(varDriverUse[d], instance.getFixedCostForDriver());
//        }

        //idle cost for vehicle for two task are combined cased

        for (int k = 0; k < nbDepot; k++) {
            obj.addTerm(varVehicleSourceDepotArc[k], instance.getFixedCostForVehicle());
        }


        //idle cost for vehicle for two task are not all combined cased
        for (int i = 0; i < nbTrip; i++) {
            int duration_i = instance.getTrip(i).getDuration();
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            //instance.getDistance(instance.getTrip(i).getIdOfStartCity(), instance.getTrip(i).getIdOfEndCity());
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (nbVehicleNeed_i == 1 || nbVehicleNeed_j == 1) {
                        if (maxWaitingTime >= minPlanTime) {
                            int idleTimeCost_v = instance.getIdleTimeCostForVehiclePerUnit();
                            obj.addTerm(varVehicleWaitingTimeForNotAllComb[i][j], idleTimeCost_v);
                        }
                    }
                }
            }
        }


        for (int i = 0; i < nbTrip; i++) {
            int duration_i = instance.getTrip(i).getDuration();
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            //instance.getDistance(instance.getTrip(i).getIdOfStartCity(), instance.getTrip(i).getIdOfEndCity());
            for (int j = 0; j < nbTrip; j++) {
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                    if (nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                        if (maxWaitingTime >= minPlanTime) {

                            int idleTimeCost_v = instance.getIdleTimeCostForVehiclePerUnit();
                            obj.addTerm(varOneVehicleWaitingTimeForAllComb[i][j], idleTimeCost_v);
                            obj.addTerm(varWhetherTwoVehiclePerformArcForAllComb[i][j], idleTimeCost_v);
                        }
                    }
                }
            }
        }


        // cost of crew in the object function;
        /**attention the cost ; we need to know the cost for the whole path
         */
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {
                            int duration_i = instance.getTrip(i).getDuration();
                            int maxWaitingTime = (instance.getTrip(j).getLatestDepartureTime()
                                    - instance.getTrip(i).getEarliestDepartureTime() - duration_i);
                            if (maxWaitingTime >= minPlanTime) {
                                int idleTimeCost_d = instance.getIdleTimeCostForDriverPerUnit();
                                obj.addTerm(varDriverWaitingTime[d][i][j], idleTimeCost_d);
                            }
                        }
                    }
                }
            }
        }

        //cost of the changeover part here is what I need to modify// remove 2025.1.17
//        for (int d = 0; d < nbDriverValidInSchedules; d++) {
//            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
//            for (int i = 0; i < nbTrip; i++) {
//                for (int j = 0; j < nbTrip; j++) {
//                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                        if (driverSchedule.whetherArcExist(i, j)) {
//                            obj.addTerm(varChangeOver[d][i][j], instance.getCostForChangeOver());
//                        }
//                    }
//                }
//
//            }
//        }

        IloObjective objective11 = cplex.addMinimize(obj);
    }

    // This part is the Decision Variables
    private void defineDecisionVariables() throws IloException {
        //初始化所有决定变量
        //1.the dimension of the aircraft variables
        varVehicleArc = new IloNumVar[nbNodes][nbNodes];
        /**
         * add new variable about using vehicle and driver or not
         * */
//        varVehicleUse = new IloNumVar[nbVehicle];
//        varDriverUse = new IloNumVar[nbDriver];

        /**
         * add new variable about describing depot flow constraint
         */
        varVehicleSourceDepotArc = new IloNumVar[nbDepot];
        varVehicleDepotSinkArc = new IloNumVar[nbDepot];
        //________________________________________________________________________________________this is for source and sink for the vehicle
        varDriverSourceDepotArc = new IloNumVar[nbDriver][nbDepot];
        varDriverDepotSinkArc = new IloNumVar[nbDriver][nbDepot];
//        //_________________________________________________________________________________________this is for source and sink for the driver

        //1.For the crew we also use arc form; and we add the variable to describe whether the driver is driving; whether the vehicle is leading;
        //whether the driver change his original place
        varDriverArc = new IloNumVar[nbDriver][nbNodes][nbNodes];
//        varDriving = new IloNumVar[nbDriver][nbTrip]; //remove2025.1.17
//        varLeading = new IloNumVar[nbVehicle][nbTrip]; //remove2025.1.17
//        varChangeOver = new IloNumVar[nbDriver][nbTrip][nbTrip]; //remove2025.1.17


        //********************************************************* The following here are the new variables to deal with time window 2024.9.12 -20224.9.17 ****************
        varTripStartTime = new IloNumVar[nbTrip];
        varDriverWaitingTime = new IloNumVar[nbDriver][nbTrip][nbTrip];

        varVehicleWaitingTime = new IloNumVar[nbTrip][nbTrip];//add 2025.1.17

        varVehicleWaitingTimeForNotAllComb = new IloNumVar[nbTrip][nbTrip]; //add 2025.1.22
        varOneVehicleWaitingTimeForAllComb = new IloNumVar[nbTrip][nbTrip];//add 2025.1.22
        varTwoVehicleWaitingTimeForAllComb = new IloNumVar[nbTrip][nbTrip];//add 2025.1.22
        varWhetherOneVehiclePerformArcForAllComb = new IloNumVar[nbTrip][nbTrip];//add 2025.1.22
        varWhetherTwoVehiclePerformArcForAllComb = new IloNumVar[nbTrip][nbTrip];//add 2025.1.22


        varWhetherDriverShortArc = new IloNumVar[nbTrip][nbTrip];
        varWhetherVehicleShortArc = new IloNumVar[nbTrip][nbTrip];
        varStartingTimeOfDriverSchedule = new IloNumVar[nbDriver];
        varEndingTimeOfDriverSchedule = new IloNumVar[nbDriver];
        //********************* the above are the new variables dealing with time window 2024.9.12 -20224.9.17********************


        //2.变量的类型及取值范围声明
        //variable of vehicle use -type and range
        // variable of vehicle pass which arc ---type and range
        /**
         * add two new variable range about use of vehicle and driver
         * */
        if (this.whetherSolveRelaxationProblem == true) {


            for (int p = 0; p < instance.getNbDepots(); p++) {
                varVehicleSourceDepotArc[p] = cplex.numVar(0, nbVehicle, "x_v" + "_source_" + p);
            }


            for (int k = 0; k < instance.getNbDepots(); k++) {
                varVehicleDepotSinkArc[k] = cplex.numVar(0, nbVehicle, "x_v" + "_" + k + "_sink");
            }


            for (int d = 0; d < nbDriver; d++) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    varDriverSourceDepotArc[d][p] = cplex.numVar(0, 1, "x_d" + d + "_source_" + p);
                }
            }

            for (int d = 0; d < nbDriver; d++) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    varDriverDepotSinkArc[d][k] = cplex.numVar(0, 1, "x_d" + d + "_" + k + "_sink");
                }
            }

            //___________________________________________________________________________________here is all the new variable

            for (int i = 0; i < nbNodes; i++) {
                for (int j = 0; j < nbNodes; j++) {
                    if (i < this.nbTrip && j < this.nbTrip) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            varVehicleArc[i][j] = cplex.numVar(0, nbVehicle, "x_v" + "_" + i + "_" + j);
                        }
                    } else {
                        varVehicleArc[i][j] = cplex.numVar(0, nbVehicle, "x_v" + "_" + i + "_" + j);
                    }
                }
            }

            // variables for driver pass which arcs---- type and range
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbNodes; i++) {
                    for (int j = 0; j < nbNodes; j++) {
                        if (i < this.nbTrip && j < this.nbTrip) {
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                varDriverArc[d][i][j] = cplex.numVar(0, 1, "x_d" + d + "_" + i + "_" + j);
                            }
                        } else {
                            varDriverArc[d][i][j] = cplex.numVar(0, 1, "x_d" + d + "_" + i + "_" + j);
                        }
                    }
                }
            }
            // ****************************************************Here are the new variables to deal with the time window 2024.9.16- 2024.9.17
            for (int i = 0; i < nbTrip; i++) {
                varTripStartTime[i] = cplex.numVar(startHorizon, endHorizon, "t_trip_" + i);
            }
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                varDriverWaitingTime[d][i][j] = cplex.numVar(0, maxWaitingTime, "t_d_" + d + "_" + i + "_" + j);
                            }
                        }
                    }
                }
            }


            //************************* modify 9.19 ********************************************************************
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    varWhetherDriverShortArc[i][j] = cplex.numVar(0, 1, "b^DS_" + i + "_" + j);
                }
            }

            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    varWhetherVehicleShortArc[i][j] = cplex.numVar(0, 1, "b^VS_" + i + "_" + j);
                }
            }

            for (int d = 0; d < nbDriver; d++) {
                varStartingTimeOfDriverSchedule[d] = cplex.numVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), "s^d" + d);
            }

            for (int d = 0; d < nbDriver; d++) {
                varEndingTimeOfDriverSchedule[d] = cplex.numVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), "e^d" + d);
            }


        } else { //solving integer problem

            /**
             * add the new variable for the depot flow
             */

            for (int p = 0; p < instance.getNbDepots(); p++) {
                varVehicleSourceDepotArc[p] = cplex.intVar(0, nbVehicle, "x_v" + "_source_" + p);
            }


            for (int k = 0; k < instance.getNbDepots(); k++) {
                varVehicleDepotSinkArc[k] = cplex.intVar(0, nbVehicle, "x_v" + "_" + k + "_sink");
            }

            for (int d = 0; d < nbDriverValidInSchedules; d++) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    varDriverSourceDepotArc[d][p] = cplex.boolVar("x_d" + d + "_source_" + p);
                }
            }
            for (int d = 0; d < nbDriverValidInSchedules; d++) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    varDriverDepotSinkArc[d][k] = cplex.boolVar("x_d" + d + "_" + k + "_sink");
                }
            }
            //___________________________________________________________________________________here is all the new variable

            for (int i = 0; i < nbNodes; i++) {
                for (int j = 0; j < nbNodes; j++) {
                    if (i < this.nbTrip && j < this.nbTrip) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            varVehicleArc[i][j] = cplex.intVar(0, nbVehicle, "x_v" + "_" + i + "_" + j);
                        }
                    } else {
                        varVehicleArc[i][j] = cplex.intVar(0, nbVehicle, "x_v" + "_" + i + "_" + j);
                    }

                }
            }

            // variables for driver pass which arcs---- type and range
            for (int d = 0; d < nbDriverValidInSchedules; d++) {
                for (int i = 0; i < nbNodes; i++) {
                    for (int j = 0; j < nbNodes; j++) {
                        if (i < this.nbTrip && j < this.nbTrip) {
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                varDriverArc[d][i][j] = cplex.boolVar("x_d" + d + "_" + i + "_" + j);
                            }
                        } else {
                            varDriverArc[d][i][j] = cplex.boolVar("x_d" + d + "_" + i + "_" + j);
                        }

                    }
                }
            }
            //remove  whether diver is driving whether vehicle is leading  whether driver changeover //remove 2025.1.17

            //*********************************Here are the new variables to deal with the time window 2024.9.12-2024.9.19********************
            for (int i = 0; i < nbTrip; i++) {
                varTripStartTime[i] = cplex.intVar(startHorizon, endHorizon, "StartTime_t_i" + i);
            }

            for (int d = 0; d < nbDriverValidInSchedules; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                varDriverWaitingTime[d][i][j] = cplex.intVar(0, maxWaitingTime, "t_" + d + "_" + i + "_" + j);
                            }
                        }
                    }
                }
            }


            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            varVehicleWaitingTimeForNotAllComb[i][j] = cplex.intVar(0, maxWaitingTime, "t_V_" + i + "_" + j);
                        }
                    }
                }
            }


            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            varOneVehicleWaitingTimeForAllComb[i][j] = cplex.intVar(0, maxWaitingTime, "t_V1_" + "_" + i + "_" + j);
                        }
                    }
                }
            }


            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            varTwoVehicleWaitingTimeForAllComb[i][j] = cplex.intVar(0, 2 * maxWaitingTime, "t_V2_" + "_" + i + "_" + j);
                        }
                    }
                }
            }

            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        varWhetherOneVehiclePerformArcForAllComb[i][j] = cplex.boolVar("u^{V1}_" + "_" + i + "_" + j);
                    }
                }
            }


            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        varWhetherTwoVehiclePerformArcForAllComb[i][j] = cplex.boolVar("u^{V2}_" + i + "_" + j);
                    }
                }
            }


            // 2024.9.17
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    varWhetherDriverShortArc[i][j] = cplex.boolVar("b^DS_" + i + "_" + j);
                }
            }

            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    varWhetherVehicleShortArc[i][j] = cplex.boolVar("b^VS_" + i + "_" + j);
                }
            }

            for (int d = 0; d < nbDriver; d++) {
                varStartingTimeOfDriverSchedule[d] = cplex.intVar(startHorizon, endHorizon, "s^d_" + d);
            }

            for (int d = 0; d < nbDriverValidInSchedules; d++) {
                varEndingTimeOfDriverSchedule[d] = cplex.intVar(startHorizon, endHorizon, "e^d_" + d);
            }

        }


    }


     //here I need to rethink about it, I need to rewrite how to obtain the solution from cplexForVehicle , then the different from last time is it also resolved driver schedule with time
    // so we want print solution togehter , but so for driver we need to set a new start TripWithStart (status as CG to keep maxWorking and maxDriver still satify), and arc flow not change, just change starting time
     // instead of just getRoutesofVehicleFromCplex, then directly merge the information
    // then until now only ensure the feasiblity of vehicle route and driver route, not consider changeover
    private Solution getSolutionObtainedByMergeRoutesAndScheduleInformation() throws IloException {
        Solution solution = new Solution(instance);
        //give vehicle path from the cplex solution
        Routes routes=this.getVehicleRoutesFromCplex();
        for(int r=0;r<routes.getRoutes().size();r++){
            Route route=routes.getRoutes().get(r);
            PathForVehicle pathForVehicle= new PathForVehicle(instance,r);
            pathForVehicle.setVehiclePathFromRoute(route,r);
            System.out.println("path for vehicle: idVehicle nbTrips [idTrip startingTime] "+pathForVehicle);
            solution.addPathInSetForVehicle(pathForVehicle);
        }

        //give driver path from the schedules of CG and time from cplex solution
        for (int d = 0; d < nbDriverValidInSchedules; d++) {
            DriverSchedule driverSchedule = this.schedules.getDriverSchedules().get(d);
            //int idDriver=this.schedules.getDriverSchedules()
            PathForDriver pathForDriver = new PathForDriver(instance, d);
            for (int l = 0; l < driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); l++) {
                int idTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getIdOfTrip();
                boolean whetherDrive =  driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getDrivingStatus();// set as CG because maybe already conflic maxDriving
                int startTime = (int) Math.round(cplex.getValue(varTripStartTime[idTrip]));
                int idOfVehicleInTrip = Integer.MAX_VALUE;
                for (int v = 0; v < solution.getPathForVehicles().size(); v++) {
                    if (solution.getPathForVehicles().get(v).isPresentInPath(instance.getTrip(idTrip))) {
                        idOfVehicleInTrip = v;// set the first show up vehicle as
                    }
                }
                TripWithDriveInfos tripWithDriveInfos = new TripWithDriveInfos(instance.getTrip(idTrip), idOfVehicleInTrip, whetherDrive, startTime);
                pathForDriver.addTripInDriverPath(tripWithDriveInfos);

            }
            System.out.println("path for driver is: idDriver nbTrips [idTrip; id vehicle; status; starting time] "+"\n" + pathForDriver);
            solution.addPathInSetForDriver(pathForDriver);
        }
        return solution;
    }


    public Routes getVehicleRoutesFromCplex() throws IloException {
        // Initialize the capacity matrix
        int[][] CapacityMatrix = initializeResidualCapacityMatrixForAllTrips();
//        System.out.println("capacityMatrix" + initializeResidualCapacityMatrixForAllTrips());
        // Create a Routes object to store all generated routes
        Routes allVehicleRoutes = new Routes(instance);
        // Loop through each depot
        for (int depotIndex = 0; depotIndex < nbDepot; depotIndex++) {
            Depot depot = instance.getDepot(depotIndex);
            generateRoutesFromStartingDepot(depot, CapacityMatrix, allVehicleRoutes);
        }
        return allVehicleRoutes;
    }

    private void generateRoutesFromStartingDepot(Depot startingDepot, int[][] CapacityMatrix, Routes allVehicleRoutes) throws IloException {
        Routes routes = new Routes(instance);
        generateRoutesFromTrip(instance, startingDepot, CapacityMatrix, routes);
        allVehicleRoutes.addAllRoutes(routes);
    }

// Helper method to generate routes from a trip

    private void generateRoutesFromTrip(Instance instance, Depot startDepot, int[][] CapacityMatrix, Routes routes) throws IloException {
        int nbRouteFromThisDepot = 0;
        for (int i = 0; i < nbTrip; i++) {
            Trip trip = instance.getTrip(i);
            nbRouteFromThisDepot = nbRouteFromThisDepot + CapacityMatrix[startDepot.getIndexOfDepotAsStartingPoint()][i];

        }
        //System.out.println("in the Matrix nbRoutes from depot " + startDepot + " is " + nbRouteFromThisDepot);
        Node startDepotNode = new Node(startDepot, true);

        for (int r = 0; r < nbRouteFromThisDepot; r++) {
            Route route = new Route(instance);
            route.addTripWithStartInfosInNodes(startDepotNode);

            int idCurrentNode = startDepot.getIndexOfDepotAsStartingPoint(); // 重置当前行程为起始行程
            boolean findNext = true;
            int loopCount = 0; // 用于限制循环次数，避免无限循环
            while (findNext) {
                findNext = false;
                for (int l = 0; l < nbNodes; l++) {
                    if (CapacityMatrix[idCurrentNode][l] >= 1) {
                        if (l < nbTrip) {
                            int startTimeOfTrip= (int) Math.round(cplex.getValue(varTripStartTime[instance.getTrip(l).getIdOfTrip()]));
                            //System.out.println("check start time "+cplex.getValue(varTripStartTime[instance.getTrip(l).getIdOfTrip()]));
                            TripWithStartingInfos tripWithStartingInfos= new TripWithStartingInfos(instance.getTrip(l),startTimeOfTrip);
                            Node nextNode = new Node(tripWithStartingInfos);
                            route.addTripWithStartInfosInNodes(nextNode);
                            CapacityMatrix[idCurrentNode][l]--;
                            idCurrentNode = l;
                            findNext = true;
                            break;
                        }else {
                            findNext=false;
                            for (int p = 0; p < nbDepot; p++) {
                                Depot endingDepot = instance.getDepot(p);
                                int indexEndingDepot = endingDepot.getIndexOfDepotAsEndingPoint();
                                if (indexEndingDepot == l) {
                                    Node endDepotNode = new Node(endingDepot, false);
                                    route.addTripWithStartInfosInNodes(endDepotNode);
                                }
                            }

                        }
                    }

                }
            }
            loopCount++; // 增加循环次数
            routes.addRoute(route);
        }
    }

    public int[][] initializeResidualCapacityMatrixForAllTrips() throws IloException {
        int[][] residualCapacityMatrix = new int[nbNodes][nbNodes];
        // 初始化残余容量矩阵为0
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                residualCapacityMatrix[i][j] = 0;
            }
        }
        // 根据cplex的结果给它更改相应的值
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                // normal trip to trip
                if (i < nbTrip && j < nbTrip) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (cplex.getValue(varVehicleArc[i][j]) > 0.99) {
                            residualCapacityMatrix[i][j] = (int) (Math.round(cplex.getValue(varVehicleArc[i][j])));
                            if (residualCapacityMatrix[i][j] == 2) {
                                //System.out.println("residual Capacity ==2, Matrix i_" + i + " j_" + j + " value: " + cplex.getValue(varVehicleArc[i][j]));
                            } else if (residualCapacityMatrix[i][j] == 1) {
                                //System.out.println("residual Capacity ==1, Matrix i_" + i + " j_" + j + " value: " + cplex.getValue(varVehicleArc[i][j]));
                            }
                        }
                    }
                } else if (i < nbTrip && j >= nbTrip) {
                    Trip trip = instance.getTrip(i);
                    if (instance.whetherVehicleCanEndAtTrip(trip.getIdOfTrip())) {
                        int idEndingCity = trip.getIdOfEndCity();
                        for (int p = 0; p < nbDepot; p++) {
                            Depot depot = instance.getDepot(p);
                            int idCityAsDepot = depot.getIdOfCityAsDepot();
                            if (idEndingCity == idCityAsDepot) {
                                if (cplex.getValue(varVehicleArc[i][depot.getIndexOfDepotAsEndingPoint()]) > 0.99) {
                                    residualCapacityMatrix[i][depot.getIndexOfDepotAsEndingPoint()] =
                                            (int) Math.round(cplex.getValue(varVehicleArc[i][depot.getIndexOfDepotAsEndingPoint()]));
                                }
                            }
                        }
                    }
                } else if (i >= nbTrip && j < nbTrip) {
                    Trip trip = instance.getTrip(j);
                    if (instance.whetherVehicleCanStartWithTrip(trip.getIdOfTrip())) {
                        int idStartingCity = trip.getIdOfStartCity();
                        for (int p = 0; p < nbDepot; p++) {
                            Depot depot = instance.getDepot(p);
                            int idCityAsDepot = depot.getIdOfCityAsDepot();
                            if (idStartingCity == idCityAsDepot) {
                                if (cplex.getValue(varVehicleArc[depot.getIndexOfDepotAsStartingPoint()][j]) > 0.99) {
                                    residualCapacityMatrix[depot.getIndexOfDepotAsStartingPoint()][j] =
                                            (int) Math.round(cplex.getValue(varVehicleArc[depot.getIndexOfDepotAsStartingPoint()][j]));
                                }
                            }
                        }
                    }
                }
            }
        }
        return residualCapacityMatrix;
    }

    public double getTimeInSec() {
        return timeInMilliSec / 1000;
    }


    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public double getGap() {
        double decimalNumber = 100 * (upperBound - lowerBound) / upperBound;
        return decimalNumber;
    }


    @Override
    public String toString() {
        return "SolverWithFormulation_2{" +
                //"instance=" + instance +
                '}';
    }


    public static void main(String[] args) throws IloException, IOException {

//        InstanceReader reader = new InstanceReader("largerExample.txt");
        InstanceReader reader1 = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips040_combPer0.25_TW2.txt");

        Instance instance = reader1.readFile(); //这个语句将文本的内容就读出来了
        System.out.println(instance);


        SchedulesReader reader2 = new SchedulesReader("scheduleSolution_inst_nbCity03_Size90_Day1_nbTrips040_combPer0.25_TW2.txt", instance);

        Schedules schedules1 = reader2.readFile();

        System.out.println("schedules " + schedules1);

        SolverTwoIndexBasedOnDriverScheduleWithNoChangeOver solverWithFormulation1 = new SolverTwoIndexBasedOnDriverScheduleWithNoChangeOver(instance, schedules1, false);


        // print solution

        Solution sol = solverWithFormulation1.solveWithCplexBasedOnGivenSchedules();
        Routes routes=solverWithFormulation1.getVehicleRoutesFromCplex();
        System.out.println("vehicle routes "+routes);
        if (sol != null) {
            sol.printInfile("sol_inst_nbCity03_Size90_Day1_nbTrips040_combPer0.25_TW2.txt");
        }

        solverWithFormulation1.initializeResidualCapacityMatrixForAllTrips();
        solverWithFormulation1.getSolutionObtainedByMergeRoutesAndScheduleInformation();
        //System.out.println(sol);
        System.out.println("time : " + solverWithFormulation1.getTimeInSec() + " s");
        System.out.println("lb : " + solverWithFormulation1.getLowerBound());
        System.out.println("ub : " + solverWithFormulation1.getUpperBound());
    }
}
