package GUI.Models;

import AnomalyDetector.ProcessReport;
import GUI.Events.ProcessReportResponse;
import Logs.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Oliver on 2014-10-16.
 */
public class ProcessReportModel extends Model<ProcessReportResponse> {
    Log log = new Log();
    public void getListItem(String host, int port, String period){
        ProcessReport result = log.getProcessReport(host, port);
        if (result != null) {
            for (ProcessReportResponse o : this.getObservers())
                o.searchResult(result, period);
        }
    }
}
