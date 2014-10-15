package GUI.Controllers;

import GUI.LogBrowser;

/**
 * Created by Oliver on 2014-10-14.
 */
public class FrontController {
    public static FrontController instance = null;
    private MainController mainController = null;
    private GcReportController gcReportController = null;
    private GcStatsController gcStatsController = null;
    private AnomalyReportController anomalyReportsController = null;
    public FrontController() {
        LogBrowser.getInstance().build();
        mainController = new MainController();
        gcReportController = new GcReportController();
        gcStatsController = new GcStatsController();
        anomalyReportsController = new AnomalyReportController();
        instance = this;
        goToMainView();
    }

    public static FrontController getInstance(){
        return FrontController.instance;
    }

    public void goToMainView(){
        mainController.mainAction();
    }

    public void goToGcReportsView(){
        gcReportController.browseAction();
    }

    public void goToGcStatsView(){
        gcStatsController.browseAction();
    }

    public void goToAnomalyReportsView(){
        anomalyReportsController.browseAction();
    }

}
