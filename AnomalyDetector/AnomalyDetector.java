package AnomalyDetector;

//import GUI.Controllers.FrontController;
//import GUI.LogBrowser;
import GUI.Controllers.FrontController;
import Listeners.AnomalyListener;
import Listeners.SimpleAnomalyListener;
import Logs.GcReport;
import Logs.Log;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


import java.util.ArrayList;

/**
 * Created by Oliver on 2014-09-10.
 */
public class AnomalyDetector {
    private Log log;
    private ArrayList<JMXAgent> agents;
    private ArrayList<ProcessConnection> connections;
    private Analyzer analyzer;
    private AnomalyListener listener;

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public AnomalyDetector(){
        this(null);
    }

    public AnomalyDetector(AnomalyListener listener){
        agents = new ArrayList<>();
        connections = new ArrayList<>();
        log = Log.getInstance();
        analyzer = new Analyzer(this);
        this.listener = listener;
    }

    /**
     * Connects to process, use default interval.
     * @param hostName hostname
     * @param port port
     */
    public boolean connect(String hostName, int port){
       return connect(hostName, port, ProcessConnection.DEFAULT_INTERVAL);
    }

    /**
     * Connects to process and set interval for memory statistics for that process.
     * @param hostName hostname
     * @param port port
     * @param interval interval in milliseconds decides how often memory statistics are gathered.
     */
    public boolean connect(String hostName, int port, int interval){
        boolean success = false;
        agents.add(new JMXAgent(hostName, port, this));
        (new Thread(agents.get(agents.size()-1))).start();
        //(new Thread(agents.get(agents.size()-1))).start();
        if (agents.get(agents.size()-1).isConnected()) {
            connections.add(new ProcessConnection(hostName, port, interval));
            analyzer.addIntervalTimer(hostName, port, interval);
            success = true;
        }
        return success;
    }
    //@TODO Disconnect if process shuts down?
    public boolean disconnect(String hostName, int port){
        boolean disconnected = false;
        for (JMXAgent a : agents) {
            if (a.getHostName().equals(hostName) && a.getPort() == port) {
                agents.remove(a);
                for (ProcessConnection p : connections){
                    if (p.getPort() == port && p.getHostName().equals(hostName))
                        connections.remove(p);
                }
                disconnected = true;
            }
        }
        return disconnected;
    }

    /**
     * Get connection-names and port
     * @return ArrayList<String> HOSTNAME:PORT
     */
    public ArrayList<String> getConnections(){
        ArrayList<String> connStrings = new ArrayList<>();
        for (ProcessConnection p : connections){
            connStrings.add(p.getHostName() + ":" + p.getPort());
        }
        return connStrings;
    }

    public ArrayList<ProcessConnection> getProcessConnections(){
        return connections;
    }

    /**
     * Clear all data in database
     */
    public void clearData(){
        log.clearData();
    }

    /**
     * Clears all data for the specified processes in the database.
     * @param processes ArrayList of processes in format String HOSTNAME:PORT
     */
    public void clearData(ArrayList<String> processes){
        log.clearData(processes);
    }

    /**
     * Returns interval of connection with specified hostName and port
     * @param hostName hostname
     * @param port port
     * @return IF EXISTS: interval of specified connection ELSE: -1
     */
    public int getInterval(String hostName, int port){
        for (ProcessConnection p : connections){
            if (p.getPort() == port && p.getHostName().equals(hostName))
                return p.getInterval();
        }
        return -1;
    }

    public AnomalyListener getListener(){
        return listener;
    }

    public void setThreshold(double threshold){
        //@TODO Implement
    }

    public static void main(String args[]){
        //java AnomalyDetector hostname:port, hostname:port 20, hostname:port
        if (args.length != 0) {
            ArrayList<ProcessConnection> pConnections = new ArrayList<>();
            int count = 0;
            while (count < args.length) {
                String hostNPort[] = args[count].split(":");
                String hostName = hostNPort[0];
                char lastChar = hostNPort[1].charAt(hostNPort[1].length() - 1);
                count++;
                if (lastChar == ',') {
                    //Create ProcessConnection with default interval
                    int port = Integer.parseInt(hostNPort[1].substring(0, hostNPort[1].length() - 1));
                    pConnections.add(new ProcessConnection(hostName, port));
                } else if (count < args.length) {
                    //Create ProcessConnection with specified interval.
                    int port = Integer.parseInt(hostNPort[1]);
                    int interval = 0;
                    if (args[count].charAt(args[count].length() - 1) == ',') {
                        interval = Integer.parseInt(args[count].substring(0, args[count].length() - 1));
                    } else
                        interval = Integer.parseInt(args[count]);
                    pConnections.add(new ProcessConnection(hostName, port, interval));
                    count++;
                } else {
                    int port = Integer.parseInt(hostNPort[1]);
                    pConnections.add(new ProcessConnection(hostName, port));
                }

            }
            SimpleAnomalyListener listener = new SimpleAnomalyListener();
            AnomalyDetector ad = new AnomalyDetector(listener);
            for (ProcessConnection p : pConnections) {
                ad.connect(p.getHostName(), p.getPort(), p.getInterval());
            }

            ArrayList<String> sConnections = ad.getConnections();

            for (String s : sConnections) {
                System.out.println("Connected to: " + s);
            }

            String cmdOutput = "";
            Scanner in = new Scanner(System.in);
            do
            {
                if (in.hasNext()) {
                    String cmdInput = in.nextLine();
                    cmdOutput = command(cmdInput, ad);
                    System.out.println(cmdOutput);
                }
            } while(!cmdOutput.equals("Shutting down"));
            in.close();
        }
    }

    private static String command(String cmd, AnomalyDetector ad){
        String output = "";
        String[] cmds = cmd.split(" -");
        String cmdMain = cmd.split(" -")[0];
        String cmdParam = "";
        if (cmds.length > 1)
        {
            cmdParam = cmds[1];
        }

        switch (cmdMain){

            case "help":{
                output = "Examples use: \n";
                output += "COMMAND or ";
                output += "COMMAND -PARAMETER (Some commands require a parameter others do not have any parameters)\n \n";
                output += "clear"+ " (Clears database of all log entries (EXAMPLE: clear -all)) \n";
                output += "Paramers:\n -all \n" +
                        "-HOST:PORT\n" +
                        "-HOST:PORT, ...., HOST:PORT \n \n";

                output += "browse (Opens LogBrowser (EXAMPLE: browse)) \n \n";

                output += "quit (Shuts down program (EXAMPLE: quit)) \n";
                break;
            }
            case "clear":
                if (cmdParam.equals("all"))
                {
                    output = "All rows in all tables cleared";
                    Log.getInstance().clearData();
                }
                else
                {
                    //HOST:PORT
                    String[] connections = cmdParam.split(", ");
                    Log.getInstance().clearData(new ArrayList<>(Arrays.asList(connections)));
                    output = "Clearing all rows in all tables for specified connections";
                }
                break;
            case "threshold":
                if (!cmdParam.equals("")) {
                    double t = Double.parseDouble(cmdParam);
                    if (t > 0) {
                        output = "Threshold set to: " + t + "\n";
                        ad.setThreshold(t);
                    }
                    else
                        output = "Format error when trying to set threshold.";
                }
                else
                    output = "Format error when trying to set threshold.";
                break;
            case "browse":
                new Runnable(){
                    @Override
                public void run(){
                        new FrontController();
                    }
                }.run();
                break;
            case "quit":
                output = "Shutting down";
                break;

            //@TODO Implement CLI commands for example show anomaly reports for a process.
        }
        return output;
    }


}
