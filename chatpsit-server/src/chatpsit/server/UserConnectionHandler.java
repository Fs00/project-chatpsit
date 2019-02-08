package chatpsit.server;

import chatpsit.common.Message;
import chatpsit.common.User;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Gestisce la connessione di un client, fornendo metodi per recapitare messaggi all'utente connesso e
 * notificando il server per ogni messaggio che il client manda.
 */
public class UserConnectionHandler
{
    private final User user;
    private final Server server;
    private Date lastActivity;
    private final boolean isAdminPanelConnection;

    private final Socket clientSocket;
    private final BufferedReader connectionReader;
    private final PrintWriter connectionWriter;

    private Thread messageSenderThread;
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    public UserConnectionHandler(Server server, User user, Socket clientSocket, BufferedReader connectionReader,
                                 PrintWriter connectionWriter, boolean isAdminPanelConnection)
    {
        this.server = server;
        this.user = user;
        this.clientSocket = clientSocket;
        this.connectionReader = connectionReader;
        this.connectionWriter = connectionWriter;
        this.isAdminPanelConnection = isAdminPanelConnection;
        this.lastActivity = new Date();
    }

    /**
     * Quando il gestore della connessione viene fatto partire, viene avviato soltanto il thread preposto
     * all'ascolto dei messaggi inviati dal client.
     * I messaggi destinati al client in coda vengono inviati solo dopo che esso ha recapitato al server un messaggio
     * Ready (pronto a ricevere messaggi).
     */
    public void start()
    {
        new Thread(this::listenForMessages).start();
    }

    /**
     * Rimane in ascolto di messaggi in arrivo da parte del client fino alla chiusura del socket.
     * Da eseguire su un thread parallelo.
     */
    private void listenForMessages()
    {
        try
        {
            String rawMessage;
            while ((rawMessage = connectionReader.readLine()) != null)
            {
                try
                {
                    lastActivity = new Date();
                    Message receivedMessage = Message.deserialize(rawMessage);

                    switch (receivedMessage.getType())
                    {
                        case Ready:
                            startMessageSenderThread();
                            break;
                        case PrivateMessage:
                            server.deliverPrivateMessage(receivedMessage, this);
                            break;
                        case GlobalMessage:
                            server.sendToAllClients(receivedMessage);
                            break;
                        case Report:
                            server.sendToAdminPanelsOnly(receivedMessage);
                            break;
                        case Ban:
                        case Unban:
                            if (isAdminPanelConnection)
                                server.performBanOrUnban(receivedMessage);
                            else
                                Logger.logEvent(Logger.EventType.Warning, "L'utente non admin " + user.getUsername() +
                                                " ha tentato il ban/unban dell'utente " + receivedMessage.getField(Message.Field.Username));
                            break;
                    }

                    if (receivedMessage.isLastMessage())
                        closeAndStopProcessing();
                }
                catch (Exception exc)
                {
                    Logger.logEvent(Logger.EventType.Error, "Errore nell'elaborazione del messaggio ricevuto " +
                                    "dall'utente " + user.getUsername() + ": " + exc.getMessage());
                }
            }
        }
        catch (Exception exc) {
            if (!exc.getMessage().equalsIgnoreCase("Socket closed"))
                Logger.logEvent(Logger.EventType.Error, "Errore fatale del thread di ricezione dei messaggi dall'utente " +
                            user.getUsername() + ": " + exc.getMessage());
        }
    }

    private void startMessageSenderThread()
    {
        messageSenderThread = new Thread(this::processMessageQueue);
        messageSenderThread.start();
    }

    /**
     * Elabora la coda dei messaggi in attesa di essere inviati al client.
     * Da eseguire su un thread parallelo.
     */
    private void processMessageQueue()
    {
        try
        {
            while (!clientSocket.isClosed())
            {
                Message messageToSend = messageQueue.take();
                connectionWriter.println(messageToSend.serialize());

                if (messageToSend.isLastMessage())
                    closeAndStopProcessing();
            }
        }
        catch (Exception exc)
        {
            if (!(exc instanceof InterruptedException))
                Logger.logEvent(Logger.EventType.Error, "Errore nella chiusura del socket dell'utente " +
                                user.getUsername() + ": " + exc.getMessage());
        }
    }

    /**
     * Chiude il socket, ferma i thread di elaborazione dei messaggi
     * e notifica il server della chiusura della connessione
     */
    public void closeAndStopProcessing()
    {
        try {
            clientSocket.close();
        }
        catch (Exception exc) {
            Logger.logEvent(Logger.EventType.Error, "Errore nella chiusura del socket per l'utente " + user +
                            ": " + exc.getMessage());
        }

        if (messageSenderThread != null && messageSenderThread.isAlive())
            messageSenderThread.interrupt();

        server.notifyClosedConnection(this);
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

    public boolean isAdminPanelConnection() {
        return isAdminPanelConnection;
    }
}
