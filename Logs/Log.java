package Logs;

import AnomalyDetector.AnomalyReport;
import AnomalyDetector.ProcessConnection;
import AnomalyDetector.ProcessReport;

import java.lang.reflect.Array;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

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
    private Connection DBConnection;

    public Log()
    {
        DBConnection = null;
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
        DBTableCreation();

    }

    public static final void main(String[] args) throws ClassNotFoundException
    {
        Log test = new Log();

    }

    public void DBTableCreation()
    {
        try
        {
            Statement DB = null;
            DB = DBConnection.createStatement();

//            DB.executeUpdate("DROP TABLE IF EXISTS MemLog");
//            DB.executeUpdate("DROP TABLE IF EXISTS GCLog");
//            DB.executeUpdate("DROP TABLE IF EXISTS GCReport");
//            DB.executeUpdate("DROP TABLE IF EXISTS ProcessReport");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS MemLog(MemId INTEGER PRIMARY KEY AUTOINCREMENT, timestamp BIGINT, usedMemory BIGINT, hostname VARCHAR(25), port INTEGER)");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS ProcessReport(prId INTEGER PRIMARY KEY AUTOINCREMENT, startTime BIGINT, endTime BIGINT, hostname VARCHAR(25), port INT, status VARCHAR(25)," +
                    "consec_mem_inc_count INT, usageAfterFirstGc BIGINT, usageAfterLastGc BIGINT, dailySumMemUsageDif FLOAT, weeklySumMemUsageDif FLOAT, monthlySumMemUsageDif FLOAT, " +
                    "dailyMinMemUsageDif FLOAT, weeklyMinMemUsageDif FLOAT, monthlyMinMemUsageDif FLOAT, dailyIncreaseCount INT, weeklyIncreaseCount INT, monthlyIncreaseCount INT, " +
                    "dailyDecreaseCount INT, weeklyDecreaseCount INT, monthlyDecreaseCount INT, dailyReportCount INT, weeklyReportCount INT, monthlyReportCount INT)");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS GCLog(gcId INTEGER PRIMARY KEY AUTOINCREMENT, timestamp BIGINT," +
                    " memUsageAfter BIGINT, memUsageBefore BIGINT, GCCollectionTime BIGINT, hostname VARCHAR(25), port INTEGER," +
                    " FOREIGN KEY(hostname) REFERENCES GCReport(hostname), FOREIGN KEY(port) REFERENCES GCReport(port) )");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS GCReport(GCReportId INTEGER PRIMARY KEY AUTOINCREMENT, sumCollected BIGINT, minCollected BIGINT, maxCollected BIGINT, minMemoryUsage BIGINT," +
                    "maxMemoryUsage BIGINT, sumMemoryUsage BIGINT, startMemoryUsage BIGINT, endMemoryUsage BIGINT,sumTimeBetweenGc BIGINT, " +
                    "minTimeBetweenGc BIGINT, maxTimeBetweenGc BIGINT, sumCollectionTime BIGINT, minCollectionTime BIGINT, maxCollectionTime BIGINT," +
                    "starttime BIGINT, endTime BIGINT, hostname VARCHAR(25), port INTEGER, gcCount INTEGER, sumMinMemoryUsage BIGINT, reportCount INTEGER, status VARCHAR(50), FOREIGN KEY(hostname) REFERENCES GCLog(hostname)," +
                    "FOREIGN KEY(port) REFERENCES GCLog(port) ) ");
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS AnomalyReport(aId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "hostname VARCHAR(25), port INTEGER, timestamp BIGINT, errorMsg VARCHAR(25), startTimeIncrease BIGINT," +
                    "anomalyStatus VARCHAR(25), memIncreasePercentage INT, memIncreaseBytes BIGINT )");
            DB.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public  void printSpecifiedTable(String input)
    {
        try
        {
            Statement DB = null;
            DB = DBConnection.createStatement();

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
                    System.out.println("status = " + rs.getString("status"));

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
            Statement DB = null;
            DBConnection = DriverManager.getConnection("jdbc:sqlite:test11.db");
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
        //if processreport for this host port doesnt exist, create one here. setusageafterfirstGC = memoryUsedAfter
        if (getProcessReport(hostname, port) == null){
            sendUsageAfterFirstGc(memoryUsedAfter, hostname, port);
        }
        try
        {
            Statement DB = null;
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
    //@TODO something fishy  here, SQLException disk I/O error pointed to this method.
    @Override
    public void sendMemoryLog(long memoryUsed, long timestamp, String hostname, int port)
    {
        try
        {
            Statement DB = null;
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
        String fetchHostNamePort = "";
        HashMap<String, ArrayList<GcStats>> instanceOfGCStats = new HashMap<>();
        String query = "SELECT timestamp,memUsageAfter,memUsageBefore,GCCollectionTime, hostname,port FROM GCLog WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp;";
        try
        {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setLong(1, startTime);
            stmt.setLong(2, endTime);
            ResultSet rs = stmt.executeQuery();
            while(rs.next())
            {
                GcStats GCstatistics = new GcStats();
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
            stmt.close();
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
        int processesCounter = 0;
        String getPortHostname = "";
        HashMap<String, ArrayList<GcStats>> instanceOfGCStats = new HashMap<>();
        String query = "SELECT timestamp,memUsageAfter,memUsageBefore,hostname,port FROM GCLog " +
                "WHERE timestamp >= ? AND timestamp <= ? AND port = ? AND hostname = ? ORDER BY timestamp;";

        if (processes.toString().contains(":"))
        {
            try
            {
                PreparedStatement stmt = DBConnection.prepareStatement(query);
                while(processesCounter < processes.size())
                {
                    getPortHostname = processes.get(processesCounter);
                    processesCounter++;
                    String[] theSplit = getPortHostname.split("\\:");
                    int port = Integer.parseInt(theSplit[1]);

                    stmt.setLong(1, startTime);
                    stmt.setLong(2, endTime);
                    stmt.setInt(3, port);
                    stmt.setString(4, theSplit[0]);
                    ResultSet rs = stmt.executeQuery();
                    while(rs.next())
                    {
                        GcStats GCstatistics = new GcStats();
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
                stmt.close();
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
        if (countRows("GCLog", hostname, port) < 1)
            return null;

        ArrayList<GcStats> GCStatistics = new ArrayList<>();
        String query = "SELECT timestamp,memUsageAfter,memUsageBefore,hostname,port, GCCollectionTime FROM GCLog " +
                "WHERE timestamp >= ? AND timestamp <= ? AND port = ? AND hostname = ? ORDER BY timestamp;";
        try
        {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setLong(1, startTime);
            stmt.setLong(2, endTime);
            stmt.setInt(3, port);
            stmt.setString(4, hostname);
            ResultSet rs = stmt.executeQuery();
            while(rs.next())
            {
                GcStats GCstatsFetch = new GcStats();
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
            stmt.close();
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
        String fetchHostNamePort = "";
        HashMap<String, ArrayList<MemoryStats>> instanceOfMemStats = new HashMap<>();

        try
        {
            Statement DB = null;
            DB = DBConnection.createStatement();
            ResultSet rs = DB.executeQuery("SELECT timestamp,usedMemory,hostname,port FROM MemLog " +
                    "WHERE timestamp >="+startTime+" AND timestamp <="+endTime+" ORDER BY timestamp");
            while(rs.next())
            {
                MemoryStats memstats = new MemoryStats();
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
        String fetchHostNamePort = "";
        int processesCounter = 0;
        HashMap<String, ArrayList<MemoryStats>> instanceOfMemStats = new HashMap<>();

        if (processes.toString().contains(":"))
        {
            try
            {
                Statement DB = null;
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
                        MemoryStats memstats = new MemoryStats();
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
            Statement DB = null;
            DB = DBConnection.createStatement();
            String input = "INSERT INTO  GCReport(sumCollected, minCollected, maxCollected, minMemoryUsage,"+
                            "maxMemoryUsage, sumMemoryUsage, startMemoryUsage, endMemoryUsage,sumTimeBetweenGc,"+
                            "minTimeBetweenGc, maxTimeBetweenGc, sumCollectionTime, minCollectionTime, maxCollectionTime,"+
                            "starttime, endTime,hostname, port, gcCount, sumMinMemoryUsage, reportCount, status) "+
                    "VALUES("+ analyzedDailyGcStats.getSumCollected()+","+analyzedDailyGcStats.getMinCollected()+","+analyzedDailyGcStats.getMaxCollected()+","+analyzedDailyGcStats.getMinMemoryUsage()+","
                    +analyzedDailyGcStats.getMaxMemoryUsage()+","+analyzedDailyGcStats.getSumMemoryUsage()+","+
                    analyzedDailyGcStats.getStartMemoryUsage()+","+analyzedDailyGcStats.getEndMemoryUsage()+","+analyzedDailyGcStats.getSumTimeBetweenGc()+","+
                    analyzedDailyGcStats.getMinTimeBetweenGc()+","+analyzedDailyGcStats.getMaxTimeBetweenGc()+","+analyzedDailyGcStats.getSumCollectionTime()+","+
                    analyzedDailyGcStats.getMinCollectionTime()+","+analyzedDailyGcStats.getMaxCollectionTime()+","+analyzedDailyGcStats.getStartTime()+","+analyzedDailyGcStats.getEndTime()+",'"+hostName+"',"+port+","
                    +analyzedDailyGcStats.getGcCount()+","+analyzedDailyGcStats.getSumMinMemoryUsage()+","+analyzedDailyGcStats.getReportCount()+", "+"'"+analyzedDailyGcStats.getStatus().toString()+"'"+" )";
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
        HashMap<String, ArrayList<GcReport>> instanceOfGCLog = new HashMap<>();
        String query = "SELECT sumCollected, minCollected, maxCollected, minMemoryUsage," +
                "maxMemoryUsage, sumMemoryUsage, startMemoryUsage, endMemoryUsage,sumTimeBetweenGc,"+
                "minTimeBetweenGc, maxTimeBetweenGc, sumCollectionTime, minCollectionTime, maxCollectionTime,"+
                "starttime, endTime,gcCount, sumMinMemoryUsage, reportCount, hostname, status, port FROM GCReport WHERE startTime >= ? AND endTime <= ? ORDER BY startTime;";
        try
        {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setLong(1, startTime);
            stmt.setLong(2, endTime);

            ResultSet rs = stmt.executeQuery();
            while(rs.next())
            {
                GcReport theGcReport = new GcReport();
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

                String status = rs.getString("status");
                theGcReport.setStatus(status);

                GCReports.add(theGcReport);
                instanceOfGCLog.put(fetchHostnamePort, GCReports);
            }
            stmt.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }

        return instanceOfGCLog;
    }

    @Override
    public Map<String, ArrayList<GcReport>> getGcReports(long startTime, long endTime, ArrayList<ProcessConnection> connections) {
        HashMap<String, ArrayList<GcReport>> reportsMap = new HashMap<>();

        for (ProcessConnection c : connections) {
            String key = c.getHostName() + ":" + c.getPort();
            reportsMap.put(key, getGcReports(startTime, endTime, c));
        }

        return reportsMap;
    }

    public ArrayList<GcReport> getGcReports(long startTime, long endTime, ProcessConnection connection){
        ArrayList<GcReport> gcReports = new ArrayList<GcReport>();
        String query = "SELECT sumCollected, minCollected, maxCollected, minMemoryUsage," +
                "maxMemoryUsage, sumMemoryUsage, startMemoryUsage, endMemoryUsage,sumTimeBetweenGc,"+
                "minTimeBetweenGc, maxTimeBetweenGc, sumCollectionTime, minCollectionTime, maxCollectionTime,"+
                "starttime, endTime,gcCount, sumMinMemoryUsage, reportCount, hostname, status, port FROM GCReport WHERE startTime >= ? AND" +
                " endTime <= ? AND hostname = ? AND port = ? ORDER BY startTime;";
        try
        {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setLong(1, startTime);
            stmt.setLong(2, endTime);
            stmt.setString(3, connection.getHostName());
            stmt.setInt(4, connection.getPort());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GcReport theGcReport = new GcReport();
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

                String status = rs.getString("status");
                theGcReport.setStatus(status);

                String fetchHostnamePort = rs.getString("hostname") + ":" + rs.getString("port");

                gcReports.add(theGcReport);
            }
            stmt.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NumberFormatException nfe)
        {
            System.out.println("NumberFormatException: " + nfe.getMessage());
        }

        return gcReports;
    }

    public ArrayList<GcReport> getGcReports(long startTime, long endTime, String host, int port){
        return getGcReports(startTime, endTime, new ProcessConnection(host, port));
    }

    @Override
    public Map<String, ProcessReport> getProcessReports()
    {
        HashMap<String, ProcessReport> reportsMap = new HashMap<>();
        String query = "SELECT startTime, endTime, port, hostname, status FROM ProcessReport ORDER BY startTime";
        try
        {
            Statement DB = DBConnection.createStatement();
            ResultSet rs = DB.executeQuery(query);

            while(rs.next())
            {
                ProcessReport oneReport = new ProcessReport();
                Long startTime = Long.parseLong(rs.getString("startTime"));
                oneReport.setStartTime(startTime);
                Long endTime =  Long.parseLong(rs.getString("endTime"));
                oneReport.setEndTime(endTime);
                int port = Integer.parseInt(rs.getString("port"));
                oneReport.setPort(port);
                String hostname = rs.getString("hostname");
                oneReport.setHostName(hostname);
                String status = rs.getString("status");
                oneReport.setStatus(status);
                int consecMemInc = rs.getInt("consec_mem_inc_count");
                oneReport.setConsec_mem_inc_count(consecMemInc);
                long usageAfterFirstGc = rs.getLong("usageAfterFirstGc");
                oneReport.setUsageAfterFirstGc(usageAfterFirstGc);
                long usageAfterLastGc = rs.getLong("usageAfterLastGc");
                oneReport.setUsageAfterLastGc(usageAfterLastGc);
                double dailySumMemUsageDif = rs.getDouble("dailySumMemUsageDif");
                oneReport.setDailySumMemUsageDif(dailySumMemUsageDif);
                double weeklySumMemUsageDif = rs.getDouble("weeklySumMemUsageDif");
                oneReport.setWeeklySumMemUsageDif(weeklySumMemUsageDif);
                double monthlySumMemUsageDif = rs.getDouble("monthlySumMemUsageDif");
                oneReport.setMonthlySumMemUsageDif(monthlySumMemUsageDif);
                double dailyMinMemUsageDif = rs.getDouble("dailyMinMemUsageDif");
                oneReport.setDailyMinMemUsageDif(dailyMinMemUsageDif);
                double weeklyMinMemUsageDif = rs.getDouble("weeklyMinMemUsageDif");
                oneReport.setWeeklyMinMemUsageDif(weeklyMinMemUsageDif);
                double monthlyMinMemUsageDif = rs.getDouble("monthlyMinMemUsageDif");
                oneReport.setMonthlyMinMemUsageDif(monthlyMinMemUsageDif);
                int dailyIncreaseCount = rs.getInt("dailyIncreaseCount");
                oneReport.setDailyIncreaseCount(dailyIncreaseCount);
                int weeklyIncreaseCount = rs.getInt("weeklyIncreaseCount");
                oneReport.setWeeklyIncreaseCount(weeklyIncreaseCount);
                int monthlyIncreaseCount = rs.getInt("monthlyIncreaseCount");
                oneReport.setMonthlyIncreaseCount(monthlyIncreaseCount);
                int dailyDecreaseCount = rs.getInt("dailyDecreaseCount");
                oneReport.setDailyDecreaseCount(dailyDecreaseCount);
                int weeklyDecreaseCount = rs.getInt("weeklyDecreaseCount");
                oneReport.setWeeklyDecreaseCount(weeklyDecreaseCount);
                int monthlyDecreaseCount = rs.getInt("monthlyDecreaseCount");
                oneReport.setMonthlyDecreaseCount(monthlyDecreaseCount);
                int dailyReportCount = rs.getInt("dailyReportCount");
                oneReport.setDailyReportCount(dailyReportCount);
                int weeklyReportCount = rs.getInt("weeklyReportCount");
                oneReport.setWeeklyReportCount(weeklyReportCount);
                int monthlyReportCount = rs.getInt("monthlyReportCount");
                oneReport.setMonthlyReportCount(monthlyReportCount);

                String key = hostname + ":" + port;
                reportsMap.put(key, oneReport);
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
        return reportsMap;
    }

    @Override
    public Map<String, ProcessReport> getProcessReports(ArrayList<ProcessConnection> processes)
    {
        HashMap<String, ProcessReport> reportsMap = new HashMap<>();

        for (ProcessConnection p : processes){
            String key = p.getHostName() + ":" + p.getPort();
            reportsMap.put(key, getProcessReport(p.getHostName(), p.getPort()));
        }

        return reportsMap;
    }

    @Override
    public ProcessReport getProcessReport(String hostName, int port)
    {
        //Return null if it does not exist
        if (countRows("ProcessReport", hostName, port) < 1)
            return null;

        String query = "SELECT startTime, endTime, port, hostname, status FROM ProcessReport WHERE hostname = ? AND port = ? ORDER BY startTime;";
        ProcessReport oneReport = new ProcessReport();
        try
        {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, hostName);
            stmt.setInt(2, port);

            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                Long startTime = Long.parseLong(rs.getString("startTime"));
                oneReport.setStartTime(startTime);
                Long endTime =  Long.parseLong(rs.getString("endTime"));
                oneReport.setEndTime(endTime);
                port = Integer.parseInt(rs.getString("port"));
                oneReport.setPort(port);
                String hostname = rs.getString("hostname");
                oneReport.setHostName(hostname);
                String status = rs.getString("status");
                oneReport.setStatus(status);
                int consecMemInc = rs.getInt("consec_mem_inc_count");
                oneReport.setConsec_mem_inc_count(consecMemInc);
                long usageAfterFirstGc = rs.getLong("usageAfterFirstGc");
                oneReport.setUsageAfterFirstGc(usageAfterFirstGc);
                long usageAfterLastGc = rs.getLong("usageAfterLastGc");
                oneReport.setUsageAfterLastGc(usageAfterLastGc);
                double dailySumMemUsageDif = rs.getDouble("dailySumMemUsageDif");
                oneReport.setDailySumMemUsageDif(dailySumMemUsageDif);
                double weeklySumMemUsageDif = rs.getDouble("weeklySumMemUsageDif");
                oneReport.setWeeklySumMemUsageDif(weeklySumMemUsageDif);
                double monthlySumMemUsageDif = rs.getDouble("monthlySumMemUsageDif");
                oneReport.setMonthlySumMemUsageDif(monthlySumMemUsageDif);
                double dailyMinMemUsageDif = rs.getDouble("dailyMinMemUsageDif");
                oneReport.setDailyMinMemUsageDif(dailyMinMemUsageDif);
                double weeklyMinMemUsageDif = rs.getDouble("weeklyMinMemUsageDif");
                oneReport.setWeeklyMinMemUsageDif(weeklyMinMemUsageDif);
                double monthlyMinMemUsageDif = rs.getDouble("monthlyMinMemUsageDif");
                oneReport.setMonthlyMinMemUsageDif(monthlyMinMemUsageDif);
                int dailyIncreaseCount = rs.getInt("dailyIncreaseCount");
                oneReport.setDailyIncreaseCount(dailyIncreaseCount);
                int weeklyIncreaseCount = rs.getInt("weeklyIncreaseCount");
                oneReport.setWeeklyIncreaseCount(weeklyIncreaseCount);
                int monthlyIncreaseCount = rs.getInt("monthlyIncreaseCount");
                oneReport.setMonthlyIncreaseCount(monthlyIncreaseCount);
                int dailyDecreaseCount = rs.getInt("dailyDecreaseCount");
                oneReport.setDailyDecreaseCount(dailyDecreaseCount);
                int weeklyDecreaseCount = rs.getInt("weeklyDecreaseCount");
                oneReport.setWeeklyDecreaseCount(weeklyDecreaseCount);
                int monthlyDecreaseCount = rs.getInt("monthlyDecreaseCount");
                oneReport.setMonthlyDecreaseCount(monthlyDecreaseCount);
                int dailyReportCount = rs.getInt("dailyReportCount");
                oneReport.setDailyReportCount(dailyReportCount);
                int weeklyReportCount = rs.getInt("weeklyReportCount");
                oneReport.setWeeklyReportCount(weeklyReportCount);
                int monthlyReportCount = rs.getInt("monthlyReportCount");
                oneReport.setMonthlyReportCount(monthlyReportCount);
            }
            stmt.close();

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

    public int countRows(String tableName, String hostName, int port){
        String query = "SELECT COUNT(*) AS count FROM " + tableName + " WHERE hostname = ? AND port = ?;";
        int count = 0;
        try {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, hostName);
            stmt.setInt(2, port);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                count = rs.getInt(1);
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //If error
        return 0;
    }

    /**
     * Returns number of rows in specified table where the specified column has the specified value
     * @param tableName Name of the table in the database
     * @param hostName hostname of process (connection)
     * @param port port of process (connection)
     * @param columnName Name of column which will be used in a WHERE-condition
     * @param value Value in specified column
     * @return Number of rows found.
     */
    public int countRows(String tableName, String hostName, int port, String columnName, String value){
        String query = "SELECT COUNT(*) AS count FROM " + tableName + " WHERE hostname = ? AND port = ? AND " + columnName + " = ?;";
        int count = 0;
        try {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, hostName);
            stmt.setInt(2, port);
            stmt.setString(3, value);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                count = rs.getInt(1);
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //If error
        return 0;
    }

    public int deleteRows(String tableName, String hostName, int port, String columnName, String value){
        int count = 0;
        String query = "DELETE FROM " + tableName + " WHERE hostname = ? AND port = ? AND " + columnName + " = ?;";
        try {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, hostName);
            stmt.setInt(2, port);
            stmt.setString(3, value);
            count = stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public ArrayList<AnomalyReport> getAnomalyReports(String hostname, int port)
    {
        ArrayList<AnomalyReport> fetchReports = new ArrayList<>();
        String query = "SELECT hostname, port, timestamp, errorMsg, startTimeIncrease, anomalyStatus," +
                "memIncreasePercentage, memIncreaseBytes FROM AnomalyReport WHERE hostname = ? AND port = ?;";
        try
        {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, hostname);
            stmt.setInt(2, port);
            ResultSet rs = stmt.executeQuery();
            while(rs.next())
            {
                try
                {
                    AnomalyReport tempReport = new AnomalyReport();
                    tempReport.setHost(rs.getString("hostname"));
                    tempReport.setPort(Integer.parseInt(rs.getString("port")));
                    tempReport.setTimestamp(Long.parseLong(rs.getString("timestamp")));
                    tempReport.setErrorMsg(rs.getString("errorMsg"));
                    tempReport.setStartTimeIncrease(Long.parseLong(rs.getString("startTimeIncrease")));
                    tempReport.setAnomaly(rs.getString("anomalyStatus"));
                    tempReport.setMemIncreasePercentage(Integer.parseInt(rs.getString("memIncreasePercentage")));
                    tempReport.setMemIncreaseBytes(Long.parseLong(rs.getString("memIncreaseBytes")));
                    fetchReports.add(tempReport);
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }
            stmt.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return fetchReports;
    }

    @Override
    public ArrayList<AnomalyReport> getAnomalyReports(long startTime, long endTime, String hostName, int port) {
        ArrayList<AnomalyReport> fetchReports = new ArrayList<>();
        String query = "SELECT hostname, port, timestamp, errorMsg, startTimeIncrease, anomalyStatus," +
                "memIncreasePercentage, memIncreaseBytes FROM AnomalyReport WHERE hostname = ? AND port = ? AND timestamp >= ? AND timestamp <= ?;";
        try
        {
            AnomalyReport tempReport = new AnomalyReport();
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, hostName);
            stmt.setInt(2, port);
            stmt.setLong(3, startTime);
            stmt.setLong(4, endTime);
            ResultSet rs = stmt.executeQuery();
            while(rs.next())
            {
                try
                {
                    tempReport.setHost(rs.getString("hostname"));
                    tempReport.setPort(Integer.parseInt(rs.getString("port")));
                    tempReport.setTimestamp(Long.parseLong(rs.getString("timestamp")));
                    tempReport.setErrorMsg(rs.getString("errorMsg"));
                    tempReport.setStartTimeIncrease(Long.parseLong(rs.getString("startTimeIncrease")));
                    tempReport.setAnomaly(rs.getString("anomalyStatus"));
                    tempReport.setMemIncreasePercentage(Integer.parseInt(rs.getString("memIncreasePercentage")));
                    tempReport.setMemIncreaseBytes(Long.parseLong(rs.getString("memIncreaseBytes")));
                    fetchReports.add(tempReport);
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }
            stmt.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return fetchReports;
    }

    @Override
    public void sendAnomalyReport(AnomalyReport aReport)
    {
        try
        {
            Statement DB = null;
            DB = DBConnection.createStatement();
            DB.executeUpdate("INSERT INTO AnomalyReport(hostname, port, timestamp, errorMsg, startTimeIncrease," +
                    " anomalyStatus, memIncreasePercentage, memIncreaseBytes)"+
                    "VALUES("+"'"+aReport.getHost()+"',"+aReport.getPort()+","+aReport.getTimestamp()+
                    ",'"+aReport.getErrorMsg()+"',"+aReport.getStartTimeIncrease()+",'"+aReport.getAnomaly().toString()+"',"+
                    aReport.getMemIncreasePercentage()+","+aReport.getMemIncreaseBytes()+")");
            DB.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //@TODO not sure if this is still used, if so, then it needs to be modified to either to create a processreport,
    // and a check if exists to modify an existing one
    @Override
    public void sendProcessReport(int port, String hostname, ProcessReport report)
    {
        try
        {
            Statement DB = null;
            DB = DBConnection.createStatement();
            DB.executeUpdate("INSERT INTO ProcessReport(startTime, endTime, hostname, port, status, consec_mem_inc_count, usageAfterFirstGc, usageAfterLastGc, dailySumMemUsageDif, weeklySumMemUsageDif, " +
                            "monthlySumMemUsageDif, dailyMinMemUsageDif, weeklyMinMemUsageDif, monthlyMinMemUsageDif, dailyIncreaseCount, weeklyIncreaseCount, monthlyIncreaseCount, dailyDecreaseCount, " +
                            "weeklyDecreaseCount, monthlyDecreaseCount, dailyReportCount, weeklyReportCount, monthlyReportCount)"+
                            " VALUES("+report.getStartTime()+","+report.getEndTime()+",'"+hostname+"',"+port+",'"+report.getStatus()+"',"
                            +report.getConsec_mem_inc_count()+","+report.getUsageAfterFirstGc()+","
                            +report.getUsageAfterLastGc()+","+report.getDailySumMemUsageDif()+","+report.getWeeklySumMemUsageDif()+","+report.getMonthlySumMemUsageDif()+","+report.getDailyMinMemUsageDif()+","
                            +report.getWeeklyMinMemUsageDif()+","+report.getMonthlyMinMemUsageDif()+","+report.getDailyIncreaseCount()+","+report.getWeeklyIncreaseCount()+","+report.getMonthlyIncreaseCount()+","
                            +report.getDailyDecreaseCount()+","+report.getWeeklyDecreaseCount()+","+report.getMonthlyDecreaseCount()+","+report.getDailyReportCount()+","+report.getWeeklyReportCount()+","
                            +report.getMonthlyReportCount()+")");
            DB.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public long firstGcValue(String process)
    {
        String[] hostPort = process.split(":");
        String hostname = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);
        if (countRows("ProcessReport", hostname, port) < 1)
            return -1;
        long GcMinMemValue = -1;
        String query = "SELECT usageAfterFirstGc FROM ProcessReport WHERE hostname = ? AND port = ?;";
        try
        {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, hostname);
            stmt.setInt(2, port);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                GcMinMemValue = Long.parseLong(rs.getString("usageAfterFirstGc"));
            stmt.close();
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
            Statement DB = null;
            DB = DBConnection.createStatement();
            String input = "";
            input = "DELETE FROM GCReport";
            DB.executeUpdate(input);
            input = "DELETE FROM GCLog";
            DB.executeUpdate(input);
            input = "DELETE FROM MemLog";
            DB.executeUpdate(input);
            input = "DELETE FROM ProcessReport";
            DB.executeUpdate(input);
            input = "DELETE FROM AnomalyReport";
            DB.executeUpdate(input);
            DB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendUsageAfterLastGc(long usageAfterLastGc, String hostname, int port) {
        //ProcessReport does not exist
        if (getProcessReport(hostname, port) == null){
            ProcessReport pReport = new ProcessReport(hostname, port);
            pReport.setUsageAfterLastGc(usageAfterLastGc);
            sendProcessReport(port, hostname, pReport);
        }
        //ProcessReport exists
        else {
            String query = "UPDATE ProcessReport SET usageAfterLastGc = ? WHERE hostname = ? AND port = ?;";
            try {
                PreparedStatement stmt = DBConnection.prepareStatement(query);
                stmt.setLong(1, usageAfterLastGc);
                stmt.setString(2, hostname);
                stmt.setInt(3, port);
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendUsageAfterFirstGc(long usageAfterFirstGc, String hostname, int port){
        //ProcessReport does not exist
        if (getProcessReport(hostname, port) == null){
            ProcessReport pReport = new ProcessReport(hostname, port);
            pReport.setUsageAfterFirstGc(usageAfterFirstGc);
            sendProcessReport(port, hostname, pReport);
        }
        //ProcessReport exists and needs to be updated
        else{
            String query = "UPDATE ProcessReport SET usageAfterFirstGc = " + usageAfterFirstGc + " WHERE hostname = '" +
                    hostname + "' AND " + " port = " + port;
            try {
                Statement DB = null;
                DB = DBConnection.createStatement();
                DB.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            while(processesCounter < processes.size())
            {
                getPortHostname = processes.get(processesCounter);
                processesCounter++;

                if(getPortHostname.contains(":"))
                {
                    String[] portHostname = getPortHostname.split("\\:");
                    String host = portHostname[0];
                    int port = Integer.parseInt(portHostname[1]);
                    input = "DELETE FROM GCLog WHERE hostname = ? AND port = ?;";
                    PreparedStatement stmt = DBConnection.prepareStatement(input);
                    stmt.setString(1, host);
                    stmt.setInt(2, port);
                    stmt.executeUpdate(input);
                    stmt.close();
                    input = "DELETE FROM MemLog WHERE hostname = ? AND port = ?;";
                    stmt = DBConnection.prepareStatement(input);
                    stmt.setString(1, host);
                    stmt.setInt(2, port);
                    stmt.executeUpdate(input);
                    stmt.close();
                    input = "DELETE FROM GCReport WHERE hostname = ? AND port = ?;";
                    stmt = DBConnection.prepareStatement(input);
                    stmt.setString(1, host);
                    stmt.setInt(2, port);
                    stmt.executeUpdate(input);
                    stmt.close();
                    input = "DELETE FROM ProcessReport WHERE hostname = ? AND port = ?";
                    stmt = DBConnection.prepareStatement(input);
                    stmt.setString(1, host);
                    stmt.setInt(2, port);
                    stmt.executeUpdate(input);
                    stmt.close();
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<GcReport> getPossibleMemoryLeaks(String host, int port) {
        ArrayList<GcReport> reports = new ArrayList<>();
        String query = "SELECT * FROM GcReport WHERE status = ? AND hostname = ? AND port = ? ORDER BY startTime;";

        try {
            PreparedStatement stmt = DBConnection.prepareStatement(query);
            stmt.setString(1, GcReport.Status.POSSIBLE_MEMORY_LEAK.toString());
            stmt.setString(2, host);
            stmt.setInt(3, port);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                GcReport theGcReport = new GcReport();
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

                reports.add(theGcReport);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    @Override
    public int clearPossibleMemoryLeaks(String hostname, int port) {
        int count = countRows("GcReport", hostname, port, "status", GcReport.Status.POSSIBLE_MEMORY_LEAK.toString());
        deleteRows("GcReport", hostname, port, "status", GcReport.Status.POSSIBLE_MEMORY_LEAK.toString());
        return count;
    }

}
