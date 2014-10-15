package GUI.Listeners;

import GUI.Events.ListUpdatedEvent;

/**
 * Created by Oliver on 2014-10-14.
 */
public interface GcReportListener extends ListViewListener {
    //public void search(SearchEvent e);
   // public void mainMenu();
    public void listUpdated(ListUpdatedEvent e);
}
