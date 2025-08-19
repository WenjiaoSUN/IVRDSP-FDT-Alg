//package Generator;
//
//import Instance.*;
//import java.util.*;
//
//public class ReversePathPlanner {
//    private Instance instance;         // 实例数据
//    private Trip firstTrip;            // 起点 Trip
//    private Trip keyTrip;              // 关键 Trip
//    private int keyTripStartTime;      // 关键 Trip 的固定出发时间
//    private ArrayList<TripWithDepartureTime> bestPathWithTime; // 最优路径（Trip + 出发时间）
//    private int minGapToKeyTrip = Integer.MAX_VALUE; // 记录最紧凑路径的 keyTrip 与 firstTrip 时间间隔
//
//    public ReversePathPlanner(Instance instance, Trip firstTrip, Trip keyTrip, int keyTripStartTime) {
//        this.instance = instance;
//        this.firstTrip = firstTrip;
//        this.keyTrip = keyTrip;
//        this.keyTripStartTime = keyTripStartTime;
//        this.bestPathWithTime = new ArrayList<>();
//    }
//
//    public ArrayList<TripWithDepartureTime> compute() {
//        return reverseDFS(keyTrip, keyTripStartTime, new ArrayList<>(), new ArrayList<>(), new HashSet<>());
//    }
//
//    // 🔁 主递归逻辑：从 keyTrip 出发，向前找 firstTrip，记录最紧凑路径及对应出发时间
//    private ArrayList<TripWithDepartureTime> reverseDFS(Trip currentTrip, int departureTimeForCurrentTrip, List<Trip> path, List<Integer> startTimes, Set<Integer> visited) {
//       // System.out.println("\n🔍 当前递归 Trip: " + currentTrip.getIdOfTrip() + ", 出发时间: " + departureTimeForCurrentTrip);
//        path.add(0, currentTrip);
//        startTimes.add(0, departureTimeForCurrentTrip);
//        visited.add(currentTrip.getIdOfTrip());
//
//        // ✅ 终止条件：路径到达 firstTrip
//        if (currentTrip.getIdOfTrip() == firstTrip.getIdOfTrip()) {
//            int firstTime = departureTimeForCurrentTrip;
//            int gap = keyTripStartTime - (firstTime + currentTrip.getDuration());
//            // ✅ 优先选择路径更长的，其次选 keyTrip 更靠近的（gap 更小）
//            if (path.size() > bestPathWithTime.size() ||
//                    (path.size() == bestPathWithTime.size() && gap < minGapToKeyTrip)) {
//                bestPathWithTime.clear();
//                for (int i = 0; i < path.size(); i++) {
//                    bestPathWithTime.add(new TripWithDepartureTime(path.get(i), startTimes.get(i)));
//                    //System.out.println("    → Trip " + path.get(i).getIdOfTrip() + " @ " + startTimes.get(i));
//                }
//                minGapToKeyTrip = gap;
//            }
//            path.remove(0);
//            startTimes.remove(0);
//            visited.remove(currentTrip.getIdOfTrip());
//            return bestPathWithTime;
//        }
//
//        for (Trip predTrip : instance.getPreTrips(currentTrip)) {
//            int predId = predTrip.getIdOfTrip();
//            if (visited.contains(predId)) continue;//如果已经访问过了，就跳过；继续下一轮访问没测试过的前驱trip
//            int duration = predTrip.getDuration();
//           // System.out.println("  ↪ 尝试前驱 Trip " + predId + " 时间窗: [" + predTrip.getEarliestDepartureTime() + ", " + predTrip.getLatestDepartureTime() + "]");
//
//            // ✅ 遍历 predTrip 时间窗，从靠近 keyTrip 的时间往前尝试
//            for (int t = predTrip.getLatestDepartureTime(); t >= predTrip.getEarliestDepartureTime(); t--) {
//                int arrival = t + duration;
//                int gap = departureTimeForCurrentTrip - arrival;
//                // ✅ 必须满足衔接间隔 ≥ 最小要求，才进行递归
//                if (gap >= instance.getMinPlanTurnTime()) {
//                   // System.out.println("      ✅ 可行: 出发 " + t + "，到达 " + arrival + "，接上 Trip " + currentTrip.getIdOfTrip() + "@" + departureTimeForCurrentTrip);
//                    reverseDFS(predTrip, t, path, startTimes, visited);
//                } else {
//                   // System.out.println("      ❌ 排除: 出发 " + t + "，到达 " + arrival + "，间隔 " + gap + " < 最小要求 " + instance.getMinPlanTurnTime());
//                }
//            }
//        }
//
//        path.remove(0);
//        startTimes.remove(0);
//        visited.remove(currentTrip.getIdOfTrip());
//        return bestPathWithTime;
//    }
//
//    public ArrayList<TripWithDepartureTime> getBestPathWithTime() {
//        return bestPathWithTime;
//    }
//
//    public static void main(String[] args) {
//        InstanceReader reader3 = new InstanceReader("instance_nbCity03_Size90_Day1_nbTrips05.txt");
//        Instance instance3 = reader3.readFile();
//        //    Instance instance = reader.readFile();
//        System.out.println(instance3);
//
////        Trip firstTrip = instance3.getTrip(0);
////        Trip keyTrip = instance3.getTrip(2);
////        ReversePathPlanner planner = new ReversePathPlanner(instance3, firstTrip, keyTrip, 493);
//
//
////        // testGroup2:
////
////        Trip firstTrip = instance3.getTrip(1);
////        Trip keyTrip = instance3.getTrip(4);
////        ReversePathPlanner planner = new ReversePathPlanner(instance3, firstTrip, keyTrip, 570);//9:30
//
//
////        //test Group3
////
////
////        Trip firstTrip = instance3.getTrip(0);
////        Trip keyTrip = instance3.getTrip(4);
////        ReversePathPlanner planner = new ReversePathPlanner(instance3, firstTrip, keyTrip, 570);//9:30
//
//
//        //test Group4
//
//
//        Trip firstTrip = instance3.getTrip(1);
//        Trip keyTrip = instance3.getTrip(2);
//        ReversePathPlanner planner = new ReversePathPlanner(instance3, firstTrip, keyTrip, 493);// should  null
//
//
//
//        planner.compute();
//
//        ArrayList<TripWithDepartureTime> finalPath = planner.compute();
//        for (TripWithDepartureTime twd : finalPath) {
//            System.out.println(twd);
//        }
//    }
//}
