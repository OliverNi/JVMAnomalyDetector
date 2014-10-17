package GUI.Views;

import AnomalyDetector.ProcessReport;
import GUI.Events.ProcessReportResponse;
import GUI.LogBrowser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Vector;

/**
 * Created by Oliver on 2014-10-16.
 */
public class ProcessReportView extends ListView implements ProcessReportResponse {

    public ProcessReportView(){
        String[] periods = {"Daily stats", "Weekly stats", "Monthly stats"};
        cboxPeriod.setModel(new DefaultComboBoxModel<String>(periods));
    }
    private void populateTable(ProcessReport report, String period){
        tableLogs.setModel(createTableModel(report, period));
        tableLogs.setPreferredScrollableViewportSize(new Dimension(LogBrowser.getInstance().getFrame().getWidth(),
                LogBrowser.getInstance().getFrame().getHeight()));
        tableLogs.setFillsViewportHeight(true);
        scrollTableLogs.getViewport().repaint();
        this.revalidate();
        this.repaint();
    }

    private DefaultTableModel createTableModel(ProcessReport report, String period){
        Vector<String> columnNames = getColumnNames(period);

        Vector<Vector<Object>> items = new Vector<>();
        if (report != null) {
            Vector<Object> row = getRow(report, period);
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

    private Vector<String> getColumnNames(String period){
        Vector<String> columnNames = new Vector<>();
        columnNames.add("First GC");
        columnNames.add("Last GC");

        columnNames.add("Consecutive increase");

        columnNames.add(period + " Avg Dif");
        columnNames.add(period + " Min Dif");
        columnNames.add(period + " Inc. Count");
        columnNames.add(period + " Dec. Count");
        columnNames.add(period + " Report Count");

        return columnNames;
    }

    private Vector<Object> getRow(ProcessReport report, String period){
        Vector<Object> row = new Vector<>();

        row.add(report.getUsageAfterFirstGc() / 1024);
        row.add(report.getUsageAfterLastGc() / 1024);
        switch (period){
            case "Daily stats":
                row.add(report.getDailyAvgMemUsageDif() + "%");
                row.add(report.getDailyMinMemUsageDif() + "%");
                row.add(report.getDailyIncreaseCount());
                row.add(report.getDailyDecreaseCount());
                row.add(report.getDailyReportCount());
                break;
            case "Weekly stats":
                row.add(report.getWeeklyAvgMemUsageDif() + "%");
                row.add(report.getWeeklyMinMemUsageDif() + "%");
                row.add(report.getWeeklyIncreaseCount());
                row.add(report.getWeeklyDecreaseCount());
                row.add(report.getWeeklyReportCount());
                break;
            case "Monthly stats":
                row.add(report.getMonthlyAvgMemUsageDif() + "%");
                row.add(report.getMonthlyMinMemUsageDif() + "%");
                row.add(report.getMonthlyIncreaseCount());
                row.add(report.getMonthlyDecreaseCount());
                row.add(report.getMonthlyReportCount());
                break;
        }

        return row;
    }

    @Override
    public void searchResult(ProcessReport result, String period) {
        if (result == null){
            populateTable(null, period);
            System.out.println("DEBUG: No results");
        }
        else{
            populateTable(result, period);
        }
    }
}
