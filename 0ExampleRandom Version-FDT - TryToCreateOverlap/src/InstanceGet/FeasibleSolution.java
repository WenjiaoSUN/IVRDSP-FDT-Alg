package InstanceGet;

import InstanceInput.Input;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
public class FeasibleSolution {
    private int totalCost;
    private Input input;
    private LinkedList<Shift> shifts;
    public FeasibleSolution(Input input){
        this.input=input;
        this.shifts=new LinkedList<>();

    }

    public LinkedList<Shift> getShifts() {
        return shifts;
    }

    public void addShift(Shift shift){
        this.shifts.add(shift);
    }



    @Override
    public String toString() {
        return "FeasibleSolution{" +
                "input=" + input +
                ", shifts=" + shifts +
                '}';
    }
}
