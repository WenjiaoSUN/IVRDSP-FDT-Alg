package NewGraph;

import java.util.BitSet;

public class Test {
    public static void main(String[] args) {
        BitSet set1 = new BitSet(100);
        set1.set(10);
        set1.set(15);
        System.out.println("set1: " + set1);
        BitSet set2 = new BitSet(100);

//        set2=(BitSet) set1.clone();
        set2.set(20);
        set2.set(10);
        set2.set(15);
        System.out.println("set2: " + set2);

        BitSet interSection =(BitSet) set2.clone();
        interSection.and(set1);
        System.out.println("set interSection" +interSection);
        System.out.println(!interSection.equals(set2));
        System.out.println(interSection.equals(set1));



        set1=(BitSet) set2.clone();
        System.out.println("set1: " + set1);
        System.out.println("set2: " + set2);

        BitSet set3= (BitSet) set2.clone();
        set3.and(set1);
        boolean whetherSet2ContainsAllSet1=set3.equals(set1);

        System.out.println("check whether set1 contains all set2" +set1.intersects (set2));
        System.out.println("check whether all the set 2 be covered by set 1" +whetherSet2ContainsAllSet1);
    }
}
