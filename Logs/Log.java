package Logs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.JDBC;
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
    private static Statement DB;

    public static final void main(String[] args) throws ClassNotFoundException
    {

        Log test = new Log();

//        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");


        Connection connection = null;
        try
        {
//            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            DB = connection.createStatement();
            DB.setQueryTimeout(30);  // set timeout to 30 sec.

            //statement.executeUpdate("create table MemLog(MemId INTEGER PRIMARY KEY AUTOINCREMENT, timestamp BIGINT, usedMemory BIGINT, hostname VARCHAR(25), port INTEGER)" );

//            DB.executeUpdate("create table GCLog(gcId INTEGER PRIMARY KEY AUTOINCREMENT, timestamp BIGINT," +
//                    " memUsageAfter BIGINT, memUsageBefore BIGINT, GCCollectionTime BIGINT, hostname VARCHAR(25), port INTEGER)");

//            DB.executeUpdate("create table AnalyzedGCData(analyzedId INTEGER PRIMARY KEY AUTOINCREMENT, avgCollected BIGINT, minCollected BIGINT, maxCollected BIGINT, minMemoryUsage BIGINT," +
//                    "maxMemoryUsage BIGINT, avgMemoryUsage BIGINT, startMemoryUsage BIGINT, endMemoryUsage BIGINT,avgTimeBetweenGc BIGINT, " +
//                    "minTimeBetweenGc BIGINT, maxTimeBetweenGc BIGINT, starttime BIGINT, endTime BIGINT, hostname VARCHAR(25), port INTEGER, trend VARCHAR(45), gcCount INTEGER)");

            //   statement.executeUpdate("INSERT INTO MemLog(timestamp, usedMemory, hostname, port) VALUES(13371337, 10241024, '127.0.0.1', 3500)");

            //  System.out.println(test.createMemLogEntry(10001000,1337669,"localhost",3800));
            //statement.executeUpdate(test.createMemLogEntry(10001000,1337669,"localhost",3800));
            //statement.executeUpdate("DELETE  FROM MemLog WHERE MemID = 12342145");
            //statement.executeUpdate("DROP TABLE IF EXISTS MemLog");
//            statement.executeUpdate("create table person (id integer, name string)");
//            statement.executeUpdate("insert into person values(1, 'leo')");
//            statement.executeUpdate("insert into person values(2, 'yui')");

//            AnalyzedGcStats temp = new AnalyzedGcStats();
//            test.sendAnalyzedGCData("127.0.0.1",3800,temp);

            //this one gives duplicate rows when performed
         //  test.sendMemoryLog(1337,2233222, "127.0.0.1",3800);
//
            //test.sendGarbageCollectionLog(0,0,0,0, "127.0.0.1", 3800);
           // DB.executeUpdate("DELETE FROM MemLog WHERE MemId = 4" );
            ResultSet rs = DB.executeQuery("select * from MemLog");

            while(rs.next())
            {
                // read the result set

                System.out.println("MemId = " + rs.getString("MemId"));
                System.out.println("timestamp = " + rs.getString("timestamp"));
                System.out.println("usedMemory = " + rs.getString("usedMemory"));
                System.out.println("hostname = " + rs.getString("hostname"));
                System.out.println("port = " + rs.getString("port"));
            }
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e);
            }

        }

    }

    public long getGCTimeStamp() {
        return GCTimeStamp;
    }

    public long getGCmemoryUsageAfter() {
        return GCmemoryUsageAfter;
    }

    public long getGCmemoryUsageBefore() {
        return GCmemoryUsageBefore;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getGCTime() {
        return GCTime;
    }

    public Log()
    {
        GCTime = 0;
        GCTimeStamp = 0;
        GCmemoryUsageAfter = 0;
        GCmemoryUsageBefore = 0;
        memoryUsed = 0;
        timeStamp = 0;
        ip = "";
        port = 0;
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
            DB.executeUpdate("INSERT INTO  GCLog(timestamp, memUsageAfter, memUsageBefore, GCCollectionTime, hostname, port)" +
                    " VALUES("+timestamp +","+memoryUsedAfter+","+memoryUsedBefore+","+collectionTime+",'"+hostname+"',"+port+")");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //basic input values  works, BUT duplicate rows are possible
    @Override
    public void sendMemoryLog(long memoryUsed, long timestamp, String hostname, int port)
    {
        try
        {
            DB.executeUpdate("INSERT INTO  MemLog(timestamp,usedMemory,hostname,port) VALUES("+timestamp +","+memoryUsed+","+"'"+hostname+"'"+","+port+")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, ArrayList<GcStats>> getGarbageCollectionStats(long startTime, long endTime)
    {
        return null;
    }

    @Override
    public Map<String, ArrayList<GcStats>> getGarbageCollectionStats(long startTime, long endTime, ArrayList<String> processes)
    {
        return null;
    }

    @Override
    public Map<String, ArrayList<MemoryStats>> getMemoryStats(long startTime, long endTime)
    {
        return null;
    }

    @Override
    public Map<String, ArrayList<MemoryStats>> getMemoryStats(long startTime, long endTime, ArrayList<String> processes)
    {
        return null;
    }


    //input values works
    @Override
    public void sendAnalyzedGCData(String hostName, int port, AnalyzedGcStats analyzedDailyGcStats)
    {
        try
        {
            String fetch = "INSERT INTO  AnalyzedGCData(avgCollected, minCollected,maxCollected,minMemoryUsage,maxMemoryUsage," +
                    "avgMemoryUsage,startMemoryUsage,endMemoryUsage,avgTimeBetweenGc,minTimeBetweenGc,maxTimeBetweenGc,starttime,endtime,hostname,port,trend,gcCount) " +
                    "VALUES("+ analyzedDailyGcStats.getAvgCollected()+","+analyzedDailyGcStats.getMinCollected()+","+analyzedDailyGcStats.getMaxCollected()+","+analyzedDailyGcStats.getMinMemoryUsage()+","
                    +analyzedDailyGcStats.getMaxMemoryUsage()+","+analyzedDailyGcStats.getAvgMemoryUsage()+","+
                    analyzedDailyGcStats.getStartMemoryUsage()+","+analyzedDailyGcStats.getEndMemoryUsage()+","+analyzedDailyGcStats.getAvgTimeBetweenGc()+","+
            analyzedDailyGcStats.getMinTimeBetweenGc()+","+analyzedDailyGcStats.getMaxTimeBetweenGc()+","+analyzedDailyGcStats.getStartTime()+","+analyzedDailyGcStats.getEndTime()+",'"+hostName+"',"+port+",'"+analyzedDailyGcStats.getTrend()+"',"
                    +analyzedDailyGcStats.getGcCount()+")";
            System.out.println(fetch);
            DB.executeUpdate(fetch);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
