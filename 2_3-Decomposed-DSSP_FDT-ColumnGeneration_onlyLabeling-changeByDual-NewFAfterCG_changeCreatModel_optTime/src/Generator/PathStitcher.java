//package Generator;
//
//import Instance.*;
//import Instance.TripWithDepartureTime;
//
//import java.util.*;
//
//public class PathStitcher {
//    private Instance instance;
//    private Trip firstTrip;
//    private Trip keyTrip;
//    private int keyTripStartTime;
//    private int maxDuration;
//
//    public PathStitcher(Instance instance, Trip firstTrip, Trip keyTrip, int keyTripStartTime, int maxDuration) {
//        this.instance = instance;
//        this.firstTrip = firstTrip;
//        this.keyTrip = keyTrip;
//        this.keyTripStartTime = keyTripStartTime;
//        this.maxDuration = maxDuration;
//    }
//
//    public  ArrayList<TripWithDepartureTime> stitchAndValidate(
//            ArrayList<TripWithDepartureTime> reversePath,
//            ArrayList<TripWithDepartureTime> forwardPath,
//            int maxTotalDuration
//    ) {
//        if (reversePath == null || reversePath.isEmpty()) return new ArrayList<>();
//
//        // ⚠️ 移除 forwardPath 中重复的 keyTrip（它在两个路径中都会出现）
//        TripWithDepartureTime keyTrip = reversePath.get(reversePath.size() - 1);
//        ArrayList<TripWithDepartureTime> trimmedForward = new ArrayList<>();
//        for (TripWithDepartureTime trip : forwardPath) {
//            if (trip.getIdOfTrip() != keyTrip.getIdOfTrip()) {
//                trimmedForward.add(trip);
//            }
//        }
//
//        // 🔗 拼接路径：reverse 在前，forward 在后
//        ArrayList<TripWithDepartureTime> fullPath = new ArrayList<>(reversePath);
//        fullPath.addAll(trimmedForward);
//
//        // 🕓 计算总耗时：最后一个 Trip 出发时间 + duration - 第一个 Trip 出发时间
//        int startTime = fullPath.get(0).getDepartureTime();
//        TripWithDepartureTime lastTrip = fullPath.get(fullPath.size() - 1);
//        int endTime = lastTrip.getDepartureTime() + lastTrip.getDuration();
//        int totalDuration = endTime - startTime;
//
//        System.out.println("🕓 路径总耗时: " + totalDuration + " 分钟");
//
//        if (totalDuration > maxTotalDuration) {
//            System.out.println("❌ 超出最大允许时间 " + maxTotalDuration + "，路径无效。");
//            return new ArrayList<>();
//        } else {
//            System.out.println("✅ 路径合法，成功拼接。");
//            return fullPath;
//        }
//    }
//
//
//    public static void main(String[] args) {
//        InstanceReader reader3 = new InstanceReader("instance_nbCity03_Size90_Day1_nbTrips05.txt");
//        Instance instance3 = reader3.readFile();
//
//        System.out.println("instance3" + instance3);
//        Trip t0 = instance3.getTrip(0);
//        System.out.println("Trip 0 时间窗: [" + t0.getEarliestDepartureTime() + ", " + t0.getLatestDepartureTime() + "]");
//        System.out.println("Trip 0 duration: " + t0.getDuration());
//
//        Trip t2 = instance3.getTrip(2);
//        System.out.println("Trip 2 固定出发时间: 493");
//
//        //    Instance instance = reader.readFile();
//        System.out.println(instance3);
//
////
////        //test group 1:
////        Trip firstTrip = instance3.getTrip(0);
////        Trip keyTrip = instance3.getTrip(2);
////        ForwardPathPlanner plannerForward = new ForwardPathPlanner(instance3, firstTrip, keyTrip, 493);
////        plannerForward.compute();
////
////        ReversePathPlanner plannerReverse = new ReversePathPlanner(instance3, firstTrip, keyTrip, 493);
////        plannerReverse.compute();
////
////        ArrayList<TripWithDepartureTime> finalPathForward = plannerForward.compute();
////        for (TripWithDepartureTime twdF : finalPathForward) {
////            System.out.println(twdF);
////        }
////
////        ArrayList<TripWithDepartureTime> finalPathReverse = plannerReverse.compute();
////        for (TripWithDepartureTime twdR : finalPathReverse) {
////            System.out.println(twdR);
////        }
////
////        PathStitcher pathStitcher =new PathStitcher(instance3,firstTrip,keyTrip,493,instance3.getMaxPlanTime());
//
//        //test group 2:
//
//        Trip firstTrip = instance3.getTrip(0);
//        Trip keyTrip = instance3.getTrip(2);
//        ForwardPathPlanner plannerForward = new ForwardPathPlanner(instance3, firstTrip, keyTrip, 493);
//        plannerForward.compute();
//
//        ReversePathPlanner plannerReverse = new ReversePathPlanner(instance3, firstTrip, keyTrip, 493);
//        plannerReverse.compute();
//
//        ArrayList<TripWithDepartureTime> finalPathForward = plannerForward.compute();
//        for (TripWithDepartureTime twdF : finalPathForward) {
//            System.out.println(twdF);
//        }
//
//        ArrayList<TripWithDepartureTime> finalPathReverse = plannerReverse.compute();
//        for (TripWithDepartureTime twdR : finalPathReverse) {
//            System.out.println(twdR);
//        }
//
//        PathStitcher pathStitcher =new PathStitcher(instance3,firstTrip,keyTrip,493,instance3.getMaxPlanTime());
//
//
//        ArrayList<TripWithDepartureTime>  finalPath=pathStitcher.stitchAndValidate(finalPathReverse,finalPathForward,instance3.getMaxWorkingTime());
//        System.out.println("final path"+finalPath);
//
//
//    }
//}
