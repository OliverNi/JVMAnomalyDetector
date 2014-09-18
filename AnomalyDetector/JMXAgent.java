package AnomalyDetector;

import Logs.ILogging;
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

    /**
     * Sends GarbageCollection statistics to log.
     * @param info Info about the Garbage Collection.
     */
    private void gcLog(GarbageCollectionNotificationInfo info){
        MemoryUsage oldGenAfter = info.getGcInfo().getMemoryUsageAfterGc().get("PS Old Gen");
        MemoryUsage oldGenBefore = info.getGcInfo().getMemoryUsageBeforeGc().get("PS Old Gen");
        long timeStamp = info.getGcInfo().getEndTime();
        long collectionTime = info.getGcInfo().getDuration();
        log.sendGarbageCollectionLog(oldGenAfter.getUsed(), oldGenBefore.getUsed(), timeStamp, collectionTime, hostName, port);
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
        long memoryUsed = MXBeanProxy.get(0).getUsage().getUsed();
        //Get timestamp
        Calendar calendar = Calendar.getInstance();
        java.util.Date date = calendar.getTime();
        long timeStamp = date.getTime();
        //Handle logging
        memoryLog(memoryUsed, timeStamp);

    }


    //Settings
    private String hostName;
    private int port;
    private double interval;
    //Resources
    private JMXServiceURL url;
    private JMXConnector jmxc;
    private MBeanServerConnection mbsc;
    private AgentListener listener;
    //@TODO Remove list and only use old gen?
    private ArrayList<MemoryPoolMXBean> MXBeanProxy = new ArrayList<>();
    private ArrayList<GarbageCollectorMXBean> gcProxy = new ArrayList<>();
    private AnomalyDetector ad;
    ILogging log;

    //Saved variables
    private long previousUsedMem;

    public JMXAgent(String hostName, int port, AnomalyDetector ad) {
        this.hostName = hostName;
        this.port = port;
        this.ad = ad;
        log = ad.getLog();
        this.listener = new AgentListener(this);
        try {
            connect();
        } catch (IOException e) {
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

        try{
            addListeners();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void addListeners() throws MalformedObjectNameException, IOException, InstanceNotFoundException {
        //Add listener to MXBean
        ObjectName name = new ObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep");
        mbsc.addNotificationListener(name, listener, null, null);
    }

    private void createProxies() throws MalformedObjectNameException{
        //Old Gen
        MemoryPoolMXBean oldGenProxy= JMX.newMXBeanProxy(mbsc, new ObjectName("java.lang:type=MemoryPool,name=PS Old Gen"),
                MemoryPoolMXBean.class);
        MXBeanProxy.add(oldGenProxy);
        //Survivor Space
        /*
        MemoryPoolMXBean survivorProxy= JMX.newMXBeanProxy(mbsc, new ObjectName("java.lang:type=MemoryPool,name=PS Survivor Space"),
                MemoryPoolMXBean.class);
        MXBeanProxy.add(survivorProxy);
        */
        //Eden Space
        /*
        MemoryPoolMXBean edenProxy= JMX.newMXBeanProxy(mbsc, new ObjectName("java.lang:type=MemoryPool,name=PS Eden Space"),
                MemoryPoolMXBean.class);
        MXBeanProxy.add(edenProxy);
        */
        //GC PS MarkSweep
        gcProxy.add(JMX.newMBeanProxy(mbsc, new ObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep"), GarbageCollectorMXBean.class));

        //GC PS Scavenge
        gcProxy.add(JMX.newMBeanProxy(mbsc, new ObjectName("java.lang:type=GarbageCollector,name=PS Scavenge"), GarbageCollectorMXBean.class));
    }

    /**
     * For testing purposes
     */
    public void gather(){
        System.out.println(hostName + ":" + port);
        //Memory stats
        System.out.println("--MemoryPool--");
        for (MemoryPoolMXBean bean : MXBeanProxy){
            System.out.println(bean.getName() + " usage: " + (bean.getUsage().getUsed() / 1024) + " kb");
            System.out.println(bean.getName() + " peak usage: " + bean.getPeakUsage().getUsed() / 1024 + " kb");
           // System.out.println(bean.getName() + " " + bean.getUsageThreshold() / 1024 + " kb");

            System.out.println(bean.getName() + " collection usage, used: " + bean.getCollectionUsage().getUsed() / 1024 + " kb");
            System.out.println(bean.getName() + " committed: " + bean.getUsage().getCommitted() / 1024 + " kb");
        }

        //GC stats
        System.out.println("--GarbageCollector--");
        for (GarbageCollectorMXBean bean : gcProxy) {
            System.out.println(bean.getName() + " GC COUNT: " + bean.getCollectionCount());
            System.out.println(bean.getName() + " GC TIME: " + bean.getCollectionTime() + " ms");


        }
    }



}
