package GUI.Models;

import AnomalyDetector.AnomalyReport;
import GUI.Events.AnomalyReportResponse;
import Logs.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Oliver on 2014-10-15.
 */
public class AnomalyReportModel extends Model<AnomalyReportResponse> {
    Log log = new Log(); //@TODO Singleton log?
    public void getListItems(String host, int port, String period){
        ArrayList<AnomalyReport> result = new ArrayList<>();
        switch(period){
            case "All":
                result = log.getAnomalyReport(host, port);
                break;
            case "Today":
                break;
            case "This week":
                break;
            case "This month":
                break;
        }
        if (result != null) {
            for (AnomalyReportResponse o : this.getObservers())
                o.searchResult(result);
        }
    }
}
