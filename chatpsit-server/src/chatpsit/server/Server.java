package chatpsit.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server implements Runnable
{
    public enum Mode {
        Remote,
        Local
    }

    public Server(Server.Mode mode)
    {
        Logger.initialize(mode);
    }

    private ServerSocket serverSocket;
    private final int SERVER_PORT = 7777;

    @Override
    public void run()
    {
        try
        {
            serverSocket = new ServerSocket(SERVER_PORT);
            Logger.logEvent(LogEventType.Info, "Server started");
        }
        catch (Exception exc)
        {
            Logger.logEvent(LogEventType.Error, "Errore nell'apertura del socket del server: " + exc.getMessage());
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
                Logger.logEvent(LogEventType.Info, "Richiesta chiusura del server");
            }
            catch (IOException exc)
            {
                Logger.logEvent(LogEventType.Error, "Errore nel creare il socket per un client: " + exc.getMessage());
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
