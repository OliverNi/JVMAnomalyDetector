package AnomalyDetector;

import GUI.Controllers.FrontController;
import Listeners.AnomalyListener;
import Listeners.SimpleAnomalyListener;
import Logs.Log;

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
    private ArrayList<AnomalyListener> listeners = new ArrayList<>();
    private SocketListener socketListener;

    /**
     * Get the AnomalyDetector's Analyzer object
     * @return the Analyzer object used by the AnomalyDetector
     */
    public Analyzer getAnalyzer() {
        return analyzer;
    }

    /**
     * Create an AnomalyDetector which can monitor several processes. It will gather and analyze memory statistics.
     */
    public AnomalyDetector(){
        this(null);
    }

    /**
     * Create an AnomalyDetector which can monitor several processes. It will gather and analyze memory statistics.
     * @param listener an AnomalyListener which will receive notifications if an Anomaly is found.
     */
    public AnomalyDetector(AnomalyListener listener){
        agents = new ArrayList<>();
        connections = new ArrayList<>();
        log = Log.getInstance();
        log.clearData(); //@TODO remove later (used for presentation)
        analyzer = new Analyzer(this);
        this.listeners.add(listener);
        socketListener = new SocketListener(this);
        Thread thread = new Thread(socketListener);
        thread.start();
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
        for (JMXAgent a : agents){
            //Replace agent if there already exists an agent for this connection
            if (a.getHostName().equals(hostName) && a.getPort() == port)
                a = new JMXAgent(hostName, port, this);
        }
        agents.add(new JMXAgent(hostName, port, this));

        Thread thread = new Thread(agents.get(agents.size()-1));
        thread.start();

        connections.add(new ProcessConnection(hostName, port, interval));
        analyzer.addIntervalTimer(hostName, port, interval);

        if (agents.get(agents.size()-1).isConnected())
            sendViaSocket("Connected to: " + hostName + ":" + port + " ...");

        return agents.get(agents.size()-1).isConnected();
    }

    /**
     * Disconnects connection to a monitored process.
     * @param hostName hostname for the process
     * @param port port for the process
     * @return IF PROCESS WAS DISCONNECTED: TRUE ELSE: FALSE
     */
    public boolean disconnect(String hostName, int port){
        boolean disconnected = false;
        for (JMXAgent a : agents)
        {
            if (a.getHostName().equals(hostName) && a.getPort() == port) {
                agents.remove(a);
                analyzer.removeTimer(hostName, port);
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

    /**
     * Get information for all active monitored program connections.
     * @return a list of ProcessConnections containing information about connections
     */
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

    /**
     * Get a list of listeners
     * @return a list of listeners
     */
    public ArrayList<AnomalyListener> getListeners(){
        return listeners;
    }

    /**
     * Add a listener which will receive Anomaly reports.
     * @param listener the listener
     */
    public void addListener(AnomalyListener listener){
        listeners.add(listener);
    }

    public void setThreshold(double threshold){
        //@TODO Implement
    }

    public static void main(String args[]){
        //java AnomalyDetector hostname:port, hostname:port 20, hostname:port
        ArrayList<ProcessConnection> pConnections = new ArrayList<>();
        if (args.length != 0) {
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
        }
            SimpleAnomalyListener listener = new SimpleAnomalyListener();
            AnomalyDetector ad = new AnomalyDetector(listener);
            for (ProcessConnection p : pConnections) {
                ad.connect(p.getHostName(), p.getPort(), p.getInterval());
            }

            String cmdOutput = "";
            Scanner in = new Scanner(System.in);
            do
            {
                if (in.hasNext()) {
                    String cmdInput = in.nextLine();
                    cmdOutput = ad.command(cmdInput);
                    System.out.println(cmdOutput);
                }
            } while(!cmdOutput.equals("Shutting down"));
            in.close();
    }

    /**
     * Commands for the program to do something
     * @param cmd the command
     * @return Output text
     */
    public String command(String cmd){
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

                output += "connect (Connects to a JVM process (EXAMPLE connect -localhost:1111, locahlhost:1212, localhost:1313)) \n";
                output += "Parameters: \n";
                output += "-HOST:PORT \n";
                output += "-HOST:PORT, ...., HOST:PORT \n \n";


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
                        setThreshold(t);
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
            case "connect":
                //HOST:PORT
                String[] connections = cmdParam.split(", ");
                for (String s : connections){
                    if (s.contains(":")) {
                        String[] hostNPort = s.split(":");
                        String host = hostNPort[0];
                        int port = Integer.parseInt(hostNPort[1]);
                        connect(host, port);
                    }
                    else
                        System.out.println("Format error when trying to connect");
                        //@TODO send "Wrong format connection"

                }
                output = "Connecting...";
                break;
            case "quit":
                output = "Shutting down";
                break;
            default:
                output = "Wrong command";
                break;

            //@TODO Implement CLI commands
            //@TODO Current settings
            //Java 7 autoclose
        }
        return output;
    }

    /**
     * Prints to both local command line and connections
     * @param text text to be printed
     */
    public void print(String text){
        System.out.println(text);
        sendViaSocket(text);
    }

    /**
     * Sends a text to all socket-connections
     * @param text text to be sent.
     */
    public void sendViaSocket(String text){
        this.socketListener.send(text);
    }

}
