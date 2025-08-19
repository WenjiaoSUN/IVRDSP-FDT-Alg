
package ColumnGe;


import Solution.DriverSchedule;
import Solution.SchedulesReader;
import Solution.Solution;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import Instance.Instance;
import Instance.*;

import java.io.IOException;

public class SubProblemSolvedByCplex {
    private Instance instance;
    private double[] dualSolutionOfRMPConstraintSelectOneTripDepartureTime;//lambda
    private double[][] dualSolutionOfRMPConstraintLinkSelectScheduleAndSelectDepartureTime;// delta
    private double[] dualSolutionOfRMPConstraintOneDriverDriving;//beta
    private double[] dualSolutionOfRMPConstraintShortConnectionOut;//xi
    private double[] dualSolutionOfRMPConstraintShortConnectionIn;//eta
    private double[][] dualSolutionOfRMPConstraintLinkShortConnection;//zeta
    private double dualSolutionOfRMPConstraintNbAvailableDriver;//gamma

    private double[][] dualSolutionOfRMPConstraintUpperBoundOfSelectDepartureTime;//mu
    private MasterProblem masterProblem;
    private IloCplex cplex;
    private IloNumVar varSourceDepot[];
    private IloNumVar varDepotSink[];
    private IloNumVar varDriverArc[][];
    private IloNumVar varDrivingStatusTrip[];
    private IloNumVar varChangeOver[][];
    private IloNumVar varIdleTime[][];//2025.1.7
    private IloNumVar varWhetherIsDriverShortConnection[][];//2025.1.7


//    private IloNumVar varWhetherGreaterThanMinP[][];//20245.2.5
//    private IloNumVar varWhetherLessThanDriverShort[][];//20245.2.5

    private IloNumVar varTaskDepartureTime[];//2025.1.7
    private IloNumVar varScheduleStartTime;//2025.1.7
    private IloNumVar varScheduleEndTime;//2025.1.7

    private IloNumVar[][] varWhetherIsTheStartTime;

    private final double eps = 0.00001;


    public SubProblemSolvedByCplex(Instance instance, MasterProblem masterProblem) {
        this.instance = instance;
        this.masterProblem = masterProblem;
    }

    public void initializeModelForSubProblem() throws IloException {

        this.cplex = new IloCplex();
        defineDecisionVariables();// change 2025.1.9
        defineObjectiveFunction();// here should be modified only define the initial objective function


        cnstOneDepotIsUsed();
        cnstSameStartingAndEndingDepot();
        cnstFlowOfStartingDepot();
        cnstFlowOfEndingDepot();
        cnstFlowForEachTrip();
        cnstMaxDrivingTime();//2025.1.9
        cnstLinkDrivingStatusAndDriveArc();//2025.1.9

        cnstChangeOver1();
        cnstChangeOver2();

        cnstIdleTimePerformArc1();//add 2025.1.9
        cnstIdleTimePerformArc2();//add 2025.1.9
        cnstIdleTimeNotPerformArc1();//add 2025.1.9
        cnstIdleTimeNotPerformArc2();//add 2025.1.9


//        cnstDefineShortConnection();//add 2025.1.9// remove because it couldnt detect the time= T^{DS}的情况
//        cnstDefineShortConnection2();// add 2025.2.5

//        cnstWhetherGreaterThanMinPlan1();//2025.2.5
//        cnstWhetherGreaterThanMinPlan2();//2025.2.5
//        cnstWhetherLessThanDriverShort1();//2025.2.5
//        cnstWhetherLessThanDriverShort2();//2025.2.5
//
//        cnstWhetherIsShortLinkWhetherGreaterThanMinPlan();//2025.2.5
//        cnstWhetherIsShortLinkWhetherLessThanShortDriver();//2025.2.5
//        cnstWhetherIsShortLinkMinAndShortDriver();//2025.2.5


        cnstDepartureTimeEarlest();//add 2025.1.9
        cnstDepartureTimeLatest();//add 2025.1.9
        cnstMaxWorkingTime();//add 2025.1.9
        cnstLinkDepartureTimeTripWithStartingTimeSchedule();//add 2025.1.9
        cnstLinkDepartureTimeTripWithEndingTimeSchedule();//add 2025.1.9

        cnstAtLeastOneTripIsDrivingStatus();

        cnstStartExcatlyTime1();//2025.1.21
        cnstStartExcatlyTime2();//2025.1.21

    }

    public double getReducedCostOfSubProblemFromCplex() throws IloException {
        //here we don't use getObjectiveValues
        if (cplex.getStatus().equals(IloCplex.Status.Optimal)) {
            return this.cplex.getObjValue();

        } else {
            System.out.println("Pay attention now cplex not solved optimal"+cplex.getStatus());
            return this.cplex.getBestObjValue();
        }
    }

    // write the objective function will iterate each time  and use this.cplex.getObjective.setExpr(obj);

    public DriverSchedule solveSubProblemWithCplex() throws IloException {
        try {
            System.out.println("now we try to solve the subProblemWithCplex");//2025.1.10
            setObjectiveFunctionAccordingToMasterProblem();
            cplex.exportModel("subProblemOfDriverSchedule.lp");

            //solve
            this.cplex.setParam(IloCplex.Param.Threads, 1);

            if (cplex.getNMIPStarts() > 0) {
                cplex.deleteMIPStarts(0, cplex.getNMIPStarts());
            }
            //here is to avoid mip start, which will influence my result
            boolean solve = this.cplex.solve();
            System.out.println(solve);
            //print
            System.out.println("subProblem solving status: " + cplex.getStatus());
            System.out.println("SubProblem solved by Cplex obj (objective) = " + cplex.getObjValue());// print out the subProblem objective values
            System.out.println("subProblem solved by Cplex best obj= " + cplex.getBestObjValue());
            if (cplex.getObjValue() < cplex.getBestObjValue() - 1) {
                System.out.println("Here is something wrong, we need to compare the reduced cost with labeling to depend on which one to use.");
                // System.exit(0);
            }
            printOutSolution();// print the variables values

        } catch (IloException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return getOneBestDriverSchedule();
    }

    private void setObjectiveFunctionAccordingToMasterProblem() throws IloException {
        // get the dual solution from the master problem, (this part must be done after solving the master problem)
//        this.dualSolutionOfRMPConstraintNbDriver = this.masterProblem.getDualFromConstraintNbDriver();//lameda
        //check waiting for modify dual
        //2025.1.6
        this.dualSolutionOfRMPConstraintSelectOneTripDepartureTime = this.masterProblem.getDualFromConstraintTripSelectOneDepartureTime();//2025.1.6//lambda
        this.dualSolutionOfRMPConstraintLinkSelectScheduleAndSelectDepartureTime = this.masterProblem.getDualFromConstraintLinkScheduleSelectedAndDepartureTimeSelect();////2025.1.6
        //delt_i_t
        this.dualSolutionOfRMPConstraintOneDriverDriving = this.masterProblem.getDualFromConstraintOneDriving();//beta

        this.dualSolutionOfRMPConstraintShortConnectionOut = this.masterProblem.getDualFromConstraintShortConnectionOut();//xi
        this.dualSolutionOfRMPConstraintShortConnectionIn = this.masterProblem.getDualFromConstraintShortConnectionIn();//eta
        this.dualSolutionOfRMPConstraintLinkShortConnection = this.masterProblem.getDualFromConstraintLinkShortConnection();//zeta
        this.dualSolutionOfRMPConstraintNbAvailableDriver = this.masterProblem.getDualFromConstraintAvailableDriver();//gamma
        this.dualSolutionOfRMPConstraintUpperBoundOfSelectDepartureTime = this.masterProblem.getDualFromConstraintsUpperBoundTripSelectDepartureTime();// mu2025.1.6

        double fixedValueInObjective = instance.getFixedCostForDriver() - this.dualSolutionOfRMPConstraintNbAvailableDriver;
        IloLinearNumExpr obj = cplex.linearNumExpr();
        obj.setConstant(fixedValueInObjective);//add the constant to the objective
        // System.out.println("check subProblem fixed " + obj);

        //+ idle time cost
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    obj.addTerm(instance.getIdleTimeCostForDriverPerUnit(), this.varIdleTime[i][j]);
                }
            }
        }
        //+changeover cost
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    obj.addTerm(instance.getCostForChangeOver(), this.varChangeOver[i][j]);
                }
            }
        }


        //\sum\sum -\delta_i_t * x_i^t // modify 2025.1.21
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepTime = trip.getEarliestDepartureTime();
            int lDepTime = trip.getLatestDepartureTime();
            int idOfCityEnding = instance.getTrip(i).getIdOfEndCity();

            for (int t = eDepTime; t <= lDepTime; t++) {
                obj.addTerm(-this.dualSolutionOfRMPConstraintLinkSelectScheduleAndSelectDepartureTime[i][t], varWhetherIsTheStartTime[i][t]);
                double delta = this.dualSolutionOfRMPConstraintLinkSelectScheduleAndSelectDepartureTime[i][t];
                //System.out.println("Chek delta in cplex"+delta);
                if (delta > 0) {
                   // System.out.println("check cplex  dual delta i t (-)" + delta);
                    break;
                }
            }
        }


        //\sum\sum -\beta * x_i
        for (int i = 0; i < instance.getNbTrips(); i++) {
            obj.addTerm(-this.dualSolutionOfRMPConstraintOneDriverDriving[i], varDrivingStatusTrip[i]);
            double beta = this.dualSolutionOfRMPConstraintOneDriverDriving[i];
           // System.out.println("check cplex dual beta_i "+i+": "+beta);
        }

        //-\sum\sum shortConnection * \zeta_ij
        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            for (int j = 0; j < this.instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaiting = instance.getMinWaitingTime(i, j);
                    if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                        obj.addTerm(-this.dualSolutionOfRMPConstraintLinkShortConnection[i][j], varDriverArc[i][j]);
                        double zeta = this.dualSolutionOfRMPConstraintLinkShortConnection[i][j];
                       // System.out.println("check dual zeta in cplex "+zeta);
                        if (zeta > 0) {
                            System.out.println("check error dual zeta (-) in set obj " + zeta);
                            break;
                        }
                    }
                }
            }
        }
        // solve set
        this.cplex.getObjective().setExpr(obj);
    }

    public void cnstForceSchedule()throws IloException{
        int lb=1;
        this.varDrivingStatusTrip[14].setLB(lb);
        this.varDrivingStatusTrip[21].setLB(lb);
        this.varDriverArc[14][21].setLB(lb);
        this.varDrivingStatusTrip[32].setLB(lb);
        this.varDriverArc[21][32].setLB(lb);
        this.varDrivingStatusTrip[0].setLB(lb);
        this.varDriverArc[32][0].setLB(lb);
        this.varDriverArc[0][1].setLB(lb);
        this.varDrivingStatusTrip[1].setUB(0);
        this.varDrivingStatusTrip[19].setLB(lb);
        this.varDriverArc[1][19].setLB(lb);
    }

    private DriverSchedule getOneBestDriverSchedule() throws IloException {
        // here is try to build a new DriverSchedule according to this subProblem
        DriverSchedule driverScheduleFromSubP = new DriverSchedule(this.instance);
        //find the first trip
        int idOfFirstTrip = -1;
        for (int i = 0; i < instance.getNbTrips(); i++) {
            boolean whetherDriving = false;
            if (instance.whetherDriverCanStartWithTrip(i)) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    int idCityAsDepot = instance.getDepot(p).getIdOfCityAsDepot();
                    int idOfStartingCityOfTrip = instance.getTrip(i).getIdOfStartCity();
                    if (idCityAsDepot == idOfStartingCityOfTrip) {
                        int idOfIndexAsStarting = instance.getDepot(p).getIndexOfDepotAsStartingPoint();
                        int idOfIndexAsEnding = instance.getDepot(p).getIndexOfDepotAsEndingPoint();
                        if (cplex.getValue(this.varDriverArc[idOfIndexAsStarting][i]) > 0.99) {
                            //System.out.println("check first trip " + i + " value " + cplex.getValue(this.varDriverArc[idOfIndexAsStarting][i]));
                            driverScheduleFromSubP.setIdOfDepot(p);
                            driverScheduleFromSubP.setIndexDepotAsStartingPoint(idOfIndexAsStarting);
                            driverScheduleFromSubP.setIndexDepotAsEndingPoint(idOfIndexAsEnding);
                            idOfFirstTrip = i;
                            Trip firstTrip = instance.getTrip(i);
                            if (cplex.getValue(this.varDrivingStatusTrip[i]) > 0.99) {
                                whetherDriving = true;
                            }
                            int eDepT = instance.getTrip(i).getEarliestDepartureTime();
                            int lDepT = instance.getTrip(i).getLatestDepartureTime();
                            int depT = Integer.MAX_VALUE;

                            //
                            if ((int)  Math.round(cplex.getValue(this.varTaskDepartureTime[i]) )>= eDepT  && (int)  Math.round(cplex.getValue(this.varTaskDepartureTime[i]) )<= lDepT ) {
                                depT = (int) Math.round(cplex.getValue(this.varTaskDepartureTime[i]));
                            }else {
                                System.out.println("Error In subProblem depature time is:"+cplex.getValue(this.varTaskDepartureTime[i]));
                                System.out.println("but earlest time is: "+eDepT+" latest is:"+lDepT);
                            }

                            TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(firstTrip, whetherDriving, depT);
                            driverScheduleFromSubP.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTime);
                            break;
                        }

                    }

                }
            }
        }
        //now find the next trip
        int idOfCurrentTrip = idOfFirstTrip;
        //for(int j=0;j<instance.getNbTrips();j++){
        boolean findNextTrip = true;
        while (findNextTrip) {
            findNextTrip = false;
            for (int j = 0; j < instance.getNbTrips(); j++) {
                boolean whetherDrive = false;
                //System.out.println("check " + idOfCurrentTrip + " " + j);
                //System.out.println(instance.getConnectionTime(idOfCurrentTrip, j) <= instance.getMaxPlanTime());
                if (instance.whetherHavePossibleArcAfterCleaning(idOfCurrentTrip, j)) {
                    if (cplex.getValue(this.varDriverArc[idOfCurrentTrip][j]) > 0.9999) {
                        //System.out.println("check subProblem arc " + idOfCurrentTrip + " " + j);
                        Trip trip = instance.getTrip(j);
                        if (cplex.getValue(this.varDrivingStatusTrip[j]) > 0.9999) {
                            whetherDrive = true;
                        }
                        int depTime = Integer.MAX_VALUE;
                        int eDepT = trip.getEarliestDepartureTime();
                        int lDepT = trip.getLatestDepartureTime();
                        if ((int)  Math.round(cplex.getValue(this.varTaskDepartureTime[j])) >= eDepT  && (int)  Math.round(cplex.getValue(this.varTaskDepartureTime[j]) )<= lDepT ) {
                            depTime = (int)  Math.round(cplex.getValue(this.varTaskDepartureTime[j]));
                        }else {
                            System.out.println("Error In subProblem depature time is:"+cplex.getValue(this.varTaskDepartureTime[j]));
                            System.out.println("but earlest time is"+eDepT+" latest is "+lDepT);
                        }

                        TripWithWorkingStatusAndDepartureTime tripWithWorkingStatusAndDepartureTime = new TripWithWorkingStatusAndDepartureTime(trip, whetherDrive, depTime);
                        driverScheduleFromSubP.addTripWithWorkingStatusInSchedule(tripWithWorkingStatusAndDepartureTime);
                        idOfCurrentTrip = j;
                        findNextTrip = true;

                    }

                }
            }
        }
        System.out.println("best schedule solved from subProblem: " + driverScheduleFromSubP);
        return driverScheduleFromSubP;
    }

    private void printOutSolution() throws IloException {
        for (int p = 0; p < instance.getNbDepots(); p++) {
            if (cplex.getValue(this.varSourceDepot[p]) > 0.99) {
                // System.out.println("check subProblem source depot" + p);
            }
            if (cplex.getValue(this.varDepotSink[p]) > 0.99) {
                // System.out.println("check supProblem depot sink" + p);
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            if (cplex.getValue(this.varDrivingStatusTrip[i]) > 0.99) {
                // System.out.println("check subProblem solution driving status in trip "+i +" value:  "+ cplex.getValue(this.varDrivingStatusTrip[i]));
            }

        }
//        System.out.println("trip 19: "+cplex.getValue(this.varDrivingStatusTrip[19]));

        for (int i = 0; i < instance.getNbTrips(); i++) {
            if (instance.whetherDriverCanStartWithTrip(i)) {
                Trip startingTrip = instance.getTrip(i);
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    Depot depot = instance.getDepot(p);
                    int indexOfStartingDepot = depot.getIndexOfDepotAsStartingPoint();
                    if (depot.getIdOfCityAsDepot() == startingTrip.getIdOfStartCity()) {
                        if (cplex.getValue(this.varDriverArc[indexOfStartingDepot][i]) > 0.999) {
                            // System.out.println("check subProblem solution source index depot_"+depot.getIdOfDepot()+" trip_ " + i +"value of varDrivingArc "+cplex.getValue(this.varDriverArc[indexOfStartingDepot][i]) );
                        }
                    }
                }
            }

            if (instance.whetherDriverCanEndAtTrip(i)) {
                Trip endingTrip = instance.getTrip(i);
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    Depot depot = instance.getDepot(p);
                    int indexOfEndingDepot = depot.getIndexOfDepotAsEndingPoint();
                    if (depot.getIdOfCityAsDepot() == endingTrip.getIdOfEndCity()) {
                        if (cplex.getValue(this.varDriverArc[i][indexOfEndingDepot]) > 0.999) {
                            //   System.out.println("check subProblem trip_"+i+"sink_"+depot.getIdOfDepot()+"value of varDrivingArc" +cplex.getValue(this.varDriverArc[i][indexOfEndingDepot]));
                        }
                    }
                }
            }
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    if (cplex.getValue(this.varDriverArc[i][j]) > 0.999) {
                        //  System.out.println("check subProblem solution arc trip_" + i + "_" + j + " = " + cplex.getValue(this.varDriverArc[i][j]));
                    }
                }
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    if (cplex.getValue(this.varIdleTime[i][j]) > 0.99) {
                        //System.out.println("check subProblem solution idle time i_"+i+"_j_"+j+" time :"+cplex.getValue(this.varIdleTime[i][j]));
                    }
                }
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            if (cplex.getValue(this.varTaskDepartureTime[i]) >=0) {
                //   System.out.println("check subProblem solution departure t_i"+i+" value is :"+this.cplex.getValue(this.varTaskDepartureTime[i]));
            }
        }
    }

    private void cnstStartExcatlyTime1() throws IloException {// add 2025.1.21
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDep = trip.getEarliestDepartureTime();
            int lDep = trip.getLatestDepartureTime();
            for (int t = eDep; t <= lDep; t++) {
                IloLinearNumExpr whetherStartExact = cplex.linearNumExpr();
                whetherStartExact.addTerm(this.varTaskDepartureTime[i], 1);
                whetherStartExact.addTerm(this.varWhetherIsTheStartTime[i][t], lDep);
                IloRange cnstStarLeft = cplex.addLe(whetherStartExact, lDep + t);
            }
        }
    }

    private void cnstStartExcatlyTime2() throws IloException {// add 2025.1.21
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDep = trip.getEarliestDepartureTime();
            int lDep = trip.getLatestDepartureTime();
            for (int t = eDep; t <= lDep; t++) {
                IloLinearNumExpr whetherStartExact = cplex.linearNumExpr();
                whetherStartExact.addTerm(this.varTaskDepartureTime[i], -1);
                whetherStartExact.addTerm(this.varWhetherIsTheStartTime[i][t], lDep);
                IloRange cnstStarLeft = cplex.addLe(whetherStartExact, lDep - t);
            }
        }
    }

    private void cnstAtLeastOneTripIsDrivingStatus() throws IloException {
        //add constraint at least one trip the driver should be in driving status
        IloLinearNumExpr exprOneDriving = cplex.linearNumExpr();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            exprOneDriving.addTerm(this.varDrivingStatusTrip[i], 1);
        }
        IloRange constraintForAtLestOneTripShouldInDrivingStatus = cplex.addGe(exprOneDriving, 1, "totalNbDrivingTrip");
    }

    private void cnstOneDepotIsUsed() throws IloException {
        // add constraint only one depot is used on the solution
        IloLinearNumExpr exprNbDepot = cplex.linearNumExpr();
        for (int p = 0; p < instance.getNbDepots(); p++) {
            exprNbDepot.addTerm(this.varSourceDepot[p], 1);
        }
        IloRange constraintForTotalNbDepot = cplex.addEq(exprNbDepot, 1, "totalNbDepot");
    }

    private void cnstIdleTimePerformArc1() throws IloException {//2025.1.9  tij= t_j-(t_i+D_i)
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int M_l = instance.getEndingPlaningHorizon();
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(this.varIdleTime[i][j], -1);
                    expr.addTerm(this.varTaskDepartureTime[j], 1);
                    expr.addTerm(this.varTaskDepartureTime[i], -1);
                    expr.addTerm(this.varDriverArc[i][j], M_l);
                    int bound = M_l + instance.getTrip(i).getDuration();
                    IloRange constraintsIdleTimePerformedArcLeft = cplex.addLe(expr, bound, "idleTime_takeValue_left");
                }
            }
        }

    }


    private void cnstIdleTimePerformArc2() throws IloException {//2025.1.9  tij= t_j-(t_i+D_i)
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int M_r = instance.getEndingPlaningHorizon();
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(this.varIdleTime[i][j], -1);
                    expr.addTerm(this.varTaskDepartureTime[j], 1);
                    expr.addTerm(this.varTaskDepartureTime[i], -1);
                    expr.addTerm(this.varDriverArc[i][j], -M_r);
                    int bound = instance.getTrip(i).getDuration() - M_r;//M_right=maxWaiting
                    IloRange constraintsIdleTimePerformedArcRight = cplex.addGe(expr, bound, "idleTime_takeValue_right");

                }
            }
        }

    }

    private void cnstIdleTimeNotPerformArc1() throws IloException { //2025.1.9 tij = 0
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(this.varIdleTime[i][j], -1);
                    expr.addTerm(this.varDriverArc[i][j], instance.getMinPlanTurnTime());
                    IloRange constraintsIdleTimeNotPerformedArcLeft = cplex.addLe(expr, 0, "idleTime_take0_left");
                }
            }

        }

    }

    private void cnstIdleTimeNotPerformArc2() throws IloException { //2025.1.9 tij = 0
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int largestWaiting = instance.getTrip(j).getLatestDepartureTime()
                            - (instance.getTrip(i).getEarliestDepartureTime() + instance.getTrip(i).getDuration());
                    IloLinearNumExpr expr = cplex.linearNumExpr();
                    expr.addTerm(this.varIdleTime[i][j], -1);
                    expr.addTerm(this.varDriverArc[i][j], largestWaiting);
                    IloRange constraintsIdleTimeNotPerformedArcRight = cplex.addGe(expr, 0, "idleTime_take0_right");
                }
            }

        }

    }

    private void cnstDefineShortConnection() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(this.varIdleTime[i][j], -1);
                        expr.addTerm(this.varDriverArc[i][j], instance.getShortConnectionTimeForDriver());
                        expr.addTerm(this.varWhetherIsDriverShortConnection[i][j], instance.getMinPlanTurnTime() - instance.getShortConnectionTimeForDriver());
                        IloRange constraintsShortConnection = cplex.addLe(expr, 0, "idleTimeANDShortConnection");
                    }
                }
            }
        }
    }

//    private void cnstWhetherGreaterThanMinPlan1() throws IloException {// add 2025.2.5
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M = instance.getEndingPlaningHorizon();
//                    int minPlan = instance.getMinPlanTurnTime();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varIdleTime[i][j], -1);
//                        expr.addTerm(this.varWhetherGreaterThanMinP[i][j], M);
//                        expr.addTerm(this.varDriverArc[i][j], minPlan - 1);
//                        IloRange constraintsShortConnection = cplex.addGe(expr, 0, "idleTimeWhetherGreaterThanMinP1");
//                    }
//                }
//
//            }
//
//        }
//
//    }
//
//    private void cnstWhetherGreaterThanMinPlan2() throws IloException {
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M = instance.getEndingPlaningHorizon();
//                    int minPlan = instance.getMinPlanTurnTime();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varIdleTime[i][j], -1);
//                        expr.addTerm(this.varWhetherGreaterThanMinP[i][j], M);
//                        expr.addTerm(this.varDriverArc[i][j], minPlan);
//                        IloRange constraintsShortConnection = cplex.addLe(expr, M, "idleTimeWhetherGreaterThanMinP2");
//                    }
//                }
//
//            }
//
//        }
//
//    }
//
//
//    private void cnstWhetherLessThanDriverShort1() throws IloException {
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M = instance.getEndingPlaningHorizon();
//                    int TDS = instance.getShortConnectionTimeForDriver();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varIdleTime[i][j], -1);
//                        expr.addTerm(this.varWhetherLessThanDriverShort[i][j], -M);
//                        expr.addTerm(this.varDriverArc[i][j], TDS+1);
//                        IloRange constraintsShortConnection = cplex.addLe(expr, 0, "cnstWhetherLessThanDriverShort1");
//                    }
//                }
//
//            }
//
//        }
//
//    }
//
//    private void cnstWhetherLessThanDriverShort2() throws IloException {
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M = instance.getEndingPlaningHorizon();
//                    int TDS = instance.getShortConnectionTimeForDriver();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varIdleTime[i][j], -1);
//                        expr.addTerm(this.varWhetherLessThanDriverShort[i][j], -M);
//                        expr.addTerm(this.varDriverArc[i][j], TDS);
//                        IloRange constraintsShortConnection = cplex.addGe(expr, -M, "cnstWhetherLessThanDriverShort1");
//                    }
//                }
//
//            }
//
//        }
//
//    }
//
//    private void cnstWhetherIsShortLinkWhetherGreaterThanMinPlan() throws IloException {
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M = instance.getEndingPlaningHorizon();
//                    int TDS = instance.getShortConnectionTimeForDriver();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varWhetherGreaterThanMinP[i][j], -1);
//                        expr.addTerm(this.varWhetherIsDriverShortConnection[i][j], 1);
//                        IloRange constraintsShortConnection = cplex.addLe(expr,0, "cnstWhetherLessThanDriverShort1");
//                    }
//                }
//
//            }
//
//        }
//
//    }
//
//    private void cnstWhetherIsShortLinkWhetherLessThanShortDriver() throws IloException {
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M = instance.getEndingPlaningHorizon();
//                    int TDS = instance.getShortConnectionTimeForDriver();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varWhetherLessThanDriverShort[i][j], -1);
//                        expr.addTerm(this.varWhetherIsDriverShortConnection[i][j], 1);
//                        IloRange constraintsShortConnection = cplex.addLe(expr,0, "cnstWhetherLessThanDriverShort1");
//                    }
//                }
//
//            }
//
//        }
//
//    }
//
//    private void cnstWhetherIsShortLinkMinAndShortDriver() throws IloException {
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M = instance.getEndingPlaningHorizon();
//                    int TDS = instance.getShortConnectionTimeForDriver();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varWhetherLessThanDriverShort[i][j], -1);
//                        expr.addTerm(this.varWhetherGreaterThanMinP[i][j],-1);
//                        expr.addTerm(this.varWhetherIsDriverShortConnection[i][j], 1);
//                        IloRange constraintsShortConnection = cplex.addGe(expr,-1, "cnstWhetherLessThanDriverShort1");
//                    }
//                }
//
//            }
//
//        }
//
//    }


//    private void cnstDefineShortConnection2() throws IloException {
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaitingTime = instance.getMinWaitingTime(i, j);
//                    int M=instance.getEndingPlaningHorizon();
//                    if (minWaitingTime < instance.getShortConnectionTimeForDriver()) {
//                        IloLinearNumExpr expr = cplex.linearNumExpr();
//                        expr.addTerm(this.varIdleTime[i][j], -1);
//                        expr.addTerm(this.varWhetherIsDriverShortConnection[i][j], -M);
//                        IloRange constraintsShortConnection = cplex.addGe(expr, -M, "idleTimeANDShortConnection2");
//                    }
//                }
//
//            }
//
//        }
//    }


    private void cnstDepartureTimeEarlest() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            int eDepTime = instance.getTrip(i).getEarliestDepartureTime();
            expr.addTerm(this.varTaskDepartureTime[i], 1);
            IloRange constraintsDeparture = cplex.addGe(expr, eDepTime);
        }

    }

    private void cnstDepartureTimeLatest() throws IloException {
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            int lDepTime = instance.getTrip(i).getLatestDepartureTime();
            expr.addTerm(this.varTaskDepartureTime[i], 1);
            IloRange constraintsDeparture = cplex.addLe(expr, lDepTime);
        }
    }

    private void cnstChangeOver2() throws IloException {
        //constraint changeover2
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {//2025.1.9
                    IloLinearNumExpr exprChangeOver2 = cplex.linearNumExpr();
                    exprChangeOver2.addTerm(this.varDriverArc[i][j], 1);
                    exprChangeOver2.addTerm(this.varDrivingStatusTrip[j], 1);
                    exprChangeOver2.addTerm(this.varDrivingStatusTrip[i], -1);
                    exprChangeOver2.addTerm(this.varChangeOver[i][j], -1);
                    IloRange constraintForChangeOver = cplex.addLe(exprChangeOver2, 1, "changeover2" + i + j);
                }
            }
        }
    }

    private void cnstChangeOver1() throws IloException {
        //constraint changeover1
        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {//change 2025.1.9
                    IloLinearNumExpr exprChangeOver1 = cplex.linearNumExpr();
                    exprChangeOver1.addTerm(this.varDriverArc[i][j], 1);
                    exprChangeOver1.addTerm(this.varDrivingStatusTrip[i], 1);
                    exprChangeOver1.addTerm(this.varDrivingStatusTrip[j], -1);
                    exprChangeOver1.addTerm(this.varChangeOver[i][j], -1);
                    IloRange constraintForChangeOver = cplex.addLe(exprChangeOver1, 1, "changeover1 Trip" + i + j);
                }
            }
        }
    }

    private void cnstLinkDrivingStatusAndDriveArc() throws IloException {
        //  linking constraint driving status and drive arc
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr exprDriving = cplex.linearNumExpr();
            exprDriving.addTerm(this.varDrivingStatusTrip[i], 1);

            IloLinearNumExpr exprDriveArc = cplex.linearNumExpr();

            //end at trip i
            if (instance.whetherDriverCanEndAtTrip(i)) {
                for (int k = 0; k < instance.getNbDepots(); k++) {
                    Depot depot = instance.getDepot(k);
                    if (depot.getIdOfCityAsDepot() == instance.getTrip(i).getIdOfEndCity()) {
                        exprDriveArc.addTerm(this.varDriverArc[i][depot.getIndexOfDepotAsEndingPoint()], 1);
                    }
                }
            }
            //normal case
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    exprDriveArc.addTerm(this.varDriverArc[i][j], 1);
                }
            }
            IloConstraint constraintLink = cplex.addLe(exprDriving, exprDriveArc, "DrivingAndArcFor trip" + i);
        }
    }

    private void cnstMaxWorkingTime() throws IloException {//2025.1.9
        //maxWorking
        IloLinearNumExpr exprWorkingTime = cplex.linearNumExpr();
        exprWorkingTime.addTerm(this.varScheduleStartTime, -1);
        exprWorkingTime.addTerm(this.varScheduleEndTime, 1);
        IloRange constraintMaxWorking = cplex.addLe(exprWorkingTime, instance.getMaxWorkingTime(), "maxWorking");
    }

    private void cnstLinkDepartureTimeTripWithStartingTimeSchedule() throws IloException {//2025.1.9
        int M = instance.getEndingPlaningHorizon() + 1;
        for (int p = 0; p < instance.getNbDepots(); p++) {
            Depot depot = instance.getDepot(p);
            int indexAsStartingPoint = depot.getIndexOfDepotAsStartingPoint();
            for (int i = 0; i < instance.getNbTrips(); i++) {
                if (instance.whetherDriverCanStartWithTrip(i)) {
                    Trip trip = instance.getTrip(i);
                    if (trip.getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(this.varTaskDepartureTime[i], -1);
                        expr.addTerm(this.varScheduleStartTime, 1);
                        expr.addTerm(this.varDriverArc[indexAsStartingPoint][i], M);
                        IloRange constraintDepartureTripAndSchedule = cplex.addLe(expr, M, "departureTripAndStartingSchedule");
                    }

                }
            }
        }

    }

    private void cnstLinkDepartureTimeTripWithEndingTimeSchedule() throws IloException {
        int M = instance.getEndingPlaningHorizon() + 1;
        for (int p = 0; p < instance.getNbDepots(); p++) {
            Depot depot = instance.getDepot(p);
            int indexAsEndingPoint = depot.getIndexOfDepotAsEndingPoint();
            for (int i = 0; i < instance.getNbTrips(); i++) {
                Trip trip = instance.getTrip(i);
                int duration = trip.getDuration();
                if (instance.whetherDriverCanEndAtTrip(i)) {
                    if (trip.getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                        IloLinearNumExpr expr = cplex.linearNumExpr();
                        expr.addTerm(this.varTaskDepartureTime[i], 1);
                        expr.addTerm(this.varScheduleEndTime, -1);
                        expr.addTerm(this.varDriverArc[i][indexAsEndingPoint], M);
                        IloRange constraintDepartureTripAndSchedule = cplex.addLe(expr, M - duration, "departureTripAndEndingSchedule");
                    }
                }
            }
        }

    }

    private void cnstMaxDrivingTime() throws IloException {
        //Driving time limited Constraint
        IloLinearNumExpr expr = cplex.linearNumExpr();
        for (int i = 0; i < instance.getNbTrips(); i++) {
            double duration = instance.getTrip(i).getDuration();
            expr.addTerm(this.varDrivingStatusTrip[i], duration);
        }
        IloRange constraint = cplex.addLe(expr, instance.getMaxDrivingTime(), "maxDriving");
    }

    private void cnstFlowForEachTrip() throws IloException {
        //constraint for flow for trip
        for (int i = 0; i < instance.getNbTrips(); i++) {
            IloLinearNumExpr exprFlowEnter = cplex.linearNumExpr();
            IloLinearNumExpr exprFlowOut = cplex.linearNumExpr();
            //first case:Start with i
            if (instance.whetherDriverCanStartWithTrip(i)) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    Depot depot = instance.getDepot(p);
                    if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                        exprFlowEnter.addTerm(this.varDriverArc[depot.getIndexOfDepotAsStartingPoint()][i], 1);
                    }

                }
            }
            //second case: Trip i is the last trip for ending
            if (instance.whetherDriverCanEndAtTrip(i)) {
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    Depot depot = instance.getDepot(p);
                    if (instance.getTrip(i).getIdOfEndCity() == depot.getIdOfCityAsDepot()) {
                        exprFlowOut.addTerm(this.varDriverArc[i][depot.getIndexOfDepotAsEndingPoint()], 1);
                    }
                }
            }
            //third case: trip i is in the middle has former and later trip  j
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    exprFlowOut.addTerm(this.varDriverArc[i][j], 1);
                }
                if (instance.whetherHavePossibleArcAfterCleaning(j, i)) {
                    exprFlowEnter.addTerm(this.varDriverArc[j][i], 1);
                }
            }
            IloConstraint constraintEndDepotAndEndArc = cplex.addEq(exprFlowEnter, exprFlowOut, "FlowTrip" + i);
        }
    }

    private void cnstFlowOfEndingDepot() throws IloException {
        //            //constraint end depot and ending trip
        for (int p = 0; p < instance.getNbDepots(); p++) {
            Depot depot = instance.getDepot(p);
            IloLinearNumExpr exprDepotSink = cplex.linearNumExpr();
            exprDepotSink.addTerm(this.varDepotSink[p], 1);

            IloLinearNumExpr exprTripDepot = cplex.linearNumExpr();
            for (int i = 0; i < instance.getNbTrips(); i++) {
                if (instance.whetherDriverCanEndAtTrip(i)) {
                    if (depot.getIdOfCityAsDepot() == instance.getTrip(i).getIdOfEndCity()) {
                        int endDepotIndex = this.instance.getDepot(p).getIndexOfDepotAsEndingPoint();
                        exprTripDepot.addTerm(this.varDriverArc[i][endDepotIndex], 1);
                    }
                }
            }
            IloConstraint constraintEndDepotAndEndArc = cplex.addEq(exprDepotSink, exprTripDepot, "FlowConstraint end depot" + p);
        }
    }

    private void cnstFlowOfStartingDepot() throws IloException {
        //constraint start depot and starting trip
        for (int p = 0; p < instance.getNbDepots(); p++) {
            Depot depot = instance.getDepot(p);
            IloLinearNumExpr exprSourceDepot = cplex.linearNumExpr();
            IloLinearNumExpr exprDepotTrip = cplex.linearNumExpr();
            exprSourceDepot.addTerm(this.varSourceDepot[p], 1);
            for (int i = 0; i < instance.getNbTrips(); i++) {
                if (instance.whetherDriverCanStartWithTrip(i)) {
                    if (instance.getTrip(i).getIdOfStartCity() == depot.getIdOfCityAsDepot()) {
                        int startDepotIndex = this.instance.getDepot(p).getIndexOfDepotAsStartingPoint();
                        exprDepotTrip.addTerm(this.varDriverArc[startDepotIndex][i], 1);
                    }
                }
            }
            IloConstraint constraintStartDepotAndStartArc = cplex.addEq(exprSourceDepot, exprDepotTrip, "DepotFlow" + p);
        }
    }

    private void cnstSameStartingAndEndingDepot() throws IloException {
        //constraint same start and end depot
        for (int p = 0; p < this.instance.getNbDepots(); p++) {
            IloLinearNumExpr exprSource = cplex.linearNumExpr();
            IloLinearNumExpr exprSink = cplex.linearNumExpr();
            exprSource.addTerm(this.varSourceDepot[p], 1);
            exprSink.addTerm(this.varDepotSink[p], 1);
            IloConstraint constraintSameStartEndDepot = cplex.addEq(exprSource, exprSink, p + " So&Si");
        }
    }

    //Object function \sum(c_ij^2*x_ij)+\sum(c_ij^3 * y_ij)-\sum(\lameda_i ^ \sum x_ij) +.....+ M^2+  \Gamma
    private void defineObjectiveFunction() throws IloException {
        double fixedValueInObjective = instance.getFixedCostForDriver() - this.dualSolutionOfRMPConstraintNbAvailableDriver;// fixed cost+ gamma
//        System.out.println("Check gamma in cplex "+this.dualSolutionOfRMPConstraintNbAvailableDriver);
        IloLinearNumExpr obj = cplex.linearNumExpr();
        obj.setConstant(fixedValueInObjective);//add the constant to the objective

        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            for (int j = 0; j < this.instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    obj.addTerm(instance.getIdleTimeCostForDriverPerUnit(), this.varIdleTime[i][j]);//idle time cost
                }
            }
        }
        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            for (int j = 0; j < this.instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    obj.addTerm(instance.getCostForChangeOver(), this.varChangeOver[i][j]);// changeoverCost
                }
            }
        }

        //\sum\sum -\deta_i^t * x_i_t    2025.2.12
        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepT = trip.getEarliestDepartureTime();
            int lDepT = trip.getLatestDepartureTime();
            if (instance.whetherDriverCanEndAtTrip(i)) {
                int idOfCityEnding = instance.getTrip(i).getIdOfEndCity();
                for (int p = 0; p < instance.getNbDepots(); p++) {
                    Depot depot = instance.getDepot(p);
                    if (depot.getIdOfCityAsDepot() == idOfCityEnding) {
                        for (int t = eDepT; t <= lDepT; t++) {
                            obj.addTerm(0, varWhetherIsTheStartTime[i][t]);//change 2025.2.12
                        }
                    }
                }
            }

            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    for (int t = eDepT; t <= lDepT; t++) {
                        obj.addTerm(0, varWhetherIsTheStartTime[i][t]); //modify the dual of masterProblem coefficient 0 for initialize
                    }
                }
            }
        }

        //\sum\sum -\beta * x_i
        for (int i = 0; i < instance.getNbTrips(); i++) {
            obj.addTerm(0, varDrivingStatusTrip[i]); //modify the dual of masterProblem coefficient 0 for initialize
        }
        //-\sum\sum  \zeta_ij  * b_ij
        for (int i = 0; i < this.instance.getNbTrips(); i++) {
            for (int j = 0; j < this.instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaitingTime = instance.getMinWaitingTime(i, j);
                    if (minWaitingTime <instance.getShortConnectionTimeForDriver()) {
                        obj.addTerm(0, varWhetherIsDriverShortConnection[i][j]);//2025.1.10 for initialize
                    }
                }
            }
        }
        IloObjective objective = cplex.addMinimize(obj);
    }
    private void defineDecisionVariables() throws IloException {
        //define variable
        this.varSourceDepot = new IloNumVar[this.instance.getNbDepots()];
        this.varDepotSink = new IloNumVar[this.instance.getNbDepots()];
        this.varDriverArc = new IloNumVar[this.instance.getNbTrips() + 2 * this.instance.getNbDepots()][this.instance.getNbTrips() + 2 * this.instance.getNbDepots()];
        this.varDrivingStatusTrip = new IloNumVar[this.instance.getNbTrips()];
        this.varChangeOver = new IloNumVar[this.instance.getNbTrips()][this.instance.getNbTrips()];

        this.varIdleTime = new IloNumVar[this.instance.getNbTrips()][this.instance.getNbTrips()];// t_ij 2025.1.9
        this.varWhetherIsDriverShortConnection = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];// bij 2025.1.9

//        this.varWhetherGreaterThanMinP = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];// bij_{DS1} 2025.2.5
//        this.varWhetherLessThanDriverShort = new IloNumVar[instance.getNbTrips()][instance.getNbTrips()];// bij_{DS1} 2025.2.5

        this.varTaskDepartureTime = new IloNumVar[instance.getNbTrips()];// t_i 2025.1.9
        this.varScheduleStartTime = this.cplex.numVar(0, instance.getEndingPlaningHorizon(), "startingTime");
        this.varScheduleEndTime = this.cplex.numVar(0, instance.getEndingPlaningHorizon(), "endingTime");

        this.varWhetherIsTheStartTime = new IloNumVar[this.instance.getNbTrips()][this.instance.getEndingPlaningHorizon()];


        //the range of decision variable
        for (int p = 0; p < instance.getNbDepots(); p++) {
            this.varSourceDepot[p] = this.cplex.boolVar("x_source_" + p);
        }
        for (int p = 0; p < instance.getNbDepots(); p++) {
            this.varDepotSink[p] = this.cplex.boolVar("x_" + p + "_sink");
        }
        for (int i = 0; i < 2 * instance.getNbDepots() + instance.getNbTrips(); i++) {
            for (int j = 0; j < 2 * instance.getNbDepots() + instance.getNbTrips(); j++) {
                this.varDriverArc[i][j] = this.cplex.boolVar("x_" + i + "_" + j);
            }
        }
        for (int i = 0; i < instance.getNbTrips(); i++) {
            this.varDrivingStatusTrip[i] = this.cplex.boolVar("o" + "_" + i);
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                this.varChangeOver[i][j] = this.cplex.boolVar("y" + "_" + i + "_" + j);
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaiting = instance.getMinWaitingTime(i, j);
                    int maxWaiting = instance.getMaxWaitingTime(i, j);
                    //maxWaiting
                    this.varIdleTime[i][j] = this.cplex.intVar(0, Integer.MAX_VALUE, "t_" + i + "_" + j);//2025.1.10// shouldnt put the minWaiting as lower bound, becuase when there is no arc, it forces the idle time equal to 0
                }
            }
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            for (int j = 0; j < instance.getNbTrips(); j++) {
                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
                    int minWaiting = instance.getMinWaitingTime(i, j);
                    if (minWaiting < instance.getShortConnectionTimeForDriver()) {
                        this.varWhetherIsDriverShortConnection[i][j] = this.cplex.boolVar("b" + "_" + i + "_" + j);//2025.1.10
                    }
                }
            }
        }

//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaiting = instance.getMinWaitingTime(i, j);
//                    if (minWaiting <instance.getShortConnectionTimeForDriver()) {
//                        this.varWhetherGreaterThanMinP[i][j] = this.cplex.boolVar("b1" + "_" + i + "_" + j);//2025.1.10
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < instance.getNbTrips(); i++) {
//            for (int j = 0; j < instance.getNbTrips(); j++) {
//                if (instance.whetherHavePossibleArcAfterCleaning(i, j)) {
//                    int minWaiting = instance.getMinWaitingTime(i, j);
//                    if (minWaiting < instance.getShortConnectionTimeForDriver()) {
//                        this.varWhetherLessThanDriverShort[i][j] = this.cplex.boolVar("b2" + "_" + i + "_" + j);//2025.1.10
//                    }
//                }
//            }
//        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepT = trip.getEarliestDepartureTime();
            int lDepT = trip.getLatestDepartureTime();
            this.varTaskDepartureTime[i] = this.cplex.intVar(eDepT, lDepT, "t_i" + i);//2025.1.10
        }

        for (int i = 0; i < instance.getNbTrips(); i++) {
            Trip trip = instance.getTrip(i);
            int eDepT = trip.getEarliestDepartureTime();
            int lDepT = trip.getLatestDepartureTime();
            for (int t = eDepT; t <= lDepT; t++) {
                this.varWhetherIsTheStartTime[i][t] = this.cplex.boolVar("x_" + i + "_" + t);
            }
        }

    }

    @Override
    public String toString() {
        return "SubProblemSolvedByCplex{" +
                "instance=" + instance +
                ", MasterProblem=" + masterProblem +
                '}';
    }

    public static void main(String[] args) throws IloException, IOException {
//     3.  In your test, you should ensure that the following operations are done in that order :
//        - define and initialize a master problem
//        - define and initialize a sub problem
//        - solve the RMP of the master problem (if you do not solve it, there will not be any dual ...)
//        - then solve the sub problem (at that time the dual values will exist)
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);

        SchedulesReader schedulesReader = new SchedulesReader("scheduleSolution_inst_nbCity20_Size300_Day3_nbTrips300_combPer0.1.txt", instance);
        Solution initialSchedules = schedulesReader.readFile();

        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);

        // MasterProblem masterProblem = new MasterProblem(instance);//--- define and initialize a master problem
        SubProblemSolvedByCplex subProblemSolvedByCplex = new SubProblemSolvedByCplex(instance, masterProblem);//--- define and initialize a sub problem

        masterProblem.solveRMPWithCplex();//--- solve the RMP

        //masterProblem.getDualSolutionOfRMP();
        subProblemSolvedByCplex.solveSubProblemWithCplex();//--- solve the subP

    }


}
