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

     //@TODO implement creation of a processReport for each executed garbage collection
    //@TODO implement eventual excessive GC scan detection
    public void analyzeHourlyGc()
    {
            Calendar cal = Calendar.getInstance();

            long day = 3600000L * 24;
            long hour = day/24;
            Date hourlyEndTime = cal.getTime();
            long hourlyStartTime = cal.getTime().getTime()- hour;

            //inputs current time and the time an hour ago and fetches all available logs for all current processes.
            Map<String, ArrayList<GcStats>> thisHourlyReportsMap = log.getGarbageCollectionStats(hourlyStartTime, hourlyEndTime.getTime());

            //fetchar alla current processes med ip:port
            ArrayList<String> connections = ad.getConnections();
            //för varje process så skapas en ny AnalyzedGcReport
            for (int i = 0;  i < connections.size(); i++)
            {
                //Creates an arraylist of GcStats and fetches all GCstats entries for the current process through the set starttime and endtime above
                ArrayList<GcStats> todayReports = thisHourlyReportsMap.get(connections.get(i));

                long minimumMemValue = 0L;
                long originalMinimumMemValue = log.firstGcValue(connections.get(i));
                ProcessReport tempReport = new ProcessReport();
                boolean passedLastGCexec = false;
                //fetches ip for the current process
                String[] hostPort = connections.get(i).split(":");
                //fetches port for the current process
                int port = Integer.parseInt(hostPort[1]);

                for(int j=0; j<todayReports.size(); j++)
                {
                    minimumMemValue = todayReports.get(j).getMemoryUsedAfter();

                    //checks for a breach of tolerance level (above or exactly 10% over the oldest minimumMemValue taken from the current process.
                    if(minimumMemValue >= originalMinimumMemValue*DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING )
                    {
                        tempReport.setHostName(hostPort[0]);
                        tempReport.setPort(port);

                    }
                    //checks if the heap memory allocation goes down within tolerance level after the breach and before the end of the one hour interval
                    if( j == todayReports.size()-1 && minimumMemValue < originalMinimumMemValue*DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING)
                    {
                        passedLastGCexec = true;
                    }
                    //if we are on the last lap and the minimumGCMemUsage hasn't gone down, then it's time to create a process report
                    if(!passedLastGCexec && j == todayReports.size()-1)
                    {
                        log.sendProcessReport(hourlyStartTime, hourlyEndTime.getTime(),port,hostPort[0],"SUSPECTED_MEMORY_LEAK,");
                    }
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
