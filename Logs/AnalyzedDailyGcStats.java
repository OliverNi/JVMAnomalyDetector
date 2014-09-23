package Logs;

/**
 * Created by Oliver on 2014-09-22.
 */
public class AnalyzedDailyGcStats {
    public enum Trend{
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
    }
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
    private Trend trend;
    private long startTime;
    private long endTime;
    private int gcCount;

    /**
     * Constructor
     * @param avgCollected average memory collected for each GC for this day.
     * @param minCollected minimum memory collected from one GC for this day.
     * @param maxCollected maximum memory collected from one GC for this day.
     * @param minMemoryUsage minimum memory used after a GC for this day.
     * @param maxMemoryUsage maximum memory used after a GC for this day.
     * @param avgMemoryUsage average memory used after a GC for this day.
     * @param startMemoryUsage memory usage after first GC for this day.
     * @param endMemoryUsage memory usage after last GC for this day.
     * @param startTime start time for this day.
     * @param endTime end time for this day.
     * @param trend Growth trend for this day.
     */
    public AnalyzedDailyGcStats(long avgTimeBetweenGc, long minTimeBetweenGc, long maxTimeBetweenGc,
                                long avgCollected, long minCollected, long maxCollected,
                                long minMemoryUsage, long maxMemoryUsage, long avgMemoryUsage,
                                long startMemoryUsage, long endMemoryUsage,
                                long startTime, long endTime, int gcCount, Trend trend){
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
        this.trend = trend;
    }

    public AnalyzedDailyGcStats(){
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Trend.STABLE);
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

    public Trend getTrend() {
        return trend;
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

    public void setTrend(Trend trend) {
        this.trend = trend;
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
}
