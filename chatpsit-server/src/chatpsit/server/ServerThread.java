package chatpsit.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerThread extends Thread
{
    private ServerSocket serverSocket;
    private final int SERVER_PORT = 7777;

    @Override
    public void run()
    {
        try
        {
            serverSocket = new ServerSocket(SERVER_PORT);
        }
        catch (Exception exc)
        {
            System.out.println("Errore nell'apertura del socket del server: " + exc.getMessage()); // TODO: replace with generic log function
        }

        while (true)
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientConnection(clientSocket)).start();
            }
            catch (SocketException exc)
            {
                System.out.println("Richiesta chiusura del server");  // TODO: replace with generic log function
            }
            catch (IOException exc)
            {
                System.out.println("Errore nel creare il socket per un client: " + exc.getMessage()); // TODO: replace with generic log function
            }
        }
    }

    private void shutdownServer()
    {
        // TODO
    }

    private void handleClientConnection(Socket clientSocket)
    {
        // TODO
    }
}
