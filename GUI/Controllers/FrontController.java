package GUI.Controllers;

import GUI.LogBrowser;

/**
 * Created by Oliver on 2014-10-14.
 */
public class FrontController {
    public static FrontController instance;

    public FrontController() {
        LogBrowser.createInstance();
        instance = this;
        (new GcReportController()).browseAction();
    }

    public static FrontController getInstance(){
        return FrontController.instance;
    }

    /*
    public void browseAction() {
        (new GcReportController()).browseAction();
    }*/
}
