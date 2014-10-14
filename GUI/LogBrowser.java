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
    private static LogBrowser instance = null;

    CardLayout cl = new CardLayout();
    private JPanel panel = new JPanel();

    private LogBrowser(){
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        this.panel.setLayout(this.cl);
        frame.getContentPane().add(this.panel, BorderLayout.PAGE_START);
        System.out.println("DEBUG: LogBrowser created");
    }

    public static LogBrowser getInstance(){
        return LogBrowser.instance;
    }

    public static void createInstance(){
        instance = new LogBrowser();
    }

    public JFrame getFrame(){
        return frame;
    }

    public void add (Component component){
        this.panel.add(component, component.getClass().getSimpleName());
        this.cl.show(this.panel, component.getClass().getSimpleName());
        frame.setSize(800, 600);
        frame.setResizable(true);
        System.out.println("DEBUG: Something added");
    }
}
