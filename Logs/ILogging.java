package Logs;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Oliver on 2014-09-18.
 */
public interface ILogging {
    /**
     * Sends statistics for a GarbageCollection
     * @param memoryUsedAfter Memory usage after GarbageCollection.
     * @param memoryUsedBefore Memory usage before GarbageCollection.
     * @param timestamp When the GarbageCollection occurred.
     * @param collectionTime The time it took to perform the GarbageCollection.
     * @param hostname Hostname of the monitored process.
     * @param port Port of the monitored process.
     */
    public void sendGarbageCollectionLog(long memoryUsedAfter, long memoryUsedBefore, long timestamp,
                                    long collectionTime, String hostname, int port);

    /**
     * Sends information to be stored in a file
     * @param memoryUsed Current memory usage.
     * @param timestamp Current time
     * @param hostname hostname for monitored process
     * @param port port for monitored process
     */
    public void sendMemoryLog(long memoryUsed, long timestamp, String hostname, int port);

    /**
     * Get statistics for all GarbageCollection which occurred between startTime and endTime.
     * @param startTime Statistics for GarbageCollections which were done before this time will not be included.
     * @param endTime Statistics for GarbageCollections which were done after this time will not be included.
     * @return A list of GcStats from all processes separated by key hostname:port as a String
     */
    public Map<String, ArrayList<GcStats>> getGarbageCollectionStats(long startTime, long endTime);

    /**
     * Get statistics for all GarbageCollection which occurred in the specified processes between startTime and endTime.
     * @param startTime Statistics for GarbageCollections which were done before this time will not be included.
     * @param endTime Statistics for GarbageCollections which were done after this time will not be included.
     * @param processes A list of processes to be included named as hostname:port as a String
     * @return A list of GcStats from specified processes separated by key hostname:port as a String
     */
    public Map<String, ArrayList<GcStats>> getGarbageCollectionStats(long startTime, long endTime,
                                                                     ArrayList<String> processes);

    /**
     * Get Memory-statistics from all processes which were gathered between a specified timeframe.
     * @param startTime Statistics that were gathered before this time will not be included.
     * @param endTime Statistics that were gathered after this time will not be included.
     * @return A list of MemoryStats from all processes separated by a key hostname:port as a String.
     */
    public Map<String, ArrayList<MemoryStats>> getMemoryStats(long startTime, long endTime);

    /**
     * Get Memory-statistics from specified processes which were gathered between a specified timeframe.
     * @param startTime Statistics that were gathered before this time will not be included.
     * @param endTime Statistics that were gathered after this time will not be included.
     * @param processes A list of processes to be included named as hostname:port as a String.
     * @return A list of MemoryStats from the specified processes separated by a key HOSTNAME:PORT as a String.
     */
    public Map<String, ArrayList<MemoryStats>> getMemoryStats(long startTime, long endTime,
                                                              ArrayList<String> processes);

    public void sendAnalyzedGCData(String hostName, int port, AnalyzedDailyGcStats analyzedDailyGcStats);

}
