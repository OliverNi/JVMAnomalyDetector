package GUI.Views;

import GUI.Events.MainResponse;
import GUI.Listeners.MainListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Oliver on 2014-10-14.
 */
public class MainView extends View<MainListener> implements MainResponse {
    private JPanel panelButtonsList = new JPanel();
    private JButton buttonAnomalyReports = new JButton("Anomaly Reports");
    private JButton buttonGcReports = new JButton("Combined Reports");
    private JButton buttonGcStats = new JButton("GcStats");

    public MainView(){
        this.setLayout(new BorderLayout());
        build();
    }

    private void build(){
        panelButtonsList.setLayout(new BorderLayout());
        panelButtonsList.add(buttonGcStats, BorderLayout.NORTH);
        panelButtonsList.add(buttonGcReports, BorderLayout.CENTER);
        panelButtonsList.add(buttonAnomalyReports, BorderLayout.SOUTH);
        this.add(panelButtonsList, BorderLayout.CENTER);

        buttonAnomalyReports.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickAnomalyReportsAction(e);
            }
        });

        buttonGcReports.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickGcReportAction(e);
            }
        });

        buttonGcStats.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickGcStatsAction(e);
            }
        });
        this.add(panelButtonsList);
    }

    private void clickGcReportAction(ActionEvent e){
        for (MainListener o : this.getObservers())
            o.clickGcReports();
    }

    private void clickGcStatsAction(ActionEvent e){
        for (MainListener o : this.getObservers())
            o.clickGcStats();
    }

    private void clickAnomalyReportsAction(ActionEvent e){
        for (MainListener o : this.getObservers())
            o.clickAnomalyReports();
    }
    @Override
    public void changedView() {

    }
}
