package GUI.Controllers;

import GUI.Events.ListUpdatedEvent;
import GUI.Events.SearchEvent;
import GUI.Listeners.GcReportListener;
import GUI.LogBrowser;
import GUI.Models.GcReportModel;
import GUI.Views.GcReportView;
import Logs.GcReport;
import Logs.Log;

/**
 * Created by Oliver on 2014-10-14.
 */
public class GcReportController implements GcReportListener {
    private GcReportView view;
    private GcReportModel model;

    public GcReportController(){
        this.model = new GcReportModel();
        this.view = new GcReportView();
        this.model.subscribe(this.view);
        this.view.subscribe(this);

        LogBrowser.getInstance().add(this.view);
    }

    public void browseAction() {
        LogBrowser.getInstance().show(this.view);
    }

    @Override
    public void search(SearchEvent e) {
        //@TODO Send to model
        model.getListItems(e.getHost(), e.getPort(), e.getPeriod());
    }

    @Override
    public void listUpdated(ListUpdatedEvent e) {

    }
}
