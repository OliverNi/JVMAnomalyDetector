package AnomalyDetector;

import Logs.GcReport;
import Logs.GcStats;
import Logs.ILogging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by Oliver on 2014-09-18.
 */
public class Analyzer {
    //@TODO Add timer to combine GcReports each day/week/month
    public static final int DIFFERENCE_ALLOWED = 20;
    private AnomalyDetector ad;
    private JMXAgent agent;
    private ILogging log;
    public Analyzer(JMXAgent agent){
        this.log = ad.getLog();
    }


    public void analyzeHourlyGc(){

    }

    /**
     * Analyze GcReports by comparing a daily GcReport to the previous day's GcReport
     */
    public void analyzeDailyGC(){

    }

    /**
     * Analyze GcReports by comparing a weekly GcReport to the previous weeks's GcReport
     */
    public void analyzeWeeklyGC(){

    }

    /**
     * Analyze GcReports by comparing a monthly GcReport to the previous month's GcReport
     */
    public void analyzeMonthlyGC(){

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
    private GcReport combineAnalyzedGcStats(ArrayList<GcReport> analyzedStats) {
        GcReport combined = new GcReport();

        for (GcReport a : analyzedStats){
            combined.addGcReport(a);
        }

        return combined;
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
            log.sendAnalyzedGCData(hostPort[0], port, combineDailyGcStats(gcStats));
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
            log.sendAnalyzedGCData(hostPort[0], port, combineGcReports(dailyReports));
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
            log.sendAnalyzedGCData(hostPort[0], port, combineGcReports(dailyReports));
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
