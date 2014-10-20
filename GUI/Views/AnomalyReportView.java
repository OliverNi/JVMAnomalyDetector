package GUI.Views;

import AnomalyDetector.AnomalyReport;
import GUI.Events.AnomalyReportResponse;
import GUI.Events.SearchEvent;
import GUI.Listeners.AnomalyReportListener;
import GUI.LogBrowser;

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
public class AnomalyReportView extends ListView implements AnomalyReportResponse {

    public AnomalyReportView(){
        String[] periods = {"All", "Today", "This week", "This month"};
        cboxPeriod.setModel(new DefaultComboBoxModel<String>(periods));
    }

    private void populateTable(ArrayList<AnomalyReport> result){
        tableLogs.setModel(createTableModel(result));
        tableLogs.setPreferredScrollableViewportSize(new Dimension(LogBrowser.getInstance().getFrame().getWidth(),
                LogBrowser.getInstance().getFrame().getHeight()));
        tableLogs.setFillsViewportHeight(true);
        scrollTableLogs.getViewport().repaint();
        this.revalidate();
        this.repaint();
    }

    private DefaultTableModel createTableModel(ArrayList<AnomalyReport> result){
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Anomaly");
        columnNames.add("Time increase");
        columnNames.add("Memory increase(percentage)");
        columnNames.add("Memory increase(bytes)");
        columnNames.add("Error message");

        Vector<Vector<Object>> items = new Vector<>();
        for (AnomalyReport r : result){
            Vector<Object> row = new Vector<>();

            row.add(r.getAnomaly().toString());
            Date dateTime = new Date(r.getStartTimeIncrease());
            if (dateTime.getTime() > 0L)
                row.add(dateTime.toString());
            else
                row.add("");
            row.add(r.getMemIncreasePercentage());
            row.add(r.getMemIncreaseBytes());
            row.add(r.getErrorMsg());

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
    public void searchResult(ArrayList<AnomalyReport> result) {
        if (result == null){
            System.out.println("DEBUG: No results");
        }
        else{
            populateTable(result);
        }
    }
}
