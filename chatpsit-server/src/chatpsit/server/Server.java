package chatpsit.server;

import chatpsit.common.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Server implements Runnable
{
    // Il server può essere eseguito in locale o su server remoto
    public enum Mode {
        Remote,
        Local
    }

    private ServerSocket serverSocket;
    private final int SERVER_PORT = 7777;

    private final Map<String, UserConnection> currentUserClientConnections = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, UserConnection> currentAdminPanelConnections = Collections.synchronizedMap(new HashMap<>());
    private final List<User> registeredUsers = Collections.synchronizedList(new ArrayList<>());

    public Server(Server.Mode mode) throws Exception
    {
        Logger.setMode(mode, this);
        //Logger.startLoggingOnFile();
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
        /*
         deve:
         - scrivere i dati degli utenti aggiornati sul file
         - mandare a tutti i client connessi un messaggio per segnalare la chiusura
         - chiudere tutte le connessioni
         - chiudere il file di log
         */
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
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String rawMessage = bufferedReader.readLine();
            Message message = Message.deserialize(rawMessage);

            if(message.getType() == Message.Type.UserLogin)
            {
                String username = message.getFields().get("username");
                String password = message.getFields().get("password");

                int i = 0;
                while(registeredUsers.get(i).getUsername().equals(username) && registeredUsers.get(i).getHashedPassword().equals(password)){

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Recapita il messaggio privato al destinatario specificato all'interno del messaggio
     * @param message messaggio di tipo PrivateMessage
     */
    public void deliverPrivateMessage(Message message)
    {
        // TODO
    }

    /**
     * Invia il messaggio a tutti i client connessi, sia sessioni utente che pannelli di amministrazione
     * @param message il messaggio da inviare
     */
    public void sendToAllClients(Message message)
    {
        // TODO
        sendToAdminPanelsOnly(message);
    }

    /**
     * Invia il messaggio specificato a tutte le sessioni attive del panello di amministrazione
     * Viene utilizzato da Logger
     */
    public void sendToAdminPanelsOnly(Message message)
    {
        synchronized (currentAdminPanelConnections)
        {
            for (UserConnection connection : currentAdminPanelConnections.values())
                connection.sendMessage(message);
        }
    }

    /**
     * Carica i dati degli utenti da un file locale chiamato usersdata.txt
     * Gli utenti nel file sono salvati uno per riga, nel seguente formato:
     *   [!@]username;hashedPassword
     * dove ! indica un utente bannato, mentre @ indica un utente admin
     * @throws Exception le eccezioni legate alla lettura del file devono far saltare l'avvio del server
     */
    private void loadUserData() throws Exception
    {
        File file = new File(System.getProperty("user.dir"), "usersdata.txt");
        try
        {
            int currentLine = 0;
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine())
            {
                String userAsString = scanner.nextLine();
                try
                {
                    User user = User.deserialize(userAsString);
                    registeredUsers.add(user);
                }
                catch (Exception exc)
                {
                    Logger.logEvent(Logger.EventType.Error, "Dati dell'utente malformati alla riga " + currentLine +
                                    ": " + exc.getMessage());
                }
                currentLine++;
            }
        }
        catch (Exception e)
        {
            Logger.logEvent(Logger.EventType.Error, "Impossibile leggere i dati degli utenti: " + e.getMessage() +
                            "\nIl server non può avviarsi.");
            throw e;
        }
    }
}
