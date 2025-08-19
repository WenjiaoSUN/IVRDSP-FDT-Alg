package Instance;
import java.util.Comparator;

    public class TripComparators {
        // 按照 latestDepartureTime 降序 -> duration 降序 -> earliestDepartureTime 降序 -> id 升序
        public static final Comparator<Trip> BY_LATEST_DEPARTURE = Comparator
                .comparing(Trip::getLatestDepartureTime, Comparator.reverseOrder()) // latestDepartureTime 降序:即 latestDepartureTime 越大，排在越前）
                .thenComparing(Trip::getDuration, Comparator.reverseOrder()) // duration 降序（即 duration 越大，排在越前）。
                .thenComparing(Trip::getEarliestDepartureTime, Comparator.reverseOrder()) // earliestDepartureTime 降序（即 earliestDepartureTime 越大，排在越前）。
                .thenComparingInt(Trip::getIdOfTrip); // id 升序（即 idOfTrip 越小，排在越前）。


//        @Override
//        public int compareTo(Trip trip) {
//            if (this.getLatestDepartureTime() > trip.getLatestDepartureTime()) {
//                return -1;
//            } else if (this.getLatestDepartureTime() < trip.getLatestDepartureTime()) {
//                return 1;
//            } else {
//                if (this.getDuration() > trip.getDuration()) {
//                    return -1;
//                } else if (this.getDuration() < trip.getDuration()) {
//                    return 1;
//                } else {
//                    if (this.getEarliestDepartureTime() > trip.getEarliestDepartureTime()) {
//                        return -1;
//                    } else if (this.getEarliestDepartureTime() < trip.getEarliestDepartureTime()) {
//                        return 1;
//                    } else {
//                        return this.idOfTrip - trip.idOfTrip;
//                        // if there are two trip which have the same departure and arrival time,
//                        // then it will first return the trip with  small id, second with the trip with bigger ID (order)
//                        //instead of the +/- result of the id value
//                    }
//                }
//            }
//        }
///**
// * 排序规则
// * 按最大出发时间 (latestDepartureTime) 降序排序：
// * 如果 this 的 latestDepartureTime 大于 trip 的 latestDepartureTime，则排在前，返回 -1。
// * 如果小于，则排在后，返回 1。
// * 当最大出发时间相同时，按行程持续时间 (duration) 升序排序：
// *
// * 如果 this 的 duration 小于 trip 的 duration，则排在前，返回 -1。
// * 如果大于，则排在后，返回 1。
// * 当最大出发时间和行程持续时间相同时，按最早出发时间 (earliestDepartureTime) 降序排序：
// *
// * 如果 this 的 earliestDepartureTime 大于 trip 的 earliestDepartureTime，则排在前，返回 -1。
// * 如果小于，则排在后，返回 1。
// * 当最大出发时间、行程持续时间和最早出发时间都相同时，按 idOfTrip 升序排序：

        // 按照 departureTime 升序 -> arrivalTime 升序 -> id 升序
        public static final Comparator<Trip> BY_EARLIEST_DEPARTURE = Comparator
                .comparingInt(Trip::getEarliestDepartureTime) // 升序
                .thenComparingInt(Trip::getLatestDepartureTime) // 升序
                .thenComparing(Trip::getDuration) // duration 升序（即 duration 越小，排在越前）。
                .thenComparingInt(Trip::getIdOfTrip); // 升序
    }


