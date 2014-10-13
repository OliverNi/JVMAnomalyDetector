package AnomalyDetector;

import Listeners.AnomalyEvent;
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
            analyzeIntervalGc();
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
    class IntervalTask extends TimerTask{
        IntervalTask(){
        }
        public void run(){
            analyzeIntervalGc();
        }
    }

    public static final double DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING = 1.1;
    public static final double DEFAULT_CONSECUTIVE_MEM_INC = 1;
    private AnomalyDetector ad;
    private ILogging log;
    Timer hourlyTimer; //@TODO remove?
    Timer dailyTimer;
    Timer weeklyTimer;
    Timer monthlyTimer;
    Timer intervalTimer;


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
        ArrayList<Timer> intervalTimers = new ArrayList<>();
        long minute = 60000L;
        long hour = 3600000L;
        long day = hour * 24;
        long week = day * 7;
        long month = day * 30;
        Date firstTime;
        Calendar cal = Calendar.getInstance();
        //Hourly task
        cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + 1);
        firstTime = cal.getTime();


        /*
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
        */

        for (int i = 0; i < ad.getProcessConnections().size(); i++) {
            int firstInterval = ad.getInterval(ad.getProcessConnections().get(i).getHostName(),
                    ad.getProcessConnections().get(i).getPort());
            intervalTimers.add(new Timer());
            intervalTimers.get(i).scheduleAtFixedRate(new IntervalTask(), firstInterval * minute, firstInterval * minute);
        }

    }


    public void analyzeIntervalGc()
    {
        System.out.println("DEBUG: Entered interval analysis");
            Calendar cal = Calendar.getInstance();

            //fetches all current processes in the format of ip:port
           // ArrayList<String> connections = ad.getConnections(); //@TODO You can use getProcessConnections to save som hassle.
        ArrayList<ProcessConnection> connections = ad.getProcessConnections();




            int amountOfConnections = 0;
            amountOfConnections = ad.getConnections().size();
            int[] intervalInMinutes = new int[amountOfConnections];
            Long[] intervalInMs = new Long[intervalInMinutes.length];

            //fetches each interval in minutes from each monitored process
            for(int i=0; i<amountOfConnections; i++)
            {
                try
                {

                    String hostName = connections.get(i).getHostName();
                    int port = connections.get(i).getPort();
                    intervalInMinutes[i] = ad.getInterval(hostName, port);
                    System.out.println("DEBUG: interval analysis: Connection: " + hostName + ":" + port);
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
                    String fetchValue = Integer.toString((intervalInMinutes[i]*60)*1000);
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

                String hostName = connections.get(i).getHostName();
                int port = connections.get(i).getPort();
                intervalReportsMap.put(connections.get(i).getHostName()+":"+connections.get(i).getPort(),log.getGarbageCollectionStats(intervalStartTime, intervalEndTime.getTime(),hostName, port)  );
            }

            //for each process, a new GCstats is created
            for (int i = 0;  i < connections.size(); i++)
            {
                //Creates an arraylist of GcStats and fetches all GCstats entries for the current process through the set starttime and endtime above
                ArrayList<GcStats> currentReports = intervalReportsMap.get(connections.get(i).getHostName() + ":" + connections.get(i).getPort());


                if (currentReports != null) { //@TODO Debug this, always null? or is this okay?
                    System.out.println("DEBUG: currentReports not null");
                    long minimumMemValue = 0;
                    long originalMinimumMemValue = log.firstGcValue(connections.get(i).getHostName()+":"+connections.get(i).getPort());

                    GcReport tempReport = new GcReport();
                    for (GcStats g : currentReports) {
                        tempReport.addGcStats(g);
                        System.out.println("DEBUG: Adding gcStats");
                    }

                    long IntervalStartTimeOnSuspectedMemLeak = 0;

                    try {
                        int memConsecutiveIncCounter = 0;
                        //fetches ip for the current process
                       // String[] hostPort = connections.get(i).split(":");
                        //fetches port for the current process
                      //  int port = Integer.parseInt(hostPort[1]);

                        for (int j = 0; j < currentReports.size(); j++) {
                            //compares originalMinimumMemValue from last iteration with a new one on the current iteration, if the newer value is above originalMinimumMemValue, there will be a consecutive count
                            //as well as an intervalStartTime which will be set on the timeframe of the increase.
                            if (originalMinimumMemValue < currentReports.get(j).getMemoryUsedAfter()) {
                                if (IntervalStartTimeOnSuspectedMemLeak == 0) {
                                    IntervalStartTimeOnSuspectedMemLeak = cal.getTimeInMillis();
                                }
                                memConsecutiveIncCounter++;
                            }
                            //if the next value is lower, then the counter is reset together with intervalStartTime.
                            else if (originalMinimumMemValue >= currentReports.get(j).getMemoryUsedAfter() && minimumMemValue != 0) {
                                memConsecutiveIncCounter = 0;
                                IntervalStartTimeOnSuspectedMemLeak = 0;
                            }
                            minimumMemValue = currentReports.get(j).getMemoryUsedAfter();

                            //if the value equals or is above 10% threshhold of firstGCMinMemValue then the intervalStartTime is set to this timeframe,
                            // that is if the intervalStartTime has not already been set on another timeframe of a start of a slow increase for example.
                            if (minimumMemValue >= (originalMinimumMemValue * DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING) && minimumMemValue != 0) {
                                if (IntervalStartTimeOnSuspectedMemLeak == 0) {
                                    IntervalStartTimeOnSuspectedMemLeak = cal.getTimeInMillis();
                                }
                            }

                            //if we are on the last lap and the minimumGCMemUsage is above or equal the 10% threshhold of firstGCMemMinValue of this process
                            // then it's time to create a process report with a "SUSPECTED_MEMORY_LEAK" warning
                            if (j == currentReports.size() - 1 && minimumMemValue >= (originalMinimumMemValue * DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING)) {
                                tempReport.setConsec_mem_inc_count(memConsecutiveIncCounter);

                                tempReport.setStatus(GcReport.Status.SUSPECTED_MEMORY_LEAK);
                                System.out.println("DEBUG: Suspected Memory Leak");
                                if (IntervalStartTimeOnSuspectedMemLeak != 0) {
                                    tempReport.setStartTime(IntervalStartTimeOnSuspectedMemLeak);
                                }

                                //If process has a suspected memory leak -> Create an AnomalyReport and fire AnomalyEvent (reports to listener)
                                AnomalyReport aReport = new AnomalyReport();
                                aReport.setAnomaly(AnomalyReport.Anomaly.SUSPECTED_MEMORY_LEAK);
                                aReport.setErrorMsg("Heap memory usage in PS OLD Gen above threshold.");
                                aReport.setHost(connections.get(i).getHostName());
                                aReport.setPort(connections.get(i).getPort());
                                aReport.setMemIncreaseBytes(tempReport.getEndMemoryUsage() - originalMinimumMemValue);

                                //fetches the first GcReport for the specific port and hostname which has the possible memory leak status.
                                //this is done in order to determine if the memory leak started in an interval before the current one.
                                //if this is not the case, then the  IntervalStartTimeOnSuspectedMemLeak is used from the current interval.
                                ArrayList<GcReport> possibleMemLeakReports = log.getPossibleMemoryLeaks(connections.get(i).getHostName(), connections.get(i).getPort());

                                //if an earlier GcReport is found with status Possible memory leak
                                if (possibleMemLeakReports.size() > 0 && possibleMemLeakReports.get(0).getStartTime() < tempReport.getStartTime()) {
                                    aReport.setStartTimeIncrease(possibleMemLeakReports.get(0).getStartTime());
                                }
                                //if no earlier  GCReport is found, then it is set to 0, Maybe set it to IntervalStartTimeOnSuspectedMemLeak?
                                else {
                                    aReport.setStartTimeIncrease(0);
                                }

                                aReport.setTimestamp(Calendar.getInstance().getTimeInMillis());
                                aReport.setMemIncreasePercentage(tempReport.getEndMemoryUsage() / originalMinimumMemValue);


                                //Debugging for anomalyReport
                                System.out.println("DEBUG: AnomalyReport created with status: "+aReport.toString());
                                fireAnomalyEvent(aReport);


                            }
                            // if the last gcMinMemvalue is below the 10% threshold of the firstGcMinMemValue
                            else if (j == currentReports.size() - 1 && minimumMemValue < (originalMinimumMemValue * DEFAULT_PERCENTAGE_INC_IN_MEM_USE_WARNING)) {
                                tempReport.setConsec_mem_inc_count(memConsecutiveIncCounter);
                                tempReport.setStatus(GcReport.Status.OK);
                                //if there has been 5 or more consecutive minMemoryIncreases in a row, then a processreport is created with a "LIKELY_MEMORY_LEAK"
                                if (memConsecutiveIncCounter >= DEFAULT_CONSECUTIVE_MEM_INC) {
                                    tempReport.setStatus(GcReport.Status.POSSIBLE_MEMORY_LEAK);
                                    System.out.println("DEBUG: Possible memory leak");
                                    if (IntervalStartTimeOnSuspectedMemLeak != 0) {
                                        tempReport.setStartTime(IntervalStartTimeOnSuspectedMemLeak);
                                    }
                                    //creates a GCReport log entry for every Possible memory leak.
                                    if (minimumMemValue > originalMinimumMemValue) {
                                        log.sendGcReport(connections.get(i).getHostName(), connections.get(i).getPort(), tempReport);
                                    }
                                }
                                if (tempReport.getStatus().equals(GcReport.Status.OK)) {
                                    log.clearPossibleMemoryLeaks(connections.get(i).getHostName(), connections.get(i).getPort());
                                }
                            }
                            log.sendUsageAfterLastGc(tempReport.getEndMemoryUsage(), connections.get(i).getHostName(), connections.get(i).getPort());
                        }


                    } catch (NumberFormatException e) {
                        e.printStackTrace();
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

    private void analyzeProcessReport(ProcessReport pReport){
        //@TODO Implement analysis of pReport

        //If Anomaly Found - Create AnomalyReport
        AnomalyReport aReport = pReport.createAnomalyReport();
        fireAnomalyEvent(aReport);


    }

    private void fireAnomalyEvent(AnomalyReport aReport){
        if (ad.getListener() != null){
            AnomalyEvent e = new AnomalyEvent(aReport);
            ad.getListener().anomalyFound(e);
        }
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
