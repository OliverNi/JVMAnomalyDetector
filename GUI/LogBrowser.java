package GUI;


import GUI.Views.GcReportView;
import GUI.Views.MainView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Oliver on 2014-10-14.
 */
public class LogBrowser{
    private static JFrame frame = new JFrame();
    private static LogBrowser instance = new LogBrowser();

    CardLayout cl = new CardLayout();
    private JPanel panel = new JPanel();

    private LogBrowser(){
        frame.setSize(800, 600);
        frame.setResizable(true);
    }

    public static LogBrowser getInstance(){
        return LogBrowser.instance;
    }

    public void build(){
        panel.removeAll();
        cl.removeLayoutComponent(panel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        this.panel.setLayout(this.cl);
        frame.getContentPane().add(this.panel);
    }

    public JFrame getFrame(){
        return frame;
    }

    public void add (Component component){
        this.panel.add(component, component.getClass().getSimpleName());
    }

    public void show(Component component){
        this.cl.show(this.panel, component.getClass().getSimpleName());
    }
}
