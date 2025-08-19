package NewGraph;

//import ColumnGe.MasterProblem;
import Instance.Instance;
import Instance.InstanceReader;
import Solution.SchedulesReader;
import Solution.Solution;

import java.io.IOException;
import java.util.LinkedList;

public class NodeWithLabels {
    private NewNode node;//this is a trip or given starting(ending) depot
    private LinkedList<Label> labels;

    public NodeWithLabels(NewNode node) {
        this.node = node;
        this.labels = new LinkedList<>();
    }

    public void addLabel(Label label) {
        this.labels.add(label);
    }

    public void removeLabel(Label label){this.labels.remove(label);}

    public NewNode getNewNode() {
        return node;
    }

    public LinkedList<Label> getLabels() {
        return labels;
    }


    @Override
    public String toString() {
        return "NodeWithLabels{" +
                "node=" + node +
                ", labels=" + labels +
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity05_Size180_Day1_nbTrips020_combPer0.0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);

        SchedulesReader schedulesReader = null;
        try {
            schedulesReader = new SchedulesReader("feaSol_nbCity05_Size180_Day1_nbTrips020_combPer0.1.txt", instance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //scheduleSolution_inst_nbCity05_Size180_Day1_nbTrips020_combPer0.0
        Solution initialSchedules = schedulesReader.readFile();
        System.out.println(initialSchedules);
        System.out.println("initialColumns: " + initialSchedules.getDriverSchedules().size());

//        MasterProblem masterProblem = new MasterProblem(instance, initialSchedules);


        NewNode node1 = new NewNode( instance.getDepot(0),true);
        NewNode node2 = new NewNode(instance.getTrip(1));
        NewNode node3 = new NewNode(instance.getDepot(1),false);
        NewNode node4 = new NewNode(instance.getTrip(2));
        System.out.println(node1);
        System.out.println(node2);
        System.out.println(node3);
        GraphRelatedToGivenDepot graphRelatedToGivenDepot = new GraphRelatedToGivenDepot(instance,instance.getDepot(0));

        LinkedList<NewNode> nodes = new LinkedList<>();
        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);
        NodeWithLabels nodeWithLabels1 = new NodeWithLabels(node1);
        System.out.println("check the node in node class"+ nodeWithLabels1);
        NodeWithLabels nodeWithLabels2 = new NodeWithLabels(node2);
        NodeWithLabels nodeWithLabels3 = new NodeWithLabels(node3);
        NodeWithLabels nodeWithLabels4 = new NodeWithLabels(node4);
        Label label1=null;
        Label label2=label1.extendToNextNode(node1,-1,20);
        Label label3=label2.extendToNextNode(node2,1,100);

        nodeWithLabels1.addLabel(label2);
        System.out.println("from the default label to nodeWithLabels2"+label2);
    }


}
