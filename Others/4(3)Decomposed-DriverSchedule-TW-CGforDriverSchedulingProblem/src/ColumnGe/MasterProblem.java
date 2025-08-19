package ColumnGe;
import Generator.SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots;//comment subProblem

import Instance.Instance;
import Instance.Trip;
import Instance.InstanceReader;
import Solution.Solution;
import Solution.DriverSchedule;
import Solution.SchedulesReader;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.util.ArrayList;

public class MasterProblem {
    private Instance instance;
    private ArrayList<DriverSchedule> driverSchedules;
    private IloCplex cplex;
    private IloNumVar[][] varTripSelectDepartureTime; // NEW VARIABLE 2025.1.4 \sigma_i^t
    private ArrayList<IloNumVar> varWhetherSelectTheSchedule;// \theta_k
    private IloNumVar[][] varShortConnectionIsPerformed;// \rho_ij
    private IloObjective objective;
    private SubProblemSolvedByCplex subProblemSolvedByCplex;

    private SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots;// check the labeling 2025.3.3
    private IloRange[] rangeConstraintTripSelectOneDepartureTime;// NEW constraints 2025.1.4
    private IloRange[][] rangeConstraintLinkSelectScheduleAndSelectDepartureTime;// NEW constraints  2025.1.4
    private IloRange[] rangeConstraintOneDriving;
    private IloRange[] rangeConstraintShortConnectionOut;
    private IloRange[] rangeConstraintShortConnectionIn;
    private IloRange[][] rangeConstraintLinkShortConnectionAndSelectedSchedule;
    private IloRange rangeNbAvailableDriver;
    private IloRange[][] rangeConstraintUpperBoundWhetherSelectedShortConnection;
    private IloRange[][] rangeConstraintUpperBoundWhetherTripSelectDepartureTime; // NEW constraints  2025.1.4
    private static final double EPS = 0.0000001;
    private long remainingTime = 3600000;//limit for each iteration solving pricing problem
    private int iteration;
    private long startTime = System.currentTimeMillis();
    private long finalTimeCost = 0;
    private double initialRMPValueWithOnlyFeasibleSchedulesWeCreate = Double.MAX_VALUE;//this is only use for the group1, not very useful for the other group
    private double totalTimeCostForSolvingSubProblemByCplex = 0;
    private double totalTimeCostForSolvingSubProblemByLabeling = 0;//2025.3.5
    private double[] dualSolutionOfRMPCnstTripSelectOneDepartureTime;// NEWDual lambda_i 2025.1.4
    private double[][] dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime;//NewDual  delta_i_t  2025.1.4
    private double[] dualSolutionOfRMPConstraintOneDriverDriving;//beta
    private double[][] dualSolutionOfRMPConstraintLinkShortConnection;//zeta
    private double dualSolutionOfRMPConstraintNbAvailableDriver;//gamma
    private double[][] dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime;// NEWDual mu_i_t 2025.1.4

    private final double eps=0.0001;



    public MasterProblem(Instance instance, Solution solution) {
        this.instance = instance;
        this.driverSchedules = new ArrayList<>();
        initializeSchedule(solution);
        System.out.println("Initialize schedules as :");
        System.out.println("NbTrips: " + instance.getNbTrips());
        System.out.println("Ending Planning Horizon: " + instance.getEndingPlaningHorizon());
        //Comment CPLEX 1:
        this.subProblemSolvedByCplex = new SubProblemSolvedByCplex(this.instance, this);
        this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots = new SubPSolByLabelAlgorithmGeneMinRedCostPathsForAllDepots(this.instance,this);//2025.3.5

        this.dualSolutionOfRMPCnstTripSelectOneDepartureTime = new double[instance.getNbTrips()];// NEW 2025.1.4
        this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];//NEW 2025.1.4
        this.dualSolutionOfRMPConstraintOneDriverDriving = new double[instance.getNbTrips()];
        this.dualSolutionOfRMPConstraintLinkShortConnection = new double[instance.getNbTrips()][instance.getNbTrips()];
        this.dualSolutionOfRMPConstraintNbAvailableDriver =0;
        this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];//New 2025.1.9
    }


    private void initializeSchedule(Solution solution) {
        for (int s = 0; s < solution.getDriverSchedules().size(); s++) {
            DriverSchedule driverSchedule = solution.getDriverSchedules().get(s);
            this.driverSchedules.add(driverSchedule);
            // System.out.println("initialize solution driver_"+driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList());
        }
    }

    public void createModelRMP() throws IloException {
        this.cplex = new IloCplex();
        //1.1 define decision variables
        defineDecisionVariables();
        //1.2  dual variables
        dualVariables();
        //2. define Objective function
        defineObjectiveFunction();
        //3. addConstraints
        cnstSelectOneDepartureTimeForTrip();// new constraints 2025.1.6
        cnstLinkSelectScheduleAndSelectDepartureTimeOfTrip();// new constraints 2025.1.6
        cnstOneDrivingInEachTrip();
        cnstOneShortConnectionOut();
        cnstOneShortConnectionIn();
        cnstLinkShortConnectionAndSelectedSchedule();
        cnstEnoughNbDriversForSchedules();
        cnstUpperBoundOfWhetherShortConnectionIsPerformed();
        cnstUpperBoundOfSelectTripDepartureTime();//new constraints 2025.1.6
    }

    private void cnstSelectOneDepartureTimeForTrip() throws IloException { // new constraints 2025.1.6
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            for (int t = EDepTime; t <= LDepTime; t++) {
                expr.addTerm(1, this.varTripSelectDepartureTime[i][t]);
            }
            this.rangeConstraintTripSelectOneDepartureTime[i] = this.cplex.addEq(expr, 1);
        }
    }

    private void cnstLinkSelectScheduleAndSelectDepartureTimeOfTrip() throws IloException { // new constraints 2025.1.6
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            for (int t = EDepTime; t <= LDepTime; t++) {
                IloLinearNumExpr expr = this.cplex.linearNumExpr();
                for (int k = 0; k < this.driverSchedules.size(); k++) {
                    expr.addTerm(this.driverSchedules.get(k).getCoefficientF(i, t), this.varWhetherSelectTheSchedule.get(k));//change the coefficient 2025.1.9
                }
                expr.addTerm(-instance.getMaxNbDriverAvailable(), this.varTripSelectDepartureTime[i][t]);
                this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t] = this.cplex.addLe(expr, 0, "link trip_" + i + " time_" + t);
            }
        }
    }

    private void cnstOneDrivingInEachTrip() throws IloException {
        //Constraints for have only one driver driving in a trip
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            for (int k = 0; k < this.driverSchedules.size(); k++) {
                if (this.driverSchedules.get(k).getCoefficientA(i) != 0) {
                    expr.addTerm(this.driverSchedules.get(k).getCoefficientG(i), this.varWhetherSelectTheSchedule.get(k));
                }
            }
            this.rangeConstraintOneDriving[i] = this.cplex.addEq(expr, 1, "OneDrivingInTrip_" + i);
        }
    }

    private void cnstOneShortConnectionOut() throws IloException {
        //Constraints for only one short connection going out from i
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    //change 2025.1.9
                    int minConTime = instance.getMinWaitingTime(i, j);
                    if (minConTime < instance.getShortConnectionTimeForDriver()) {
                        expr.addTerm(1, this.varShortConnectionIsPerformed[i][j]);
                    }
                }

            }
            this.rangeConstraintShortConnectionOut[i] = this.cplex.addLe(expr, 1, "ShortConnectionOut" + i);
        }
    }

    private void cnstOneShortConnectionIn() throws IloException {
        //Constraints for only one short connection go into i
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //change 2025.1.9
                if (instance.whetherHavePossibleArcAfterCleaning(j, i)) {
                    int minConTime = instance.getMinWaitingTime(j, i);
                    if (minConTime < instance.getShortConnectionTimeForDriver()) {
                        expr.addTerm(1, this.varShortConnectionIsPerformed[j][i]);
                    }
                }
            }
            this.rangeConstraintShortConnectionIn[i] = this.cplex.addLe(expr, 1, "ShortConnectionIn" + i);
        }

    }

    private void cnstLinkShortConnectionAndSelectedSchedule() throws IloException {
        //Constraint for the short connection linking constraints
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //2025.1.6
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConTime = instance.getMinWaitingTime(i, j);
                    if (minConTime < this.instance.getShortConnectionTimeForDriver()) {
                        IloLinearNumExpr expr = this.cplex.linearNumExpr();
                        //until now considered all the possible short connection arc
                        for (int k = 0; k < this.driverSchedules.size(); k++) {
                            expr.addTerm(this.driverSchedules.get(k).getCoefficientForShortConnectionB(i, j), this.varWhetherSelectTheSchedule.get(k));//change the coefficient 2025.1.9
                        }
                        expr.addTerm(-instance.getMaxNbDriverAvailable(), this.varShortConnectionIsPerformed[i][j]);
                        this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j] = this.cplex.addLe(expr, 0, "link" + i + j);
                    }
                }
            }
        }
    }

    private void cnstEnoughNbDriversForSchedules() throws IloException {
        IloLinearNumExpr expr = this.cplex.linearNumExpr();
        for (int k = 0; k < this.driverSchedules.size(); k++) {
            expr.addTerm(1, this.varWhetherSelectTheSchedule.get(k));
        }
        this.rangeNbAvailableDriver = this.cplex.addLe(expr, instance.getDrivers().length);
    }

    private void cnstUpperBoundOfWhetherShortConnectionIsPerformed() throws IloException {
        //change 2025.1.6
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConTime = instance.getMinWaitingTime(i, j);//modify 2025.1.9
                    if (minConTime < instance.getShortConnectionTimeForDriver()) {
                        IloLinearNumExpr expr = this.cplex.linearNumExpr();
                        expr.addTerm(1, this.varShortConnectionIsPerformed[i][j]);
                        this.rangeConstraintUpperBoundWhetherSelectedShortConnection[i][j] = this.cplex.addLe(expr, 1);
                    }
                }
            }
        }
    }


    private void cnstUpperBoundOfSelectTripDepartureTime() throws IloException { //add constraints 2025.1.6
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int eDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int lDepTime = instance.getTrip(i).getLatestDepartureTime();
            for (int t = eDepTime; t <= lDepTime; t++) {
                IloLinearNumExpr expr = this.cplex.linearNumExpr();
                expr.addTerm(1, this.varTripSelectDepartureTime[i][t]);
                this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime[i][t] = this.cplex.addLe(expr, 1);
            }
        }
    }


    private void defineObjectiveFunction() throws IloException {
        IloLinearNumExpr obj = this.cplex.linearNumExpr();
        for (int k = 0; k < this.driverSchedules.size(); k++) {
            obj.addTerm(this.varWhetherSelectTheSchedule.get(k), this.driverSchedules.get(k).getCostC());// getCost change 2025.1.9
        }
        this.objective = this.cplex.addMinimize(obj);
    }

    private void dualVariables() {
        //dual value
        this.rangeConstraintTripSelectOneDepartureTime = new IloRange[this.instance.getNbTrips()];// 2025.1.6 new lambda_i
        this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime = new IloRange[instance.getNbTrips()][instance.getEndingPlaningHorizon()];// 2025.1.6 delta_i^t
        this.rangeConstraintOneDriving = new IloRange[this.instance.getNbTrips()];//beta
        this.rangeConstraintShortConnectionOut = new IloRange[this.instance.getNbTrips()];//xi
        this.rangeConstraintShortConnectionIn = new IloRange[this.instance.getNbTrips()];//eta
        this.rangeConstraintLinkShortConnectionAndSelectedSchedule = new IloRange[this.instance.getNbTrips()][this.instance.getNbTrips()];//zeta
        //gamma 1 dem 不需要这么写
        this.rangeConstraintUpperBoundWhetherSelectedShortConnection = new IloRange[this.instance.getNbTrips()][this.instance.getNbTrips()];//alpha
        this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime = new IloRange[this.instance.getNbTrips()][instance.getEndingPlaningHorizon()];// 2025.1.6 mu_i_t
    }

    private void defineDecisionVariables() throws IloException {
        //1 decision variables
        this.varTripSelectDepartureTime = new IloNumVar[instance.getNbTrips()][instance.getEndingPlaningHorizon()];// new add 2025.1.6
        this.varWhetherSelectTheSchedule = new ArrayList<>();
        this.varShortConnectionIsPerformed = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];

        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            for (int t = EDepTime; t <= LDepTime; t++) {
                this.varTripSelectDepartureTime[i][t] = cplex.numVar(0, Double.MAX_VALUE, "sigma");// new add 2025.1.6
            }
        }

        for (int k = 0; k < this.driverSchedules.size(); k++) {
            IloNumVar varSelect = cplex.numVar(0, Double.MAX_VALUE, "theta");
            this.varWhetherSelectTheSchedule.add(varSelect);
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                // modify 2025.1.6 the range to all the arc which have possibility to cause the short connection
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConTime = instance.getMinWaitingTime(i, j);
                    if (minConTime < instance.getShortConnectionTimeForDriver()) {
                        this.varShortConnectionIsPerformed[i][j] = cplex.numVar(0, Double.MAX_VALUE, "rho");// new change 2025.1.6
                    }
                }
            }
        }
    }

    public int[] solveRMPWithCplex() {// void need to be modified
        // Here is the first time solve RMP with cplex
        try {
            //create the model
            createModelRMP();
            // export the model
            cplex.exportModel("RMP.lp");

            cplex.setParam(IloCplex.Param.TimeLimit, remainingTime / 1000); //here we limit the running time is 1 hour
            cplex.setParam(IloCplex.Param.Threads, 1);
            // solve
            cplex.setOut(System.out);
            cplex.solve();
            System.out.println("MasterProblem obj = " + this.cplex.getObjValue());
            //***Here is to store the dual value in order to give  parameter when calculate the reduced cost****************
            storeDualVals();

            this.dualSolutionOfRMPConstraintNbAvailableDriver = cplex.getDual(this.rangeNbAvailableDriver);//gamma
            //System.out.println("Check dual gamma: "+cplex.getDual(this.rangeNbAvailableDriver));
            // the following is the information to print out
            printOutInformation();

        } catch (IloException e) {
            throw new RuntimeException(e);
        }
        return new int[0];
    }

    private void printOutInformation() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int eDepT = instance.getTrip(i).getEarliestDepartureTime();
            int lDepT = instance.getTrip(i).getLatestDepartureTime();
            for (int t = eDepT; t <= lDepT; t++) {
                if (cplex.getValue(varTripSelectDepartureTime[i][t]) > 0.99) {
                    //System.out.println(" trip_"+i+" is select departure time "+t);
                }
            }
        }

        for (int k = 0; k < this.driverSchedules.size(); k++) {
            if (cplex.getValue(varWhetherSelectTheSchedule.get(k)) > 0.99) { // Variable in master problem theta
//                    System.out.println("schedule:" + this.driverSchedules.get(k));
            }
        }
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaiting = instance.getMinWaitingTime(i, j);
                    if (minWaiting < instance.getShortConnectionTimeForDriver()) {// Variable in master problem rho
                        if (cplex.getValue(varShortConnectionIsPerformed[i][j]) > 0.99) {
                            // System.out.println(" short connection between trip_" + i + " and trip_" + j+ "is performed, variable value" +cplex.getValue(varShortConnectionIsPerformed[i][j]));
                        }
                    }
                }
            }
        }
    }

    private void storeDualVals() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            this.dualSolutionOfRMPCnstTripSelectOneDepartureTime[i] = cplex.getDual(this.rangeConstraintTripSelectOneDepartureTime[i]);// 2025.1.9 lambda_i
            //System.out.println("Check Dual lambda_i"+this.dualSolutionOfRMPCnstTripSelectOneDepartureTime[i]);
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();
            for (int t = eDepTime; t <= lDepTime; t++) {
                this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime[i][t] = cplex.getDual(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t]);// 2025.1.9 delta_i^t
                // System.out.println("Check Dual delta_i_"+i+"_t_"+t+": "+this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime[i][t]);
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            this.dualSolutionOfRMPConstraintOneDriverDriving[i] = cplex.getDual(this.rangeConstraintOneDriving[i]);//beta_i
            //System.out.println("Check Dual in master problem beta_i_ "+i+" "+this.dualSolutionOfRMPConstraintOneDriverDriving[i]);
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //change 2025.2.11 change to stricly less than
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        this.dualSolutionOfRMPConstraintLinkShortConnection[i][j] = cplex.getDual(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j]);//zeta
                         //System.out.println("Check Dual zeta_i_"+i+"_j_"+j+" : "+this.dualSolutionOfRMPConstraintLinkShortConnection[i][j]);
                    }
                }
            }
        }


        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();
            for (int t = eDepTime; t <= lDepTime; t++) {

                this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime[i][t] = cplex.getDual(this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime[i][t]);// mu_i^T
                // System.out.println("Check Dual mu_i_"+i+"_t_"+ t+": "+this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime[i][t]);
            }

        }
    }

    public double[] getDualFromConstraintTripSelectOneDepartureTime() {//2025.1.9
        double dual[] = new double[instance.getNbTrips()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            try {
                dual[i] = this.cplex.getDual(this.rangeConstraintTripSelectOneDepartureTime[i]);
            } catch (IloCplex.UnknownObjectException e) {
                throw new RuntimeException(e);
            } catch (IloException e) {
                throw new RuntimeException(e);
            }
        }
        return dual;
    }

    public double[][] getDualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect() {//2025.1.9
        double dual[][] = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDep = trip.getEarliestDepartureTime();
            int lDep = trip.getLatestDepartureTime();
            for (int t = eDep; t <= lDep; t++) {
                //System.out.println("check dual in Link "+ dual);
                try {
                    dual[i][t] = this.cplex.getDual(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t]);
                    //System.out.println("check dual dela"+ dual[i][t]+ "constraints "+ this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t]);
                } catch (IloCplex.UnknownObjectException e) {
                    throw new RuntimeException(e);
                } catch (IloException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return dual;
    }

    public double[] getDualFromConstraintOneDriving() {
        double[] dual = new double[instance.getNbTrips()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            try {
                dual[i] = this.cplex.getDual(this.rangeConstraintOneDriving[i]);
            } catch (IloException e) {
                throw new RuntimeException(e);
            }
        }
        return dual;

    }

    public double[] getDualFromConstraintShortConnectionOut() {//change 2025.1.9
        double[] dual = new double[instance.getNbTrips()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    //change 2025.1.6
                    int minWaiting = instance.getMinWaitingTime(i, j);
                    if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                        try {
                            // System.out.println(this.rangeConstraintShortConnectionOut[i]);
                            dual[i] = this.cplex.getDual(this.rangeConstraintShortConnectionOut[i]);
                        } catch (IloException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }

            }

        }
        return dual;
    }

    public double[] getDualFromConstraintShortConnectionIn() {//change 2025.1.9
        double[] dual = new double[instance.getNbTrips()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //change 2025.1.6
                if (instance.whetherHavePossibleArcAfterCleaning(j, i)) {
                    int minWaiting = instance.getMinWaitingTime(j, i);
                    if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                        try {
                            dual[i] = this.cplex.getDual(this.rangeConstraintShortConnectionIn[i]);
                        } catch (IloException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }

        }
        return dual;
    }

    public double[][] getDualFromConstraintLinkShortConnection() {//change 2025.1.9
        double dual[][] = new double[instance.getNbTrips()][instance.getNbTrips()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //change 2025.1.6
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        try {
                            dual[i][j] = this.cplex.getDual(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j]);
                            //System.out.println("check dual link short connection "+dual[i][j]);
                        } catch (IloException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
        }
        return dual;
    }

    public double getDualFromConstraintAvailableDriver() {
        double dual = 0;
        try {
            dual = this.cplex.getDual(this.rangeNbAvailableDriver);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return dual;
    }


    public double[][] getDualFromConstraintWhetherSelectShortConnection() {//change 2025.1.9
        double dual[][] = new double[instance.getNbTrips()][instance.getNbTrips()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConTime = instance.getMinWaitingTime(i, j);
                    if (minConTime < instance.getShortConnectionTimeForDriver()) {
                        try {
                            dual[i][j] = this.cplex.getDual(this.rangeConstraintUpperBoundWhetherSelectedShortConnection[i][j]);
                        } catch (IloCplex.UnknownObjectException e) {
                            throw new RuntimeException(e);
                        } catch (IloException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }
        return dual;
    }


    public double[][] getDualFromConstraintsUpperBoundTripSelectDepartureTime() {
        double dual[][] = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int eDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int lDepTime = instance.getTrip(i).getLatestDepartureTime();
            for (int t = eDepTime; t <= lDepTime; t++) {
                try {
                    dual[i][t] = this.cplex.getDual(this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime[i][t]);
                    //System.out.println("check dual "+dual[i][t]);
                } catch (IloCplex.UnknownObjectException e) {
                    throw new RuntimeException(e);
                } catch (IloException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return dual;// mu_i_t
    }

    double finalMasterObjectiveValue = Double.MAX_VALUE;// This is not the real lowerBound
    double finalBestLowerBoundOfMP = 0;//Double.NEGATIVE_INFINITY;// negative infinity? TODO: Check whether here I should put the lower bound be zero?

    double finalIntegerValueOfMasterObjective = Integer.MAX_VALUE;

    //这里是最后一步

    public Solution solve() throws IloException {
        //(0) create the Model
        System.out.println("Here is to solve the first RMP");
        //(1) solve the RMP
        solveRMPWithCplex();
        initialRMPValueWithOnlyFeasibleSchedulesWeCreate = cplex.getObjValue();
        System.out.println("initial RMP value: " + initialRMPValueWithOnlyFeasibleSchedulesWeCreate);
        storeDualVals();//再一次存储对偶变量的值
        //Comment 2: solve the subProblemSolvedByCplex with Cplex
        this.subProblemSolvedByCplex.initializeModelForSubProblem();//initialize the subProblemSolvedByCplex model without dual coeffi
        double firstStartTimeOfCplexSolveSubProblem = System.currentTimeMillis();
        DriverSchedule bestDriverSchedule = this.subProblemSolvedByCplex.solveSubProblemWithCplex();//when solving change objective function every time, because the dual variable change

        System.out.println("Best Driver schedule: " + bestDriverSchedule);
        double firstEndTimeOfCplexSolveSubProblem = System.currentTimeMillis();
        double firstDurationOfCplexSolveSubProblem = firstEndTimeOfCplexSolveSubProblem - firstStartTimeOfCplexSolveSubProblem;
        totalTimeCostForSolvingSubProblemByCplex = firstDurationOfCplexSolveSubProblem;
        System.out.println("initial time cost subProblem solved by cplex" + firstDurationOfCplexSolveSubProblem);
        System.out.println("Check reduce cost of cplex solve the sub-problem:  " + this.subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex());
        System.out.println("Check 1 the first best lower bound " + finalBestLowerBoundOfMP);
        System.out.println("Check 1 the Lagrangian lower bound " + this.getLagrangianLowerBoundOnOptimalValueOfRMP());
        //************(2)******solve the sub-problem with labeling and record the time, the difference is that labeling could report more than one column
//        //TODO : ADD RECORD TIME FOR LABLEING
        ArrayList<DriverSchedule> bestSchedules= subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.generateSchedulesWithMostMinReducedCostAmongAllStartingTrips();
        System.out.println("Here are the schedules with min reduced cost generated by labeling"+bestSchedules);
        double reducedCostLabeling=this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getMostMinReducedCost();
        boolean whethereReducedCostSameAsCplex=(Math.abs(reducedCostLabeling-this.subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex())<eps);
        System.out.println("Compare reduced cost whether is the same "+whethereReducedCostSameAsCplex);
        if(whethereReducedCostSameAsCplex==false){
            System.out.println("reducedCost by cplex"+this.subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex());
            System.out.println("reducedCost by labeling"+reducedCostLabeling);
        }

        if (this.getLagrangianLowerBoundOnOptimalValueOfRMP() > finalBestLowerBoundOfMP) {
            finalBestLowerBoundOfMP = this.getLagrangianLowerBoundOnOptimalValueOfRMP();//初始化lower bound 为当前的 lagrangian bound (因为有的问题不需要迭代就可以获得最优解) 跟0当中比较大的那个
        }
        System.out.println("Check 2 the second best lower bound after initialize" + finalBestLowerBoundOfMP);
        //(3) start the iteration
        iteration = 0;
//        Comment 3: the while stop condition
        while (bestDriverSchedule.getReducedCost(this.getDualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect(),
                this.getDualFromConstraintOneDriving(),
                this.getDualFromConstraintLinkShortConnection(),
                this.getDualFromConstraintAvailableDriver()) < -EPS) {
            if(iteration==8){
                System.out.println("We stop here with cplex best schedule "+bestDriverSchedule);
                System.out.println("best reduced cost for cplex "+this.subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex());
            }
            if(iteration==9){
                this.subProblemSolvedByCplex.cnstForceSchedule();
                bestDriverSchedule=this.subProblemSolvedByCplex.solveSubProblemWithCplex();
                System.out.println("now iteration==9  the best schedule is "+ bestSchedules);
                System.out.println("now iteration==9 best reduced cost is "+this.subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex());
                break;
            }
            long currentTime = System.currentTimeMillis();
            long iterationElapsedTime = (currentTime - startTime) / 1000;
            if (iterationElapsedTime >= 3600) {//here is to limit the iteration time, if the iteration time is less than 1 hour, then continue
                System.out.println("final iteration is " + iteration);
                System.out.println("final columns is " + varWhetherSelectTheSchedule.size());
                break;
            }
            //Comment 4: add the bestDriverSchedule
            this.driverSchedules.add(bestDriverSchedule);
            //Comment 5: add the bestDriverSchedule column into the master problem
            iteration = iteration + 1;
            System.out.println("nbIteration: " + iteration);
            //update the variable in the RMP ( objective and constraints add new coefficient)
            System.out.println("getCostOfSchedule of the best schedule: " + bestDriverSchedule.getCostC());
            System.out.println("Elapsed time: " + iterationElapsedTime + " seconds");

            IloColumn newColumn = this.cplex.column(objective, bestDriverSchedule.getCostC());//这里需要核实一在目标函数中新增项的系数ck
            System.out.println("add new column with coefficient " + bestDriverSchedule.getCostC());
            // 这里改变新增一列后对整体约束条件有哪些变化，哪些需要额外累计上去
            for (int i = 0; i < instance.getNbTrips(); i++) {
                Trip trip = instance.getTrip(i);
                int eDepT = trip.getEarliestDepartureTime();
                int lDep = trip.getLatestDepartureTime();
                for (int t = eDepT; t <= lDep; t++) {
                    newColumn = newColumn.and(cplex.column(this.rangeConstraintOneDriving[i], 0));// 没影响
                }
            }

            for (int i = 0; i < instance.getNbTrips(); i++) {
                Trip trip = instance.getTrip(i);
                if (bestDriverSchedule.getCoefficientA(i) != 0) {
                    int eDepT = trip.getEarliestDepartureTime();
                    int lDep = trip.getLatestDepartureTime();
                    for (int t = eDepT; t <= lDep; t++) {
                        newColumn = newColumn.and(cplex.column(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t], bestDriverSchedule.getCoefficientF(i, t)));

                    }
                }
            }

            for (int i = 0; i < instance.getNbTrips(); i++) {
                newColumn = newColumn.and(cplex.column(this.rangeConstraintOneDriving[i], bestDriverSchedule.getCoefficientG(i)));
            }

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    // 2025.1.6
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int minWaiting = instance.getMinWaitingTime(i, j);
                        if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                            //here consider all the short connection
                            if (bestDriverSchedule.whetherIsShortConnectionInDriverSchedule(i, j)) { // 没影响// here we only consider the short connection in the best schedule
                                newColumn = newColumn.and(cplex.column(this.rangeConstraintShortConnectionOut[i], 0));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    //2025.1.6
                    if (instance.whetherHavePossibleArcAfterCleaning(j, i)) {
                        //if (instance.getConnectionTime(j, i) <= instance.getShortConnectionTimeForDriver()) {
                        int minWaiting = instance.getMinWaitingTime(j, i);
                        if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                            // here are consier all the  i
                            if (bestDriverSchedule.whetherIsShortConnectionInDriverSchedule(j, i)) {// 没影响// here only consider whether the best schedule have short connection
                                newColumn = newColumn.and(cplex.column(this.rangeConstraintShortConnectionIn[i], 0));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    //2025.1.6
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        int minWaiting = instance.getMinWaitingTime(i, j);
                        if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                                newColumn = newColumn.and(cplex.column(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j], bestDriverSchedule.getCoefficientForShortConnectionB(i, j)));
                        }
                    }
                }
            }
            newColumn = newColumn.and(cplex.column(this.rangeNbAvailableDriver, 1));

            IloNumVar x = cplex.numVar(newColumn, 0, Double.MAX_VALUE);
            this.varWhetherSelectTheSchedule.add(x);

            long beforeCplexSolve = System.currentTimeMillis() - startTime;
            remainingTime = 3600000 - beforeCplexSolve;
            System.out.println("remainingTime is: " + remainingTime);
            //check whether changeover variable need to add? no, it never changes according to the column
            //solve the RMP again
            solveRMPWithCplex();

            System.out.println("MasterProblem re-solve RMP with cplex obj = " + this.getMPObjValue());
            if(this.getMPObjValue()<0){
                System.out.println("There is an error: Check now the check the obj is less than 0 in iteration "+iteration+" with value "+this.getMPObjValue());
                break;
            }
            //solve the subProblemSolvedByCplex with Cplex();
            double startTimeIterationCplexSolveSubProblem = System.currentTimeMillis();
            bestDriverSchedule = this.subProblemSolvedByCplex.solveSubProblemWithCplex();
            double endTimeIterationCplexSolveSubProblem = System.currentTimeMillis();
            double durationTimeIterationCplexSolveSubProblem = endTimeIterationCplexSolveSubProblem - startTimeIterationCplexSolveSubProblem;
            totalTimeCostForSolvingSubProblemByCplex = totalTimeCostForSolvingSubProblemByCplex + durationTimeIterationCplexSolveSubProblem;

            System.out.println("solve RMP again  by cplex with  new column: " + bestDriverSchedule);
            double reducedCostFromCplexAgain = this.subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex();
            System.out.println("reduced cost solved by Cplex is: " + reducedCostFromCplexAgain);
            //above is the problem solveWithRMP again ***************************************************************************************************************************

            // now we start to solved by the labeling algorithm***********
            bestSchedules=this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.generateSchedulesWithMostMinReducedCostAmongAllStartingTrips();
            reducedCostLabeling=this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getMostMinReducedCost();

            boolean whetherReducedCostIsSameAgain=(Math.abs(reducedCostFromCplexAgain-reducedCostLabeling)<eps);
            System.out.println("Compare reduced cost whether is the same "+ whetherReducedCostIsSameAgain);
            if(whetherReducedCostIsSameAgain==false){
                System.out.println("reducedCost by cplex"+subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex());
                System.out.println("reducedCost by labeling"+reducedCostLabeling);
            }

            //above is we try to solved by the labeling algorithm again*************
            double LagrangianLowerBoundInCurrentIteration;
            LagrangianLowerBoundInCurrentIteration = this.getLagrangianLowerBoundOnOptimalValueOfRMP();

            if (LagrangianLowerBoundInCurrentIteration >= finalBestLowerBoundOfMP) {
                finalBestLowerBoundOfMP = LagrangianLowerBoundInCurrentIteration;
                System.out.println("final best lower bound is updated to: " + finalBestLowerBoundOfMP);
            }

            System.out.println("Check the current best lower bound from lagrangian bound which should greater than before is " + finalBestLowerBoundOfMP);

            long iterationElapsedTimeAfterCplexSolve = (System.currentTimeMillis() - startTime) / 1000;

            System.out.println("current elapsed time after finishing this iteration is: " + iterationElapsedTimeAfterCplexSolve);

            if (iterationElapsedTimeAfterCplexSolve >= 3600 || remainingTime <= 120000) {//time remaining less than 2 minutes
                System.out.println("final iteration is " + iteration);
                System.out.println("final columns is " + varWhetherSelectTheSchedule.size());
                break;
            }
        }

        finalMasterObjectiveValue = this.getMPObjValue();
        System.out.println("final master obj=" + this.getMPObjValue());
        System.out.println("final best lower bound: " + finalBestLowerBoundOfMP);

        //After the loop, the variables in the model are converted to integer (binary) variables using the IloConversion object.
        for (int k = 0; k < this.varWhetherSelectTheSchedule.size(); k++) {
            IloConversion conv = this.cplex.conversion(this.varWhetherSelectTheSchedule.get(k), IloNumVarType.Int);
            this.cplex.add(conv);
            //this.getMPObjValue();
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //2025.1.6
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minCon = instance.getMinWaitingTime(i, j);
                    if (minCon < instance.getShortConnectionTimeForDriver()) {
                        IloConversion conv = this.cplex.conversion(this.varShortConnectionIsPerformed[i][j], IloNumVarType.Int);
                        this.cplex.add(conv);
                    }
                }
            }
        }

        for(int i=0;i<instance.getNbTrips();i++){
            Trip trip =instance.getTrip(i);
            int eDep=trip.getEarliestDepartureTime();
            int lDep=trip.getLatestDepartureTime();
            for(int t=eDep;t<=lDep;t++){
                IloConversion conv =this.cplex.conversion(this.varTripSelectDepartureTime[i][t],IloNumVarType.Int);
                this.cplex.add(conv);
            }
        }

        //  The final master problem is solved using cplex.solve() after the variables are converted to integer.
        //  after change the variable into integer, it means I had changed my problem structure, so I need to solve the problem again.
        this.cplex.setOut(System.out);

        long beforeTheLastCplexSolve = System.currentTimeMillis();
        if (beforeTheLastCplexSolve >= 3600) {
            this.cplex.setParam(IloCplex.Param.TimeLimit, 300);
            this.cplex.setParam(IloCplex.Param.Threads, 1);
        }
        this.cplex.solve();

        finalIntegerValueOfMasterObjective = this.getMPObjValue();
        System.out.println("Final integer master objective: " + this.getMPObjValue());

//        System.out.println("lagrangian bound here is "+ this.getLagrangianLowerBoundOnOptimalValueOfRMP());
        Solution solutionFromCG = new Solution(this.instance);
        for (int k = 0; k < this.driverSchedules.size(); k++) {
            if (cplex.getValue(this.varWhetherSelectTheSchedule.get(k)) > 0.999) {
                solutionFromCG.addSchedule(this.driverSchedules.get(k));
            }
        }

        finalTimeCost = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("final time cost:" + finalTimeCost);

        this.cplex.close();
        return solutionFromCG;

    }

    // here is prepare the dual value for the labeling algorithm********2025.3.3
    public double getDualValueFromOneDriving(int idOfTask){
        return this.dualSolutionOfRMPConstraintOneDriverDriving[idOfTask];
    }
    public double getDualValueFromNbAvailableDriver(){
        return this.dualSolutionOfRMPConstraintNbAvailableDriver;
    }
    public double getDualValueFromLinkSelectScheduleAndTripOneDepTime(int idTask,int depTime){
        return this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime[idTask][depTime];

    }

    public double getDualValueFromLinkDriverShortConTime(int idFTask, int idSTask){
        return this.dualSolutionOfRMPConstraintLinkShortConnection[idFTask][idSTask];
    }
            //***


    public double getLagrangianLowerBoundOnOptimalValueOfRMP() throws IloException {
        double LagrangianLowerBound;
        double sumLambda = 0;//2025.1.6
        double sumDelta = 0;//2025.1.6
        double sumXi = 0;
        double sumEta = 0;
        double sumAlpha = 0;
        double sumZeta = 0;
        double sumMu = 0;//2025.1.6

        for (int i = 0; i < instance.getNbTrips(); i++) {
            //2025.1.6
            Trip trip = instance.getTrip(i);
            int eDep = trip.getEarliestDepartureTime();
            int lDep = trip.getLatestDepartureTime();
            for (int t = eDep; t <= lDep; t++) {
                sumLambda = sumLambda + this.getDualFromConstraintTripSelectOneDepartureTime()[i];//20256.1.6 new lambda
            }
        }
        //System.out.println("Check sumLambda " + sumLambda);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            //2025.1.6
            Trip trip = instance.getTrip(i);
            int eDep = trip.getEarliestDepartureTime();
            int lDep = trip.getLatestDepartureTime();
            for (int t = eDep; t <= lDep; t++) {
                sumDelta = sumDelta + this.getDualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect()[i][t];//20256.1.6 new lambda
            }
        }
        //System.out.println("Check sumDelta " + sumDelta);
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) { //2025.1.10
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) { // change 2025.1.10
                        sumXi = sumXi + this.getDualFromConstraintShortConnectionOut()[i];
                    }
                }
            }
        }
       // System.out.println("sumXi " + sumXi);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {//2025.1.10
                if (instance.whetherHavePossibleArcAfterCleaning(j, i)) {
                    int minWaitingTime = instance.getMinWaitingTime(j, i);
                    if (minWaitingTime < this.instance.getShortConnectionTimeForDriver()) {
                        sumEta = sumEta + this.getDualFromConstraintShortConnectionIn()[i];
                    }
                }
            }
        }
        //System.out.println("sumEta " + sumEta);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //2025.1.10
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        sumAlpha = sumAlpha + this.getDualFromConstraintWhetherSelectShortConnection()[i][j];
                    }
                }
            }
        }
       // System.out.println("sumAlpha " + sumAlpha);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //2025.1.6
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        sumZeta = sumZeta + this.getDualFromConstraintLinkShortConnection()[i][j];
                    }
                }
            }
        }
       // System.out.println("sumZeta " + sumZeta);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = this.instance.getTrip(i);
            int eDep = trip.getEarliestDepartureTime();
            int lDep = trip.getLatestDepartureTime();
            for (int t = eDep; t <= lDep; t++) {
                sumMu = sumMu + this.getDualFromConstraintsUpperBoundTripSelectDepartureTime()[i][t];
            }

        }
       // System.out.println("sumMu " + sumMu);

        //Comment 7: the lagrangianLowerbound
        LagrangianLowerBound = this.cplex.getObjValue() - sumLambda - sumMu - sumXi - sumEta - sumAlpha
                + instance.getMaxNbDriverAvailable() * (sumZeta + this.subProblemSolvedByCplex.getReducedCostOfSubProblemFromCplex())
                + instance.getMaxNbDriverAvailable() * sumDelta;
        return LagrangianLowerBound;
    }

    public double getFinalMasterObjectiveValue() {
        return finalMasterObjectiveValue;
    }

    public double getFinalLowerBoundOfMP() {
        return finalBestLowerBoundOfMP;
    }

    public double getFinalIntegerValueOfMasterObjective() {
        return finalIntegerValueOfMasterObjective;
    }

    public int getFinalNbIterations() {
        return iteration;
    }

    public int getFinalColumns() {
        return varWhetherSelectTheSchedule.size();
    }

    public double getMPObjValue() throws IloException {
        return this.cplex.getObjValue();
    }

    public double getFinalTimeCostInSec() {
        return finalTimeCost;
    }

    public double getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() {
        //double decimalGap = 100 * (this.finalIntegerValueOfMasterObjective - this.getFinalMasterObjectiveValue()) / this.getFinalIntegerValueOfMasterObjective();
        double decimalGap = 100 * (this.finalIntegerValueOfMasterObjective - this.getFinalLowerBoundOfMP()) / this.getFinalIntegerValueOfMasterObjective();
        return decimalGap;
    }

    public double getInitialRMPValue() {
        return initialRMPValueWithOnlyFeasibleSchedulesWeCreate;
    }

    public double getTotalTimeCostForSolvingSubProblemByCplex() {
        return this.totalTimeCostForSolvingSubProblemByCplex;
    }


    public static void main(String[] args) throws IloException, IOException {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips035_combPer0.0_TW0.txt");//test vehicle example
        //inst_nbCity20_Size300_Day3_nbTrips300_combPer0.1
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        System.out.println(instance.getTimeWindowRange());

        SchedulesReader schedulesReader = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips035_combPer0.0_TW0.txt", instance);
        //scheduleSolution_inst_nbCity05_Size180_Day1_nbTrips020_combPer0.0
        Solution initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);
        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());

        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
        masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况

        Solution solution = masterProblem.solve();//这一步求解的是最终迭代完之后的最终结果。
        solution.printDriverSchedulingSolutionInFile("sol.txt");
        // 注意这里的结果尽管目标函数值都是95，
        // 但是Pattern的样式确与Cplex求解的结果sol.text 中的不一样
        System.out.println(masterProblem);
//        System.out.println("whether solution is feasible: " + solution.whetherSolutionIsFeasible());
        System.out.println("initial columns: " + initialSchedules.getDriverSchedules().size());

        System.out.println("final masterProblem objective Value: " + masterProblem.getFinalMasterObjectiveValue());
        System.out.println("final integer objective value: " + masterProblem.getFinalIntegerValueOfMasterObjective());
        System.out.println("nbOfIteration: " + masterProblem.iteration);
        System.out.println("nbFinalColumns: " + masterProblem.varWhetherSelectTheSchedule.size());
        System.out.println("final lower bound: " + masterProblem.getFinalLowerBoundOfMP());

        System.out.println("time in sec " + masterProblem.getFinalTimeCostInSec() + " sec");
        System.out.println("gap: " + masterProblem.getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() + "%");
        System.out.println("total time in milli second of cplex solving pricing problem: " + masterProblem.getTotalTimeCostForSolvingSubProblemByCplex() + " milli seconds ");
        //System.out.println("total time in milli second of labeling algorithm solving pricing problem: " + masterProblem.getTotalTimeCostForSolvingSubProblemByLabeling() + " milli seconds ");


    }

}
