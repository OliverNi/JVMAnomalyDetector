package GUI.Controllers;

import GUI.LogBrowser;

/**
 * Created by Oliver on 2014-10-14.
 */
public class FrontController {
    public static FrontController instance;
    private MainController mainController = new MainController();
    private GcReportController gcReportController = new GcReportController();
    public FrontController() {
        LogBrowser.createInstance();
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
