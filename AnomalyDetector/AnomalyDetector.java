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
        int index = 0;
        int agentIndex = -1;

        while (index < agents.size() && agentIndex == -1){
            if (agents.get(index).getHostName().equals(hostName) && agents.get(index).getPort() == port) {
                agentIndex = index;
                analyzer.removeTimer(hostName, port);
                ProcessConnection remove = null;
                for (ProcessConnection p : connections){
                    if (p.getPort() == port && p.getHostName().equals(hostName))
                        remove = p;
                }
                connections.remove(remove);
            }
        }

        if (agentIndex != -1){
            print("Disconnected from: " + agents.get(index).getHostName() + ":" + agents.get(index).getPort());
            disconnected = true;
            agents.remove(agentIndex);
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
        Analyzer.setPERCENTAGE_INC_IN_MEM_USE_WARNING(threshold);
    }

    public void setExcessiveGcTime(long threshold){
        Analyzer.setTIME_EXCESSIVE_SCAN_WARNING(threshold);
    }

    public void setInterval(String host, int port, int interval){
        for (ProcessConnection c : connections){
            if (c.getHostName().equals(host) && c.getPort() == port)
                c.setInterval(interval);
        }
        analyzer.removeTimer(host, port);
        analyzer.addIntervalTimer(host, port, interval);
    }

    private String getConnectionStatus(String host, int port){
        String status = "No connection";
        for (JMXAgent a : agents){
            if (a.getHostName().equals(host) && a.getPort() == port){
                if (a.isConnected())
                    status = "Connected";
                else
                    status = "Connection failure";
            }
        }

        return status;
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

                output += "connect (Connects to a JVM process (EXAMPLE: connect -localhost:1111, locahlhost:1212, localhost:1313)) \n";
                output += "Parameters: \n";
                output += "-HOST:PORT \n";
                output += "-HOST:PORT, ...., HOST:PORT \n \n";

                output += "disconnect (Disconnects from a monitored process (EXAMPLE: disconnect -localhost:1111, locahlhost:1212, localhost:1313))";
                output += "Parameters: \n";
                output += "-HOST:PORT \n";
                output += "-HOST:PORT, ...., HOST:PORT \n \n";

                output += "settings (Displays settings (EXAMPLE: settings))\n\n";

                output += "excessivegc (Sets Excessive GC Time Warning in milliseconds (EXAMPLE: excessivegc -1000)\n";
                output += "Parameters: \n";
                output += "-MILLISECONDS \n \n";

                output += "threshold (Sets memory increase warning threshold in percent (EXAMPLE: threshold -10))";
                output += "Parameters: \n";
                output += "-DOUBLE \n \n";

                output += "connections (Displays all connections and their status (EXAMPLE: connections))";

                output += "setinterval (Set analysis interval in minutes for spec. process (EXAMPLE: setinterval -localhost:3500:5))";
                output += "Parameters: \n";
                output += "-HOST:PORT:INTEGER \n \n";

                output += "anomaly (Get all anomalies for one process (EXAMPLE: anomaly -localhost:3500))";
                output += "Parameters: \n";
                output += "-HOST:PORT \n\n";

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
            case "disconnect": {
                //HOST:PORT
                String[] connections = cmdParam.split(", ");
                for (String s : connections) {
                    if (s.contains(":")) {
                        String[] hostNPort = s.split(":");
                        String host = hostNPort[0];
                        int port = Integer.parseInt(hostNPort[1]);
                        disconnect(host, port);
                    } else
                        print("Wrong format for connection. Use HOST:PORT");
                }
                break;
            }
            case "browse":
                new Runnable(){
                    @Override
                public void run(){
                        new FrontController();
                    }
                }.run();
                break;
            case "connect": {
                //HOST:PORT
                String[] connections = cmdParam.split(", ");
                for (String s : connections) {
                    if (s.contains(":")) {
                        String[] hostNPort = s.split(":");
                        String host = hostNPort[0];
                        int port = Integer.parseInt(hostNPort[1]);
                        connect(host, port);
                    } else
                        print("Wrong format for connection. Use HOST:PORT");
                }
                break;
            }
            case "settings":
                output += "Excessive GC Scan threshold: " + Analyzer.getTIME_EXCESSIVE_SCAN_WARNING() + "\n";
                output += "Memory increase threshold: " + Analyzer.getPERCENTAGE_INC_IN_MEM_USE_WARNING() + "\n";
                for (ProcessConnection c : connections){
                    output += c.getHostName() + ":" + c.getPort() + ":\n";
                    output += "Analyzer interval: " + c.getInterval() + "min \n";
                }
                break;
            case "excessivegc": {
                long time = -1;
                try {
                    time = Long.parseLong(cmdParam);
                    setExcessiveGcTime(time);
                    output = "Excessive GC Time Warning set to " + time + " ms";
                }
                catch (Exception e){
                    e.printStackTrace();
                    output = "Format error";
                }

                break;
            }
            case "connections":
                for (ProcessConnection c : connections) {
                    output += c.getHostName() + ":" + c.getPort() + ": ";
                    output += getConnectionStatus(c.getHostName(), c.getPort()) + "\n";
                }
                break;
            case "setinterval": {
                String[] params = cmdParam.split(":");
                String host = params[0];
                int port = Integer.parseInt(params[1]);
                int interval = Integer.parseInt(params[2]);
                setInterval(host, port, interval);
                break;
            }
            case "anomaly":{
                String[] params = cmdParam.split(":");
                String host = params[0];
                int port = Integer.parseInt(params[1]);
                ArrayList<AnomalyReport> aReports = log.getAnomalyReports(host, port);
                for (AnomalyReport a : aReports)
                    output += a.toString();
            }
            break;
            case "quit":
                output = "Shutting down";
                break;
            default:
                output = "Wrong command";
                break;
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
