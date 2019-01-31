package chatpsit.server;

import chatpsit.common.Message;
import chatpsit.common.User;

import java.io.BufferedReader;
import java.io.IOException;
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

    private Thread messageQueueConsumer, messageReceiver;
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

    public void start()
    {
        messageQueueConsumer = new Thread(this::processMessageQueue);
        messageQueueConsumer.start();

        // TODO thread per ricezione messaggi dal client
        messageReceiver = new Thread(this::listenForMessages);
        messageReceiver.start();
    }

    private void listenForMessages()
    {
        try
        {
            String rawMessage;
            while ((rawMessage = connectionReader.readLine()) != null)
            {
                Message messageDeserialized = Message.deserialize(rawMessage);

                switch (messageDeserialized.getType()) {
                    case PrivateMessage:
                        server.deliverPrivateMessage(messageDeserialized);
                        break;
                    case GlobalMessage:
                        server.sendToAllClients(messageDeserialized);
                        break;
                    case Report:
                        server.sendToAdminPanelsOnly(messageDeserialized);
                        break;
                    case Logout:
                        server.notifyClosedConnection(this);
                        break;
                    case Ban:
                        //TODO
                        break;
                }

                if (messageDeserialized.isLastMessage())
                    clientSocket.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
                    clientSocket.close();
            }
        }
        catch (Exception exc)
        {
            if (exc instanceof InterruptedException)
                Logger.logEvent(Logger.EventType.Info, "Thread di invio messaggi per l'utente " + user.getUsername() + " in chiusura.");
            else
                Logger.logEvent(Logger.EventType.Error, "Errore nella chiusura del socket dell'utente " +
                                user.getUsername() + ": " + exc.getMessage());
        }
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
