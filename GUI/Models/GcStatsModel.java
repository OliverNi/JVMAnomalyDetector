package GUI.Models;

import GUI.Events.GcStatsResponse;
import Logs.GcStats;
import Logs.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Oliver on 2014-10-15.
 */
public class GcStatsModel extends Model<GcStatsResponse>{
    Log log = new Log();

    public GcStatsModel(){

    }

    public void getListItems(String host, int port, String period){
        ArrayList<GcStats> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        switch(period){
            case "All":
                result = log.getGarbageCollectionStats(0L, Calendar.getInstance().getTimeInMillis(), host, port);
                break;
            case "Today": {
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                Date todayStart = cal.getTime();
                cal.set(Calendar.HOUR, 23);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                Date todayEnd = cal.getTime();

                result = log.getGarbageCollectionStats(todayStart.getTime(), todayEnd.getTime(), host, port);
                break;
            }
            case "This week": {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                Date weekStart = cal.getTime();
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                cal.set(Calendar.HOUR, 23);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                Date weekEnd = cal.getTime();

                result = log.getGarbageCollectionStats(weekStart.getTime(), weekEnd.getTime(), host, port);
                break;
            }
            case "This month": {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                Date monthStart = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                cal.set(Calendar.HOUR, 23);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                Date monthEnd = cal.getTime();

                result = log.getGarbageCollectionStats(monthStart.getTime(), monthEnd.getTime(), host, port);
                break;
            }
        }
        if (result != null) {
            for (GcStatsResponse o : this.getObservers())
                o.searchResult(result);
        }
    }
}
