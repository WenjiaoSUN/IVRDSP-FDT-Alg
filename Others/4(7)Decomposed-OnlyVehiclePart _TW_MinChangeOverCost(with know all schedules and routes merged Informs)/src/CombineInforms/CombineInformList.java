package CombineInforms;



import Instance.Instance;

import java.util.ArrayList;

public class CombineInformList {
    private Instance instance;
    private ArrayList<CombineTripInform> combineTripInformArrayList;
    public CombineInformList(Instance instance){
        this.instance=instance;
        this.combineTripInformArrayList= new ArrayList<>();
    }

    public void addCombinedInform(CombineTripInform combineTripInform){
        this.combineTripInformArrayList.add(combineTripInform);
    }

}
