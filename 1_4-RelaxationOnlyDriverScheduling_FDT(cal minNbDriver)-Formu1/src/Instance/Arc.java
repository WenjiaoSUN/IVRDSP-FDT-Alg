package Instance;

public class Arc {
    private int firstIdOfTrip;
    private int secondIdOfTrip;

    public Arc (int firstIdOfCombined, int secondIdOfTrip){
        this.firstIdOfTrip =firstIdOfCombined;
        this.secondIdOfTrip = secondIdOfTrip;
    }

    public int getFirstIdOfTrip() {
        return firstIdOfTrip;
    }

    public void setFirstIdOfTrip(int firstIdOfTrip) {
        this.firstIdOfTrip = firstIdOfTrip;
    }

    public int getSecondIdOfTrip() {
        return secondIdOfTrip;
    }

    public void setSecondIdOfTrip(int secondIdOfTrip) {
        this.secondIdOfTrip = secondIdOfTrip;
    }




    @Override
    public String toString() {
        return "Arc{" +
                "firstIdOfCombinedTrip=" + firstIdOfTrip +
                ", secondIdOfCombinedTrip=" + secondIdOfTrip +
                '}';
    }

    public static void main(String[] args) {
        Arc arc= new Arc(4,6);
        System.out.println(arc);
    }
}
