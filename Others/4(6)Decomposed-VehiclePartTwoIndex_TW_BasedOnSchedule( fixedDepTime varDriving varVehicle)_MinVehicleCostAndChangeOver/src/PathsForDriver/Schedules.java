package PathsForDriver;

import Instance.Instance;

import java.util.ArrayList;

public class Schedules {

    private Instance instance;
    private ArrayList<DriverSchedule> driverSchedules;

    public Schedules(Instance instance) {
        this.instance = instance;
        this.driverSchedules = new ArrayList<>();
    }

    public ArrayList<DriverSchedule> getDriverSchedules() {
        return driverSchedules;
    }

    public void addSchedule(DriverSchedule driverSchedule) {
        this.driverSchedules.add(driverSchedule);
    }

    public boolean whetherIsContinuousArc(int idFirst, int idSecond){
        boolean whetherCon=false;
        for(int s=0;s<this.driverSchedules.size();s++){
            DriverSchedule driverSchedule=this.driverSchedules.get(s);
            boolean whetherConInDriverSchedule=driverSchedule.whetherArcExist(idFirst,idSecond);
            if(whetherConInDriverSchedule){
                whetherCon=true;
                break;
            }
        }
        return whetherCon;

    }


    @Override
    public String toString() {
        String string = "//Schedules: driver idDriver idDepot indexStarting tripWithWorkingStatus indexEnding \n";
        for (int s = 0; s < this.driverSchedules.size(); s++) {
            string += "driver " + s + " ";
            string += this.driverSchedules.get(s);
        }
//        for(DriverSchedule driverSchedules1: driverSchedules){
//            s+=driverSchedules1+"\n";
//        }
        return string;
    }


}
