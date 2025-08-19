//package Generator;
//
//import Instance.*;
//
//import java.util.*;
//
//public class ForwardPathPlanner {
//
//    private final Instance instance;         // 实例数据
//    private final Trip firstTrip;            // 起点 Trip（用于判断是否回到原起点）
//    private final Trip keyTrip;              // 关键 Trip
//    private final int keyTripStartTime;      // 关键 Trip 的固定出发时间
//
//    private ArrayList<TripWithDepartureTime> bestPathWithTime; // 最优路径（Trip + 出发时间）
//    private int minGapFromKeyTrip = Integer.MAX_VALUE; // 记录最紧凑路径的 keyTrip 出发到终点 Trip 的间隔
//
//    public ForwardPathPlanner(Instance instance, Trip firstTrip, Trip keyTrip, int keyTripStartTime) {
//        this.instance = instance;
//        this.firstTrip = firstTrip;
//        this.keyTrip = keyTrip;
//        this.keyTripStartTime = keyTripStartTime;
//
//        this.bestPathWithTime = new ArrayList<>();
//    }
//
//    public ArrayList<TripWithDepartureTime> compute() {
//        return forwardDFS(
//                keyTrip,
//                keyTripStartTime,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                new HashSet<>()
//        );
//    }
//
//    public ArrayList<TripWithDepartureTime> getBestPathWithTime() {
//        return bestPathWithTime;
//    }
//
//    // 🔁 正向 DFS：从 keyTrip 开始，向后查找路径直到回到 firstTrip 的起始城市
//    private ArrayList<TripWithDepartureTime> forwardDFS(
//            Trip currentTrip,
//            int departureTimeForCurrentTrip,
//            List<Trip> path,
//            List<Integer> startTimes,
//            Set<Integer> visited
//    ) {
//        //System.out.println("\n🔍 当前递归 Trip: " + currentTrip.getIdOfTrip() + ", 出发时间: " + departureTimeForCurrentTrip);
//
//        path.add(currentTrip);
//        startTimes.add(departureTimeForCurrentTrip);
//        visited.add(currentTrip.getIdOfTrip());
//
//        // ✅ 终止条件：路径结束于 firstTrip 起点，且路径长度 > 1
//        if (!path.isEmpty() && currentTrip.getIdOfEndCity() == firstTrip.getIdOfStartCity() && path.size() > 1) {
//            int lastTime = departureTimeForCurrentTrip;
//            int endTime = lastTime + currentTrip.getDuration();
//            int gap = endTime - keyTripStartTime;
//            // ✅ 优先更长路径，其次离 keyTrip 更近
//            if (path.size() > bestPathWithTime.size() ||
//                    (path.size() == bestPathWithTime.size() && gap < minGapFromKeyTrip)) {
//                bestPathWithTime.clear();
//                for (int i = 0; i < path.size(); i++) {
//                    bestPathWithTime.add(new TripWithDepartureTime(path.get(i), startTimes.get(i)));
//                  //  System.out.println("    → Trip " + path.get(i).getIdOfTrip() + " @ " + startTimes.get(i));
//                }
//                minGapFromKeyTrip = gap;
//            }
//            path.remove(path.size() - 1);
//            startTimes.remove(startTimes.size() - 1);
//            visited.remove(currentTrip.getIdOfTrip());
//            return bestPathWithTime;
//        }
//
//        for (Trip nextTrip : instance.getSuccessTrips(currentTrip)) {
//            int nextId = nextTrip.getIdOfTrip();
//            if (visited.contains(nextId)) continue;
//
//            int duration = currentTrip.getDuration();
//            int readyTime = departureTimeForCurrentTrip + duration;
//            //System.out.println("  ↪ 尝试后继 Trip " + nextId + " 时间窗: [" + nextTrip.getEarliestDepartureTime() + ", " + nextTrip.getLatestDepartureTime() + "]");
//
//            // ✅ 从靠近 readyTime（最早出发）开始往后尝试（更紧凑）
//            for (int t = nextTrip.getEarliestDepartureTime(); t <= nextTrip.getLatestDepartureTime(); t++) {
//                int gap = t - readyTime;
//                if (gap >= instance.getMinPlanTurnTime()) {
//                 //   System.out.println("      ✅ 可行: 出发 " + t + "，距离上次到达 " + readyTime + "，gap = " + gap);
//                    forwardDFS(nextTrip, t, path, startTimes, visited);
//                } else {
//                   // System.out.println("      ❌ 排除: 出发 " + t + "，gap = " + gap + " < 最小要求 " + instance.getMinPlanTurnTime());
//                }
//            }
//        }
//
//        path.remove(path.size() - 1);
//        startTimes.remove(startTimes.size() - 1);
//        visited.remove(currentTrip.getIdOfTrip());
//
//        return bestPathWithTime;
//    }
//
//
//    public static void main(String[] args) {
//        InstanceReader reader3 = new InstanceReader("instance_nbCity03_Size90_Day1_nbTrips05.txt");
//        Instance instance3 = reader3.readFile();
//
//        //    Instance instance = reader.readFile();
//        System.out.println(instance3);
//        Trip firstTrip = instance3.getTrip(0);
//        Trip keyTrip = instance3.getTrip(2);
//        ForwardPathPlanner planner = new ForwardPathPlanner(instance3, firstTrip, keyTrip, 493);
//        planner.compute();
//
//        ArrayList<TripWithDepartureTime> finalPath = planner.compute();
//        for (TripWithDepartureTime twd : finalPath) {
//            System.out.println(twd);
//        }
//    }
//}