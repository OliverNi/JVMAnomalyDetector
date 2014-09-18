package AnomalyDetector;

import java.util.ArrayList;

/**
 * Created by Oliver on 2014-09-10.
 */
public class AnomalyDetector {
    private ArrayList<JMXAgent> agents;
    public AnomalyDetector(){
        agents = new ArrayList<>();
    }
    public void connect(String hostName, int port){
        agents.add(new JMXAgent(hostName, port,this));
    }

    /**
     * Testing purposes
     */
    public void poll(){
        for (JMXAgent a : agents){
            a.gather();
        }
    }
}
