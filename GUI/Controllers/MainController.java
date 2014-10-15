package GUI.Controllers;

import GUI.Listeners.MainListener;
import GUI.LogBrowser;
import GUI.Models.MainModel;
import GUI.Views.MainView;

/**
 * Created by Oliver on 2014-10-15.
 */
public class MainController implements MainListener{
    private MainView view;
    private MainModel model;

    public MainController(){
        this.model = new MainModel();
        this.view = new MainView();
        this.model.subscribe(this.view);
        this.view.subscribe(this);

        LogBrowser.getInstance().add(this.view);
    }

    public void mainAction(){
        LogBrowser.getInstance().show(this.view);
    }

    @Override
    public void clickGcStats() {
        FrontController.getInstance().goToGcStatsView();
    }

    @Override
    public void clickGcReports() {
        FrontController.getInstance().goToGcReportsView();
    }

    @Override
    public void clickAnomalyReports(){
        FrontController.getInstance().goToAnomalyReportsView();
    }
}
