package Generator;

import java.util.LinkedList;
import java.util.ListIterator;

public class Test {
    public static void main(String[] args) {
        LinkedList<String> list = new LinkedList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        ListIterator<String> iter = list.listIterator();
        while(iter.hasNext()) {
            String value = iter.next();
            if(value.equals("6")) {
                iter.remove();
            }
            if(value.equals("1")) {
                String prev = iter.previous();
                System.out.println("prev = " + prev);
                iter.add("new");
                String newNext = iter.next();
                System.out.println("next = " + newNext);
            }
            System.out.println(value);
        }
        System.out.println("after loop");
        iter = list.listIterator();
        while(iter.hasNext()) {
            String value = iter.next();
            System.out.println(value);
        }
    }
}
