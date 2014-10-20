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
import java.util.Calendar;
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

    /**
     * Sends GarbageCollection statistics to log.
     * @param info Info about the Garbage Collection.
     */
    private void gcLog(GarbageCollectionNotificationInfo info){
        MemoryUsage oldGenAfter = info.getGcInfo().getMemoryUsageAfterGc().get(heapName);
        MemoryUsage oldGenBefore = info.getGcInfo().getMemoryUsageBeforeGc().get(heapName);
        long timeStamp = info.getGcInfo().getStartTime() + Log.getProgramStart().getTime();
        long collectionTime = info.getGcInfo().getDuration();
        log.sendGarbageCollectionLog(oldGenAfter.getUsed(), oldGenBefore.getUsed(), timeStamp, collectionTime, hostName, port);
        log.sendUsageAfterLastGc(oldGenAfter.getUsed(), hostName, port);
        ad.getAnalyzer().analyzeExcessiveGcScan(hostName, port, new GcStats(oldGenAfter.getUsed(), oldGenBefore.getUsed(), timeStamp, collectionTime));
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    //Settings
    private String gcPath;
    private String heapPath;
    private String heapName;
    private String hostName;
    private int port;

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
        if(System.getProperty("os.name").startsWith("Windows")){
            gcPath = "java.lang:type=GarbageCollector,name=PS MarkSweep";
            heapPath = "java.lang:type=MemoryPool,name=PS Old Gen";
            heapName = "PS Old Gen";
        }
        else if (System.getProperty("os.name").startsWith("Linux")){
            gcPath = "java.lang:type=GarbageCollector,name=MarkSweepCompact";
            heapPath = "java.lang:type=MemoryPool,name=Tenured Gen";
            heapName = "Tenured Gen";
        }
        this.hostName = hostName;
        this.port = port;
        this.ad = ad;
        log = Log.getInstance();
        this.listener = new AgentListener(this);
        this.timer = new Timer();
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
     * Adds listeners to relevant MXBeans
     * @throws MalformedObjectNameException
     * @throws IOException
     * @throws InstanceNotFoundException
     */
    private void addListeners() throws MalformedObjectNameException, IOException, InstanceNotFoundException {
        //Add listener to MXBean
        ObjectName name = new ObjectName(gcPath);
        mbsc.addNotificationListener(name, listener, null, null);
    }

    private void createProxies() throws MalformedObjectNameException{
        //Old Gen
        oldGenProxy= JMX.newMXBeanProxy(mbsc, new ObjectName(heapPath),
                MemoryPoolMXBean.class);
        //GarbageCollector MarkSweep
        markSweepProxy = JMX.newMBeanProxy(mbsc, new ObjectName(gcPath),
                GarbageCollectorMXBean.class);
    }
}
