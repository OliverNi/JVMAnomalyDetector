package GUI.Views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Oliver on 2014-10-15.
 */
public abstract class ListView<V> extends View<V>{
    protected JPanel topPanel = new JPanel();
    protected JTextField txtHost = new JTextField();
    protected JTextField txtPort = new JTextField();
    protected JLabel labelHost = new JLabel("Host:");
    protected JLabel labelPort = new JLabel("Port:");
    protected JLabel labelPeriod = new JLabel("Period:");
    protected JComboBox<String> cboxPeriod = new JComboBox<>();

    protected JPanel timePanel = new JPanel();
    protected JButton buttonSearch = new JButton("Search");
    protected JLabel labelStartDate = new JLabel("Start date (yyyy-MM-dd):");
    protected JLabel labelEndDate = new JLabel("End date (yyyy-MM-dd):");
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected JFormattedTextField txtStartTime = new JFormattedTextField(dateFormat);
    protected JFormattedTextField txtEndTime = new JFormattedTextField(dateFormat);

    protected JPanel centerPanel = new JPanel();
    protected JTable tableLogs = new JTable();
    protected JScrollPane scrollTableLogs = new JScrollPane();

    public ListView(){
        this.setLayout(new BorderLayout());
        build();
    }

    private void build(){
        buildTopPanel();
        buildTimePanel();
        buildCenterPanel();
    }

    private void buildTopPanel(){
        this.txtHost.setPreferredSize(new Dimension(80, 20));
        txtPort.setPreferredSize(new Dimension(80, 20));

        topPanel.add(labelHost);
        topPanel.add(txtHost);

        topPanel.add(labelPort);
        topPanel.add(txtPort);

        topPanel.add(labelPeriod);
        topPanel.add(cboxPeriod);

        this.add(topPanel, BorderLayout.NORTH);


    }

    private void buildTimePanel(){
        txtStartTime.setPreferredSize(new Dimension(80, 20));
        txtEndTime.setPreferredSize(new Dimension(80, 20));

        timePanel.add(labelStartDate);
        timePanel.add(txtStartTime);

        timePanel.add(labelEndDate);
        timePanel.add(txtEndTime);

        timePanel.add(buttonSearch);

        this.add(timePanel, BorderLayout.CENTER);

        /*buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAction(e);
            }
        });*/
    }

    private void buildCenterPanel(){
        tableLogs.setPreferredScrollableViewportSize(new Dimension(this.getWidth(), this.getHeight() /2));
        tableLogs.setFillsViewportHeight(true);
        scrollTableLogs.getViewport().add(tableLogs);
        centerPanel.add(scrollTableLogs);

        this.add(centerPanel, BorderLayout.SOUTH);
    }
}
