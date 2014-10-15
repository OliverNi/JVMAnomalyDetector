package GUI.Models;

import GUI.Events.GcStatsResponse;
import Logs.GcStats;
import Logs.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Oliver on 2014-10-15.
 */
public class GcStatsModel extends Model<GcStatsResponse>{
    Log log = new Log();

    public GcStatsModel(){

    }

    public void getListItems(String host, int port, String period){
        ArrayList<GcStats> stats = new ArrayList<>();
        switch(period){
            case "All":
                stats = log.getGarbageCollectionStats(0L, Calendar.getInstance().getTimeInMillis(), host, port);
                break;
            case "Today":
                break;
            case "This week":
                break;
            case "This month":
                break;
        }
        if (stats != null) {
            for (GcStatsResponse o : this.getObservers())
                o.searchResult(stats);
        }
    }
}
