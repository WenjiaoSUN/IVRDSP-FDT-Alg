package Solver;

import InputSchedulesAndRoutesInforms.InputInforms;
import InputSchedulesAndRoutesInforms.InputInformsReader;
import InputSchedulesAndRoutesInforms.VehicleRoute;
import InputSchedulesAndRoutesInforms.DriverSchedule;

import Solution.PathForDriver;
import Solution.PathForVehicle;
import Solution.TripWithDriveInfos;
import Solution.TripWithStartingInfos;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import Instance.Vehicle;
import Instance.Instance;
import Instance.InstanceReader;
import Solution.Solution;

import java.io.*;

public class SolverWithFormulationBasedOnDriverSchedule {
    private Instance instance;
    private IloCplex cplex;
    private IloNumVar[][] varDriving; //here is the variable o i d
    private IloNumVar[][] varLeading; // here is the variable l v i
    private IloNumVar[][][] varChangeOver; // here is the variable y d i j describe whether the driver leaving his own place to other place


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


    private int M;

    private InputInforms inputInforms;

    private int nbDriverSchedulesInput;
    private int nbVehicleRouteInput;


    public SolverWithFormulationBasedOnDriverSchedule(Instance instance, InputInforms inputInforms) throws IloException {
        this.instance = instance;
        this.inputInforms = inputInforms;
        this.nbVehicle = instance.getMaxNbVehicleAvailable();
        this.nbDriver = instance.getMaxNbDriverAvailable();

        // change to schedules size 2025.1.15
        this.nbTrip = instance.getNbTrips();
        this.nbDepot = instance.getNbDepots();
        this.nbNodes = nbTrip + 2 * nbDepot;  //this is to avoid the path become a circle
        this.warmStartFilePath = null;
        this.whetherSolveRelaxationProblem = false;
        // here are the attributes to deal with the time window
        this.M = instance.getEndingPlaningHorizon();
        this.nbDriverSchedulesInput = inputInforms.getDriverSchedules().size();
        this.nbVehicleRouteInput = inputInforms.getVehicleRoutes().size();
    }

    public SolverWithFormulationBasedOnDriverSchedule(Instance instance, InputInforms inputInforms, Boolean whetherSolveRelaxationProblem) throws IloException {
        this(instance, inputInforms);
        this.whetherSolveRelaxationProblem = whetherSolveRelaxationProblem;
    }


    public Solution solveWithCplexBasedOnGivenSchedules() {
        try {
            cplex = new IloCplex();
            //cplex.setParam(IloCplex.Param.MIP.Strategy.File, 2);//give a warm start value from an outside file
            defineDecisionVariables();
            defineObjectiveFunction();

            cnstOneLeadingVehicleInCombTrip();//constraint (8)
            // for driver
            cnstDriving();//(19)
            cnstDrivingTime();
            cnstWorkStatusFirst();//(22)
            cnstWorkStatusSecond();//(23)

            //Here are the original linking constraints
            cnstChangeVehicleBetweenNormalTrips();//(27) count changeover when the driving driver change vehicle

            cnstChangeVehicleFromNormalToCombinedTrips();//(28)
            cnstVehicleChangeFromCombinedToNormalTrip();//(29)
            cnstChangeVehicleFromCombinedToCombinedTrips();//(30)
            cnstChangeVehicleFromCombinedToCombinedTripsCorrespoindingVerLeadingOrder();//(31)
            cnstChangeOverDuringCombineToCombineWhenNotSuccessiveToVehicle();//(32)

            // 5. export the model
            cplex.exportModel("modelOfRoutingAndScheduling.lp");

            cplex.setParam(IloCplex.Param.TimeLimit, 3600);// limit the time is two hour
            cplex.setParam(IloCplex.Param.Threads, 1);
            //here parameter for gap is only for the small instance which gap is 0.01 but in a very short time
            cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1e-06);
            //cplex.setParam(IloCplex.Param.Parallel, 1);
            // ----------------------------------------------here is for the warm start
            // Create a warm start according to a solution file

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
                    printSolutionAfterSolve();// need to be modified 2025.1.15
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
        }
        cplex.close();
        cplex.end();
        return null;

    }

    //need to be modified the method printSolution 2025.1.15
    private void printSolutionAfterSolve() throws IloException {
        // 7. print
        System.out.println("The mini cost of routing and scheduling is " + cplex.getObjValue());
        // here is to print out the solution of the driver solution
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                            //System.out.println("driver" + d + "_" + i + "_" + j + "=" + cplex.getValue(varDriverArc[d][i][j]));
                            if (cplex.getValue(varChangeOver[d][i][j]) > 0.9999) {
                               // System.out.println("ChangeOver happen between trip_" + i + " and trip_" + j + " for driver" + "_" + d);

                            }
                        }

                    }
                }
            }
        }

        // here is to print out which vehicle is leading during the combine part
        for (int i = 0; i < nbTrip; i++) {
            for (int j = 0; j < nbTrip; j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    if (instance.getTrip(i).getNbVehicleNeed() > 1.99) {
                        for (int v = 0; v < nbVehicleRouteInput; v++) {
                            Vehicle vehicle = instance.getVehicle(v);
                            VehicleRoute vehicleRoute=this.inputInforms.getVehicleRoutes().get(v);
                            if (vehicleRoute.whetherPerformArc(i,j)) {
                                if (cplex.getValue(varLeading[v][i]) > 0.9999) {
                                  //  System.out.println("vehicle" + "_" + v + " leading Trip" + "_" + i);
                                }
                            }
                        }
                    }
                }
            }

        }

        //Here is to print out which driver is driving the trip
        for (int i = 0; i < nbTrip; i++) {
            int q=instance.getTrip(i).getNbVehicleNeed();
            if(q==2) {
                for (int d = 0; d < nbDriverSchedulesInput; d++) {
                    DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
                    if(driverSchedule.whetherTripPresent(i)){
                        System.out.println("driver "+d +" whether diving in combine trip :"+cplex.getValue(varDriving[d][i]));
                    }

                }
            }
        }


    }


    //this constraint is about the vehicle change from combined trip to a normal trip, which is (30)
    //either driver drives the leading vehicle in combined trip enter a normal trip; or changeover happen
    private void cnstVehicleChangeFromCombinedToNormalTrip() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int v = 0; v < nbVehicleRouteInput; v++) {
                VehicleRoute vehicleRoute= this.inputInforms.getVehicleRoutes().get(v);
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() == 2) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                if (instance.getTrip(j).getNbVehicleNeed() == 1) {
                                    if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                                        if(vehicleRoute.whetherPerformArc(i,j)) {
                                            IloLinearNumExpr expr = cplex.linearNumExpr();
                                            expr.addTerm(varDriving[d][i], 1);
                                            expr.addTerm(varLeading[v][i], 1);
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
        }

    }

    //add NewConstraints 0619
    private void cnstChangeVehicleFromCombinedToCombinedTripsCorrespoindingVerLeadingOrder() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int v = 0; v < nbVehicleRouteInput; v++) {
                VehicleRoute vehicleRoute= this.inputInforms.getVehicleRoutes().get(v);
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() >= 2) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                                        if(vehicleRoute.whetherPerformArc(i,j)) {
                                            IloLinearNumExpr expr = cplex.linearNumExpr();
                                            expr.addTerm(varDriving[d][j], 1);
                                            expr.addTerm(varLeading[v][j], 1);
                                            expr.addTerm(varLeading[v][i], -1);
                                            expr.addTerm(varChangeOver[d][i][j], -1);
                                            IloRange constraint = cplex.addLe(expr, 2, "chCo&Co" + d);
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

    //add NewConstraint 0618
    public void cnstChangeOverDuringCombineToCombineWhenNotSuccessiveToVehicle() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int v = 0; v < nbVehicleRouteInput; v++) {
                VehicleRoute vehicleRoute= this.inputInforms.getVehicleRoutes().get(v);
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() >= 1.99) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                                        if(!vehicleRoute.whetherPerformArc(i,j)) {
                                            IloLinearNumExpr expr = cplex.linearNumExpr();
                                            expr.addTerm(varDriving[d][i], 1);
                                            expr.addTerm(varLeading[v][j], 1);// it doesnt matter we change li to lj
                                            expr.addTerm(varChangeOver[d][i][j], -1);
                                            IloRange constraint = cplex.addLe(expr, 1, "chNo&CoNotSuccessiveToVehicle" + d);
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

    // this constraint  is about changing vehicles between two combined trips, which is constraint (28)
    // when previous combined lead by v, the latter combined not lead by v, changeover of driver happens
    private void cnstChangeVehicleFromCombinedToCombinedTrips() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule=this.inputInforms.getDriverSchedules().get(d);
            for (int v = 0; v < nbVehicleRouteInput; v++) {
                VehicleRoute vehicleRoute=this.inputInforms.getVehicleRoutes().get(v);
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() >= 2) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    if(driverSchedule.whetherArcExist(i,j)) {
                                        if(vehicleRoute.whetherPerformArc(i,j)) {
                                            IloLinearNumExpr expr = cplex.linearNumExpr();
                                            expr.addTerm(varDriving[d][j], 1);
                                            expr.addTerm(varLeading[v][i], 1);
                                            expr.addTerm(varLeading[v][j], -1);
                                            expr.addTerm(varChangeOver[d][i][j], -1);
                                            IloRange constraint = cplex.addLe(expr, 1, "chCo&Co" + d);
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

    // this constraint  is about changing vehicles between normal trip (one vehicle) to combined trip (several vehicles),
    // either driver drive the leading in v, or changeover occurs  which in paper is (27)
    private void cnstChangeVehicleFromNormalToCombinedTrips() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int v = 0; v < nbVehicleRouteInput; v++) {
                VehicleRoute vehicleRoute=this.inputInforms.getVehicleRoutes().get(v);
                for (int i = 0; i < nbTrip; i++) {
                    if (instance.getTrip(i).getNbVehicleNeed() == 1) {
                        for (int j = 0; j < nbTrip; j++) {
                            if (instance.getTrip(j).getNbVehicleNeed() >= 2) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                                    if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                                        if(vehicleRoute.whetherPerformArc(i,j)) {
                                            IloLinearNumExpr expr = cplex.linearNumExpr();
                                            expr.addTerm(varDriving[d][j], 1);
                                            expr.addTerm(varLeading[v][j], -1);
                                            expr.addTerm(varChangeOver[d][i][j], -1);
                                            IloRange constraint = cplex.addLe(expr, 0, "chNo&Co" + d);
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


    // this constraint is about changing vehicles between normal trip   which is (26) //change 0619
    // it describes if drive leave or separates with his original vehicle cause to changeover
    private void cnstChangeVehicleBetweenNormalTrips() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {//2025.1.15
                            boolean whetherExsitOneVehiclePerformArc = false;

                            for (int v = 0; v < nbVehicleRouteInput; v++) {
                                VehicleRoute vehicleRoute = this.inputInforms.getVehicleRoutes().get(v);
                                if (vehicleRoute.whetherPerformArc(i, j)) {
                                    whetherExsitOneVehiclePerformArc = true;
                                }
                            }
                            if (whetherExsitOneVehiclePerformArc == false) {
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(varDriving[d][i], 1);
                                expr.addTerm(varChangeOver[d][i][j], -1);
                                IloRange constraint = cplex.addLe(expr, 0, "chNo&No" + d);
                            }
                        }
                    }
                }
            }
        }
    }


    // this changeOver constraint is about the work status second constraint, which is (25) in paper
    private void cnstWorkStatusSecond() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varDriving[d][j], 1);
                            expr.addTerm(varChangeOver[d][i][j], -1);
                            expr.addTerm(varDriving[d][i], -1);
                            IloRange constraint = cplex.addLe(expr, 0, "statusChangeover_S" + d);
                        }
                    }
                }
            }
        }
    }

    //start here, we consider the changeover
    // this constraint is about the work status, which is (24) in paper
    private void cnstWorkStatusFirst() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(varDriving[d][i], 1);
                            expr.addTerm(varChangeOver[d][i][j], -1);
                            expr.addTerm(varDriving[d][j], -1);
                            IloRange constraint = cplex.addLe(expr, 0, "staCh_F" + d);
                        }
                    }
                }
            }
        }

    }


    // this constraint is about the driving time, which is (19) in the paper
    private void cnstDrivingTime() throws IloException {
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule=this.inputInforms.getDriverSchedules().get(d);
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int i = 0; i < nbTrip; i++) {
                if(driverSchedule.whetherTripPresent(i)) {
                    int duration = instance.getTrip(i).getDuration();
                    expr.addTerm(varDriving[d][i], duration);
                }
            }
            IloRange constraint = cplex.addLe(expr, instance.getMaxDrivingTime(), "D" + d + "drivingTime");
        }
    }


    // this constraint is about only one person drives the vehicle, which is (18) in the paper
    private void cnstDriving() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int d = 0; d < nbDriverSchedulesInput; d++) {
                DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
                if (driverSchedule.whetherTripPresent(i)) {
                    expr.addTerm(varDriving[d][i], 1);
                }
            }
            IloRange constraint = cplex.addEq(expr, 1, "dri_" + i);
        }
    }


    // this is the constraint (8) for those combined trips, in each only one leading vehicle
    private void cnstOneLeadingVehicleInCombTrip() throws IloException {
        for (int i = 0; i < nbTrip; i++) {
            if (instance.getTrip(i).getNbVehicleNeed() > 1) {
                IloLinearNumExpr expr = cplex.linearNumExpr();
                for (int v = 0; v < nbVehicleRouteInput; v++) {
                    VehicleRoute vehicleRoute=this.inputInforms.getVehicleRoutes().get(v);
                    if(vehicleRoute.whetherTripPresent(i)) {
                        expr.addTerm(varLeading[v][i], 1);
                    }
                }
                IloRange constraint = cplex.addEq(expr, 1, "CombTrip" + i + "leadV");
            }
        }

    }


    //____________________________________
    private void defineObjectiveFunction() throws IloException {
        //3. 定义目标函数 cost for the aircraft part in the objective function
        IloLinearNumExpr obj = cplex.linearNumExpr();

        //cost of the changeover part here is what I need to modify
        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule = inputInforms.getDriverSchedules().get(d);
            for (int i = 0; i < nbTrip; i++) {
                for (int j = 0; j < nbTrip; j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.whetherArcExist(i, j)) {
                            obj.addTerm(varChangeOver[d][i][j], instance.getCostForChangeOver());
                        }
                    }
                }

            }
        }

        IloObjective objective11 = cplex.addMinimize(obj);
    }

    // This part is the Decision Variables
    private void defineDecisionVariables() throws IloException {
        //初始化所有决定变量
        varDriving = new IloNumVar[nbDriver][nbTrip];
        varLeading = new IloNumVar[nbVehicle][nbTrip];
        varChangeOver = new IloNumVar[nbDriver][nbTrip][nbTrip];

        //2.变量的类型及取值范围声明
        //variable of vehicle use -type and range
        // variable of vehicle pass which arc ---type and range
        /**
         * add two new variable range about use of vehicle and driver
         * */
        if (this.whetherSolveRelaxationProblem == true) {

            // variable for whether diver is driving the arc---- type and range
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    varDriving[d][i] = cplex.numVar(0, 1, "o_d" + d + "_" + i);
                }
            }
            // variable the type of variable whether leading the vehicle
            for (int v = 0; v < nbVehicleRouteInput; v++) {
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

        } else { //solving integer problem


            // variable for whether diver is driving the arc---- type and range
            for (int d = 0; d < nbDriver; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    varDriving[d][i] = cplex.boolVar("o_d" + d + "_" + i);
                }
            }
            // variable the type of variable whether leading the vehicle
            for (int v = 0; v < nbVehicleRouteInput; v++) {
                for (int i = 0; i < nbTrip; i++) {
                    varLeading[v][i] = cplex.boolVar("l_v" + v + "_" + i);
                }
            }
            // the type of variable whether changeover happen to the driver
            for (int d = 0; d < nbDriverSchedulesInput; d++) {
                for (int i = 0; i < nbTrip; i++) {
                    for (int j = 0; j < nbTrip; j++) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            varChangeOver[d][i][j] = cplex.boolVar("y_d" + d + "_" + i + "_" + j);
                        }
                    }
                }
            }

        }


    }

    // here I need to rethink about it, I need to rewrite how to obtain the solution from cplex
    private Solution getSolutionFromCplex() throws IloException {
        Solution solution = new Solution(instance);
        //give vehicle path from the cplex solution
        for (int v = 0; v < inputInforms.getVehicleRoutes().size(); v++) {
            VehicleRoute vehicleRoute=inputInforms.getVehicleRoutes().get(v);
            PathForVehicle pathForVehicle= new PathForVehicle(instance,v);
            for (int i=0;i<vehicleRoute.getTripWithStartingInfosArrayList().size();i++) {
                int idTrip=vehicleRoute.getTripWithStartingInfosArrayList().get(i).getIdOfTrip();
                int startTime=vehicleRoute.getTripWithStartingInfosArrayList().get(i).getStartingTime();
                TripWithStartingInfos tripWithStartingInfos_l = new TripWithStartingInfos(instance.getTrip(idTrip),startTime);
                pathForVehicle.addTripInVehiclePath(tripWithStartingInfos_l);

                System.out.println("path for vehicle is :" + pathForVehicle);
            }
                solution.addPathInSetForVehicle(pathForVehicle);
        }

        //give crew path from cplex solution

        for (int d = 0; d < nbDriverSchedulesInput; d++) {
            DriverSchedule driverSchedule =inputInforms.getDriverSchedules().get(d);
            //int idDriver=this.schedules.getDriverSchedules()
            PathForDriver pathForDriver = new PathForDriver(instance, d);

            for (int l = 0; l < driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); l++) {
                int idTrip = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getIdOfTrip();
                int idOfVehicleInTrip = Integer.MAX_VALUE;
                boolean whetherDrive = false;
                int startTime =driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(l).getDepartureTime();
                for (int v = 0; v < inputInforms.getVehicleRoutes().size(); v++) {
                    VehicleRoute vehicleRoute=inputInforms.getVehicleRoutes().get(v);
                    if (vehicleRoute.whetherTripPresent(idTrip)) {
                        if (instance.getTrip(idTrip).getNbVehicleNeed() == 2) {
                            if (cplex.getValue(varLeading[v][idTrip]) > 0.999) {
                                System.out.println("when driver" + d + "is passenger in combine trip " + idTrip + " Leading by " + v);
                                idOfVehicleInTrip = v;
                            }
                        } else {
                            idOfVehicleInTrip = v;
                        }

                    }
                }
                System.out.println("cplex.getValue(varDriving[d][idTrip]" + d + " " + idTrip + " value is: " + cplex.getValue(varDriving[d][idTrip]));
                if (cplex.getValue(varDriving[d][idTrip]) > 0.99) {
                    whetherDrive = true;
                }

                TripWithDriveInfos tripWithDriveInfos = new TripWithDriveInfos(instance.getTrip(idTrip), idOfVehicleInTrip, whetherDrive, startTime);
                System.out.println("tripWithDriveInfos:" + idTrip + " " + whetherDrive + " " + startTime);
                pathForDriver.addTripInDriverPath(tripWithDriveInfos);

            }
            System.out.println("path for driver is :" + pathForDriver);
            solution.addPathInSetForDriver(pathForDriver);
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


    public static void main(String[] args) throws IloException, IOException {

//        InstanceReader reader = new InstanceReader("largerExample.txt");
        InstanceReader reader1 = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips035_combPer0.0_TW4.txt");

        Instance instance = reader1.readFile(); //这个语句将文本的内容就读出来了
        System.out.println(instance);


        InputInformsReader inputInformsReader = new InputInformsReader("MergedSolutionArcTimeInforms_inst_nbCity03_Size90_Day1_nbTrips035_combPer0.0_TW4.txt", instance);
        InputInforms inputInforms = inputInformsReader.readFile();
        System.out.println(inputInforms);


        SolverWithFormulationBasedOnDriverSchedule solverWithFormulation1 = new SolverWithFormulationBasedOnDriverSchedule(instance, inputInforms, false);

        // print solution
        Solution sol = solverWithFormulation1.solveWithCplexBasedOnGivenSchedules();
        if (sol != null) {
            sol.printInfile("sol_inst_nbCity03_Size90_Day1_nbTrips035_combPer0.0_TW4.txt");
        }
        //System.out.println(sol);
        System.out.println("time : " + solverWithFormulation1.getTimeInSec() + " s");
        System.out.println("lb : " + solverWithFormulation1.getLowerBound());
        System.out.println("ub : " + solverWithFormulation1.getUpperBound());
        System.out.println("idle Driver "+sol.getTotalIdleTimeCostOfAllDriver());
        System.out.println("idle Vehicle "+sol.getTotalIdleTimeCostOfAllVehicle());
    }
}
