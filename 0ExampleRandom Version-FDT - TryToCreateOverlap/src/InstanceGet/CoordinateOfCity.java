package InstanceGet;

public class CoordinateOfCity {
    private int x;
    private int y;
    public CoordinateOfCity(int x, int y){
        this.x=x;
        this.y=y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "CoordinateOfCity{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
