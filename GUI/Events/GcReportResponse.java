package GUI.Events;

import Logs.GcReport;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Oliver on 2014-10-14.
 */
public interface GcReportResponse {

    public void searchResult(ArrayList<GcReport> reports);

    //public void updateLists(long startTime, long endTime);
}
