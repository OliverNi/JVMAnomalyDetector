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
    private JButton buttonGcReports = new JButton("Combined Reports");
    private JButton buttonGcStats = new JButton("GcStats");

    public MainView(){
        this.setLayout(new BorderLayout());
        build();
    }

    private void build(){
        panelButtonsList.add(buttonGcStats, BorderLayout.NORTH);
        panelButtonsList.add(buttonGcReports, BorderLayout.CENTER);

        buttonGcReports.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickGcReportAction(e);
            }
        });

        this.add(panelButtonsList);
    }

    private void clickGcReportAction(ActionEvent e){
        for (MainListener o : this.getObservers())
            o.clickGcReports();
    }
    @Override
    public void changedView() {

    }
}
