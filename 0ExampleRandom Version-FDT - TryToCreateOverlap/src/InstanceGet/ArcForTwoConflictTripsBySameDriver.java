package InstanceGet;

public class ArcForTwoConflictTripsBySameDriver {
    private int idOfFirstTrip;
    private int idOfSecondTrip;

    public ArcForTwoConflictTripsBySameDriver(int idOfFirstTrip, int idOfSecondTrip) {
        this.idOfFirstTrip = idOfFirstTrip;
        this.idOfSecondTrip = idOfSecondTrip;
    }

    public int getIdOfFirstTrip() {
        return idOfFirstTrip;
    }

    public void setIdOfFirstTrip(int idOfFirstTrip) {
        this.idOfFirstTrip = idOfFirstTrip;
    }

    public int getIdOfSecondTrip() {
        return idOfSecondTrip;
    }

    public void setIdOfSecondTrip(int idOfSecondTrip) {
        this.idOfSecondTrip = idOfSecondTrip;
    }



    @Override
    public String toString() {
        return idOfFirstTrip +
                " " + idOfSecondTrip;
    }

    public static void main(String[] args) {
        ArcForTwoConflictTripsBySameDriver arcForTwoConflictTripsBySameDriver = new ArcForTwoConflictTripsBySameDriver(0,1);
        System.out.println(arcForTwoConflictTripsBySameDriver);
    }
}
