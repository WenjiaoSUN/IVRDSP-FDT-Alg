package InputSchedulesAndRoutesInforms;

import CombineInforms.CombineTripInform;
import Instance.Instance;
import Instance.InstanceReader;
import java.util.ArrayList;

public class InputInforms {
    private Instance instance;
    private ArrayList<VehicleRoute> vehicleRoutes;
    private ArrayList<DriverSchedule> driverSchedules;
    private ArrayList<CombineTripInform> combineInformsArrayList;

    public InputInforms(Instance instance){
        this.instance=instance;
        this.vehicleRoutes = new ArrayList<>();
        this.driverSchedules = new ArrayList<>();
        this.combineInformsArrayList=new ArrayList<>();
    }


    public void addVehicleRouteInput(VehicleRoute pathForVehicle) {
        this.vehicleRoutes.add(pathForVehicle);
    }

    public void addDriverScheduleInput(DriverSchedule pathForDriver) {
        this.driverSchedules.add(pathForDriver);
    }


    public void addCombinedInput(CombineTripInform combineTripInform){this.combineInformsArrayList.add(combineTripInform);}

    public ArrayList<VehicleRoute> getVehicleRoutes() {
        return vehicleRoutes;
    }

    public ArrayList<DriverSchedule> getDriverSchedules() {
        return driverSchedules;
    }

    public ArrayList<CombineTripInform> getCombineInformsArrayList() {
        return combineInformsArrayList;
    }

    @Override
    public String toString() {
        return "InputInforms{" +
                "instance=" + instance +
                ", vehicleRoutes=" + vehicleRoutes +
                ", driverSchedules=" + driverSchedules +
                ", combineInformsArrayList=" + combineInformsArrayList +
                '}';
    }

    public static void main(String[] args) {
    InstanceReader reader = new InstanceReader("instance");
    Instance instance= reader.readFile();

    }



}
