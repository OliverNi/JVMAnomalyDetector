package AnomalyDetector;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by Oliver on 2014-10-06.
 */
public class ProcessConnection {
    public static double DEFAULT_INTERVAL = 1000;
    private String hostName;
    private int port;
    private double interval;

    public ProcessConnection(String hostName, int port, double interval){
        this.hostName = hostName;
        this.port = port;
        this.interval = interval;
    }

    public ProcessConnection(String hostName, int port){
        this(hostName, port, DEFAULT_INTERVAL);
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public double getInterval() {
        return interval;
    }
}
