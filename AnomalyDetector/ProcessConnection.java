package AnomalyDetector;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by Oliver on 2014-10-06.
 */
public class ProcessConnection {
    public static int DEFAULT_INTERVAL = 20;
    private String hostName;
    private int port;
    private int interval;

    /**
     * Information about a connection to a process
     * @param hostName hostname
     * @param port port
     * @param interval time in minutes between every analysis.
     */
    public ProcessConnection(String hostName, int port, int interval){
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

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public int getInterval() {
        return interval;
    }

    @Override
    public String toString(){
        return hostName + ":" + port;
    }
}
