package InstanceInput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSV {

    public static void main(String[] args) {
        String line = "";
        String splitBy = ",";
        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Dell\\Desktop\\csvDemo.csv"));

            line= br.readLine();//read the first line of the CSV file, which contains the column header

            while ((line = br.readLine()) != null)
            {
                String[] data = line.split(splitBy);

                //for(int i=0; i< data.length; i++){
                    //use comma as separator
                System.out.println("Emp[First Name=" + data[1] + ", Last Name=" + data[2] + ", Contact=" + data[3] + ", City= " + data[4] + "]");

                //}


            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
