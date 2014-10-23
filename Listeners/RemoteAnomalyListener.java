package Listeners;

import AnomalyDetector.SocketListenerClientThread;

/**
 * Created by Oliver on 2014-10-23.
 */
public class RemoteAnomalyListener implements AnomalyListener {
    SocketListenerClientThread connection = null;

    public RemoteAnomalyListener(SocketListenerClientThread connection){
        this.connection = connection;
    }
    @Override
    public void anomalyFound(AnomalyEvent e) {
        connection.send(e.toString());
    }
}
