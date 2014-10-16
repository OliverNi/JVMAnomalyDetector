package GUI.Events;

import AnomalyDetector.ProcessReport;

/**
 * Created by Oliver on 2014-10-16.
 */
public interface ProcessReportResponse{
    public void searchResult(ProcessReport result, String period);
}
