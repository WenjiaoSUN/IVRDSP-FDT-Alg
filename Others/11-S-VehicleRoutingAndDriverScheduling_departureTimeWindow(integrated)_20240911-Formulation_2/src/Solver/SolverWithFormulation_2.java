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
import jdk.swing.interop.SwingInterOpUtils;

import java.io.*;

import java.util.ArrayList;

public class SolverWithFormulation_2 {
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

    // Here are the new variables to deal with the time window 2024.9.17
    private IloNumVar[][] varTripStartUnit;
    private IloNumVar[][][][] varVehicleWaitingUnitBetweenTwoTrips;
    private IloNumVar[][][][] varDriverWaitingUnitBetweenTwoTrips;

    private IloNumVar[][] varWhetherDriverShortArc; //b_ij^DS
    private IloNumVar[][] varWhetherVehicleShortArc;//b_ij^VS
    private IloNumVar[] varStartingTimeOfDriverSchedule;//s^d
    private IloNumVar[] varEndingTimeOfDriverSchedule;//e^d

    // above are the new variables to deal with the time window 2024.9.17

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

    private int nbTimeSlots;
    private int nbMinPlanUnits;
    // Above are the attributes added to  deal with the time window 2024.9.12


    private int M;

    public SolverWithFormulation_2(Instance instance) throws IloException {
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
        this.nbTimeSlots = (instance.getEndingPlaningHorizon() - instance.getStartingPlanningHorizon()) / timeUnitSlot + 1;
        this.nbMinPlanUnits = instance.getMinPlanTurnTime() / timeUnitSlot;
        this.M = instance.getEndingPlaningHorizon();
    }

    public SolverWithFormulation_2(Instance instance, String warmStartFilePath, Boolean whetherSolveRelaxationProblem) throws IloException {
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
            cnstOneStartingTime();//(57)
            cnstLinkWaitingAndVehicleArc();//(58)
////            cnstVehicleWaitingArcAndStartingTime();//(59) which has the same meaning with constraints (60) and (61)
            cnstVehicleWaitingArcAndStartingTimeBetweenCombinedArc();//(60)
            cnstVehicleWaitingArcAndStartingTimeForNotAllCombinedArc();//(61)
            cnstDetectVehicleShortConnectionArc();//(62)
            cnstKeepSameVehiclesDuringShortConnectionInCombinedArc();//(63)
            cnstLinkWaitingAndDriverArc();//(64)
//            cnstDriverWaitingArcAndStartingTime();//(65)
            cnstDriverWaitingArcAndStartingTimeForm1();// new (66)
//            cnstDriverWaitingArcAndStartingTimeCompactForm();//(67) which is a stronger version oif the constraint (65) this  constraint may cause millions, should consider to reduce the number of Arcs now
            cnstDetectDriverShortConnectionArc();//(68)
            cnstProhibitChangeVehicleDuringDriverShortConnection();//(69)
            cnstStartingTimeOfSchedule();//(70)
            cnstEndingTimeOfSchedule();//(71)
            cnstWorkingTimeLimitation();//(72)
            // 5. export the model
            cplex.exportModel("modelOfRoutingAndScheduling.lp");

            cplex.setParam(IloCplex.Param.TimeLimit, 3600);// limit the time is two hour
            cplex.setParam(IloCplex.Param.Threads, 1);
            //here parameter for gap is only for the small instance which gap is 0.01 but in a very short time

            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1e-06);
//            cplex.setParam(IloCplex.Param.MIP.Limits.Solutions, 0); // 禁用MIP求解
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

            // Count variables and constraints before solving
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
            System.out.println("nbMaxPossibleArcs " + instance.getMaxNbPossibleArcs());

            // 6. solve
            long startTime = System.currentTimeMillis();
            if (cplex.solve()) {
                this.timeInMilliSec = (int) (System.currentTimeMillis() - startTime);
                this.lowerBound = this.cplex.getBestObjValue();  //getBestObjValue will give the low bound
                this.upperBound = this.cplex.getObjValue();
                System.out.println("problem solved !");
                if (this.whetherSolveRelaxationProblem == false) {
                    printSolutionAfterSolve();
                    return getSolutionFromCplex();
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
//
////              //Here is the part for varChangeOver[idOfDriver][idOfFirstTrip][idOfSecondTrip]
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
                    starValue.add(index, 1);
                    //System.out.println("varVehicleSourceDepot: " + varVehicleSourceDepotArc[idOfVehicle][Integer.valueOf(values[3])]);
                    index++;
                    startVar.add(index, varVehicleDepotSinkArc[idOfVehicle][Integer.valueOf(values[2])]);
                    starValue.add(index, 1);
                    //System.out.println("varVehicleDepotSink: " + varVehicleDepotSinkArc[idOfVehicle][Integer.valueOf(values[3])]);
                    index++;
                    //varVehicleArc (including the depot to trip and trip to depot)
                    //Here only varVehicleArc (Now is the depot to trip and trip to depot)
                    int i = 3;
                    startVar.add(index, varVehicleArc[idOfVehicle][Integer.valueOf(values[i])][Integer.valueOf(values[i + 1])]);
                    starValue.add(index, 1);
                    index++;
                    i = i + 1;
                    //Now : varVehicleArc (including the trip to trip, and including the departure time)2024.9.26
                    while (i < values.length - 2) {
                        startVar.add(index, varVehicleArc[idOfVehicle][Integer.valueOf(values[i])][Integer.valueOf(values[i + 2])]);
                        starValue.add(index, 1);
                        index++;
                        int timeUnit = Integer.valueOf(values[i + 1]) / timeUnitSlot;
                        //System.out.println("check the time unit =" +timeUnit);
                        startVar.add(index, varTripStartUnit[Integer.valueOf(values[i])][timeUnit]);
                        starValue.add(index, 1);
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
                    startVar.add(index, varDriverArc[idOfDriver][Integer.valueOf(values[j])][Integer.valueOf(values[j + 1])]);
                    starValue.add(index, 1);
                    index++;
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
                e.printStackTrace();  // 打印完整的异常堆栈信息
                System.out.println("exception during warm start to read the file");
                System.out.println(e.getMessage());
            } catch (IloException e) {
                e.printStackTrace();  // 打印完整的异常堆栈信息
                System.out.println("exception during warm start to read the file");
                System.out.println(e.getMessage());
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
                            if (cplex.getValue(varVehicleArc[v][i][depot.getIndexOfDepotAsEndingPoint()]) == 1) {
                                //System.out.println("vehicle" + v + "_" + i + "_" + depot.getIndexOfDepotAsEndingPoint() + "=1.0");
                            }
                        }
                    }
                }

                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        //check whether there is arc before let cplex get the value
                        if (instance.getIdleTimeCostForVehicle(i, j) < Integer.MAX_VALUE) {//here pay attention: if the value is not less than infinitive, then we couldn't get the value of this variable, because it is not show up in the model
                            // System.out.println("trip_"+i+"trip_"+j);
                            if (cplex.getValue(varVehicleArc[v][i][j]) == 1) {
                                System.out.println("vehicle" + v + "_" + i + "_" + j + "=" + cplex.getValue(varVehicleArc[v][i][j]));
                            }

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
                                    //System.out.println("driver" + "_" + d + " drive" + " Trip" + "_" + i);
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
                                // System.out.println("driver" + "_" + d + " drive" + " Trip" + "_" + i);
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
            int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
            int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
            for (int t = a_i; t < b_i + 1; t++) {
                if (cplex.getValue(varTripStartUnit[i][t]) > 0.99) {
                    System.out.println("trip_" + i + " start time unit_" + t);
                }
            }
        }

        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (cplex.getValue(varVehicleArc[v][i][j]) > 0.99) {
                            int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                            int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                            if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                                if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                    for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                        if (varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j] != null) {
//                                            if (cplex.getValue(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j]) > 0.999) {
//                                                System.out.println("vehicle_" + v + " waiting _" + h + " units between trip_" + i + "_" + j);
//                                            }
                                        } else {
                                            System.out.println("Variable varVehicleWaitingUnitBetweenTwoTrips[" + v + "][" + h + "][" + i + "][" + j + "] is null");
                                        }
                                    }
                                } else {
                                    for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                        if (varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j] != null) {
//                                            if (cplex.getValue(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j]) > 0.999) {
//                                                System.out.println("vehicle_" + v + " waiting _" + h + " units between trip_" + i + "_" + j);
//                                            }
                                        } else {
                                            System.out.println("Variable varVehicleWaitingUnitBetweenTwoTrips[" + v + "][" + h + "][" + i + "][" + j + "] is null");
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
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)
//                                    && instance.getConnectionTime(i, j) >= instance.getShortConnectionTimeForVehicle()
//                                    && instance.getConnectionTime(i, j) >= instance.getShortConnectionTimeForDriver()
                            ) {
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
               // int departureTime = trip.getEarliestDepartureTime();
                //int arrivalTime = trip.getLatestDepartureTime();
                int duration=trip.getDuration();
                expr.addTerm(varDriving[d][i],duration);
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

    //constraint (12) for driver use and driver source and depot arc
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

    //constraint (13) for the driver source depot and depot sink arc
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

    //constraint (14) flow constraint for the depot and the source(source depot  and depot trip arc)
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

    //constraint (15) flow constraint for the depot and sink
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

    //cnst (57)

    private void cnstOneStartingTime() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr exprStartingTime = cplex.linearNumExpr();
            Trip trip_i = instance.getTrip(i);
            int a_i = (trip_i.getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / instance.getTimeSlotUnit();
            int b_i = (trip_i.getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / instance.getTimeSlotUnit();
            for (int t = a_i; t < b_i + 1; t++) {
                exprStartingTime.addTerm(varTripStartUnit[i][t], 1);
            }
            IloConstraint constraint = cplex.addEq(exprStartingTime, 1, "StartTime_trip_" + i);
        }
    }

    //cnst (58)
    private void cnstLinkWaitingAndVehicleArc() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                }
                            }
                            expr.addTerm(varVehicleArc[v][i][j], -1);
                            IloConstraint constraint = cplex.addEq(expr, 0, "LinkWaitingAndArc_Vehicle_" + v + "_" + i + "_" + j);
                        }
                    }
                }
            }
        }

    }

    //cnst (59)
    private void cnstVehicleWaitingArcAndStartingTime() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                for (int j = 0; j < nbTrip; j++) {
                    int a_j = (instance.getTrip(j).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    int b_j = (instance.getTrip(j).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int durationUnit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - durationUnit_i;
                                            if (diff != h) {
                                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                                expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                                expr.addTerm(varTripStartUnit[i][t1], 1);
                                                expr.addTerm(varTripStartUnit[j][t2], 1);
                                                IloConstraint constraint = cplex.addLe(expr, 2, "Conflict for vehicle_" + v + " first trip_" + i + "start_" + t1 + " second trip_" + j + "start_" + t2 + " Waiting_" + h);
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int durationUnit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - durationUnit_i;
                                            if (diff != h) {
                                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                                expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                                expr.addTerm(varTripStartUnit[i][t1], 1);
                                                expr.addTerm(varTripStartUnit[j][t2], 1);
                                                IloConstraint constraint = cplex.addLe(expr, 2, "Conflict for vehicle_" + v + " first trip_" + i + "start_" + t1 + " second trip_" + j + "start_" + t2 + " Waiting_" + h);
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

    }

    // (60)
    int nbConstraintsRelatedToVehicleWaitingUnits = 0;

    private void cnstVehicleWaitingArcAndStartingTimeBetweenCombinedArc() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
                for (int j = 0; j < nbTrip; j++) {
                    int a_j = (instance.getTrip(j).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    int b_j = (instance.getTrip(j).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j) && nbVehicleNeed_i == 2 && nbVehicleNeed_j == 2) {
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        IloLinearNumExpr expr = cplex.linearNumExpr();
                                        expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                        expr.addTerm(varTripStartUnit[i][t1], 1);
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int durationUnit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - durationUnit_i;
                                            if (diff != h) {
                                                expr.addTerm(varTripStartUnit[j][t2], 1);
                                            }
                                        }
                                        IloConstraint constraint = cplex.addLe(expr, 2);
                                        nbConstraintsRelatedToVehicleWaitingUnits++;
                                    }
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        IloLinearNumExpr expr = cplex.linearNumExpr();
                                        expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                        expr.addTerm(varTripStartUnit[i][t1], 1);
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int durationUnit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - durationUnit_i;
                                            if (diff != h) {
                                                expr.addTerm(varTripStartUnit[j][t2], 1);
                                            }
                                        }
                                        IloConstraint constraint = cplex.addLe(expr, 2);
                                        nbConstraintsRelatedToVehicleWaitingUnits++;
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    //(61)
    private void cnstVehicleWaitingArcAndStartingTimeForNotAllCombinedArc() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
            int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
            int nbVehicleNeed_i = instance.getTrip(i).getNbVehicleNeed();
            for (int j = 0; j < nbTrip; j++) {
                int a_j = (instance.getTrip(j).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int b_j = (instance.getTrip(j).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int nbVehicleNeed_j = instance.getTrip(j).getNbVehicleNeed();
                if (instance.whetherHavePossibleArcAfterCleaning(i, j) && (nbVehicleNeed_i == 1 || nbVehicleNeed_j == 1)) {
                    int durationUnit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                    int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                    int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                    if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                        if (nbMinWaitingUnits >= nbMinPlanUnits) {
                            for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    for (int v = 0; v < nbVehicle; v++) {
                                        expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                    }
                                    expr.addTerm(varTripStartUnit[i][t1], 1);
                                    for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                        int value = t1 + durationUnit_i + h;
                                        if (t2 != value) {
                                            expr.addTerm(varTripStartUnit[j][t2], 1);
                                        }
                                    }
                                    IloConstraint constraint = cplex.addLe(expr, 2, "ConflictCheckNonAllCombinedArcs_" + i + "_" + j + " waiting time_" + h);
                                    nbConstraintsRelatedToVehicleWaitingUnits++;
                                }
                            }
                        } else {
                            for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    for (int v = 0; v < nbVehicle; v++) {
                                        expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], 1);
                                    }
                                    expr.addTerm(varTripStartUnit[i][t1], 1);
                                    for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                        int value = t1 + durationUnit_i + h;
                                        if (t2 != value) {
                                            expr.addTerm(varTripStartUnit[j][t2], 1);
                                        }
                                    }
                                    IloConstraint constraint = cplex.addLe(expr, 2, "ConflictCheckNonAllCombinedArcs_" + i + "_" + j + " waiting time_" + h);
                                    nbConstraintsRelatedToVehicleWaitingUnits++;
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    //(62)
    private void cnstDetectVehicleShortConnectionArc() throws IloException {
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varWhetherVehicleShortArc[i][j], 1);
                        int nbTimeUnitsForVehicleShort = instance.getShortConnectionTimeForVehicle() / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        // *** define nbLimitWaiting to avoid the null variable 2024.9.24
                        int nbLimitWaiting = nbTimeUnitsForVehicleShort;
                        int maxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        if (maxWaitingUnits >= nbMinPlanUnits) {
                            if (maxWaitingUnits < nbTimeUnitsForVehicleShort) {
                                nbLimitWaiting = maxWaitingUnits;
                            }

                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbLimitWaiting + 1; h++) {
                                    expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], -1);
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbLimitWaiting + 1; h++) {
                                    expr.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], -1);

                                }
                            }
                        }
                        IloConstraint constraint = cplex.addGe(expr, 0);
                    }
                }
            }
        }

    }

    //(63)
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


    //(64)
    private void cnstLinkWaitingAndDriverArc() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], 1);
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], 1);
                                }
                            }
                            expr.addTerm(varDriverArc[d][i][j], -1);
                            IloConstraint constraint = cplex.addEq(expr, 0, "LinkWaitingAndArc_Driver_" + d + "_" + i + "_" + j);
                        }
                    }
                }
            }
        }

    }

    //cnst (65)
    private void cnstDriverWaitingArcAndStartingTime() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                for (int j = 0; j < nbTrip; j++) {
                    int a_j = (instance.getTrip(j).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    int b_j = (instance.getTrip(j).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int durationUnit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - durationUnit_i;
                                            if (diff != h) {
                                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                                expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], 1);
                                                expr.addTerm(varTripStartUnit[i][t1], 1);
                                                expr.addTerm(varTripStartUnit[j][t2], 1);
                                                IloConstraint constraint = cplex.addLe(expr, 2);
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int durationUnit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - durationUnit_i;
                                            if (diff != h) {
                                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                                expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], 1);
                                                expr.addTerm(varTripStartUnit[i][t1], 1);
                                                expr.addTerm(varTripStartUnit[j][t2], 1);
                                                IloConstraint constraint = cplex.addLe(expr, 2);
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

    }


    //(66)
    int nbConstraintsRelatedToDriverWaitingUnits = 0;
    private void cnstDriverWaitingArcAndStartingTimeForm1() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int duration_Unit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
              
                for (int j = 0; j < nbTrip; j++) {
                    int a_j = (instance.getTrip(j).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    int b_j = (instance.getTrip(j).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        for (int t1 = a_i; t1 < b_i + 1; t1++) {
                            for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                int diff = t2 - t1 - duration_Unit_i;
                                if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                                    if(nbMinWaitingUnits>=nbMinPlanUnits) {
                                        if (diff >= nbMinWaitingUnits && diff <= nbMaxWaitingUnits) {//
                                            int h = diff;
                                            IloLinearNumExpr expr = cplex.linearNumExpr();
                                            expr.addTerm(varDriverArc[d][i][j], 1);
                                            expr.addTerm(varTripStartUnit[i][t1], 1);
                                            expr.addTerm(varTripStartUnit[j][t2], 1);
                                            expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], -1);
                                            IloConstraint constraint = cplex.addLe(expr, 2);
                                            nbConstraintsRelatedToDriverWaitingUnits++;
                                        }
                                    }else {
                                        if (diff >= nbMinPlanUnits && diff <= nbMaxWaitingUnits) {//
                                            int h = diff;
                                            IloLinearNumExpr expr = cplex.linearNumExpr();
                                            expr.addTerm(varDriverArc[d][i][j], 1);
                                            expr.addTerm(varTripStartUnit[i][t1], 1);
                                            expr.addTerm(varTripStartUnit[j][t2], 1);
                                            expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], -1);
                                            IloConstraint constraint = cplex.addLe(expr, 2);
                                            nbConstraintsRelatedToDriverWaitingUnits++;
                                        }

                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        System.out.println("nbConstraints related to driverWaitingUnit until 66: " + nbConstraintsRelatedToDriverWaitingUnits);
    }

    //(67)
    private void cnstDriverWaitingArcAndStartingTimeCompactForm() throws IloException {
        int nbConstraintsRelatedToDriverWaitingUnits = 0;
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                for (int j = 0; j < nbTrip; j++) {
                    int a_j = (instance.getTrip(j).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    int b_j = (instance.getTrip(j).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        IloLinearNumExpr expr = cplex.linearNumExpr();
                                        expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], 1);
                                        expr.addTerm(varTripStartUnit[i][t1], 1);
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int duration_Unit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - duration_Unit_i;
                                            if (diff != h) {
                                                expr.addTerm(varTripStartUnit[j][t2], 1);

                                            }
                                        }
                                        IloConstraint constraint = cplex.addLe(expr, 2);
                                        nbConstraintsRelatedToDriverWaitingUnits++;
                                    }
//                                }
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    for (int t1 = a_i; t1 < b_i + 1; t1++) {
                                        IloLinearNumExpr expr = cplex.linearNumExpr();
                                        expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], 1);
                                        expr.addTerm(varTripStartUnit[i][t1], 1);
                                        for (int t2 = a_j; t2 < b_j + 1; t2++) {
                                            int duration_Unit_i = instance.getTrip(i).getDuration() / timeUnitSlot;
                                            int diff = t2 - t1 - duration_Unit_i;
                                            if (diff != h) {
                                                expr.addTerm(varTripStartUnit[j][t2], 1);

                                            }
                                        }
                                        IloConstraint constraint = cplex.addLe(expr, 2);
                                        nbConstraintsRelatedToDriverWaitingUnits++;
                                    }
//                                }
                                }

                            }
                        }
                    }
                }
            }
        }
        System.out.println("nbConstraints related to driverWaitingUnit " + nbConstraintsRelatedToDriverWaitingUnits);

    }


    //(68)
    private void cnstDetectDriverShortConnectionArc() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varWhetherDriverShortArc[i][j], 1);

                        int nbTimeUnitsForDriverShort = instance.getShortConnectionTimeForDriver() / timeUnitSlot;

                        // *** define nbLimitWaiting to avoid the null variable 2024.9.24
                        int nbLimitWaiting = nbTimeUnitsForDriverShort;
                        int maxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (maxWaitingUnits >= nbMinWaitingUnits) {
                            if (maxWaitingUnits < nbTimeUnitsForDriverShort) {
                                nbLimitWaiting = maxWaitingUnits;
                            }
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbLimitWaiting + 1; h++) {
                                    expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], -1);
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbLimitWaiting + 1; h++) {
                                    expr.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], -1);
                                }
                            }
                            IloConstraint constraint = cplex.addGe(expr, 0);
                        }
                    }
                }
            }
        }

    }

    //(69)
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

    //(70)
    private void cnstStartingTimeOfSchedule() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int p = 0; p < nbDepot; p++) {
                int idCityAsDepot = instance.getDepot(p).getIdOfCityAsDepot();
                for (int i = 0; i < nbTrip; i++) {
                    int idStartCityOfTrip = instance.getTrip(i).getIdOfStartCity();
                    if (idCityAsDepot == idStartCityOfTrip) {
                        int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                        int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varStartingTimeOfDriverSchedule[d], 1);
                        for (int t = a_i; t < b_i + 1; t++) {
                            expr.addTerm(varTripStartUnit[i][t], -t * timeUnitSlot);
                        }
                        expr.addTerm(varDriverArc[d][instance.getDepot(p).getIndexOfDepotAsStartingPoint()][i], M);
                        IloConstraint constraint = cplex.addLe(expr, M);
                    }
                }
            }
        }
    }

    //(71)
    private void cnstEndingTimeOfSchedule() throws IloException {
        for (int d = 0; d < nbDriver; d++) {
            for (int p = 0; p < nbDepot; p++) {
                int idCityAsDepot = instance.getDepot(p).getIdOfCityAsDepot();
                for (int i = 0; i < nbTrip; i++) {
                    int idEndingCityOfTrip = instance.getTrip(i).getIdOfEndCity();
                    if (idCityAsDepot == idEndingCityOfTrip) {
                        int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                        int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                        int duration_i = instance.getTrip(i).getDuration();
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(varEndingTimeOfDriverSchedule[d], 1);
                        for (int t = a_i; t < b_i + 1; t++) {
                            expr.addTerm(varTripStartUnit[i][t], -t * timeUnitSlot);
                        }
                        expr.addTerm(varDriverArc[d][i][instance.getDepot(p).getIndexOfDepotAsEndingPoint()], -M);
                        IloConstraint constraint = cplex.addGe(expr, duration_i - M);
                    }
                }
            }
        }
    }

    //(72)
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
        int cpt = 0;
        /**
         * add new variable in the objective function
         * */

        //cost for using vehicle
        for (int v = 0; v < nbVehicle; v++) {
            cpt++;
            obj.addTerm(varVehicleUse[v], instance.getFixedCostForVehicle());
        }

        //cost for using driver
        for (int d = 0; d < nbDriver; d++) {
            cpt++;
            obj.addTerm(varDriverUse[d], instance.getFixedCostForDriver());
        }
        for (int v = 0; v < nbVehicle; v++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                        cpt++;
                                        int idleTimeCost_v = h * timeUnitSlot * instance.getIdleTimeCostForVehiclePerUnit();
                                        obj.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], idleTimeCost_v);

                                    }
                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                        cpt++;
                                        int idleTimeCost_v = h * timeUnitSlot * instance.getIdleTimeCostForVehiclePerUnit();
                                        obj.addTerm(varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j], idleTimeCost_v);

                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
//        System.out.println("cpt = " + cpt);
        // cost of crew in the object function;
        /**attention the cost ; we need to know the cost for the whole path
         */
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                        int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                        if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                            if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    int idleTimeCost_d = h * timeUnitSlot * instance.getIdleTimeCostForDriverPerUnit();
                                    obj.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], idleTimeCost_d);

                                }
                            } else {
                                for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                    int idleTimeCost_d = h * timeUnitSlot * instance.getIdleTimeCostForDriverPerUnit();
                                    obj.addTerm(varDriverWaitingUnitBetweenTwoTrips[d][h][i][j], idleTimeCost_d);

                                }

                            }
                        }
                    }
                }
            }
        }
//        System.out.println("cpt = " + cpt);
        //cost of the changeover part here is what I need to modify
        for (int d = 0; d < nbDriver; d++) {
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        cpt++;
                        obj.addTerm(varChangeOver[d][i][j], instance.getCostForChangeOver(i, j));
                    }
                }

            }
        }
//        System.out.println("cpt = " + cpt);
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
        varTripStartUnit = new IloNumVar[nbTrip][nbTimeSlots];
        varDriverWaitingUnitBetweenTwoTrips = new IloNumVar[nbDriver][nbTimeSlots][nbTrip][nbTrip];
        varVehicleWaitingUnitBetweenTwoTrips = new IloNumVar[nbVehicle][nbTimeSlots][nbTrip][nbTrip];


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
                varVehicleUse[v] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_v_" + v);
            }
            //variable of driver use --type and range
            for (int d = 0; d < nbDriver; d++) {
                varDriverUse[d] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_d_" + d);
            }
            /**
             * add the new variable for the depot flow
             */

            for (int v = 0; v < nbVehicle; v++) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    varVehicleSourceDepotArc[v][p] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_v" + v + "_source_" + p);
                }
            }
            for (int v = 0; v < nbVehicle; v++) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    varVehicleDepotSinkArc[v][k] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_v" + v + "_" + k + "_sink");
//                    System.out.println("Check the linear or not" + v + ": " +  varVehicleDepotSinkArc[v][k].getUB());
                }
            }

            for (int d = 0; d < nbDriver; d++) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    varDriverSourceDepotArc[d][p] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_d" + d + "_source_" + p);
                }
            }

            for (int d = 0; d < nbDriver; d++) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    varDriverDepotSinkArc[d][k] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_d" + d + "_" + k + "_sink");
                }
            }

            //___________________________________________________________________________________here is all the new variable
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbNodes; i++) {
                    for (int j = 0; j < nbNodes; j++) {
                        if (i < this.nbTrip && j < this.nbTrip) {
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                varVehicleArc[v][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_v" + v + "_" + i + "_" + j);
                            }
                        } else {
                            varVehicleArc[v][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_v" + v + "_" + i + "_" + j);
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
                                varDriverArc[d][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_d" + d + "_" + i + "_" + j);
                            }
                        } else {
                            varDriverArc[d][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_d" + d + "_" + i + "_" + j);
                        }
                    }
                }
            }
            // variable for whether diver is driving the arc---- type and range
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    varDriving[d][i] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "o_d" + d + "_" + i);
                }
            }
            // variable the type of variable whether leading the vehicle
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    varLeading[v][i] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "l_v" + v + "_" + i);
                }
            }
            // the type of variable whether changeover happen to the driver
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            varChangeOver[d][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "y_d" + d + "_" + i + "_" + j);
                        }
                    }
                }
            }
            // ****************************************************Here are the new variables to deal with the time window 2024.9.16- 2024.9.17
            for (int i = 0; i < nbTrip; i++) {
                for (int t = 0; t < nbTimeSlots; t++) {
                    int earliestDepartureUnit = instance.getTrip(i).getEarliestDepartureTime() / timeUnitSlot;
                    int latestDepartureUnit = instance.getTrip(i).getLatestDepartureTime() / timeUnitSlot;
                    if (t <= latestDepartureUnit && t >= earliestDepartureUnit) {
                        varTripStartUnit[i][t] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_trip_" + i + "_t_" + t);
                    }
                }
            }
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                            int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                            if (nbMaxWaitingUnits >= nbMinWaitingUnits) {
                                if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                    for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {//
                                        varDriverWaitingUnitBetweenTwoTrips[d][h][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_d_" + d + "_h" + h + "arc_" + i + "_" + j);
                                    }
                                } else {
                                    for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {//
                                        varDriverWaitingUnitBetweenTwoTrips[d][h][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_d_" + d + "_h" + h + "arc_" + i + "_" + j);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                            int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                            if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                                if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                    for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                        varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_v_" + v + "_h" + h + "arc_" + i + "_" + j);
                                    }
                                } else {
                                    for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                        varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "x_v_" + v + "_h" + h + "arc_" + i + "_" + j);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        varWhetherDriverShortArc[i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "b^DS_" + i + "_" + j);
                    }


                }
            }

            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        varWhetherVehicleShortArc[i][j] = cplex.numVar(0.0, 1.0, IloNumVarType.Float, "b^VS_" + i + "_" + j);
                    }
                }
            }

            for (int d = 0; d < nbDriver; d++) {
                varStartingTimeOfDriverSchedule[d] = cplex.numVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), IloNumVarType.Float, "s^d" + d);
            }

            for (int d = 0; d < nbDriver; d++) {
                varEndingTimeOfDriverSchedule[d] = cplex.numVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), IloNumVarType.Float, "e^d" + d);
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
            //*********************************Here are the new variables to deal with the time window 2027.9.12-2024.9.17*********************
            for (int i = 0; i < nbTrip; i++) {
                for (int t = 0; t < nbTimeSlots; t++) {
                    int earliestDepartureUnit = instance.getTrip(i).getEarliestDepartureTime() / timeUnitSlot;
                    int latestDepartureUnit = instance.getTrip(i).getLatestDepartureTime() / timeUnitSlot;
                    if (t <= latestDepartureUnit && t >= earliestDepartureUnit) {
                        varTripStartUnit[i][t] = cplex.boolVar("StartUnit_x_i" + i + "_" + t);
                    }

                }
            }

            int nbDriverWaitingUnitArcVariables = 0;

            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                            int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
                            if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                                if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                    for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {

                                        varDriverWaitingUnitBetweenTwoTrips[d][h][i][j] = cplex.boolVar("x_" + d + "_" + h + "_" + i + "_" + j);

                                        nbDriverWaitingUnitArcVariables++;
                                    }
                                } else {
                                    for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                        varDriverWaitingUnitBetweenTwoTrips[d][h][i][j] = cplex.boolVar("x_" + d + "_" + h + "_" + i + "_" + j);

                                        nbDriverWaitingUnitArcVariables++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("nbVariables related to DriverWaitingUnitArc " + nbDriverWaitingUnitArcVariables);

            int nbVehicleWaitingUnitArcVariables = 0;
            for (int v = 0; v < nbVehicle; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            int nbMaxWaitingUnits = instance.getMaxWaitingTime(i, j) / timeUnitSlot;
                            int nbMinWaitingUnits = instance.getMinWaitingTime(i, j) / timeUnitSlot;
//                            System.out.println("check the nbMinUnits"+nbMinWaitingUnits);
                            if (nbMaxWaitingUnits >= nbMinPlanUnits) {
                                if (nbMinWaitingUnits >= nbMinPlanUnits) {
                                    for (int h = nbMinWaitingUnits; h < nbMaxWaitingUnits + 1; h++) {
                                        varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j] = cplex.boolVar("x_" + v + "_" + h + "_" + i + "_" + j);
                                        nbVehicleWaitingUnitArcVariables++;
                                    }
                                } else {
                                    for (int h = nbMinPlanUnits; h < nbMaxWaitingUnits + 1; h++) {
                                        varVehicleWaitingUnitBetweenTwoTrips[v][h][i][j] = cplex.boolVar("x_" + v + "_" + h + "_" + i + "_" + j);
                                        nbVehicleWaitingUnitArcVariables++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("nbVariables related to VehicleWaitingUnitArc: " + nbVehicleWaitingUnitArcVariables);

            // 2024.9.17
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        varWhetherDriverShortArc[i][j] = cplex.boolVar("b^DS_" + i + "_" + j);
                    }
                }
            }

            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        varWhetherVehicleShortArc[i][j] = cplex.boolVar("b^VS_" + i + "_" + j);
                    }
                }
            }

            for (int d = 0; d < nbDriver; d++) {
                varStartingTimeOfDriverSchedule[d] = cplex.intVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), "s^d_" + d);
            }

            for (int d = 0; d < nbDriver; d++) {
                varEndingTimeOfDriverSchedule[d] = cplex.intVar(instance.getStartingPlanningHorizon(), instance.getEndingPlaningHorizon(), "e^d_" + d);
            }


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
                                        int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                                        int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                                        for (int t = a_i; t < b_i + 1; t++) {
                                            if (cplex.getValue(varTripStartUnit[firstTrip][t]) > 0.99) {
                                                TripWithStartingInfos tripWithStartingInfos = new TripWithStartingInfos(instance.getTrip(firstTrip), t);
                                                pathForVehicle.addTripInVehiclePath(tripWithStartingInfos);
                                            }
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
                                int a_l = (instance.getTrip(l).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                                int b_l = (instance.getTrip(l).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                                for (int t = a_l; t < b_l + 1; t++) {
                                    if (cplex.getValue(varTripStartUnit[l][t]) > 0.999) {
                                        TripWithStartingInfos tripWithStartingInfos_l = new TripWithStartingInfos(instance.getTrip(l), t);
                                        pathForVehicle.addTripInVehiclePath(tripWithStartingInfos_l);
                                        currentTrip = l;
                                        findNextTrip = true;
                                    }
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
                                        int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                                        int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;


                                        for (int v = 0; v < solution.getPathForVehicles().size(); v++) {
                                            PathForVehicle pathForVehicle = solution.getPathForVehicles().get(v);
                                            if (pathForVehicle.isPresentInPath(instance.getTrip(i))) {
                                                idOfVehicleInTrip = instance.getVehicle(v).getIdOfVehicle();

                                            }

                                        }
                                        if (cplex.getValue(varDriving[d][i]) > 0.9999) {
                                            drivingStatus = true;
                                        }

                                        for (int t = a_i; t < b_i + 1; t++) {
                                            if (cplex.getValue(varTripStartUnit[i][t]) > 0.99) {

                                                TripWithDriveInfos tripWithDriveInfos = new TripWithDriveInfos(instance.getTrip(i), idOfVehicleInTrip, drivingStatus, t);
                                                pathForDriver.addTripInDriverPath(tripWithDriveInfos);

                                            }
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
                                                if (cplex.getValue(varLeading[v][i]) > 0.9) {
                                                    System.out.println("when driver" + d + "is driving in combine trip, varLeading" + cplex.getValue(varLeading[v][i]));
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
                                                if (cplex.getValue(varLeading[v][i]) > 0.9) {
                                                    System.out.println("when driver" + d + "is a passenger in combined trip, varLeading" + cplex.getValue(varLeading[v][i]));
                                                    idOfVehicleInTrip = v;

                                                }
                                            } else {
                                                idOfVehicleInTrip = v;
                                            }
                                        }
                                    }

                                }
                                int a_i = (instance.getTrip(i).getEarliestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                                int b_i = (instance.getTrip(i).getLatestDepartureTime() - instance.getStartingPlanningHorizon()) / timeUnitSlot;
                                for (int t = a_i; t < b_i + 1; t++) {
                                    if (cplex.getValue(varTripStartUnit[i][t]) > 0.999) {
                                        TripWithDriveInfos tripWithDriveInfos = new TripWithDriveInfos(instance.getTrip(i), idOfVehicleInTrip, drivingStatus, t);
                                        pathForDriver.addTripInDriverPath(tripWithDriveInfos);
                                        currentTrip = i;
                                        findNextTrip = true;
                                    }
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

        InstanceReader reader = new InstanceReader("inst_Test.txt");
        InstanceReader reader1 = new InstanceReader("inst_nbCity05_Size180_Day1_nbTrips010_combPer0.0_TW1.txt");

        Instance instance = reader1.readFile(); //这个语句将文本的内容就读出来了
        System.out.println(instance);
//        System.out.println("nbArcs: " + instance.getNbArcs());

//        String warmStartFileName = "feaSol_nbCity05_Size180_Day1_nbTrips100_combPer0.25_TW3.txt";


        //String warmStartFilePath = "C:\\Users\\wsun\\IdeaProjects\\4(2)Vehicle routing and scheduling\\VehicleRoutingAndDriverScheduling - unknowNbVehicleAndDriverAndUnknowDepotVersion" +
        //"feaSol_nbCity05_Size180_Day2_nbTrips200_combPer0.1.txt";

        SolverWithFormulation_2 solverWithFormulation2 = new SolverWithFormulation_2(instance, null, false);

        // print solution
        Solution sol = solverWithFormulation2.solveWithCplex();

        if (sol != null) {
            sol.printInfile("sol_test.txt");
        }
        //System.out.println(sol);
        System.out.println("time : " + solverWithFormulation2.getTimeInSec() + " s");
        System.out.println("lb : " + solverWithFormulation2.getLowerBound());
        System.out.println("ub : " + solverWithFormulation2.getUpperBound());
    }

}
