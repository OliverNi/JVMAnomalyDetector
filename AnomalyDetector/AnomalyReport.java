package AnomalyDetector;

/**
 * Created by Oliver on 2014-09-24.
 */

import java.util.Date;

/**
 * Object created when an anomaly is found. This class is responsible for notifying the AnomalyDetector and
 * Log to be stored away. An object of this class will contain information about the anomaly.
 */
public class AnomalyReport
{
    public enum Anomaly {
        EXCESSIVE_GC_SCAN,
        LIKELY_MEMORY_LEAK,
        SUSPECTED_MEMORY_LEAK,
        POSSIBLE_MEMORY_LEAK, //May not be used (setting?)
        UNKNOWN    //If this is shown something has gone wrong
    }
    private long timestamp;
    private String host;
    private int port;
    private String errorMsg;
    private long startTimeIncrease;
    private Anomaly anomaly;
    private int memIncreasePercentage;
    private long memIncreaseBytes;
    public AnomalyReport()
    {
        this(0, "", -1, "No Msg", 0, Anomaly.UNKNOWN, 0, 0);
    }

    public AnomalyReport(long timestamp, String host, int port, String errorMsg,
                         long startTimeIncrease, Anomaly anomaly, int memIncreasePercentage,
                         long memIncreaseBytes) {
        this.timestamp = timestamp;
        this.host = host;
        this.port = port;
        this.errorMsg = errorMsg;
        this.startTimeIncrease = startTimeIncrease;
        this.anomaly = anomaly;
        this.memIncreasePercentage = memIncreasePercentage;
        this.memIncreaseBytes = memIncreaseBytes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public long getStartTimeIncrease() {
        return startTimeIncrease;
    }

    public Anomaly getAnomaly() {
        return anomaly;
    }

    public int getMemIncreasePercentage() {
        return memIncreasePercentage;
    }

    public long getMemIncreaseBytes() {
        return memIncreaseBytes;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setStartTimeIncrease(long startTimeIncrease) {
        this.startTimeIncrease = startTimeIncrease;
    }

    public void setAnomaly(Anomaly anomaly) {
        this.anomaly = anomaly;
    }

    public void setAnomaly(String anomalyStatus)
    {
        if(anomalyStatus.contains("EXCESSIVE_GC_SCAN"))
        {
            anomaly = Anomaly.EXCESSIVE_GC_SCAN;
        }
        else if(anomalyStatus.contains("LIKELY_MEMORY_LEAK"))
        {
            anomaly = Anomaly.LIKELY_MEMORY_LEAK;
        }
        else if(anomalyStatus.contains("SUSPECTED_MEMORY_LEAK"))
        {
            anomaly = Anomaly.SUSPECTED_MEMORY_LEAK;
        }
        else if(anomalyStatus.contains("POSSIBLE_MEMORY_LEAK"))
        {
            anomaly = Anomaly.POSSIBLE_MEMORY_LEAK;
        }
        else
        {
            anomaly = Anomaly.POSSIBLE_MEMORY_LEAK;
        }
    }

    public void setMemIncreasePercentage(int memIncreasePercentage) {
        this.memIncreasePercentage = memIncreasePercentage;
    }

    public void setMemIncreaseBytes(long memIncreaseBytes) {
        this.memIncreaseBytes = memIncreaseBytes;
    }

    @Override
    public String toString(){
        String info = new String();
        info += "Anomaly Detected \n";
        Date time = new Date();
        time.setTime(timestamp);
        info += ("Time: " + time.toString() + "\n");
        info += "Process operating on: " + host + ":" + port + "\n";
        info += errorMsg + "\n";
        info += "Type: " + anomaly.toString() + "\n";
        time.setTime(startTimeIncrease);
        info += "Memory increase started at: " + time.toString() + "\n"; //@TODO Maybe Change to between (time between the Gc pre memleak and GC after memleak)
        info += "Memory has increased by: " + memIncreasePercentage + "% \n";
        info += "Memory has increased by: " + memIncreaseBytes + " bytes \n";
        return info;
    }


}
