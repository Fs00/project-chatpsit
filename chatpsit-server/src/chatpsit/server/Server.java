package chatpsit.server;

import chatpsit.common.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class Server implements Runnable
{
    // Il server può essere eseguito in locale o su server remoto
    public enum Mode {
        Remote,
        Local
    }

    private ServerSocket serverSocket;
    private final int SERVER_PORT = 7777;

    private List<UserConnection> currentUserConnections;
    private List<User> registeredUsers;

    public Server(Server.Mode mode)
    {
        Logger.setMode(mode, this);
        loadUserData();
    }

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
        // deve rispondere ai messaggi di login e registrazione
        // se il login va a buon fine deve essere eseguito UserConnection.run() in un nuovo thread
        // per tutti gli altri messaggi il server risponderà al client con un errore
    }

    /**
     * Invia il messaggio specificato a tutti gli utenti amministratori
     * Viene utilizzato da Logger
     */
    public void sendToAdminsOnly(Message message)
    {
        // TODO
    }

    private void loadUserData()
    {
        /*
         deve effettuare il parsing dei dati da un file chiamato usersdata.txt
         posizionato nella stessa cartella dell'eseguibile.
         I dati degli utenti vanno salvati nella lista registeredUsers.
         Per la lettura da file è preferibile utilizzare la classe Scanner
         */
    }
}
