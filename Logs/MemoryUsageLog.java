package Logs;

import AnomalyDetector.AnomalyReport;
import AnomalyDetector.ProcessConnection;
import AnomalyDetector.ProcessReport;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Oliver on 2014-09-18.
 */
public interface MemoryUsageLog {
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
     *
     * @param startTime Statistics for GarbageCollections which were done before this time will not be included.
     * @param endTime Statistics for GarbageCollections which were done after this time will not be included.
     * @param hostname the hostname/ip address for the process
     * @param port the current port number for the process
     * @return
     */
    public ArrayList<GcStats> getGarbageCollectionStats(long startTime, long endTime,
                                                                     String hostname, int port);

    public void sendGcReport(String hostName, int port, GcReport analyzedDailyGcStats);

    /**
     * Get all GcReports from database within the specified timeframe.
     * @param startTime start-time (lower limit)
     * @param endTime end-time (upper limit)
     * @return A list of GcReports separated by a key HOSTNAME:PORT as a String.
     */
    public Map<String, ArrayList<GcReport>> getGcReports(long startTime, long endTime);

    public Map<String, ArrayList<GcReport>> getGcReports(long startTime, long endTime, ArrayList<ProcessConnection> connections);

    public ArrayList<GcReport> getGcReports(long startTime, long endTime, ProcessConnection connection);

    public ArrayList<GcReport> getGcReports(long startTime, long endTime, String host, int port);

    public Map<String, ArrayList<GcReport>> getGcReports(long startTime, long endTime, GcReport.Period period);

    public Map<String, ArrayList<GcReport>> getGcReports(long startTime, long endTime, GcReport.Period period, ArrayList<ProcessConnection> connections);

    public ArrayList<GcReport> getGcReports(long startTime, long endTime, GcReport.Period period, ProcessConnection connection);

    public ArrayList<GcReport> getGcReports(long startTime, long endTime, GcReport.Period period, String host, int port);

    public Map<String, ProcessReport> getProcessReports();

    public Map<String, ProcessReport> getProcessReports(ArrayList<ProcessConnection> processes);

    /**
     * Retrieves one ProcessReport
     * @param hostName
     * @param port
     * @return
     */
    public ProcessReport getProcessReport(String hostName, int port);

    /**
     *
     * @param hostname input for specific ip address/hostname
     * @param port input for specific process
     * @return an Arraylist of type AnomalyReports based in hostname, port input
     */
    public ArrayList<AnomalyReport> getAnomalyReports(String hostname, int port);

    public ArrayList<AnomalyReport> getAnomalyReports(long startTime, long endTime, String hostName, int port);

    /**
     * Sends an AnomalyReport to be logged.
     * @param aReport the AnomalyReport
     */
    public void sendAnomalyReport(AnomalyReport aReport);

    /**
     *
     * @param port the current port of the process
     * @param hostname the hostname or ip address of the process
     * @param report the created processReport
     */
    public void sendProcessReport(int port, String hostname, ProcessReport report);

    /**
     * Retrieves usageAfterFirstGc from the specified process
     * @param process HOSTNAME:PORT as String
     * @return usageAfterFirstGc for the specified process
     */
    public long firstGcValue(String process);

    public long getTimeOfLastGc(ProcessConnection connection);

    public void sendTimeOfLastGc(ProcessConnection connection, long timeOfLastGc);

    /**
     * Clears data for all processes in the database
     */
    public void clearData();

    // anropa ProcessReport och uppdatera firstGC

    /**
     *  calls on ProcessReport to update it for the specific port and hostname with a new usageAfterLastGc
     * @param usageAfterLastGc
     * @param hostname
     * @param port
     */
    public void sendUsageAfterLastGc(long usageAfterLastGc, String hostname, int port);

    public void sendUsageAfterFirstGc(long usageAfterLastGc, String hostname, int port);
    /**
     * Clears data for specified processes in the database.
     * @param processes list of processes HOSTNAME:PORT
     */
    public void clearData(ArrayList<String> processes);

    public ArrayList<GcReport> getPossibleMemoryLeaks(String host, int port);

    /**
     * Clears all GcReports marked as POSSIBLE_MEMORY_LEAK for the specified process
     * @param hostname host
     * @param port port
     * @return Number of reports removed
     */
    public int clearPossibleMemoryLeaks(String hostname, int port);

    public void setProcessReportStatus(String host, int port, ProcessReport.Status status);

    /**
     * clears old data for GCStats (2months), GCReport(daily(2month),weekly(4months),monthly(1year)) and AnomalyReport(6months)
     */
    public void clearOldData();

}
