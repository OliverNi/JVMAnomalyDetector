package AnomalyDetector;

/**
 * Created by Oliver on 2014-09-25.
 */

import Logs.AnalyzedGcReport;

/**
 * A Process Report will be responsible for keeping track of a process' status and warn if it suspects a memory leak.
 */
public class ProcessReport
{
    //Create AnomalyReport if excessive GC scan
    public enum Status
    {
        LIKELY_MEMORY_LEAK,
        SUSPECTED_MEMORY_LEAK,
        POSSIBLE_MEMORY_LEAK,
        EXCESSIVE_GC_SCAN,
        OK
    }
    // default value in milliseconds (20min) corresponding to a warning
    public static final long DEFAULT_TIME_BETWEEN_GC_WARNING = 1200000;
    //default value of a percentage increase corresponding to a warning
    public static final double DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING = 1.1;
    public static long TIME_BETWEEN_GC_WARNING;
    public static double PERCENTAGE_INC_IN_MEM_USE_WARNING;



    //@TODO implement uptimeInDays in DB table ProcessReports?
    private int uptimeInDays;

    private long startTime;
    private long endTime;
    private String hostName;
    private int port;
    private Status status;
    //GC
    //Usage after first recorded GC
    private long usageAfterFirstGc;

    //Usage after last recorded GC
    private long usageAfterLastGc;

    //Difference (percentage) sum (Used to calc avg)
    private double dailySumMemUsageDif;
    private double weeklySumMemUsageDif;
    private double monthlySumMemUsageDif;

    //Difference in minimum memory used after a GC
    private double dailyMinMemUsageDif;
    private double monthlyMinMemUsageDif;
    private double weeklyMinMemUsageDif;

    //@TODO Possibly remove, not sure how it would be used to find memory leaks. Should instead focus on minimum value
    //How many times have the memory usage increased compared to the previous day/week/month
    private int dailyIncreaseCount;
    private int weeklyIncreaseCount;
    private int monthlyIncreaseCount;

    //@TODO Possibly remove, not sure how it would be used to find memory leaks. Should instead focus on minimum value
    //How many times have the memory usage decreased compared to the previous day/week/month
    private int dailyDecreaseCount;
    private int weeklyDecreaseCount;
    private int monthlyDecreaseCount;

    //How many reports of the different types
    private int dailyReportCount;
    private int weeklyReportCount;
    private int monthlyReportCount;



    //MemStats

    public ProcessReport(String hostName, int port)
    {
        this.status = Status.OK;
        TIME_BETWEEN_GC_WARNING = DEFAULT_TIME_BETWEEN_GC_WARNING;
        PERCENTAGE_INC_IN_MEM_USE_WARNING = DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING;
    }

    public ProcessReport(){
        this("Unknown", 0);
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public void setPort(int Port)
    {
        this.port = Port;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public void setStartTime(long startTime)
    {
        this.startTime = startTime;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public void setStatus(String status)
    {
        if(status.equals("POSSIBLE_MEMORY_LEAK"))
        {
            this.status = Status.POSSIBLE_MEMORY_LEAK;
        }
        else if(status.equals("EXCESSIVE_GC_SCAN"))
        {
            this.status = Status.EXCESSIVE_GC_SCAN;
        }
        else if(status.equals("LIKELY_MEMORY_LEAK"))
        {
            this.status = Status.LIKELY_MEMORY_LEAK;
        }
        else if(status.equals("SUSPECTED_MEMORY_LEAK"))
        {
            this.status = Status.SUSPECTED_MEMORY_LEAK;
        }
        else if(status.equals("OK"))
        {
          this.status = Status.OK;
        }
        else
        {
            this.status = Status.OK;
        }

    }

    public double getDailyAvgMemUsageDif(){
        return dailySumMemUsageDif / dailyReportCount;
    }

    public double getWeeklyAvgMemUsageDif(){
        return weeklySumMemUsageDif / weeklyReportCount;
    }

    public double getMonthlyAvgMemUsageDif(){
        return monthlySumMemUsageDif / monthlyReportCount;
    }

    public void addReport(AnalyzedGcReport report){
        switch(report.getType()){
            case HOURLY:

                break;
            case DAILY:
                if (report.getAvgMinMemoryUsageDif() < dailyMinMemUsageDif)
                    dailyMinMemUsageDif = report.getAvgMinMemoryUsageDif();
                if (report.getAvgMemoryUsageDif() > 1)
                    dailyIncreaseCount++;
                else if (report.getAvgMemoryUsageDif() < 1)
                    dailyDecreaseCount++;
                dailySumMemUsageDif += report.getAvgMemoryUsageDif();
                dailyReportCount++;
                break;
            case WEEKLY:
                break;
            case MONTHLY:
                break;
        }
    }

    private void createAnomalyReport(){
        //@TODO Implement
    }
}
