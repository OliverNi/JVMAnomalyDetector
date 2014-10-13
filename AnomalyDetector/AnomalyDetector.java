package AnomalyDetector;

import Listeners.AnomalyListener;
import Listeners.SimpleAnomalyListener;
import Logs.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


import java.util.ArrayList;

/**
 * Created by Oliver on 2014-09-10.
 */
public class AnomalyDetector {
    //@TODO Implement / remove
    public boolean ANALYZE_HOURLY_STATISTICS = false;
    public boolean ANALYZE_DAILY_STATISTICS = true;
    public boolean ANALYZE_WEEKLY_STATISTICS = true;
    public boolean ANALYZE_MONTHLY_STATISTICS = true;

    public Log getLog() {
        return log;
    }

    private Log log;
    private ArrayList<JMXAgent> agents;
    private ArrayList<ProcessConnection> connections;
    private Analyzer analyzer;
    private AnomalyListener listener;
    public AnomalyDetector(){
        this(null);
    }

    public AnomalyDetector(AnomalyListener listener){
        agents = new ArrayList<>();
        connections = new ArrayList<>();
        log = new Log();
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
        //@TODO Use threads?
        agents.add(new JMXAgent(hostName, port, this));
        if (agents.get(agents.size() -1).isConnected()){
            agents.get(agents.size()-1).setInterval(interval);
            connections.add(new ProcessConnection(hostName, port, interval));
            success = true;
            if (connections.size() == 1) //@TODO Fix better solution
                analyzer = new Analyzer(this);
        }
        else {
            agents.remove(agents.size() - 1);
            success = false;
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

            do{
                Scanner in = new Scanner(System.in);
                try {
                    System.out.println(command(in.next(), ad));
                } catch (NoSuchElementException e){
                    e.printStackTrace();
                }
                in.close();
            } while(cmdOutput.equals("Shutting down"));
        }
    }

    private static String command(String cmd, AnomalyDetector ad){
        String output = "";
        String[] cmds = cmd.split(" -");
        String cmdMain = cmds[0];
        String cmdParam = "";
        if (cmds.length > 1)
            cmdParam = cmds[1];
        switch (cmdMain){
            case "help":{
                output = "clear: Clears database of all log entries \n";
                output += " Paramers: -HOST:PORT \n";
                output += "quit: Shuts down program \n";
                break;
            }
            case "clear":
                if (cmdParam.equals("all")){
                    output = "All rows in all tables cleared";
                    ad.getLog().clearData();
                }
                else{
                    String[] connections = cmdParam.split(",");
                    for (int i = 0; i < connections.length; i++){
                        ad.getLog().clearData(new ArrayList<String>(Arrays.asList(connections)));
                    }
                    output = "Clearing alla rows in all tables for specified connections";
                }
                break;
            case "quit":
                output = "Shutting down";
                break;

            //@TODO Implement CLI commands for example show anomaly reports for a process.
            //@TODO (Additional feature) Implement command for opening LogBrowser(GUI).
            //@TODO Set limit (Threshold)
        }
        return output;
    }


}
