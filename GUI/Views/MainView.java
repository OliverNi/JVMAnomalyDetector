package GUI.Views;

import GUI.Events.MainResponse;
import GUI.GridBagUtilities;
import GUI.Listeners.MainListener;
import GUI.LogBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Oliver on 2014-10-14.
 */
public class MainView extends View<MainListener> implements MainResponse {
    private JButton buttonProcessReports = new JButton("Process Reports");
    private JButton buttonAnomalyReports = new JButton("Anomaly Reports");
    private JButton buttonGcReports = new JButton("Combined Reports");
    private JButton buttonGcStats = new JButton("GcStats");

    public MainView(){
        this.setLayout(new GridBagLayout());
        build();
    }

    private void build(){
        GridBagUtilities.makeCell(this, buttonAnomalyReports, new Point(0, 0), 0, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);
        GridBagUtilities.makeCell(this, buttonProcessReports, new Point(0, 1), 0, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);
        GridBagUtilities.makeCell(this, buttonGcReports, new Point(0, 2), 0, 0, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);
        GridBagUtilities.makeCell(this, buttonGcStats, new Point(0, 3), 0, 1, 0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);

        buttonProcessReports.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickProcessReportsAction(e);
            }
        });

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

    private void clickProcessReportsAction(ActionEvent e){
        for (MainListener o : this.getObservers()){
            o.clickProcessReports();
        }
    }
    @Override
    public void changedView() {
        this.revalidate();
        this.repaint();
    }
}
