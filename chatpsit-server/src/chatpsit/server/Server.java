package chatpsit.server;

import chatpsit.common.Message;
import chatpsit.common.ServerConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable
{
    // Il server può essere eseguito in locale o su server remoto
    public enum Mode {
        Remote,
        Local
    }

    private ServerSocket serverSocket;
    private final int SERVER_PORT = 7777;

    private final Map<String, UserConnection> currentUserClientConnections = new ConcurrentHashMap<>();
    private final Map<String, UserConnection> currentAdminPanelConnections = new ConcurrentHashMap<>();
    private final List<User> registeredUsers = new CopyOnWriteArrayList<>();

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
        try
        {
            BufferedReader connectionReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter connectionWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String rawMessage = connectionReader.readLine();
            Message message = Message.deserialize(rawMessage);

            // Esci subito se il tipo di messaggio non è tra quelli adatti per cominciare una connessione
            if (message.getType() != Message.Type.AdminPanelLogin &&
                message.getType() != Message.Type.UserLogin &&
                message.getType() != Message.Type.Register)
            {
                Message errMessage = new Message(Message.Type.NotifyError, Message.createFieldsMap("description", "Tipo di messaggio inappropriato"));
                connectionWriter.println(errMessage.serialize());
                clientSocket.close();
                return;
            }

            // Estrapola username e password dal messaggio e cerca se esiste un utente con lo stesso nome
            String username = message.getFields().get("username");
            String password = message.getFields().get("password");
            User existingUser = registeredUsers.stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);

            switch (message.getType())
            {
                case UserLogin:
                case AdminPanelLogin:
                    if (existingUser == null || !existingUser.passwordMatches(password))
                    {
                        Message errMessage = new Message(Message.Type.NotifyError, Message.createFieldsMap("description", ServerConstants.WRONG_CREDENTIALS_ERR));
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }

                    if (message.getType() == Message.Type.AdminPanelLogin && !existingUser.isAdmin())
                    {
                        Message errMessage = new Message(Message.Type.NotifyError, Message.createFieldsMap("description", ServerConstants.ONLY_ADMIN_CAN_ERR));
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }

                    // A seconda del tipo di login, seleziona la map sulla quale controllare se c'è già una connessione dello stesso utente
                    Map<String, UserConnection> connectionsMap;
                    if (message.getType() == Message.Type.UserLogin)
                        connectionsMap = currentUserClientConnections;
                    else
                        connectionsMap = currentAdminPanelConnections;

                    if (connectionsMap.containsKey(existingUser.getUsername()))
                    {
                        Message errMessage = new Message(Message.Type.NotifyError, Message.createFieldsMap("description", ServerConstants.ALREADY_LOGGED_IN_MSG_ERR));
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }
                    else
                    {
                        // Notifica a tutti che l'utente si è connesso
                        this.sendToAllClients(new Message(Message.Type.UserConnected, Message.createFieldsMap("username", existingUser.getUsername())));

                        // Fai partire il gestore della connessione utente su un altro thread
                        UserConnection loggedInUserConn = new UserConnection(existingUser, clientSocket,
                                                   this, message.getType() == Message.Type.AdminPanelLogin);
                        new Thread(loggedInUserConn).start();
                        connectionsMap.put(username, loggedInUserConn);
                        Logger.logEvent(Logger.EventType.Info, "Accesso effettuato dall'utente " + username);

                        // Notifica all'utente che il login è avvenuto con successo e inviagli tutti i nominativi degli utenti connessi
                        loggedInUserConn.sendMessage(new Message(Message.Type.NotifySuccess, Message.createFieldsMap("description", "")));
                        for (UserConnection connection : currentUserClientConnections.values())
                        {
                            String connectedUserName = connection.getUser().getUsername();
                            if (!connectedUserName.equals(username))
                                loggedInUserConn.sendMessage(new Message(Message.Type.UserConnected, Message.createFieldsMap("username", connectedUserName)));
                        }
                    }
                    break;

                case Register:
                    if (existingUser != null)
                    {
                        Message errMessage = new Message(Message.Type.NotifyError, Message.createFieldsMap("description", ServerConstants.ALREADY_REGISTERED_ERR));
                        connectionWriter.println(errMessage.serialize());
                    }
                    else
                    {
                        boolean registrationSuccessful = false;
                        try
                        {
                            User newUser = new User(username, User.hashPassword(password), false, false);
                            registeredUsers.add(newUser);
                            registrationSuccessful = true;
                        }
                        catch (Exception e)
                        {
                            Message errMessage = new Message(Message.Type.NotifyError, Message.createFieldsMap("description", ServerConstants.WRONG_USERNAME_FORMAT_ERR));
                            connectionWriter.println(errMessage.serialize());
                        }

                        if (registrationSuccessful)
                        {
                            Message successMessage = new Message(Message.Type.NotifySuccess, Message.createFieldsMap("description", ""));
                            connectionWriter.println(successMessage.serialize());
                            Logger.logEvent(Logger.EventType.Info, "Nuovo utente registrato: " + username);
                        }
                    }
                    clientSocket.close();
                    break;
            }
        }
        catch (Exception e) {
            Logger.logEvent(Logger.EventType.Error, "Errore durante la creazione della connessione con l'host " +
                            clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " " + e.getMessage());
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
        for (UserConnection connection : currentAdminPanelConnections.values())
            connection.sendMessage(message);
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
            int currentLine = 1;
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
