package AnomalyDetector;

import Logs.GcStats;
import Logs.ILogging;
import Logs.Log;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.remote.*;
import javax.security.auth.Subject;
import java.io.IOException;
import java.lang.management.*;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Oliver on 2014-09-12.
 */
public class JMXAgent implements Runnable{
    @Override
    public void run() {

    }

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
                System.out.println("OPENED");
                agent.connected = true;
                try {
                    agent.mbsc = agent.jmxc.getMBeanServerConnection();
                    agent.addListeners();
                }catch (IOException e){
                    e.printStackTrace();
                } catch (MalformedObjectNameException e) {
                    e.printStackTrace();
                } catch (InstanceNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else if (notification.getType().equals(JMXConnectionNotification.CLOSED)){
                System.out.println("Connection closed: " + agent.hostName + ":" + agent.port);
                agent.ad.disconnect(agent.hostName, agent.port);
            }
            else if (notification.getType().equals(JMXConnectionNotification.FAILED)){
                System.out.println("FAILED");
                agent.reconnect(RECONNECT_TIME);
            }
            else if (notification.getType().equals(JMXConnectionNotification.NOTIFS_LOST)){
                System.out.println("LOST");
            }
            else
                System.out.println("DON'T KNOW");
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
    public static long RECONNECT_TIME = 1000 * 60; //One minute
    private String gcPath;
    private String heapPath;
    private String heapName;
    private String hostName;
    private int port;

    //Resources
    private JMXServiceURL url;
    private JMXConnector jmxc;
    private MBeanServerConnection mbsc = null;
    private AgentListener listener;
    private AnomalyDetector ad;
    private ILogging log;
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

    private void connect() {
        //@TODO try again if process is down
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
            System.out.println("Connection failed");
        }

        if (!connected)
            reconnect(RECONNECT_TIME);
        System.out.println("Connected: " + connected);
    }

    private void reconnect(long delay){
        Timer timer = new Timer();
        timer.schedule(new ReconnectTask(), delay);
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
}
