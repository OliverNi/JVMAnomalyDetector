package GUI.Models;

import AnomalyDetector.AnomalyReport;
import GUI.Events.AnomalyReportResponse;
import Logs.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Oliver on 2014-10-15.
 */
public class AnomalyReportModel extends Model<AnomalyReportResponse> {
    Log log = Log.getInstance();
    public void getListItems(String host, int port, String period){
        ArrayList<AnomalyReport> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        switch(period){
            case "All":
                result = log.getAnomalyReports(host, port);
                break;
            case "Today": {
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                Date start = cal.getTime();
                cal.set(Calendar.HOUR, 23);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                Date end = cal.getTime();

                result = log.getAnomalyReports(start.getTime(), end.getTime(), host, port);
                break;
            }
            case "This week": {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                Date start = cal.getTime();
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                cal.set(Calendar.HOUR, 23);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                Date end = cal.getTime();

                result = log.getAnomalyReports(start.getTime(), end.getTime(), host, port);
                break;
            }
            case "This month": {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                Date start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                cal.set(Calendar.HOUR, 23);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MINUTE, 59);
                Date end = cal.getTime();

                result = log.getAnomalyReports(start.getTime(), end.getTime(), host, port);
                break;
            }
        }
        if (result != null) {
            for (AnomalyReportResponse o : this.getObservers())
                o.searchResult(result);
        }
        else {
            for (AnomalyReportResponse o : this.getObservers())
                o.searchResult(new ArrayList<AnomalyReport>());
        }
    }
}
