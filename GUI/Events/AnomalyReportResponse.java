package GUI.Events;

import AnomalyDetector.AnomalyReport;

import java.util.ArrayList;

/**
 * Created by Oliver on 2014-10-15.
 */
public interface AnomalyReportResponse {
    public void searchResult(ArrayList<AnomalyReport> result);
}
