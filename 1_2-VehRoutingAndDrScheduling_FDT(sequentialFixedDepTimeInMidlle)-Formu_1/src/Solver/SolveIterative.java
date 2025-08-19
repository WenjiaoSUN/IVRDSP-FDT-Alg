package Solver;

import Instance.Instance;
import Instance.Trip;
import Instance.InstanceReader;
import Solution.Solution;
import Solution.TripWithStartingInfos;
import ilog.concert.*;

import java.util.ArrayList;

//
//
public class SolveIterative {
    private Instance instance;

    private SolveWithChooseThreeDepartureTime solveWithChooseThreeDepartureTime;
    private SolveWithFixedDepartureTime solveWithFixedDepartureTime;
    private SolveWithFixedArc solveWithFixedArc;

    private boolean whetherSolveRelaxationProblem;

    private String warmStartFilePath;

    private double timeInMilliSec;

    private double upperBound = Double.MAX_VALUE;

    private double lowerBound = 0;

    private int nbIteration = 0;

    public SolveIterative(Instance instance, Boolean whetherSolveRelaxationProblem) throws IloException {
        this.instance = instance;
        this.solveWithChooseThreeDepartureTime = new SolveWithChooseThreeDepartureTime(this.instance);
        this.solveWithFixedDepartureTime = new SolveWithFixedDepartureTime(this.instance);
        this.solveWithFixedArc = new SolveWithFixedArc(this.instance);
        this.whetherSolveRelaxationProblem = whetherSolveRelaxationProblem;
        this.warmStartFilePath = null;
    }

    public SolveIterative(Instance instance, String warmStartFilePath, Boolean whetherSolveRelaxation) throws IloException {
        this.instance = instance;
        this.solveWithChooseThreeDepartureTime = new SolveWithChooseThreeDepartureTime(this.instance);
        this.solveWithFixedDepartureTime = new SolveWithFixedDepartureTime(this.instance);
        this.solveWithFixedArc = new SolveWithFixedArc(this.instance);
        this.whetherSolveRelaxationProblem = whetherSolveRelaxation;
        this.warmStartFilePath = warmStartFilePath;
    }


    public Solution solveIterativeByCplex_WithChooseOneDepTimeAmongThreeThenFixedArcInFirstIteration() throws IloException {
        long startTime = System.currentTimeMillis();
        double lastValidCost = Double.MAX_VALUE;
        Solution lastValidSolution = null;
        Solution solution=null;
        double formerCost = Double.MAX_VALUE;
        double latterCost = 0;
        ArrayList<TripWithStartingInfos> previousTripWithStartingInfosArrayList = null;
        ArrayList<TripWithStartingInfos> currentTripWithStartingInfosArrayList = null;
        while (shouldContinue(lastValidSolution, formerCost, latterCost,
                previousTripWithStartingInfosArrayList, currentTripWithStartingInfosArrayList, nbIteration)) {
            // 检查总时长是否超过7200秒// change 2025.7.7
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // 转换为秒
            if (elapsedTime > 7200) {
                System.out.println("Iteration stopped due to timeout. Total elapsed time: " + elapsedTime + " seconds.");
                break;
            }
            if (nbIteration == 0) {
                int currentIteration=nbIteration+1;
                // Step 1.1: Solve with three choice of Departure Time, the sub-problem aims to optimize the path arc and choose good departure time among three choices
                solveWithChooseThreeDepartureTime = new SolveWithChooseThreeDepartureTime(this.instance, warmStartFilePath, whetherSolveRelaxationProblem);
                solution = solveWithChooseThreeDepartureTime.solveWithCplex();

                if (solution != null) {
                    formerCost = solution.getTotalCost();
                    lastValidSolution = solution;
                    lastValidCost = solution.getTotalCost();
                    previousTripWithStartingInfosArrayList = solution.getAllTripWithStartingTimeInfos();
                    System.out.println("Solution found in choose three Departure time Sub-problem with cost: " + lastValidCost + " in the iteration " + currentIteration);
                } else {
                    System.out.println("Fixed departure sub-problem failed!");
                    throw new RuntimeException("Unexpected null solution in Step 1.1. Check your input data or model setup.");
                }

                /**
                 *  when the step 1 solution is not null, take the solution in step 1 as input for the step 2
                 * */
                // Step 1.2: Solve with Fixed Arc, the sub-problem aims to optimize the departure time
                System.out.println("Input to Fixed Arc Sub-problem in iteration_" + currentIteration + " is: " + solution);
                solveWithFixedArc = new SolveWithFixedArc(this.instance, false);
                solveWithFixedArc.setArcDecisionVariables(solution);
                solveWithFixedArc.setValueOnArcDecisionVariablesIntheModel(solution);
                solution = solveWithFixedArc.solveWithCplex();

                System.out.println("Output from Fixed Arc Sub-problem in iteration_" + currentIteration + " is: " + solution);

                // -----Step 2.1: check weather have solution
                if (solution != null) {
                    latterCost = (int) solution.getTotalCost();
                    lastValidSolution = solution;
                    lastValidCost = solution.getTotalCost();
                    System.out.println("Solution found in Fixed Arc Sub-problem with cost: " + lastValidCost+ " in the iteration"+currentIteration);
                    currentTripWithStartingInfosArrayList = solution.getAllTripWithStartingTimeInfos();
                } else {
                    System.out.println("Fixed arc sub-problem failed!");
                    throw new RuntimeException("Unexpected null solution in Step 1.2. This should not happen for a valid initial solution.");
                }
                upperBound = lastValidCost;
                //这里第一次迭代肯定有解，因为我们都选中间时间窗的时候就有解
            } else {
                solution=lastValidSolution;
                upperBound=lastValidCost;
                int currentIteration = nbIteration + 1;
                System.out.println(" new we enter the  iteration "+currentIteration);
                // Step 2.1: Solve with Fixed Departure Time, the sub-problem aims to focus only on optimizing the path arc
                ArrayList<TripWithStartingInfos> inputTripWithStartingInfosArrayList = lastValidSolution.getAllTripWithStartingTimeInfos();
                System.out.println("now the iteration is " + currentIteration  + " input from iteration " + currentIteration  + " is " + inputTripWithStartingInfosArrayList);
                solveWithFixedDepartureTime = new SolveWithFixedDepartureTime(this.instance, whetherSolveRelaxationProblem);
                solveWithFixedDepartureTime.setDepartureTimeVariableList(inputTripWithStartingInfosArrayList);
                solveWithFixedDepartureTime.setValuesOnDepartureTimeInTheModel(inputTripWithStartingInfosArrayList);
                solution = solveWithFixedDepartureTime.solveWithCplex();

                if (solution != null) {
                    lastValidSolution = solution;
                    lastValidCost = solution.getTotalCost();
                    formerCost = solution.getTotalCost();
                    previousTripWithStartingInfosArrayList = solution.getAllTripWithStartingTimeInfos();
                    System.out.println("Solution found in fix Departure time Sub-problem with cost: " + lastValidCost + " in the iteration " + currentIteration );

                } else {
                    System.out.println("Fixed departure sub-problem failed!");
                }

                /**
                 *  when the step 1 solution is not null, take the solution in step 1 as input for the step 2
                 * */

                // Step 2.2: Solve with Fixed Arc, the sub-problem aims to optimize the departure time
                System.out.println("Input to Fixed Arc Sub-problem in iteration_" + currentIteration + " is: " + solution);
                solveWithFixedArc = new SolveWithFixedArc(this.instance, false);
                solveWithFixedArc.setArcDecisionVariables(solution);
                solveWithFixedArc.setValueOnArcDecisionVariablesIntheModel(solution);
                solution = solveWithFixedArc.solveWithCplex();
                System.out.println("Output from Fixed Arc Sub-problem in iteration_" + currentIteration  + " is: " + solution);

                // -----Step 2.1: check weather have solution
                if (solution != null) {
                    lastValidSolution = solution;
                    lastValidCost = solution.getTotalCost();
                    latterCost = solution.getTotalCost();
                    currentTripWithStartingInfosArrayList = solution.getAllTripWithStartingTimeInfos();
                    System.out.println("Solution found in Fixed Arc Sub-problem with cost: " + lastValidCost + " in the iteration " + currentIteration );
                } else {
                    System.out.println("Fixed arc sub-problem failed!");
                    break;
                }
                upperBound = lastValidCost;
            }
            nbIteration++;
            System.out.println(" now we finished the iteration "+nbIteration);
            this.timeInMilliSec = (int) (System.currentTimeMillis() - startTime);
            break;
        }
        if (lastValidSolution != null) {
            this.timeInMilliSec = (int) (System.currentTimeMillis() - startTime);
            System.out.println("Final upper bound " + upperBound);
            System.out.println("Final solution found with cost: " + lastValidSolution.getTotalCost() + " in the final iteration " + nbIteration);
            System.out.println("Total Idle Time cost " + lastValidSolution.getTotalIdleTimeCost());
            System.out.println("Total Idle Time cost for drivers " + lastValidSolution.getTotalIdleTimeCostOfAllDriver());
            System.out.println("Total Idle Time Cost for vehicles " + lastValidSolution.getTotalIdleTimeCostOfAllVehicle());
            System.out.println("Total Changeover Cost " + lastValidSolution.getTotalChangeoverCostForAllDriver());
            return lastValidSolution;
        } else {
            this.timeInMilliSec = (int) (System.currentTimeMillis() - startTime);
            System.out.println("the problem is not solved!");
            return null;
        }
    }

    private boolean shouldContinue(Solution lastValidSolution, double formerCost, double latterCost,
                                   ArrayList<TripWithStartingInfos> previousTrips,
                                   ArrayList<TripWithStartingInfos> currentTrips, int nbIteration) {
        // 最大迭代次数，建议用可配置参数代替硬编码
        final int MAX_ITERATIONS = 10;

        // 1. 检查最大迭代次数
        if (nbIteration >= MAX_ITERATIONS) {
            System.out.println("Terminating: Reached maximum iterations (" + MAX_ITERATIONS + ").");
            return false;
        }

        // 2. 检查是否有有效解（非第一次迭代）
        if (nbIteration != 0 && lastValidSolution == null) {
            System.out.println("Terminating: No valid solution found in iteration " + nbIteration + ".");
            return false;
        }

        // 3. 检查是否收敛（成本和行程信息一致）
        if (formerCost == latterCost) {
            if (previousTrips != null && currentTrips != null) {
                if (previousTrips.equals(currentTrips)) {
                    System.out.println("Terminating: Convergence detected in iteration " + nbIteration +
                            " (Cost: " + formerCost + ").");
                    return false;
                }
            }
        }
        // 如果以上条件都不满足，则继续迭代
        return true;
    }


    private ArrayList<TripWithStartingInfos> getInitialDepartureTimeInformation() throws IloException {
        ArrayList<TripWithStartingInfos> tripWithStartingInfosArrayList = new ArrayList<>();
        for (int t = 0; t < instance.getNbTrips(); t++) {
            Trip trip = instance.getTrip(t);
            int idOfTrip = trip.getIdOfTrip();
            int timeStartTW = this.instance.getTrip(t).getEarliestDepartureTime();
            //System.out.println("trip _" + idOfTrip + " earliest tw " + timeStartTW);
            int timeEndTW = this.instance.getTrip(t).getLatestDepartureTime();
            //System.out.println("trip_" + idOfTrip + " latest tw " + timeEndTW);
            int difference = timeEndTW - timeStartTW;
            //System.out.println("trip time window difference " + difference);
            int timeWindowDuration = this.instance.getMaxFolderOfTimeSlotAsTimeWindow() * instance.getTimeSlotUnit();
            if (timeWindowDuration - difference < 0.1 || difference - timeWindowDuration < 0.1) {
//            int timeMidTW= timeStartTW+(timeEndTW-timeStartTW)/2;
                int timeMidTW = timeStartTW + Math.round((timeEndTW - timeStartTW) / 2.0f);//%标准的四舍五入方法，会返回最接近的整数。
                TripWithStartingInfos tripWithStartingInfos = new TripWithStartingInfos(instance.getTrip(t), timeMidTW);
                tripWithStartingInfosArrayList.add(t, tripWithStartingInfos);
                //System.out.println("the trip " + t + " is initialize  to start at time: " + timeMidTW);
            } else {
                // System.out.println(" trip " + trip + " has not the given time window " + difference);
            }
        }
        return tripWithStartingInfosArrayList;
    }


    public double getUpperBound() {
        return this.upperBound;
    }

    public int getNbIteration() {
        return this.nbIteration;
    }


    public double getTimeInSec() {
        return timeInMilliSec / 1000;
    }

    public static void main(String[] args) throws IloException {

        InstanceReader reader1 = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW6.txt");

        Instance instance = reader1.readFile(); //这个语句将文本的内容就读出来了
        System.out.println(instance);

        String warmStartPath = "feaSol_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW6.txt";

        SolveIterative solveIterative = new SolveIterative(instance, warmStartPath, false);
        // print solution
        Solution sol = solveIterative.solveIterativeByCplex_WithChooseOneDepTimeAmongThreeThenFixedArcInFirstIteration();
        if (sol != null) {
            sol.printInfile("sol_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW6.txt");
            int variableCost = (int) Math.round(sol.getTotalIdleTimeCost() + sol.getTotalChangeoverCostForAllDriver());
            System.out.println("variable cost is " + variableCost);
        }

    }
}
