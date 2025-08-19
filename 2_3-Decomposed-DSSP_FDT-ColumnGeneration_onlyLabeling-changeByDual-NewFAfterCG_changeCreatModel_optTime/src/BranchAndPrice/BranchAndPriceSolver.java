//package BranchAndPrice;
//
//import Generator.SubPSolByLabelAlgGeneGivenNegReducedCostPathsForAllDepots;
//
//import Instance.Instance;
//import Instance.Trip;
//import Instance.InstanceReader;
//import Solution.Solution;
//import Solution.DriverSchedule;
//import Solution.SchedulesReader;
//import ilog.concert.*;
//import ilog.cplex.IloCplex;
//
//import java.io.IOException;
//import java.util.*;
//
//import ColumnGe.MasterProblem;
//
//
//public class BranchAndPriceSolver {
//    private Instance instance;
//    private Solution initialFeasibleSolution;
//    double globalLowerBound = Double.POSITIVE_INFINITY;
//
//
//    public BranchAndPriceSolver(Instance instance, Solution initialSolution) {
//        this.instance = instance;
//        this.initialFeasibleSolution = initialSolution;
//    }
//
////    public void runOneLevelBranching() throws IloException {
////        System.out.println("▶️ Running Column Generation at Root Node...");
////        MasterProblem root = new MasterProblem(instance, initialFeasibleSolution);
////        Solution rootSol = root.solve(); // full CG + ILP at root
////        double rootLPValue = root.getFinalLowerBoundOfMP();
////        double rootIntValue = root.getFinalIntegerValueOfMasterObjective();
////        System.out.println("Root LP bound: " + rootLPValue);
////        System.out.println("Root Integer: " + rootIntValue);
////
////        // find fractional variable
////        IloCplex cplex = root.getCplex(); // add getCplex() to your MasterProblem
////        ArrayList<DriverSchedule> allSchedules = root.getDriverSchedules();
////        DriverSchedule fracSchedule = null;
////
////        for (int k = 0; k < allSchedules.size(); k++) {
////            DriverSchedule schedule = allSchedules.get(k);
////            Double val = rootSol.getScheduleValue(schedule); // ✅ 取代 cplex.getValue()
////            if (val != null && val > 1e-4 && val < 0.9999) {
////                fracSchedule = schedule;
////                System.out.println("Found fractional schedule x_" + k + " = " + val);
////                break;
////            }
////        }
////
////        if (fracSchedule == null) {
////            System.out.println("✅ No fractional schedule found. Already integer solution.");
////            return;
////        }
////
////        // 👈 分支节点 A：x_j = 0
////        System.out.println("🌲 Branch Node A: x_j = 0");
////        Solution solA = solveWithBranchConstraint(fracSchedule, false);
////        // 👈 分支节点 B：x_j = 1
////        System.out.println("🌲 Branch Node B: x_j = 1");
////        Solution solB = solveWithBranchConstraint(fracSchedule, true);
////
////        // ➕ 比较子节点
////        System.out.println("Branch A: " + solA.getTotalCost() + ", Branch B: " + solB.getTotalCost());
////    }
//
//
//    public void runBranchAndPrice() throws IloException {
//        System.out.println("▶️ Running full Branch-and-Price...");
//
//        // Step 1: 构造根节点
//        MasterProblem root = new MasterProblem(instance, initialFeasibleSolution);
//        Solution rootSol = root.solve(); // full CG + ILP
//        double bestIntValue = root.getFinalIntegerValueOfMasterObjective();
//
//        // Step 2: 创建根节点对象
//        BranchNode rootNode = new BranchNode(root);
//        rootNode.solution = rootSol;
//        rootNode.bound = root.getFinalLowerBoundOfMP();
//        rootNode.isSolved = true;
//        nodeQueue.add(rootNode);
//
//        // Step 3: 初始化 best solution
//        Solution bestSolution = rootSol;
//
//        // Step 4: 主循环
//        while (!nodeQueue.isEmpty()) {
//            BranchNode node = nodeQueue.poll();
//
//            // 🔽 更新全局下界（即使被剪枝也可以更新）
//            if (node.bound < globalLowerBound) {
//                globalLowerBound = node.bound;
//                System.out.printf("📉 Updated global lower bound: %.2f\n", globalLowerBound);
//            }
//
//            if (node.bound >= bestIntValue - 1e-4) {
//                System.out.println("⛔️ 节点被剪枝：bound=" + node.bound + ", currentBest=" + bestIntValue);
//                continue;
//            }
//
//            // 获取 fractional 变量
//            DriverSchedule fracSchedule = getFractionalSchedule(node.masterProblem);
//            if (fracSchedule == null) {
//                boolean feasible = node.solution != null && node.solution.whetherSolutionIsFeasible();
//                if (feasible) {
//                    System.out.println("✅ Found better integer solution, value = " + node.masterProblem.getFinalIntegerValueOfMasterObjective());
//                    bestIntValue = node.masterProblem.getFinalIntegerValueOfMasterObjective();
//                    bestSolution = node.solution;
//                    bestMasterProblem = node.masterProblem;  // ✅ 关键补充，别漏
//                } else {
//                    System.out.println("⚠️ LP variables are integer but solution is not feasible (some trips may be uncovered)");
//                }
//                continue;
//            }
//
//            // 创建两个子节点
//            createChildNodes(node, fracSchedule);
//        }
//        System.out.printf("✅ Branch-and-Price 最终全局下界 (LP): %.2f\n", globalLowerBound);
//
//        System.out.println("✅ 最终最优整数解: " + bestIntValue);
//        System.out.println(bestSolution);
//        if (bestSolution != null && bestSolution.whetherSolutionIsFeasible()) {
//            System.out.println("✅ 最终最优整数解: " + bestIntValue);
//            System.out.println(bestSolution);
//        } else {
//            System.out.println("⚠️ Branch-and-Price ended without any integer solution.");
//        }
//
//
//    }
//
////   //原来一步的
////    private DriverSchedule getFractionalSchedule(IloCplex cplex, List<DriverSchedule> schedules, Map<DriverSchedule, IloNumVar> varMap) throws IloException {
////        for (DriverSchedule schedule : schedules) {
////            IloNumVar var = varMap.get(schedule);
////            if (var == null) continue;
////
////            double val = cplex.getValue(var);
////            if (val > 1e-4 && val < 0.9999) {
////                System.out.println("⚠️ Found fractional var for schedule: x = " + val);
////                return schedule;
////            }
////        }
////        return null; // no fractional schedule found
////    }
//    // 一步的现在改为这个：
//private DriverSchedule getFractionalSchedule(MasterProblem mp) throws IloException {
//    IloCplex cplex = mp.getCplex();
//
//    // ⚠️ 先检查 CPLEX 是否已经被求解过（否则 _cplexi 可能未初始化）
//    try {
//        IloCplex.Status status = cplex.getStatus();  // 会触发 NullPointerException if not solved
//        System.out.println("Solver Status: " + status);
//
//        if (!mp.getCplex().isPrimalFeasible() || mp.getCplex().getStatus() == null) {
//            System.out.println("⚠️ 当前模型 LP 不可行或未求解，跳过此节点");
//            return null;
//        }
//
//        if (!cplex.isPrimalFeasible()) {
//            System.out.println("⚠️ 当前模型没有可行解，跳过分支。");
//            return null;
//        }
//    } catch (NullPointerException e) {
//        System.out.println("❌ CPLEX 尚未求解，跳过 getFractionalSchedule()");
//        return null;
//    }
//
//    // ✅ 安全了，再去遍历变量取值
//    ArrayList<DriverSchedule> schedules = mp.getDriverSchedules();
//    for (DriverSchedule schedule : schedules) {
//        IloNumVar var = mp.getVarForSchedule(schedule);
//        if (var == null) continue;
//
//        double val = cplex.getValue(var);
//        if (val > 1e-4 && val < 0.9999) {
//            System.out.println("⚠️ Found fractional schedule: x = " + val);
//            return schedule;
//        }
//    }
//    return null;
//}
//
//
//
////
////    private List<MasterProblem> createChildNodes(DriverSchedule fracSchedule, MasterProblem parent) throws IloException {
////        List<MasterProblem> children = new ArrayList<>();
////
////        for (boolean fixToOne : new boolean[]{false, true}) {
////            MasterProblem child = new MasterProblem(instance, initialFeasibleSolution); // 重新构建
////
////            // 添加父节点已有 schedule
////            for (DriverSchedule s : parent.getDriverSchedules()) {
////                child.addDriverSchedule(s);
////            }
////
////            child.solveRMPWithCplex(); // 初始化 RMP
////
////            // 确保变量存在
////            IloNumVar var = child.getVarForSchedule(fracSchedule);
////            if (var == null) {
////                child.addDriverSchedule(fracSchedule);
////                var = child.getVarForSchedule(fracSchedule);
////            }
////
////            // 添加约束
////            if (fixToOne) {
////                child.getCplex().addEq(var, 1);
////            } else {
////                child.getCplex().addEq(var, 0);
////            }
////
////            // 加入到候选子节点
////            children.add(child);
////        }
////
////        return children;
////    }
//
//    //上面一步的，现在改为：
//    private void createChildNodes(BranchNode parent, DriverSchedule fracSchedule) throws IloException {
//        for (boolean fixToOne : new boolean[]{false, true}) {
//            MasterProblem childMP = new MasterProblem(instance, initialFeasibleSolution);
//
//            // 复制父节点已有的 driver schedule
//            for (DriverSchedule s : parent.masterProblem.getDriverSchedules()) {
//                childMP.addDriverSchedule(s,"copyFromParent");
//            }
//
//            childMP.solveRMPWithCplex();
//
//            // 添加分支约束
//            IloNumVar var = childMP.getVarForSchedule(fracSchedule);
//            if (var == null) {
//                childMP.addDriverSchedule(fracSchedule,"newBranch");
//                var = childMP.getVarForSchedule(fracSchedule);
//            }
//            if (fixToOne) {
//                childMP.getCplex().addEq(var, 1);
//            } else {
//                childMP.getCplex().addEq(var, 0);
//            }
//
//            // ✅🔽 添加 solve() 和调试输出
//            Solution sol = childMP.solve();
//
//            if (sol == null || !childMP.getCplex().isPrimalFeasible()) {
//                System.out.println("❌ 子节点求解失败或无解，跳过该子节点");
//                continue;
//            }
//            System.out.println("🧠 子节点求解完成，fixToOne=" + fixToOne +
//                    ", bound = " + childMP.getFinalLowerBoundOfMP() +
//                    ", intValue = " + childMP.getFinalIntegerValueOfMasterObjective());
//
//            //创建子节点对象
//            BranchNode child = new BranchNode(childMP);
//            child.solution = sol;
//            child.bound = childMP.getFinalLowerBoundOfMP();
//            child.isSolved = true;
//
//            // ➕ 剪枝判断
//            if (child.bound < parent.bound - 1e-4) {
//                nodeQueue.add(child);
//                System.out.println("🆕 Added child node, fixToOne=" + fixToOne + ", bound=" + child.bound);
//            } else {
//                System.out.println("⛔️ Pruned child node, bound too large.");
//            }
//        }
//    }
//
//
//
//
//
//
//
//
//    private Solution solveWithBranchConstraint(DriverSchedule schedule, boolean fixToOne) throws IloException {
//        MasterProblem node = new MasterProblem(instance, initialFeasibleSolution);
//        node.solveRMPWithCplex(); // 先创建模型
//        IloNumVar var = node.getVarForSchedule(schedule); // 你需要新增方法获取变量
//        if (var == null) {
//            node.addDriverSchedule(schedule); // 新增该 schedule 列
//            var = node.getVarForSchedule(schedule);
//        }
//        if (fixToOne) {
//            node.getCplex().addEq(var, 1);
//        } else {
//            node.getCplex().addEq(var, 0);
//        }
//        return node.solve(); // 全流程 CG + integer solve
//    }
//
//    // 维护一个待处理节点队列（Node Queue）我们会始终优先处理 bound 最小的节点。
//    private PriorityQueue<BranchNode> nodeQueue = new PriorityQueue<>(
//            Comparator.comparingDouble(n -> n.bound)
//    );
//
//    // Benchmark 中用
//
//    // 添加在类的最后（比如 main() 方法前）
//    private MasterProblem bestMasterProblem;
//
//    public MasterProblem getBestMasterProblem() {
//        return bestMasterProblem;
//    }
//
//
//    public static void main(String[] args) throws IOException, IloException {
//        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips030_combPer0.25_TW4.txt");
//        //InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips040_combPer0.25_TW5.txt");
//        //inst_nbCity20_Size300_Day3_nbTrips300_combPer0.1
//        Instance instance = reader.readFile(); //this will  read the file
//        System.out.println(instance);
//        System.out.println(instance.getTimeWindowRange());
//
//        Instance instance1=reader.readFile();
//        SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips030_combPer0.25_TW4.txt", instance);
//        //  SchedulesReader schedulesReader1 = new SchedulesReader("feaSol_nbCity03_Size90_Day1_nbTrips040_combPer0.25_TW5.txt", instance);
////        scheduleSolution_inst_nbCity05_Size180_Day1_nbTrips020_combPer0.0
//        //Solution initialSchedules = schedulesReader1.readFile();
////        System.out.println(initialSchedules);
////        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());
//        //    SchedulesReader schedulesReader1 = new SchedulesReader("feas_basic.txt", instance1);
//        Solution initialSchedules = schedulesReader1.readFile();
//        System.out.println("inti"+initialSchedules);
//
//        System.out.println("instance1"+instance1);
//        BranchAndPriceSolver solver = new BranchAndPriceSolver(instance, initialSchedules);
////        solver.runOneLevelBranching();
//        solver.runBranchAndPrice();
//
//
//
//    }
//}
