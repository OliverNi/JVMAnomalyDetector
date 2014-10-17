package GUI.Models;

import AnomalyDetector.ProcessConnection;
import GUI.Events.GcReportResponse;
import GUI.Listeners.GcReportListener;
import Logs.GcReport;
import Logs.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Oliver on 2014-10-14.
 */
public class GcReportModel extends Model<GcReportResponse>{
    Log log = new Log();

    public GcReportModel(){

    }

    public void getListItems(String host, int port, String period){
        ArrayList<GcReport> result = null;
        switch(period){
            case "All":
                result = log.getGcReports(0L, Long.MAX_VALUE, new ProcessConnection(host, port));
                break;
            case "Daily":
                result = log.getGcReports(0L, Long.MAX_VALUE, GcReport.Period.DAILY, new ProcessConnection(host, port));
                break;
            case "Weekly":
                result = log.getGcReports(0L, Long.MAX_VALUE, GcReport.Period.WEEKLY, new ProcessConnection(host, port));
                break;
            case "Monthly":
                result = log.getGcReports(0L, Long.MAX_VALUE, GcReport.Period.MONTHLY, new ProcessConnection(host, port));
                break;
            case "Possible leaks":
                result = log.getPossibleMemoryLeaks(host, port);
                break;
        }
        //Inform observers
        if (result != null) {
            for (GcReportResponse g : this.getObservers())
                g.searchResult(result);
        }
        else{
            for (GcReportResponse g : this.getObservers())
                g.searchResult(new ArrayList<GcReport>());
        }

    }
}
