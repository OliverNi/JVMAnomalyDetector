package AnomalyDetector;

/**
 * Created by Oliver on 2014-09-25.
 */

import Logs.AnalyzedGcReport;

import java.util.Calendar;

/**
 * A Process Report will be responsible for keeping track of a process' status and warn if it suspects a memory leak.
 */
public class ProcessReport
{
    public ProcessReport()
    {
        this(0L, 0L, "Unknown", 0, Status.OK, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0);
    }
    public ProcessReport(String hostName, int port)
    {
        this(0L, 0L, hostName, port, Status.OK, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, 0);

    }

    public ProcessReport(long startTime, long endTime, String hostName, int port, Status status, int consecMemIncCount, long usageAfterFirstGc, long usageAfterLastGc,
                         double dailySumMemUsageDif, double weeklySumMemUsageDif, double monthlySumMemUsageDif, double dailyMinMemUsageDif, double monthlyMinMemUsageDif,
                         double weeklyMinMemUsageDif, int dailyIncreaseCount, int weeklyIncreaseCount, int monthlyIncreaseCount, int dailyDecreaseCount, int weeklyDecreaseCount,
                         int monthlyDecreaseCount, int dailyReportCount, int weeklyReportCount, int monthlyReportCount, long timeOfLastGc) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.hostName = hostName;
        this.port = port;
        this.status = status;
        this.consecMemIncCount = consecMemIncCount;
        this.usageAfterFirstGc = usageAfterFirstGc;
        this.usageAfterLastGc = usageAfterLastGc;
        this.dailySumMemUsageDif = dailySumMemUsageDif;
        this.weeklySumMemUsageDif = weeklySumMemUsageDif;
        this.monthlySumMemUsageDif = monthlySumMemUsageDif;
        this.dailyMinMemUsageDif = dailyMinMemUsageDif;
        this.monthlyMinMemUsageDif = monthlyMinMemUsageDif;
        this.weeklyMinMemUsageDif = weeklyMinMemUsageDif;
        this.dailyIncreaseCount = dailyIncreaseCount;
        this.weeklyIncreaseCount = weeklyIncreaseCount;
        this.monthlyIncreaseCount = monthlyIncreaseCount;
        this.dailyDecreaseCount = dailyDecreaseCount;
        this.weeklyDecreaseCount = weeklyDecreaseCount;
        this.monthlyDecreaseCount = monthlyDecreaseCount;
        this.dailyReportCount = dailyReportCount;
        this.weeklyReportCount = weeklyReportCount;
        this.monthlyReportCount = monthlyReportCount;
        this.timeOfLastGc = timeOfLastGc;
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
    public static long TIME_BETWEEN_GC_WARNING = DEFAULT_TIME_BETWEEN_GC_WARNING;
    public static double PERCENTAGE_INC_IN_MEM_USE_WARNING = DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING;

    private long startTime;
    private long endTime;
    private String hostName;
    private int port;
    private Status status;

    //keeps a track on if the minimumMemvalue after each GC has increased, and counts how many times in a row it has increased.
    private int consecMemIncCount;

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

    //How many times have the memory usage increased compared to the previous day/week/month
    private int dailyIncreaseCount;
    private int weeklyIncreaseCount;
    private int monthlyIncreaseCount;

    //How many times have the memory usage decreased compared to the previous day/week/month
    private int dailyDecreaseCount;
    private int weeklyDecreaseCount;
    private int monthlyDecreaseCount;

    //How many reports of the different types
    private int dailyReportCount;
    private int weeklyReportCount;
    private int monthlyReportCount;

    private long timeOfLastGc;



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
        return status.toString();
    }

    public int getConsecMemIncCount() {
        return consecMemIncCount;
    }

    public void setConsecMemIncCount(int consecMemIncCount) {
        this.consecMemIncCount = consecMemIncCount;
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

    public void setDailySumMemUsageDif(double dailySumMemUsageDif) {
        this.dailySumMemUsageDif = dailySumMemUsageDif;
    }

    public void setWeeklySumMemUsageDif(double weeklySumMemUsageDif) {
        this.weeklySumMemUsageDif = weeklySumMemUsageDif;
    }

    public void setMonthlySumMemUsageDif(double monthlySumMemUsageDif) {
        this.monthlySumMemUsageDif = monthlySumMemUsageDif;
    }

    public void setDailyMinMemUsageDif(double dailyMinMemUsageDif) {
        this.dailyMinMemUsageDif = dailyMinMemUsageDif;
    }

    public void setMonthlyMinMemUsageDif(double monthlyMinMemUsageDif) {
        this.monthlyMinMemUsageDif = monthlyMinMemUsageDif;
    }

    public void setWeeklyMinMemUsageDif(double weeklyMinMemUsageDif) {
        this.weeklyMinMemUsageDif = weeklyMinMemUsageDif;
    }

    public void setDailyIncreaseCount(int dailyIncreaseCount) {
        this.dailyIncreaseCount = dailyIncreaseCount;
    }

    public void setWeeklyIncreaseCount(int weeklyIncreaseCount) {
        this.weeklyIncreaseCount = weeklyIncreaseCount;
    }

    public void setMonthlyIncreaseCount(int monthlyIncreaseCount) {
        this.monthlyIncreaseCount = monthlyIncreaseCount;
    }

    public void setDailyDecreaseCount(int dailyDecreaseCount) {
        this.dailyDecreaseCount = dailyDecreaseCount;
    }

    public void setWeeklyDecreaseCount(int weeklyDecreaseCount) {
        this.weeklyDecreaseCount = weeklyDecreaseCount;
    }

    public void setMonthlyDecreaseCount(int monthlyDecreaseCount) {
        this.monthlyDecreaseCount = monthlyDecreaseCount;
    }

    public void setDailyReportCount(int dailyReportCount) {
        this.dailyReportCount = dailyReportCount;
    }

    public void setWeeklyReportCount(int weeklyReportCount) {
        this.weeklyReportCount = weeklyReportCount;
    }

    public void setMonthlyReportCount(int monthlyReportCount) {
        this.monthlyReportCount = monthlyReportCount;
    }

    public long getTimeOfLastGc() {
        return timeOfLastGc;
    }

    public void setTimeOfLastGc(long timeOfLastGc) {
        this.timeOfLastGc = timeOfLastGc;
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
