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
        ArrayList<GcReport> reports = null;
        switch(period){
            case "All":
                reports = log.getGcReports(0L, Long.MAX_VALUE, new ProcessConnection(host, port));
                break;
            case "Daily":
                break;
            case "Weekly":
                break;
            case "Monthly":
                break;
            case "Possible leaks":
                reports = log.getPossibleMemoryLeaks(host, port);
                break;
        }
        //Inform observers
        if (reports != null) {
            for (GcReportResponse g : this.getObservers())
                g.searchResult(reports);
        }
        else{
            for (GcReportResponse g : this.getObservers())
                g.searchResult(new ArrayList<GcReport>());
        }

    }
}
