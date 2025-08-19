package Solver;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import Instance.Vehicle;
import Instance.Instance;
import Instance.InstanceReader;
import Instance.Trip;
import Instance.Depot;
import Instance.Driver;
import Solution.Solution;
import Solution.PathForVehicle;
import Solution.TripWithStartingInfos;
import Solution.PathForDriver;
import Solution.TripWithDriveInfos;

import java.io.*;

import java.util.ArrayList;

public class SolverWithFormulation_1 {
    private Instance instance;
    private IloCplex cplex;
    private IloNumVar[] varVehicleUse;

    private IloNumVar[] varDriverUse;
    //-------------------------------------------------------
    private IloNumVar[][] varVehicleSourceDepotArc;

    private IloNumVar[][] varVehicleDepotSinkArc;

    private IloNumVar[][] varDriverSourceDepotArc;

    private IloNumVar[][] varDriverDepotSinkArc;
    //-------------------------------------------------------
    private IloNumVar[][][] varVehicleArc; //here is the vehicle variable
    private IloNumVar[][][] varDriverArc; //here is the variable x d i j
    private IloNumVar[][] varDriving; //here is the variable o i d
    private IloNumVar[][] varLeading; // here is the variable l v i
    private IloNumVar[][][] varChangeOver; // here is the variable y d i j describe whether the driver leaving his own place to other place

    // new variables to deal with the time window
    private IloNumVar[][] varWhetherDriverShortArc; //b_ij^DS
    private IloNumVar[][] varWhetherVehicleShortArc;//b_ij^VS
    private IloNumVar[] varStartingTimeOfDriverSchedule;//s^d
    private IloNumVar[] varEndingTimeOfDriverSchedule;//e^d

    // Here are the new variables to deal with the time window 2024.9.18
    private IloNumVar[] varTripStartTime;//t_i
    private IloNumVar[][] varVehicleWaitingTimeForNotAllComb;//t^V_ij

    private IloNumVar[][][] varVehicleWaitingTimeForAllComb;//t^v_ij
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

    public SolverWithFormulation_1(Instance instance) throws IloException {
        this.instance = instance;
        this.nbVehicle = instance.getMaxNbVehicleAvailable();
        this.nbDriver = instance.getMaxNbDriverAvailable();
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

    public SolverWithFormulation_1(Instance instance, String warmStartFilePath, Boolean whetherSolveRelaxationProblem) throws IloException {
        this(instance);
        this.warmStartFilePath = warmStartFilePath;
        this.whetherSolveRelaxationProblem = whetherSolveRelaxationProblem;
    }


    public Solution solveWithCplex() {
        try {
            cplex = new IloCplex();
            //cplex.setParam(IloCplex.Param.MIP.Strategy.File, 2);//give a warm start value from an outside file
            defineDecisionVariables();
            defineObjectiveFunction();

            // for vehicle
            cnstRelationshipOfVehicleUseAndSourceDepot(); //（2）
            cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotSinkArc(); //（3）****
            cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotTripArc(); //（4）
            cnstRelationshipOfVehicleDepotSinkArcAndVehicleDepotTripArc(); //（5）*****
            cnstVehicleFlowConstraintForTrip();//(6) Vehicle flow constraint for trip
            cnstnbVehicleInTrip(); //constraint (7)
            cnstOneLeadingVehicleInCombTrip();//constraint (8)
            cnstRelationshipOfVehicleLeadingAndTripArc();//constraint (9)
            cnstUseVehicleOrder();// constraints(10)

            // for driver
            cnstRelationshipOfDriverUseAndSourceDepot(); //（13）
            cnstRelationshipOfDriverSourceDepotArcAndDriverDepotSinkArc(); //（14）
            cnstRelationshipOfDriverSourceDepotArcAndDriverDepotTripArc(); //（15）****
            cnstRelationshipOfDriverDepotSinkAndDepotTrip(); //（16）****
            cnstDriverFlowConstraintForTrip();//(17)
            cnstPassenger();//(18)
            cnstDriving();//(19)
            cnstDrivingTime();//(20)
            cnstDrivingAndArc();//(21)
            cnstWorkStatusFirst();//(22)
            cnstWorkStatusSecond();//(23)
            cnstUseDriverOrder();//(24)

            //Here are the original linking constraints
            cnstChangeVehicleBetweenNormalTrips();//(27) count changeover when the driving driver change vehicle
            cnstChangeVehicleFromNormalToCombinedTrips();//(28)
            cnstVehicleChangeFromCombinedToNormalTrip();//(29)
            cnstChangeVehicleFromCombinedToCombinedTrips();//(30)
            cnstChangeVehicleFromCombinedToCombinedTripsCorrespoindingVerLeadingOrder();//(31)
            cnstChangeOverDuringCombineToCombineWhenNotSuccessiveToVehicle();//(32)

            //=============================================================================2024.9.16 Formulation-2 Extra Constraints====================
            cnstStartingTimeRange1();//(33)-1
            cnstStartingTimeRange2();//(33)-1
            cnstVehicleWaitingTimeForNotAllCombinedArc_M1();//(34)-1
            cnstVehicleWaitingTimeForNotAllCombinedArc_M2();//(34)-2
            cnstVehicleWaitingTimeForNotAllCombinedArc_1();//(35)-1
            cnstVehicleWaitingTimeForNotAllCombinedArc_2();//(35)-2

            cnstVehicleWaitingTimeForAllCombinedArc_M1();//(36)-1
            cnstVehicleWaitingTimeForAllCombinedArc_M2();//(36)-2
            cnstVehicleWaitingTimeForAllCombinedArc_1();//(37)-1
            cnstVehicleWaitingTimeForAllCombinedArc_2();//(37)-2

            cnstDriverWaitingTime_M1();//(38)-1
            cnstDriverWaitingTime_M2();//(38)-2
            cnstDriverWaitingTime_1();//(39)-1
            cnstDriverWaitingTime_2();//(39)-2

            cnstDetectVehicleShortConnectionArc();//(40)
            cnstKeepSameVehiclesDuringShortConnectionInCombinedArc();//(41)
            cnstDetectDriverShortConnectionArc();//(42)
            cnstProhibitChangeVehicleDuringDriverShortConnection();//(43)
            cnstStartingTimeOfSchedule();//(44)
            cnstEndingTimeOfSchedule();//(45)
            cnstWorkingTimeLimitation();//(46)

            // 5. export the model
            cplex.exportModel("modelOfRoutingAndScheduling.lp");

            cplex.setParam(IloCplex.Param.TimeLimit, 7200);// limit the time is two hour
          //  cplex.setParam(IloCplex.Param.Threads, 4);
//            //here parameter for gap is only for the small instance which gap is 0.01 but in a very short time
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1e-06);// 0703 tolerance 06 now change to 07 to solve specific instances
            //cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1e-08);
            //cplex.setParam(IloCplex.Param.MIP.Tolerances.AbsMIPGap, 1.0); // Set absolute gap to 1
           // cplex.setParam(IloCplex.Param.MIP.Limits.Nodes, 2147483647); // Remove node limit
           // cplex.setParam(IloCplex.Param.Emphasis.MIP, 2); // Emphasize feasibility





            //cplex.setParam(IloCplex.Param.Parallel, 1);
            //cplex.setParam(IloCplex.Param.MIP.Strategy.PresolveNode,0);//Node presolve strategy.
            // Set the HeurFreq parameter to -1 to turn off the node heuristic
            // cplex.setParam(IloCplex.Param.MIP.Strategy.HeuristicFreq, -1);
            // cplex.setParam(IloCplex.Param.MIP.Strategy.VariableSelect, 4);//try a less expensive variable selection strategy pseudo reduced costs.
            // cplex.setParam(IloCplex.Param.Emphasis.MIP, 1);//this emphasize feasibility over optimality
            // ----------------------------------------------here is for the warm start
            // Create a warm start according to a solution file
            if (this.warmStartFilePath != null && this.whetherSolveRelaxationProblem == false) {
                addWarmStartBeforeSolve(warmStartFilePath);
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
                System.out.println("Status: " + cplex.getStatus());
                System.out.println("Nodes processed: " + cplex.getNnodes());
                System.out.println("Is proven optimal: " + (cplex.getStatus() == IloCplex.Status.Optimal));
                if (this.whetherSolveRelaxationProblem == false) {
                    printSolutionAfterSolve();
                    return getSolutionFromCplex();
                } else {
                    return null;
                }
            } else {
                System.out.println("lp problem not be solved");
                System.out.println("Status: " + cplex.getStatus());
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cplex.close();
        cplex.end();

        return null;

    }

    private void addWarmStartBeforeSolve(String warmStartFilePath) throws IOException, IloException {
        if (warmStartFilePath != null) {
            File warmStartFile = new File(warmStartFilePath);
            ArrayList<IloNumVar> startVar = new ArrayList<>();
            ArrayList<Integer> starValue = new ArrayList<>();
            try {
                FileReader fileReader = new FileReader(warmStartFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                String line = bufferedReader.readLine();//read first line
                int index = 0;

                while (line.startsWith("//") || line.startsWith("The") || line.isEmpty()) {
                    line = bufferedReader.readLine();
                }
//              //Here is the part for varChangeOver[idOfDriver][idOfFirstTrip][idOfSecondTrip]
                while (line != null && line.startsWith("passengerAndNonLeading")) {
                    String[] values;
                    values = line.split(" ");
                    int m = 1;
                    startVar.add(index, varDriving[Integer.valueOf(values[m])][Integer.valueOf(values[m + 1])]);
                    starValue.add(index, 0);
//                  p
                    index++;
                    startVar.add(index, varLeading[Integer.valueOf(values[m])][Integer.valueOf(values[m + 1])]);
                    starValue.add(index, 0);
//                    System.out.println("varLeading(NonLeading)" + varLeading[Integer.valueOf(values[m])][Integer.valueOf(values[m + 1])]);
                    index++;
                    line = bufferedReader.readLine();

                }

                while (line.startsWith("//") || line.startsWith("The") || line.isEmpty()) {
                    line = bufferedReader.readLine();
                }
                //below here is vehicle arc part including vehicle use,vehicle source and sink depot, vehicle arc starting from depot ending to*****
                int idOfVehicle = 0;
                for (int v = 0; v < instance.getMaxNbVehicleAvailable(); v++) {
                    String[] values;
                    values = line.split(" ");
                    //varVehicleUse
                    startVar.add(index, varVehicleUse[idOfVehicle]);
                    starValue.add(index, 1);
                    //System.out.println("varVehicleUse " + varVehicleUse[idOfVehicle]);
                    index++;
                    //varVehicleSourceDepot&varVehicleDepotSink
                    startVar.add(index, varVehicleSourceDepotArc[idOfVehicle][Integer.valueOf(values[2])]);
                    starValue.add(index, 1);                    //System.out.println("varVehicleSourceDepot: " + varVehicleSourceDepotArc[idOfVehicle][Integer.valueOf(values[3])]);
                    index++;
                    startVar.add(index, varVehicleDepotSinkArc[idOfVehicle][Integer.valueOf(values[2])]);
                    starValue.add(index, 1);
                    //System.out.println("varVehicleDepotSink: " + varVehicleDepotSinkArc[idOfVehicle][Integer.valueOf(values[3])]);
                    index++;
                    //Here only varVehicleArc (Now is the depot to trip and trip to depot)
                    int i = 3;
//                    startVar.add(index, varVehicleArc[idOfVehicle][Integer.valueOf(values[i])][Integer.valueOf(values[i + 1])]);
//                    starValue.add(index, 1);
//                    index++;
                    i = 4;
                    //Now : varVehicleDriverArc (including the trip to trip, and including departure time)2024.9.26
                    while (i < values.length - 2) {
                        if (varVehicleArc[idOfVehicle][Integer.valueOf(values[i])][Integer.valueOf(values[i + 2])] == null) {
                            System.out.println(idOfVehicle + " " + Integer.valueOf(values[i]) + " " + Integer.valueOf(values[i + 2]));
                        }
                        startVar.add(index, varVehicleArc[idOfVehicle][Integer.valueOf(values[i])][Integer.valueOf(values[i + 2])]);
                        starValue.add(index, 1);
                        index++;
                        startVar.add(index, varTripStartTime[Integer.valueOf(values[i])]);
                        starValue.add(index, Integer.valueOf(values[i + 1]));
                        index++;
                        i = i + 2;
                    }
                    line = bufferedReader.readLine();
                    idOfVehicle = idOfVehicle + 1;
                }

                while (line.startsWith("//") || line.startsWith("The") || line.isEmpty()) {
                    line = bufferedReader.readLine();
                }
                int idOfDriver = 0;

                for (int d = 0; d < instance.getMaxNbDriverAvailable(); d++) {
                    String[] values;
                    values = line.split(" ");
                    //varDriverUse
                    startVar.add(index, varDriverUse[idOfDriver]);
                    starValue.add(index, 1);
                    index++;
                    startVar.add(index, varDriverSourceDepotArc[idOfDriver][Integer.valueOf(values[2])]);
                    starValue.add(index, 1);
                    //System.out.println("Warm start file: varDriverSourceDepot: " + varDriverSourceDepotArc[idOfDriver][Integer.valueOf(values[3])]);
                    index++;
                    startVar.add(index, varDriverDepotSinkArc[idOfDriver][Integer.valueOf(values[2])]);
                    starValue.add(index, 1);
                    //System.out.println("varDriverDepotSink: " + varDriverDepotSinkArc[idOfDriver][Integer.valueOf(values[3])]);
                    index++;
                    //varDriverDriverArc (including the depot to trip)
                    int j = 3;
//                    startVar.add(index, varDriverArc[idOfDriver][Integer.valueOf(values[j])][Integer.valueOf(values[j + 1])]);
//                    starValue.add(index, 1);
//                    index++;
                    j++;
                    //varDriverDriverArc (including the trip to trip, and omit departure time)
                    while (j < values.length - 1) {
                        startVar.add(index, varDriverArc[idOfDriver][Integer.valueOf(values[j])][Integer.valueOf(values[j + 2])]);
                        starValue.add(index, 1);
                        j = j + 2;
                        index++;
                    }
                    line = bufferedReader.readLine();
                    idOfDriver = idOfDriver + 1;
                }

                fileReader.close();
                bufferedReader.close();
                IloNumVar[] vars = new IloNumVar[startVar.size()];
                double[] vals = new double[starValue.size()];
                for (int i = 0; i < startVar.size(); i++) {
                    vars[i] = startVar.get(i);
                }
                for (int i = 0; i < starValue.size(); i++) {
                    vals[i] = starValue.get(i);
                }
                cplex.addMIPStart(vars, vals);
            } catch (IOException e) {
                e.printStackTrace();  // 打印完整的异常堆栈信息
                System.out.println("exception during warm start to read the file");
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("exception during warm start to read the file");
                System.out.println(e.getMessage());
                e.printStackTrace();  // 打印完整的异常堆栈信息
            } catch (IloException e) {
                System.out.println("exception during warm start to read the file");
                System.out.println(e.getMessage());
                e.printStackTrace();  // 打印完整的异常堆栈信息
            }
        }
    }

    private void printSolutionAfterSolve() throws IloException {
        // 7. print
        System.out.println("The mini cost of routing and scheduling is " + cplex.getObjValue());

        for (int v = 0; v < nbVehicle; v++) {
            if (cplex.getValue(varVehicleUse[v]) > 0.9999) {
                //System.out.println("vehicle" + v + " is used in the planing");
            }
        }

        for (int d = 0; d < nbDriver; d++) {
            if (cplex.getValue(varDriverUse[d]) > 0.9999) {
                //System.out.println("driver" + d + " is used in the planing");
            }
        }

        for (int v = 0; v < nbVehicle; v++) {
            Vehicle vehicle = instance.getVehicle(v);
            for (int k = 0; k < instance.getNbDepots(); k++) {
                Depot depot = instance.getDepot(k);

                if (cplex.getValue(varVehicleSourceDepotArc[v][k]) > 0.99) {

                    // System.out.println("vehicle" + v + "_source_" + depot.getIndexOfDepotAsStartingPoint() + "=1.0");
                }
                if (cplex.getValue(varVehicleDepotSinkArc[v][k]) > 0.9999) {
                    //System.out.println("vehicle" + v + "_" + depot.getIndexOfDepotAsEndingPoint() + "_sink=1.0");
                }
            }
            for (int i = 0; i < nbTrip; i++) {
                if (instance.whetherVehicleCanStartWithTrip(v, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                            if (cplex.getValue(varVehicleArc[v][depot.getIndexOfDepotAsStartingPoint()][i]) > 0.9999) {
                                // System.out.println("vehicle" + v + "_" + depot.getIndexOfDepotAsStartingPoint() + "_" + i + "=1.0");
                            }
                        }
                    }
                }

                if (instance.whetherVehicleCanEndAtTrip(v, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                            if (cplex.getValue(varVehicleArc[v][i][depot.getIndexOfDepotAsEndingPoint()]) >0.999 ) {
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
                            if (cplex.getValue(varVehicleArc[v][i][j]) >0.999) {
                                System.out.println("vehicle_" + v + "perform arc " + i + "_" + j + "=" + cplex.getValue(varVehicleArc[v][i][j]));
                            }

                    }
                }
            }
//
        }
        // here is to print out the solution of the driver solution
        for (int d = 0; d < nbDriver; d++) {
            Driver driver = instance.getDriver(d);
            for (int i = 0; i < nbTrip; i++) {
                if (instance.whetherDriverCanStartWithTrip(d, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                            if (cplex.getValue(varDriverSourceDepotArc[d][k]) > 0.9999) {
                                // System.out.println("driver" + d + "_source_" + depot.getIndexOfDepotAsStartingPoint() + "=1.0");
                            }
                            if (cplex.getValue(varDriverArc[d][depot.getIndexOfDepotAsStartingPoint()][i]) > 0.9999) {
                                //System.out.println("driver" + d + "_" + depot.getIndexOfDepotAsStartingPoint() + "_" + i + "=1.0");
                            }
                        }
                    }
                }

                for (int k = 0; k < nbDepot; k++) {
                    Depot depot = instance.getDepot(k);
                    if (instance.whetherDriverCanEndAtTrip(d, i)) {
                        for (int k1 = 0; k1 < nbDepot; k1++) {
                            Depot depot1 = instance.getDepot(k1);
                            if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                                if (cplex.getValue(varDriverDepotSinkArc[d][k]) > 0.9999) {
                                    // System.out.println("driver" + d + "_" + depot.getIndexOfDepotAsEndingPoint() + "_sink=1.0");
                                }
                                if (cplex.getValue(varDriverArc[d][i][depot.getIndexOfDepotAsEndingPoint()]) > 0.9999) {
                                    //System.out.println("driver" + d + "_"+i+ "_"+ depot.getIndexOfDepotAsEndingPoint()  + "=1.0");
                                }
                            }
                        }

                    }

                }

                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (cplex.getValue(varDriverArc[d][i][j]) > 0.9999) {
                            //System.out.println("driver" + d + "_" + i + "_" + j + "=" + cplex.getValue(varDriverArc[d][i][j]));
                        }

                        if (cplex.getValue(varChangeOver[d][i][j]) > 0.9999) {
                            System.out.println("ChangeOver happen between trip_" + i + " and trip_" + j + " for driver" + "_" + d);

                        }

                    }
                }
            }
        }

        // here is to print out which vehicle is leading during the combine part
        for (int i = 0; i < nbTrip; i++) {
            for (int j = 0; j < nbTrip; j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    if (instance.getTrip(i).getNbVehicleNeed() > 1) {
                        for (int v = 0; v < nbVehicle; v++) {
                            Vehicle vehicle = instance.getVehicle(v);
                            if (cplex.getValue(varVehicleArc[v][i][j]) > 0.9999) {
                                if (cplex.getValue(varLeading[v][i]) > 0.9999) {
                                    System.out.println("vehicle" + "_" + v + " leading Trip" + "_" + i);
                                }
                            }
                        }
                    }
                }
            }

        }

        //Here is to print out which driver is driving the trip
        for (int i = 0; i < nbTrip; i++) {
            for (int d = 0; d < nbDriver; d++) {
                Driver driver = instance.getDriver(d);
                if (instance.whetherDriverCanEndAtTrip(d, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                            if (cplex.getValue(varDriverArc[d][i][depot.getIndexOfDepotAsEndingPoint()]) == 1) {
                                if (cplex.getValue(varDriving[d][i]) > 0.9999) {
                                    System.out.println("driver" + "_" + d + " drive" + " Trip" + "_" + i);
                                    if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                                        System.out.println("driver_" + d + "is driving in combined trip_" + i);
                                    }
                                }
                            }
                        }
                    }
                }
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (cplex.getValue(varDriverArc[d][i][j]) > 0.9999) {
                            if (cplex.getValue(varDriving[d][i]) > 0.9999) {
                                 System.out.println("driver" + "_" + d + " drive" + " Trip" + "_" + i);
                                if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                                    System.out.println("driver_" + d + " drive combined trip_" + i);
                                }
                            }
                        }


                    }
                }
            }
        }


        //2024.9.17
        for (int d = 0; d < nbDriver; d++) {
            if (cplex.getValue(varStartingTimeOfDriverSchedule[d]) > 0.99) {
                System.out.println("driver schedule_" + d + " start time " + varStartingTimeOfDriverSchedule[d]);
            }
        }

        for (int i = 0; i < nbTrip; i++) {
            if (cplex.getValue(varTripStartTime[i]) >= 0) {
                System.out.println("trip_" + i + " start time unit_" + cplex.getValue(varTripStartTime[i]));
            }
        }

        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int duration_i = instance.getDuration(instance.getTrip(i).getIdOfStartCity(), instance.getTrip(i).getIdOfEndCity());
                int nbVehicleNeed_i=instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {

                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int maxWaitingTime = Math.round(instance.getTrip(j).getLatestDepartureTime()
                                - instance.getTrip(i).getEarliestDepartureTime() - duration_i);
                        int nbVehicleNeed_j=instance.getTrip(j).getNbVehicleNeed();
                        if (maxWaitingTime >= minPlanTime) {
                            if (cplex.getValue(varVehicleArc[v][i][j]) > 0.99) {
                                if(nbVehicleNeed_i==1||nbVehicleNeed_j==1) {
                                    if (cplex.getValue(varVehicleWaitingTimeForNotAllComb[i][j]) > 0.99) {
                                        System.out.println("vehicle_" + v + " waiting _" + cplex.getValue(varVehicleWaitingTimeForNotAllComb[i][j]) + "time between trip_" + i + "_" + j);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                int duration_i = instance.getDuration(instance.getTrip(i).getIdOfStartCity(), instance.getTrip(i).getIdOfEndCity());
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                        int maxWaitingTime = (instance.getTrip(j).getLatestDepartureTime()
                                - instance.getTrip(i).getEarliestDepartureTime() - duration_i);
                        if (cplex.getValue(varVehicleArc[v][i][j]) > 0.99) {
                            if (maxWaitingTime >= minPlanTime) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    if (cplex.getValue(varVehicleWaitingTimeForAllComb[v][i][j]) > 0.99) {
                                        System.out.println("Vehicle_" + v + " waiting _" + cplex.getValue(varVehicleWaitingTimeForAllComb[v][i][j]) + "time between trip_" + i + "_" + j);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                int duration_i = instance.getDuration(instance.getTrip(i).getIdOfStartCity(), instance.getTrip(i).getIdOfEndCity());
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int maxWaitingTime = (instance.getTrip(j).getLatestDepartureTime()
                                - instance.getTrip(i).getEarliestDepartureTime() - duration_i);
                        if (cplex.getValue(varDriverArc[d][i][j]) > 0.99) {
                            if (maxWaitingTime >= minPlanTime) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
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

    //************Below here is to add new constraints **********************2023.11.24
    //for short connection go out
    private void cnstUseVehicleOrder() throws IloException {
        for (int v = 0; v < nbVehicle - 1; v++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            expr.addTerm(varVehicleUse[v + 1], 1);
            expr.addTerm(varVehicleUse[v], -1);
            IloRange constraint = cplex.addLe(expr, 0);
        }
    }

    private void cnstUseDriverOrder() throws IloException {
        for (int d = 0; d < nbDriver - 1; d++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            expr.addTerm(varDriverUse[d + 1], 1);
            expr.addTerm(varDriverUse[d], -1);
            IloConstraint constraint = cplex.addLe(expr, 0);
        }
    }


    //***********Above here is to add new constraints************************2023.11..24


    //this constraint is about the vehicle change from combined trip to a normal trip, which is (30)
    //either driver drives the leading vehicle in combined trip enter a normal trip; or changeover happen
    private void cnstVehicleChangeFromCombinedToNormalTrip() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                if (instance.getTrip(j).getNbVehicleNeed() == 1) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(varDriving[d][i], 1);
                                    expr.addTerm(varLeading[v][i], 1);
                                    expr.addTerm(varDriverArc[d][i][j], 1);
                                    expr.addTerm(varVehicleArc[v][i][j], -1);
                                    expr.addTerm(varChangeOver[d][i][j], -1);
                                    IloRange constraint = cplex.addLe(expr, 2);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    //add NewConstraints 0619
    private void cnstChangeVehicleFromCombinedToCombinedTripsCorrespoindingVerLeadingOrder() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() >= 2) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(varDriverArc[d][i][j], 1);
                                    expr.addTerm(varVehicleArc[v][i][j], 1);
                                    expr.addTerm(varDriving[d][j], 1);
                                    expr.addTerm(varLeading[v][j], 1);
                                    expr.addTerm(varLeading[v][i], -1);
                                    expr.addTerm(varChangeOver[d][i][j], -1);
                                    IloRange constraint = cplex.addLe(expr, 3, "chCo&Co" + d);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //add NewConstraint 0618
    public void cnstChangeOverDuringCombineToCombineWhenNotSuccessiveToVehicle() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() >= 1.99) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(varDriverArc[d][i][j], 1);
                                    expr.addTerm(varDriving[d][i], 1);
                                    expr.addTerm(varLeading[v][j], 1);// it doesnt matter we change li to lj
                                    expr.addTerm(varVehicleArc[v][i][j], -1);
                                    expr.addTerm(varChangeOver[d][i][j], -1);
                                    IloRange constraint = cplex.addLe(expr, 2, "chNo&CoNotSuccessiveToVehicle" + d);
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    // this constraint  is about changing vehicles between two combined trips, which is constraint (28)
    // when previous combined lead by v, the latter combined not lead by v, changeover of driver happens
    private void cnstChangeVehicleFromCombinedToCombinedTrips() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() >= 2) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(varDriverArc[d][i][j], 1);
                                    expr.addTerm(varVehicleArc[v][i][j], 1);
                                    expr.addTerm(varDriving[d][j], 1);
                                    expr.addTerm(varLeading[v][i], 1);
                                    expr.addTerm(varLeading[v][j], -1);
                                    expr.addTerm(varChangeOver[d][i][j], -1);
                                    IloRange constraint = cplex.addLe(expr, 3, "chCo&Co" + d);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // this constraint  is about changing vehicles between normal trip (one vehicle) to combined trip (several vehicles),
    // either driver drive the leading in v, or changeover occurs  which in paper is (27)
    private void cnstChangeVehicleFromNormalToCombinedTrips() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() == 1) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)
//                                        && instance.getConnectionTime(i, j) >= instance.getShortConnectionTimeForVehicle()
//                                        && instance.getConnectionTime(i, j) >= instance.getShortConnectionTimeForDriver()
                                ) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(varDriverArc[d][i][j], 1);
                                    expr.addTerm(varVehicleArc[v][i][j], 1);
                                    expr.addTerm(varDriving[d][j], 1);
                                    expr.addTerm(varLeading[v][j], -1);
                                    expr.addTerm(varChangeOver[d][i][j], -1);
                                    IloRange constraint = cplex.addLe(expr, 2, "chNo&Co" + d);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // this constraint is about changing vehicles between normal trip   which is (26) //change 0619
    // it describes if drive leave or separates with his original vehicle cause to changeover
    private void cnstChangeVehicleBetweenNormalTrips() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varDriverArc[d][i][j], 1);
                        expr.addTerm(varDriving[d][i], 1);
                        expr.addTerm(varChangeOver[d][i][j], -1);
                        for (int v = 0; v < nbVehicle; v++) {
                            expr.addTerm(varVehicleArc[v][i][j], -1);
                        }
                        IloRange constraint = cplex.addLe(expr, 1, "chNo&No" + d);
                    }
                }
            }
        }
    }


    // this changeOver constraint is about the work status second constraint, which is (25) in paper
    private void cnstWorkStatusSecond() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varDriverArc[d][i][j], 1);
                        expr.addTerm(varDriving[d][j], 1);
                        expr.addTerm(varChangeOver[d][i][j], -1);
                        expr.addTerm(varDriving[d][i], -1);
                        IloRange constraint = cplex.addLe(expr, 1, "statusChangeover_S" + d);
                    }
                }
            }
        }
    }

    //start here, we consider the changeover
    // this constraint is about the work status, which is (24) in paper
    private void cnstWorkStatusFirst() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varDriverArc[d][i][j], 1);
                        expr.addTerm(varDriving[d][i], 1);
                        expr.addTerm(varChangeOver[d][i][j], -1);
                        expr.addTerm(varDriving[d][j], -1);
                        IloRange constraint = cplex.addLe(expr, 1, "staCh_F" + d);
                    }
                }
            }
        }

    }

    // this constraint is the mix about the driving variable and driver arc, which is (21) in paper
    private void cnstDrivingAndArc() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            Driver driver = instance.getDriver(d);
            for (int i = 0; i < nbTrip; i++) {
                IloLinearNumExpr expr = cplex.linearNumExpr();
                expr.addTerm(varDriving[d][i], 1);
                if (instance.whetherDriverCanEndAtTrip(d, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                            expr.addTerm(varDriverArc[d][i][depot.getIndexOfDepotAsEndingPoint()], -1);
                        }
                    }

                }
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j))
                        expr.addTerm(varDriverArc[d][i][j], -1);
                }
                IloRange constraint = cplex.addLe(expr, 0, "D" + d + "Trip" + i);
            }
        }
    }


    // this constraint is about the driving time, which is (19) in the paper
    private void cnstDrivingTime() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int i = 0; i < nbTrip; i++) {
                Trip trip = instance.getTrip(i);
//                int departureTime = trip.getEarliestDepartureTime();
//                int arrivalTime = trip.getLatestDepartureTime();
                int duration=trip.getDuration();
                expr.addTerm(varDriving[d][i], duration);
            }
            IloRange constraint = cplex.addLe(expr, instance.getMaxDrivingTime(), "D" + d + "drivingTime");
        }
    }

    // this constraint is about only one person drives the vehicle, which is (18) in the paper
    private void cnstDriving() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int d = 0; d < nbDriver; d++) {
                expr.addTerm(varDriving[d][i], 1);
            }
            IloRange constraint = cplex.addEq(expr, 1, "dri_" + i);
        }
    }

    // this constraint is about at least one person in each trip, which is (17) in the paper
    private void cnstPassenger() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int d = 0; d < nbDriver; d++) {
                Driver driver = instance.getDriver(d);
                if (instance.whetherDriverCanEndAtTrip(d, i)) {
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
        for (int d = 0; d < nbDriver; d++) {
            Driver driver = instance.getDriver(d);
            for (int i = 0; i < nbTrip; i++) {
                IloLinearNumExpr exprFlowEnter = cplex.linearNumExpr();
                IloLinearNumExpr exprFlowOut = cplex.linearNumExpr();
                //first case: Start with trip i
                if (instance.whetherDriverCanStartWithTrip(d, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                            exprFlowEnter.addTerm(varDriverArc[d][depot.getIndexOfDepotAsStartingPoint()][i], 1);
                        }
                    }
                }
                //second case: End at trip i
                if (instance.whetherDriverCanEndAtTrip(d, i)) {
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

    // this is the constraint (9) for whether v leads in combined trip i limited by whether there is a v perform trip i
    private void cnstRelationshipOfVehicleLeadingAndTripArc() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            if (instance.getTrip(i).getNbVehicleNeed() > 1) {

                for (int v = 0; v < nbVehicle; v++) {
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(varLeading[v][i], 1);
                    //special case
                    if (instance.whetherVehicleCanEndAtTrip(v, i)) {
                        for (int k = 0; k < nbDepot; k++) {
                            Depot depot = instance.getDepot(k);
                            int indexOfDepotAsEndingPoint = depot.getIndexOfDepotAsEndingPoint();
                            if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                                expr.addTerm(varVehicleArc[v][i][indexOfDepotAsEndingPoint], -1);
                            }
                        }

                    }
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j))
                            expr.addTerm(varVehicleArc[v][i][j], -1);
                    }
                    IloRange constraint = cplex.addLe(expr, 0, "LeadingTrip" + i + "V" + v);
                }
            }
        }
    }


    // this is the constraint (8) for those combined trips, in each only one leading vehicle
    private void cnstOneLeadingVehicleInCombTrip() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            if (instance.getTrip(i).getNbVehicleNeed() > 1) {
                IloLinearNumExpr expr = cplex.linearNumExpr();
                for (int v = 0; v < nbVehicle; v++) {
                    Vehicle vehicle = instance.getVehicle(v);
                    expr.addTerm(varLeading[v][i], 1);
                }
                IloRange constraint = cplex.addEq(expr, 1, "CombTrip" + i + "leadV");
            }
        }

    }


    // this constraint is for combine number of vehicle in each trip, which is (7) in paper
    private void cnstnbVehicleInTrip() throws IloException {
        // count the number of vehicles go out from trip i,
        // in each trip the total number of vehicle arcs equals required number of vehicles
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int v = 0; v < nbVehicle; v++) {
                Vehicle vehicle = instance.getVehicle(v);
                //first case: some vehicle can leave i enter the depot
                // then the vehicle go out to depot, then we need to know where is i end to obtain the out arc
                if (instance.whetherVehicleCanEndAtTrip(v, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        int indexOfDepotAsEndingPoint = depot.getIndexOfDepotAsEndingPoint();
                        if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                            expr.addTerm(varVehicleArc[v][i][indexOfDepotAsEndingPoint], 1);
                        }
                    }

                }
                // second case some vehicle leave i to another trip j behind
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j))
                        expr.addTerm(varVehicleArc[v][i][j], 1);//------here we need assign the number of vehicles in each trip
                }
            }
            IloRange constraint = cplex.addEq(expr, instance.getTrip(i).getNbVehicleNeed(), "Trip" + i + "nbV");
        }
    }


    // this FlowConstraint about the trip, which is constraint (6) in paper
    private void cnstVehicleFlowConstraintForTrip() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            Vehicle vehicle = instance.getVehicle(v);
            for (int i = 0; i < nbTrip; i++) {
                IloLinearNumExpr exprFlowEnter = cplex.linearNumExpr();
                IloLinearNumExpr exprFlowOut = cplex.linearNumExpr();
                //Starting with trip i
                if (instance.whetherVehicleCanStartWithTrip(v, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                            exprFlowEnter.addTerm(varVehicleArc[v][depot.getIndexOfDepotAsStartingPoint()][i], 1);
                        }
                    }
                }
                //Ending with trip i
                if (instance.whetherVehicleCanEndAtTrip(v, i)) {
                    for (int k = 0; k < nbDepot; k++) {
                        Depot depot = instance.getDepot(k);
                        if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                            exprFlowOut.addTerm(varVehicleArc[v][i][depot.getIndexOfDepotAsEndingPoint()], 1);
                        }
                    }
                }
                //general case
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(j, i))
                        exprFlowEnter.addTerm(varVehicleArc[v][j][i], 1);
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j))
                        exprFlowOut.addTerm(varVehicleArc[v][i][j], 1);

                }
                IloConstraint constraint = cplex.addEq(exprFlowOut, exprFlowEnter, "V" + v + "Trip" + i + "Flow");
            }
        }
    }


    /**
     * new  part about the vehicle and driver use and depot flow constraint
     */
    //__________________________________________________________________________________________________________________

    //constraint (2) for vehicle use and vehicle source and depot arc
    private void cnstRelationshipOfVehicleUseAndSourceDepot() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            IloLinearNumExpr exprVehicleUse = cplex.linearNumExpr();
            IloLinearNumExpr exprVehicleSourceDepot = cplex.linearNumExpr();
            exprVehicleUse.addTerm(varVehicleUse[v], 1);
            for (int k = 0; k < nbDepot; k++) {
                Depot depot = instance.getDepot(k);
                exprVehicleSourceDepot.addTerm(varVehicleSourceDepotArc[v][k], 1);
            }
            IloConstraint constraint = cplex.addEq(exprVehicleUse, exprVehicleSourceDepot, "V" + v + "Use&SoDp");
        }
    }

    //constraint (3) for the vehicle source depot and depot sink arc
    private void cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotSinkArc() throws IloException {
        for (int k = 0; k < nbDepot; k++) {
            Depot depot = instance.getDepot(k);
            int indexOfDepotInModel = depot.getIndexOfDepotAsStartingPoint();
            IloLinearNumExpr exprVehicleSourceDepot = cplex.linearNumExpr();
            IloLinearNumExpr exprVehicleDepotSink = cplex.linearNumExpr();
            for (int v = 0; v < instance.getMaxNbVehicleAvailable(); v++) {
                exprVehicleSourceDepot.addTerm(varVehicleSourceDepotArc[v][k], 1);
                exprVehicleDepotSink.addTerm(varVehicleDepotSinkArc[v][k], 1);
            }
            IloConstraint constraint = cplex.addEq(exprVehicleSourceDepot, exprVehicleDepotSink, "Vehicle Dp" + k + "So&Si");

        }
    }

    //constraint (4) flow constraint for the depot and the source
    private void cnstRelationshipOfVehicleSourceDepotArcAndVehicleDepotTripArc() throws IloException {
        for (int v = 0; v < instance.getMaxNbVehicleAvailable(); v++) {
            for (int k = 0; k < instance.getNbDepots(); k++) {
                IloLinearNumExpr exprVehicleSourceDepot = cplex.linearNumExpr();
                IloLinearNumExpr exprVehicleDepotTrip = cplex.linearNumExpr();

                exprVehicleSourceDepot.addTerm(varVehicleSourceDepotArc[v][k], 1);

                Depot depot = instance.getDepot(k);
                int indexOfDepotAsStartingPoint = depot.getIndexOfDepotAsStartingPoint();
                for (int j = 0; j < instance.getNbTrips(); j++) {

                    Trip trip = instance.getTrip(j);
                    if (trip.getIdOfStartCity() == depot.getIdOfCityAsDepot()) {//only when the trip start from the depot
                        exprVehicleDepotTrip.addTerm(varVehicleArc[v][indexOfDepotAsStartingPoint][j], 1);
                    }
                }

                IloConstraint constraint = cplex.addEq(exprVehicleSourceDepot, exprVehicleDepotTrip, "V" + v + "Dp" + k + "SoFlow");

            }

        }

    }

    //constraint (5) flow constraint for the depot and sink
    private void cnstRelationshipOfVehicleDepotSinkArcAndVehicleDepotTripArc() throws IloException {
        for (int v = 0; v < instance.getMaxNbVehicleAvailable(); v++) {
            for (int k = 0; k < instance.getNbDepots(); k++) {
                IloLinearNumExpr exprVehicleDepotSink = cplex.linearNumExpr();
                IloLinearNumExpr exprVehicleTripDepot = cplex.linearNumExpr();
                exprVehicleDepotSink.addTerm(varVehicleDepotSinkArc[v][k], 1);
                Depot depot = instance.getDepot(k);
                int indexOfDepotAsEndingPoint = depot.getIndexOfDepotAsEndingPoint();
                for (int i = 0; i < instance.getNbTrips(); i++) {
                    Trip trip = instance.getTrip(i);
                    if (trip.getIdOfEndCity() == depot.getIdOfCityAsDepot()) {//only when the trip start from the depot
                        exprVehicleTripDepot.addTerm(varVehicleArc[v][i][indexOfDepotAsEndingPoint], 1);
                    }
                }

                IloConstraint constraint = cplex.addEq(exprVehicleDepotSink, exprVehicleTripDepot, "V" + v + "Dp" + k + "SiFlow");

            }

        }

    }
    //_______________________________________________________________add constraint of driver part

    //constraint (13) for driver use and driver source and depot arc
    private void cnstRelationshipOfDriverUseAndSourceDepot() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            IloLinearNumExpr exprDriverUse = cplex.linearNumExpr();
            IloLinearNumExpr exprDriverSourceDepot = cplex.linearNumExpr();
            exprDriverUse.addTerm(varDriverUse[d], 1);
            for (int k = 0; k < nbDepot; k++) {
                exprDriverSourceDepot.addTerm(varDriverSourceDepotArc[d][k], 1);
            }
            IloConstraint constraint = cplex.addEq(exprDriverUse, exprDriverSourceDepot, "D" + d + "Use&SoDp");
        }
    }

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
                        for (int v = 0; v < nbVehicle; v++) {
                            expr.addTerm(varVehicleArc[v][i][j], -M_Waiting);
                        }
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
                        int M_Waiting = duration_i+instance.getTrip(i).getLatestDepartureTime()-instance.getTrip(j).getEarliestDepartureTime()+1;
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varVehicleWaitingTimeForNotAllComb[i][j], 1);
                        expr.addTerm(varTripStartTime[j], -1);
                        expr.addTerm(varTripStartTime[i], 1);
                        for (int v = 0; v < nbVehicle; v++) {
                            expr.addTerm(varVehicleArc[v][i][j], M_Waiting);
                        }
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
                        for (int v = 0; v < nbVehicle; v++) {
                            expr.addTerm(varVehicleArc[v][i][j], -minPlanTime);
                        }
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
                        for (int v = 0; v < nbVehicle; v++) {
                            expr.addTerm(varVehicleArc[v][i][j], -maxWaitingTime);
                        }
                        IloConstraint constraint = cplex.addLe(expr, 0, "cnstVehicleWaitingTimeForNotAllCombinedArc_1_" + i + "_" + j);
                    }
                }
            }
        }
    }


    //(36)-1
    private void cnstVehicleWaitingTimeForAllCombinedArc_M1() throws IloException {
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
                            expr.addTerm(varVehicleWaitingTimeForAllComb[v][i][j], 1);
                            expr.addTerm(varTripStartTime[j], -1);
                            expr.addTerm(varTripStartTime[i], 1);
                            expr.addTerm(varVehicleArc[v][i][j], -M_Waiting);
                            IloConstraint constraint = cplex.addGe(expr, -duration_i -M_Waiting, "cnstVehicleWaitingTimeForAllCombinedArc_M1_" + i + "_" + j);

                        }
                    }
                }
            }
        }
    }


    //(36)-2
    private void cnstVehicleWaitingTimeForAllCombinedArc_M2() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if ((nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) && instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int duration_i = instance.getTrip(i).getDuration();
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            int M_Waiting = duration_i+instance.getTrip(i).getLatestDepartureTime()-instance.getTrip(j).getEarliestDepartureTime()+1;
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varVehicleWaitingTimeForAllComb[v][i][j], 1);
                            expr.addTerm(varTripStartTime[j], -1);
                            expr.addTerm(varTripStartTime[i], 1);
                            expr.addTerm(varVehicleArc[v][i][j], M_Waiting);
                            IloConstraint constraint = cplex.addLe(expr, -duration_i + M_Waiting, "cnstVehicleWaitingTimeForAllCombinedArc_M2_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }


    //(37)-1
    private void cnstVehicleWaitingTimeForAllCombinedArc_1() throws IloException {
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
                            expr.addTerm(varVehicleWaitingTimeForAllComb[v][i][j], 1);
                            expr.addTerm(varVehicleArc[v][i][j], -minPlanTime);
                            IloConstraint constraint = cplex.addGe(expr, 0, "cnstVehicleWaitingTimeForAllCombinedArc_1_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }


    //(37)-2
    private void cnstVehicleWaitingTimeForAllCombinedArc_2() throws IloException {
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
                            expr.addTerm(varVehicleWaitingTimeForAllComb[v][i][j], 1);
                            expr.addTerm(varVehicleArc[v][i][j], -maxWaitingTime);
                            IloConstraint constraint = cplex.addLe(expr, 0, "cnstVehicleWaitingTimeForAllCombinedArc_2_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }

    // here is after copy
    //(38)-1
    private void cnstDriverWaitingTime_M1() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
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


    //(38)-2
    private void cnstDriverWaitingTime_M2() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int duration_i = instance.getTrip(i).getDuration();
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            int M_Waiting = instance.getTrip(i).getLatestDepartureTime()+duration_i-instance.getTrip(j).getEarliestDepartureTime()+1;
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


    //(39)-1
    private void cnstDriverWaitingTime_1() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
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

    //(39)-2
    private void cnstDriverWaitingTime_2() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
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

    //(40)
    private void cnstDetectVehicleShortConnectionArc() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                        int vehicleShortTime = instance.getShortConnectionTimeForVehicle();
                        int diffValue = instance.getMinPlanTurnTime() - vehicleShortTime;
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varVehicleWaitingTimeForAllComb[v][i][j], 1);
                        expr.addTerm(varVehicleArc[v][i][j], -vehicleShortTime);
                        expr.addTerm(varWhetherVehicleShortArc[i][j], -diffValue);
                        IloConstraint constraint = cplex.addGe(expr, 0, "DetectVehicleShortConnection_" + v + "_" + i + "_" + j);
                    }
                }
            }
        }

    }

    //(41)
    private void cnstKeepSameVehiclesDuringShortConnectionInCombinedArc() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varVehicleArc[v][i][j], 1);
                        expr.addTerm(varWhetherVehicleShortArc[i][j], 1);
                        for (int u = 0; u < nbVehicle; u++) {
                            if (u != v) {
                                expr.addTerm(varVehicleArc[u][i][j], 1);
                            }
                        }
                        IloConstraint constraint = cplex.addLe(expr, 1);
                    }
                }
            }
        }
    }


    //(42)
    private void cnstDetectDriverShortConnectionArc() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
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

    //(43)
    private void cnstProhibitChangeVehicleDuringDriverShortConnection() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varWhetherDriverShortArc[i][j], 1);
                        expr.addTerm(varDriverArc[d][i][j], 1);
                        for (int v = 0; v < nbVehicle; v++) {
                            expr.addTerm(varVehicleArc[v][i][j], -1);
                        }
                        IloConstraint constraint = cplex.addLe(expr, 1);
                    }
                }
            }
        }
    }

    //(44)
    private void cnstStartingTimeOfSchedule() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
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
        for (int d = 0; d < nbDriver; d++) {
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
        for (int d = 0; d < nbDriver; d++) {
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
        for (int v = 0; v < nbVehicle; v++) {

            obj.addTerm(varVehicleUse[v], instance.getFixedCostForVehicle());
        }

        //cost for using driver
        for (int d = 0; d < nbDriver; d++) {

            obj.addTerm(varDriverUse[d], instance.getFixedCostForDriver());
        }

        //idle cost for vehicle for two task are combined cased
        for (int v = 0; v < nbVehicle; v++) {
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
                                obj.addTerm(varVehicleWaitingTimeForAllComb[v][i][j], idleTimeCost_v);
                            }
                        }
                    }
                }
            }
        }

        //idle cost for vehicle for two task are combined cased
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
        // cost of crew in the object function;
        /**attention the cost ; we need to know the cost for the whole path
         */
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
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

        //cost of the changeover part here is what I need to modify
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {

                        obj.addTerm(varChangeOver[d][i][j], instance.getCostForChangeOver(i, j));
                    }
                }

            }
        }

        IloObjective objective11 = cplex.addMinimize(obj);
    }

    // This part is the Decision Variables
    private void defineDecisionVariables() throws IloException {
        //初始化所有决定变量
        //1.the dimension of the aircraft variables
        varVehicleArc = new IloNumVar[nbVehicle][nbNodes][nbNodes];
        /**
         * add new variable about using vehicle and driver or not
         * */
        varVehicleUse = new IloNumVar[nbVehicle];
        varDriverUse = new IloNumVar[nbDriver];

        /**
         * add new variable about describing depot flow constraint
         */
        varVehicleSourceDepotArc = new IloNumVar[nbVehicle][nbDepot];
        varVehicleDepotSinkArc = new IloNumVar[nbVehicle][nbDepot];
        //________________________________________________________________________________________this is for source and sink for the vehicle
        varDriverSourceDepotArc = new IloNumVar[nbDriver][nbDepot];
        varDriverDepotSinkArc = new IloNumVar[nbDriver][nbDepot];
//        //_________________________________________________________________________________________this is for source and sink for the driver

        //1.For the crew we also use arc form; and we add the variable to describe whether the driver is driving; whether the vehicle is leading;
        //whether the driver change his original place
        varDriverArc = new IloNumVar[nbDriver][nbNodes][nbNodes];
        varDriving = new IloNumVar[nbDriver][nbTrip];
        varLeading = new IloNumVar[nbVehicle][nbTrip];
        varChangeOver = new IloNumVar[nbDriver][nbTrip][nbTrip];


        //********************************************************* The following here are the new variables to deal with time window 2024.9.12 -20224.9.17 ****************
        varTripStartTime = new IloNumVar[nbTrip];
        varDriverWaitingTime = new IloNumVar[nbDriver][nbTrip][nbTrip];
        varVehicleWaitingTimeForNotAllComb = new IloNumVar[nbTrip][nbTrip];
        varVehicleWaitingTimeForAllComb = new IloNumVar[nbVehicle][nbTrip][nbTrip];


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
            for (int v = 0; v < nbVehicle; v++) {
                varVehicleUse[v] = cplex.numVar(0, 1, "x_v_" + v);
            }
            //variable of driver use --type and range
            for (int d = 0; d < nbDriver; d++) {
                varDriverUse[d] = cplex.numVar(0, 1, "x_d_" + d);
            }
            /**
             * add the new variable for the depot flow
             */

            for (int v = 0; v < nbVehicle; v++) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    varVehicleSourceDepotArc[v][p] = cplex.numVar(0, 1, "x_v" + v + "_source_" + p);
                }
            }
            for (int v = 0; v < nbVehicle; v++) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    varVehicleDepotSinkArc[v][k] = cplex.numVar(0, 1, "x_v" + v + "_" + k + "_sink");
                }
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
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbNodes; i++) {
                    for (int j = 0; j < nbNodes; j++) {
                        if (i < this.nbTrip && j < this.nbTrip) {
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                varVehicleArc[v][i][j] = cplex.numVar(0, 1, "x_v" + v + "_" + i + "_" + j);
                            }
                        } else {
                            varVehicleArc[v][i][j] = cplex.numVar(0, 1, "x_v" + v + "_" + i + "_" + j);
                        }
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
            // variable for whether diver is driving the arc---- type and range
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    varDriving[d][i] = cplex.numVar(0, 1, "o_d" + d + "_" + i);
                }
            }
            // variable the type of variable whether leading the vehicle
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    varLeading[v][i] = cplex.numVar(0, 1, "l_v" + v + "_" + i);
                }
            }
            // the type of variable whether changeover happen to the driver
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            varChangeOver[d][i][j] = cplex.numVar(0, 1, "y_d" + d + "_" + i + "_" + j);
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

            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                        if (maxWaitingTime >= minPlanTime) {
                            varVehicleWaitingTimeForNotAllComb[i][j] = cplex.numVar(0, maxWaitingTime, "t_V_" + i + "_" + j);
                        }
                    }
                }
            }

            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                varVehicleWaitingTimeForAllComb[v][i][j] = cplex.numVar(0, maxWaitingTime, "t_v_" + v + "_" + i + "_" + j);
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
            for (int v = 0; v < nbVehicle; v++) {
                varVehicleUse[v] = cplex.boolVar("x_v_" + v);
            }
            //variable of driver use --type and range
            for (int d = 0; d < nbDriver; d++) {
                varDriverUse[d] = cplex.boolVar("x_d_" + d);
            }
            /**
             * add the new variable for the depot flow
             */
            for (int v = 0; v < nbVehicle; v++) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    varVehicleSourceDepotArc[v][p] = cplex.boolVar("x_v" + v + "_source_" + p);
                }
            }
            for (int v = 0; v < nbVehicle; v++) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    varVehicleDepotSinkArc[v][k] = cplex.boolVar("x_v" + v + "_" + k + "_sink");
                }
            }
            for (int d = 0; d < nbDriver; d++) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    varDriverSourceDepotArc[d][p] = cplex.boolVar("x_d" + d + "_source_" + p);
                }
            }
            for (int d = 0; d < nbDriver; d++) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    varDriverDepotSinkArc[d][k] = cplex.boolVar("x_d" + d + "_" + k + "_sink");
                }
            }
            //___________________________________________________________________________________here is all the new variable
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbNodes; i++) {
                    for (int j = 0; j < nbNodes; j++) {
                        if (i < this.nbTrip && j < this.nbTrip) {
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                varVehicleArc[v][i][j] = cplex.boolVar("x_v" + v + "_" + i + "_" + j);
                            }
                        } else {
                            varVehicleArc[v][i][j] = cplex.boolVar("x_v" + v + "_" + i + "_" + j);
                        }

                    }
                }
            }
            // variables for driver pass which arcs---- type and range
            for (int d = 0; d < nbDriver; d++) {
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
            // variable for whether diver is driving the arc---- type and range
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    varDriving[d][i] = cplex.boolVar("o_d" + d + "_" + i);
                }
            }
            // variable the type of variable whether leading the vehicle
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    varLeading[v][i] = cplex.boolVar("l_v" + v + "_" + i);
                }
            }
            // the type of variable whether changeover happen to the driver
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            varChangeOver[d][i][j] = cplex.boolVar("y_d" + d + "_" + i + "_" + j);
                        }
                    }
                }
            }
            //*********************************Here are the new variables to deal with the time window 2027.9.12-2024.9.19********************
            for (int i = 0; i < nbTrip; i++) {
                varTripStartTime[i] = cplex.intVar(startHorizon, endHorizon, "StartTime_t_i" + i);
            }

            for (int d = 0; d < nbDriver; d++) {
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

            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int maxWaitingTime = instance.getMaxWaitingTime(i, j);
                            if (maxWaitingTime >= minPlanTime) {
                                varVehicleWaitingTimeForAllComb[v][i][j] = cplex.intVar(0, maxWaitingTime, "t_v_" + v + i + "_" + j);
                            }
                        }
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
                varStartingTimeOfDriverSchedule[d] = cplex.intVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), "s^d_" + d);
            }

            for (int d = 0; d < nbDriver; d++) {
                varEndingTimeOfDriverSchedule[d] = cplex.intVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), "e^d_" + d);
            }

//            varVehicleArc[0][23][24].setLB(1); varVehicleArc[0][23][24].setUB(1);   // Trip 23 -> Trip 24
//
//            varVehicleArc[1][7][8].setLB(1); varVehicleArc[1][7][8].setUB(1);       // Trip 7 -> Trip 8
//            varVehicleArc[1][8][9].setLB(1); varVehicleArc[1][8][9].setUB(1);       // Trip 8 -> Trip 9
//            varVehicleArc[1][9][10].setLB(1); varVehicleArc[1][9][10].setUB(1);     // Trip 9 -> Trip 10
//            varVehicleArc[1][10][11].setLB(1); varVehicleArc[1][10][11].setUB(1);   // Trip 10 -> Trip 11
//            varVehicleArc[1][11][12].setLB(1); varVehicleArc[1][11][12].setUB(1);   // Trip 11 -> Trip 12
//            varVehicleArc[1][12][13].setLB(1); varVehicleArc[1][12][13].setUB(1);   // Trip 12 -> Trip 13
//
//            varVehicleArc[2][14][2].setLB(1); varVehicleArc[2][14][2].setUB(1);     // Trip 14 -> Trip 2
//            varVehicleArc[2][2][3].setLB(1); varVehicleArc[2][2][3].setUB(1);       // Trip 2 -> Trip 3
//            varVehicleArc[2][3][4].setLB(1); varVehicleArc[2][3][4].setUB(1);       // Trip 3 -> Trip 4
//            varVehicleArc[2][4][18].setLB(1); varVehicleArc[2][4][18].setUB(1);     // Trip 4 -> Trip 18
//            varVehicleArc[2][18][19].setLB(1); varVehicleArc[2][18][19].setUB(1);   // Trip 18 -> Trip 19
//
//            varVehicleArc[3][20][21].setLB(1); varVehicleArc[3][20][21].setUB(1);   // Trip 20 -> Trip 21
//            varVehicleArc[3][21][22].setLB(1); varVehicleArc[3][21][22].setUB(1);   // Trip 21 -> Trip 22
//            varVehicleArc[3][22][0].setLB(1); varVehicleArc[3][22][0].setUB(1);     // Trip 22 -> Trip 0
//            varVehicleArc[3][0][1].setLB(1); varVehicleArc[3][0][1].setUB(1);       // Trip 0 -> Trip 1
//            varVehicleArc[3][1][15].setLB(1); varVehicleArc[3][1][15].setUB(1);     // Trip 1 -> Trip 15
//            varVehicleArc[3][15][16].setLB(1); varVehicleArc[3][15][16].setUB(1);   // Trip 15 -> Trip 16
//            varVehicleArc[3][16][17].setLB(1); varVehicleArc[3][16][17].setUB(1);   // Trip 16 -> Trip 17
//            varVehicleArc[3][17][5].setLB(1); varVehicleArc[3][17][5].setUB(1);     // Trip 17 -> Trip 5
//            varVehicleArc[3][5][6].setLB(1); varVehicleArc[3][5][6].setUB(1);       //
//
//            varDriverArc[0][0][1].setLB(1); varDriverArc[0][0][1].setUB(1);         // Trip 0 -> Trip 1
//            varDriverArc[0][1][15].setLB(1); varDriverArc[0][1][15].setUB(1);       // Trip 1 -> Trip 15
//            varDriverArc[0][15][16].setLB(1); varDriverArc[0][15][16].setUB(1);     // Trip 15 -> Trip 16
//            varDriverArc[0][16][17].setLB(1); varDriverArc[0][16][17].setUB(1);     // Trip 16 -> Trip 17
//            varDriverArc[0][17][5].setLB(1); varDriverArc[0][17][5].setUB(1);       // Trip 17 -> Trip 5
//            varDriverArc[0][5][6].setLB(1); varDriverArc[0][5][6].setUB(1);         // Trip 5 -> Trip 6
//
//            varDriverArc[1][23][24].setLB(1); varDriverArc[1][23][24].setUB(1);     // Trip 23 -> Trip 24
//
//            varDriverArc[2][14][2].setLB(1); varDriverArc[2][14][2].setUB(1);       // Trip 14 -> Trip 2
//            varDriverArc[2][2][3].setLB(1); varDriverArc[2][2][3].setUB(1);         // Trip 2 -> Trip 3
//            varDriverArc[2][3][4].setLB(1); varDriverArc[2][3][4].setUB(1);         // Trip 3 -> Trip 4
//            varDriverArc[2][4][18].setLB(1); varDriverArc[2][4][18].setUB(1);       // Trip 4 -> Trip 18
//            varDriverArc[2][18][19].setLB(1); varDriverArc[2][18][19].setUB(1);     // Trip 18 -> Trip 19
//
//            varDriverArc[3][20][21].setLB(1); varDriverArc[3][20][21].setUB(1);     // Trip 20 -> Trip 21
//            varDriverArc[3][21][22].setLB(1); varDriverArc[3][21][22].setUB(1);     // Trip 21 -> Trip 22
//
//            varDriverArc[4][7][8].setLB(1); varDriverArc[4][7][8].setUB(1);         // Trip 7 -> Trip 8
//            varDriverArc[4][8][9].setLB(1); varDriverArc[4][8][9].setUB(1);         // Trip 8 -> Trip 9
//            varDriverArc[4][9][10].setLB(1); varDriverArc[4][9][10].setUB(1);       // Trip 9 -> Trip 10
//            varDriverArc[4][10][11].setLB(1); varDriverArc[4][10][11].setUB(1);     // Trip 10 -> Trip 11
//            varDriverArc[4][11][12].setLB(1); varDriverArc[4][11][12].setUB(1);     // Trip 11 -> Trip 12
//            varDriverArc[4][12][13].setLB(1); varDriverArc[4][12][13].setUB(1);     // Trip 12 -> Trip 13



        }


    }

    // here I need to rethink about it, I need to rewrite how to obtain the solution from cplex
    private Solution getSolutionFromCplex() throws IloException {
        Solution solution = new Solution(instance);
        //give vehicle path from the cplex solution
        for (int v = 0; v < instance.getMaxNbVehicleAvailable(); v++) {
            if (cplex.getValue(varVehicleUse[v]) > 0.999) {
                PathForVehicle pathForVehicle = new PathForVehicle(instance, v);
                Vehicle vehicle = this.instance.getVehicle(v);
                int firstTrip = -1;
                for (int k = 0; k < nbDepot; k++) {
                    Depot depot = instance.getDepot(k);
                    if (cplex.getValue(varVehicleSourceDepotArc[v][k]) > 0.99) {
                        for (int i = 0; i < nbTrip; i++) {
                            if (instance.whetherVehicleCanStartWithTrip(v, i)) {
                                if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                                    if (cplex.getValue(varVehicleArc[v][depot.getIndexOfDepotAsStartingPoint()][i]) > 0.99) {
                                        firstTrip = i;

                                        if (cplex.getValue(varTripStartTime[firstTrip]) >= 0) {
                                            int t = (int) Math.round(cplex.getValue(varTripStartTime[firstTrip]));
                                            TripWithStartingInfos tripWithStartingInfos = new TripWithStartingInfos(instance.getTrip(firstTrip), t);
                                            pathForVehicle.addTripInVehiclePath(tripWithStartingInfos);
                                        }


                                    }
                                }
                            }
                        }
                    }
                }
                // find the other trips for that vehicle
                int currentTrip = firstTrip;
                //System.out.println("currentTrip" + currentTrip);

                boolean findNextTrip = true;
                while (findNextTrip) {
                    findNextTrip = false;
                    for (int l = 0; l < this.nbTrip; l++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(currentTrip, l)) {
                            //System.out.println("check varVehicleArc" + cplex.getValue(varVehicleArc[v][currentTrip][l]));
                            if (cplex.getValue(varVehicleArc[v][currentTrip][l]) > 0.99) {
                                if (cplex.getValue(varTripStartTime[l]) >= 0) {
                                    int t = (int) Math.round(cplex.getValue(varTripStartTime[l]));
                                    TripWithStartingInfos tripWithStartingInfos_l = new TripWithStartingInfos(instance.getTrip(l), t);
                                    pathForVehicle.addTripInVehiclePath(tripWithStartingInfos_l);
                                    currentTrip = l;
                                    findNextTrip = true;
                                }


                            }
                        }
                    }

                }
                System.out.println(pathForVehicle);
                solution.addPathInSetForVehicle(pathForVehicle);

            }
        }
        //give crew path from cplex solution
        for (int d = 0; d < instance.getMaxNbDriverAvailable(); d++) {
            if (cplex.getValue(varDriverUse[d]) > 0.9999) {
                PathForDriver pathForDriver = new PathForDriver(instance, d);
                Driver driver = this.instance.getDriver(d);
                // find the first trip to the driver
                int firstTrip = -1;
                for (int i = 0; i < nbTrip; i++) {
                    boolean drivingStatus = false;
                    int idOfVehicleInTrip = Integer.MAX_VALUE;
                    if (instance.whetherDriverCanStartWithTrip(d, i)) {
                        for (int k = 0; k < nbDepot; k++) {
                            if (cplex.getValue(varDriverSourceDepotArc[d][k]) > 0.9999) {
                                Depot depot = instance.getDepot(k);
                                if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                                    if (cplex.getValue(varDriverArc[d][depot.getIndexOfDepotAsStartingPoint()][i]) > 0.9999) {
                                        firstTrip = i;

//                                        for (int v = 0; v < solution.getPathForVehicles().size(); v++) {
//                                            PathForVehicle pathForVehicle = solution.getPathForVehicles().get(v);
//                                            if (pathForVehicle.isPresentInPath(instance.getTrip(i))) {
//                                                idOfVehicleInTrip = instance.getVehicle(v).getIdOfVehicle();
//
//                                            }
//
//                                        }
                                        if (cplex.getValue(varDriving[d][i]) > 0.9999) {
                                            drivingStatus = true;
                                            for (int v = 0; v < solution.getPathForVehicles().size(); v++) {
                                                if (solution.getPathForVehicles().get(v).isPresentInPath(instance.getTrip(i))) {
                                                    if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                                                        if (cplex.getValue(varLeading[v][i]) > 0.999) {
                                                            System.out.println("when driver" + d + "is driving in combine trip "+i+" Leading by " +v);
                                                            idOfVehicleInTrip = v;
                                                        }
                                                    } else {
                                                        idOfVehicleInTrip = v;
                                                    }

                                                }
                                            }
                                        }else {
                                            drivingStatus=false;
                                            for (int v = 0; v < solution.getPathForVehicles().size(); v++) {
                                        if (solution.getPathForVehicles().get(v).isPresentInPath(instance.getTrip(i))) {
                                            if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                                                if (cplex.getValue(varLeading[v][i]) > 0.999) {
                                                    System.out.println("when driver" + d + "is passenger in combine trip "+i+" Leading by " +v);
                                                    idOfVehicleInTrip = v;
                                                }
                                            } else {
                                                idOfVehicleInTrip = v;
                                            }

                                        }
                                    }
                                        }


                                        if (cplex.getValue(varTripStartTime[i]) >= 0) {
                                            int t = (int) Math.round(cplex.getValue(varTripStartTime[i]));

                                            TripWithDriveInfos tripWithDriveInfos = new TripWithDriveInfos(instance.getTrip(i), idOfVehicleInTrip, drivingStatus, t);
                                            pathForDriver.addTripInDriverPath(tripWithDriveInfos);

                                        }


                                        //System.out.println("vehicle_"+v+"path whether contain combined Trip"+pathForVehicle.isPresentInPath(instance.getTrip(16)));

                                    }

                                }
                            }
                        }
                    }
                }
                //find the other trip for the driver
                int currentTrip = firstTrip;
                boolean findNextTrip = true;

                int idOfVehicleInTrip = Integer.MAX_VALUE;
                while (findNextTrip) {
                    findNextTrip = false;
                    for (int i = 0; i < nbTrip; i++) {
                        boolean drivingStatus = false;
                        if (instance.whetherHavePossibleArcAfterCleaning(currentTrip, i)) {
                            if (cplex.getValue(varDriverArc[d][currentTrip][i]) > 0.9999) {
                                if (cplex.getValue(varDriving[d][i]) > 0.9999) {
                                    drivingStatus = true;
                                    for (int v = 0; v < solution.getPathForVehicles().size(); v++) {
                                        if (solution.getPathForVehicles().get(v).isPresentInPath(instance.getTrip(i))) {
                                            if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                                                if (cplex.getValue(varLeading[v][i]) > 0.999) {
                                                    System.out.println("when driver" + d + "is driving in combine trip "+i+" Leading by " +v);
                                                    idOfVehicleInTrip = v;
                                                }
                                            } else {
                                                idOfVehicleInTrip = v;
                                            }

                                        }
                                    }

                                } else {//driver is working as passenger now
                                    for (int v = 0; v < solution.getPathForVehicles().size(); v++) {
                                        if (solution.getPathForVehicles().get(v).isPresentInPath(instance.getTrip(i))) {
                                            if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                                                if (cplex.getValue(varLeading[v][i]) > 0.99) {
                                                    System.out.println("when driver" + d + "is a passenger in combined trip"+i +" Leading by vehicle "+ v);
                                                    idOfVehicleInTrip = v;

                                                }
                                            } else {
                                                idOfVehicleInTrip = v;
                                            }
                                        }
                                    }

                                }

                                if (cplex.getValue(varTripStartTime[i]) >= 0) {
                                    int t = (int) Math.round(cplex.getValue(varTripStartTime[i]));
                                    TripWithDriveInfos tripWithDriveInfos = new TripWithDriveInfos(instance.getTrip(i), idOfVehicleInTrip, drivingStatus, t);
                                    pathForDriver.addTripInDriverPath(tripWithDriveInfos);
                                    currentTrip = i;
                                    findNextTrip = true;
                                }

                            }
                        }
                    }
                }
                System.out.println(pathForDriver);
                solution.addPathInSetForDriver(pathForDriver);

            }
        }
        return solution;
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


    public static void main(String[] args) throws IloException {

//        InstanceReader reader = new InstanceReader("largerExample.txt");
        InstanceReader reader1 = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW0.txt");

        Instance instance = reader1.readFile(); //这个语句将文本的内容就读出来了
        System.out.println(instance);

        String warmStartFileName = "feaSol_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW0.txt";


        SolverWithFormulation_1 solverWithFormulation1 = new SolverWithFormulation_1(instance, null, false);//warmStartFileName,

        // print solution
        Solution sol = solverWithFormulation1.solveWithCplex();
        if (sol != null) {
            sol.printInfile("sol_inst_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW0.txt");
        }
        //System.out.println(sol);
        System.out.println("time : " + solverWithFormulation1.getTimeInSec() + " s");
        System.out.println("lb : " + solverWithFormulation1.getLowerBound());
        System.out.println("ub : " + solverWithFormulation1.getUpperBound());
    }
}
