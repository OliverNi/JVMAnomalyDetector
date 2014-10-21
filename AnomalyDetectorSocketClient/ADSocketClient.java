/**
 * Created by Martin on 2014-10-21.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ADSocketClient implements Runnable
{
    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static PrintStream os = null;
    // The input stream
    private static DataInputStream is = null;

    private static BufferedReader inputLine = null;
    private static boolean closed = false;

    public static void main(String[] args)
    {
        // The default port.
        int portNumber = 0;
        // The default host.
        String host = "";

        try
        {
            System.out.println("AnomalyDetector command interface client started...");
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter hostname / ip address: ");
            host = inputLine.readLine();
            System.out.print("Enter port: ");

            portNumber  = Integer.parseInt(inputLine.readLine());

            //feeds the clientSocket with hostname(localhost in my case) and portnumber
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));

            //opens output stream that is used to send messages from the client to the server
            os = new PrintStream(clientSocket.getOutputStream());

            //opens input stream that is used to recieve messages from the server
            is = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e)
        {
            System.err.println("Unknown host: " + host);
            System.err.println("Program is exciting");
        } catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to the host "
                    + host);
        }catch(NumberFormatException nfe)
        {
            System.err.println("Invalid Format!");
        }

        //when everything is initialized data read/write will occur to the socket on which a connection with a specified port number has been opened
        if (clientSocket != null && os != null && is != null)
        {
            try
            {
                //creates a thread to read from the server
                new Thread(new ADSocketClient()).start();
                while (!closed)
                {
                    os.println(inputLine.readLine().trim());
                }

                //closes input/output stream and the socket
                os.close();
                is.close();
                clientSocket.close();
                System.exit(0);
            } catch (IOException e)
            {
                System.err.println("IOException:  " + e);
            }
        }
    }

    //creates thread to read from server
    @SuppressWarnings("deprecation")
    public void run()
    {
        //keeps reading from the socket until ">>> Good bye" is recieved, and then there is a disconnect
        String responseLine;
        try
        {
            while ((responseLine = is.readLine()) != null)
            {
                //prints the response from server
                System.out.println(responseLine);

                //if the input was successful
                if((responseLine.indexOf("Input successful!") != -1) )
                {
                    //recieves output from server, perhaps Anomaly Report notification?
                    String fetch = responseLine.toString();

                }

                if (responseLine.indexOf(">>> Good bye ") != -1)
                {
                    break;
                }

            }
            closed = true;
        } catch (IOException e)
        {
            System.err.println("IOException:  " + e);
        }
    }

}


