package AnomalyDetector;

import Listeners.AnomalyEvent;
import Listeners.AnomalyListener;
import Logs.*;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.*;

/**
 * Created by Oliver on 2014-09-18.
 */
public class Analyzer {
    class DailyTask extends TimerTask {
        DailyTask(){
        }
        public void run(){
            createDailyGcReports();
            analyzeDailyGC();
            log.clearOldData();
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
            scheduleMonthlyTask();
        }
    }
    class IntervalTask extends TimerTask{
        String host;
        int port, interval;
        IntervalTask(String host, int port, int interval){
            this.host = host;
            this.port = port;
            this.interval = interval;
        }
        public void run(){
            analyzeIntervalGc(host, port, interval);
        }
    }
    class CooldownTask extends TimerTask{
        String host;
        int port;
        CooldownTask(String host, int port){
            this.host = host;
            this.port = port;
        }
        public void run(){
            String process = host + ":" + port;
            cooldown.put(process, false);
        }
    }

    public static final double DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING = 1.1;
    private static double PERCENTAGE_INC_IN_MEM_USE_WARNING = DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING;
    public static final double DEFAULT_CONSECUTIVE_MEM_INC = 1;
    public static final long DEFAULT_TIME_EXCESSIVE_SCAN_WARNING = 1000;
    private static long TIME_EXCESSIVE_SCAN_WARNING = DEFAULT_TIME_EXCESSIVE_SCAN_WARNING;
    public static final long EXCESSIVE_GC_COOLDOWN = 3600000L; //One hour
    private HashMap<String, Boolean> cooldown = new HashMap<>();
    private AnomalyDetector ad;
    private ILogging log;
    private Timer dailyTimer;
    private Timer weeklyTimer;
    private Timer monthlyTimer;
    private Map<String, Timer> intervalTimers;

    public Analyzer(AnomalyDetector ad){
        this.ad = ad;
        this.log = Log.getInstance();
        setTimers();
    }

    private void setTimers(){
        dailyTimer = new Timer();
        weeklyTimer = new Timer();
        monthlyTimer = new Timer();
        intervalTimers = new HashMap<>();

        long hour = 3600000L;
        long day = hour * 24;
        long week = day * 7;

        scheduleDailyTask(day);
        scheduleWeeklyTask(week);
        scheduleMonthlyTask();
    }

    private void scheduleDailyTask(long day){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.AM_PM, Calendar.PM);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date firstTime = cal.getTime();
        dailyTimer.scheduleAtFixedRate(new DailyTask(), firstTime.getTime(), day);
    }

    private void scheduleWeeklyTask(long week){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.AM_PM, Calendar.PM);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date firstTime = cal.getTime();
        weeklyTimer.scheduleAtFixedRate(new WeeklyTask(), firstTime.getTime(), week);
    }

    private void scheduleMonthlyTask(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.AM_PM, Calendar.PM);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
        Date firstTime = cal.getTime();
        monthlyTimer.schedule(new MonthlyTask(), firstTime);
    }


    public void addIntervalTimer(String host, int port, int interval){
        long minute = 60000L;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new IntervalTask(host, port, interval), interval * minute, interval * minute);
        intervalTimers.put(host + ":" + port, timer);
    }

    /**
     * Looks for excessive Gc Scans. Creates an AnomalyReport if GC scans happen to often.
     * @param host host
     * @param port port
     * @param currentGc The GcStats which will be analyzed.
     */
    public void analyzeExcessiveGcScan(String host, int port, GcStats currentGc){
        ProcessConnection conn = new ProcessConnection(host, port);
        long timeOfLastGc = Log.getInstance().getTimeOfLastGc(conn);
        if (timeOfLastGc > 0L) {
            ProcessReport pReport = log.getProcessReport(host, port);
            long timeWarning = TIME_EXCESSIVE_SCAN_WARNING;
            if ((currentGc.getTimeStamp() - timeOfLastGc) < timeWarning) {
                AnomalyReport aReport = new AnomalyReport();
                aReport.setAnomaly(AnomalyReport.Anomaly.EXCESSIVE_GC_SCAN);
                aReport.setErrorMsg("Time between Garbage Collections has gone under " + timeWarning + " milliseconds!");
                aReport.setHost(host);
                aReport.setPort(port);
                aReport.setMemIncreaseBytes(currentGc.getMemoryUsedAfter() - pReport.getUsageAfterFirstGc());
                int percentage = (int) ((((double)currentGc.getMemoryUsedAfter() - pReport.getUsageAfterFirstGc()) / pReport.getUsageAfterFirstGc()) * 100);
                aReport.setMemIncreasePercentage(percentage);
                aReport.setTimestamp(currentGc.getTimeStamp());

                //Cooldown check
                if (!cooldown.containsKey(conn.toString())){
                    cooldown.put(conn.toString(), true);
                    new Timer().schedule(new CooldownTask(conn.getHostName(), conn.getPort()), EXCESSIVE_GC_COOLDOWN);
                    fireAnomalyEvent(aReport);
                    Log.getInstance().setProcessReportStatus(host, port, ProcessReport.Status.EXCESSIVE_GC_SCAN);
                } else if (!cooldown.get(conn.toString())) {
                    cooldown.put(conn.toString(), true);
                    new Timer().schedule(new CooldownTask(conn.getHostName(), conn.getPort()), EXCESSIVE_GC_COOLDOWN);
                    fireAnomalyEvent(aReport);
                    Log.getInstance().setProcessReportStatus(host, port, ProcessReport.Status.EXCESSIVE_GC_SCAN);
                }
            }
        }
        Log.getInstance().sendTimeOfLastGc(conn, currentGc.getTimeStamp());
    }

    public void analyzeIntervalGc(String host, int port, int interval)
    {
       // System.out.println("DEBUG: Entered interval analysis");
        Calendar cal = Calendar.getInstance();

        long intervalInMs = 0;

       // System.out.println("DEBUG: interval analysis: Connection: " + host + ":" + port);

        //converts interval value in minutes to milliseconds
        try
        {
            String fetchValue = Integer.toString((interval*60)*1000);
            intervalInMs = Long.parseLong(fetchValue);
        }catch (NumberFormatException e)
        {
            e.printStackTrace();
        }

        long day = 3600000L * 24;
        long hour = day/24;

         //fetches the current time and sets it to the endTime
         Date intervalEndTime = cal.getTime();

         long intervalStartTime = 0L;

        //fetches from database per input host/port parameters dynamic startTime and a current time as endtTime
        //and  puts it into a hashmap with corresponding ip:port key
        HashMap<String, ArrayList<GcStats>> intervalReportsMap = new HashMap<>();

        intervalStartTime = cal.getTime().getTime()- intervalInMs;
        intervalReportsMap.put(host+":"+port,log.getGarbageCollectionStats(intervalStartTime, intervalEndTime.getTime(),host, port) );

        //Creates an arraylist of GcStats and fetches all GCstats entries for the current process through the set starttime and endtime above
        ArrayList<GcStats> currentReports = intervalReportsMap.get(host + ":" + port);
      //  System.out.println("DEBUG: currentReports gathered");

        if (currentReports != null) {
        //    System.out.println("DEBUG: currentReports not null");
            long minimumMemValue = 0;
            long originalMinimumMemValue = log.firstGcValue(host+":"+port);

            GcReport tempReport = new GcReport();
            for (GcStats g : currentReports) {
                tempReport.addGcStats(g);
           //     System.out.println("DEBUG: Adding gcStats");
            }

            long IntervalStartTimeOnSuspectedMemLeak = 0;

            try
            {
                int memConsecutiveIncCounter = 0;

                for (int j = 0; j < currentReports.size(); j++)
                {
                    //compares originalMinimumMemValue from last iteration with a new one on the current iteration,
                    // if the newer value is above originalMinimumMemValue, there will be a consecutive count
                    //as well as an intervalStartTime which will be set on the timeframe of the increase.
                    if (originalMinimumMemValue < currentReports.get(j).getMemoryUsedAfter())
                    {
                        if (IntervalStartTimeOnSuspectedMemLeak == 0)
                        {
                            IntervalStartTimeOnSuspectedMemLeak = currentReports.get(j).getTimeStamp();
                        }
                        memConsecutiveIncCounter++;
                    }
                    //if the next value is lower, then the counter is reset together with intervalStartTime.
                    else if (originalMinimumMemValue >= currentReports.get(j).getMemoryUsedAfter() && minimumMemValue != 0)
                    {
                        memConsecutiveIncCounter = 0;
                        IntervalStartTimeOnSuspectedMemLeak = 0;
                    }
                    minimumMemValue = currentReports.get(j).getMemoryUsedAfter();

                    //Debugging
                //    System.out.println("DEBUG: original first GCMinMemValue = "+originalMinimumMemValue);
                //    System.out.println("DEBUG: current GcMinMemValue = "+minimumMemValue);

                    //if we are on the last lap and the minimumGCMemUsage is above or equal the 10% threshhold of firstGCMemMinValue of this process
                    // then it's time to create a process report with a "SUSPECTED_MEMORY_LEAK" warning
                    if (j == currentReports.size() - 1 && minimumMemValue >= (originalMinimumMemValue * PERCENTAGE_INC_IN_MEM_USE_WARNING))
                    {
                        tempReport.setConsec_mem_inc_count(memConsecutiveIncCounter);

                        tempReport.setStatus(GcReport.Status.SUSPECTED_MEMORY_LEAK);
                     //   System.out.println("DEBUG: Suspected Memory Leak");
                        if (IntervalStartTimeOnSuspectedMemLeak != 0)
                        {
                            tempReport.setStartTime(IntervalStartTimeOnSuspectedMemLeak);
                        }

                        //If process has a suspected memory leak -> Create an AnomalyReport and fire AnomalyEvent (reports to listener)
                        AnomalyReport aReport = new AnomalyReport();
                        aReport.setAnomaly(AnomalyReport.Anomaly.SUSPECTED_MEMORY_LEAK);
                        aReport.setErrorMsg("Heap memory usage in PS OLD Gen above threshold.");
                        aReport.setHost(host);
                        aReport.setPort(port);
                        aReport.setMemIncreaseBytes(tempReport.getEndMemoryUsage() - originalMinimumMemValue);

                        //fetches the first GcReport for the specific port and hostname which has the possible memory leak status.
                        //this is done in order to determine if the memory leak started in an interval before the current one.
                        //if this is not the case, then the  IntervalStartTimeOnSuspectedMemLeak is used from the current interval.
                        ArrayList<GcReport> possibleMemLeakReports = log.getPossibleMemoryLeaks(host, port);

                        //if an earlier GcReport is found with status Possible memory leak
                        if (possibleMemLeakReports.size() > 0 && possibleMemLeakReports.get(0).getStartTime() < tempReport.getStartTime()) {
                            aReport.setStartTimeIncrease(possibleMemLeakReports.get(0).getStartTime());
                        }
                        //if no earlier  GCReport is found, then it is set to 0, Maybe set it to IntervalStartTimeOnSuspectedMemLeak?
                        else {
                            aReport.setStartTimeIncrease(tempReport.getStartTime());
                        }

                        aReport.setTimestamp(Calendar.getInstance().getTimeInMillis());

                        String endMemTemp = Long.toString(tempReport.getEndMemoryUsage());
                        double endMemUsage = Double.valueOf(endMemTemp);
                        String origMemUsage = Long.toString(originalMinimumMemValue);
                        double originalMemUsage = Double.valueOf(origMemUsage);

                        //calculates latest number which is bigger, with the original smaller value * 100 and
                        // then subtracts 100 to get percentage increase.
                        double calculateMemIncreasePercentage = (((endMemUsage/originalMemUsage)*100)-100);

                        int memIncPercTest = (int) calculateMemIncreasePercentage;
                        aReport.setMemIncreasePercentage(memIncPercTest);

                        //debug for getEndMemoryUsage which is used to calculated MemIncreasePercentage
               //         System.out.println("DEBUG: EndMemoryUsage: "+tempReport.getEndMemoryUsage());

                        //debug for mem increase percentage
                 //       System.out.println("DEBUG: Mem increase percentage: "+aReport.getMemIncreasePercentage());

                        //Debugging for anomalyReport
                //        System.out.println("DEBUG: AnomalyReport created with status: "+aReport.toString());
                        fireAnomalyEvent(aReport);


                    }
                    // if the last gcMinMemvalue is below the 10% threshold of the firstGcMinMemValue
                    else if (j == currentReports.size() - 1 && minimumMemValue < (originalMinimumMemValue * PERCENTAGE_INC_IN_MEM_USE_WARNING))
                    {
                        tempReport.setConsec_mem_inc_count(memConsecutiveIncCounter);
                        tempReport.setStatus(GcReport.Status.OK);
                        //if there has been 5 or more consecutive minMemoryIncreases in a row, then a processreport is created with a "LIKELY_MEMORY_LEAK"
                        if (memConsecutiveIncCounter >= DEFAULT_CONSECUTIVE_MEM_INC) {
                            tempReport.setStatus(GcReport.Status.POSSIBLE_MEMORY_LEAK);
                  //          System.out.println("DEBUG: Possible memory leak");
                            if (IntervalStartTimeOnSuspectedMemLeak != 0) {
                                tempReport.setStartTime(IntervalStartTimeOnSuspectedMemLeak);
                            }
                            //creates a GCReport log entry for every Possible memory leak.
                            if (minimumMemValue > originalMinimumMemValue)
                            {
                                log.sendGcReport(host, port, tempReport);
                            }
                        }
                        if (tempReport.getStatus().equals(GcReport.Status.OK))
                        {
                            log.clearPossibleMemoryLeaks(host, port);
                        }
                    }
                }

            } catch (NumberFormatException e) {
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
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        Date todayStartTime = cal.getTime();
        //Set endTime (today 23:59:59)
        cal.set(Calendar.AM_PM, Calendar.PM);
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
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStartTime = cal.getTime();
        //Set endTime (Sunday 23:59:59)
        cal.set(Calendar.AM_PM, Calendar.PM);
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
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DATE, 1);
        Date monthStartTime = cal.getTime();
        //Set endTime (Last day of the month 23:59:59)
        cal.set(Calendar.AM_PM, Calendar.PM);
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
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        Date todayStartTime = cal.getTime();
        //Set endTime (today 23:59:59)
        cal.set(Calendar.AM_PM, Calendar.PM);
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
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStartTime = cal.getTime();
        //Set endTime (Sunday 23:59:59)
        cal.set(Calendar.AM_PM, Calendar.PM);
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
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DATE, 1);
        Date monthStartTime = cal.getTime();
        //Set endTime (Last day of the month 23:59:59)
        cal.set(Calendar.AM_PM, Calendar.PM);
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

    private void fireAnomalyEvent(AnomalyReport aReport){
        for (AnomalyListener l : ad.getListeners()){
            Log.getInstance().sendAnomalyReport(aReport);
            l.anomalyFound(new AnomalyEvent(aReport));
        }
    }

    public void removeTimer(String host, int port){
        Timer timer = intervalTimers.get(host + ":" + port);
        timer.cancel();
        timer.purge();
    }

    public static long getTIME_EXCESSIVE_SCAN_WARNING() {
        return TIME_EXCESSIVE_SCAN_WARNING;
    }

    public static void setTIME_EXCESSIVE_SCAN_WARNING(long TIME_EXCESSIVE_SCAN_WARNING) {
        Analyzer.TIME_EXCESSIVE_SCAN_WARNING = TIME_EXCESSIVE_SCAN_WARNING;
    }

    public static double getPERCENTAGE_INC_IN_MEM_USE_WARNING() {
        return PERCENTAGE_INC_IN_MEM_USE_WARNING;
    }

    public static void setPERCENTAGE_INC_IN_MEM_USE_WARNING(double PERCENTAGE_INC_IN_MEM_USE_WARNING) {
        Analyzer.PERCENTAGE_INC_IN_MEM_USE_WARNING = PERCENTAGE_INC_IN_MEM_USE_WARNING;
    }
}
