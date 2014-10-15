package GUI.Views;

import GUI.Events.SearchEvent;
import GUI.Listeners.ListViewListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Oliver on 2014-10-15.
 */
public abstract class ListView extends View<ListViewListener>{
    protected JPanel panelTop = new JPanel();

    protected JPanel panelSettings = new JPanel();
    protected JTextField txtHost = new JTextField();
    protected JTextField txtPort = new JTextField();
    protected JLabel labelHost = new JLabel("Host:");
    protected JLabel labelPort = new JLabel("Port:");
    protected JLabel labelPeriod = new JLabel("Period:");
    protected JComboBox<String> cboxPeriod = new JComboBox<>();

    protected JPanel panelTime = new JPanel();
    protected JButton buttonSearch = new JButton("Search");
    protected JButton buttonBack = new JButton("Back");
    protected JLabel labelStartDate = new JLabel("Start date (yyyy-MM-dd):");
    protected JLabel labelEndDate = new JLabel("End date (yyyy-MM-dd):");
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected JFormattedTextField txtStartTime = new JFormattedTextField(dateFormat);
    protected JFormattedTextField txtEndTime = new JFormattedTextField(dateFormat);

    protected JPanel panelCenter = new JPanel();
    protected JTable tableLogs = new JTable();
    protected JScrollPane scrollTableLogs = new JScrollPane();

    public ListView(){
        this.setLayout(new BorderLayout());
        build();
    }

    private void build(){
        buildTimePanel();
        buildTopPanel();
        buildCenterPanel();
    }

    private void buildTopPanel(){
        this.txtHost.setPreferredSize(new Dimension(80, 20));
        txtPort.setPreferredSize(new Dimension(80, 20));
        panelTop.setLayout(new BorderLayout());
        panelSettings.add(labelHost);
        panelSettings.add(txtHost);

        panelSettings.add(labelPort);
        panelSettings.add(txtPort);

        panelSettings.add(labelPeriod);
        panelSettings.add(cboxPeriod);

        panelTop.add(panelSettings, BorderLayout.NORTH);
        panelTop.add(panelTime, BorderLayout.SOUTH);

        this.add(panelTop, BorderLayout.NORTH);


    }

    private void buildTimePanel(){
        txtStartTime.setPreferredSize(new Dimension(80, 20));
        txtEndTime.setPreferredSize(new Dimension(80, 20));

        panelTime.add(buttonBack);

        panelTime.add(labelStartDate);
        panelTime.add(txtStartTime);

        panelTime.add(labelEndDate);
        panelTime.add(txtEndTime);

        panelTime.add(buttonSearch);

        buttonBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backAction(e);
            }
        });

        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAction(e);
            }
        });
    }

    private void buildCenterPanel(){
        tableLogs.setPreferredScrollableViewportSize(new Dimension(this.getWidth(), this.getHeight() /2));
        tableLogs.setFillsViewportHeight(true);
        scrollTableLogs.getViewport().add(tableLogs);
        panelCenter.setLayout(new BorderLayout());
        panelCenter.add(scrollTableLogs, BorderLayout.CENTER);

        this.add(panelCenter, BorderLayout.CENTER);
    }

    private void backAction(ActionEvent e){
        for (ListViewListener o : this.getObservers())
            o.mainMenu();
    }

    private void searchAction(ActionEvent e){
        int port = Integer.parseInt(txtPort.getText());
        for (ListViewListener o : this.getObservers())
            o.search(new SearchEvent(this, txtHost.getText(), port, cboxPeriod.getSelectedItem().toString()));
    }
}
