package AnomalyDetector;

import Logs.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;


import java.util.ArrayList;

/**
 * Created by Oliver on 2014-09-10.
 */
public class AnomalyDetector {

    public boolean ANALYZE_HOURLY_STATISTICS = false;
    public boolean ANALYZE_DAILY_STATISTICS = true;
    public boolean ANALYZE_WEEKLY_STATISTICS = true;
    public boolean ANALYZE_MONTHLY_STATISTICS = true;
    public Log getLog() {
        return log;
    }

    private Log log;
    private ArrayList<JMXAgent> agents;
    private ArrayList<String> connections;
    public AnomalyDetector(){
        agents = new ArrayList<>();
        connections = new ArrayList<>();
    }

    /**
     * Connects to process, use default interval.
     * @param hostName hostname
     * @param port port
     */
    public void connect(String hostName, int port){
        log = new Log();
        agents.add(new JMXAgent(hostName, port, this));
        //@TODO Fix error check if connection fails.
        connections.add(hostName + ":" + port);
    }

    /**
     * Connects to process and set interval for memory statistics for that process.
     * @param hostName hostname
     * @param port port
     * @param interval interval in milliseconds decides how often memory statistics are gathered.
     */
    public void connect(String hostName, int port, int interval){
        log = new Log();
        agents.add(new JMXAgent(hostName, port, this));
        //@TODO Fix error check if connection fails.
        agents.get(agents.size()-1).setInterval(interval);
        connections.add(hostName + ":" + port);
    }

    public boolean disconnect(String hostName, int port){
        boolean disconnected = false;
        for (JMXAgent a : agents) {
            if (a.getHostName().equals(hostName) && a.getPort() == port) {
                agents.remove(a);
                //@TODO replace string-list with something less redundant.
                connections.remove(hostName + ":" + port);
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

}
