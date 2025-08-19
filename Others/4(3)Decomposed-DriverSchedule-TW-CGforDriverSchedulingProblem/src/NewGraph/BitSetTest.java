package NewGraph;

import jdk.swing.interop.SwingInterOpUtils;

import java.util.BitSet;

public class BitSetTest {
    public static void main(String[] args) {

        // 创建BitSet S1
        BitSet S1 = new BitSet();
        S1.set(1);
        S1.set(3);
        S1.set(5);

        BitSet S2= new BitSet();
        S2.set(1);
        S2.set(3);
        S2.set(5);


        BitSet S3= new BitSet();
        S3.set(1);
        S3.set(3);

        BitSet S4= new BitSet();
        S4.set(2);

        //S2 is the subSet but not proper subset of S1
        BitSet interSection12=(BitSet) S1.clone();
        interSection12.and(S2);
        if(interSection12.equals(S2)){
            System.out.println("s2 is the subset of s1");
            if(S1.equals(S2)){
                System.out.println(" but s2 is not the proper subset");
            }
            else {
                System.out.println(" and s2 is  the proper subset");

            }
        }else {
            System.out.println("s2 is not the subset of s1");
        }

        // S3 is subset and  proper subset of S1
        BitSet interSection13=(BitSet) S1.clone();
        interSection13.and(S3);
        System.out.println("comment element of 1and 3:"+interSection13);
        if(interSection13.equals(S3)){
            System.out.println("s3 is the subset of s1");
            if(S1.equals(S3)){
                System.out.println(" but s3 is not the proper subset");
            }
            else {
                System.out.println(" and s3 is  the proper subset");

            }
        }else {
            System.out.println("s3 is not the subset of s1");
        }
        // S4 is not the subset of S1
        BitSet interSection14=(BitSet) S1.clone();
        interSection14.and(S4);
        if(interSection14.equals(S4)){
            System.out.println("s4 is the subset of s1");
            if(S1.equals(S4)){
                System.out.println(" but s4 is not the proper subset");
            }
            else {
                System.out.println(" and s4 is  the proper subset");

            }
        }else {
            System.out.println("s4 is not the subset of s1");
        }


    }
}
