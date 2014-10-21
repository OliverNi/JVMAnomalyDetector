package AnomalyDetector;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Martin on 2014-10-21.
 */
public class SocketListener implements Runnable
{
    //Receive commands - give callback
    //

    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;

    // Sets a limit on the number of connected users
    private static final int nrOfUsers = 1;
    //creates a SocketListenerClientThread with a max amount of 5 simultaneous users
    private static final SocketListenerClientThread[] threads = new SocketListenerClientThread[nrOfUsers];
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
                int i = 0;
                for (i = 0; i < nrOfUsers; i++)
                {
                    //if a thread is empty then it can be assigned a new connection
                    if (threads[i] == null)
                    {
                        (threads[i] = new SocketListenerClientThread(clientSocket, threads, ad)).start();
                        break;
                    }
                }
                //if more than 5 users connects then an error message is displayed
                if (i == nrOfUsers)
                {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Max number of users have been reached, please try again later");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e)
            {
                System.out.println(e);
            }

        }
    }

    public void send(String text){
        for (SocketListenerClientThread t : threads)
            t.send(text);
    }

    @Override
    public void run() {
        runListener();
    }
}

