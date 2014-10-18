package AnomalyDetector;

import Logs.GcStats;
import Logs.ILogging;
import Logs.Log;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Oliver on 2014-09-12.
 */
public class JMXAgent {
    /**
     * Class which handles the notifications
     */
    public static class AgentListener implements NotificationListener{
        JMXAgent agent;

        public AgentListener(JMXAgent agent){
            this.agent = agent;
        }
        public void handleNotification(Notification notification, Object handback){
            //GarbageCollection has occurred.
            if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)){
                System.out.println("GARBAGECOLLECTION NOTIFICIATION!"); // Test
                GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                agent.gcLog(info);
            }
        }
    }

    class PollTimer extends TimerTask{
        JMXAgent agent;
        PollTimer(JMXAgent agent){
            this.agent = agent;
        }
        public void run(){
            agent.gatherMemoryStatistics();
            System.out.println("BOOP");
        }
    }

    /**
     * Sends GarbageCollection statistics to log.
     * @param info Info about the Garbage Collection.
     */
    private void gcLog(GarbageCollectionNotificationInfo info){
        MemoryUsage oldGenAfter = info.getGcInfo().getMemoryUsageAfterGc().get("PS Old Gen");
        MemoryUsage oldGenBefore = info.getGcInfo().getMemoryUsageBeforeGc().get("PS Old Gen");
        long timeStamp = Calendar.getInstance().getTimeInMillis();
        long collectionTime = info.getGcInfo().getDuration();
        log.sendGarbageCollectionLog(oldGenAfter.getUsed(), oldGenBefore.getUsed(), timeStamp, collectionTime, hostName, port);
        log.sendUsageAfterLastGc(oldGenAfter.getUsed(), hostName, port);
        ad.getAnalyzer().analyzeExcessiveGcScan(hostName, port, new GcStats(oldGenAfter.getUsed(), oldGenBefore.getUsed(), timeStamp, collectionTime));
    }

    /**
     * Sends memory statistics to log
     * @param memoryUsed Current memory usage in bytes
     * @param timeStamp Timestamp
     */
    private void memoryLog(long memoryUsed, long timeStamp){
        log.sendMemoryLog(memoryUsed, timeStamp, hostName, port);
    }

    /**
     * Gather statistics about Old Gen which will be sent to log
     */
    private void gatherMemoryStatistics(){
        //Get memory usage in old gen
        long memoryUsed = oldGenProxy.getUsage().getUsed();
        //Get timestamp
        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        long timeStamp = date.getTime();
        //Handle logging
        memoryLog(memoryUsed, timeStamp);

    }


    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    //Settings
    private String hostName;
    private int port;
    private double interval;
    public static int DEFAULT_INTERVAL_MINUTES = 5;

    //Resources
    private JMXServiceURL url;
    private JMXConnector jmxc;
    private MBeanServerConnection mbsc;
    private AgentListener listener;
    MemoryPoolMXBean oldGenProxy;
    GarbageCollectorMXBean markSweepProxy;
    private AnomalyDetector ad;
    ILogging log;
    Timer timer;

    private boolean connected = false;

    public JMXAgent(ProcessConnection c, AnomalyDetector ad){
        this(c.getHostName(), c.getPort(), ad);
    }

    public JMXAgent(String hostName, int port, AnomalyDetector ad) {
        this.hostName = hostName;
        this.port = port;
        this.ad = ad;
        log = Log.getInstance();
        this.listener = new AgentListener(this);
        this.interval=DEFAULT_INTERVAL_MINUTES;
        this.timer = new Timer();
        scheduleGathering();
        try {
            connect();
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
        }

        try {
            createProxies();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws IOException {
        this.url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName +
                ":" + port + "/jmxrmi");
        this.jmxc = JMXConnectorFactory.connect(url, null);
        this.mbsc = jmxc.getMBeanServerConnection();
        connected = true;
        try{
            addListeners();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected(){
        return connected;
    }

    /**
     * Schedule how often memory-statistics will be gathered and logged, based on set interval.
     */
    private void scheduleGathering() {
        timer.scheduleAtFixedRate(new PollTimer(this), 1000, (long)interval * 60 * 1000);
    }

    /**
     * Adds listeners to relevant MXBeans
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws InstanceNotFoundException
     */
    private void addListeners() throws MalformedObjectNameException, IOException, InstanceNotFoundException {
        //Add listener to MXBean
        if(System.getProperty("os.name").startsWith("Windows"))
        {
            ObjectName name = new ObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep");
            mbsc.addNotificationListener(name, listener, null, null);
        }
        else if (System.getProperty("os.name").startsWith("Linux"))
        {
            ObjectName name = new ObjectName("java.lang:type=GarbageCollector,name=MarkSweepCompact");
            mbsc.addNotificationListener(name, listener, null, null);
        }
    }

    private void createProxies() throws MalformedObjectNameException{
        //Old Gen
        System.out.println(System.getProperty("os.name"));
        if(System.getProperty("os.name").startsWith("Windows"))
        {
            oldGenProxy= JMX.newMXBeanProxy(mbsc, new ObjectName("java.lang:type=MemoryPool,name=PS Old Gen"),
                    MemoryPoolMXBean.class);
            //GarbageCollector MarkSweep
            markSweepProxy = JMX.newMBeanProxy(mbsc, new ObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep"),
                    GarbageCollectorMXBean.class);

        }
        else if(System.getProperty("os.name").startsWith("Linux"))
        {
            // oldGenProxy= JMX.newMXBeanProxy(mbsc, new ObjectName("java.lang:type=MemoryPool,name=PS Old Gen"),
            oldGenProxy= JMX.newMXBeanProxy(mbsc, new ObjectName("java.lang:type=MemoryPool,name=Tenured Gen"),
                    MemoryPoolMXBean.class);
            //GarbageCollector MarkSweep
            markSweepProxy = JMX.newMBeanProxy(mbsc, new ObjectName("java.lang:type=GarbageCollector,name=MarkSweepCompact"),
                    GarbageCollectorMXBean.class);
        }

    }
}
