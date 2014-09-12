package AnomalyDetector;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created by Oliver on 2014-09-12.
 */
public class JMXAgent {
    /**
     * Class which handles the notifications
     */
    public static class AgentListener implements NotificationListener{
        public void handleNotification(Notification notification, Object handback){

        }
    }

    private String hostName;
    private int port;
    private double interval;
    JMXServiceURL url;
    JMXConnector jmxc;
    MBeanServerConnection mbsc;
    AgentListener listener;
    ArrayList<MemoryPoolMXBean> MXBeanProxy = new ArrayList<>();

    public JMXAgent(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.listener = new AgentListener();
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
    }

    private void createProxies() throws MalformedObjectNameException{
        MemoryPoolMXBean oldGenProxy= JMX.newMXBeanProxy(mbsc, new ObjectName("java.lang:type=MemoryPool,name=PS Old Gen"),
                MemoryPoolMXBean.class);
        MXBeanProxy.add(oldGenProxy);
    }

    /**
     * For testing purposes
     */
    public void gather(){
        System.out.println(hostName + ":" + port);
        for (MemoryPoolMXBean bean : MXBeanProxy){
            System.out.println(bean.getName() + " usage: " + (bean.getUsage().getUsed() / 1024) + " kb");
            System.out.println(bean.getName() + " peak usage: " + bean.getPeakUsage().getUsed() / 1024 + " kb");
            System.out.println(bean.getName() + " " + bean.getUsageThreshold() / 1024 + " kb");
            System.out.println(bean.getName() + " collection usage, used: " + bean.getCollectionUsage().getUsed() / 1024 + " kb");
        }
    }



}
