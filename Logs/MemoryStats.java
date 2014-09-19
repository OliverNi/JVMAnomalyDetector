package Logs;

/**
 * Created by Oliver on 2014-09-19.
 */
public class MemoryStats {
    private long memoryUsed;
    private long timeStamp;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getMemoryUsed() {

        return memoryUsed;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }
}
