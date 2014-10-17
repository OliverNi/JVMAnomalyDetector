package Logs;

/**
 * Created by Oliver on 2014-09-19.
 */
public class GcStats {

    private long memoryUsedAfter;
    private long memoryUsedBefore;
    private long timeStamp;
    private long collectionTime;


    public long getAmountCollected(){
        return memoryUsedBefore - memoryUsedAfter;
    }

    public long getMemoryUsedAfter() {
        return memoryUsedAfter;
    }

    public void setMemoryUsedAfter(long memoryUsedAfter) {
        this.memoryUsedAfter = memoryUsedAfter;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public long getTimeStamp() {

        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getMemoryUsedBefore() {

        return memoryUsedBefore;
    }

    public void setMemoryUsedBefore(long memoryUsedBefore) {
        this.memoryUsedBefore = memoryUsedBefore;
    }
}
