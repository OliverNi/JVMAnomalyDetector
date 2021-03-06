package GUI.Views;

import GUI.Events.GcReportResponse;
import GUI.Events.SearchEvent;
import GUI.Listeners.GcReportListener;
import GUI.LogBrowser;
import Logs.GcReport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Oliver on 2014-10-14.
 */
public class GcReportView extends ListView implements GcReportResponse {


    public GcReportView(){
        String[] periods = {"All", "Daily", "Weekly", "Monthly", "Possible Leaks"};
        cboxPeriod.setModel(new DefaultComboBoxModel<String>(periods));
    }

    private void populateTable(ArrayList<GcReport> reports){
        tableLogs.setModel(createTableModel(reports));
        tableLogs.setPreferredScrollableViewportSize(new Dimension((int)LogBrowser.getInstance().getFrame().getBounds().getWidth(),
                (int)LogBrowser.getInstance().getFrame().getBounds().getHeight()));
        tableLogs.setFillsViewportHeight(true);
        scrollTableLogs.getViewport().repaint();
        this.revalidate();
        this.repaint();
    }

    private DefaultTableModel createTableModel(ArrayList<GcReport> reports){
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Avg Col Time");
        columnNames.add("Min Col Time");
        columnNames.add("Max Col Time");

        columnNames.add("Avg Time Between Gc");
        columnNames.add("Min Time Between Gc");
        columnNames.add("Max Time between Gc");

        columnNames.add("Avg Col");
        columnNames.add("Min Col");
        columnNames.add("Max Col");

        columnNames.add("Avg Mem Use");
        columnNames.add("Min Mem Use");
        columnNames.add("Max Mem Use");

        columnNames.add("Start Mem Use");
        columnNames.add("End Mem Use");

        columnNames.add("Start Time");
        columnNames.add("End Time");

        columnNames.add("Gc Count");

        Vector<Vector<Object>> items = new Vector<>();
        for (GcReport g : reports){
            Vector<Object> row = new Vector<>();
            row.add(g.getAvgCollectionTime());
            row.add(g.getMinCollectionTime());
            row.add(g.getMaxCollectionTime());

            row.add(g.getAvgTimeBetweenGc());
            row.add(g.getMinTimeBetweenGc());
            row.add(g.getMaxTimeBetweenGc());

            row.add(g.getAvgCollected());
            row.add(g.getMinCollected());
            row.add(g.getMaxCollected());

            row.add(g.getAvgMemoryUsage());
            row.add(g.getMinMemoryUsage());
            row.add(g.getMaxMemoryUsage());

            row.add(g.getStartMemoryUsage());
            row.add(g.getEndMemoryUsage());

            row.add(g.getStartTime());
            row.add(g.getEndTime());

            row.add(g.getGcCount());

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
    public void searchResult(ArrayList<GcReport> reports) {
        if (reports == null){
            System.out.println("DEBUG: No results");
        }
        else{
            populateTable(reports);
        }
    }
}
