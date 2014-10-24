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
public class SocketListener implements Runnable
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
        while (true)
        {
            try
            {
                clientSocket = serverSocket.accept();
                SocketListenerClientThread listenerThread = new SocketListenerClientThread(clientSocket, ad);
                threads.add(listenerThread);
                ad.addListener(new RemoteAnomalyListener(listenerThread));
                listenerThread.start();

            } catch (IOException e)
            {
                System.out.println(e);
            }

        }
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
        threads.remove(t);
    }


    @Override
    public void run() {
        runListener();
    }
}

