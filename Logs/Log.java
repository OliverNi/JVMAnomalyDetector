package Logs;

/**
 * Created by Martin on 2014-09-18.
 */
public class Log implements  ILogging
{
    private int GCTime;
    private int GCCount;
    private Long minHeapSize;
    private Long maxHeapSize;
    private Long unixTimeFrame;
    private Long latestGCPerformedInUnixTime;
    private String date;

    public int getGCTime() {
        return GCTime;
    }

    public int getGCCount() {
        return GCCount;
    }

    public Long getMinHeapSize() {
        return minHeapSize;
    }

    public Long getMaxHeapSize() {
        return maxHeapSize;
    }

    public Long getUnixTimeFrame() {
        return unixTimeFrame;
    }

    public Long getLatestGCPerformedInUnixTime() {
        return latestGCPerformedInUnixTime;
    }

    public String getDate() {
        return date;
    }

    static void main()
    {

    }

    Log()
    {
        GCTime = 0;
        GCCount = 0;
        minHeapSize = 0L;
        maxHeapSize = 0L;
        unixTimeFrame = 0L;
        latestGCPerformedInUnixTime = 0L;
        date = "";
    }

    public void setGCTime(int GCTime) {
        this.GCTime = GCTime;
    }

    Log(int GCTime, int GCCount, Long minHeapSize, Long maxHeapSize, Long unixTimeFrame, String date, Long latestGCPerformedInUnixTime)
    {
        this.GCTime = GCTime;
        this.GCCount = GCCount;
        this.minHeapSize = minHeapSize;
        this.maxHeapSize = maxHeapSize;
        this.unixTimeFrame = unixTimeFrame;
    }

    Boolean appendToFile(String fileName)
    {
       return null;
    }

    Boolean checkFileSize(String filename)
    {
        return null;
    }


    @Override
    public void sendGarbageCollectionLog(long memoryUsedAfter, long memoryUsedBefore, long timestamp, long collectionTime, String hostname, int port) {

    }

    @Override
    public void sendMemoryLog(long memoryUsed, long timestamp, String hostname, int port) {

    }
}
