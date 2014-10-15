package GUI.Controllers;

import GUI.Events.SearchEvent;
import GUI.Listeners.AnomalyReportListener;
import GUI.LogBrowser;
import GUI.Models.AnomalyReportModel;
import GUI.Views.AnomalyReportView;

/**
 * Created by Oliver on 2014-10-15.
 */
public class AnomalyReportController implements AnomalyReportListener {
    private AnomalyReportView view;
    private AnomalyReportModel model;
    public AnomalyReportController(){
        this.model = new AnomalyReportModel();
        this.view = new AnomalyReportView();
        this.model.subscribe(this.view);
        this.view.subscribe(this);

        LogBrowser.getInstance().add(this.view);
    }

    public void browseAction(){
        LogBrowser.getInstance().show(this.view);
    }

    @Override
    public void search(SearchEvent e) {
        model.getListItems(e.getHost(), e.getPort(), e.getPeriod());
    }
}
