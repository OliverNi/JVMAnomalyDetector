package AnomalyDetector;

import Logs.GcStats;
import Logs.MemoryUsageLog;
import Logs.Log;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.remote.*;
import java.io.IOException;
import java.lang.management.*;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Oliver on 2014-09-12.
 */
public class JMXAgent extends Thread{
    class ReconnectTask extends TimerTask{
        @Override
        public void run() {
            connect();
        }
    }

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
                GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                agent.gcLog(info);
            }
            else if (notification.getType().equals(JMXConnectionNotification.OPENED)){
                agent.ad.print("Connected to: " + agent.hostName + ":" + agent.port + " ...");
                agent.connected = true;
                try {
                    agent.mbsc = agent.jmxc.getMBeanServerConnection();
                    agent.addListeners();
                    agent.createProxies();
                }catch (IOException e){
                    e.printStackTrace();
                } catch (MalformedObjectNameException e) {
                    e.printStackTrace();
                } catch (InstanceNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if (notification.getType().equals(JMXConnectionNotification.CLOSED)){
                agent.ad.print("Connection to " + agent.hostName + ":" + agent.port + " closed");
                agent.connected = false;
                agent.reconnect(RECONNECT_TIME);
            }
            else if (notification.getType().equals(JMXConnectionNotification.FAILED)){
                agent.ad.print("Error with connection to " + agent.hostName + ":" + agent.port);
            }
            else if (notification.getType().equals(JMXConnectionNotification.NOTIFS_LOST)){
                agent.ad.print("NOTIFS Lost for connection: " + agent.hostName + ":" + agent.port);
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
        long timeStamp = info.getGcInfo().getStartTime() + runtimeMXBean.getStartTime();
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
    public static long RECONNECT_TIME = 1000 * 60; //One minute
    private String gcPath;
    private String heapPath;
    private String heapName;
    private String hostName;
    private String runtimePath;
    private int port;

    //Resources
    private JMXServiceURL url;
    private JMXConnector jmxc;
    private MBeanServerConnection mbsc = null;
    private AgentListener listener;
    private AnomalyDetector ad;
    private MemoryUsageLog log;
    private RuntimeMXBean runtimeMXBean;
    private Timer reconnectTimer = new Timer();

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
        connect();
    }

    public void connect() {
        reconnectTimer.cancel();
        try {
            this.url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName +
                    ":" + port + "/jmxrmi");
            this.jmxc = JMXConnectorFactory.newJMXConnector(url, null);
            jmxc.addConnectionNotificationListener(listener, null, null);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jmxc.connect();
        }
        catch (IOException e){
            System.out.println(hostName + ":" + port + ": Connection error...");
        }

        if (!connected) {
            ad.print(hostName + ":" + port + ": Connection failed...");
            reconnect(RECONNECT_TIME);
        }
    }

    private void reconnect(long delay){
        ad.print("Reconnecting in " + RECONNECT_TIME / 1000 + " seconds.");
        reconnectTimer = new Timer();
        reconnectTimer.schedule(new ReconnectTask(), delay);
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

    private void createProxies() throws IOException{
        runtimeMXBean = ManagementFactory.getPlatformMXBean(mbsc, RuntimeMXBean.class);
    }

    public void cancelTimer(){
        reconnectTimer.cancel();
        reconnectTimer.purge();
    }
}
