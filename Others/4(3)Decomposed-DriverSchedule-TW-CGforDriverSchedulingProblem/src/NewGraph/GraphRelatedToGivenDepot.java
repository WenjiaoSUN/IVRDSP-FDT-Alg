package NewGraph;

import Instance.Instance;
import Instance.InstanceReader;
import Instance.Depot;

public class GraphRelatedToGivenDepot {
    private Instance instance;
    private Depot givenDepot;
    final double smallValue=0.00001;
    final double largeValue=9999999;

    public GraphRelatedToGivenDepot(Instance instance, Depot givenDepot) {
        this.instance=instance;
        this.givenDepot=givenDepot;
    }

    public Instance getInstance() {
        return instance;
    }

    public Depot getGivenDepot() {
        return givenDepot;
    }
    public double getArcWeightOnBetweenTwoNodesOnNewGraph(NewNode newNode1, NewNode newNode2) {
        String startingDepotType="startingDepot";
        String tripType="trip";
        String endingDepotType="endingDepot";
        double connectionTime=largeValue;
        if(newNode1.getNodeType()==tripType && newNode2.getNodeType()==tripType){
            if(instance.whetherHavePossibleArcAfterCleaning(newNode1.getId(), newNode2.getId())){
                connectionTime=instance.getMinWaitingTime(newNode1.getId(), newNode2.getId())+instance.getTrip(newNode2.getId()).getDuration();// 2025.1.03 原来只考虑connection time, 现在用minConnection + 后面Trip的Duration
            }

        } else if (newNode1.getNodeType()==tripType && newNode2.getNodeType()==endingDepotType) {
            int idTrip= newNode1.getId();
            int idDepot= newNode2.getId();
            if(idDepot==givenDepot.getIdOfDepot()) {
                if (instance.getTrip(idTrip).getIdOfEndCity() == instance.getDepot(idDepot).getIdOfCityAsDepot()) {
                    connectionTime = smallValue;//smallValue;
                }
            }
        } else if (newNode2.getNodeType()==tripType && newNode1.getNodeType()==startingDepotType) {
            int idTrip= newNode2.getId();
            int idDepot= newNode1.getId();
            if(idDepot==givenDepot.getIdOfDepot()){
                if(instance.getTrip(idTrip).getIdOfStartCity()==instance.getDepot(idDepot).getIdOfCityAsDepot()){
                    connectionTime=smallValue+instance.getTrip(idTrip).getDuration();// 2025.1.03 原来是Small Value,现在改成以这个depot做其实出发点的Trip的Duration了（但是不影响结果，因为我们只考虑从所有的trip 出发到deport的情
                }
            }

        }
        return connectionTime;
    }

    @Override
    public String toString() {
        return "NewGraph{" +"\n"+
                "\t"+"Here is the instance: " + instance +" ; "+"\n"+
                "\t"+"Here is the givenDepot: " + givenDepot +"\n"+
                '}';
    }

    public static void main(String[] args) {
        InstanceReader reader = new InstanceReader("inst_nbCity03_Size90_Day1_nbTrips025_combPer0.0_TW0.txt");
        Instance instance = reader.readFile();
        System.out.println(instance);

        NewNode newNode1 = new NewNode( instance.getDepot(0),true);
        NewNode newNode2 = new NewNode(instance.getTrip(7));
        NewNode newNode3 = new NewNode(instance.getTrip(8));
        System.out.println(newNode1);
        System.out.println(newNode2);
        System.out.println(newNode3);
        GraphRelatedToGivenDepot newGraph = new GraphRelatedToGivenDepot(instance,instance.getDepot(0));
        System.out.println("newGraph is "+ newGraph);
        System.out.println( "minTime To Depot="+newGraph.getArcWeightOnBetweenTwoNodesOnNewGraph(newNode2, newNode3));
    }
}
