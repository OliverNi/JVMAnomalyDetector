package AnomalyDetector;

import Listeners.RemoteAnomalyListener;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by Martin on 2014-10-21.
 */

/**
 * Handles one connection
 */
public class SocketListenerClientThread extends Thread
{
    private String clientName = null;
    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private SocketListener socketListener = null;
    private static int nrOfConnectedUsers;
    private RemoteAnomalyListener remoteAnomalyListener = null;
    private AnomalyDetector ad;
    public SocketListenerClientThread(Socket clientSocket, AnomalyDetector ad, SocketListener socketListener)
    {
        this.ad = ad;
        this.clientSocket = clientSocket;
        this.socketListener = socketListener;
    }

    @SuppressWarnings("deprecation")
    public void run()
    {
        try
        {
            //creates input and output streams for the current connected users
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            String name;
            while (true)
            {
                os.println("Enter your username.");
                name = is.readLine().trim();

                if(name.length() != 0)
                {
                    break;
                }
            }

            //information about a new user that has connected
            os.println("Welcome " + name
                    + " to the AnomalyDetector Command interface!. \n For available commands, please input: help \nTo quit the command interface, please enter: quit");
            nrOfConnectedUsers++;

            //inputs connected user name into a name variable for the specific thread that has been assigned to him/her
            synchronized (this)
            {
                clientName = name;
            }

            //starts Command interface listener
            while (!clientSocket.isClosed())
            {
                String line = is.readLine();
                if (line.startsWith("quit"))
                {
                    break;
                }

                //checks if the current input has been used
                //for the current connected user
                synchronized (this)
                {
                    if (!line.startsWith("browse") && !line.startsWith("shutdown"))
                        this.os.println(ad.command(line));
                    else
                        this.os.println("command does not work via socket connection.");
                }
            }
            nrOfConnectedUsers--;
            socketListener.removeListenerThread(this);
            os.println(">>> Good bye " + name + " >>>");
            //sets the current thread to null once the user is disconnected so another user can connect

            //closes the output, input stream and the socket
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e)
        {
            nrOfConnectedUsers--;
            socketListener.removeListenerThread(this);
        }
    }

    public void send(String text){
        this.os.println(text);
    }

    public boolean isConnected(){
        return clientSocket.isConnected();
    }

    public RemoteAnomalyListener getRemoteAnomalyListener() {
        return remoteAnomalyListener;
    }

    public void setRemoteAnomalyListener(RemoteAnomalyListener remoteAnomalyListener) {
        this.remoteAnomalyListener = remoteAnomalyListener;
    }

    public static int getNrOfConnectedUsers(){
        return nrOfConnectedUsers;
    }

    public void cancel(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
