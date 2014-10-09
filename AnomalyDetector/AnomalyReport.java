package AnomalyDetector;

/**
 * Created by Oliver on 2014-09-24.
 */

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
        POSSIBLE_MEMORY_LEAK //May not be used (setting?)
    }
    private long timestamp;
    private String host;
    private int port;
    private String errorMsg;
    private long startTimeIncrease;
    private Anomaly anomaly;
    private double memIncreasePercentage;
    private long memIncreaseBytes;
    public AnomalyReport()
    {
        this(0, null, -1, "No Msg", 0, null, 0, 0);
    }

    public AnomalyReport(long timestamp, String host, int port, String errorMsg,
                         long startTimeIncrease, Anomaly anomaly, double memIncreasePercentage,
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

    public double getMemIncreasePercentage() {
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

    public void setMemIncreasePercentage(double memIncreasePercentage) {
        this.memIncreasePercentage = memIncreasePercentage;
    }

    public void setMemIncreaseBytes(long memIncreaseBytes) {
        this.memIncreaseBytes = memIncreaseBytes;
    }

    @Override
    public String toString(){
        //@TODO Implement (Will be used in AnomalyEvent
        return null;
    }


}
