package GUI.Views;

import GUI.Events.GcStatsResponse;
import GUI.Events.SearchEvent;
import GUI.Listeners.GcStatsListener;
import GUI.LogBrowser;
import Logs.GcReport;
import Logs.GcStats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

/**
 * Created by Oliver on 2014-10-15.
 */
public class GcStatsView extends ListView<GcStatsListener> implements GcStatsResponse {

    public GcStatsView(){
        String[] periods = {"All", "Today", "This week", "This month"};
        cboxPeriod.setModel(new DefaultComboBoxModel<String>(periods));
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAction(e);
            }
        });
    }

    private void searchAction(ActionEvent e){
        int port = Integer.parseInt(txtPort.getText());
        for (GcStatsListener o : this.getObservers())
            o.search(new SearchEvent(this, txtHost.getText(), port, cboxPeriod.getSelectedItem().toString()));
    }

    private void populateTable(ArrayList<GcStats> stats){
        tableLogs.setModel(createTableModel(stats));
        tableLogs.setPreferredScrollableViewportSize(new Dimension(LogBrowser.getInstance().getFrame().getWidth(),
                LogBrowser.getInstance().getFrame().getHeight()));
        tableLogs.setFillsViewportHeight(true);
        scrollTableLogs.getViewport().repaint();
        this.revalidate();
        this.repaint();
    }

    private DefaultTableModel createTableModel(ArrayList<GcStats> stats){
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Mem Used After (kb)");
        columnNames.add("Mem used before (kb)");
        columnNames.add("Time");
        columnNames.add("Col. Time (ms)");

        Vector<Vector<Object>> items = new Vector<>();
        for (GcStats s : stats){
            Vector<Object> row = new Vector<>();

            row.add(s.getMemoryUsedAfter() /1024);
            row.add(s.getMemoryUsedBefore() / 1024);
            Date dateTime = new Date(s.getTimeStamp());
            row.add(dateTime.toString());
            row.add(s.getCollectionTime());

            items.add(row);
        }

        DefaultTableModel dModel = new DefaultTableModel(items, columnNames){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        return dModel;
    }


    @Override
    public void searchResult(ArrayList<GcStats> result) {
        if (result == null){
            System.out.println("DEBUG: No results");
        }
        else{
            populateTable(result);
        }
    }
}
