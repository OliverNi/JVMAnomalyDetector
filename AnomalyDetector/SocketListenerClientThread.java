package AnomalyDetector;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by Martin on 2014-10-21.
 */
class SocketListenerClientThread extends Thread
{
    private String clientName = null;
    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private final SocketListenerClientThread[] threads;
    private int nrOfUsers;
    private static int nrOfConnectedUsers;
    private AnomalyDetector ad;
    public SocketListenerClientThread(Socket clientSocket, SocketListenerClientThread[] threads, AnomalyDetector ad)
    {
        this.ad = ad;
        this.clientSocket = clientSocket;
        this.threads = threads;
        nrOfUsers = threads.length;
    }

    @SuppressWarnings("deprecation")
    public void run()
    {
        int nrOfUsers = this.nrOfUsers;
        SocketListenerClientThread[] threads = this.threads;

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
                for (int i = 0; i < nrOfUsers; i++)
                {
                    if (threads[i] != null && threads[i] == this)
                    {
                        clientName = name;
                        break;
                    }
                }

            }

            //starts Command interface listener
            while (true)
            {
                String line = is.readLine();
                if (line.startsWith("quit"))
                {
                    break;
                }

                //checks if the current input has been used
                //@TODO add further set commands for excessive GC scan /  mem leak threshhold / etc in this IF statement
                //for the current connected user
                synchronized (this)
                {
                    for (int i = 0; i < nrOfUsers; i++)
                    {
                        this.os.println(ad.command(line));
                    }
                }

                //probably not needed
                //for any other connected users - maybe useful if AnomalyReports shall be sent to all connected users.
//                    synchronized (this)
//                    {
//
//                        for (int i = 0; i < nrOfUsers; i++)
//                        {
//                            if (threads[i] != null && threads[i] != this && threads[i].clientName != null )
//                            {
//                               // threads[i].os.println("AnomalyReport notification");
//                            }
//                        }
               // }
            }
            nrOfConnectedUsers--;

            os.println(">>> Good bye " + name + " >>>");
            //sets the current thread to null once the user is disconnected so another user can connect
            synchronized (this)
            {
                for (int i = 0; i < nrOfUsers; i++)
                {
                    if (threads[i] == this)
                    {
                        threads[i] = null;
                    }
                }
            }

            //closes the output, input stream and the socket
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e)
        {
        }
    }

    public void send(String text){
        this.os.println(text);
    }

}
