package Logs;

/**
 * Created by Oliver on 2014-09-22.
 */
public class GcReport {
   /* public enum Trend{ @TODO Fix / remove Trend
        //Using more memory than before
        CONTINUOUSLY_GROWING,
        CHANGING_FROM_GROWING_TO_STABLE,
        CHANGING_FROM_STABLE_TO_GROWING,

        //Using less memory than before
        CONTINUOUSLY_DECREASING,
        CHANGING_FROM_DECREASING_TO_STABLE,
        CHANGING_FROM_STABLE_TO_DECREASING,

        //Memory usage practically unchanged
        STABLE,

        //Memory usage practically unchanged / Using more/less memory than before
        CHANGING_FROM_GROWING_TO_DECREASING,
        CHANGING_FROM_DECREASING_TO_GROWING
    }*/
    private long avgCollectionTime;
    private long minCollectionTime;
    private long maxCollectionTime;
    private long avgTimeBetweenGc;
    private long minTimeBetweenGc;
    private long maxTimeBetweenGc;
    private long avgCollected;
    private long minCollected;
    private long maxCollected;
    private long minMemoryUsage;
    private long maxMemoryUsage;
    private long avgMemoryUsage;
    private long startMemoryUsage;
    private long endMemoryUsage;
   // private ArrayList<Trend> trend;
    private long startTime;
    private long endTime;
    private int gcCount;
    private long avgMinMemoryUsage;

    /**
     * Constructor
     * @param avgTimeBetweenGc average time between GCs performed this period.
     * @param minTimeBetweenGc minimum time between two GCs for this period.
     * @param maxTimeBetweenGc maximum time between two GCs for this period.
     * @param avgCollected average memory collected for each GC for this period.
     * @param minCollected minimum memory collected from one GC for this period.
     * @param maxCollected maximum memory collected from one GC for this period.
     * @param minMemoryUsage minimum memory used after a GC for this period.
     * @param maxMemoryUsage maximum memory used after a GC for this period.
     * @param avgMemoryUsage average memory used after a GC for this period.
     * @param startMemoryUsage memory usage after first GC for this period.
     * @param endMemoryUsage memory usage after last GC for this period.
     * @param startTime start time for this period.
     * @param endTime end time for this period.
     * @param gcCount how many GCs where performed this period.
     */
    public GcReport(long avgCollectionTime, long minCollectionTime, long maxCollectionTime,
                    long avgTimeBetweenGc, long minTimeBetweenGc, long maxTimeBetweenGc,
                    long avgCollected, long minCollected, long maxCollected,
                    long minMemoryUsage, long maxMemoryUsage, long avgMemoryUsage,
                    long startMemoryUsage, long endMemoryUsage,
                    long startTime, long endTime, int gcCount, long avgMinMemoryUsage){
        this.avgCollectionTime = avgCollectionTime;
        this.minCollectionTime = minCollectionTime;
        this.maxCollectionTime = maxCollectionTime;
        this.avgTimeBetweenGc = avgTimeBetweenGc;
        this.minTimeBetweenGc = minTimeBetweenGc;
        this.maxTimeBetweenGc = maxTimeBetweenGc;
        this.avgCollected = avgCollected;
        this.minCollected = minCollected;
        this.maxCollected = maxCollected;
        this.minMemoryUsage = minMemoryUsage;
        this.maxMemoryUsage = maxMemoryUsage;
        this.avgMemoryUsage = avgMemoryUsage;
        this.startMemoryUsage = startMemoryUsage;
        this.endMemoryUsage = endMemoryUsage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.gcCount = gcCount;
        this.avgMinMemoryUsage = avgMinMemoryUsage;
    }

    public GcReport(){
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public long getAvgCollected() {
        return avgCollected;
    }

    public long getMinCollected() {
        return minCollected;
    }

    public long getMaxCollected() {
        return maxCollected;
    }

    public long getMinMemoryUsage() {
        return minMemoryUsage;
    }

    public long getMaxMemoryUsage() {
        return maxMemoryUsage;
    }

    public long getAvgMemoryUsage() {
        return avgMemoryUsage;
    }

/*
    public Trend getTrend() {
        //@TODO Trend
        return Trend.STABLE;
    } */

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartMemoryUsage() {
        return startMemoryUsage;
    }

    public long getEndMemoryUsage() {
        return endMemoryUsage;
    }

    public long getAvgTimeBetweenGc() {
        return avgTimeBetweenGc;
    }

    public long getMinTimeBetweenGc() {
        return minTimeBetweenGc;
    }

    public long getMaxTimeBetweenGc() {
        return maxTimeBetweenGc;
    }

    public int getGcCount() {
        return gcCount;
    }

    public long getAvgCollectionTime() {
        return avgCollectionTime;
    }

    public long getMinCollectionTime() {
        return minCollectionTime;
    }

    public long getMaxCollectionTime() {
        return maxCollectionTime;
    }

    public long getAvgMinMemoryUsage() {
        return avgMinMemoryUsage;
    }

    public void setAvgCollected(long avgCollected) {
        this.avgCollected = avgCollected;
    }

    public void setMinCollected(long minCollected) {
        this.minCollected = minCollected;
    }

    public void setMaxCollected(long maxCollected) {
        this.maxCollected = maxCollected;
    }

    public void setMinMemoryUsage(long minMemoryUsage) {
        this.minMemoryUsage = minMemoryUsage;
    }

    public void setMaxMemoryUsage(long maxMemoryUsage) {
        this.maxMemoryUsage = maxMemoryUsage;
    }

    public void setAvgMemoryUsage(long avgMemoryUsage) {
        this.avgMemoryUsage = avgMemoryUsage;
    }

/*
    public void setTrend(Trend trend) {
        //@TODO Trend
    }*/

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setStartMemoryUsage(long startMemoryUsage) {
        this.startMemoryUsage = startMemoryUsage;
    }

    public void setEndMemoryUsage(long endMemoryUsage) {
        this.endMemoryUsage = endMemoryUsage;
    }

    public void setAvgTimeBetweenGc(long avgTimeBetweenGc) {
        this.avgTimeBetweenGc = avgTimeBetweenGc;
    }

    public void setMinTimeBetweenGc(long minTimeBetweenGc) {
        this.minTimeBetweenGc = minTimeBetweenGc;
    }

    public void setMaxTimeBetweenGc(long maxTimeBetweenGc) {
        this.maxTimeBetweenGc = maxTimeBetweenGc;
    }

    public void setGcCount(int gcCount) {
        this.gcCount = gcCount;
    }

    public void setAvgCollectionTime(long avgCollectionTime) {
        this.avgCollectionTime = avgCollectionTime;
    }

    public void setMinCollectionTime(long minCollectionTime) {
        this.minCollectionTime = minCollectionTime;
    }

    public void setMaxCollectionTime(long maxCollectionTime) {
        this.maxCollectionTime = maxCollectionTime;
    }

    public void setAvgMinMemoryUsage(long avgMinMemoryUsage) {
        this.avgMinMemoryUsage = avgMinMemoryUsage;
    }

    public void addGcReport(GcReport ags){
        //Avg time between GCs
        this.avgTimeBetweenGc = (avgTimeBetweenGc + ags.getAvgTimeBetweenGc()) / 2;
        //Min/Max time between GCs
        if (ags.getMinTimeBetweenGc() < this.getMinTimeBetweenGc()){
            this.setMinTimeBetweenGc(ags.getMinTimeBetweenGc());
        }
        if (ags.getMaxTimeBetweenGc() > this.getMaxTimeBetweenGc()){
            this.setMaxTimeBetweenGc(ags.getMaxTimeBetweenGc());
        }

        //Avg collected
        this.avgCollected = (avgCollected + ags.getAvgCollected()) / 2;
        //Min/Max collected
        if (ags.getMinCollected() < this.getMinCollected()){
            this.setMinCollected(ags.getMinCollected());
        }
        if (ags.getMaxCollected() > this.getMaxCollected()){
            this.setMaxCollected(ags.getMaxCollected());
        }

        //Avg memory usage
        this.avgMemoryUsage = (avgMemoryUsage + ags.getAvgMemoryUsage()) / 2;
        //Min/Max memory usage
        if (ags.getMinMemoryUsage() < this.getMinMemoryUsage()){
            this.setMinMemoryUsage(ags.getMinMemoryUsage());
        }
        if (ags.getMaxMemoryUsage() > this.getMaxMemoryUsage()){
            this.setMaxMemoryUsage(ags.getMaxMemoryUsage());
        }

        //Start/End memory usage (Dependent on time of GC)
        //And start/end time
        if (ags.getStartTime() < this.getStartTime()){
            this.setStartMemoryUsage(ags.getStartMemoryUsage());
            this.setStartTime(ags.getStartTime());
        }
        if (ags.getEndTime() > this.getEndTime()){
            this.setEndMemoryUsage(ags.getEndMemoryUsage());
            this.setEndTime(ags.getEndTime());
        }

        //GcCount
        this.gcCount += ags.gcCount;

        //Average minMemoryUsage
        this.avgMinMemoryUsage = (avgMinMemoryUsage + ags.avgMinMemoryUsage) / 2;
    }
}
