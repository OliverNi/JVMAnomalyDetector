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
    public static final int DIFFERENCE_ALLOWED = 20;
    private AnomalyDetector ad;
    private JMXAgent agent;
    private ILogging log;
    public Analyzer(JMXAgent agent){
        this.log = ad.getLog();
    }

    /**
     * Analyze
     */
    public void analyzeDailyGC(){

    }

    private GcReport analyzeGcStats(ArrayList<GcStats> gcStats){
        GcReport analyzed = new GcReport();
        long[] collected = new long[gcStats.size()];
        long[] usedAfter = new long[gcStats.size()];
        long[] timePerformed = new long[gcStats.size()];
        //Time between two GCs
        long[] timeBetweenGc;
        if (gcStats.size() % 2 == 0){
            timeBetweenGc = new long[gcStats.size() - 1];
        }
        else{
            timeBetweenGc = new long[gcStats.size()-2];
        }
        //Set gcCount
        analyzed.setGcCount(gcStats.size());

        //Set start memory usage (First GC of the day)
        analyzed.setStartMemoryUsage(gcStats.get(0).getMemoryUsedAfter());

        int count = 0;
        for (GcStats g : gcStats){
            //Memory used
            usedAfter[count] = g.getMemoryUsedAfter();
            if (usedAfter[count] < analyzed.getMinMemoryUsage()){
                analyzed.setMinMemoryUsage(usedAfter[count]);
            }
            if (usedAfter[count] > analyzed.getMaxMemoryUsage()){
                analyzed.setMaxMemoryUsage(g.getMemoryUsedAfter());
            }
            //Memory collected
            collected[count] = g.getAmountCollected();
            if (collected[count] < analyzed.getMinCollected()){
                analyzed.setMinCollected(collected[count]);
            }
            if (collected[count] > analyzed.getMaxCollected()){
                analyzed.setMaxCollected(collected[count]);
            }
            //Time performed
            timePerformed[count] = g.getTimeStamp();
            if (timePerformed[count] < analyzed.getStartTime()){
                analyzed.setStartTime(timePerformed[count]);
            }
            if (timePerformed[count] > analyzed.getEndTime()){
                analyzed.setEndTime(timePerformed[count]);
            }
            //Time between GCs
            if (count % 2 == 0){
                timeBetweenGc[count - 1] = timePerformed[count] - timePerformed[count-1];
                if (timeBetweenGc[count-1] < analyzed.getMinTimeBetweenGc()){
                    analyzed.setMinTimeBetweenGc(timeBetweenGc[count-1]);
                }
                else if (timeBetweenGc[count-1] > analyzed.getMaxTimeBetweenGc()){
                    analyzed.setMaxTimeBetweenGc(timeBetweenGc[count-1]);
                }
            }
            /* @TODO Fix / remove Trend
            //Trend first half of the day
            if (count == gcStats.size()/2){
                if (analyzed.getStartMemoryUsage() + DIFFERENCE_ALLOWED > usedAfter[count]){
                    analyzed.setTrend(GcReport.Trend.CONTINUOUSLY_DECREASING);
                }
                else if (analyzed.getStartMemoryUsage() + DIFFERENCE_ALLOWED < usedAfter[count]){
                    analyzed.setTrend(GcReport.Trend.CONTINUOUSLY_GROWING);
                }
                else if ((analyzed.getStartMemoryUsage() - usedAfter[count]) <= DIFFERENCE_ALLOWED){
                    analyzed.setTrend(GcReport.Trend.STABLE);
                }
            } */
        }

        //Set end memory usage (Last GC of the day)
        analyzed.setEndMemoryUsage(usedAfter[count]);

        /* //@TODO Fix / remove Trend
        //Trend second half of the day
        long midValue = usedAfter[gcStats.size()/2];
        long endValue = analyzed.getEndMemoryUsage();
        if (analyzed.getTrend() == GcReport.Trend.CONTINUOUSLY_DECREASING){
            if (midValue + DIFFERENCE_ALLOWED < endValue){
                analyzed.setTrend(GcReport.Trend.CHANGING_FROM_DECREASING_TO_GROWING);
            }
            else if ((midValue - endValue) <= DIFFERENCE_ALLOWED){
                analyzed.setTrend(GcReport.Trend.CHANGING_FROM_DECREASING_TO_STABLE);
            }
        }
        else if (analyzed.getTrend() == GcReport.Trend.CONTINUOUSLY_GROWING){
            if (midValue + DIFFERENCE_ALLOWED > endValue){
                analyzed.setTrend(GcReport.Trend.CHANGING_FROM_GROWING_TO_DECREASING);
            }
            else if ((midValue - endValue) <= DIFFERENCE_ALLOWED) {
                analyzed.setTrend(GcReport.Trend.CHANGING_FROM_GROWING_TO_STABLE);
            }
        }
        else if (analyzed.getTrend() == GcReport.Trend.STABLE){
            if (midValue + DIFFERENCE_ALLOWED > endValue){
                analyzed.setTrend(GcReport.Trend.CHANGING_FROM_STABLE_TO_DECREASING);
            }
            if (midValue + DIFFERENCE_ALLOWED < endValue){
                analyzed.setTrend(GcReport.Trend.CHANGING_FROM_STABLE_TO_GROWING);
            }
        }*/

        //Calculate Average
        analyzed.setAvgTimeBetweenGc(calcAvg(timeBetweenGc));
        analyzed.setAvgCollected(calcAvg(collected));
        analyzed.setAvgMemoryUsage(calcAvg(usedAfter));

        return analyzed;
    }


    public void combineDailyGcStats(){
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
            log.sendAnalyzedGCData(hostPort[0], port, analyzeGcStats(gcStats));
        }
    }
    private GcReport combineAnalyzedGcStats(ArrayList<GcReport> analyzedStats) {
        GcReport combined = new GcReport();

        for (GcReport a : analyzedStats){
            combined.addAnalyzedStatistics(a);
        }

        return combined;
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
