//package Generator;
//
//import Instance.Instance;
//import Instance.Trip;
//import Instance.TripWithDepartureTime;
//
//import Instance.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ReverseRouteCalulate {
//    private Instance instance;
//    private Trip firstTrip;            // 起点 Trip
//    private TripWithDepartureTime givenLastTripWithDepTime;              // 关键 Trip
//    private ArrayList<TripWithDepartureTime> bestPathWithTime; // 最优路径（Trip + 出发时间）
//
//
//    public ReverseRouteCalulate(Instance instance, Trip firstTrip, TripWithDepartureTime givenLastTripWithDepTime) {
//        this.instance = instance;
//        this.firstTrip = firstTrip;
//        this.givenLastTripWithDepTime = givenLastTripWithDepTime;
//        this.bestPathWithTime = new ArrayList<>();
//    }
//
//
//    public int getDepartureFirstTrip(Instance instance,Trip firstTrip, TripWithDepartureTime givenLastTripWithDepTime) {
//        int currentTripId = givenLastTripWithDepTime.getIdOfTrip();
//        int currentDepTime = givenLastTripWithDepTime.getDepartureTime();
//
//        // 如果已到达 firstTrip，直接返回该节点作为路径起点
//        if (currentTripId == firstTrip.getIdOfTrip()) {
//            List<TripWithDepartureTime> path = new ArrayList<>();
//            path.add(currentTripWithTime);
//            return new PathResult(currentDepTime, path);
//        }
//
//        List<Integer> preTripIds = instance.getPreTrips(currentTripId);
//        if (preTripIds.isEmpty()) {
//            return null; // 无前驱，路径断了
//        }
//    }
//
//
//    public static void main(String[] args) {
//        InstanceReader reader = new InstanceReader("instance_nbCity03_Size90_Day1_nbTrips05.txt");
////        Instance instance3 = reader3.readFile();
////        //    Instance instance = reader.readFile();
////        System.out.println(instance3);
////        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW1.txt");
//        //inst_nbCity20_Size300_Day3_nbTrips300_combPer0.1
//        Instance instance = reader.readFile(); //this will  read the file
//        System.out.println(instance);
//        ReverseRouteCalulate reverseRouteCalulate = new ReverseRouteCalulate(instance, instance.getDepot(0), instance.getTrip(0));
//        System.out.println(reverseRouteCalulate.getBestDepartureFirstTrip(instance, instance.getDepot(0), instance.getTrip(0)));
//        int minutes = reverseRouteCalulate.getBestDepartureFirstTrip(instance, instance.getDepot(0), instance.getTrip(0));
//        int hours = minutes / 60;
//        int mins = minutes % 60;
//
//        System.out.printf("最佳发车时间是：%02d:%02d\n", hours, mins);
//
//    }
//}
