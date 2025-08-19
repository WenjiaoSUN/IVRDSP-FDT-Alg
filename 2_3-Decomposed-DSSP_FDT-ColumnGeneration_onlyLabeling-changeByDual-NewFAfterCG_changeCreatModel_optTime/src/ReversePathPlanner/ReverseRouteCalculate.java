//package ReversePathPlanner;
//import Instance.Instance;
//import Instance.Trip;
//import Instance.TripWithDepartureTime;
//
//import Instance.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ReverseRouteCalculate {
//    private Instance instance;
//    private Trip firstTrip;            // 起点 Trip
//    private TripWithDepartureTime givenLastTripWithDepTime;              // 关键 Trip
//    private ArrayList<TripWithDepartureTime> bestPathWithTime; // 最优路径（Trip + 出发时间）
//
//    public ReverseRouteCalculate(Instance instance, Trip firstTrip, TripWithDepartureTime givenLastTripWithDepTime) {
//        this.instance = instance;
//        this.firstTrip = firstTrip;
//        this.givenLastTripWithDepTime = givenLastTripWithDepTime;
//        this.bestPathWithTime = new ArrayList<>();
//    }
//
//
//    public PathResult getBestPathBackToFirstTrip(Instance instance,Trip firstTrip, TripWithDepartureTime givenLastTripWithDepTime) {
//        TripWithDepartureTime currentTripWithTime=givenLastTripWithDepTime;
//        int currentTripId = givenLastTripWithDepTime.getIdOfTrip();
//        int currentDepTime = givenLastTripWithDepTime.getDepartureTime();
//        Trip currentTrip=instance.getTrip(currentTripId);
//
//        // 如果已到达 firstTrip，直接返回该节点作为路径起点
//        if (currentTripId == firstTrip.getIdOfTrip()) {
//            ArrayList<TripWithDepartureTime> path = new ArrayList<>();
//            path.add(currentTripWithTime);
//            return new PathResult(currentDepTime, path);
//        }
//
//        ArrayList<Trip> preTrips = instance.getPreTrips(currentTrip);
//        if (preTrips.isEmpty()) {
//            return null; // 无前驱，路径断了
//        }
//        PathResult bestResult = null;
//
//        for (Trip preTrip : preTrips) {
//            int depE = preTrip.getEarliestDepartureTime();
//            int depL = preTrip.getLatestDepartureTime();
//            int duration = preTrip.getDuration();
//            int estimatedDep = currentDepTime - instance.getMinPlanTurnTime() - duration;
//
//            if (estimatedDep < depE) continue;//which is not feasible for the next preTrip
//            if (estimatedDep > depL) {
//                estimatedDep = depL;
//            }
//
//            TripWithDepartureTime preTripWithTime = new TripWithDepartureTime(preTrip, estimatedDep);
//
//            PathResult candidateResult = getBestPathBackToFirstTrip(instance, firstTrip,preTripWithTime);
//            if (candidateResult != null) {
//                if (bestResult == null || candidateResult.depTime < bestResult.depTime) {
//                    bestResult = new PathResult(candidateResult.depTime, new ArrayList<>(candidateResult.path));
//                    bestResult.path.add(currentTripWithTime); // 将当前节点加入路径（递归回溯）
//                }
//            }
//        }
//
//        return bestResult;
//    }
//
//
//    public static void main(String[] args) {
//        InstanceReader reader = new InstanceReader("instance_nbCity03_Size90_Day1_nbTrips05.txt");
////        Instance instance3 = reader3.readFile();
////        //    Instance instance = reader.readFile();
////        System.out.println(instance3);
////        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1.txt");
////        inst_nbCity20_Size300_Day3_nbTrips300_combPer0.1
//        Instance instance = reader.readFile(); //this will  read the file
//        System.out.println(instance);
//        TripWithDepartureTime tripWithDepartureTime=new TripWithDepartureTime(instance.getTrip(5),)
//        ReverseRouteCalculate reverseRouteCalulate = new ReverseRouteCalculate (instance,  instance.getTrip(0),instance.getTrip(5));
//        System.out.println(reverseRouteCalulate.getBestDepartureFirstTrip(instance, instance.getDepot(0), instance.getTrip(0)));
//        int minutes = reverseRouteCalulate.getBestDepartureFirstTrip(instance, instance.getDepot(0), instance.getTrip(0));
//        int hours = minutes / 60;
//        int mins = minutes % 60;
//
//        System.out.printf("最佳发车时间是：%02d:%02d\n", hours, mins);
//
//    }
//}
