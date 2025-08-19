package InstanceGet;

public class Pair {
    private int first;
    private int second;

    public Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public boolean isConflict(Pair other) {
        return (this.first == other.first || this.first == other.second)
                || (this.second == other.first || this.second == other.second);
    }
}

