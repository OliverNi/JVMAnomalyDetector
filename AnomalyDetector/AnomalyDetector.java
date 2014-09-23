package AnomalyDetector;

import Logs.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;


import java.util.ArrayList;

/**
 * Created by Oliver on 2014-09-10.
 */
public class AnomalyDetector {

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
    public void connect(String hostName, int port){
        log = new Log();
        agents.add(new JMXAgent(hostName, port, this));
        //@TODO Fix error check if connection fails.
        connections.add(hostName + ":" + port);
    }

    public boolean disconnect(String hostName, int port){
        boolean disconnected = false;
        for (JMXAgent a : agents) {
            if (a.getHostName().equals(hostName) && a.getPort() == port) {
                agents.remove(a);
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

}
