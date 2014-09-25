package AnomalyDetector;

/**
 * Created by Oliver on 2014-09-25.
 */

/**
 * A Process Report will be responsible for keeping track of a process' status and warn if it suspects a memory leak.
 */
public class ProcessReport {
    //@TODO Create Listener
    //Create AnomalyReport if excessive GC scan
    public enum Status{
        LIKELY_MEMORY_LEAK,
        SUSPECTED_MEMORY_LEAK,
        POSSIBLE_MEMORY_LEAK,
        EXCESSIVE_GC_SCAN,
        OK
    }
    public static final long DEFAULT_TIME_BETWEEN_GC_WARNING = 5000;
    public static long TIME_BETWEEN_GC_WARNING;
    private String hostName;
    private int port;
    private Status status;

    //GC
    //Usage after first recorded GC
    private int usageAfterFirstGc;

    //Usage after last recorded GC
    private int usageAfterLastGc;

    //Difference (percentage) in average memory usage
    private double dailyAvgMemUsageDif;
    private double weeklyAvgMemUsageDif;
    private double monthlyAvgMemUsageDif;

    //Difference in minimum memory used after a GC
    private double dailyMinMemUsageDif;
    private double monthlyMinMemUsageDif;
    private double weeklyMinMemUsageDif;

    //How many times have the memory usage increased compared to the previous day/week/month
    private int dailyIncreaseCount;
    private int weeklyIncreaseCount;
    private int monthlyIncreaseCount;

    //How many times have the memory usage decreased compared to the previous day/week/month
    private int dailyDecreaseCount;
    private int weeklyDecreaseCount;
    private int monthlyDecreaseCount;

    //Interval

    public ProcessReport(String hostName, int port){
        TIME_BETWEEN_GC_WARNING = DEFAULT_TIME_BETWEEN_GC_WARNING;
    }

    public ProcessReport(){
        this("Unknown", 0);
    }
}