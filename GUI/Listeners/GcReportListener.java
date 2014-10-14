package GUI.Listeners;

import GUI.Events.ListUpdatedEvent;
import GUI.Events.SearchEvent;

/**
 * Created by Oliver on 2014-10-14.
 */
public interface GcReportListener {
    public void search(SearchEvent e);
    public void listUpdated(ListUpdatedEvent e);
}
