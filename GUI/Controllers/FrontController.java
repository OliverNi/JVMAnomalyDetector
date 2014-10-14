package GUI.Controllers;

/**
 * Created by Oliver on 2014-10-14.
 */
public class FrontController {
    public static FrontController instance;

    public FrontController() {
        (new GcReportController()).browseAction();
        instance = this;
    }

    public static FrontController getInstance(){
        return FrontController.instance;
    }

    /*
    public void browseAction() {
        (new GcReportController()).browseAction();
    }*/
}
