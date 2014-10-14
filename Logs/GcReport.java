package Logs;

import AnomalyDetector.AnomalyReport;

/**
 * Created by Oliver on 2014-09-22.
 */
public class GcReport {
    public enum Period{
        MIXED,
        DAILY,
        WEEKLY,
        MONTHLY
    }
    public enum Status
    {
        LIKELY_MEMORY_LEAK,
        SUSPECTED_MEMORY_LEAK,
        POSSIBLE_MEMORY_LEAK,
        EXCESSIVE_GC_SCAN,
        OK,
        UNKNOWN
    }
    private long sumCollectionTime;
    private long minCollectionTime;
    private long maxCollectionTime;
    private long sumTimeBetweenGc;
    private long minTimeBetweenGc;
    private long maxTimeBetweenGc;
    private long sumCollected;
    private long minCollected;
    private long maxCollected;
    private long minMemoryUsage; //@TODO Check order of every attribute, corresponding with Log?
    private long maxMemoryUsage;
    private long sumMemoryUsage;
    private long startMemoryUsage;
    private long endMemoryUsage;
    private long startTime;
    private long endTime;
    private int gcCount;
    private int reportCount;
    private long sumMinMemoryUsage;
    private Status status;
    //keeps a track on if the minimumMemvalue after each GC has increased, and counts how many times in a row it has increased.
    private int consec_mem_inc_count;
    private Period period; //@TODO Implement Log

    /**
     * Constructor
     * @param sumTimeBetweenGc sum of time between GCs performed this period (used to calc avg).
     * @param minTimeBetweenGc minimum time between two GCs for this period.
     * @param maxTimeBetweenGc maximum time between two GCs for this period.
     * @param sumCollected average memory collected for each GC for this period.
     * @param minCollected minimum memory collected from one GC for this period.
     * @param maxCollected maximum memory collected from one GC for this period.
     * @param minMemoryUsage minimum memory used after a GC for this period.
     * @param maxMemoryUsage maximum memory used after a GC for this period.
     * @param sumMemoryUsage average memory used after a GC for this period.
     * @param startMemoryUsage memory usage after first GC for this period.
     * @param endMemoryUsage memory usage after last GC for this period.
     * @param startTime start time for this period.
     * @param endTime end time for this period.
     * @param gcCount how many GCs where performed this period.
     * @param reportCount how man reports this report is based on.
     * @param sumMinMemoryUsage the sum of every minMemoryUsage in every report (used to calc avg).
     */
    public GcReport(long sumCollectionTime, long minCollectionTime, long maxCollectionTime,
                    long sumTimeBetweenGc, long minTimeBetweenGc, long maxTimeBetweenGc,
                    long sumCollected, long minCollected, long maxCollected,
                    long minMemoryUsage, long maxMemoryUsage, long sumMemoryUsage,
                    long startMemoryUsage, long endMemoryUsage,
                    long startTime, long endTime, int gcCount, int reportCount, long sumMinMemoryUsage){
        this.sumCollectionTime = sumCollectionTime;
        this.minCollectionTime = minCollectionTime;
        this.maxCollectionTime = maxCollectionTime;
        this.sumTimeBetweenGc = sumTimeBetweenGc;
        this.minTimeBetweenGc = minTimeBetweenGc;
        this.maxTimeBetweenGc = maxTimeBetweenGc;
        this.sumCollected = sumCollected;
        this.minCollected = minCollected;
        this.maxCollected = maxCollected;
        this.minMemoryUsage = minMemoryUsage;
        this.maxMemoryUsage = maxMemoryUsage;
        this.sumMemoryUsage = sumMemoryUsage;
        this.startMemoryUsage = startMemoryUsage;
        this.endMemoryUsage = endMemoryUsage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.gcCount = gcCount;
        this.reportCount = reportCount;
        this.sumMinMemoryUsage = sumMinMemoryUsage;
        this.status = Status.UNKNOWN;

    }

    public GcReport(){
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public long getAvgCollected() {
        if (gcCount == 0)
            return 0;
        return sumCollected / gcCount;
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
        if (gcCount == 0)
            return 0;
        return sumMemoryUsage / gcCount;
    }

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
        if (gcCount == 0)
            return 0;
        return sumTimeBetweenGc / gcCount;
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
        if (gcCount == 0)
            return 0;
        return sumCollectionTime / gcCount;
    }

    public long getMinCollectionTime() {
        return minCollectionTime;
    }

    public long getMaxCollectionTime() {
        return maxCollectionTime;
    }

    public long getAvgMinMemoryUsage() {
        return sumMinMemoryUsage / gcCount;
    }

    public long getSumCollectionTime() {
        return sumCollectionTime;
    }

    public long getSumTimeBetweenGc() {
        return sumTimeBetweenGc;
    }

    public long getSumCollected() {
        return sumCollected;
    }

    public long getSumMemoryUsage() {
        return sumMemoryUsage;
    }

    public int getReportCount() {
        return reportCount;
    }

    public long getSumMinMemoryUsage() {
        return sumMinMemoryUsage;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
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

    public void setMinTimeBetweenGc(long minTimeBetweenGc) {
        this.minTimeBetweenGc = minTimeBetweenGc;
    }

    public void setMaxTimeBetweenGc(long maxTimeBetweenGc) {
        this.maxTimeBetweenGc = maxTimeBetweenGc;
    }

    public void setGcCount(int gcCount) {
        this.gcCount = gcCount;
    }

    public void setSumCollectionTime(long sumCollectionTime) {
        this.sumCollectionTime = sumCollectionTime;
    }

    public void setMinCollectionTime(long minCollectionTime) {
        this.minCollectionTime = minCollectionTime;
    }

    public void setMaxCollectionTime(long maxCollectionTime) {
        this.maxCollectionTime = maxCollectionTime;
    }

    public void setSumMinMemoryUsage(long sumMinMemoryUsage) {
        this.sumMinMemoryUsage = sumMinMemoryUsage;
    }

    public void setSumTimeBetweenGc(long sumTimeBetweenGc) {
        this.sumTimeBetweenGc = sumTimeBetweenGc;
    }

    public void setSumCollected(long sumCollected) {
        this.sumCollected = sumCollected;
    }

    public void setSumMemoryUsage(long sumMemoryUsage) {
        this.sumMemoryUsage = sumMemoryUsage;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setConsec_mem_inc_count(int consec_mem_inc_count) {
        this.consec_mem_inc_count = consec_mem_inc_count;
    }

    public long getDuration(){
        return endTime - startTime;
    }

    public Status getStatus() {
        return status;
    }

    public int getConsec_mem_inc_count() {
        return consec_mem_inc_count;
    }

    public void addGcReport(GcReport ags){
        //Avg time between GCs
        this.sumTimeBetweenGc = (sumTimeBetweenGc + ags.getAvgTimeBetweenGc()) / 2;
        //Min/Max time between GCs
        if (ags.getMinTimeBetweenGc() < this.getMinTimeBetweenGc()){
            this.setMinTimeBetweenGc(ags.getMinTimeBetweenGc());
        }
        if (ags.getMaxTimeBetweenGc() > this.getMaxTimeBetweenGc()){
            this.setMaxTimeBetweenGc(ags.getMaxTimeBetweenGc());
        }

        //Avg collected
        this.sumCollected = (sumCollected + ags.getAvgCollected()) / 2;
        //Min/Max collected
        if (ags.getMinCollected() < this.getMinCollected()){
            this.setMinCollected(ags.getMinCollected());
        }
        if (ags.getMaxCollected() > this.getMaxCollected()){
            this.setMaxCollected(ags.getMaxCollected());
        }

        //Avg memory usage
        this.sumMemoryUsage = (sumMemoryUsage + ags.getAvgMemoryUsage()) / 2;
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

        reportCount++;
        //Average minMemoryUsage
        this.sumMinMemoryUsage = (sumMinMemoryUsage + ags.sumMinMemoryUsage) / 2;
    }

    public void addGcStats(GcStats stats) {
        if (gcCount == 0) {
            this.setStartTime(stats.getTimeStamp());
            this.sumTimeBetweenGc = Long.MAX_VALUE;
        } else {
            //Memory used
            this.sumMemoryUsage += stats.getMemoryUsedAfter();
            if (stats.getMemoryUsedAfter() < this.getMinMemoryUsage()) {
                this.setMinMemoryUsage(stats.getMemoryUsedAfter());
            } else if (stats.getMemoryUsedAfter() > this.getMaxMemoryUsage()) {
                this.setMaxMemoryUsage(stats.getMemoryUsedAfter());
            }
            //Memory collected
            this.sumCollected += stats.getAmountCollected();
            if (stats.getAmountCollected() < this.getMinCollected()) {
                this.setMinCollected(stats.getAmountCollected());
            } else if (stats.getAmountCollected() > this.getMaxCollected()) {
                this.setMaxCollected(stats.getAmountCollected());
            }
            //Collection Time
            this.sumCollectionTime += stats.getCollectionTime();
            if (stats.getCollectionTime() < this.getMinCollectionTime()) {
                this.setMinCollectionTime(stats.getCollectionTime());
            } else if (stats.getCollectionTime() > this.getMaxCollectionTime()) {
                this.setMaxCollectionTime(stats.getCollectionTime());
            }
            //Time performed
            if (stats.getTimeStamp() < this.getStartTime()) {
                this.setStartTime(stats.getTimeStamp());
            } else if (stats.getTimeStamp() > this.getEndTime()) {
                this.setEndTime(stats.getTimeStamp());
            }
            //Time between GCs
            if (gcCount % 2 == 0) {
                long time = stats.getTimeStamp() - this.getEndTime();
                sumTimeBetweenGc = time;
                if (time < this.getMinTimeBetweenGc()) {
                    this.setMinTimeBetweenGc(time);
                } else if (time > this.getMaxTimeBetweenGc()) {
                    this.setMaxTimeBetweenGc(time);
                }
            }
        }
        this.sumMemoryUsage += stats.getMemoryUsedAfter();
        this.sumCollected += stats.getAmountCollected();
        this.sumCollectionTime += stats.getCollectionTime();
        this.setEndMemoryUsage(stats.getMemoryUsedAfter());
        this.gcCount++;
    }

    public AnomalyReport createAnomalyReport(){
        return null; //@TODO Implement
    }

}
