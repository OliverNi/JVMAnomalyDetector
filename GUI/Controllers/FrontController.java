package GUI.Controllers;

import GUI.LogBrowser;

/**
 * Created by Oliver on 2014-10-14.
 */
public class FrontController {
    public static FrontController instance = null;
    private MainController mainController = null;
    private GcReportController gcReportController = null;
    public FrontController() {
        LogBrowser.getInstance().build();
        mainController = new MainController();
        gcReportController = new GcReportController();
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
        //@TODO Implement
    }

}
