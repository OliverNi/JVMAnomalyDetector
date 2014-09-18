package AnomalyDetector;

import Logs.Log;

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
    public AnomalyDetector(){
        agents = new ArrayList<>();
    }
    public void connect(String hostName, int port){
        log = new Log();
        agents.add(new JMXAgent(hostName, port, this));
    }

}
