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
    public ProcessReport()
    {
        this("Unknown", 0);
    }
    public ProcessReport(String hostName, int port)
    {
        usageAfterFirstGc = 0;
        usageAfterLastGc = 0;
        consec_mem_inc_count = 0;
        startTime = 0;
        endTime = 0;
        this.port = 0;
        this.hostName = "";
        this.status = Status.OK;
        TIME_BETWEEN_GC_WARNING = DEFAULT_TIME_BETWEEN_GC_WARNING;
        PERCENTAGE_INC_IN_MEM_USE_WARNING = DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING;
    }

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

    //@TODO possible scrap for the uptime variable
    private int uptimeInDays;

    //@TODO implement in addReport
    private long startTime;
    private long endTime;
    private String hostName;
    private int port;
    private Status status;

    //keeps a track on if the minimumMemvalue after each GC has increased, and counts how many times in a row it has increased.
    private int consec_mem_inc_count;

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


    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public long getUsageAfterFirstGc() {
        return usageAfterFirstGc;
    }

    public long getUsageAfterLastGc() {
        return usageAfterLastGc;
    }

    public void setUsageAfterFirstGc(long usageAfterFirstGc) {
        this.usageAfterFirstGc = usageAfterFirstGc;
    }

    public void setUsageAfterLastGc(long usageAfterLastGc) {
        this.usageAfterLastGc = usageAfterLastGc;
    }


    public String getStatus()
    {
        String returnStatus = "";
        if(status.equals("POSSIBLE_MEMORY_LEAK"))
        {
            returnStatus = "POSSIBLE_MEMORY_LEAK";
        }
        else if(status.equals("EXCESSIVE_GC_SCAN"))
        {
            returnStatus = "EXCESSIVE_GC_SCAN";
        }
        else if(status.equals("LIKELY_MEMORY_LEAK"))
        {
            returnStatus = "LIKELY_MEMORY_LEAK";
        }
        else if(status.equals("SUSPECTED_MEMORY_LEAK"))
        {
            returnStatus = "SUSPECTED_MEMORY_LEAK";
        }
        else if(status.equals("OK"))
        {
            returnStatus = "OK;";
        }
        else
        {
            returnStatus = "OK";
        }
        return returnStatus;
    }

    public int getConsec_mem_inc_count() {
        return consec_mem_inc_count;
    }

    public void setConsec_mem_inc_count(int consec_mem_inc_count) {
        this.consec_mem_inc_count = consec_mem_inc_count;
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
        if (dailyReportCount == 0)
            return 0;
        return dailySumMemUsageDif / dailyReportCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getDailySumMemUsageDif() {
        return dailySumMemUsageDif;
    }

    public double getWeeklySumMemUsageDif() {
        return weeklySumMemUsageDif;
    }

    public double getMonthlySumMemUsageDif() {
        return monthlySumMemUsageDif;
    }

    public double getDailyMinMemUsageDif() {
        return dailyMinMemUsageDif;
    }

    public double getMonthlyMinMemUsageDif() {
        return monthlyMinMemUsageDif;
    }

    public double getWeeklyMinMemUsageDif() {
        return weeklyMinMemUsageDif;
    }

    public int getDailyIncreaseCount() {
        return dailyIncreaseCount;
    }

    public int getWeeklyIncreaseCount() {
        return weeklyIncreaseCount;
    }

    public int getMonthlyIncreaseCount() {
        return monthlyIncreaseCount;
    }

    public int getDailyDecreaseCount() {
        return dailyDecreaseCount;
    }

    public int getWeeklyDecreaseCount() {
        return weeklyDecreaseCount;
    }

    public int getMonthlyDecreaseCount() {
        return monthlyDecreaseCount;
    }

    public int getDailyReportCount() {
        return dailyReportCount;
    }

    public int getWeeklyReportCount() {
        return weeklyReportCount;
    }

    public int getMonthlyReportCount() {
        return monthlyReportCount;
    }

    public double getWeeklyAvgMemUsageDif(){
        if (weeklyReportCount == 0)
            return 0;
        return weeklySumMemUsageDif / weeklyReportCount;
    }

    public double getMonthlyAvgMemUsageDif(){
        if (monthlyReportCount == 0)
            return 0;
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
                if (report.getAvgMemoryUsageDif() < weeklyMinMemUsageDif)
                    weeklyMinMemUsageDif = report.getAvgMinMemoryUsageDif();
                if (report.getAvgMemoryUsageDif() > 1)
                    weeklyIncreaseCount++;
                else if (report.getAvgMemoryUsageDif() < 1)
                    weeklyDecreaseCount++;
                weeklySumMemUsageDif += report.getAvgMemoryUsageDif();
                weeklyReportCount++;
                break;
            case MONTHLY:
                if (report.getAvgMemoryUsageDif() < monthlyMinMemUsageDif)
                    monthlyMinMemUsageDif = report.getAvgMinMemoryUsageDif();
                if (report.getAvgMemoryUsageDif() > 1)
                    monthlyIncreaseCount++;
                else if (report.getAvgMemoryUsageDif() < 1)
                    monthlyDecreaseCount++;
                monthlySumMemUsageDif += report.getAvgMemoryUsageDif();
                monthlyReportCount++;
                break;
        }
    }


}
