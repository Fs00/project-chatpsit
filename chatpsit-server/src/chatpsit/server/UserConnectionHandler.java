package chatpsit.server;

import chatpsit.common.Message;

import java.net.Socket;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Gestisce la connessione di un client, fornendo metodi per recapitare messaggi all'utente connesso e
 * notificando il server per ogni messaggio che il client manda.
 * Da eseguire su un thread parallelo.
 */
public class UserConnectionHandler implements Runnable
{
    private User user;
    private Socket clientSocket;
    private Server server;
    private Date lastActivity;
    private boolean isAdminPanelConnection;
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    public UserConnectionHandler(User user, Socket clientSocket, Server server, boolean isAdminPanelConnection) {
        this.user = user;
        this.clientSocket = clientSocket;
        this.server = server;
        this.isAdminPanelConnection = isAdminPanelConnection;
        this.lastActivity = new Date();
    }

    @Override
    public void run()
    {
        // TODO
    }

    /**
     * Accoda un messaggio per l'invio all'utente
     * @param message il messaggio da inviare
     */
    public void sendMessage(Message message)
    {
        try {
            messageQueue.put(message);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public User getUser()
    {
        return user;
    }

    public Date getLastActivity()
    {
        return lastActivity;
    }
}
