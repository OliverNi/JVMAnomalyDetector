package AnomalyDetector;

import Listeners.RemoteAnomalyListener;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Martin on 2014-10-21.
 */
public class SocketListener extends Thread
{
    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;

    // Sets a limit on the number of connected users
    private static final int nrOfUsers = 2;
    private static ArrayList<SocketListenerClientThread> threads = new ArrayList<>();
    // The default port number.
    private static final int portNumber = 27015;
    private AnomalyDetector ad;
    private boolean listening = true;
    public SocketListener(AnomalyDetector ad)
    {
        this.ad = ad;
    }

    public void runListener()
    {
        System.out.println("Socket command listener interface for AnomalyDetector Started! Listening for connections on port: " + portNumber);

        //opens a server socket on the supplied portNumber
        try
        {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e)
        {
            System.out.println(e);
        }

        //creates a client socket for each new connection and passes it to a client thread.
        while (listening)
        {
            try
            {
                clientSocket = serverSocket.accept();
                SocketListenerClientThread listenerThread = new SocketListenerClientThread(clientSocket, ad, this);
                threads.add(listenerThread);
                listenerThread.setRemoteAnomalyListener(new RemoteAnomalyListener(listenerThread));
                ad.addListener(listenerThread.getRemoteAnomalyListener());
                listenerThread.start();

            } catch (IOException e)
            {
                System.out.println(e);
            }

        }
        System.out.println("DEBUG");
    }

    public void send(String text){
        int count = 0;

        while (count < threads.size()){
            if (threads.get(count).isConnected())
                threads.get(count).send(text);
            else {
                removeListenerThread(threads.get(count));
                count--;
            }
            count++;
        }
    }

    public void removeListenerThread(SocketListenerClientThread t){
        t.interrupt();
        threads.remove(t);
        ad.removeListener(t.getRemoteAnomalyListener());
        System.out.println("Listener removed, nr of connected users: " + SocketListenerClientThread.getNrOfConnectedUsers());
    }

    public void cancel(){
        listening = false;
        while (threads.iterator().hasNext()){
            removeListenerThread(threads.iterator().next());
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        runListener();
    }
}

