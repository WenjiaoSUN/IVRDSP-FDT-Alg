package BranchAndPrice;

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
import ColumnGe.MasterProblem;
public class BranchNode {
    public MasterProblem masterProblem;
    public Solution solution;
    public double bound;
    public boolean isSolved;

    public ArrayList<IloConstraint> branchConstraints;

    public BranchNode(MasterProblem masterProblem) {
        this.masterProblem = masterProblem;
        this.solution = null;
        this.bound = Double.MAX_VALUE;
        this.isSolved = false;
        this.branchConstraints = new ArrayList<>();
    }

    public void addBranchConstraint(IloConstraint constraint) {
        branchConstraints.add(constraint);
    }
}
