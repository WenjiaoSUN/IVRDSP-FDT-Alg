package ColumnGe;

import Generator.SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots;

import Instance.Instance;
import Instance.Trip;
import Instance.TripWithWorkingStatusAndDepartureTime;
import Instance.InstanceReader;
import Solution.Solution;
import Solution.DriverSchedule;
import Solution.SchedulesReader;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;

public class MasterProblem {
    private Instance instance;
    private ArrayList<DriverSchedule> driverSchedules;

    private ArrayList<DriverSchedule> kBestDriverSchedules;// add 0612
    private IloCplex cplex;
    private IloNumVar[][] varTripSelectDepartureTime; // NEW VARIABLE 2025.1.4 \sigma_i^t
    private ArrayList<IloNumVar> varWhetherSelectTheSchedule;// \theta_k
    private IloNumVar[][] varShortConnectionIsPerformed;// \rho_ij
    private IloObjective objective;
    private SubProblemSolvedByCplex subProblemSolvedByCplex;

    private SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots;// check the labeling 2025.3.3
    private IloRange[] rangeConstraintTripSelectOneDepartureTime;// NEW constraints 2025.1.4
    private IloRange[][] rangeConstraintLinkSelectScheduleAndSelectDepartureTime;// NEW constraints  2025.1.4
    private IloRange[] rangeConstraintOneDriving;
    private IloRange[] rangeConstraintShortConnectionOut;
    private IloRange[] rangeConstraintShortConnectionIn;
    private IloRange[][] rangeConstraintLinkShortConnectionAndSelectedSchedule;
    private IloRange rangeNbAvailableDriver;
    private IloRange[][] rangeConstraintUpperBoundWhetherSelectedShortConnection;
    private IloRange[][] rangeConstraintUpperBoundWhetherTripSelectDepartureTime; // NEW constraints  2025.1.4
    private static final double EPS = 1e-7;
    private long remainingTime = 3600000;//limit for each iteration solving pricing problem
    private int iteration;
    private long startTime;
    private long finalTimeCost = 0;
    private double initialRMPValueWithOnlyFeasibleSchedulesWeCreate = Double.MAX_VALUE;//this is only use for the group1, not very useful for the other group
    private double totalTimeCostForSolvingSubProblemByCplex = 0;//2025.4.3

    private double totalTimeOnDominanceRuleForAllIteration = 0;//2025.4.3

    private long totalLabelsGenerateForAllIterations = 0;//2025.4.3
    private int finalNbLabelsLastIteration = 0;
    private double totalTimeCostForSolvingSubProblemByLabeling = 0;//2025.3.5

    public double totalTimeOfLabeling = 0;
    private double totalTimeOfSolRMPByCplex;// 2025.3.19
    private double[] dualSolutionOfRMPCnstTripSelectOneDepartureTime;// NEWDual lambda_i 2025.1.4
    private double[][] dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime;//NewDual  delta_i_t  2025.1.4
    private double[] dualSolutionOfRMPConstraintOneDriverDriving;//beta
    private double[] dualSolutionOfRMPCnstShortConnectionOut;//xi_i
    private double[] dualSolutionOfRMPCnstShortConnectionIn;//eta_i
    private double[][] dualSolutionOfRMPConstraintLinkShortConnection;//zeta

    private double[][] dualSolutionOfRMPCnstUpperboundWhetherSelectDriverShortConnection;//alpha
    private double dualSolutionOfRMPConstraintNbAvailableDriver;//gamma
    private double[][] dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime;// NEWDual mu_i_t 2025.1.4

    private double timeForSolvingLastIntegerProblemInSec = 0;
    private double timeForPostProcessOfRichScheduleByDepartureTime = 0;
    private double timeForCreateModel=0;

    private int nbColumnsGeneratedByPostProcess = 0;

    // ========= NEW MIP MODEL VARIABLES (Following your naming style) =========
    private IloCplex newCplex; // New CPLEX model for departure time optimization
    private ArrayList<IloNumVar> newVarWhetherSelectTheSchedule; // θ_k: schedule selection from ΩR
    private IloNumVar[][] newVarTripSelectDepartureTime; // σ_i^t: trip departure time selection
    private IloNumVar[][] newVarShortConnectionIsPerformed; // ρ_ij: short connection variables
    //Extra new variables
    private ArrayList<IloNumVar[][]> newVarIdleTimeBetweenTripsInSchedule; // t_ij^k: idle time between trips in schedules
    private ArrayList<IloNumVar[][]> newVarAuxiliaryZ; // z_ij^k:=theta^k*b_ij^k  auxiliary binary variables
    private ArrayList<IloNumVar[][]> newVarAuxiliaryBIsShortConInSchedule; // b_ij^k: auxiliary binary variables
    private ArrayList<IloNumVar[][]> newVarAuxiliaryX;// x_it^k=theta^k*sigma_it
    private ArrayList<IloNumVar[][]> newVarAuxiliaryU;// u_ij^k=t_ij^k* theta_k

    private IloObjective newObjective; // New objective function

    // ========= NEW MIP MODEL CONSTRAINTS (Following your naming style) =========
    private IloRange[] newRangeConstraintTripSelectOneDepartureTime; // ∑(t∈[Ei,Li]) σ_i^t = 1
    private IloRange[][] newRangeConstraintLinkSelectScheduleAndSelectDepartureTime; // ∑f_i^k*θk - |D|*σ_i^t ≤ 0
    private IloRange[] newRangeConstraintOneDriving; // ∑g_i^k*θk = 1
    private IloRange[] newRangeConstraintShortConnectionOut; // ∑ρ_ij ≤ 1
    private IloRange[] newRangeConstraintShortConnectionIn; // ∑ρ_ji ≤ 1
    private IloRange[][] newRangeConstraintLinkZAndRho; // ∑z_ij^k ≤ |D|*ρ_ij
    private IloRange newRangeConstraintDriverAvailability; // ∑θk ≤ |D|

    // ========= ADDITIONAL CONSTRAINTS FOR NEW FORMULATION =========
    private IloRange[] newRangeConstraintMaxWorkingTime; // Driver maximum working time
    private ArrayList<IloRange[][]> newRangeConstraintIdleTimeLowerBound; // T^minP*h_ij^k ≤ t_ij^k
    private ArrayList<IloRange[][]> newRangeConstraintIdleTimeUpperBound; // t_ij^k ≤ (Lj-(Ei+Di))*h_ij^k
    private ArrayList<IloRange[][]> newRangeConstraintLinkageIdleDepChoiceLower; // Idle time range lower bound
    private ArrayList<IloRange[][]> newRangeConstraintLinkageIdleDepChoiceUpper; // Idle time range upper bound
    private ArrayList<IloRange[][]> newRangeConstraintShortConnectionDetection; // Short connection detection

    private ArrayList<IloRange[][]> newRangeConstraintZBLink; // z_ij^k ≤ b_ij^k
    private ArrayList<IloRange[][]> newRangeConstraintZThetaLink; // z_ij^k ≤ θk
    private ArrayList<IloRange[][]> newRangeConstraintZBThetaLink; // z_ij^k ≥ b_ij^k + θk - 1

    private ArrayList<IloRange[][]> newRangeConstraintXSigmaLink; // x_it^k ≤ sigma_it
    private ArrayList<IloRange[][]> newRangeConstraintXThetaLink; // x_it^k ≤ θk
    private ArrayList<IloRange[][]> newRangeConstraintXSigmaThetaLink; // x_it^k ≥ sigma_it + θk - 1

    private IloRange[][][] newRangeConstraintLinearObj1;
    private IloRange[][][] newRangeConstraintLinearObj2;
    private IloRange[][][] newRangeConstraintLinearObj3;


    int numVars;
    int numConstraints;

    private boolean whetherPostProcess;
    private boolean whetherUseNewFinalMIP;
    private boolean whetherNewFinalMIPSolveInteger;
    private boolean whetherRestrictedColumnWhenSolveNewFinalMIP;
    private int nbFolderOfNbTripAsNbBestSchedulesInFinalMIP;

    // to improve code perform on getDual
    private int[][] precomputedTimeSlots; // [tripId][slotIndex] = timeValue//20250729



    public MasterProblem(Instance instance, Solution solution) {
        this.instance = instance;
        this.driverSchedules = new ArrayList<>();
        initializeSchedule(solution);
        System.out.println("Check to initialize driver schedules " + this.driverSchedules.size());

//        for (int ini = 0; ini < this.driverSchedules.size(); ini++) {
//            DriverSchedule driverSchedule_input = this.driverSchedules.get(ini);
//            DriverSchedule comparedSchedule = this.getComparedDriverSchedule();
//            if (driverSchedule_input.isSameStructureIgnoreDepartureTime(comparedSchedule)) {
//                System.out.println("Check to  same structure as compared be put from initial schedules" + driverSchedule_input);
//                if (driverSchedule_input.isExactlySameAs(comparedSchedule)) {
//                    System.out.println("Check to Exactly same driver schedule be put from initial schedules" + driverSchedule_input);
//                }
//            }
//        }
        //Comment CPLEX 1:
        this.subProblemSolvedByCplex = new SubProblemSolvedByCplex(this.instance, this);
        this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots = new SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots(this.instance, this);//2025.3.5
        this.dualSolutionOfRMPCnstTripSelectOneDepartureTime = new double[instance.getNbTrips()];// NEW 2025.1.4
        this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];//NEW 2025.1.4
        this.dualSolutionOfRMPConstraintOneDriverDriving = new double[instance.getNbTrips()];
        this.dualSolutionOfRMPCnstShortConnectionOut = new double[instance.getNbTrips()];// 2025.4.7
        this.dualSolutionOfRMPCnstShortConnectionIn = new double[instance.getNbTrips()];// 2025.4.7
        this.dualSolutionOfRMPCnstUpperboundWhetherSelectDriverShortConnection = new double[instance.getNbTrips()][instance.getNbTrips()];// 2025.4.7
        this.dualSolutionOfRMPConstraintLinkShortConnection = new double[instance.getNbTrips()][instance.getNbTrips()];
        this.dualSolutionOfRMPConstraintNbAvailableDriver = 0;
        this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];//New 2025.1.9
        this.kBestDriverSchedules = new ArrayList<DriverSchedule>();
        precomputeTimeSlots();

    }

    public MasterProblem(Instance instance, Solution solution, boolean whetherPostProcess, boolean whetherUseNewFinalMIP, boolean whetherNewFinalMIPSolveInteger, boolean whetherRestrictedColumnWhenSolveNewFinalMIP, int nbFolderOfNbTripAsNbBestSchedulesInFinalMIP) {
        this.instance = instance;
        this.driverSchedules = new ArrayList<>();
        initializeSchedule(solution);
        System.out.println("Check to initialize driver schedules " + this.driverSchedules.size());

//        for (int ini = 0; ini < this.driverSchedules.size(); ini++) {
//            DriverSchedule driverSchedule_input = this.driverSchedules.get(ini);
//            DriverSchedule comparedSchedule = this.getComparedDriverSchedule();
//            if (driverSchedule_input.isSameStructureIgnoreDepartureTime(comparedSchedule)) {
//                System.out.println("Check to  same structure as compared be put from initial schedules" + driverSchedule_input);
//                if (driverSchedule_input.isExactlySameAs(comparedSchedule)) {
//                    System.out.println("Check to Exactly same driver schedule be put from initial schedules" + driverSchedule_input);
//                }
//            }
//        }
        //Comment CPLEX 1:
        this.subProblemSolvedByCplex = new SubProblemSolvedByCplex(this.instance, this);
        this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots = new SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots(this.instance, this);//2025.3.5
        this.dualSolutionOfRMPCnstTripSelectOneDepartureTime = new double[instance.getNbTrips()];// NEW 2025.1.4
        this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];//NEW 2025.1.4
        this.dualSolutionOfRMPConstraintOneDriverDriving = new double[instance.getNbTrips()];
        this.dualSolutionOfRMPCnstShortConnectionOut = new double[instance.getNbTrips()];// 2025.4.7
        this.dualSolutionOfRMPCnstShortConnectionIn = new double[instance.getNbTrips()];// 2025.4.7
        this.dualSolutionOfRMPCnstUpperboundWhetherSelectDriverShortConnection = new double[instance.getNbTrips()][instance.getNbTrips()];// 2025.4.7
        this.dualSolutionOfRMPConstraintLinkShortConnection = new double[instance.getNbTrips()][instance.getNbTrips()];
        this.dualSolutionOfRMPConstraintNbAvailableDriver = 0;
        this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime = new double[instance.getNbTrips()][instance.getEndingPlaningHorizon()];//New 2025.1.9


        this.whetherPostProcess = whetherPostProcess;
        this.whetherUseNewFinalMIP = whetherUseNewFinalMIP;
        this.whetherNewFinalMIPSolveInteger = whetherNewFinalMIPSolveInteger;
        this.whetherRestrictedColumnWhenSolveNewFinalMIP = whetherRestrictedColumnWhenSolveNewFinalMIP;
        this.nbFolderOfNbTripAsNbBestSchedulesInFinalMIP = nbFolderOfNbTripAsNbBestSchedulesInFinalMIP;
        this.kBestDriverSchedules = new ArrayList<DriverSchedule>();
        precomputeTimeSlots();

    }


    private void initializeSchedule(Solution solution) {
        for (int s = 0; s < solution.getDriverSchedules().size(); s++) {
            DriverSchedule driverSchedule = solution.getDriverSchedules().get(s);
            this.driverSchedules.add(driverSchedule);
            // System.out.println("initialize solution driver_" + driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList());
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
        cnstLinkSelectScheduleAndSelectDepartureTimeOfTrip();// new constraints 2025.1.6 MAIN TIME CONSUMEING
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
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime /(double) timeUnit);
            int nbUL = (int) Math.round(LDepTime / (double)timeUnit);
            for (int n = nbUE; n <= nbUL; n++) {
                int t = n * timeUnit;
                expr.addTerm(1, this.varTripSelectDepartureTime[i][t]);
            }
            this.rangeConstraintTripSelectOneDepartureTime[i] = this.cplex.addEq(expr, 1);
        }
    }

    private void cnstLinkSelectScheduleAndSelectDepartureTimeOfTrip() throws IloException { // new constraints 2025.1.6
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                IloLinearNumExpr expr = this.cplex.linearNumExpr();
                for (int k = 0; k < this.driverSchedules.size(); k++) {
                    int coEfficient_i_t = this.driverSchedules.get(k).getCoefficientF(i, t);
                    if (coEfficient_i_t > 1e-6) {// change improve the solving time 2025.4.13
                        expr.addTerm(coEfficient_i_t, this.varWhetherSelectTheSchedule.get(k));//change the coefficient 2025.1.9
                    }
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
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(eDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(lDepTime / (double)timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
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
            Trip trip = instance.getTrip(i);
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(eDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
            for (int n = nbUE; n <= nbUL; n++) {
                int t = n * timeUnit;
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
            long startCreate1= System.currentTimeMillis();
            createModelRMP();
            long endCreat1=System.currentTimeMillis();
            long duraiton1=endCreat1-startCreate1;
            timeForCreateModel=timeForCreateModel+duraiton1;
            numVars = cplex.getNcols();
            numConstraints = cplex.getNrows();
            System.out.println("Number of variables : " + numVars);
            System.out.println("Number of constraints: " + numConstraints);
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
            int eDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int lDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(eDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
            for (int n = nbUE; n <= nbUL; n++) {
                int t = n * timeUnit;
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

//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            Trip trip = instance.getTrip(i);
//            int eDepTime = trip.getEarliestDepartureTime();
//            int lDepTime = trip.getLatestDepartureTime();
//            int timeUnit = instance.getTimeSlotUnit();
//            int nbUE = (int) Math.round(eDepTime /(double) timeUnit);
//            int nbUL = (int) Math.round(lDepTime / (double)timeUnit);
//            for (int n = nbUE; n <= nbUL; n++) {
//                int t = n * timeUnit;
//                this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime[i][t] = cplex.getDual(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t]);// 2025.1.9 delta_i^t
//                // System.out.println("Check Dual delta_i_"+i+"_t_"+t+": "+this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime[i][t]);
//            }
//        }//优化上面的代码for improve the time, we change to optimize by the following for loop
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int t : precomputedTimeSlots[i]) {
                this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime[i][t] = cplex.getDual(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t]);
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
                        this.dualSolutionOfRMPCnstShortConnectionOut[i] = cplex.getDual(this.rangeConstraintShortConnectionOut[i]);//xi_i
                        //System.out.println("Check Dual xi_i_"+i +this.dualSolutionOfRMPConstraintShortConnectionOut[i]);
                    }
                }
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //change 2025.2.11 change to strictly less than
                if (instance.whetherHavePossibleArcAfterCleaning(j, i)) {
                    int minWaitingTime = instance.getMinWaitingTime(j, i);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        this.dualSolutionOfRMPCnstShortConnectionIn[i] = cplex.getDual(this.rangeConstraintShortConnectionIn[i]);//eta_i
                        //System.out.println("Check Dual eta_i_"+i +this.dualSolutionOfRMPConstraintShortConnectionIn[i]);
                    }
                }
            }
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


//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            Trip trip = instance.getTrip(i);
//            int eDepTime = trip.getEarliestDepartureTime();
//            int lDepTime = trip.getLatestDepartureTime();
//            int timeUnit = instance.getTimeSlotUnit();
//            int nbUE = (int) Math.round(eDepTime /(double) timeUnit);
//            int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
//            for (int n = nbUE; n <= nbUL; n++) {
//                int t = n * timeUnit;
//                this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime[i][t] = cplex.getDual(this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime[i][t]);// mu_i^T
//                // System.out.println("Check Dual mu_i_"+i+"_t_"+ t+": "+this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime[i][t]);
//            }
//        }
        //上面的优化后的代码：20250729
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int t : precomputedTimeSlots[i]) {
                this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime[i][t] = cplex.getDual(this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime[i][t]);// mu_i^T
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        this.dualSolutionOfRMPCnstUpperboundWhetherSelectDriverShortConnection[i][j] = cplex.getDual(this.rangeConstraintUpperBoundWhetherSelectedShortConnection[i][j]);//alpha_i_j
                    }
                }
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
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(eDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
            for (int n = nbUE; n <= nbUL; n++) {
                int t = n * timeUnit;
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
                            dual[i][j] = this.cplex.getDual(this.rangeConstraintUpperBoundWhetherSelectedShortConnection[i][j]);// alpha_i_j
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
            Trip trip = instance.getTrip(i);
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(eDepTime /(double) timeUnit);
            int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
            for (int n = nbUE; n <= nbUL; n++) {
                int t = n * timeUnit;
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
        long startColGenLoop = System.currentTimeMillis();
        System.out.println("Here is to solve the first RMP");
        long totalTimeOnAddColumns = 0;
        startTime = System.currentTimeMillis();


        //(1) solve the RMP
        iteration = 1;
        System.out.println("nbIteration: " + iteration);
        double firstStartSolRMP = System.currentTimeMillis();
        solveRMPWithCplex();
        double firstEndSolRMP = System.currentTimeMillis();
        double firstDurationSolRMP = firstEndSolRMP - firstStartSolRMP;
        totalTimeOfSolRMPByCplex = firstDurationSolRMP;
        initialRMPValueWithOnlyFeasibleSchedulesWeCreate = cplex.getObjValue();
        System.out.println("initial RMP value: " + initialRMPValueWithOnlyFeasibleSchedulesWeCreate);
        storeDualVals();//再一次存储对偶变量的值

        //************(2)******solve the sub-problem with labeling and record the time, the difference is that labeling could report more than one column
        double firstStartTimeOfLabelingAlgorithmSolveSubProblem = System.currentTimeMillis();
        ArrayList<DriverSchedule> bestSchedules = subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.generateSchedulesWithGivenNegReducedCostAmongAllStartingTrips();
        double firstEndTimeOfLabelingAlgorithmSolveSubProblem = System.currentTimeMillis();
        double firstDurationOfLabeling = firstEndTimeOfLabelingAlgorithmSolveSubProblem - firstStartTimeOfLabelingAlgorithmSolveSubProblem;
        System.out.println("total time on labeling record in master class in milliseconds is: " + firstDurationOfLabeling);
        totalTimeCostForSolvingSubProblemByLabeling = totalTimeCostForSolvingSubProblemByLabeling + firstDurationOfLabeling;

//        double interTimeOfLabeling=subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getTotalLabelingTime();
//        System.out.println("inner total time on labeling in milli sec "+interTimeOfLabeling);
//        totalTimeOfLabeling=totalTimeOfLabeling+interTimeOfLabeling;

        System.out.println("Here are the schedules with min reduced cost generated by labeling" + bestSchedules);
        double reducedCostLabeling = this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getMostMinReducedCost();
        System.out.println("reduced cost" + reducedCostLabeling + "+solve by labeling in iteration " + iteration);

//
//        double totalTimeOnDominanceRule = subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getTotalDominanceTime();
//        System.out.println("inner total time on dominance rule record in master class in milli seconds is: " + totalTimeOnDominanceRule);
//        totalTimeOnDominanceRuleForAllIteration = totalTimeOnDominanceRuleForAllIteration + totalTimeOnDominanceRule;

        int nbGeneratedLabels = subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getNbTotalGenerateLabels();
        finalNbLabelsLastIteration = nbGeneratedLabels;
        System.out.println("total nbLabels Generate is:" + nbGeneratedLabels);
        totalLabelsGenerateForAllIterations = totalLabelsGenerateForAllIterations + nbGeneratedLabels;
        //***********************(2)********solve subProblem with labeling ***********************************************************************

        if (this.getLagrangianLowerBoundOnOptimalValueOfRMP() > finalBestLowerBoundOfMP) {
            finalBestLowerBoundOfMP = this.getLagrangianLowerBoundOnOptimalValueOfRMP();//初始化lower bound 为当前的 lagrangian bound (因为有的问题不需要迭代就可以获得最优解) 跟0当中比较大的那个
        }
        System.out.println("Check the second best lower bound after initialize" + finalBestLowerBoundOfMP);


        //(3) start the iteration

//        Comment 3: the while stop condition
        while (reducedCostLabeling < -EPS) {// comment for checking
//        while (iteration<=1){
            long currentTime = System.currentTimeMillis();
            long iterationElapsedTime = (currentTime - startTime) / 1000;
            if (iterationElapsedTime >= 3600) {//here is to limit the iteration time, if the iteration time is less than 1 hour, then continue
                System.out.println("final iteration is " + iteration);
                System.out.println("final columns is " + varWhetherSelectTheSchedule.size());
                long currentTime1 = System.currentTimeMillis();
                finalTimeCost = (currentTime1 - startTime) / 1000;
                System.out.println("final time cost in sec " + finalTimeCost);
                break;
            }
            long t1 = System.currentTimeMillis(); // before add columns// 构造新列 + 添加新变量
            // according to the new columns solved by labeling Algorithm
            //Comment 4: add the bestDriverSchedules
            for (int s = 0; s < bestSchedules.size(); s++) {
                DriverSchedule bestDriverSchedule = bestSchedules.get(s);
                this.driverSchedules.add(bestDriverSchedule);
                //新增一列之后，对应的目标函数的修改以及约束函数也要修改
//                IloColumn newColumn = this.cplex.column(objective, bestDriverSchedule.getCostC());// 修改目标函数
//                // 约束函数中对于这个新增列的系数变化
//              // constraints 1 change nothing
//
//                for (int i = 0; i < instance.getNbTrips(); i++) {
//                    int eDepTime = instance.getTrip(i).getEarliestDepartureTime();
//                    int lDepTime = instance.getTrip(i).getLatestDepartureTime();
//                    int timeUnit = instance.getTimeSlotUnit();
//                    int nbUE = (int) Math.round(eDepTime /(double) timeUnit);
//                    int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
//                    for (int n = nbUE; n <= nbUL; n++) {
//                        int t = n * timeUnit;
//                        newColumn = newColumn.and(cplex.column(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t], bestDriverSchedule.getCoefficientF(i, t)));
//                    }
//                }// constraints 2 add a coefficient
//
//                for (int i = 0; i < instance.getNbTrips(); i++) {
//                    newColumn = newColumn.and(cplex.column(this.rangeConstraintOneDriving[i], bestDriverSchedule.getCoefficientG(i)));
//                }//constraints 3 add  a coefficient
//
//                for (int i = 0; i < instance.getNbTrips(); i++) {
//                    for (int j = 0; j < instance.getNbTrips(); j++) {
//                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                            int minConnection = instance.getMinWaitingTime(i, j);
//                            if (minConnection < instance.getShortConnectionTimeForDriver()) {
//                                newColumn = newColumn.and(cplex.column(this.rangeConstraintShortConnectionOut[i], 0));
//                            }//constraints 4 short connection add nothing
//                        }
//                    }
//                }
//
//                for (int i = 0; i < instance.getNbTrips(); i++) {
//                    for (int j = 0; j < instance.getNbTrips(); j++) {
//                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                            int minConnection = instance.getMinWaitingTime(i, j);
//                            if (minConnection < instance.getShortConnectionTimeForDriver()) {
//                                newColumn = newColumn.and(cplex.column(this.rangeConstraintShortConnectionIn[i], 0));
//                            }//constraints 5 short connection add nothing
//                        }
//                    }
//                }
//
//
//                for (int i = 0; i < instance.getNbTrips(); i++) {
//                    for (int j = 0; j < instance.getNbTrips(); j++) {
//                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                            int minConnection = instance.getMinWaitingTime(i, j);
//                            if (minConnection < instance.getShortConnectionTimeForDriver()) {
//                                newColumn = newColumn.and(cplex.column(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j], bestDriverSchedule.getCoefficientForShortConnectionB(i, j)));
//                            }//constraints 6 short connection add nothing
//                        }
//                    }
//                }
//
//                newColumn = newColumn.and(cplex.column(this.rangeNbAvailableDriver, 1));
//                //然后将对应好目标函数系数的，对应好约束函数系数的列，添加进模型中去
//                //Comment 5: add the bestDriverSchedules columns into the master problem
//                //update the variable in the RMP ( objective and constraints add new coefficient)
//                IloNumVar x = cplex.numVar(newColumn, 0, Double.MAX_VALUE);
//                this.varWhetherSelectTheSchedule.add(x);
//


                //0729
                // 1. 创建所有约束的引用数组（避免重复查找）
                ArrayList<IloRange> allConstraints = new ArrayList<>();
                ArrayList<Double> allCoefficients = new ArrayList<>();

                // 2. 目标函数系数
                IloColumn newColumn = this.cplex.column(objective, bestDriverSchedule.getCostC());

                // 3. 批量收集所有约束和系数
                collectConstraintCoefficients(bestDriverSchedule, allConstraints, allCoefficients);

                // 4. 批量添加到column中
                for (int idx = 0; idx < allConstraints.size(); idx++) {
                    IloRange constraint = allConstraints.get(idx);
                    double coefficient = allCoefficients.get(idx);
                    if (Math.abs(coefficient) > 1e-6) { // 只添加非零系数
                        newColumn = newColumn.and(cplex.column(constraint, coefficient));
                    }
                }

                // 5. 创建变量
                IloNumVar x = cplex.numVar(newColumn, 0, Double.MAX_VALUE, "theta_" + this.varWhetherSelectTheSchedule.size());
                this.varWhetherSelectTheSchedule.add(x);

            }

            long t2 = System.currentTimeMillis(); // after add columns, before RMP
            long timeAddColumns = (t2 - t1);
            totalTimeOnAddColumns = totalTimeOnAddColumns + timeAddColumns;
            System.out.println("Add column use: " + (t2 - t1) + " milli sec in iteration" + iteration);
            System.out.println("until now add new columns time in milli sec" + totalTimeOnAddColumns);
            iteration = iteration + 1;
            System.out.println("nbIteration: " + iteration);

            long currentElapsedTime = (currentTime - startTime) / 1000;
            System.out.println("Elapsed time: " + currentElapsedTime + " seconds");
            long beforeCplexSolve = System.currentTimeMillis() - startTime;
            remainingTime = 3600000 - beforeCplexSolve;
            System.out.println("remainingTime is: " + remainingTime);

            //solve the RMP again
            double startTimeSolRMPByCplexAgain = System.currentTimeMillis();
            solveRMPWithCplex();// 里面包含了存储对偶变量
            double endTimeSolRMPByCplexAgain = System.currentTimeMillis();
            double durationSolRMPByCplexAgain = endTimeSolRMPByCplexAgain - startTimeSolRMPByCplexAgain;
            totalTimeOfSolRMPByCplex = totalTimeOfSolRMPByCplex + durationSolRMPByCplexAgain;
            System.out.println("MasterProblem solve RMP with cplex obj=" + this.getMPObjValue());


            //  below here now we start to solved subProblem by the labeling algorithm*************************************************************************************
            double startTimeIterationLabelingAlgorithmSolveSubProblem = System.currentTimeMillis();
            bestSchedules = this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.generateSchedulesWithGivenNegReducedCostAmongAllStartingTrips();
            reducedCostLabeling = this.subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getMostMinReducedCost();
            double endTimeIterationLabelingAlgorithmSolveSubProblem = System.currentTimeMillis();
            double durationTimeIterationLabelingAlgorithmSolveSubProblem = endTimeIterationLabelingAlgorithmSolveSubProblem - startTimeIterationLabelingAlgorithmSolveSubProblem;

            totalTimeCostForSolvingSubProblemByLabeling = totalTimeCostForSolvingSubProblemByLabeling + durationTimeIterationLabelingAlgorithmSolveSubProblem;
            System.out.println("total time on labeling until iteration " + iteration + " record in master in milli sec is: " + totalTimeCostForSolvingSubProblemByLabeling);

//            double innerTimeOnlabeling=subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getTotalLabelingTime();
//            totalTimeOfLabeling=totalTimeOfLabeling+innerTimeOnlabeling;
//            System.out.println("inner total time on labeling until iteration "+iteration+" totalTimeOfLabeling is"+totalTimeOfLabeling);

            double totalTimeOnDominanceRuleInIteration = subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getTotalDominanceTime();
            totalTimeOnDominanceRuleForAllIteration = totalTimeOnDominanceRuleForAllIteration + totalTimeOnDominanceRuleInIteration;
            System.out.println("inner total time on dominance rule in iteration " + iteration + " record in master in milli sec is: " + totalTimeOnDominanceRuleInIteration);

            int nbGeneratedLabelsInIteration = subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getNbTotalGenerateLabels();
            System.out.println("total nbLabels Generate in iteration " + iteration + "  is: " + nbGeneratedLabelsInIteration);
            finalNbLabelsLastIteration = nbGeneratedLabelsInIteration;//updated
            totalLabelsGenerateForAllIterations = totalLabelsGenerateForAllIterations + nbGeneratedLabelsInIteration;

            if (totalLabelsGenerateForAllIterations < 0) {
                System.err.println("WARNING: totalNbGeneratedLabels overflowed! Consider switching to long.");
            }

            // System.out.println("best driver schedules from subProblem by using labeling algorithm: " + bestSchedules);
            System.out.println("reduced cost solved by labeling algorithm is: " + reducedCostLabeling);
            //above is we try to solved by the labeling algorithm again*****************************************************************************************

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
                long currentTime1 = System.currentTimeMillis();
                finalTimeCost = (currentTime1 - startTime) / 1000;
                System.out.println("final time cost in sec " + finalTimeCost);
                break;
            }

        }

        finalMasterObjectiveValue = this.getMPObjValue();
        System.out.println("final master obj=" + this.getMPObjValue());
        System.out.println("final best lower bound: " + finalBestLowerBoundOfMP);

        //After the loop, before the variables in the model are converted to integer (binary) variables
        // 🔹 1. 打印被选择的 Schedule（theta_k > 0）
        for (int k = 0; k < this.varWhetherSelectTheSchedule.size(); k++) {
            double val = this.cplex.getValue(this.varWhetherSelectTheSchedule.get(k));
            if (val > 1e-6) {
                //if (val > 1e-4 && val < 0.999) {
                System.out.println("[After End Column Generation (theta_k > 0) ] Schedule k = " + k + ", Value = " + val);
                // 你也可以打印 driverSchedules.get(k) 的内容：
                System.out.println("corresponding schedule: " + this.driverSchedules.get(k));
                //}
            }
        }

        // 🔹 2. 打印被选择的CG short connection（rho_ij > 0）
        System.out.println("\n========= Short Connections with rho_ij > 0 =========");
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaiting = instance.getMinWaitingTime(i, j);
                    if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                        double val = cplex.getValue(varShortConnectionIsPerformed[i][j]);
                        if (val > 1e-6) {
                            System.out.println("[After End Column Generation (rho_ij  > 0) ] Short connection (rho) from trip_" + i + " to trip_" + j + ", value = " + val);
                        }
                    }
                }
            }
        }

        // 🔹 3. 打印被选择的Trip starting time（sigma_i_t > 0）
        System.out.println("\n========= starting time  with sigma_it > 0 =========");
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(eDepTime /(double) timeUnit);
            int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
            for (int n = nbUE; n <= nbUL; n++) {
                int t = n * timeUnit;
                double val = cplex.getValue(varTripSelectDepartureTime[i][t]);
                if (val > 1e-6) {
                    System.out.println("[After End Column Generation (Sigma_it  > 0) ]for trip_" + i + " with dep time " + t + ", value = " + val);
                }
            }
        }

        if (whetherPostProcess == true) {
            // we start the post-process of CG
            int nbAddScheduleChangePassengerTimeAccordingToDriverTime = 0;
            //1.针对所有的schedule, 收集每个 trip 的 driver 是 driving 出发时间
            ArrayList<DriverSchedule> originalSchedules = new ArrayList<>(this.driverSchedules);
            System.out.println("NbDriverSchedulesBeforePostProcess is" + originalSchedules.size());
            long startGenRichScheduleBasedOnDepTimeOfDriverAndPassenger = System.currentTimeMillis();

            //********* below here we start check to compare the good schedule generated which part  ***
//        DriverSchedule comparedSchedule = getComparedDriverSchedule();
//
//        for (int o = 0; o < originalSchedules.size(); o++) {
//            DriverSchedule driverSchedule = originalSchedules.get(o);
//            if (driverSchedule.isSameStructureIgnoreDepartureTime(comparedSchedule)) {
//                System.out.println("Check to compare the good schedule: same structure as compared, generated in CG process ");
//                //System.out.println("Compared schedule: " +comparedSchedule);
//                System.out.println("Generated in CG process: " + driverSchedule);
//            }
//            if (driverSchedule.isExactlySameAs(comparedSchedule)) {
//                System.out.println("Check to compare the good schedule: Exactly same as compared, generated in CG process ");
//                // System.out.println("Compared schedule: " +comparedSchedule);
//                System.out.println("Generated in CG process: " + driverSchedule);
//
//            }
//        }
            //********* above here we start check to compare the good schedule generated which part ***
            for (int i = 0; i < instance.getNbTrips(); i++) {
                int idTrip = instance.getTrip(i).getIdOfTrip();
                BitSet departureTimesUnderDrivingStatus_I = new BitSet();
                // 第一步：收集所有 driving 状态下该 trip 的出发时间
                for (DriverSchedule schedule : originalSchedules) {
                    for (TripWithWorkingStatusAndDepartureTime trip : schedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
                        if (trip.getIdOfTrip() == idTrip && trip.getDrivingStatus()) {
                            departureTimesUnderDrivingStatus_I.set(trip.getDepartureTime());
                        }
                    }
                }
                System.out.println("dep time under driving status related to trip_" + i + "includes: " + departureTimesUnderDrivingStatus_I);

//
//            // 第二步：收集所有 passenger 状态下该 trip 的出发时间
//            BitSet departureTimesUnderPassengerStatus_I = new BitSet();
//            for (DriverSchedule schedule : originalSchedules) {
//                for (TripWithWorkingStatusAndDepartureTime trip : schedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
//                    if (trip.getIdOfTrip() == idTrip && !trip.getDrivingStatus()) {
//                        departureTimesUnderPassengerStatus_I.set(trip.getDepartureTime());
//                    }
//                }
//            }

                //第三步：确定相关的trip 的时间窗
//            int minDrivingDep = departureTimesUnderDrivingStatus_I.nextSetBit(0);
//            int minPassengerDep = departureTimesUnderPassengerStatus_I.nextSetBit(0);
//
//            int minDepT;
//            if (minDrivingDep == -1 && minPassengerDep == -1) {
//                minDepT = -1;
//            } else if (minDrivingDep == -1) {
//                minDepT = minPassengerDep;
//            } else if (minPassengerDep == -1) {
//                minDepT = minDrivingDep;
//            } else {
//                minDepT = Math.min(minDrivingDep, minPassengerDep);
//            }
//
//            // 获取最大出发时间：使用 BitSet.length() - 1，注意 BitSet 是稀疏的
//            int maxDrivingDep = departureTimesUnderDrivingStatus_I.length() - 1;
//            int maxPassengerDep = departureTimesUnderPassengerStatus_I.length() - 1;
//
//            int maxDepT;
//            if (departureTimesUnderDrivingStatus_I.isEmpty() && departureTimesUnderPassengerStatus_I.isEmpty()) {
//                maxDepT = -1;
//            } else {
//                maxDepT = Math.max(maxDrivingDep, maxPassengerDep);
//            }
//
//            System.out.println("Trip_" + idTrip + ": min departure time = " + minDepT + ", max departure time = " + maxDepT);



                // 第四步：尝试把 passenger 状态的出发时间改成 driving 状态下的时间
                for (int depTimeDriving = departureTimesUnderDrivingStatus_I.nextSetBit(0);
                     depTimeDriving >= 0;
                     depTimeDriving = departureTimesUnderDrivingStatus_I.nextSetBit(depTimeDriving + 1)) {

                    for (DriverSchedule schedule : originalSchedules) {
                        //System.out.println("original schedule: "+schedule);
                        if (schedule.whetherTripPresent(idTrip)) {
                            for (int j = 0; j < schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); j++) {
                                TripWithWorkingStatusAndDepartureTime trip = schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j);
                                if (trip.getIdOfTrip() == idTrip && !trip.getDrivingStatus()) {
                                    int currentTimeAsPassenger = trip.getDepartureTime();
                                    if (currentTimeAsPassenger != depTimeDriving) {
                                        boolean feasible = whetherCouldChangeConsiderFeasibilityConnection(depTimeDriving, schedule, j, trip.getDuration());
                                        if (feasible) {
                                            DriverSchedule newSchedule = schedule.deepCopy();
                                            // System.out.println("copy schedule: "+ newSchedule);
                                            newSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j).setDepartureTime(depTimeDriving);
                                            if (!this.containsSchedule(newSchedule)) {
                                                if (newSchedule.whetherFeasible(false)) {//also need to check check the total working time,connection time
                                                    this.addDriverSchedule(newSchedule, "POSTChangPassengerTimeAccordingToDriver");
                                                    nbAddScheduleChangePassengerTimeAccordingToDriverTime = nbAddScheduleChangePassengerTimeAccordingToDriverTime + 1;
                                                    //System.out.println(" new schedule is added！ " + newSchedule);
                                                    if (newSchedule.getCostC() != schedule.getCostC()) {
                                                        //System.out.println("Even change cost from " + schedule.getCostC() + " to cost " + newSchedule.getCostC());
                                                        //  System.out.println(" new schedule is added！ " + newSchedule);
                                                    }
//                                                if (newSchedule.isSameStructureIgnoreDepartureTime(comparedSchedule)) {
//                                                    System.out.println("Check to compare the good schedule: same structure as compared, in post change passenger time according to driver time ");
//                                                    System.out.println("Compared schedule: " + comparedSchedule);
//                                                    System.out.println("Generated in CG process: " + newSchedule);
//                                                }
//
//                                                if (newSchedule.isExactlySameAs(comparedSchedule)) {
//                                                    System.out.println("Check to compare the good schedule: Exactly same as compared, in post change passenger time according to driver time ");
//                                                    System.out.println("Compared schedule: " + comparedSchedule);
//                                                    System.out.println("Generated in CG process: " + newSchedule);
//
//                                                }
                                                }
                                                // 还有改进的空间，那就是只添加没有出现过的
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("add new schedules change passenger Task time to driver time: " + nbAddScheduleChangePassengerTimeAccordingToDriverTime);
            nbColumnsGeneratedByPostProcess = nbColumnsGeneratedByPostProcess + nbAddScheduleChangePassengerTimeAccordingToDriverTime;
            System.out.println("afterAdd new schedules change passenger Task time to driver time nbDriverSchedules is: " + varWhetherSelectTheSchedule.size());

            //对称的做法；
            int nbAddScheduleChangeDriverTimeAccordingToPassenger = 0;
            for (int i = 0; i < instance.getNbTrips(); i++) {
                int idTrip = instance.getTrip(i).getIdOfTrip();
                BitSet departureTimesUnderPassengerStatus_I = new BitSet();

                // 第一步：收集所有 passenger 状态下该 trip 的出发时间
                for (DriverSchedule schedule : originalSchedules) {
                    for (TripWithWorkingStatusAndDepartureTime trip : schedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
                        if (trip.getIdOfTrip() == idTrip && !trip.getDrivingStatus()) {
                            departureTimesUnderPassengerStatus_I.set(trip.getDepartureTime());
                        }
                    }
                }
                System.out.println("dep time under passenger status related to trip_" + i + "includes: " + departureTimesUnderPassengerStatus_I);
//            BitSet departureTimesUnderDrivingStatus_I = new BitSet();
//            // 第二步：收集所有 driving 状态下该 trip 的出发时间
//            for (DriverSchedule schedule : originalSchedules) {
//                for (TripWithWorkingStatusAndDepartureTime trip : schedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
//                    if (trip.getIdOfTrip() == idTrip && trip.getDrivingStatus()) {
//                        departureTimesUnderDrivingStatus_I.set(trip.getDepartureTime());
//                    }
//                }
//            }
//            System.out.println("dep time under driving status related to trip_" + i + "includes: " + departureTimesUnderDrivingStatus_I);


                //第三步：确定相关的trip 的时间窗
//            int minDrivingDep = departureTimesUnderDrivingStatus_I.nextSetBit(0);
//            int minPassengerDep = departureTimesUnderPassengerStatus_I.nextSetBit(0);
//
//            int minDepT;
//            if (minDrivingDep == -1 && minPassengerDep == -1) {
//                minDepT = -1;
//            } else if (minDrivingDep == -1) {
//                minDepT = minPassengerDep;
//            } else if (minPassengerDep == -1) {
//                minDepT = minDrivingDep;
//            } else {
//                minDepT = Math.min(minDrivingDep, minPassengerDep);
//            }
//
//            // 获取最大出发时间：使用 BitSet.length() - 1，注意 BitSet 是稀疏的
//            int maxDrivingDep = departureTimesUnderDrivingStatus_I.length() - 1;
//            int maxPassengerDep = departureTimesUnderPassengerStatus_I.length() - 1;
//
//            int maxDepT;
//            if (departureTimesUnderDrivingStatus_I.isEmpty() && departureTimesUnderPassengerStatus_I.isEmpty()) {
//                maxDepT = -1;
//            } else {
//                maxDepT = Math.max(maxDrivingDep, maxPassengerDep);
//            }
//
//            System.out.println("Trip_" + idTrip + ": min departure time = " + minDepT + ", max departure time = " + maxDepT);



//            // 第四步：尝试把 driving 状态的出发时间改成 passenger 状态下的时间
                for (int depTimePassenger = departureTimesUnderPassengerStatus_I.nextSetBit(0);
                     depTimePassenger >= 0;
                     depTimePassenger = departureTimesUnderPassengerStatus_I.nextSetBit(depTimePassenger + 1)) {
                    for (DriverSchedule schedule : originalSchedules) {
                        if (schedule.whetherTripPresent(idTrip)) {
                            for (int j = 0; j < schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); j++) {
                                TripWithWorkingStatusAndDepartureTime trip = schedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j);
                                if (trip.getIdOfTrip() == idTrip && trip.getDrivingStatus()) {
                                    int currentTimeAsDriving = trip.getDepartureTime();
                                    if (currentTimeAsDriving != depTimePassenger) {
                                        boolean feasible = whetherCouldChangeConsiderFeasibilityConnection(depTimePassenger, schedule, j, trip.getDuration());
                                        if (feasible) {
                                            DriverSchedule newSchedule = schedule.deepCopy();
                                            TripWithWorkingStatusAndDepartureTime newTrip = newSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j);
                                            newTrip.setDepartureTime(depTimePassenger);
                                            if (!this.containsSchedule(newSchedule)) {
                                                if (newSchedule.whetherFeasible(false)) {//also need to check the total working time,connection time
                                                    this.addDriverSchedule(newSchedule, "POSTChangeDriverTimeAccordingToPassenger");
                                                    nbAddScheduleChangeDriverTimeAccordingToPassenger = nbAddScheduleChangeDriverTimeAccordingToPassenger + 1;
                                                    // System.out.println(" new schedule is added！ " + newSchedule);
                                                    if (newSchedule.getCostC() != schedule.getCostC()) {
                                                        // System.out.println("Even change cost from " + schedule.getCostC() + " to cost " + newSchedule.getCostC());
                                                        // System.out.println(" new schedule is added！ " + newSchedule);
                                                    }

//                                                if (newSchedule.isSameStructureIgnoreDepartureTime(comparedSchedule)) {
//                                                    System.out.println("Check to compare the good schedule: same structure as compared, in post change driver time according to passenger time ");
//                                                    System.out.println("Compared schedule: " + comparedSchedule);
//                                                    System.out.println("Generated in CG process: " + newSchedule);
//                                                }
//
//                                                if (newSchedule.isExactlySameAs(comparedSchedule)) {
//                                                    System.out.println("Check to compare the good schedule: Exactly same as compared, in post change driver time according to passenger time ");
//                                                    System.out.println("Compared schedule: " + comparedSchedule);
//                                                    System.out.println("Generated in CG process: " + newSchedule);
//                                                }
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
            System.out.println("add new schedules change driver time to passenger time" + nbAddScheduleChangeDriverTimeAccordingToPassenger);
            nbColumnsGeneratedByPostProcess = nbColumnsGeneratedByPostProcess + nbAddScheduleChangeDriverTimeAccordingToPassenger;
            System.out.println("afterAdd new schedules change driver time to passenger time nbDriverSchedules is " + varWhetherSelectTheSchedule.size());

            long endGenRichScheduleBasedOnDepTimeOfDriverAndPassenger = System.currentTimeMillis();
            double durationOfPostProcessGenerateMoreSchedules = (endGenRichScheduleBasedOnDepTimeOfDriverAndPassenger - startGenRichScheduleBasedOnDepTimeOfDriverAndPassenger) / 1000;
            System.out.println("Post process time duration for rich the schedule departure time in sec: " + durationOfPostProcessGenerateMoreSchedules);
            timeForPostProcessOfRichScheduleByDepartureTime=timeForPostProcessOfRichScheduleByDepartureTime+durationOfPostProcessGenerateMoreSchedules;

            //After the loop, the variables in the model are converted to integer (binary) variables using the IloConversion object.

            long lastCplexStart = System.currentTimeMillis();
            for (int k = 0; k < this.varWhetherSelectTheSchedule.size(); k++) {
//            if(this.cplex.getValue(this.varWhetherSelectTheSchedule.get(k))>0){
//                System.out.println("after Column generation the variable value is");
//            }
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

            for (int i = 0; i < instance.getNbTrips(); i++) {
                Trip trip = instance.getTrip(i);
                int eDepTime = trip.getEarliestDepartureTime();
                int lDepTime = trip.getLatestDepartureTime();
                int timeUnit = instance.getTimeSlotUnit();
                int nbUE = (int) Math.round(eDepTime / (double)timeUnit);
                int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
                for (int n = nbUE; n <= nbUL; n++) {
                    int t = n * timeUnit;
                    IloConversion conv = this.cplex.conversion(this.varTripSelectDepartureTime[i][t], IloNumVarType.Int);
                    this.cplex.add(conv);
                }
            }


            // ----------- Add warm start: select first N schedules ------------
            System.out.println("Here is the final MIP, I add the warm start to help solve in case those use 300 sec but status is unknown");
            int nbWarmStartDrivers = instance.getMaxNbDriverAvailable(); // 用于 warm start 的 driver 数量
            ArrayList<IloNumVar> warmStartVars = new ArrayList<>();
            ArrayList<Double> warmStartVals = new ArrayList<>();

            for (int k = 0; k < this.varWhetherSelectTheSchedule.size(); k++) {
                warmStartVars.add(this.varWhetherSelectTheSchedule.get(k));
                if (k < nbWarmStartDrivers) {
                    warmStartVals.add(1.0); // 前几个 schedule 被选择
                } else {
                    warmStartVals.add(0.0); // 其余不选
                }
            }

            try {
                IloNumVar[] varsArray = warmStartVars.toArray(new IloNumVar[0]);
                double[] valsArray = warmStartVals.stream().mapToDouble(Double::doubleValue).toArray();
                this.cplex.addMIPStart(varsArray, valsArray, IloCplex.MIPStartEffort.Auto); // 或 CheckFeasibility
                System.out.println("Warm start added with first " + nbWarmStartDrivers + " driver schedules.");
            } catch (IloException e) {
                e.printStackTrace();
            }


            //  The final master problem is solved using cplex.solve() after the variables are converted to integer.
            //  after change the variable into integer, it means I had changed my problem structure, so I need to solve the problem again.
            this.cplex.setOut(System.out);
            long beforeTheLastCplexSolve = System.currentTimeMillis();
            if (beforeTheLastCplexSolve >= 3600) {
                this.cplex.setParam(IloCplex.Param.TimeLimit, 300);// change from 300 to 600
                this.cplex.setParam(IloCplex.Param.Threads, 4);
            }
            this.cplex.solve();
            System.out.println("Final MIP cplex solved status: " + cplex.getStatus());
            long lastCplexEnd = System.currentTimeMillis();
            double durationLastCplexSolve = (lastCplexEnd - lastCplexStart) / 1000;
            System.out.println("Final integer solve took: " + durationLastCplexSolve + " sec");
            timeForSolvingLastIntegerProblemInSec = durationLastCplexSolve;

            finalIntegerValueOfMasterObjective = this.getMPObjValue();
            System.out.println("Final integer master objective: " + this.getMPObjValue());

            System.out.println("lagrangian bound here is " + this.getLagrangianLowerBoundOnOptimalValueOfRMP());
            Solution solutionFromCG = new Solution(this.instance);
            for (int k = 0; k < this.driverSchedules.size(); k++) {
                if (cplex.getValue(this.varWhetherSelectTheSchedule.get(k)) > 0.999) {
                    solutionFromCG.addSchedule(this.driverSchedules.get(k));
                }
            }

            for (int k = 0; k < this.varWhetherSelectTheSchedule.size(); k++) {
                if (cplex.getValue(this.varWhetherSelectTheSchedule.get(k)) > 0.999) {
                    System.out.println("✔️ Final integer schedule: k = " + k);
                    System.out.println(driverSchedules.get(k));
                }
            }

            //above here is solve the integer problem by converted to integer.
            // Close the current CPLEX model
            this.cplex.close();
            finalTimeCost = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("final time cost in sec: " + finalTimeCost);
            long endColGenLoop = System.currentTimeMillis();
            System.out.println("ColGen run time:" + (endColGenLoop - startColGenLoop) + "ms");
            return solutionFromCG;
        } else if (whetherUseNewFinalMIP == true) {
            // Close the current CPLEX model
            this.cplex.close();
            // below we try to build we try to build a new mip
            // Now start to build a new MIP model
            System.out.println("now start build a new MIP model");
            // Build new MIP model based on the mathematical formulation provided
            if (whetherRestrictedColumnWhenSolveNewFinalMIP == true) {
                long startGenerateRestrictedMoreColumnsBasedOnDepTimeForDriverAndPassenger = System.currentTimeMillis();
                int K = nbFolderOfNbTripAsNbBestSchedulesInFinalMIP * instance.getNbTrips();
//                // here is the first part including all variables has no relationship with variables
//                ArrayList<DriverSchedule> KbestSchedules = new ArrayList<>();


                // 第一步：贪心选择保证覆盖所有trip
                BitSet coveredTrips = new BitSet(instance.getNbTrips());
                ArrayList<DriverSchedule> guaranteedSchedules = new ArrayList<>();
                ArrayList<DriverSchedule> availableSchedules = new ArrayList<>(this.driverSchedules);

// 贪心覆盖所有trip
                while (coveredTrips.cardinality() < instance.getNbTrips() && !availableSchedules.isEmpty()) {
                    DriverSchedule bestSchedule = null;
                    int maxNewTrips = 0;

                    for (DriverSchedule schedule : availableSchedules) {
                        int newTrips = 0;
                        for (TripWithWorkingStatusAndDepartureTime tripWithStatus : schedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
                            int tripId = tripWithStatus.getIdOfTrip();
                            if (!coveredTrips.get(tripId)) {
                                newTrips++;
                            }
                        }
                        if (newTrips > maxNewTrips) {
                            maxNewTrips = newTrips;
                            bestSchedule = schedule;
                        }
                    }

                    guaranteedSchedules.add(bestSchedule);
                    for (TripWithWorkingStatusAndDepartureTime tripWithStatus : bestSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList()) {
                        int tripId = tripWithStatus.getIdOfTrip();
                        coveredTrips.set(tripId);
                    }
                    availableSchedules.remove(bestSchedule);
                }

                // 第二步：剩余名额按成本排序选择最优schedules
                availableSchedules.sort(Comparator.comparingInt(DriverSchedule::getCostC));
                int remainingSlots = K - guaranteedSchedules.size();
                int actualRemaining = Math.min(remainingSlots, availableSchedules.size());

// 添加保证覆盖的schedules
                for (DriverSchedule schedule : guaranteedSchedules) {
                    this.kBestDriverSchedules.add(schedule);
                }
// 添加剩余最优成本的schedules
                for (int k = 0; k < actualRemaining; k++) {
                    this.kBestDriverSchedules.add(availableSchedules.get(k));
                }

                System.out.println("Here we add " + guaranteedSchedules.size() + " schedules for guaranteed trip coverage and " + actualRemaining + " additional best cost schedules for the final new Formulation with restricted schedules");

                long endGenerateRestrictedMoreColumnsBasedOnDepTimeForDriverAndPassenger = System.currentTimeMillis();
                double durationOfPostGenerated = (endGenerateRestrictedMoreColumnsBasedOnDepTimeForDriverAndPassenger - startGenerateRestrictedMoreColumnsBasedOnDepTimeForDriverAndPassenger) / 1000;
                timeForPostProcessOfRichScheduleByDepartureTime = durationOfPostGenerated;
//                // 创建副本避免修改原列表
//                ArrayList<DriverSchedule> sortedSchedules = new ArrayList<>(this.driverSchedules);
//                sortedSchedules.sort(Comparator.comparingInt(DriverSchedule::getCostC));
//
//                int actualK = Math.min(K, sortedSchedules.size());
//                for (int k = 0; k < actualK; k++) {
//                    this.kBestDriverSchedules.add(sortedSchedules.get(k));
//                }
//                System.out.println("Here we add " + actualK + " best of schedules for the final new Formulation with restricted schedules");
                Solution finalSolution = buildNewMIPModelBasedOnFormulation(this.kBestDriverSchedules);
                finalTimeCost = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("final time cost in sec: " + finalTimeCost);
                long endColGenLoop = System.currentTimeMillis();
                System.out.println("ColGen run time:" + (endColGenLoop - startColGenLoop) + "ms");
                return finalSolution;
            } else {
                Solution finalSolution = buildNewMIPModelBasedOnFormulation(this.driverSchedules);
                finalTimeCost = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("final time cost in sec: " + finalTimeCost);
                long endColGenLoop = System.currentTimeMillis();
                System.out.println("ColGen run time:" + (endColGenLoop - startColGenLoop) + "ms");
                return finalSolution;
            }
        } else {
            //After the loop, the variables in the model are converted to integer (binary) variables using the IloConversion object.
            long lastCplexStart = System.currentTimeMillis();
            for (int k = 0; k < this.varWhetherSelectTheSchedule.size(); k++) {
//            if(this.cplex.getValue(this.varWhetherSelectTheSchedule.get(k))>0){
//                System.out.println("after Column generation the variable value is");
//            }
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

            for (int i = 0; i < instance.getNbTrips(); i++) {
                Trip trip = instance.getTrip(i);
                int eDepTime = trip.getEarliestDepartureTime();
                int lDepTime = trip.getLatestDepartureTime();
                int timeUnit = instance.getTimeSlotUnit();
                int nbUE = (int) Math.round(eDepTime /(double) timeUnit);
                int nbUL = (int) Math.round(lDepTime /(double) timeUnit);
                for (int n = nbUE; n <= nbUL; n++) {
                    int t = n * timeUnit;
                    IloConversion conv = this.cplex.conversion(this.varTripSelectDepartureTime[i][t], IloNumVarType.Int);
                    this.cplex.add(conv);
                }
            }

            //  The final master problem is solved using cplex.solve() after the variables are converted to integer.
            //  after change the variable into integer, it means I had changed my problem structure, so I need to solve the problem again.
            this.cplex.setOut(System.out);
            long beforeTheLastCplexSolve = System.currentTimeMillis();
            if (beforeTheLastCplexSolve >= 3600) {
                this.cplex.setParam(IloCplex.Param.TimeLimit, 300);// change from 300 to 600
                this.cplex.setParam(IloCplex.Param.Threads, 4);
            }
            this.cplex.solve();
            System.out.println("Final MIP cplex solved status: " + cplex.getStatus());
            long lastCplexEnd = System.currentTimeMillis();
            double durationLastCplexSolve = (lastCplexEnd - lastCplexStart) / 1000;
            System.out.println("Final integer solve took: " + durationLastCplexSolve + " sec");
            timeForSolvingLastIntegerProblemInSec = durationLastCplexSolve;

            finalIntegerValueOfMasterObjective = this.getMPObjValue();
            System.out.println("Final integer master objective: " + this.getMPObjValue());

            System.out.println("lagrangian bound here is " + this.getLagrangianLowerBoundOnOptimalValueOfRMP());
            Solution solutionFromCG = new Solution(this.instance);
            for (int k = 0; k < this.driverSchedules.size(); k++) {
                if (cplex.getValue(this.varWhetherSelectTheSchedule.get(k)) > 0.999) {
                    solutionFromCG.addSchedule(this.driverSchedules.get(k));
                }
            }

            for (int k = 0; k < this.varWhetherSelectTheSchedule.size(); k++) {
                if (cplex.getValue(this.varWhetherSelectTheSchedule.get(k)) > 0.999) {
                    System.out.println("✔️ Final integer schedule: k = " + k);
                    System.out.println(driverSchedules.get(k));
                }
            }

            //above here is solve the integer problem by converted to integer.
            // Close the current CPLEX model
            this.cplex.close();
            finalTimeCost = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("final time cost in sec: " + finalTimeCost);
            long endColGenLoop = System.currentTimeMillis();
            System.out.println("ColGen run time:" + (endColGenLoop - startColGenLoop) + "ms");
            return solutionFromCG;

        }
    }

    // ========= NEW MIP MODEL BUILDING METHODS =========

    /**
     * Build a new MIP model based on the mathematical formulation provided:
     * min{∑(ωk∈ΩR) c̃k*θk + ∑(ωk∈ΩR)∑((i,j)∈A) c^DI*t_ij^k}
     */
    private Solution buildNewMIPModelBasedOnFormulation(ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        System.out.println("Building new MIP model based on mathematical formulation with " + this.driverSchedules.size() + " generated columns" + " same size as the current input " + driverSchedulesInput.size());

        //*************************** here is to check the wheather it should be
//        for (int input = 0; input < this.driverSchedules.size(); input++) {
//            DriverSchedule driverSchedule_input = this.driverSchedules.get(input);
//            DriverSchedule comparedSchedule = this.getComparedDriverSchedule();
//            if (driverSchedule_input.isSameStructureIgnoreDepartureTime(comparedSchedule)) {
//                System.out.println("Check to same structure as compared be put for from CG final columns, then be input of the new MIP schedules" + driverSchedule_input+" column id: "+input);
//                if (driverSchedule_input.isExactlySameAs(comparedSchedule)) {
//                    System.out.println("Check to Exactly same driver schedule be put from CG final columns, then be input of the new MIP schedules " + driverSchedule_input+" column id "+ input);
//                }
//            }
//        }
        //*********************************8 above here is to check the post-process generated schedule wheather be input into it

        // Create new CPLEX model
        IloCplex newCplex = new IloCplex();
        // Step 1: Define decision variables
        defineNewMIPVariables(newCplex, driverSchedulesInput);

        // Step 2: Define objective function
        defineNewMIPObjective(newCplex, driverSchedulesInput);

        // Step 3: Define basic constraints
        defineNewMIPBasicConstraints(newCplex, driverSchedulesInput);

//        // Step 4: Define additional constraints
        defineAdditionalMIPConstraints(newCplex, driverSchedulesInput);

        // Step 5: Solve the model
        Solution solution = solveNewMIPModel(newCplex, driverSchedulesInput);

//        // 添加验证步骤
//        if (solution != null) {
        //boolean isValid = validateSolution(solution);
//            if (!isValid) {
//                System.err.println("Warning: Solution validation failed!");
//            }
//        }

        // 清理资源
        newCplex.end();

        return solution;
    }


    private Solution solveNewMIPModel(IloCplex newCplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        System.out.println("========== SOLVING MIP MODEL ==========");

        // Set CPLEX parameters for better performance
        setCplexParametersForNewMIP(newCplex);

        // Print model statistics before solving
        printModelStatistics(newCplex);

        // Solve the model
        long startTime = System.currentTimeMillis();
        System.out.println("Starting CPLEX solve...");

        boolean solved = newCplex.solve();

        long endTime = System.currentTimeMillis();
        double solutionTime = (endTime - startTime) / 1000.0;

        // Check solve status
        if (!solved) {
            System.err.println("MIP model could not be solved!");
            System.err.println("CPLEX Status: " + newCplex.getStatus());
            // 启用冲突分析
            if (newCplex.getStatus() == IloCplex.Status.Infeasible) {
                newCplex.writeConflict("conflict.ilp");
                System.err.println("Conflict written to conflict.ilp");
            }
            return null;
        } else {
            System.out.println("CPLEX Status: " + newCplex.getStatus());
            System.out.println("New MIP Objective value: " + newCplex.getObjValue());
            System.out.println("Change original Mater integer value of Obj to the new mip obj");
            finalIntegerValueOfMasterObjective = newCplex.getObjValue();

        }
        Solution solution = new Solution(instance);
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            if (newCplex.getValue(newVarWhetherSelectTheSchedule.get(k)) > 0.99) {
                System.out.println("New mip select" + k + " variable value " + newCplex.getValue(newVarWhetherSelectTheSchedule.get(k)));
                DriverSchedule driverScheduleToBuild = new DriverSchedule(instance);
                DriverSchedule driverScheduleInSet = driverSchedulesInput.get(k);
                int idDepotToSet = driverScheduleInSet.getIdOfDepot();
                int indexStartDepotToSet = driverScheduleInSet.getIndexDepotAsStartingPoint();
                int indexEndDepotToSet = driverScheduleInSet.getIndexDepotAsEndingPoint();

                for (int i = 0; i < driverScheduleInSet.getTripWithWorkingStatusAndDepartureTimeArrayList().size(); i++) {
                    int idTripToSet = driverScheduleInSet.getTripWithWorkingStatusAndDepartureTimeArrayList().get(i).getIdOfTrip();
                    Trip tripToSet = instance.getTrip(idTripToSet);
                    boolean whetherDriveToSet = driverScheduleInSet.getTripWithWorkingStatusAndDepartureTimeArrayList().get(i).getDrivingStatus();
                    int depTimeToSet = Integer.MAX_VALUE;

                    for (int idOfTrip = 0; idOfTrip < instance.getNbTrips(); idOfTrip++) {
                        if (idOfTrip == idTripToSet) {
                            int EDepTime = instance.getTrip(idOfTrip).getEarliestDepartureTime();
                            int LDepTime = instance.getTrip(idOfTrip).getLatestDepartureTime();
                            int timeUnit = instance.getTimeSlotUnit();
                            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
                            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
                            for (int n = nbUE; n <=nbUL; n++) {
                                int t = n * timeUnit;
                                IloNumVar var = newVarTripSelectDepartureTime[idTripToSet][t];
                                if (var != null && newCplex.getValue(var) > 0.99) {
                                    depTimeToSet = t;
                                    break;
                                }
                            }
                        }
                    }

                    TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTimeToBuild = new TripWithWorkingStatusAndDepartureTime(tripToSet, whetherDriveToSet, depTimeToSet);
                    driverScheduleToBuild.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTimeToBuild);
                }
                driverScheduleToBuild.setIdOfDepot(idDepotToSet);
                driverScheduleToBuild.setIndexDepotAsStartingPoint(indexStartDepotToSet);
                driverScheduleToBuild.setIndexDepotAsEndingPoint(indexEndDepotToSet);
                System.out.println(" new Driver schedule is add" + driverScheduleToBuild);

                solution.addSchedule(driverScheduleToBuild);
            }

        }
        return solution;
    }

    /**
     * Set CPLEX parameters for the new MIP model
     */
    private void setCplexParametersForNewMIP(IloCplex cplex) throws IloException {
        System.out.println("Setting CPLEX parameters...");
        // Time limit (in seconds) - adjust based on your needs
        cplex.setParam(IloCplex.Param.TimeLimit, 1800); // 1 hour

        // MIP gap tolerance - adjust based on your accuracy needs
        cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.000001); // 1%

//        // Number of threads - adjust based on your machine
        cplex.setParam(IloCplex.Param.Threads, 4);

        // Node selection strategy
        cplex.setParam(IloCplex.Param.MIP.Strategy.NodeSelect, 1); // Best bound

        System.out.println("CPLEX parameters configured");
    }

    /**
     * Print model statistics before solving
     */
    private void printModelStatistics(IloCplex cplex) throws IloException {
        System.out.println("========== MODEL STATISTICS ==========");
        // Count variables
        int totalVars = 0;
        int binaryVars = 0;
        int continuousVars = 0;

        // Count theta variables
        totalVars += this.newVarWhetherSelectTheSchedule.size();
        binaryVars += this.newVarWhetherSelectTheSchedule.size();

        // Count sigma variables
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                if (newVarTripSelectDepartureTime[i][t] != null) {
                    totalVars++;
                    binaryVars++;
                }
            }
        }

        // Count rho variables
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (newVarShortConnectionIsPerformed[i][j] != null) {
                    totalVars++;
                    binaryVars++;
                }
            }
        }

        // Count t_ij^k, and u_ij^k variables (continuous)
        for (int k = 0; k < this.newVarIdleTimeBetweenTripsInSchedule.size(); k++) {
            IloNumVar[][] scheduleIdleTimes = this.newVarIdleTimeBetweenTripsInSchedule.get(k);
            IloNumVar[][] scheduleU = this.newVarAuxiliaryU.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (scheduleIdleTimes[i][j] != null) {
                        totalVars++;
                        continuousVars++;
                    }

                    if (scheduleU[i][j] != null) {
                        totalVars++;
                        continuousVars++;
                    }

                }
            }
        }

        // Count auxiliary variables (z , x and b)
        for (int k = 0; k < this.newVarAuxiliaryZ.size(); k++) {
            IloNumVar[][] scheduleZ = this.newVarAuxiliaryZ.get(k);
            IloNumVar[][] scheduleX = this.newVarAuxiliaryX.get(k);

            IloNumVar[][] scheduleB = this.newVarAuxiliaryBIsShortConInSchedule.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (scheduleZ[i][j] != null) {
                        totalVars++;
                        binaryVars++;
                    }
                    if (scheduleB[i][j] != null) {
                        totalVars++;
                        binaryVars++;
                    }
                }
                int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
                int LDepTime = instance.getTrip(i).getLatestDepartureTime();
                int timeUnit = instance.getTimeSlotUnit();
                int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
                int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
                for (int n = nbUE; n <=nbUL; n++) {
                    int t = n * timeUnit;
                    if (scheduleX[i][t] != null) {
                        totalVars++;
                        binaryVars++;
                    }
                }
            }
        }

        System.out.println("Generated Schedules: " + this.driverSchedules.size());
        System.out.println("Total Variables: " + totalVars);
        System.out.println("  - Binary Variables: " + binaryVars);
        System.out.println("  - Continuous Variables: " + continuousVars);
        System.out.println("=====================================");
    }


    /**
     * Define all decision variables for the new MIP model
     */
    private void defineNewMIPVariables(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {

        System.out.println("Defining new MIP decision variables");

        // here is the second part including all variables related to the schedules
        //(1) θk: Binary variables for schedule selection (from ΩR)
        newVarWhetherSelectTheSchedule = new ArrayList<>();
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            if (whetherNewFinalMIPSolveInteger) {
                IloNumVar var = cplex.boolVar("theta_" + k);
                newVarWhetherSelectTheSchedule.add(var);


            } else {
                IloNumVar var = cplex.numVar(0, 1, "theta_" + k); //  continuous [0,1]
                newVarWhetherSelectTheSchedule.add(var);

            }


        }

        System.out.println("Check nbSchedules " + driverSchedulesInput.size());
        // here I am forcing new formulation must take some schedule
        System.out.println("60: " + this.driverSchedules.get(60));
        newVarWhetherSelectTheSchedule.get(60).setLB(1); //
        System.out.println("1848: " + this.driverSchedules.get(1848));
        newVarWhetherSelectTheSchedule.get(1848).setLB(1);
        System.out.println("2182: " + this.driverSchedules.get(2182));
        newVarWhetherSelectTheSchedule.get(2182).setLB(1);

        System.out.println("Created " + newVarWhetherSelectTheSchedule.size() + " theta variables for schedule selection");


        //(2) σ_i^t: Binary variables for trip departure time selection
        newVarTripSelectDepartureTime = new IloNumVar[instance.getNbTrips()][];
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);

            newVarTripSelectDepartureTime[i] = new IloNumVar[LDepTime];
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                if (whetherNewFinalMIPSolveInteger) {
                    newVarTripSelectDepartureTime[i][t] = cplex.boolVar("sigma_" + i + "_" + t);
                } else {
                    newVarTripSelectDepartureTime[i][t] = cplex.numVar(0, 1, "sigma_" + i + "_" + t);

                }
            }
        }
        System.out.println("Created sigma variables for trip departure time selection");

        //(3) ρ_ij: Binary variables for short connections
        newVarShortConnectionIsPerformed = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];
        int shortConnCount = 0;
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConnection = instance.getMinWaitingTime(i, j);
                    if (minConnection < instance.getShortConnectionTimeForDriver()) {
                        if (whetherNewFinalMIPSolveInteger) {
                            newVarShortConnectionIsPerformed[i][j] = cplex.boolVar("rho_" + i + "_" + j);
                        } else {
                            newVarShortConnectionIsPerformed[i][j] = cplex.numVar(0, 1, "rho_" + i + "_" + j);
                        }
                        shortConnCount++;
                    }
                }
            }
        }
        System.out.println("Created " + shortConnCount + " rho variables for short connections");


        //(4) t_ij^k: Continuous variables for idle time between trips in schedules
        newVarIdleTimeBetweenTripsInSchedule = new ArrayList<>();
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleIdleTimes = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];
            DriverSchedule schedule = driverSchedulesInput.get(k);

            //For each consecutive pair of trips in the schedule

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (schedule.getCoefficientHWhetherPerformArc(i, j) == 1) {

                            if (whetherNewFinalMIPSolveInteger) {
                                scheduleIdleTimes[i][j] = cplex.intVar(0, Integer.MAX_VALUE, "t_" + k + "_" + i + "_" + j);
                            } else {
                                scheduleIdleTimes[i][j] = cplex.numVar(0, Double.MAX_VALUE, "t_" + k + "_" + i + "_" + j);

                            }
                        }

                    }
                }
            }
            newVarIdleTimeBetweenTripsInSchedule.add(scheduleIdleTimes);
        }
        System.out.println("Created t_ij variables for idle time between trips");

        //(5) z_ij^k and b_ij^k: Auxiliary binary variables
        newVarAuxiliaryZ = new ArrayList<>();
        newVarAuxiliaryBIsShortConInSchedule = new ArrayList<>();

        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule schedule = driverSchedulesInput.get(k);
            IloNumVar[][] scheduleZ = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];
            IloNumVar[][] scheduleB = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (schedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            int minWaiting = instance.getMinWaitingTime(i, j);
                            if (minWaiting < instance.getShortConnectionTimeForDriver()) {

                                if (whetherNewFinalMIPSolveInteger) {
                                    scheduleZ[i][j] = cplex.boolVar("z_" + k + "_" + i + "_" + j);
                                    scheduleB[i][j] = cplex.boolVar("b_" + k + "_" + i + "_" + j);
                                } else {
                                    scheduleZ[i][j] = cplex.numVar(0, 1, "z_" + k + "_" + i + "_" + j);
                                    scheduleB[i][j] = cplex.numVar(0, 1, "b_" + k + "_" + i + "_" + j);
                                }
                            }
                        }

                    }
                }
            }
            newVarAuxiliaryZ.add(scheduleZ);
            newVarAuxiliaryBIsShortConInSchedule.add(scheduleB);
        }


        //(6) x_it^k : Auxiliary binary variables
        newVarAuxiliaryX = new ArrayList<>();

        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            IloNumVar[][] scheduleX = new IloNumVar[instance.getNbTrips()][instance.getEndingPlaningHorizon()];
            for (int i = 0; i < instance.getNbTrips(); i++) {
                if (driverSchedule.getNewCoefficientF(i) == 1) {
                    int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
                    int LDepTime = instance.getTrip(i).getLatestDepartureTime();
                    int timeUnit = instance.getTimeSlotUnit();
                    int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
                    int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
                    for (int n = nbUE; n <=nbUL; n++) {
                        int t = n * timeUnit;
                        if (whetherNewFinalMIPSolveInteger) {
                            scheduleX[i][t] = cplex.boolVar("x_" + k + "_" + i + "_" + t);
                        } else {
                            scheduleX[i][t] = cplex.numVar(0, 1, "x_" + k + "_" + i + "_" + t);
                        }
                    }
                }
            }
            newVarAuxiliaryX.add(scheduleX);
        }

        //(7) u_ij^k: Auxiliary integer variables
        newVarAuxiliaryU = new ArrayList<>();
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleU = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            int ubIdleTime = instance.getTrip(j).getLatestDepartureTime() - (instance.getTrip(i).getEarliestDepartureTime() + instance.getTrip(i).getDuration());
                            if (whetherNewFinalMIPSolveInteger) {
                                scheduleU[i][j] = cplex.intVar(0, ubIdleTime, "u_" + k + "_" + i + "_" + j);
                            } else {
                                scheduleU[i][j] = cplex.numVar(0, ubIdleTime, "u_" + k + "_" + i + "_" + j);
                            }
                        }

                    }
                }
            }
            newVarAuxiliaryU.add(scheduleU);
        }

        System.out.println("Created z_ij,b_ij, x_it^k and u_ij^k auxiliary variables");

    }


    /**
     * Define the objective function for the new MIP model
     * Objective: min{∑(ωk∈ΩR) c̃k*θk + ∑(ωk∈ΩR)∑((i,j)∈A) c^DI*t_ij^k}
     * Where c̃k = c^DF + ∑((i,j)∈A) c^DC * ỹ_ij^k (partial cost of feasible structure schedule)
     */
    private void defineNewMIPObjective(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        System.out.println("Defining MIP objective function...");
        IloLinearNumExpr obj = cplex.linearNumExpr();

        //(1) First part: ∑(ωk∈ΩR) c̃k*θk// Calculate c̃k = c^DF + ∑((i,j)∈A) c^DC * ỹ_ij^k
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule schedule = driverSchedulesInput.get(k);
            double partialCost = schedule.getPartialCostC();
            obj.addTerm(this.newVarWhetherSelectTheSchedule.get(k), partialCost);
        }

        //(2) Second part: ∑(ωk∈ΩR)∑((i,j)∈A) c^DI*u_ij^k| only hij^k==1
        double idleCostCoefficient = instance.getIdleTimeCostForDriverPerUnit(); // c^DI - you should set this to your actual idle cost
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            IloNumVar[][] newU = this.newVarAuxiliaryU.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            if (newU[i][j] != null) {
                                obj.addTerm(newU[i][j], idleCostCoefficient);
                            }
                        }
                    }
                }
            }
        }
        this.newObjective = cplex.addMinimize(obj);
        System.out.println("Objective function defined with partial costs and idle time penalties");
    }

    /**
     * Define the basic constraints for the new MIP model
     */
    private void defineNewMIPBasicConstraints(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        System.out.println("Defining basic MIP constraints for new MIP");

        // Initialize constraint arrays
        initializeNewConstraintArrays(driverSchedulesInput);
        // Add basic constraints
        newCnstTripSelectOneDepartureTime(cplex);// not related to the driver schedules
        newCnstOneDrivingInEachTrip(cplex, driverSchedulesInput);
        newCnstShortConnectionOut(cplex);
        newCnstShortConnectionIn(cplex);
        newCnstLinkZAndRho(cplex, driverSchedulesInput);
        newCnstDriverAvailability(cplex, driverSchedulesInput);

        System.out.println("Basic MIP constraints defined");
    }


    /**
     * Define additional constraints for the new MIP model
     */
    private void defineAdditionalMIPConstraints(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        System.out.println("Defining additional MIP constraints");
        // Add additional constraints
        newCnstDriverMaxWorkingTime(cplex, driverSchedulesInput);
        newCnstIdleTimeLowerBoundAccordingToTW(cplex, driverSchedulesInput);
        newCnstIdleTimeUpperBoundAccordingToTW(cplex, driverSchedulesInput);
        newCnstLinkIdleDepChoiceLower(cplex, driverSchedulesInput);
        newCnstLinkIdleDepChoiceUpper(cplex, driverSchedulesInput);
        newCnstShortConnectionDetection(cplex, driverSchedulesInput);
//
        newCnstZBLink(cplex, driverSchedulesInput);
        newCnstZThetaLink(cplex, driverSchedulesInput);
        newCnstZBThetaLink(cplex, driverSchedulesInput);

        newCnstLinkXSigma(cplex, driverSchedulesInput);
        newCnstLinkXTheta(cplex, driverSchedulesInput);
        newCnstLinkXSigmaTheta(cplex, driverSchedulesInput);

        newCnstLinearObj1(cplex, driverSchedulesInput);
        newCnstLinearObj2(cplex, driverSchedulesInput);
        newCnstLinearObj3(cplex, driverSchedulesInput);


        System.out.println("Additional MIP constraints defined");
    }


    /**
     * Initialize constraint arrays for the new MIP model
     */
    private void initializeNewConstraintArrays(ArrayList<DriverSchedule> driverSchedulesInput) {
        // Basic constraints
        newRangeConstraintTripSelectOneDepartureTime = new IloRange[instance.getNbTrips()];
        newRangeConstraintLinkSelectScheduleAndSelectDepartureTime = new IloRange[instance.getNbTrips()][instance.getEndingPlaningHorizon()];
        newRangeConstraintOneDriving = new IloRange[instance.getNbTrips()];
        newRangeConstraintShortConnectionOut = new IloRange[instance.getNbTrips()];
        newRangeConstraintShortConnectionIn = new IloRange[instance.getNbTrips()];
        newRangeConstraintLinkZAndRho = new IloRange[instance.getNbTrips()][instance.getNbTrips()];

        // Additional constraints
        newRangeConstraintMaxWorkingTime = new IloRange[driverSchedulesInput.size()];
        newRangeConstraintIdleTimeLowerBound = new ArrayList<>();
        newRangeConstraintIdleTimeUpperBound = new ArrayList<>();
        newRangeConstraintLinkageIdleDepChoiceLower = new ArrayList<>();
        newRangeConstraintLinkageIdleDepChoiceUpper = new ArrayList<>();
        newRangeConstraintShortConnectionDetection = new ArrayList<>();

        newRangeConstraintZBLink = new ArrayList<>();
        newRangeConstraintZThetaLink = new ArrayList<>();
        newRangeConstraintZBThetaLink = new ArrayList<>();


        newRangeConstraintXSigmaLink = new ArrayList<>();
        newRangeConstraintXThetaLink = new ArrayList<>();
        newRangeConstraintXSigmaThetaLink = new ArrayList<>();

        // Initialize time-indexed constraints
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int lDepTime = trip.getLatestDepartureTime();
            newRangeConstraintLinkSelectScheduleAndSelectDepartureTime[i] = new IloRange[lDepTime + instance.getTimeSlotUnit()];
        }

        // Initialize schedule-indexed constraint arrays
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            newRangeConstraintIdleTimeLowerBound.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);
            newRangeConstraintIdleTimeUpperBound.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);
            newRangeConstraintLinkageIdleDepChoiceLower.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);
            newRangeConstraintLinkageIdleDepChoiceUpper.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);
            newRangeConstraintShortConnectionDetection.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);

            newRangeConstraintZBLink.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);
            newRangeConstraintZThetaLink.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);
            newRangeConstraintZBThetaLink.add(new IloRange[instance.getNbTrips()][instance.getNbTrips()]);

            newRangeConstraintXSigmaLink.add(new IloRange[instance.getNbTrips()][instance.getEndingPlaningHorizon()]);
            newRangeConstraintXThetaLink.add(new IloRange[instance.getNbTrips()][instance.getEndingPlaningHorizon()]);
            newRangeConstraintXSigmaThetaLink.add(new IloRange[instance.getNbTrips()][instance.getEndingPlaningHorizon()]);


            newRangeConstraintLinearObj1 = new IloRange[driverSchedulesInput.size()][instance.getNbTrips()][instance.getNbTrips()];
            newRangeConstraintLinearObj2 = new IloRange[driverSchedulesInput.size()][instance.getNbTrips()][instance.getNbTrips()];
            newRangeConstraintLinearObj3 = new IloRange[driverSchedulesInput.size()][instance.getNbTrips()][instance.getNbTrips()];
        }
    }

    /**
     * Constraint: ∑(t∈[Ei,Li]) σ_i^t = 1, ∀i∈N
     * Each trip must select exactly one departure time
     */
    private void newCnstTripSelectOneDepartureTime(IloCplex cplex) throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                if (newVarTripSelectDepartureTime[i][t] != null) {
                    expr.addTerm(1.0, newVarTripSelectDepartureTime[i][t]);
                }
            }
            this.newRangeConstraintTripSelectOneDepartureTime[i] = cplex.addEq(expr, 1.0, "NewFTripSelectOneDepartureTime_" + i);
        }
    }

    /**
     * Constraint: ∑(ωk∈ΩR) g_i^k*θk = 1, ∀i∈N
     * Each trip must be covered exactly once by driving
     */
    private void newCnstOneDrivingInEachTrip(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        //Constraints for have only one driver driving in a trip
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int k = 0; k < driverSchedulesInput.size(); k++) {
                DriverSchedule schedule = driverSchedulesInput.get(k);
                double g_ik = schedule.getCoefficientG(i); // coefficient for driving trip i in schedule k
                if (g_ik != 0) {
                    expr.addTerm(g_ik, this.newVarWhetherSelectTheSchedule.get(k));
                }
            }
            this.newRangeConstraintOneDriving[i] = cplex.addEq(expr, 1.0, "OneDrivingInTrip_" + i);
        }
    }

    /**
     * Constraint: ∑(j:(i,j)∈A) ρ_ij ≤ 1, ∀i∈N
     * Short connection out constraints
     */
    private void newCnstShortConnectionOut(IloCplex cplex) throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int j = 0; j < instance.getNbTrips(); j++) {
                int minConnection = instance.getMinWaitingTime(i, j);
                if (minConnection < instance.getShortConnectionTimeForDriver()) {
                    if (newVarShortConnectionIsPerformed[i][j] != null) {
                        expr.addTerm(1.0, newVarShortConnectionIsPerformed[i][j]);
                    }
                }
            }

            this.newRangeConstraintShortConnectionOut[i] = cplex.addLe(expr, 1.0, "ShortConnectionOut_" + i);
        }
    }

    /**
     * Constraint: ∑(j:(j,i)∈A) ρ_ji ≤ 1, ∀i∈N
     * Short connection in constraints
     */
    private void newCnstShortConnectionIn(IloCplex cplex) throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int j = 0; j < instance.getNbTrips(); j++) {
                int minConnection = instance.getMinWaitingTime(j, i);
                if (minConnection < instance.getShortConnectionTimeForDriver()) {
                    if (newVarShortConnectionIsPerformed[j][i] != null) {
                        expr.addTerm(1.0, newVarShortConnectionIsPerformed[j][i]);
                    }
                }
            }
            this.newRangeConstraintShortConnectionIn[i] = cplex.addLe(expr, 1.0, "ShortConnectionIn_" + i);
        }
    }

    /**
     * Constraint: ∑(ωk∈ΩR) z_ij^k ≤ |D|*ρ_ij, ∀(i,j)∈A
     * Linking z variables with ρ
     */
    private void newCnstLinkZAndRho(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                int minConnection = instance.getMinWaitingTime(i, j);
                if (minConnection < instance.getShortConnectionTimeForDriver()) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                            && newVarShortConnectionIsPerformed[i][j] != null) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();

                        //∑(ωk∈ΩR) z_ij^k
                        for (int k = 0; k < driverSchedulesInput.size(); k++) {
                            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
                            if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                                IloNumVar[][] scheduleZ = this.newVarAuxiliaryZ.get(k);
                                if (scheduleZ[i][j] != null) {
                                    expr.addTerm(1.0, scheduleZ[i][j]);
                                }
                            }
                        }
                        //-|D|*ρ_ij
                        expr.addTerm(-instance.getMaxNbDriverAvailable(), newVarShortConnectionIsPerformed[i][j]);
                        this.newRangeConstraintLinkZAndRho[i][j] = cplex.addLe(expr, 0, "LinkZRho_" + i + "_" + j);
                    }
                }
            }
        }
    }

    /**
     * Constraint: ∑(ωk∈ΩR) θk ≤ |D|
     * Driver availability
     */
    private void newCnstDriverAvailability(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        IloLinearNumExpr expr = cplex.linearNumExpr();
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            expr.addTerm(1.0, this.newVarWhetherSelectTheSchedule.get(k));
        }
        this.newRangeConstraintDriverAvailability = cplex.addLe(expr, instance.getMaxNbDriverAvailable(), "DriverAvailability");
    }

    // now we put extra constraints******************************

    /**
     * Constraint: ∑(i∈N)∑(t∈[Ei,Li]) |(f_i^k==1,e_i^k==1 )  (t+Di)σ_i^t - ∑(i∈N)∑(t∈[Ei,Li])| (f_i^k==1,s_i^k==1)  t*σ_i^t ≤ T^W, ∀ωk∈ΩR
     * Driver Maximum Working Time
     */
    private void newCnstDriverMaxWorkingTime(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        double T_W = instance.getMaxWorkingTime(); // Maximum working time in minutes (8 hours) - adjust as needed

        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule schedule = driverSchedulesInput.get(k);
            IloNumVar[][] scheduleBeSelectedAndDepTime = this.newVarAuxiliaryX.get(k);
            IloLinearNumExpr expr = cplex.linearNumExpr();

            for (int i = 0; i < instance.getNbTrips(); i++) {
                Trip trip = instance.getTrip(i);

                if (schedule.getNewCoefficientF(i) == 1) {
                    int duration = trip.getDuration(); // Di
                    int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
                    int LDepTime = instance.getTrip(i).getLatestDepartureTime();
                    int timeUnit = instance.getTimeSlotUnit();
                    int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
                    int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
                    if (schedule.getCoefficientEWhetherLastTask(i) == 1) {
                        for (int n = nbUE; n <=nbUL; n++) {
                            int t = n * timeUnit;

                            if (newVarTripSelectDepartureTime[i][t] != null) {
                                // Add (t+Di)σ_i^t term
                                expr.addTerm(t + duration, scheduleBeSelectedAndDepTime[i][t]);
                            }
                        }
                    }
                    if (schedule.getCoefficientSWhetherFirstTask(i) == 1) {
                        for (int n = nbUE; n <=nbUL; n++) {
                            int t = n * timeUnit;
                            if (newVarTripSelectDepartureTime[i][t] != null) {
                                // Subtract t*σ_i^t term
                                expr.addTerm(-t, scheduleBeSelectedAndDepTime[i][t]);

                            }
                        }
                    }
                }
            }

            this.newRangeConstraintMaxWorkingTime[k] = cplex.addLe(expr, T_W, "MaxWorkingTime_" + k);
        }
    }

    // 约束linear working time constraints (1): x_{ik}^t <= σ_i^t   for any  i , t | f_ik=1
    private void newCnstLinkXSigma(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            IloNumVar[][] scheduleX = newVarAuxiliaryX.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                if (driverSchedule.getNewCoefficientF(i) == 1) {
                    int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
                    int LDepTime = instance.getTrip(i).getLatestDepartureTime();
                    int timeUnit = instance.getTimeSlotUnit();
                    int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
                    int nbUL = (int) Math.round(LDepTime /(double) timeUnit);

                    for (int n = nbUE; n <=nbUL; n++) {
                        int t = n * timeUnit;
                        if (scheduleX[i][t] != null && this.newVarTripSelectDepartureTime[i][t] != null) {
                            // 创建线性表达式: x_it^k - sigma_it
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(1.0, scheduleX[i][t]);      // +x_it^k
                            expr.addTerm(-1.0, this.newVarTripSelectDepartureTime[i][t]);   // -sigma_it
                            this.newRangeConstraintXSigmaLink.get(k)[i][t] = cplex.addLe(expr, 0, "LinkXSigma_" + k + "_" + i + "_" + t);
                        }
                    }
                }
            }
        }
    }

    // 约束linear working time：约束 (2): x_{ik}^t <= θ_k
    private void newCnstLinkXTheta(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            IloNumVar[][] scheduleX = newVarAuxiliaryX.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                if (driverSchedule.getNewCoefficientF(i) == 1) {
                    int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
                    int LDepTime = instance.getTrip(i).getLatestDepartureTime();
                    int timeUnit = instance.getTimeSlotUnit();
                    int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
                    int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
                    for (int n = nbUE; n <=nbUL; n++) {
                        int t = n * timeUnit;
                        if (scheduleX[i][t] != null) {
                            // 创建线性表达式: x_it^k - θ_k
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(1.0, scheduleX[i][t]);      // +x_it^k
                            expr.addTerm(-1.0, this.newVarWhetherSelectTheSchedule.get(k)); // -θ_k

                            this.newRangeConstraintXThetaLink.get(k)[i][t] =
                                    cplex.addLe(expr, 0, "LinkXTheta_" + k + "_" + i + "_" + t);
                        }
                    }
                }
            }
        }
    }

    // 约束linear working time：约束 (3): x_{ik}^t >= σ_i^t + θ_k - 1
    private void newCnstLinkXSigmaTheta(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            IloNumVar[][] scheduleX = newVarAuxiliaryX.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                if (driverSchedule.getNewCoefficientF(i) == 1) {
                    int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
                    int LDepTime = instance.getTrip(i).getLatestDepartureTime();
                    int timeUnit = instance.getTimeSlotUnit();
                    int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
                    int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
                    for (int n = nbUE; n <=nbUL; n++) {
                        int t = n * timeUnit;
                        if (scheduleX[i][t] != null && this.newVarTripSelectDepartureTime[i][t] != null) {
                            // 创建线性表达式: x_it^k - σ_i^t - θ_k
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(1.0, scheduleX[i][t]);                           // +x_it^k
                            expr.addTerm(-1.0, this.newVarTripSelectDepartureTime[i][t]); // -σ_i^t
                            expr.addTerm(-1.0, this.newVarWhetherSelectTheSchedule.get(k));                                  // -θ_k

                            // x_it^k - σ_i^t - θ_k >= -1 (等价于 x_it^k >= σ_i^t + θ_k - 1)
                            this.newRangeConstraintXSigmaThetaLink.get(k)[i][t] =
                                    cplex.addGe(expr, -1, "LinkXSigmaTheta_" + k + "_" + i + "_" + t);
                        }
                    }
                }
            }
        }
    }

    /**
     * Constraint: T^minP*ktheta^k ≤ t_ij^k, ∀ωk∈ΩR, ∀(i,j)∈A|h_ij^=1
     * 移项：0 ≤ t_ij^k-T^minP*h_ij^k*theta^k, ∀ωk∈ΩR, ∀(i,j)∈A|h_ij^=1
     * Idle Time Lower Bound
     */
    private void newCnstIdleTimeLowerBoundAccordingToTW(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        int T_minP = instance.getMinPlanTurnTime(); // Minimum planning time
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            IloNumVar[][] scheduleIdleTimes = this.newVarIdleTimeBetweenTripsInSchedule.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {

                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                            && scheduleIdleTimes[i][j] != null) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {

                            double idleLb = T_minP;
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(1, scheduleIdleTimes[i][j]);
                            expr.addTerm(-idleLb, this.newVarWhetherSelectTheSchedule.get(k));
                            this.newRangeConstraintIdleTimeLowerBound.get(k)[i][j] = cplex.addGe(expr, 0, "IdleTimeLower_" + k + "_" + i + "_" + j);

                        }
                    }
                }
            }
        }
    }

    /**
     * Constraint: t_ij^k ≤ (Lj-(Ei+Di))*h_ij^k *theta^k, ∀ωk∈ΩR, ∀(i,j)∈A|h_ij^=1
     * 移项： t_ij^k -(Lj-(Ei+Di))*h_ij^k *theta^k≤ 0, ∀ωk∈ΩR, ∀(i,j)∈A|h_ij^=1
     * Idle Time Upper Bound
     */
    private void newCnstIdleTimeUpperBoundAccordingToTW(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {

            IloNumVar[][] scheduleIdleTimes = this.newVarIdleTimeBetweenTripsInSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                            && scheduleIdleTimes[i][j] != null) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {

                            Trip tripI = instance.getTrip(i);
                            Trip tripJ = instance.getTrip(j);
                            int eDepT_i = tripI.getEarliestDepartureTime();
                            int lDepT_j = tripJ.getLatestDepartureTime();
                            int D_i = tripI.getDuration();
                            double idleUb = (lDepT_j - (eDepT_i + D_i));
                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            expr.addTerm(1, scheduleIdleTimes[i][j]);
                            expr.addTerm(-idleUb, this.newVarWhetherSelectTheSchedule.get(k));

                            this.newRangeConstraintIdleTimeUpperBound.get(k)[i][j] = cplex.addLe(expr, 0, "IdleTimeUpper_" + k + "_" + i + "_" + j);
                        }

                    }
                }
            }
        }
    }

    /**
     * Constraint: ∑(t'∈[Ej,Lj]) t'*σ_j^t' - ∑(t∈[Ei,Li]) (t+Di)*σ_i^t - M(1- theta^k) ≤ t_ij^k  | h_ij^k==1
     * 移项：∑(t'∈[Ej,Lj]) t'*σ_j^t' - ∑(t∈[Ei,Li]) (t+Di)*σ_i^t +M *theta^k) - t_ij^k  ≤M     |h_ij^k==1
     * Idle Range Lower Bound calculate by departure time choice
     */
    private void newCnstLinkIdleDepChoiceLower(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {

        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleIdleTimes = this.newVarIdleTimeBetweenTripsInSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                            && scheduleIdleTimes[i][j] != null) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            double M = instance.getMaxWaitingTime(i, j) + 1; // Big M value (24 hours) - adjust as needed
                            Trip tripI = instance.getTrip(i);
                            Trip tripJ = instance.getTrip(j);
                            int D_i = tripI.getDuration();

                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            int timeUnit = instance.getTimeSlotUnit();
                            // Add ∑(t'∈[Ej,Lj]) t'*σ_j^t'
                            int EDepTime_j = tripJ.getEarliestDepartureTime();
                            int LDepTime_j = tripJ.getLatestDepartureTime();

                            int nbUE_j = (int) Math.round(EDepTime_j / (double)timeUnit);
                            int nbUL_j = (int) Math.round(LDepTime_j /(double) timeUnit);
                            for (int n = nbUE_j; n <=nbUL_j; n++) {
                                int t_prime = n * timeUnit;
                                if (newVarTripSelectDepartureTime[j][t_prime] != null) {
                                    expr.addTerm(t_prime, newVarTripSelectDepartureTime[j][t_prime]);
                                }
                            }

                            // Subtract ∑(t∈[Ei,Li]) (t+Di)*σ_i^t
                            int EDepTime_i = tripI.getEarliestDepartureTime();
                            int LDepTime_i = tripI.getLatestDepartureTime();
                            int nbUE_i = (int) Math.round(EDepTime_i/ (double)timeUnit);
                            int nbUL_i = (int) Math.round(LDepTime_i /(double) timeUnit);

                            for (int n = nbUE_i; n <=nbUL_i; n++) {
                                int t = n * timeUnit;
                                if (newVarTripSelectDepartureTime[i][t] != null) {
                                    expr.addTerm(-(t + D_i), newVarTripSelectDepartureTime[i][t]);
                                }
                            }
                            //Add  M*theta^k
                            double coefficient = M;
                            expr.addTerm(coefficient, this.newVarWhetherSelectTheSchedule.get(k));

                            // Subtract t_ij^k
                            expr.addTerm(-1, this.newVarIdleTimeBetweenTripsInSchedule.get(k)[i][j]);

                            this.newRangeConstraintLinkageIdleDepChoiceLower.get(k)[i][j] = cplex.addLe(expr, M, "IdleRangeLower_" + k + "_" + i + "_" + j);
                        }

                    }
                }
            }
        }
    }

    /**
     * Constraint: t_ij^k ≤ ∑(t'∈[Ej,Lj]) t'*σ_j^t' - ∑(t∈[Ei,Li]) (t+Di)*σ_i^t + M(1-h_ij^k *theta^k)
     * 移项：- M ≤ ∑(t'∈[Ej,Lj]) t'*σ_j^t' - ∑(t∈[Ei,Li]) (t+Di)*σ_i^t -Mh_ij^k *theta^k -t_ij^k
     * Idle Range Upper Bound
     */
    private void newCnstLinkIdleDepChoiceUpper(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        double M = instance.getEndingPlaningHorizon(); // Big M value

        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleIdleTimes = this.newVarIdleTimeBetweenTripsInSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                            && scheduleIdleTimes[i][j] != null) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            Trip tripI = instance.getTrip(i);
                            Trip tripJ = instance.getTrip(j);
                            int D_i = tripI.getDuration();

                            IloLinearNumExpr expr = cplex.linearNumExpr();
                            int timeUnit = instance.getTimeSlotUnit();

                            // Add ∑(t'∈[Ej,Lj]) t'*σ_j^t'
                            int EDepTime_j = tripJ.getEarliestDepartureTime();
                            int LDepTime_j = tripJ.getLatestDepartureTime();
                            int nbUE_j = (int) Math.round(EDepTime_j / (double)timeUnit);
                            int nbUL_j = (int) Math.round(LDepTime_j /(double) timeUnit);


                            for (int n = nbUE_j; n <=nbUL_j; n++) {
                                int t_prime=n*timeUnit;
                                if (newVarTripSelectDepartureTime[j][t_prime] != null) {
                                    expr.addTerm(t_prime, newVarTripSelectDepartureTime[j][t_prime]);
                                }
                            }

                            // Subtract ∑(t∈[Ei,Li]) (t+Di)*σ_i^t
                            int EDepTime_i = tripI.getEarliestDepartureTime();
                            int LDepTime_i = tripI.getLatestDepartureTime();
                            int nbUE_i = (int) Math.round(EDepTime_i/ (double)timeUnit);
                            int nbUL_i= (int) Math.round(LDepTime_i /(double) timeUnit);
                            for (int n = nbUE_i; n <=nbUL_i; n++) {
                                int t = n * timeUnit;
                                if (newVarTripSelectDepartureTime[i][t] != null) {
                                    expr.addTerm(-(t + D_i), newVarTripSelectDepartureTime[i][t]);
                                }
                            }

                            double coefficient = M;

                            // Subtract Mh_ij^k *theta^k
                            expr.addTerm(-coefficient, this.newVarWhetherSelectTheSchedule.get(k));

                            // Subtract t_ij^k
                            expr.addTerm(-1, this.newVarIdleTimeBetweenTripsInSchedule.get(k)[i][j]);

                            // Add constant put the right side: -M

                            this.newRangeConstraintLinkageIdleDepChoiceUpper.get(k)[i][j] = cplex.addGe(expr, -M, "IdleRangeUpper_" + k + "_" + i + "_" + j);
                        }
                    }
                }
            }
        }
    }

    /**
     * Constraint: t_ij^k ≥ T^DS*h_ij^k*theta^k + (T^minP - T^DS)*b_ij^k, ∀ωk∈ΩR, ∀(i,j)∈A
     * 移项：t_ij^k - (T^minP - T^DS)*b_ij^k-T^DS*h_ij^k*theta^k≥0 ， ∀ωk∈ΩR, ∀(i,j)∈A
     * Short Connection Detection
     */
    private void newCnstShortConnectionDetection(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        double T_minP = instance.getMinPlanTurnTime(); // Minimum planning time
        double T_DS = instance.getShortConnectionTimeForDriver(); // Driver status change time

        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleIdleTimes = this.newVarIdleTimeBetweenTripsInSchedule.get(k);
            IloNumVar[][] scheduleShortConnection = this.newVarAuxiliaryBIsShortConInSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    int minConnection = instance.getMinWaitingTime(i, j);
                    if (minConnection < instance.getShortConnectionTimeForDriver()) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                            if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                                if (scheduleIdleTimes[i][j] != null && scheduleShortConnection[i][j] != null) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(1, scheduleIdleTimes[i][j]);
                                    expr.addTerm(T_DS - T_minP, scheduleShortConnection[i][j]);
                                    double coefficient = T_DS;
                                    expr.addTerm(-coefficient, this.newVarWhetherSelectTheSchedule.get(k));
                                    this.newRangeConstraintShortConnectionDetection.get(k)[i][j] = cplex.addGe(expr, 0, "ShortConnectionDetection_" + k + "_" + i + "_" + j);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Constraint: z_ij^k ≤ b_ij^k, ∀ωk∈ΩR, ∀(i,j)∈A
     * 移项：z_ij^k - b_ij^k ≤ 0, ∀ωk∈ΩR, ∀(i,j)∈A
     * Z variable upper bound by B variable
     */
    private void newCnstZBLink(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleZVars = this.newVarAuxiliaryZ.get(k);
            IloNumVar[][] scheduleBVars = this.newVarAuxiliaryBIsShortConInSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            int minConnection = instance.getMinWaitingTime(i, j);
                            if (minConnection < instance.getShortConnectionTimeForDriver()) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                                        && scheduleZVars[i][j] != null
                                        && scheduleBVars[i][j] != null) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(1, scheduleZVars[i][j]); // z_ij^k
                                    expr.addTerm(-1, scheduleBVars[i][j]); // -b_ij^k
                                    this.newRangeConstraintZBLink.get(k)[i][j] = cplex.addLe(expr, 0, "ZBLink_" + k + "_" + i + "_" + j);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Constraint: z_ij^k ≤ θk, ∀ωk∈ΩR, ∀(i,j)∈A
     * 移项：z_ij^k - θk ≤ 0, ∀ωk∈ΩR, ∀(i,j)∈A
     * Z variable upper bound by Theta variable
     */
    private void newCnstZThetaLink(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleZVars = this.newVarAuxiliaryZ.get(k);
            IloNumVar scheduleThetaVar = this.newVarWhetherSelectTheSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            int minConnection = instance.getMinWaitingTime(i, j);
                            if (minConnection < instance.getShortConnectionTimeForDriver()) {
                                if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                                        && scheduleZVars[i][j] != null) {
                                    IloLinearNumExpr expr = cplex.linearNumExpr();
                                    expr.addTerm(1, scheduleZVars[i][j]); // z_ij^k
                                    expr.addTerm(-1, scheduleThetaVar); // -θk

                                    this.newRangeConstraintZThetaLink.get(k)[i][j] = cplex.addLe(expr, 0, "ZThetaLink_" + k + "_" + i + "_" + j);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Constraint: z_ij^k ≥ b_ij^k + θk - 1, ∀ωk∈ΩR, ∀(i,j)∈A
     * 移项：z_ij^k - b_ij^k - θk ≥ -1, ∀ωk∈ΩR, ∀(i,j)∈A
     * Z variable lower bound linking B and Theta variables
     */
    private void newCnstZBThetaLink(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] scheduleZVars = this.newVarAuxiliaryZ.get(k);
            IloNumVar[][] scheduleBVars = this.newVarAuxiliaryBIsShortConInSchedule.get(k);
            IloNumVar scheduleThetaVar = this.newVarWhetherSelectTheSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    int minConnection = instance.getMinWaitingTime(i, j);
                    if (minConnection < instance.getShortConnectionTimeForDriver()) {
                        if (instance.whetherHavePossibleArcAfterCleaning(i, j)
                                && scheduleZVars[i][j] != null
                                && scheduleBVars[i][j] != null) {

                            if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(1, scheduleZVars[i][j]);    // +z_ij^k
                                expr.addTerm(-1, scheduleBVars[i][j]);   // -b_ij^k
                                expr.addTerm(-1, scheduleThetaVar);      // -θk

                                this.newRangeConstraintZBThetaLink.get(k)[i][j] = cplex.addGe(
                                        expr,
                                        -1,
                                        "ZBThetaLink_" + k + "_" + i + "_" + j

                                );
                            }
                        }
                    }
                }
            }
        }
    }


    // 约束 (linear_objconst1): u_{ij}^k <= M * θ_k
    private void newCnstLinearObj1(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] u_k = newVarAuxiliaryU.get(k);
            IloNumVar theta_k = newVarWhetherSelectTheSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            double M = instance.getMaxWaitingTime(i, j) + 1; // 大M常数，根据实际问题调整
                            if (i != j && u_k[i][j] != null) { // 假设您有变量u_{ij}^k
                                // 创建线性表达式: u_{ij}^k - M * θ_k
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(1.0, u_k[i][j]);    // +u_{ij}^k
                                expr.addTerm(-M, theta_k);                   // -M * θ_k
                                this.newRangeConstraintLinearObj1[k][i][j] =
                                        cplex.addLe(expr, 0, "LinearObj1_" + k + "_" + i + "_" + j);
                            }
                        }
                    }
                }
            }
        }
    }

    // 约束 (linear_objconst2): u_{ij}^k <= t_{ij}^k
    private void newCnstLinearObj2(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] u_k = newVarAuxiliaryU.get(k);

            IloNumVar[][] t_k = this.newVarIdleTimeBetweenTripsInSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (i != j && u_k[i][j] != null && t_k[i][j] != null) {
                            if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                                // 创建线性表达式: u_{ij}^k - t_{ij}^k
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(1.0, u_k[i][j]);    // +u_{ij}^k
                                expr.addTerm(-1.0, t_k[i][j]);   // -t_{ij}^k

                                this.newRangeConstraintLinearObj2[k][i][j] =
                                        cplex.addLe(expr, 0, "LinearObj2_" + k + "_" + i + "_" + j);
                            }
                        }
                    }
                }
            }
        }
    }

    // 约束 (linear_objconst3): u_{ij}^k >= t_{ij}^k - M(1-θ_k)
    private void newCnstLinearObj3(IloCplex cplex, ArrayList<DriverSchedule> driverSchedulesInput) throws IloException {
        for (int k = 0; k < driverSchedulesInput.size(); k++) {
            IloNumVar[][] u_k = newVarAuxiliaryU.get(k);
            IloNumVar theta_k = newVarWhetherSelectTheSchedule.get(k);
            IloNumVar[][] t_k = this.newVarIdleTimeBetweenTripsInSchedule.get(k);
            DriverSchedule driverSchedule = driverSchedulesInput.get(k);

            for (int i = 0; i < instance.getNbTrips(); i++) {
                for (int j = 0; j < instance.getNbTrips(); j++) {
                    if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                        if (driverSchedule.getCoefficientHWhetherPerformArc(i, j) == 1) {
                            if (i != j && u_k[i][j] != null && t_k[i][j] != null) {
                                double M = instance.getMaxWaitingTime(i, j) + 1; // 大M常数，根据实际问题调整

                                // 创建线性表达式: u_{ij}^k - t_{ij}^k + M * θ_k
                                // 原约束: u_{ij}^k >= t_{ij}^k - M(1-θ_k)
                                // 重写为: u_{ij}^k - t_{ij}^k - M * θ_k >= -M
                                IloLinearNumExpr expr = cplex.linearNumExpr();
                                expr.addTerm(1.0, u_k[i][j]);    // +u_{ij}^k
                                expr.addTerm(-1.0, t_k[i][j]);   // -t_{ij}^k
                                expr.addTerm(-M, theta_k);                    // -M * θ_k
                                this.newRangeConstraintLinearObj3[k][i][j] =
                                        cplex.addGe(expr, -M, "LinearObj3_" + k + "_" + i + "_" + j);
                            }
                        }
                    }
                }
            }
        }
    }


    private DriverSchedule getComparedDriverSchedule() {
        DriverSchedule comparedSchedule = new DriverSchedule(instance);

//        comparedSchedule.setIdOfDepot(0);
//        comparedSchedule.setIndexDepotAsStartingPoint(50);
//        comparedSchedule.setIndexDepotAsEndingPoint(52);
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(7), true, 30));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(8), true, 105));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(9), true, 190));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(10), true, 290));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(11), true, 360));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(12), true, 465));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(13), true, 525));
//
//        comparedSchedule.setIdOfDepot(1);
//        comparedSchedule.setIndexDepotAsStartingPoint(51);
//        comparedSchedule.setIndexDepotAsEndingPoint(53);
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(47), true, 170));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(48), true, 230));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(49), true, 305));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(19), true, 365));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(20), true, 425));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(23), true, 500));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(31), true, 575));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(32), true, 700));
////        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(36), true, 785));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(30), true, 405));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(34), true, 465));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(18), true, 525));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(24), true, 590));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(25), true, 660));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(26), true, 725));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(4), true, 800));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(37), true, 870));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(6), true, 930));


        comparedSchedule.setIdOfDepot(1);
        comparedSchedule.setIndexDepotAsStartingPoint(26);
        comparedSchedule.setIndexDepotAsEndingPoint(28);


//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(14), true, 170));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(15), true, 230));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(16), true, 305));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(17), true, 380));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(0), true, 440));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(1), true, 500));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(2), false, 585));


//
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(18), true, 525));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(2), true, 585));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(3), true, 645));
//        comparedSchedule.addTripWithWorkingStatusInSchedule(new TripWithWorkingStatusAndDepartureTime(instance.getTrip(24), true, 705));


        return comparedSchedule;
    }

    private boolean whetherCouldChangeConsiderFeasibilityConnection(int depTime_new, DriverSchedule driverSchedule,
                                                                    int j, int durationCurrentTrip) {
        boolean whetherCouldChange = false;
        if (j == 0) {
            TripWithWorkingStatusAndDepartureTime tripLatter = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j + 1);
            int depTripLatter = tripLatter.getDepartureTime();
            int conTime_ToNext = depTripLatter - (depTime_new + durationCurrentTrip);
            if (conTime_ToNext >= instance.getMinPlanTurnTime()) {
                whetherCouldChange = true;
            }
        } else if (j == driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().size() - 1) {
            TripWithWorkingStatusAndDepartureTime tripFormer = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j - 1);
            int depTripFormer = tripFormer.getDepartureTime();
            int durationFormer = tripFormer.getDuration();
            int conTime_FromFormer = depTime_new - (depTripFormer + durationFormer);
            if (conTime_FromFormer >= instance.getMinPlanTurnTime()) {
                whetherCouldChange = true;
            }
        } else {
            TripWithWorkingStatusAndDepartureTime tripLatter = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j + 1);
            int depTripLatter = tripLatter.getDepartureTime();
            int conTime_ToNext = depTripLatter - (depTime_new + durationCurrentTrip);
            TripWithWorkingStatusAndDepartureTime tripFormer = driverSchedule.getTripWithWorkingStatusAndDepartureTimeArrayList().get(j - 1);
            int depTripFormer = tripFormer.getDepartureTime();
            int durationFormer = tripFormer.getDuration();
            int conTime_FromFormer = depTime_new - (depTripFormer + durationFormer);
            if (conTime_ToNext >= instance.getMinPlanTurnTime() && conTime_FromFormer >= instance.getMinPlanTurnTime()) {
                whetherCouldChange = true;
            }
        }
        return whetherCouldChange;
    }

    // here is prepare the dual value for the labeling algorithm ********2025.3.3// and for the lower bound 2025.4.07
    public double getDualValueFromOneDriving(int idOfTask) {
        return this.dualSolutionOfRMPConstraintOneDriverDriving[idOfTask];
    }

    public double getDualValueFromOneDepartureTimeForEachTask(int idOfTask) {
        return this.dualSolutionOfRMPCnstTripSelectOneDepartureTime[idOfTask];
    }

    public double getDualValueFromNbAvailableDriver() {
        return this.dualSolutionOfRMPConstraintNbAvailableDriver;
    }

    public double getDualValueFromLinkSelectScheduleAndTripOneDepTime(int idTask, int depTime) {
        return this.dualSolutionOfRMPCnstLinkSelectScheduleAndTripOneDepartureTime[idTask][depTime];
    }

    public double getDualValueFromLinkDriverShortConTime(int idFTask, int idSTask) {
        return this.dualSolutionOfRMPConstraintLinkShortConnection[idFTask][idSTask];
    }

    public double getDualValueFromDriverShortConnectionOut(int idOfTrip) {
        return this.dualSolutionOfRMPCnstShortConnectionOut[idOfTrip];//xi_i
    }

    public double getDualValueFromDriverShortConnectionIn(int idOfTrip) {
        return this.dualSolutionOfRMPCnstShortConnectionIn[idOfTrip];//eta_i
    }

    public double getDualValueFromWhetherSelectDriverShortConnection(int idFTrip, int idSTrip) {
        return this.dualSolutionOfRMPCnstUpperboundWhetherSelectDriverShortConnection[idFTrip][idSTrip];//alpha_i,j
    }

    public double getDualValueFromUpperBoundOfSelectDepartureTimeForTip(int idTrip, int depTime) {
        return this.dualSolutionofRMPCnstUpperBoundTripSelectDepartureTime[idTrip][depTime];
    }

    public double getDualRelatedDriving() throws IloException {
        double sumBeta = 0;
        for (int i = 0; i < instance.getNbTrips(); i++) {
            //2025.1.6
            Trip trip = instance.getTrip(i);
            sumBeta = sumBeta + this.getDualValueFromOneDriving(i);
            if (this.getDualValueFromOneDriving(i) != 0) {
                System.out.println("dual value beta_i " + i + " is " + this.getDualValueFromOneDriving(i));
            }
        }
        return sumBeta;
    }

    public double getLagrangianLowerBoundOnOptimalValueOfRMP() throws IloException {
        double LagrangianLowerBound;
        double sumLambda = 0;//2025.1.6
        double sumDelta = 0;//2025.1.6
        double sumXi = 0;
        double sumEta = 0;
        double sumAlpha = 0;
        double sumZeta = 0;
        double sumMu = 0;//2025.1.6

        System.out.println("check dual related whether is driving: " + getDualRelatedDriving());
        System.out.println("Check gamma " + this.getDualValueFromNbAvailableDriver());

        for (int i = 0; i < instance.getNbTrips(); i++) {
            //2025.1.6
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                sumLambda = sumLambda + this.getDualValueFromOneDepartureTimeForEachTask(i);//FromConstraintTripSelectOneDepartureTime()[i];//20256.1.6 new lambda
            }
        }
        //System.out.println("Check sumLambda " + sumLambda);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            //2025.1.6
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                sumDelta = sumDelta + this.getDualValueFromLinkSelectScheduleAndTripOneDepTime(i, t);

            }
        }
        // System.out.println("Check sumDelta " + sumDelta);
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) { //2025.1.10
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) { // change 2025.1.10
                        sumXi = sumXi + this.getDualValueFromDriverShortConnectionOut(i);//this.getDualFromConstraintShortConnectionOut()[i];
                    }
                }
            }
        }
        //System.out.println("Check sumXi " + sumXi);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {//2025.1.10
                if (instance.whetherHavePossibleArcAfterCleaning(j, i)) {
                    int minWaitingTime = instance.getMinWaitingTime(j, i);
                    if (minWaitingTime < this.instance.getShortConnectionTimeForDriver()) {
                        sumEta = sumEta + this.getDualValueFromDriverShortConnectionIn(i); //this.getDualFromConstraintShortConnectionIn()[i]
                    }
                }
            }
        }
        // System.out.println("Check sumEta " + sumEta);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //2025.1.10
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        sumAlpha = sumAlpha + this.getDualValueFromWhetherSelectDriverShortConnection(i, j);//this.getDualFromConstraintWhetherSelectShortConnection()[i][j]
                    }
                }
            }
        }
        // System.out.println("Check sumAlpha " + sumAlpha);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                //2025.1.6
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        sumZeta = sumZeta + this.getDualValueFromLinkDriverShortConTime(i, j); //this.getDualFromConstraintLinkShortConnection()[i][j];
                    }
                }
            }
        }
        //System.out.println("Check sumZeta " + sumZeta);

        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                sumMu = sumMu + this.getDualValueFromUpperBoundOfSelectDepartureTimeForTip(i, t);//this.getDualFromConstraintsUpperBoundTripSelectDepartureTime()[i][t];
            }

        }
        //System.out.println("Check sumMu " + sumMu);

        //Comment 7: the lagrangianLowerBound
        LagrangianLowerBound = this.cplex.getObjValue() - sumLambda - sumMu - sumXi - sumEta - sumAlpha
                + instance.getMaxNbDriverAvailable() * (sumZeta + subPSolvedByLabelingAlg_GenMinReducedCostPathsForAllDepots.getMostMinReducedCost())// change 3.19
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

    public double getTimeForPostProcessOfRichScheduleByDepartureTimeInSec() {
        return this.timeForPostProcessOfRichScheduleByDepartureTime;
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

    public double getTimeForSolvingLastIntegerProblemInSec() {
        return this.timeForSolvingLastIntegerProblemInSec;
    }

    public double getTotalTimeOfSolRMPByCplex() {
        return this.totalTimeOfSolRMPByCplex;
    }

    public double getTotalTimeCostForSolvingSubProblemByLabeling() {
        return this.totalTimeCostForSolvingSubProblemByLabeling;
    }

    public double getTotalTimeOfLabeling() {
        return this.totalTimeOfLabeling;
    }


    //add following on 2025.4.3
    public long getTotalLabelsGenerateForAllIterations() {
        return this.totalLabelsGenerateForAllIterations;
    }

    public long getFinalNbLabelsLastIteration() {
        return this.finalNbLabelsLastIteration;
    }

    public long getAverageLabelsByIteration() {
        long nbLabelOnAverage = this.getTotalLabelsGenerateForAllIterations() / this.getFinalNbIterations();
        return nbLabelOnAverage;
    }


    public boolean containsSchedule(DriverSchedule newSchedule) {// to add some new schedules after CG
        for (DriverSchedule existing : this.driverSchedules) {
            if (existing.isTheSame(newSchedule)) {
                return true;
            }
        }
        return false;
    }

    public int getNbColumnsGeneratedByPostProcess() {
        return this.nbColumnsGeneratedByPostProcess;
    }


    //******here I  try to use branch and price****************************************8888
    public IloCplex getCplex() {
        return this.cplex;
    }

    public ArrayList<IloNumVar> getVarWhetherSelectTheSchedule() {
        return this.varWhetherSelectTheSchedule;
    }

    public ArrayList<DriverSchedule> getDriverSchedules() {
        return this.driverSchedules;
    }

    public IloNumVar getVarForSchedule(DriverSchedule schedule) {
        int idx = this.driverSchedules.indexOf(schedule);
        if (idx >= 0) return this.varWhetherSelectTheSchedule.get(idx);
        return null;
    }

    public void addDriverSchedule(DriverSchedule schedule, String sourceTag) throws IloException {
        this.driverSchedules.add(schedule);

        // (1) 目标函数中的系数
        IloColumn column = cplex.column(objective, schedule.getCostC());

        // (2) 约束2：Link schedule 与 trip-departure
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                double coeffF = schedule.getCoefficientF(i, t);
                column = column.and(cplex.column(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t], coeffF));
            }
        }

        // (3) 约束3：每个 trip 只被一个 driver 覆盖
        for (int i = 0; i < instance.getNbTrips(); i++) {
            column = column.and(cplex.column(this.rangeConstraintOneDriving[i], schedule.getCoefficientG(i)));
        }

        // (4) 约束6：link short connection 和 schedule
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConnection = instance.getMinWaitingTime(i, j);
                    if (minConnection < instance.getShortConnectionTimeForDriver()) {
                        double coeffB = schedule.getCoefficientForShortConnectionB(i, j);
                        column = column.and(cplex.column(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j], coeffB));
                    }
                }
            }
        }

        // (5) 约束7：驱动人数限制
        column = column.and(cplex.column(this.rangeNbAvailableDriver, 1));

        // (6) 创建变量并加入模型
        IloNumVar var = cplex.numVar(column, 0, Double.MAX_VALUE);
        this.varWhetherSelectTheSchedule.add(var);
    }




    //20250729*****************************************

    public double getTimeForCreateModelInSec(){
        double timeForCreateModelInSec=this.timeForCreateModel/1000;
        return timeForCreateModelInSec;
    }
    /**
     * 初始化变量容器
     */
    private void initializeVariableContainers() {
        this.varTripSelectDepartureTime = new IloNumVar[instance.getNbTrips()][instance.getEndingPlaningHorizon()];
        this.varWhetherSelectTheSchedule = new ArrayList<>();
        this.varShortConnectionIsPerformed = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];
    }

    /**
     * 创建初始的空目标函数
     */
    private void defineInitialObjectiveFunction() throws IloException {
        IloLinearNumExpr obj = this.cplex.linearNumExpr();
        this.objective = this.cplex.addMinimize(obj);
    }

    /**
     * 创建所有约束（初始时都是空的）
     */
    private void createAllConstraintsEmpty() throws IloException {
        // 约束1: 每个trip选择一个出发时间 ∑σ_i^t = 1
        createTripSelectOneDepartureTimeConstraints();

        // 约束2: 链接schedule和出发时间选择 ∑f_i^k*θ_k - |D|*σ_i^t ≤ 0
        createLinkScheduleAndDepartureTimeConstraints();

        // 约束3: 每个trip一个驾驶员 ∑g_i^k*θ_k = 1
        createOneDrivingConstraints();

        // 约束4: 短连接出 ∑ρ_ij ≤ 1
        createShortConnectionOutConstraints();

        // 约束5: 短连接入 ∑ρ_ji ≤ 1
        createShortConnectionInConstraints();

        // 约束6: 链接短连接和选择的schedule ∑b_ij^k*θ_k - |D|*ρ_ij ≤ 0
        createLinkShortConnectionConstraints();

        // 约束7: 驾驶员数量限制 ∑θ_k ≤ |D|
        createDriverAvailabilityConstraint();

        // 约束8: 短连接变量上界 ρ_ij ≤ 1
        createShortConnectionUpperBoundConstraints();

        // 约束9: 出发时间选择变量上界 σ_i^t ≤ 1
        createTripDepartureTimeUpperBoundConstraints();
    }

    /**
     * 约束1: ∑σ_i^t = 1 (初始时左边为0)
     */
    private void createTripSelectOneDepartureTimeConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            // 初始时左边为0，等式右边为1
            this.rangeConstraintTripSelectOneDepartureTime[i] =
                    this.cplex.addEq(expr, 1, "TripSelectOneDepartureTime_" + i);
        }
    }

    /**
     * 约束2: ∑f_i^k*θ_k - |D|*σ_i^t ≤ 0 (初始时左边为0)
     */
    private void createLinkScheduleAndDepartureTimeConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                IloLinearNumExpr expr = this.cplex.linearNumExpr();
                // 初始时左边为0
                this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t] =
                        this.cplex.addLe(expr, 0, "link trip_" + i + " time_" + t);
            }
        }
    }

    /**
     * 约束3: ∑g_i^k*θ_k = 1 (初始时左边为0)
     */
    private void createOneDrivingConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            // 初始时左边为0，等式右边为1
            this.rangeConstraintOneDriving[i] =
                    this.cplex.addEq(expr, 1, "OneDrivingInTrip_" + i);
        }
    }

    /**
     * 约束4: ∑ρ_ij ≤ 1 (初始时左边为0)
     */
    private void createShortConnectionOutConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            // 初始时左边为0
            this.rangeConstraintShortConnectionOut[i] =
                    this.cplex.addLe(expr, 1, "ShortConnectionOut" + i);
        }
    }

    /**
     * 约束5: ∑ρ_ji ≤ 1 (初始时左边为0)
     */
    private void createShortConnectionInConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = this.cplex.linearNumExpr();
            // 初始时左边为0
            this.rangeConstraintShortConnectionIn[i] =
                    this.cplex.addLe(expr, 1, "ShortConnectionIn" + i);
        }
    }

    /**
     * 约束6: ∑b_ij^k*θ_k - |D|*ρ_ij ≤ 0 (初始时左边为0)
     */
    private void createLinkShortConnectionConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConTime = instance.getMinWaitingTime(i, j);
                    if (minConTime < this.instance.getShortConnectionTimeForDriver()) {
                        IloLinearNumExpr expr = this.cplex.linearNumExpr();
                        // 初始时左边为0
                        this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j] =
                                this.cplex.addLe(expr, 0, "link" + i + j);
                    }
                }
            }
        }
    }

    /**
     * 约束7: ∑θ_k ≤ |D| (初始时左边为0)
     */
    private void createDriverAvailabilityConstraint() throws IloException {
        IloLinearNumExpr expr = this.cplex.linearNumExpr();
        // 初始时左边为0
        this.rangeNbAvailableDriver =
                this.cplex.addLe(expr, instance.getDrivers().length, "DriverAvailability");
    }

    /**
     * 约束8: ρ_ij ≤ 1 (初始时左边为0)
     */
    private void createShortConnectionUpperBoundConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConTime = instance.getMinWaitingTime(i, j);
                    if (minConTime < instance.getShortConnectionTimeForDriver()) {
                        IloLinearNumExpr expr = this.cplex.linearNumExpr();
                        // 初始时左边为0
                        this.rangeConstraintUpperBoundWhetherSelectedShortConnection[i][j] =
                                this.cplex.addLe(expr, 1, "UpperBoundShortConnection_" + i + "_" + j);
                    }
                }
            }
        }
    }

    /**
     * 约束9: σ_i^t ≤ 1 (初始时左边为0)
     */
    private void createTripDepartureTimeUpperBoundConstraints() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                IloLinearNumExpr expr = this.cplex.linearNumExpr();
                // 初始时左边为0
                this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime[i][t] =
                        this.cplex.addLe(expr, 1, "UpperBoundTripDepartureTime_" + i + "_" + t);
            }
        }
    }

    /**
     * 使用 IloColumn 方式添加所有变量
     */
    private void addAllVariablesUsingColumn() throws IloException {
        // 1. 添加所有 σ_i^t 变量
        addAllSigmaVariables();

        // 2. 添加所有 ρ_ij 变量
        addAllRhoVariables();

        // 3. 添加所有初始的 θ_k 变量
        addAllInitialThetaVariables();
    }

    /**
     * 添加所有 σ_i^t 变量
     */
    private void addAllSigmaVariables() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;

                // 创建 column，从目标函数开始（系数为0）
                IloColumn column = this.cplex.column(objective, 0.0);

                // 约束1: 每个trip选择一个出发时间
                column = column.and(cplex.column(this.rangeConstraintTripSelectOneDepartureTime[i], 1.0));

                // 约束2: 链接schedule和出发时间（系数为 -|D|）
                column = column.and(cplex.column(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t],
                        -instance.getMaxNbDriverAvailable()));

                // 约束9: σ_i^t 上界约束
                column = column.and(cplex.column(this.rangeConstraintUpperBoundWhetherTripSelectDepartureTime[i][t], 1.0));

                // 创建变量
                this.varTripSelectDepartureTime[i][t] =
                        cplex.numVar(column, 0, Double.MAX_VALUE, "sigma_" + i + "_" + t);
            }
        }
    }

    /**
     * 添加所有 ρ_ij 变量
     */
    private void addAllRhoVariables() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConTime = instance.getMinWaitingTime(i, j);
                    if (minConTime < instance.getShortConnectionTimeForDriver()) {

                        // 创建 column，从目标函数开始（系数为0）
                        IloColumn column = this.cplex.column(objective, 0.0);

                        // 约束4: 短连接出约束
                        column = column.and(cplex.column(this.rangeConstraintShortConnectionOut[i], 1.0));

                        // 约束5: 短连接入约束
                        column = column.and(cplex.column(this.rangeConstraintShortConnectionIn[j], 1.0));

                        // 约束6: 链接短连接约束（系数为 -|D|）
                        column = column.and(cplex.column(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j],
                                -instance.getMaxNbDriverAvailable()));

                        // 约束8: ρ_ij 上界约束
                        column = column.and(cplex.column(this.rangeConstraintUpperBoundWhetherSelectedShortConnection[i][j], 1.0));

                        // 创建变量
                        this.varShortConnectionIsPerformed[i][j] =
                                cplex.numVar(column, 0, Double.MAX_VALUE, "rho_" + i + "_" + j);
                    }
                }
            }
        }
    }

    /**
     * 添加所有初始的 θ_k 变量
     */
    private void addAllInitialThetaVariables() throws IloException {
        for (int k = 0; k < this.driverSchedules.size(); k++) {
            DriverSchedule schedule = this.driverSchedules.get(k);
            addSingleThetaVariable(schedule);
        }
    }


    /**
     * 核心方法：添加单个 θ_k 变量
     * 这个方法在迭代过程中也会被复用
     */
    private void addSingleThetaVariable(DriverSchedule schedule) throws IloException {
        // 1. 从目标函数开始创建column
        IloColumn column = this.cplex.column(objective, schedule.getCostC());

        // 2. 为相关约束添加系数

        // 约束2: 链接schedule和出发时间
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                int coEfficient_i_t = schedule.getCoefficientF(i, t);
                if (coEfficient_i_t > 1e-6) {
                    column = column.and(cplex.column(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t], coEfficient_i_t));
                }
            }
        }

        // 约束3: 每个trip一个驾驶员
        for (int i = 0; i < instance.getNbTrips(); i++) {
            if (schedule.getCoefficientA(i) != 0) {
                column = column.and(cplex.column(this.rangeConstraintOneDriving[i], schedule.getCoefficientG(i)));
            }
        }

        // 约束6: 链接短连接
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConnection = instance.getMinWaitingTime(i, j);
                    if (minConnection < instance.getShortConnectionTimeForDriver()) {
                        double coeff = schedule.getCoefficientForShortConnectionB(i, j);
                        if (Math.abs(coeff) > 1e-6) {
                            column = column.and(cplex.column(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j], coeff));
                        }
                    }
                }
            }
        }

        // 约束7: 驾驶员数量限制
        column = column.and(cplex.column(this.rangeNbAvailableDriver, 1));

        // 3. 创建变量
        IloNumVar thetaVar = cplex.numVar(column, 0, Double.MAX_VALUE, "theta_" + this.varWhetherSelectTheSchedule.size());
        this.varWhetherSelectTheSchedule.add(thetaVar);
    }


//**************************************************************************************8888

    /**
     * 收集一个schedule在所有约束中的系数
     */
    private void collectConstraintCoefficients(DriverSchedule schedule,
                                               ArrayList<IloRange> constraints,
                                               ArrayList<Double> coefficients) {

        // 约束2：Link schedule和departure time
        for (int i = 0; i < instance.getNbTrips(); i++) {
            int EDepTime = instance.getTrip(i).getEarliestDepartureTime();
            int LDepTime = instance.getTrip(i).getLatestDepartureTime();
            int timeUnit = instance.getTimeSlotUnit();
            int nbUE = (int) Math.round(EDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(LDepTime /(double) timeUnit);
            for (int n = nbUE; n <=nbUL; n++) {
                int t = n * timeUnit;
                double coeff = schedule.getCoefficientF(i, t);
                if (Math.abs(coeff) > 1e-6) {
                    constraints.add(this.rangeConstraintLinkSelectScheduleAndSelectDepartureTime[i][t]);
                    coefficients.add(coeff);
                }
            }
        }

        // 约束3：One driving per trip
        for (int i = 0; i < instance.getNbTrips(); i++) {
            double coeff = schedule.getCoefficientG(i);
            if (Math.abs(coeff) > 1e-6) {
                constraints.add(this.rangeConstraintOneDriving[i]);
                coefficients.add(coeff);
            }
        }

        // 约束6：Link short connection
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minConnection = instance.getMinWaitingTime(i, j);
                    if (minConnection < instance.getShortConnectionTimeForDriver()) {
                        double coeff = schedule.getCoefficientForShortConnectionB(i, j);
                        if (Math.abs(coeff) > 1e-6) {
                            constraints.add(this.rangeConstraintLinkShortConnectionAndSelectedSchedule[i][j]);
                            coefficients.add(coeff);
                        }
                    }
                }
            }
        }

        // 约束7：Driver availability
        constraints.add(this.rangeNbAvailableDriver);
        coefficients.add(1.0);
    }

    //20250729 优化2
    private void precomputeTimeSlots() {
        int timeUnit = instance.getTimeSlotUnit();
        precomputedTimeSlots = new int[instance.getNbTrips()][];

        System.out.println("Precomputing time slots for " + instance.getNbTrips() + " trips...");

        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();

            int nbUE = (int) Math.round(eDepTime / (double)timeUnit);
            int nbUL = (int) Math.round(lDepTime / (double)timeUnit);

            // 预计算这个trip的所有时间槽
            int slotCount = nbUL - nbUE + 1;
            precomputedTimeSlots[i] = new int[slotCount];

            // 修复：使用正确的数组索引
            for (int n = nbUE; n <= nbUL; n++) { // 注意这里改为 <= nbUL
                int arrayIndex = n - nbUE; // 将时间槽索引转换为数组索引
                precomputedTimeSlots[i][arrayIndex] = n * timeUnit;
            }

            // 调试输出
            if (eDepTime == 0) {
                System.out.println("Trip " + i + " 包含时间0，时间槽数量: " + slotCount);
            }
        }

        System.out.println("Time slots precomputation completed.");
    }





    public static void main(String[] args) throws IloException, IOException {
        //InstanceReader reader=new InstanceReader("inst_ance basic information.txt");
        //  InstanceReader reader = new InstanceReader("instance_02_size90_6Trip.txt");//test vehicle example
        //InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips120_combPer0.0_TW6.txt");//
       // InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips100_combPer0.25_TW3.txt");//这个在second phase infeasible 3-index
         InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips080_combPer0.0_TW6.txt");//这个在second phase infeasible 3-index
        /// InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips150_combPer0.0_TW3.txt");

       // InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips040_combPer0.25_TW5.txt");
        //inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1
        //InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips080_combPer0.0_TW6.txt");
        //  InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips030_combPer0.25_TW5.txt");

        // InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1.txt");
        // InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW6.txt");
        Instance instance = reader.readFile(); //this will  read the file
        System.out.println(instance);
        System.out.println(instance.getTimeWindowRange());


        Instance instance1 = reader.readFile();


        System.out.println("instance1" + instance1);

        // SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_instance_02_size90_6Trip.txt", instance);
         //SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips120_combPer0.0_TW6.txt", instance);
       // SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips100_combPer0.25_TW3.txt", instance);
          SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips080_combPer0.0_TW6.txt", instance);
        //  SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips150_combPer0.0_TW3.txt", instance);

        //SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips080_combPer0.0_TW6.txt", instance);
        //  SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips040_combPer0.25_TW5.txt", instance);
        // SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips030_combPer0.25_TW5.txt",instance);
//        feaSol_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1
        // SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1.txt",instance);
        // SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips025_combPer0.25_TW6.txt", instance);
        //Solution initialSchedules = schedulesReader1.readFile();
//        System.out.println(initialSchedules);
//        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());
        //    SchedulesReader schedulesReader1 = new SchedulesReader("feas_basic.txt", instance1);
        Solution initialSchedules = schedulesReader1.readFile();
        System.out.println("inti" + initialSchedules);

        // MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);
        //masterProblem.solveRMPWithCplex();//这一步仅仅是看RMP的求解情况


        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules, true, false, false, false, 20);

        // MasterProblem masterProblem = new MasterProblem(instance, initialSchedules, false, true, false, true, 10);

        Solution solution = masterProblem.solve();//这一步求解的是最终迭代完之后的最终结果。
        //solution.printDriverSchedulingSolutionInFile("scheduleSolution_inbCity03_Size90_Day1_nbTrips120_combPer0.0TW6.txt");
        //solution.printDriverSchedulingSolutionInFile("scheduleSolution_inbCity03_Size90_Day1_nbTrips100_combPer0.25_TW3.txt");
        solution.printDriverSchedulingSolutionInFile("scheduleSolution_inbCity03_Size90_Day1_nbTrips080_combPer0.0_TW6.txt");
        //solution.printDriverSchedulingSolutionInFile("scheduleSolution_inbCity03_Size90_Day1_nbTrips150_combPer0.0_TW3.txt");
        // 注意这里的结果尽管目标函数值都是95，
        // 但是Pattern的样式确与Cplex求解的结果sol.text 中的不一样
        System.out.println(masterProblem);
//        System.out.println("whether solution is feasible: " + solution.whetherSolutionIsFeasible());
        System.out.println("final label generates: " + masterProblem.getFinalNbLabelsLastIteration());// 2025.4.4
        System.out.println("initial columns: " + initialSchedules.getDriverSchedules().size());
        System.out.println("final masterProblem objective Value: " + masterProblem.getFinalMasterObjectiveValue());
        System.out.println("final integer objective value: " + masterProblem.getFinalIntegerValueOfMasterObjective());

        System.out.println("nbOfIteration:" + masterProblem.getFinalNbIterations());
        System.out.println("final nbColumn:" + masterProblem.getFinalColumns());
        System.out.println("final lower bound: " + masterProblem.getFinalLowerBoundOfMP());

        System.out.println("time in sec " + masterProblem.getFinalTimeCostInSec() + " sec");

        System.out.println("gap: " + masterProblem.getPercentageGapBetweenFinalIntegerValueOfMasterProblemWithFinalLagrangianLowerBound() + "%");
        // System.out.println("total time in milli second of cplex solving pricing problem: " + masterProblem.getTotalTimeCostForSolvingSubProblemByCplex() + " milli seconds ");
        System.out.println("total time in milli second of labeling algorithm solving pricing problem: " + masterProblem.getTotalTimeCostForSolvingSubProblemByLabeling() + " milli seconds ");
        System.out.println("total labeling time in milli sec: " + masterProblem.getTotalTimeOfLabeling());
        System.out.println("time in sec for post-process: " + masterProblem.getTimeForPostProcessOfRichScheduleByDepartureTimeInSec());
        System.out.println("time in sec for creating model: "+masterProblem.getTimeForCreateModelInSec());
        System.out.println("total time in milli second of solve RMP by cplex:" + masterProblem.getTotalTimeOfSolRMPByCplex());
        System.out.println("total time of solving last integer problem by cplex:" + masterProblem.getTimeForSolvingLastIntegerProblemInSec());
        System.out.println("total label generated is :" + masterProblem.getTotalLabelsGenerateForAllIterations());
        System.out.println("time unite" + instance.getTimeSlotUnit());
        System.out.println(System.getProperty("java.home"));
        System.out.println(System.getProperty("java.version"));


    }

}