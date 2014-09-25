package Logs;

import AnomalyDetector.AnomalyReport;

/**
 * Created by Oliver on 2014-09-25.
 */

/**
 * An Analyzed GC Report contains
 */
public class AnalyzedGcReport {
    double avgCollectionTimeDif;
    double avgTimeBetweenGcDif;
    double avgCollectedDif;
    double avgMemoryUsageDif;
    GcReport[] gcReports = new GcReport[2];

    public AnalyzedGcReport(){
        this.avgCollectionTimeDif = 0;
        this.avgTimeBetweenGcDif = 0;
        this.avgCollectedDif = 0;
        this.avgMemoryUsageDif = 0;
    }

    public AnalyzedGcReport analyze(GcReport r1, GcReport r2){
        if (r1.getStartTime() < r2.getStartTime()) {
            gcReports[0] = r1;
            gcReports[1] = r2;
        }
        else{
            gcReports[0] = r2;
            gcReports[1] = r1;
        }
        calcAvgCollectedDif();
        calcAvgCollectionTimeDif();
        calcAvgMemoryUsageDif();
        calcAvgTimeBetweenGcDif();

        return this;
    }

    private void calcAvgCollectionTimeDif(){
        this.avgCollectionTimeDif = (gcReports[0].getAvgCollectionTime() / gcReports[1].getAvgCollectionTime());
    }

    private void calcAvgCollectedDif(){
        this.avgCollectedDif = (gcReports[0].getAvgCollected() / gcReports[1].getAvgCollected());
    }

    private void calcAvgMemoryUsageDif(){
        this.avgMemoryUsageDif = (gcReports[0].getAvgMemoryUsage() / gcReports[1].getAvgMemoryUsage());
    }

    private void calcAvgTimeBetweenGcDif(){
        this.avgTimeBetweenGcDif = (gcReports[0].getAvgTimeBetweenGc() / gcReports[1].getAvgTimeBetweenGc());
    }

    public GcReport getReport(int index){
        if (index > gcReports.length - 1 || index < 0){
            return null;
        }
        else{
            return gcReports[index];
        }
    }

    public long getStartTime(){
        if (gcReports.length > 0)
            return gcReports[0].getStartTime();
        else
            return 0;
    }

    public long getEndTime(){
        if (gcReports.length > 1)
            return gcReports[1].getEndTime();
        else if (gcReports.length > 0)
            return gcReports[0].getEndTime();
        else
            return 0;
    }

    public double getAvgCollectionTimeDif() {
        return avgCollectionTimeDif;
    }

    public double getAvgTimeBetweenGcDif() {
        return avgTimeBetweenGcDif;
    }

    public double getAvgCollectedDif() {
        return avgCollectedDif;
    }

    public double getAvgMemoryUsageDif() {
        return avgMemoryUsageDif;
    }
}
