package Instance;

import java.util.Objects;

public class Trip implements Comparable<Trip> {
    protected int idOfTrip;
    protected int idOfStartCity;
    protected int idOfEndCity;
    protected int earliestDepartureTime;
    protected int latestDepartureTime;

    //here we add the information about the number of vehicle which will be linked together
    protected int nbVehicleNeed;

    protected int duration;

    public Trip(int idOfTrip, int idOfStartCity, int idOfEndCity, int earliestDepartureTime, int latestDepartureTime, int nbVehicleNeed, int duration) {
        this.idOfTrip = idOfTrip;
        this.idOfStartCity = idOfStartCity;
        this.idOfEndCity = idOfEndCity;
        this.earliestDepartureTime = earliestDepartureTime;
        this.latestDepartureTime = latestDepartureTime;
        this.nbVehicleNeed = nbVehicleNeed;
        this.duration = duration;
    }

    public int getIdOfStartCity() {
        return idOfStartCity;
    }

    public int getIdOfEndCity() {
        return idOfEndCity;
    }

    public int getEarliestDepartureTime() {
        return earliestDepartureTime;
    }

    public int getLatestDepartureTime() {
        return latestDepartureTime;
    }

    public int getMiddleDepartureTime() {
        return (int) Math.round(latestDepartureTime + earliestDepartureTime) / 2;
    }

    public int getIdOfTrip() {
        return idOfTrip;
    }

    public int getNbVehicleNeed() {
        return nbVehicleNeed;
    }

    public int getDuration() {
        return duration;
    }



    @Override
    public String toString() {
        return "Trip{" +
                "trip_" + idOfTrip +
//                ", StartCity_" + idOfStartCity +
//                ", EndCity_" + idOfEndCity +
//                ", earliestDepTime=" + earliestDepartureTime +
//                ", latestDepTime=" + latestDepartureTime +
//                ", nbVehicleNeedInThisTrip=" + nbVehicleNeedInThisTrip +
                '}';
    }




    @Override
    public int compareTo(Trip trip) {
        return TripComparators.BY_EARLIEST_DEPARTURE.compare(this, trip);
    }

//    @Override
//    public int compareTo(Trip trip) {
//        if (this.getLatestDepartureTime() > trip.getLatestDepartureTime()) {
//            return -1;
//        } else if (this.getLatestDepartureTime() < trip.getLatestDepartureTime()) {
//            return 1;
//        } else {
//            if (this.getDuration() >trip.getDuration()) {
//                return -1;
//            } else if (this.getDuration() < trip.getDuration()) {
//                return 1;
//            } else {
//                if (this.getEarliestDepartureTime() > trip.getEarliestDepartureTime()) {
//                    return -1;
//                } else if (this.getEarliestDepartureTime() < trip.getEarliestDepartureTime()) {
//                    return 1;
//                } else {
//                    return this.idOfTrip - trip.idOfTrip;
//                    // if there are two trip which have the same departure and arrival time,
//                    // then it will first return the trip with  small id, second with the trip with bigger ID (order)
//                    //instead of the +/- result of the id value
//                }
//            }
//        }
//    }
//    /**
//     * 排序规则
//     * 按最大出发时间 (latestDepartureTime) 降序排序：
//     * 如果 this 的 latestDepartureTime 大于 trip 的 latestDepartureTime，则排在前，返回 -1。
//     * 如果小于，则排在后，返回 1。
//     * 当最大出发时间相同时，按行程持续时间 (duration) 升序排序：
//     *
//     * 如果 this 的 duration 小于 trip 的 duration，则排在前，返回 -1。
//     * 如果大于，则排在后，返回 1。
//     * 当最大出发时间和行程持续时间相同时，按最早出发时间 (earliestDepartureTime) 降序排序：
//     *
//     * 如果 this 的 earliestDepartureTime 大于 trip 的 earliestDepartureTime，则排在前，返回 -1。
//     * 如果小于，则排在后，返回 1。
//     * 当最大出发时间、行程持续时间和最早出发时间都相同时，按 idOfTrip 升序排序：
//     * */
        @Override
        public boolean equals (Object o){
            if (this == o) return true;
            if (!(o instanceof Trip)) return false;
            Trip trip = (Trip) o;
            return this.getIdOfTrip() == trip.getIdOfTrip();//if the trip id is same it will return ture;
        }

        @Override
        public int hashCode () {
            return Objects.hash(getIdOfTrip());//just return a integer value
        }


        public static void main (String[]args){

        }


}
