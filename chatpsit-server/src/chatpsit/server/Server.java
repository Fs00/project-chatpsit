package chatpsit.server;

import chatpsit.common.Message;
import chatpsit.common.ServerConstants;
import chatpsit.common.ServerMode;
import chatpsit.common.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable
{
    private ServerSocket serverSocket;
    private final Map<String, UserConnectionHandler> currentUserClientConnections = new ConcurrentHashMap<>();
    private final Map<String, UserConnectionHandler> currentAdminPanelConnections = new ConcurrentHashMap<>();
    private final List<User> registeredUsers = new CopyOnWriteArrayList<>();

    public Server(ServerMode mode) throws Exception
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
            serverSocket = new ServerSocket(ServerConstants.SERVER_PORT);
            Logger.logEvent(Logger.EventType.Info, "Server avviato");
        }
        catch (Exception exc)
        {
            Logger.logEvent(Logger.EventType.Error, "Errore nell'apertura del socket del server: " + exc.getMessage());
        }

        while (!serverSocket.isClosed())
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientConnection(clientSocket)).start();
            }
            catch (SocketException exc)
            {
                Logger.logEvent(Logger.EventType.Info, "Socket del server chiuso.");
            }
            catch (IOException exc)
            {
                Logger.logEvent(Logger.EventType.Error, "Errore nel creare il socket per un client: " + exc.getMessage());
            }
        }
    }

    /**
     * Effettua l'arresto del server
     */
    public void shutdownServer()
    {
        Logger.logEvent(Logger.EventType.Info, "Arresto del server in corso");

        try
        {
            StringBuilder fileContent = new StringBuilder();
            for (User user : registeredUsers)
                fileContent.append(user.serialize()).append("\n");
            Files.write(Paths.get(System.getProperty("user.dir"), "usersdata.txt"), fileContent.toString().getBytes());
        }
        catch (Exception exc)
        {
            Logger.logEvent(Logger.EventType.Error,"Errore nella scrittura del file dei dati degli utenti: " + exc.getMessage());
        }

        sendToAllClients(Message.createNew(Message.Type.ServerShutdown).lastMessage().build());

        try {
            serverSocket.close();
        } catch (IOException e) {
            Logger.logEvent(Logger.EventType.Error,"Errore nel chiudere il socket del server" + e.getMessage());
        }

        Logger.closeLogFile();
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
            PrintWriter connectionWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            String rawMessage = connectionReader.readLine();
            Logger.logEvent(Logger.EventType.Info, "L'host " + clientSocket.getInetAddress() + ":" +
                            clientSocket.getPort() + " ha instaurato una connessione con il messaggio " + rawMessage.substring(0,5));
            Message message = Message.deserialize(rawMessage);

            // Esci subito se il tipo di messaggio non è tra quelli adatti per cominciare una connessione
            if (message.getType() != Message.Type.AdminPanelLogin &&
                message.getType() != Message.Type.UserLogin &&
                message.getType() != Message.Type.Register)
            {
                Message errMessage = Message.createNew(Message.Type.NotifyError)
                                    .field("description", "Tipo di messaggio inappropriato")
                                    .lastMessage()
                                    .build();
                connectionWriter.println(errMessage.serialize());
                clientSocket.close();
                return;
            }

            // Estrapola username e password dal messaggio e cerca se esiste un utente con lo stesso nome
            String username = message.getField("username");
            String password = message.getField("password");
            User existingUser = registeredUsers.stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);

            switch (message.getType())
            {
                case UserLogin:
                case AdminPanelLogin:
                    // Verifica correttezza username e password
                    if (existingUser == null || !existingUser.passwordMatches(password))
                    {
                        Message errMessage = Message.createNew(Message.Type.NotifyError)
                                            .field("description", ServerConstants.WRONG_CREDENTIALS_ERR)
                                            .lastMessage()
                                            .build();
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }

                    // Impedisci il login al pannello di amministrazione se l'utente non è admin
                    if (message.getType() == Message.Type.AdminPanelLogin && !existingUser.isAdmin())
                    {
                        Message errMessage = Message.createNew(Message.Type.NotifyError)
                                            .field("description", ServerConstants.ONLY_ADMIN_CAN_ERR)
                                            .lastMessage()
                                            .build();
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }

                    // Impedisci accesso se l'utente è bannato
                    if (existingUser.isBanned())
                    {
                        Message errMessage = Message.createNew(Message.Type.NotifyError)
                                            .field("description", ServerConstants.USER_BANNED_ERR)
                                            .lastMessage()
                                            .build();
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }

                    // A seconda del tipo di login, seleziona la map sulla quale controllare se c'è già una connessione dello stesso utente
                    Map<String, UserConnectionHandler> connectionsMap;
                    if (message.getType() == Message.Type.UserLogin)
                        connectionsMap = currentUserClientConnections;
                    else
                        connectionsMap = currentAdminPanelConnections;

                    // Verifica se l'utente ha già una sessione attiva
                    if (connectionsMap.containsKey(existingUser.getUsername()))
                    {
                        Message errMessage = Message.createNew(Message.Type.NotifyError)
                                            .field("description", ServerConstants.ALREADY_LOGGED_IN_MSG_ERR)
                                            .lastMessage()
                                            .build();
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }
                    else
                    {
                        // Notifica all'utente che il login è avvenuto con successo
                        connectionWriter.println(Message.createNew(Message.Type.NotifySuccess).build().serialize());

                        // Notifica a tutti che l'utente si è connesso se non è un admin panel
                        if (message.getType() != Message.Type.AdminPanelLogin)
                            this.sendToAllClients(Message.createNew(Message.Type.UserConnected)
                                    .field("username", existingUser.getUsername())
                                    .build()
                            );

                        // Fai partire il gestore della connessione utente su un altro thread
                        UserConnectionHandler loggedInUserConnection = new UserConnectionHandler(this, existingUser, clientSocket,
                                connectionReader, connectionWriter, message.getType() == Message.Type.AdminPanelLogin);
                        loggedInUserConnection.start();
                        connectionsMap.put(username, loggedInUserConnection);
                        Logger.logEvent(Logger.EventType.Info, "Accesso effettuato dall'utente " + username);

                        // Inviagli tutti i nominativi degli utenti connessi quando il client segnalerà di essere pronto
                        for (UserConnectionHandler connection : currentUserClientConnections.values())
                        {
                            String connectedUserName = connection.getUser().getUsername();
                            // Se l'host è un admin panel, allora in lista può comparire il suo nome utente se è connesso con un client
                            if (loggedInUserConnection.isAdminPanelConnection() || !connectedUserName.equals(username))
                            {
                                loggedInUserConnection.sendMessage(Message.createNew(Message.Type.UserConnected)
                                        .field("username", connectedUserName)
                                        .build()
                                );
                            }
                        }

                        // Manda i dati di tutti gli utenti registrati se è un admin panel
                        if (loggedInUserConnection.isAdminPanelConnection())
                        {
                            for (User user : registeredUsers)
                            {
                                Message userDataMessage = Message.createNew(Message.Type.UserData)
                                                           .field("serializedData", user.serialize())
                                                           .build();
                                loggedInUserConnection.sendMessage(userDataMessage);
                            }
                        }
                    }
                    break;

                case Register:
                    if (existingUser != null)
                    {
                        Message errMessage = Message.createNew(Message.Type.NotifyError)
                                            .field("description", ServerConstants.ALREADY_REGISTERED_ERR)
                                            .lastMessage()
                                            .build();
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
                            Message errMessage = Message.createNew(Message.Type.NotifyError)
                                                .field("description", ServerConstants.WRONG_USERNAME_FORMAT_ERR)
                                                .lastMessage()
                                                .build();
                            connectionWriter.println(errMessage.serialize());
                        }

                        if (registrationSuccessful)
                        {
                            Message successMessage = Message.createNew(Message.Type.NotifySuccess).lastMessage().build();
                            connectionWriter.println(successMessage.serialize());
                            Logger.logEvent(Logger.EventType.Info, "Nuovo utente registrato: " + username);

                            // Notifica gli admin panel che un nuovo utente si è registrato
                            sendToAdminPanelsOnly(Message.createNew(Message.Type.UserRegistered)
                                    .field("username", username)
                                    .build()
                            );
                        }
                    }
                    clientSocket.close();
                    break;
            }
        }
        catch (Exception e)
        {
            Logger.logEvent(Logger.EventType.Error, "Errore durante la creazione della connessione con l'host " +
                            clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " " + e.getMessage());

            try {
                clientSocket.close();
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    /**
     * Effettua il ban dell'utente specificato qualora non lo fosse e notifica il ban a tutti i client collegati
     */
    private void banUser(User userToBeBanned, String reason)
    {
        if (!userToBeBanned.isBanned())
        {
            userToBeBanned.ban();

            UserConnectionHandler bannedUserConnection = currentUserClientConnections.getOrDefault(userToBeBanned.getUsername(), null);
            if (bannedUserConnection != null)
            {
                // Invia un nuovo messaggio all'utente bannato con gli stessi campi del messaggio di ban ricevuto dall'admin panel
                // e il terminatore di connessione presente
                Message banAndKickMessage = Message.createNew(Message.Type.Ban)
                                            .field("bannedUser", userToBeBanned.getUsername())
                                            .field("reason", reason)
                                            .lastMessage()
                                            .build();
                bannedUserConnection.sendMessage(banAndKickMessage);
            }

            // Notifica tutti gli utenti che l'utente è stato bannato
            sendToAllClients(Message.createNew(Message.Type.UserBanned)
                    .field("username", userToBeBanned.getUsername())
                    .build()
            );
        }
        else
            Logger.logEvent(Logger.EventType.Error, "Ban fallito: l'utente " + userToBeBanned.getUsername() + " è già bannato");
    }

    /**
     * Rimouve il ban per l'utente specificato qualora fosse bannato e notifica l'operazione a tutti i client collegati
     */
    private void unbanUser(User bannedUser)
    {
        if (bannedUser.isBanned())
        {
            bannedUser.unban();

            // Notifica tutti gli utenti che l'utente è stato bannato
            sendToAllClients(Message.createNew(Message.Type.UserUnbanned)
                    .field("username", bannedUser.getUsername())
                    .build()
            );
        }
        else
            Logger.logEvent(Logger.EventType.Error, "Rimozione del ban fallita: l'utente " + bannedUser.getUsername() + " non è bannato");
    }

    /**
     * A seconda del messaggio passatogli, effettua il ban o la rimozione del ban per l'utente specificato (se esiste)
     * @param message messaggio di tipo Ban o Unban
     */
    public void performBanOrUnban(Message message)
    {
        User existingUser = registeredUsers.stream()
                            .filter(user -> user.getUsername().equals(message.getField("bannedUser")))
                            .findFirst().orElse(null);

        if (existingUser != null)
        {
            try
            {
                if (message.getType() == Message.Type.Ban)
                    banUser(existingUser, message.getField("reason"));
                else
                    unbanUser(existingUser);
            }
            catch (Exception exc)
            {
                Logger.logEvent(Logger.EventType.Error, "Errore durante il ban/unban dell'utente " +
                                message.getField("bannedUser") + ": " + exc.getMessage());
            }
        }
        else
            Logger.logEvent(Logger.EventType.Error, "Tentato ban/unban di un utente inesistente");
    }

    /**
     * Recapita il messaggio privato al destinatario specificato all'interno del messaggio
     * @param messageToDeliver messaggio di tipo PrivateMessage
     */
    public void deliverPrivateMessage(Message messageToDeliver, UserConnectionHandler sender)
    {
        UserConnectionHandler recipientConnection = currentUserClientConnections.getOrDefault(messageToDeliver.getField("recipient"), null);
        if (recipientConnection != null)
        {
            recipientConnection.sendMessage(messageToDeliver);
            Message successMessage = Message.createNew(Message.Type.NotifySuccess).build();
            sender.sendMessage(successMessage);

            Logger.logEvent(Logger.EventType.Info, messageToDeliver.getField("sender") +
                            " ha inviato un messaggio privato a " + messageToDeliver.getField("recipient"));
        }
        else
        {
            Message errorMessage = Message.createNew(Message.Type.NotifyError)
                                    .field("description", "Destinatario non connesso")
                                    .build();
            sender.sendMessage(errorMessage);
        }
    }

    /**
     * Invia il messaggio a tutti i client connessi, sia sessioni utente che pannelli di amministrazione
     * @param message il messaggio da inviare
     */
    public void sendToAllClients(Message message)
    {
        for (UserConnectionHandler connection : currentUserClientConnections.values())
            connection.sendMessage(message);
        sendToAdminPanelsOnly(message);

        Logger.logEvent(Logger.EventType.Info, "Inviato messaggio di tipo " + message.getType() + " a tutti gli host connessi.");
    }

    /**
     * Invia il messaggio specificato a tutte le sessioni attive del panello di amministrazione
     * Viene utilizzato da Logger
     */
    public void sendToAdminPanelsOnly(Message message)
    {
        for (UserConnectionHandler connection : currentAdminPanelConnections.values())
            connection.sendMessage(message);
    }

    /**
     * Notifica il server che la connessione specificata è stata chiusa
     * @param userConnection l'istanza di UserConnectionHandler corrispondente
     */
    public void notifyClosedConnection(UserConnectionHandler userConnection)
    {
        if (userConnection.isAdminPanelConnection())
            currentAdminPanelConnections.remove(userConnection.getUser().getUsername());
        else
        {
            currentUserClientConnections.remove(userConnection.getUser().getUsername());

            // Notifica a tutti i client connessi che l'utente si è disconnesso
            this.sendToAllClients(Message.createNew(Message.Type.UserDisconnected)
                    .field("username", userConnection.getUser().getUsername())
                    .build()
            );
        }

        Logger.logEvent(Logger.EventType.Info, "L'utente " + userConnection.getUser().getUsername() +
                " ha effettuato il logout" + (userConnection.isAdminPanelConnection() ? " dal pannello di amministrazione" : ""));
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
