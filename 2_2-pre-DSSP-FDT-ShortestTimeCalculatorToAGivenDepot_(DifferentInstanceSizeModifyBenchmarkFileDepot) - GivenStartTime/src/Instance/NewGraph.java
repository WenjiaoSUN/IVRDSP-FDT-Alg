package Instance;

public class NewGraph {
    private Instance instance;
    private Depot givenDepot;

    final double smallValue=0.0000001;
    final double largeValue=9999999;


    public NewGraph(Instance instance, Depot givenDepot) {
        this.instance=instance;
        this.givenDepot=givenDepot;
    }

    public Instance getInstance() {
        return instance;
    }

    public Depot getGivenDepot() {
        return givenDepot;
    }

    public double getArcWeightOnBetweenTwoNodesOnNewGraph(NewNode node1, NewNode node2) {
        String startingDepotType="startingDepot";
        String tripType="trip";
        String endingDepotType="endingDepot";

        double connectionTime=largeValue;
        if(node1.getNodeType()==tripType && node2.getNodeType()==tripType){
            if(instance.whetherHavePossibleArcAfterCleaning(node1.getId(), node2.getId())){
                connectionTime=instance.getMinWaitingTime(node1.getId(), node2.getId())+instance.getTrip(node2.getId()).getDuration();// 2025.1.03 原来只考虑connection time, 现在用minConnection + 后面Trip的Duration
            }

        } else if (node1.getNodeType()==tripType && node2.getNodeType()==endingDepotType) {
            int idTrip=node1.getId();
            int idDepot= node2.getId();
            if(idDepot==givenDepot.getIdOfDepot()) {
                if (instance.getTrip(idTrip).getIdOfEndCity() == instance.getDepot(idDepot).getIdOfCityAsDepot()) {
                    connectionTime = smallValue;//smallValue;
                }
            }
        } else if (node2.getNodeType()==tripType && node1.getNodeType()==startingDepotType) {
            int idTrip=node2.getId();
            int idDepot= node1.getId();
            if(idDepot==givenDepot.getIdOfDepot()){
                if(instance.getTrip(idTrip).getIdOfStartCity()==instance.getDepot(idDepot).getIdOfCityAsDepot()){
                    connectionTime=instance.getTrip(idTrip).getDuration();// 2025.1.03 原来是Small Value,现在改成以这个depot做其实出发点的Trip的Duration了
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

        NewNode node1 = new NewNode( instance.getDepot(0),true);
        NewNode node2 = new NewNode(instance.getTrip(7));
        NewNode node3= new NewNode(instance.getTrip(8));
        System.out.println(node1);
        System.out.println(node2);
        System.out.println(node3);
        NewGraph newGraph = new NewGraph(instance,instance.getDepot(0));
        System.out.println("newGraph is "+ newGraph);
        System.out.println( "min conT="+newGraph.getArcWeightOnBetweenTwoNodesOnNewGraph(node2,node3));
    }
}
