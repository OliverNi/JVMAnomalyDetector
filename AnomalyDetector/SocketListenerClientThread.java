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
    public SocketListenerClientThread(Socket clientSocket, SocketListenerClientThread[] threads)
    {
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
                if( line.contains("clear -") || line.contains("help"))
                {
                    //for the current connected user
                    synchronized (this)
                    {
                        for (int i = 0; i < nrOfUsers; i++)
                        {

                            if (threads[i] != null && threads[i] == this && threads[i].clientName != null)
                            {
                                //this.os.println("AnomalyReport notification for current connected user");

                                if(line.contains("clear -all"))
                                {
                                    //call for clearing of the database tables
                                    this.os.println("Input successful!");
                                }
                                else if(line.contains(":") && line.contains("-"))
                                {
                                    //call for clearing of specific process in database tables
                                    this.os.println("Input successful!");
                                }
                                else if(line.contains("help"))
                                {
                                    this.os.println("Input successful!");
                                    String availableCommands = "Examples use: \n";
                                    availableCommands += "COMMAND or ";
                                    availableCommands += "COMMAND -PARAMETER (Some commands require a parameter others do not have any parameters)\n \n";
                                    availableCommands += "clear"+ " (Clears database of all log entries (EXAMPLE: clear -all)) \n";
                                    availableCommands += "Paramers:\n -all \n" +
                                            "-HOST:PORT\n" +
                                            "-HOST:PORT, ...., HOST:PORT \n \n";
                                    availableCommands += "quit (Shuts down program (EXAMPLE: quit)) \n";
                                    this.os.println(availableCommands);
                                }

                                break;
                            }
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
                else
                {
                    this.os.println("Error! wrong input! input: help \n for valid commands!");
                }
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

}
