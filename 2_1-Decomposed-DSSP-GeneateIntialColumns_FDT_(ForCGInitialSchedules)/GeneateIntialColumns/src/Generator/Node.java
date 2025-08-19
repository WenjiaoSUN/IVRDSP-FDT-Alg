package Generator;

import Instance.Trip;

import java.util.ArrayList;

public class Node {
    private Trip trip;
    private ArrayList<Label> labels;

    public Node (Trip trip){
        this.trip=trip;
        this.labels=new ArrayList<>();
    }

    public void addLabel(Label label){
        if(labels!=null){
            this.labels.add(label);
        }
    }

    public Trip getTrip(){return  trip;}

    public ArrayList<Label> getLabels(){return labels;}

    @Override
    public String toString() {
        return "Node{" +
                "trip=" + trip +
                ", labels=" + labels +
                '}';
    }
}
