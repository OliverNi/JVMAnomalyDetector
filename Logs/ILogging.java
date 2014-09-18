package Logs;

/**
 * Created by Oliver on 2014-09-18.
 */
public interface ILogging {
    public void sendGarbageCollectionLog(long memoryUsedAfter, long memoryUsedBefore, long timestamp,
                                    long collectionTime, String hostname, int port);
    public void sendMemoryLog(long memoryUsed, long timestamp, String hostname, int port);

}
