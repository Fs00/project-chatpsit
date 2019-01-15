package chatpsit.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server implements Runnable
{
    // Il server può essere eseguito in locale o su server remoto
    public enum Mode {
        Remote,
        Local
    }

    public Server(Server.Mode mode)
    {
        Logger.setMode(mode);
    }

    private ServerSocket serverSocket;
    private final int SERVER_PORT = 7777;

    @Override
    public void run()
    {
        try
        {
            serverSocket = new ServerSocket(SERVER_PORT);
            Logger.logEvent(Logger.EventType.Info, "Server avviato");
        }
        catch (Exception exc)
        {
            Logger.logEvent(Logger.EventType.Error, "Errore nell'apertura del socket del server: " + exc.getMessage());
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
                Logger.logEvent(Logger.EventType.Info, "Richiesta chiusura del server");
            }
            catch (IOException exc)
            {
                Logger.logEvent(Logger.EventType.Error, "Errore nel creare il socket per un client: " + exc.getMessage());
            }
        }
    }

    private void shutdownServer()
    {
        // TODO
    }

    /**
     * Decodifica il messaggio ricevuto dal nuovo client
     * @param clientSocket il socket del client
     */
    private void handleClientConnection(Socket clientSocket)
    {
        // TODO
    }
}
