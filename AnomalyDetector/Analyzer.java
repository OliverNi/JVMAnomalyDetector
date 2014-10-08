package AnomalyDetector;

import Logs.AnalyzedGcReport;
import Logs.GcReport;
import Logs.GcStats;
import Logs.ILogging;

import java.util.*;

/**
 * Created by Oliver on 2014-09-18.
 */
public class Analyzer {
    //@TODO Add timer to combine GcReports each day/week/month
    class HourlyTask extends TimerTask {
        HourlyTask(){
        }
        public void run(){
            analyzeHourlyGc();
        }
    }
    class DailyTask extends TimerTask {
        DailyTask(){
        }
        public void run(){
            createDailyGcReports();
            analyzeDailyGC();
        }
    }
    class WeeklyTask extends TimerTask {
        WeeklyTask(){
        }
        public void run(){
            createWeeklyGcReports();
            analyzeWeeklyGC();
        }
    }
    class MonthlyTask extends TimerTask {
        MonthlyTask(){
        }
        public void run(){
            createMonthlyGcReports();
            analyzeMonthlyGC();
        }
    }

    public static final double DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING = 1.1;
    public static final double DEFAULT_CONSECUTIVE_MEM_INC = 5;
    private AnomalyDetector ad;
    private ILogging log;
    Timer hourlyTimer;
    Timer dailyTimer;
    Timer weeklyTimer;
    Timer monthlyTimer;

    public Analyzer(AnomalyDetector ad){
        this.ad = ad;
        this.log = ad.getLog();
        setTimers();
    }

    private void setTimers(){
        hourlyTimer = new Timer();
        dailyTimer = new Timer();
        weeklyTimer = new Timer();
        monthlyTimer = new Timer();
        long hour = 3600000L;
        long day = hour * 24;
        long week = day * 7;
        long month = day * 30;
        Date firstTime;
        Calendar cal = Calendar.getInstance();
        //Hourly task
        cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + 1);
        firstTime = cal.getTime();

        hourlyTimer.schedule(new HourlyTask(), firstTime.getTime(), hour);
        //Daily task
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        firstTime = cal.getTime();
        dailyTimer.schedule(new DailyTask(), firstTime.getTime(), day);
        //Weekly task
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        firstTime = cal.getTime();
        weeklyTimer.schedule(new WeeklyTask(), firstTime.getTime(), week);
        //Monthly task
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        firstTime = cal.getTime();
        //@TODO Fix different amount of days in different months.
        monthlyTimer.schedule(new MonthlyTask(), firstTime, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    }

     //@TODO rename method to analyzeIntervalGc() for example
    public void analyzeHourlyGc()
    {
            Calendar cal = Calendar.getInstance();

            //fetches all current processes in the format of ip:port
            ArrayList<String> connections = ad.getConnections();


            int amountOfConnections = 0;
            amountOfConnections = ad.getConnections().size();
            int[] intervalInMinutes = new int[amountOfConnections];
            Long[] intervalInMs = new Long[intervalInMinutes.length];

            //fetches each interval in minutes from each monitored process
            for(int i=0; i<amountOfConnections; i++)
            {
                try
                {
                    String[] hostPortName = connections.get(i).split("\\:");
                    int parsePort = Integer.parseInt(hostPortName[1]);
                    intervalInMinutes[i] = ad.getInterval(hostPortName[0], parsePort );
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }

            //converts interval value in minutes to milliseconds
            for(int i=0; i<intervalInMinutes.length; i++)
            {
                try
                {
                    String fetchValue = Double.toString((intervalInMinutes[i]*60)*1000);
                    intervalInMs[i] = Long.parseLong(fetchValue);
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }

            long day = 3600000L * 24;
            long hour = day/24;

             //fetches the current time and sets it to the endTime
             Date intervalEndTime = cal.getTime();

             long intervalStartTime = 0L;

            //fetches from database per input host/port parameters dynamic startTime and a current time as endtTime
            //and  puts it into a hasmap with corresponding ip:port key
            HashMap<String, ArrayList<GcStats>> intervalReportsMap = new HashMap<>();
            for(int i=0; i<connections.size(); i++)
            {
                intervalStartTime = cal.getTime().getTime()- intervalInMs[i];
                if(connections.get(i).contains(":"))
                {
                    String[] hostPortName = connections.get(i).split("\\:");
                    int port = Integer.parseInt(hostPortName[1]);
                    intervalReportsMap.put(connections.get(i),log.getGarbageCollectionStats(intervalStartTime, intervalEndTime.getTime(),hostPortName[0], port )  );
                }
            }
            //inputs all fetched GCCollectionStats (from GCReport table DB) with a startTime and endTime depending on each process's set interval time and assigns them to a Map.
            Map<String, ArrayList<GcStats>> thisIntervalReportsMap = intervalReportsMap;


            //for each process, a new GCstats is created
            for (int i = 0;  i < connections.size(); i++)
            {
                //Creates an arraylist of GcStats and fetches all GCstats entries for the current process through the set starttime and endtime above
                ArrayList<GcStats> todayReports = thisIntervalReportsMap.get(connections.get(i));

                long minimumMemValue = 0L;
                long originalMinimumMemValue = log.firstGcValue(connections.get(i));

                ProcessReport tempReport = new ProcessReport();
                tempReport.setUsageAfterFirstGc(originalMinimumMemValue);

                long IntervalStartTimeOnSuspectedMemLeak = 0L;

                try
                {
                    int memConsecutiveIncCounter = 0;
                    //fetches ip for the current process
                    String[] hostPort = connections.get(i).split(":");
                    //fetches port for the current process
                    int port = Integer.parseInt(hostPort[1]);

                    tempReport.setHostName(hostPort[0]);
                    tempReport.setPort(port);

                    for(int j=0; j<todayReports.size(); j++)
                    {
                        //compares minimumMemValue from last iteration with a new one on the current iteration, if the newer value keeps rising, there will be a consecutive count
                        //as well as an intervalStartTime which will be set on the timeframe of the increase.
                        if(minimumMemValue < todayReports.get(j).getMemoryUsedAfter() && minimumMemValue != 0)
                        {
                            if(IntervalStartTimeOnSuspectedMemLeak == 0)
                            {
                                IntervalStartTimeOnSuspectedMemLeak = cal.getTimeInMillis();
                            }
                            memConsecutiveIncCounter++;
                        }
                        //if the next value is lower, then the counter is reset together with intervalStartTime.
                        else if (minimumMemValue >= todayReports.get(j).getMemoryUsedAfter() && minimumMemValue != 0)
                        {
                            memConsecutiveIncCounter = 0;
                            IntervalStartTimeOnSuspectedMemLeak = 0;
                        }
                        minimumMemValue = todayReports.get(j).getMemoryUsedAfter();

                        //if the value equals or is above 10% threshhold of firstGCMinMemValue then the intervalStartTime is set to this timeframe,
                        // that is if the intervalStartTime has not already been set on another timeframe of a start of a slow increase for example.
                        if(minimumMemValue >= (originalMinimumMemValue*DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING) )
                        {
                            if(IntervalStartTimeOnSuspectedMemLeak == 0)
                            {
                                IntervalStartTimeOnSuspectedMemLeak = cal.getTimeInMillis();
                            }
                        }

                        //if we are on the last lap and the minimumGCMemUsage is above or equal the 10% threshhold of firstGCMemMinValue of this process
                        // then it's time to create a process report with a "SUSPECTED_MEMORY_LEAK" warning
                        if(j == todayReports.size()-1 && minimumMemValue >= (originalMinimumMemValue*DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING) )
                        {
                            tempReport.setUsageAfterLastGc(minimumMemValue);
                            tempReport.setStatus("SUSPECTED_MEMORY_LEAK");
                            if (IntervalStartTimeOnSuspectedMemLeak != 0)
                            {
                                log.sendProcessReport(IntervalStartTimeOnSuspectedMemLeak, intervalEndTime.getTime(),port,hostPort[0], tempReport);
                            }
                            else
                            {
                                log.sendProcessReport(intervalStartTime, intervalEndTime.getTime(),port,hostPort[0], tempReport);
                            }

                        }
                        // if the last gcMinMemvalue is below the 10% threshold of the firstGcMinMemValue
                        else if (j == todayReports.size()-1 && minimumMemValue < (originalMinimumMemValue*DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING) )
                        {
                            tempReport.setUsageAfterLastGc(minimumMemValue);
                            //if there has been 5 or more consecutive minMemoryIncreases in a row, then a processreport is created with a "LIKELY_MEMORY_LEAK"
                            if(memConsecutiveIncCounter >= DEFAULT_CONSECUTIVE_MEM_INC)
                            {
                                tempReport.setStatus("LIKELY_MEMORY_LEAK");
                                if(IntervalStartTimeOnSuspectedMemLeak != 0)
                                {
                                    log.sendProcessReport(IntervalStartTimeOnSuspectedMemLeak, intervalEndTime.getTime(),port,hostPort[0],tempReport);
                                }
                                else
                                {
                                    log.sendProcessReport(intervalStartTime, intervalEndTime.getTime(),port,hostPort[0],tempReport);
                                }

                            }
                            //if minimumMemValue is below the threshold of firstGCMemMinValue*10%, then the processReport gets the status "OK"
                            else
                            {
                                tempReport.setStatus("OK");
                                log.sendProcessReport(intervalStartTime, intervalEndTime.getTime(),port,hostPort[0],tempReport);
                            }
                        }
                    }
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }
    }

    /**
     * Analyze GcReports by comparing a daily GcReport to the previous day's GcReport
     */
    public void analyzeDailyGC(){
        //Set startTime (today 00:01:00)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        Date todayStartTime = cal.getTime();
        //Set endTime (today 23:59:59)
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date todayEndTime = cal.getTime();
        ArrayList<AnalyzedGcReport> reports = new ArrayList<>();
        long day = 3600000L * 24;
        Map<String, ArrayList<GcReport>> todayReportsMap = log.getGcReports(todayStartTime.getTime(), todayEndTime.getTime());
        Map<String, ArrayList<GcReport>> yesterdayReportsMap = log.getGcReports(todayStartTime.getTime() - day,
                todayEndTime.getTime() - day);

        ArrayList<String> connections = ad.getConnections();
        for (int i = 0;  i < connections.size(); i++) {
            reports.add(new AnalyzedGcReport());
            ArrayList<GcReport> todayReports = todayReportsMap.get(connections.get(i));
            ArrayList<GcReport> yesterdayReports = yesterdayReportsMap.get(connections.get(i));
            String[] hostPort = connections.get(i).split(":");
            int port = Integer.parseInt(hostPort[1]);
            reports.get(i).analyze(yesterdayReports.get(0), todayReports.get(0));
            forwardToProcessReport(hostPort[0], port, reports.get(i));
        }
    }

    /**
     * Analyze GcReports by comparing a weekly GcReport to the previous weeks's GcReport
     */
    public void analyzeWeeklyGC(){
        //Set startTime (Monday 00:01:00)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStartTime = cal.getTime();
        //Set endTime (Sunday 23:59:59)
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date weekEndTime = cal.getTime();
        ArrayList<AnalyzedGcReport> reports = new ArrayList<>();
        long week = 3600000L * 24 * 7;
        Map<String, ArrayList<GcReport>> todayReportsMap = log.getGcReports(weekStartTime.getTime(), weekEndTime.getTime());
        Map<String, ArrayList<GcReport>> yesterdayReportsMap = log.getGcReports(weekStartTime.getTime() - week,
                weekEndTime.getTime() - week);

        ArrayList<String> connections = ad.getConnections();
        for (int i = 0;  i < connections.size(); i++) {
            reports.add(new AnalyzedGcReport());
            ArrayList<GcReport> todayReports = todayReportsMap.get(connections.get(i));
            ArrayList<GcReport> yesterdayReports = yesterdayReportsMap.get(connections.get(i));
            String[] hostPort = connections.get(i).split(":");
            int port = Integer.parseInt(hostPort[1]);
            reports.get(i).analyze(yesterdayReports.get(0), todayReports.get(0));
            forwardToProcessReport(hostPort[0], port, reports.get(i));
        }
    }

    /**
     * Analyze GcReports by comparing a monthly GcReport to the previous month's GcReport
     */
    public void analyzeMonthlyGC(){
        //Set startTime (First day of the month 00:01:00)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DATE, 1);
        Date monthStartTime = cal.getTime();
        //Set endTime (Last day of the month 23:59:59)
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        Date monthEndTime = cal.getTime();
        ArrayList<AnalyzedGcReport> reports = new ArrayList<>();
        long month = 3600000L * 24 * 7 * 30;
        Map<String, ArrayList<GcReport>> todayReportsMap = log.getGcReports(monthStartTime.getTime(), monthEndTime.getTime());
        Map<String, ArrayList<GcReport>> yesterdayReportsMap = log.getGcReports(monthStartTime.getTime() - month,
                monthEndTime.getTime() - month);

        ArrayList<String> connections = ad.getConnections();
        for (int i = 0;  i < connections.size(); i++) {
            reports.add(new AnalyzedGcReport());
            ArrayList<GcReport> todayReports = todayReportsMap.get(connections.get(i));
            ArrayList<GcReport> yesterdayReports = yesterdayReportsMap.get(connections.get(i));
            String[] hostPort = connections.get(i).split(":");
            int port = Integer.parseInt(hostPort[1]);
            reports.get(i).analyze(yesterdayReports.get(0), todayReports.get(0));
            forwardToProcessReport(hostPort[0], port, reports.get(i));
        }
    }

    private GcReport combineDailyGcStats(ArrayList<GcStats> gcStats){
        GcReport report = new GcReport();
        for (GcStats g : gcStats){
            report.addGcStats(g);
        }
        return report;
    }

    private GcReport combineGcReports(ArrayList<GcReport> reportList){
        GcReport report = new GcReport();
        for (GcReport g : reportList){
            report.addGcReport(g);
        }
        return report;
    }

    public void createDailyGcReports(){
        //Set startTime (today 00:01:00)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        Date todayStartTime = cal.getTime();
        //Set endTime (today 23:59:59)
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date todayEndTime = cal.getTime();

        Map<String, ArrayList<GcStats>> gcStatsMap = log.getGarbageCollectionStats(todayStartTime.getTime(),
                todayEndTime.getTime());

        ArrayList<String> connections = ad.getConnections();
        for (int i = 0;  i < connections.size(); i++){
            ArrayList<GcStats> gcStats = gcStatsMap.get(connections.get(i));
            String[] hostPort = connections.get(i).split(":");
            int port = Integer.parseInt(hostPort[1]);
            log.sendGcReport(hostPort[0], port, combineDailyGcStats(gcStats));
        }
    }

    public void createWeeklyGcReports(){
        //Set startTime (Monday 00:01:00)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStartTime = cal.getTime();
        //Set endTime (Sunday 23:59:59)
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date weekEndTime = cal.getTime();

        Map<String, ArrayList<GcReport>> reportsMap = log.getGcReports(weekStartTime.getTime(), weekEndTime.getTime());
        ArrayList<String> connections = ad.getConnections();
        for (int i = 0;  i < connections.size(); i++){
            ArrayList<GcReport> dailyReports = reportsMap.get(connections.get(i));
            String[] hostPort = connections.get(i).split(":");
            int port = Integer.parseInt(hostPort[1]);
            log.sendGcReport(hostPort[0], port, combineGcReports(dailyReports));
        }
    }

    public void createMonthlyGcReports(){
        //Set startTime (First day of the month 00:01:00)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DATE, 1);
        Date monthStartTime = cal.getTime();
        //Set endTime (Last day of the month 23:59:59)
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        Date monthEndTime = cal.getTime();

        Map<String, ArrayList<GcReport>> reportsMap = log.getGcReports(monthStartTime.getTime(), monthEndTime.getTime());
        ArrayList<String> connections = ad.getConnections();
        for (int i = 0;  i < connections.size(); i++){
            ArrayList<GcReport> dailyReports = reportsMap.get(connections.get(i));
            String[] hostPort = connections.get(i).split(":");
            int port = Integer.parseInt(hostPort[1]);
            log.sendGcReport(hostPort[0], port, combineGcReports(dailyReports));
        }
    }

    private void forwardToProcessReport(String hostName, int port, AnalyzedGcReport report){
        ProcessReport pr = log.getProcessReport(hostName, port);
        pr.addReport(report);
    }

    //@TODO Move function calcAvg
    private long calcAvg(long[] arr){
        long sum = 0;
        for (int i = 0; i < arr.length; i++){
            sum += arr[i];
        }
        return sum / arr.length;
    }


}
