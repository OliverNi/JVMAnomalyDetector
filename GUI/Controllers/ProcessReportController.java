package GUI.Controllers;

import GUI.Events.SearchEvent;
import GUI.Listeners.ProcessReportListener;
import GUI.LogBrowser;
import GUI.Models.ProcessReportModel;
import GUI.Views.ProcessReportView;

/**
 * Created by Oliver on 2014-10-16.
 */
public class ProcessReportController implements ProcessReportListener {
    private ProcessReportView view;
    private ProcessReportModel model;

    public ProcessReportController(){
        this.model = new ProcessReportModel();
        this.view = new ProcessReportView();
        this.model.subscribe(this.view);
        this.view.subscribe(this);

        LogBrowser.getInstance().add(this.view);
    }

    public void browseAction(){
        LogBrowser.getInstance().show(this.view);
    }
    @Override
    public void search(SearchEvent e) {
        model.getListItem(e.getHost(), e.getPort(), e.getPeriod());
    }

    @Override
    public void mainMenu() {
        FrontController.getInstance().goToMainView();
    }
}
