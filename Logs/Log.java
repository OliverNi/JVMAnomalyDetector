package Logs;

import AnomalyDetector.ProcessReport;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Martin on 2014-09-18.
 */
public class Log implements  ILogging
{
    private long GCTime;
    private long GCTimeStamp;
    private long GCmemoryUsageAfter;
    private long GCmemoryUsageBefore;
    private long memoryUsed;
    private long timeStamp;
    private String ip;
    private int port;
    private Statement DB;
    private Connection DBConnection;

    public Log()
    {
        DBConnection = null;
        DB = null;
        GCTime = 0;
        GCTimeStamp = 0;
        GCmemoryUsageAfter = 0;
        GCmemoryUsageBefore = 0;
        memoryUsed = 0;
        timeStamp = 0;
        ip = "";
        port = 0;

        try
        {
            initDatabaseConnection();

        }catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    public static final void main(String[] args) throws ClassNotFoundException
    {
        Log test = new Log();
        test.DBTableCreation();
    }

    public void DBTableCreation()
    {
        try
        {
            DB = DBConnection.createStatement();
            DB.executeUpdate("DROP TABLE IF EXISTS MemLog");
            DB.executeUpdate("DROP TABLE IF EXISTS GCLog");
            DB.executeUpdate("DROP TABLE IF EXISTS GCReport");
            DB.executeUpdate("DROP TABLE IF EXISTS ProcessReport");

            DB.executeUpdate("CREATE TABLE MemLog(MemId INTEGER PRIMARY KEY AUTOINCREMENT, timestamp BIGINT, usedMemory BIGINT, hostname VARCHAR(25), port INTEGER)");

            DB.executeUpdate("CREATE TABLE ProcessReport(prId INTEGER PRIMARY KEY AUTOINCREMENT, startTime BIGINT, endTime BIGINT, hostname VARCHAR(25), port INT, status VARCHAR(25)," +
                    "consec_mem_inc_count INTEGER, usageAfterFirstGc BIGINT, usageAfterLastGc BIGINT )");

            DB.executeUpdate("CREATE TABLE GCLog(gcId INTEGER PRIMARY KEY AUTOINCREMENT, timestamp BIGINT," +
                    " memUsageAfter BIGINT, memUsageBefore BIGINT, GCCollectionTime BIGINT, hostname VARCHAR(25), port INTEGER," +
                    " FOREIGN KEY(hostname) REFERENCES GCReport(hostname), FOREIGN KEY(port) REFERENCES GCReport(port) )");

            DB.executeUpdate("CREATE TABLE GCReport(GCReportId INTEGER PRIMARY KEY AUTOINCREMENT, sumCollected BIGINT, minCollected BIGINT, maxCollected BIGINT, minMemoryUsage BIGINT," +
                    "maxMemoryUsage BIGINT, sumMemoryUsage BIGINT, startMemoryUsage BIGINT, endMemoryUsage BIGINT,sumTimeBetweenGc BIGINT, " +
                    "minTimeBetweenGc BIGINT, maxTimeBetweenGc BIGINT, sumCollectionTime BIGINT, minCollectionTime BIGINT, maxCollectionTime BIGINT," +
                    "starttime BIGINT, endTime BIGINT, hostname VARCHAR(25), port INTEGER, gcCount INTEGER, sumMinMemoryUsage BIGINT, reportCount INTEGER, FOREIGN KEY(hostname) REFERENCES GCLog(hostname)," +
                    "FOREIGN KEY(port) REFERENCES GCLog(port) ) ");
            DB.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public  void printSpecifiedTable(String input) throws SQLException
    {
        try
        {
            ResultSet rs = DB.executeQuery("SELECT * FROM "+input);
            while(rs.next())
            {
                if (input.equals("GCReport"))
                {
                    System.out.println("GCReportId = " + rs.getString("GCReportId"));
                    System.out.println("sumCollected = " + rs.getString("sumCollected"));
                    System.out.println("minCollected = " + rs.getString("minCollected"));
                    System.out.println("maxCollected = " + rs.getString("maxCollected"));
                    System.out.println("minMemoryUsage = " + rs.getString("minMemoryUsage"));
                    System.out.println("maxMemoryUsage = " + rs.getString("maxMemoryUsage"));
                    System.out.println("sumMemoryUsage = " + rs.getString("sumMemoryUsage"));
                    System.out.println("startMemoryUsage = " + rs.getString("startMemoryUsage"));
                    System.out.println("endMemoryUsage = " + rs.getString("endMemoryUsage"));
                    System.out.println("sumTimeBetweenGc = " + rs.getString("sumTimeBetweenGc"));
                    System.out.println("minTimeBetweenGc = " + rs.getString("minTimeBetweenGc"));
                    System.out.println("maxTimeBetweenGc = " + rs.getString("maxTimeBetweenGc"));
                    System.out.println("sumCollectionTime = " + rs.getString("sumCollectionTime"));
                    System.out.println("minCollectionTime = " + rs.getString("minCollectionTime"));
                    System.out.println("maxCollectionTime = " + rs.getString("maxCollectionTime"));
                    System.out.println("startTime = " + rs.getString("startTime"));
                    System.out.println("endTime = " + rs.getString("endTime"));
                    System.out.println("hostname = " + rs.getString("hostname"));
                    System.out.println("port = " + rs.getString("port"));
                    System.out.println("sumMinMemoryUsage = " + rs.getString("sumMinMemoryUsage"));
                    System.out.println("reportCount = " + rs.getString("reportCount"));
                    System.out.println("gcCount = " + rs.getString("gcCount"));
                } else if (input.equals("MemLog"))
                {
                    System.out.println("MemId = " + rs.getString("MemId"));
                    System.out.println("timestamp = " + rs.getString("timestamp"));
                    System.out.println("usedMemory = " + rs.getString("usedMemory"));
                    System.out.println("hostname = " + rs.getString("hostname"));
                    System.out.println("port = " + rs.getString("port"));
                }
                else if(input.equals("GCLog"))
                {
                    System.out.println("gcId = " + rs.getString("gcId"));
                    System.out.println("timestamp = " + rs.getString("timestamp"));
                    System.out.println("memUsageAfter = " + rs.getString("memUsageBefore"));
                    System.out.println("memUsageBefore = " + rs.getString("memUsageBefore"));
                    System.out.println("GCCollectionTime = " + rs.getString("GCCollectionTime"));
                    System.out.println("hostname = " + rs.getString("hostname"));
                    System.out.println("port = " + rs.getString("port"));
                }
                else if(input.equals("ProcessReport"))
                {
                    System.out.println("startTime = " + rs.getString("startTime"));
                    System.out.println("endTime = " + rs.getString("endTime"));
                    System.out.println("hostname = " + rs.getString("hostname"));
                    System.out.println("port = " + rs.getString("port"));
                    System.out.println("status = " + rs.getString("status"));
                    System.out.println("consec_mem_inc_count = " + rs.getString("consec_mem_inc_count"));
                    System.out.println("usageAfterFirstGc = " + rs.getString("usageAfterFirstGc"));
                    System.out.println("usageAfterLastGc = " + rs.getString("usageAfterLastGc"));
                }

            }
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }

    }

    public long getGCTimeStamp()
    {
        return GCTimeStamp;
    }

    public long getGCmemoryUsageAfter()
    {
        return GCmemoryUsageAfter;
    }

    public long getGCmemoryUsageBefore()
    {
        return GCmemoryUsageBefore;
    }

    public long getMemoryUsed()
    {
        return memoryUsed;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public String getIp()
    {
        return ip;
    }

    public int getPort()
    {
        return port;
    }

    public long getGCTime()
    {
        return GCTime;
    }



    public void initDatabaseConnection() throws ClassNotFoundException
    {

        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        try
        {
            //possibly secure the tables with a UNIQUE constraint to prevent duplicate rows.
            // EXAMPLE:    CREATE TABLE a (i INT, j INT, UNIQUE(i, j) ON CONFLICT REPLACE);


//            // create a database connection
            DBConnection = DriverManager.getConnection("jdbc:sqlite:test.db");
            DB = DBConnection.createStatement();
            DB.setQueryTimeout(30);  // set timeout to 30 sec.




            // DB.executeUpdate("DROP TABLE IF EXISTS AnalyzedGCData");
            //   statement.executeUpdate("INSERT INTO MemLog(timestamp, usedMemory, hostname, port) VALUES(13371337, 10241024, '127.0.0.1', 3500)");

            //  System.out.println(test.createMemLogEntry(10001000,1337669,"localhost",3800));
            //statement.executeUpdate(test.createMemLogEntry(10001000,1337669,"localhost",3800));
            //statement.executeUpdate("DELETE  FROM MemLog WHERE MemID = 12342145");
            //statement.executeUpdate("DROP TABLE IF EXISTS MemLog");
//            statement.executeUpdate("create table person (id integer, name string)");
//            statement.executeUpdate("insert into person values(1, 'leo')");
//            statement.executeUpdate("insert into person values(2, 'yui')");

            // AnalyzedGcStats temp = new AnalyzedGcStats();
            //   test.sendAnalyzedGCData("127.0.0.1",3800,temp);

            //this one gives duplicate rows when performed
            //  test.sendMemoryLog(1337,2233222, "127.0.0.1",3800);
            //
            //test.sendGarbageCollectionLog(0,0,0,0, "127.0.0.1", 3800);
            // DB.executeUpdate("DELETE FROM MemLog WHERE MemId = 4" );
            // ResultSet rs = DB.executeQuery("select * from AnalyzedGCData");
            //DB.executeUpdate("DROP TABLE IF EXISTS MemLog");
             // DBTableCreation();
            //printSpecifiedTable("MemLog");
            DB.close();

        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
//        finally {
//            try {
//                if (DBConnection != null)
//                    DBConnection.close();
//            } catch (SQLException e) {
//                // connection close failed.
//                System.err.println(e);
//            }
//
//        }

    }

    public String getCurrentDate()
    {
        //Set date format
        DateFormat theFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        //fetch time in milliseconds
        Date today = Calendar.getInstance().getTime();

        //convert time into a string with the above format
        String fetchTime = theFormat.format(today);

        return fetchTime;
    }

    public void setGCTime(long GCTime)
    {
        this.GCTime = GCTime;
    }

    public void setGCTimeStamp(long GCTimeStamp)
    {
        this.GCTimeStamp = GCTimeStamp;
    }

    public void setGCmemoryUsageAfter(long GCmemoryUsageAfter)
    {
        this.GCmemoryUsageAfter = GCmemoryUsageAfter;
    }

    public void setGCmemoryUsageBefore(long GCmemoryUsageBefore)
    {
        this.GCmemoryUsageBefore = GCmemoryUsageBefore;
    }

    public void setMemoryUsed(long memoryUsed)
    {
        this.memoryUsed = memoryUsed;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    //basic input values works
    @Override
    public void sendGarbageCollectionLog(long memoryUsedAfter, long memoryUsedBefore, long timestamp, long collectionTime, String hostname, int port)
    {
        try
        {
            DB = DBConnection.createStatement();
            DB.executeUpdate("INSERT INTO  GCLog(timestamp, memUsageAfter, memUsageBefore, GCCollectionTime, hostname, port)" +
                    " VALUES("+timestamp +","+memoryUsedAfter+","+memoryUsedBefore+","+collectionTime+",'"+hostname+"',"+port+")");
            DB.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //basic input values  works,
    @Override
    public void sendMemoryLog(long memoryUsed, long timestamp, String hostname, int port)
    {
        try
        {
            DB = DBConnection.createStatement();
            DB.executeUpdate("INSERT INTO  MemLog(timestamp,usedMemory,hostname,port) VALUES("+timestamp +","+memoryUsed+","+"'"+hostname+"'"+","+port+")");
            DB.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    //fetch all GCLog rows between starttime and endtime
    @Override
    public Map<String, ArrayList<GcStats>> getGarbageCollectionStats(long startTime, long endTime)
    {
        ArrayList<GcStats> getGCStats = new ArrayList<GcStats>();
        GcStats GCstatistics = new GcStats();
        String fetchHostNamePort = "";
        HashMap<String, ArrayList<GcStats>> instanceOfGCStats = new HashMap<>();

        try
        {
            DB = DBConnection.createStatement();
            ResultSet rs = DB.executeQuery("SELECT timestamp,memUsageAfter,memUsageBefore,GCCollectionTime, hostname,port FROM GCLog WHERE timestamp >= "+startTime+" AND timestamp <= "+endTime+" ORDER BY timestamp");

            while(rs.next())
            {
                long timestamp = Long.parseLong(rs.getString("timestamp"));
                GCstatistics.setTimeStamp(timestamp);

                long  memUsageAfter = Long.parseLong(rs.getString("memUsageAfter"));
                GCstatistics.setMemoryUsedAfter(memUsageAfter);

                long memUsageBefore = Long.parseLong(rs.getString("memUsageBefore"));
                GCstatistics.setMemoryUsedBefore(memUsageBefore);

                long GCCollectionTime = Long.parseLong(rs.getString("GCCollectionTime"));
                GCstatistics.setCollectionTime(GCCollectionTime);

                fetchHostNamePort = rs.getString("hostname")+":"+rs.getString("port");

                getGCStats.add(GCstatistics);
                instanceOfGCStats.put(fetchHostNamePort, getGCStats);

            }
            DB.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }

        Map<String, ArrayList<GcStats>> fetch =  instanceOfGCStats;

        return fetch;
    }

    //fetch all rows in GCLog containing starttime, endtime and for the amount of specified processes
    @Override
    public Map<String, ArrayList<GcStats>> getGarbageCollectionStats(long startTime, long endTime, ArrayList<String> processes)
    {
        ArrayList<GcStats> getGCStats = new ArrayList<GcStats>();
        GcStats GCstatistics = new GcStats();
        int processesCounter = 0;
        String getPortHostname = "";
        HashMap<String, ArrayList<GcStats>> instanceOfGCStats = new HashMap<>();

        if (processes.toString().contains(":"))
        {
            try
            {
                DB = DBConnection.createStatement();
                while(processesCounter < processes.size())
                {
                    getPortHostname = processes.get(processesCounter);
                    processesCounter++;
                    String[] theSplit = getPortHostname.split("\\:");

                    ResultSet rs = DB.executeQuery("SELECT timestamp,memUsageAfter,memUsageBefore,hostname,port FROM GCLog " +
                            "WHERE timestamp >= "+startTime+" AND timestamp <= "+endTime+ " AND port = " + theSplit[1] + " AND hostname = " + theSplit[0]+" ORDER BY timestamp");
                    while(rs.next())
                    {
                        long timestamp = Long.parseLong(rs.getString("timestamp"));
                        GCstatistics.setTimeStamp(timestamp);

                        long memUsageAfter = Long.parseLong(rs.getString("memUsageAfter"));
                        GCstatistics.setMemoryUsedAfter(memUsageAfter);

                        long memUsageBefore = Long.parseLong(rs.getString("memUsageBefore"));
                        GCstatistics.setMemoryUsedBefore(memUsageBefore);

                        long GCCollectionTime = Long.parseLong(rs.getString("GCCollectionTime"));
                        GCstatistics.setCollectionTime(GCCollectionTime);

                        getGCStats.add(GCstatistics);
                        instanceOfGCStats.put(getPortHostname, getGCStats);
                    }
                }
                DB.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("NumberFormatException: " + nfe.getMessage());
            }
        }
        else
        {
            throw new IllegalArgumentException("String " + processes.toString() + " does not contain :");
        }
        Map<String, ArrayList<GcStats>> fetch = instanceOfGCStats;

        return fetch;
    }

    @Override
    public ArrayList<GcStats> getGarbageCollectionStats(long startTime, long endTime, String hostname, int port)
    {
        GcStats GCstatsFetch = new GcStats();
        ArrayList<GcStats> GCStatistics = new ArrayList<>();
        try
        {
            DB = DBConnection.createStatement();
            ResultSet rs = DB.executeQuery("SELECT timestamp,memUsageAfter,memUsageBefore,hostname,port FROM GCLog " +
                    "WHERE timestamp >= "+startTime+" AND timestamp <= "+endTime+ " AND port = " + port + " AND hostname = " + hostname+" ORDER BY timestamp");
            while(rs.next())
            {
                long timestamp = Long.parseLong(rs.getString("timestamp"));
                GCstatsFetch.setTimeStamp(timestamp);

                long memUsageAfter = Long.parseLong(rs.getString("memUsageAfter"));
                GCstatsFetch.setMemoryUsedAfter(memUsageAfter);

                long memUsageBefore = Long.parseLong(rs.getString("memUsageBefore"));
                GCstatsFetch.setMemoryUsedBefore(memUsageBefore);

                long GCCollectionTime = Long.parseLong(rs.getString("GCCollectionTime"));
                GCstatsFetch.setCollectionTime(GCCollectionTime);
                GCStatistics.add(GCstatsFetch);
            }
            DB.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return GCStatistics;
    }

    //@TODO is it really the right table it fetches data from? or is it supposed to be fetching data from a new table called MemReport ?
    @Override
    public Map<String, ArrayList<MemoryStats>> getMemoryStats(long startTime, long endTime)
    {
        ArrayList<MemoryStats> getMemStats = new ArrayList<MemoryStats>();
        MemoryStats memstats = new MemoryStats();
        String fetchHostNamePort = "";
        HashMap<String, ArrayList<MemoryStats>> instanceOfMemStats = new HashMap<>();

        try
        {
            DB = DBConnection.createStatement();
            ResultSet rs = DB.executeQuery("SELECT timestamp,usedMemory,hostname,port FROM MemLog " +
                    "WHERE timestamp >="+startTime+" AND timestamp <="+endTime+" ORDER BY timestamp");
            while(rs.next())
            {
                fetchHostNamePort = rs.getString("hostname")+":"+rs.getString("port");

                long timestamp = Long.parseLong(rs.getString("timestamp"));
                memstats.setTimeStamp(timestamp);

                long  usedMemory = Long.parseLong(rs.getString("usedMemory"));
                memstats.setMemoryUsed(usedMemory);

                getMemStats.add(memstats);
                instanceOfMemStats.put(fetchHostNamePort,getMemStats);
            }
            DB.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }


        Map<String, ArrayList<MemoryStats>> fetch = instanceOfMemStats;

        return fetch;
    }

    @Override
    public Map<String, ArrayList<MemoryStats>> getMemoryStats(long startTime, long endTime, ArrayList<String> processes)
    {
        ArrayList<MemoryStats> getMemStats = new ArrayList<MemoryStats>();
        MemoryStats memstats = new MemoryStats();
        String fetchHostNamePort = "";
        int processesCounter = 0;
        HashMap<String, ArrayList<MemoryStats>> instanceOfMemStats = new HashMap<>();

        if (processes.toString().contains(":"))
        {
            try
            {
                DB = DBConnection.createStatement();
                while(processesCounter < processes.size())
                {
                    String getPortHostname = processes.get(processesCounter);
                    processesCounter++;
                    String theSplit[] = getPortHostname.split("\\:");

                    ResultSet rs = DB.executeQuery("SELECT timestamp,usedMemory,hostname,port FROM MemLog " +
                            "WHERE timestamp >= "+startTime+" AND timestamp <= "+endTime+" AND hostname = "+theSplit[0]+" AND port = "+theSplit[1]+" ORDER BY timestamp");
                    while(rs.next())
                    {
                        fetchHostNamePort = rs.getString("hostname")+":"+rs.getString("port");

                        long timestamp = Long.parseLong(rs.getString("timestamp"));
                        memstats.setTimeStamp(timestamp);

                        long  usedMemory = Long.parseLong(rs.getString("usedMemory"));
                        memstats.setMemoryUsed(usedMemory);

                        getMemStats.add(memstats);

                        instanceOfMemStats.put(fetchHostNamePort,getMemStats);
                    }
                }
                DB.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("NumberFormatException: " + nfe.getMessage());
            }
        } else
        {
            throw new IllegalArgumentException("String " + processes.toString() + " does not contain :");
        }
        Map<String, ArrayList<MemoryStats>> fetch = instanceOfMemStats;

        return fetch;
    }

    //input values works, however an update is needed for the trend column, what value to implement when project member is finished with his new concept
    @Override
    public void sendGcReport(String hostName, int port, GcReport analyzedDailyGcStats)
    {
        try
        {
            DB = DBConnection.createStatement();
            String input = "INSERT INTO  GCReport(sumCollected, minCollected, maxCollected, minMemoryUsage,"+
                            "maxMemoryUsage, sumMemoryUsage, startMemoryUsage, endMemoryUsage,sumTimeBetweenGc,"+
                            "minTimeBetweenGc, maxTimeBetweenGc, avgCollectionTime, minCollectionTime, maxCollectionTime,"+
                            "starttime, endTime,hostname, port, gcCount, sumMinMemoryUsage, reportCount) "+
                    "VALUES("+ analyzedDailyGcStats.getSumCollected()+","+analyzedDailyGcStats.getMinCollected()+","+analyzedDailyGcStats.getMaxCollected()+","+analyzedDailyGcStats.getMinMemoryUsage()+","
                    +analyzedDailyGcStats.getMaxMemoryUsage()+","+analyzedDailyGcStats.getSumMemoryUsage()+","+
                    analyzedDailyGcStats.getStartMemoryUsage()+","+analyzedDailyGcStats.getEndMemoryUsage()+","+analyzedDailyGcStats.getSumTimeBetweenGc()+","+
                    analyzedDailyGcStats.getMinTimeBetweenGc()+","+analyzedDailyGcStats.getMaxTimeBetweenGc()+","+analyzedDailyGcStats.getAvgCollectionTime()+","+
                    analyzedDailyGcStats.getMinCollectionTime()+","+analyzedDailyGcStats.getMaxCollectionTime()+","+analyzedDailyGcStats.getStartTime()+","+analyzedDailyGcStats.getEndTime()+",'"+hostName+"',"+port+","
                    +analyzedDailyGcStats.getGcCount()+","+analyzedDailyGcStats.getSumMinMemoryUsage()+","+analyzedDailyGcStats.getReportCount()+")";
            DB.executeUpdate(input);
            DB.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, ArrayList<GcReport>> getGcReports(long startTime, long endTime)
    {
        ArrayList<GcReport> GCReports = new ArrayList<GcReport>();
        GcReport theGcReport = new GcReport();
        HashMap<String, ArrayList<GcReport>> instanceOfGCLog = new HashMap<>();
        try
        {
            DB = DBConnection.createStatement();
            String input = "SELECT sumCollected, minCollected, maxCollected, minMemoryUsage," +
                    "maxMemoryUsage, sumMemoryUsage, startMemoryUsage, endMemoryUsage,sumTimeBetweenGc,"+
                    "minTimeBetweenGc, maxTimeBetweenGc, sumCollectionTime, minCollectionTime, maxCollectionTime,"+
                   "starttime, endTime,gcCount, sumMinMemoryUsage, reportCount, hostname, port FROM GCReport WHERE startTime >= "+startTime+" AND endTime <= "+endTime+ " ORDER BY startTime";
            ResultSet rs = DB.executeQuery(input);
            while(rs.next())
            {
                long sumCollected = Long.parseLong(rs.getString("sumCollected"));
                theGcReport.setSumCollected(sumCollected);

                long maxCollected = Long.parseLong(rs.getString("maxCollected"));
                theGcReport.setMaxCollected(maxCollected);

                long minCollected = Long.parseLong(rs.getString("minCollected"));
                theGcReport.setMinCollected(minCollected);

                long minMemoryUsage = Long.parseLong(rs.getString("minMemoryUsage"));
                theGcReport.setMinMemoryUsage(minMemoryUsage);

                long maxMemoryUsage = Long.parseLong(rs.getString("maxMemoryUsage"));
                theGcReport.setMaxMemoryUsage(maxMemoryUsage);

                long sumMemoryUsage = Long.parseLong(rs.getString("sumMemoryUsage"));
                theGcReport.setSumMemoryUsage(sumMemoryUsage);

                long startMemoryUsage = Long.parseLong(rs.getString("startMemoryUsage"));
                theGcReport.setStartMemoryUsage(startMemoryUsage);

                long endMemoryUsage = Long.parseLong(rs.getString("endMemoryUsage"));
                theGcReport.setEndMemoryUsage(endMemoryUsage);

                long sumTimeBetweenGC = Long.parseLong(rs.getString("sumTimeBetweenGc"));
                theGcReport.setSumTimeBetweenGc(sumTimeBetweenGC);

                long minTimeBetweenGc = Long.parseLong(rs.getString("minTimeBetweenGc"));
                theGcReport.setMinTimeBetweenGc(minTimeBetweenGc);

                long maxTimeBetweenGc = Long.parseLong(rs.getString("maxTimeBetweenGc"));
                theGcReport.setMaxTimeBetweenGc(maxTimeBetweenGc);

                long sumCollectionTime = Long.parseLong(rs.getString("sumCollectionTime"));
                theGcReport.setSumCollectionTime(sumCollectionTime);

                long minCollectionTime = Long.parseLong(rs.getString("minCollectionTime"));
                theGcReport.setMinCollectionTime(minCollectionTime);

                long maxCollectionTime = Long.parseLong(rs.getString("maxCollectionTime"));
                theGcReport.setMaxCollectionTime(maxCollectionTime);

                long fetchedstartTime = Long.parseLong(rs.getString("startTime"));
                theGcReport.setStartTime(fetchedstartTime);

                long fetchedendTime = Long.parseLong(rs.getString("endTime"));
                theGcReport.setEndTime(fetchedendTime);

                long sumMinMemoryUsage = Long.parseLong(rs.getString("sumMinMemoryUsage"));
                theGcReport.setSumMinMemoryUsage(sumMinMemoryUsage);

                int reportCount = Integer.parseInt(rs.getString("reportCount"));
                theGcReport.setReportCount(reportCount);

                int gcCount = Integer.parseInt(rs.getString("gcCount"));
                theGcReport.setGcCount(gcCount);

                String fetchHostnamePort = rs.getString("hostname")+":"+rs.getString("port");

                GCReports.add(theGcReport);
                instanceOfGCLog.put(fetchHostnamePort, GCReports);
            }
            DB.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }
        Map<String, ArrayList<GcReport>> fetch = instanceOfGCLog;

        return null;
    }

    @Override
    public Map<String, ArrayList<ProcessReport>> getAllProcessReports()
    {
        HashMap<String, ArrayList<ProcessReport>> AddProcessReports  = new HashMap<>();
        ProcessReport oneReport = new ProcessReport();
        ArrayList<ProcessReport> allProccessReports = new ArrayList<>();
        try
        {
            DB = DBConnection.createStatement();
            ResultSet rs = DB.executeQuery("SELECT startTime, endTime, port, hostname, status FROM ProcessReport ORDER BY startTime");

            while(rs.next())
            {
                Long startTime = Long.parseLong(rs.getString("startTime"));
                oneReport.setStartTime(startTime);
                Long endTime =  Long.parseLong(rs.getString("endTime"));
                oneReport.setEndTime(endTime);
                int port = Integer.parseInt(rs.getString("port"));
                oneReport.setPort(port);
                String hostname = rs.getString("hostname");
                String hostnamePort = hostname+":"+port;
                oneReport.setHostName(hostname);
                String status = rs.getString("status");
                oneReport.setStatus(status);
                allProccessReports.add(oneReport);

                AddProcessReports.put(hostnamePort, allProccessReports);
            }
            DB.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }
        Map<String,ArrayList<ProcessReport>> AllProcessReports = AddProcessReports;
        return AllProcessReports;
    }

    @Override
    public Map<String, ArrayList<ProcessReport>> getProcessReports(ArrayList<String> processes)
    {
        HashMap<String, ArrayList<ProcessReport>> AddProcessReports  = new HashMap<>();
        ProcessReport oneReport = new ProcessReport();
        ArrayList<ProcessReport> allProccessReports = new ArrayList<>();
        int processesCounter = 0;
        try
        {
            DB = DBConnection.createStatement();
            while(processesCounter < processes.size())
            {
                String[] hostnamePort = processes.get(processesCounter).split("\\:");
                ResultSet rs = DB.executeQuery("SELECT startTime, endTime, port, hostname, status FROM ProcessReport WHERE "+hostnamePort[0] +" = hostname AND"+hostnamePort[1]+" = port" +"ORDER BY startTime");

                while(rs.next())
                {
                    Long startTime = Long.parseLong(rs.getString("startTime"));
                    oneReport.setStartTime(startTime);
                    Long endTime =  Long.parseLong(rs.getString("endTime"));
                    oneReport.setEndTime(endTime);
                    int port = Integer.parseInt(rs.getString("port"));
                    oneReport.setPort(port);
                    String hostname = rs.getString("hostname");
                    String theHostnamePort = hostname+":"+port;
                    oneReport.setHostName(hostname);
                    String status = rs.getString("status");
                    oneReport.setStatus(status);
                    allProccessReports.add(oneReport);

                    AddProcessReports.put(theHostnamePort, allProccessReports);
                }
                processesCounter++;
            }
            DB.close();

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }
        Map<String,ArrayList<ProcessReport>> AllProcessReports = AddProcessReports;
        return AllProcessReports;
    }

    @Override
    public ProcessReport getProcessReport(String hostName, int port)
    {
        ProcessReport oneReport = new ProcessReport();
        try
        {
            DB = DBConnection.createStatement();

            ResultSet rs = DB.executeQuery("SELECT startTime, endTime, port, hostname, status FROM ProcessReport WHERE "+hostName+" = hostname AND"+port+" = port" +"ORDER BY startTime");

            while(rs.next())
            {
                Long startTime = Long.parseLong(rs.getString("startTime"));
                oneReport.setStartTime(startTime);
                Long endTime =  Long.parseLong(rs.getString("endTime"));
                oneReport.setEndTime(endTime);
                int getport = Integer.parseInt(rs.getString("port"));
                oneReport.setPort(port);
                String hostname = rs.getString("hostname");
                String theHostnamePort = hostname+":"+getport;
                oneReport.setHostName(hostname);
                String status = rs.getString("status");

                oneReport.setStatus(status);
            }
            DB.close();

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }

        return oneReport;
    }

    @Override
    public void sendProcessReport(long startTime, long endTime, int port, String hostname, ProcessReport createdProcessReport)
    {
        try
        {
            DB = DBConnection.createStatement();
            DB.executeUpdate("INSERT INTO ProcessReport(startTime, endTime, hostname, port, status, consec_mem_inc_count, usageAfterFirstGc, usageAfterLastGc)"+
                    " VALUES("+startTime+","+endTime+","+hostname+","+port+","+createdProcessReport.getStatus()+","
                            +createdProcessReport.getConsec_mem_inc_count()+","+createdProcessReport.getUsageAfterFirstGc()+","
                            +createdProcessReport.getUsageAfterLastGc()+")");
            DB.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

//    @Override
//    public void sendProcessReport(long startTime, long endTime, int port, String hostname, String status)
//    {
//        try
//        {
//            DB.executeUpdate("INSERT INTO ProcessReport(startTime, endTime, port, hostname, status" +
//                    "VALUES("+startTime+","+endTime+","+port+","+hostname+","+status+")");
//        }catch (SQLException e)
//        {
//            e.printStackTrace();
//        }
//    }

    @Override
    public long firstGcValue(String process)
    {
        long GcMinMemValue = 0L;
        try
        {
            DB = DBConnection.createStatement();
            String input = "SELECT minMemoryUsage FROM GCReport WHERE GCReportId = 1";
            ResultSet rs = DB.executeQuery(input);

            GcMinMemValue = Long.parseLong(rs.getString("sumMinMemoryUsage"));
            DB.close();
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }
        return GcMinMemValue;
    }

    @Override
    public void clearData()
    {
        try
        {
            DB = DBConnection.createStatement();
            String input = "";
            input = "DELETE FROM GCReport";
            DB.executeUpdate(input);
            input = "DELETE FROM GCLog";
            DB.executeUpdate(input);
            input = "DELETE FROM MemLog";
            DB.executeUpdate(input);
            DB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearData(ArrayList<String> processes)
    {
        String getPortHostname = "";
        int processesCounter = 0;
        String input = "";
        try
        {
            DB = DBConnection.createStatement();
            while(processesCounter < processes.size())
            {
                getPortHostname = processes.get(processesCounter);
                processesCounter++;
                String[] theSplit = getPortHostname.split("\\:");
                input = "DELETE FROM GCLog WHERE "+theSplit[0]+ " = hostname AND port = "+theSplit[1];
                DB.executeUpdate(input);
                input = "DELETE FROM MemLog WHERE "+theSplit[0]+ " = hostname AND port = "+theSplit[1];
                DB.executeUpdate(input);
                input = "DELETE FROM GCReport WHERE "+theSplit[0]+ " = hostname AND port = "+theSplit[1];
                input = "DELETE FROM ProcessReport WHERE "+theSplit[0]+ " = hostname AND port = "+theSplit[1];
                DB.executeUpdate(input);
                processesCounter++;
            }
            DB.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

}
