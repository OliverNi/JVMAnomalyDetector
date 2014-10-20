package Logs;

/**
 * Created by Oliver on 2014-09-25.
 */

/**
 * An Analyzed GC Report contains statistics from compared daily/weekly/monthly GcReports.
 */
public class AnalyzedGcReport {
    public enum Type{
        HOURLY,
        DAILY,
        WEEKLY,
        MONTHLY,
        UNKNOWN
    }
    double avgCollectionTimeDif;
    double avgTimeBetweenGcDif;
    double avgCollectedDif;
    double avgMemoryUsageDif;
    double avgMinMemoryUsageDif;
    private long minMemoryUsage;
    private long maxMemoryUsage;
    private long endMemoryUsage;
    private Type type;

    GcReport[] gcReports = new GcReport[2];

    public AnalyzedGcReport(){
        this.avgCollectionTimeDif = 0;
        this.avgTimeBetweenGcDif = 0;
        this.avgCollectedDif = 0;
        this.avgMemoryUsageDif = 0;
        this.avgMinMemoryUsageDif = 0;
        this.minMemoryUsage = 0;
        this.maxMemoryUsage = 0;
        this.type = Type.UNKNOWN;
    }

    /**
     * Determines which of the two incoming reports is the starting one, and puts it first in an array
     * it then performs comparisons between the reports to get the percentage increase value on different variables
     */
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
        calcAvgMinMemUsageDif();
        setType();
        setMinMemoryUsage();
        setMaxMemoryUsage();
        setEndMemoryUsage();
        return this;
    }

    private void setType(){
        type = Type.UNKNOWN;

        //Under 80 min = HOURLY (20 min margin)
        long hourly = 4800000;
        //Under 25 hours = DAILY (1 hour margin)
        long daily = 90000000;
        //Under one week + 2 hours = WEEKLY (2 hour margin)
        long weekly = 612000000;
        //Under One month + 4 hours = MONTHLY (4 hour margin)
        long monthly = (long)2433600000L;
        if (gcReports[0].getDuration() <= hourly){
            type = Type.HOURLY;
        }
        else if (gcReports[0].getDuration() <= daily){
            type = Type.DAILY;
        }
        else if (gcReports[0].getDuration() <= weekly){
            type = Type.WEEKLY;
        }
        else if (gcReports[0].getDuration() <= monthly){
            type = Type.MONTHLY;
        }
    }

    private void setMinMemoryUsage(){
        if (gcReports[0].getMinMemoryUsage() < gcReports[1].getMinMemoryUsage())
            minMemoryUsage = gcReports[0].getMinMemoryUsage();
        else
            minMemoryUsage = gcReports[1].getMinMemoryUsage();
    }

    private void setMaxMemoryUsage(){
        if (gcReports[0].getMaxMemoryUsage() < gcReports[1].getMaxMemoryUsage())
            minMemoryUsage = gcReports[0].getMaxMemoryUsage();
        else
            minMemoryUsage = gcReports[1].getMaxMemoryUsage();
    }

    private void setEndMemoryUsage(){
        endMemoryUsage = gcReports[1].getEndMemoryUsage();
    }
    /**
     *   calculates the average memory usage difference between two reports in percent
     */
    private void calcAvgMinMemUsageDif()
    {
        this.avgMinMemoryUsageDif = (gcReports[1].getAvgMinMemoryUsage() / gcReports[0].getAvgMinMemoryUsage());
    }

    /**
     * calculates the average collection time difference between two reports in percent
     */
    private void calcAvgCollectionTimeDif(){
        this.avgCollectionTimeDif = (gcReports[1].getAvgCollectionTime() / gcReports[0].getAvgCollectionTime());
    }

    /**
     * calculates the average collected amount of executed Garbage collections between two reports in percent
     */
    private void calcAvgCollectedDif(){
        this.avgCollectedDif = (gcReports[1].getAvgCollected() / gcReports[0].getAvgCollected());
    }

    /**
     * calculates the average memory usage difference between two reports in percent
     */
    private void calcAvgMemoryUsageDif(){
        this.avgMemoryUsageDif = (gcReports[1].getAvgMemoryUsage() / gcReports[0].getAvgMemoryUsage());
    }

    /**
     * calculates the average time between executed garbage collection difference between two reports
     */
    private void calcAvgTimeBetweenGcDif(){
        this.avgTimeBetweenGcDif = (gcReports[1].getAvgTimeBetweenGc() / gcReports[0].getAvgTimeBetweenGc());
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

    public double getAvgMinMemoryUsageDif() {
        return avgMinMemoryUsageDif;
    }

    public Type getType() {
        return type;
    }

    public GcReport[] getGcReports() {
        return gcReports;
    }
}
