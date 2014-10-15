package GUI.Controllers;

import GUI.Events.SearchEvent;
import GUI.Listeners.GcStatsListener;
import GUI.LogBrowser;
import GUI.Models.GcStatsModel;
import GUI.Views.GcStatsView;

/**
 * Created by Oliver on 2014-10-15.
 */
public class GcStatsController implements GcStatsListener {
    private GcStatsView view;
    private GcStatsModel model;
    public GcStatsController(){
        this.model = new GcStatsModel();
        this.view = new GcStatsView();
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
