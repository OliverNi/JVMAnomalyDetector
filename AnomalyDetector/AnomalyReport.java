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
    public enum Anomaly{
        EXCESSIVE_GC_SCAN,
        OVER_TIME_INCREASING_MEMORY_USAGE,
        RAPIDLY_INCREASING_MEMORY_USAGE
    }
    private Anomaly type;
    private String hostname;
    private int port;
    private long timePeriod;
    private String errorMessage;
    private String errorAccuracy;
    private long anomalyStartTime;

    public AnomalyReport()
    {
        hostname = "";
        port = 0;
        timePeriod = 0;
        errorMessage  = "none";
        errorAccuracy = "none";
        anomalyStartTime = 0;
    }

    public AnomalyReport(long anomalyStartTime, long timePeriod, String errorMessage, String errorAccuracy, String hostname, int port)
    {
        this.anomalyStartTime = anomalyStartTime;
        this.timePeriod = timePeriod;
        this.errorMessage = errorMessage;
        this.errorAccuracy = errorAccuracy;
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public String toString(){
        //@TODO Implement (Will be used in AnomalyEvent
        return null;
    }


}
