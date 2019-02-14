package chatpsit.server;

import chatpsit.common.Message;
import chatpsit.common.ServerConstants;
import chatpsit.common.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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

    public Server(boolean logOnFile) throws Exception
    {
        Logger.setServer(this);
        if (logOnFile)
            Logger.startLoggingOnFile();

        loadUserData();
    }

    @Override
    public void run()
    {
        try
        {
            // Apri socket del server
            serverSocket = new ServerSocket(ServerConstants.SERVER_PORT);

            // Elenca gli indirizzi IPv4 della macchina sulle sue interfacce di rete
            System.out.println("-- Indirizzi IP del server --");
            for (NetworkInterface netInterface : Collections.list(NetworkInterface.getNetworkInterfaces()))
            {
                if (!netInterface.isLoopback() && netInterface.isUp())
                {
                    System.out.print("   " + netInterface.getName() + ": ");
                    for (InetAddress address : Collections.list(netInterface.getInetAddresses()))
                    {
                        if (address instanceof Inet4Address)
                            System.out.print(address.getHostAddress() + " ");
                    }

                    System.out.println();
                }
            }

            Logger.logEvent(Logger.EventType.Info, "Server avviato sulla porta " + ServerConstants.SERVER_PORT);
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
        sendToAllClients(Message.createNew(Message.Type.ServerShutdown).lastMessage().build());

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

        try
        {
            // Attendi finché tutti i client non si sono disconnessi
            while (currentAdminPanelConnections.size() > 0 || currentUserClientConnections.size() > 0) {}
            serverSocket.close();
        }
        catch (IOException e) {
            Logger.logEvent(Logger.EventType.Error,"Errore nella chiusura del socket del server" + e.getMessage());
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
                                    .field(Message.Field.Action, "connection")
                                    .field(Message.Field.Data, "Tipo di messaggio inappropriato")
                                    .lastMessage()
                                    .build();
                connectionWriter.println(errMessage.serialize());
                clientSocket.close();
                return;
            }

            // Estrapola username e password dal messaggio e cerca se esiste un utente con lo stesso nome
            String username = message.getField(Message.Field.Username);
            String password = message.getField(Message.Field.Password);
            User existingUser = registeredUsers.stream().filter(user -> user.getUsername().equals(username)).findFirst().orElse(null);

            switch (message.getType())
            {
                case UserLogin:
                case AdminPanelLogin:
                    // Verifica correttezza username e password
                    if (existingUser == null || !existingUser.passwordMatches(password))
                    {
                        Message errMessage = Message.createNew(Message.Type.NotifyError)
                                            .field(Message.Field.Action, "login")
                                            .field(Message.Field.Data, ServerConstants.WRONG_CREDENTIALS_ERR)
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
                                            .field(Message.Field.Action, "login")
                                            .field(Message.Field.Data, ServerConstants.ONLY_ADMIN_CAN_ERR)
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
                                            .field(Message.Field.Action, "login")
                                            .field(Message.Field.Data, ServerConstants.USER_BANNED_ERR)
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
                                            .field(Message.Field.Action, "login")
                                            .field(Message.Field.Data, ServerConstants.ALREADY_LOGGED_IN_MSG_ERR)
                                            .lastMessage()
                                            .build();
                        connectionWriter.println(errMessage.serialize());
                        clientSocket.close();
                        return;
                    }
                    else
                    {
                        // Notifica all'utente che il login è avvenuto con successo
                        connectionWriter.println(Message.createNew(Message.Type.NotifySuccess)
                                .field(Message.Field.Action, "login")
                                .field(Message.Field.Data, "")
                                .build()
                                .serialize()
                        );

                        // Notifica a tutti che l'utente si è connesso se non è un admin panel
                        if (message.getType() != Message.Type.AdminPanelLogin)
                            this.sendToAllClients(Message.createNew(Message.Type.UserConnected)
                                    .field(Message.Field.Username, existingUser.getUsername())
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
                                        .field(Message.Field.Username, connectedUserName)
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
                                                           .field(Message.Field.Data, user.serialize())
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
                                            .field(Message.Field.Action, "register")
                                            .field(Message.Field.Data, ServerConstants.ALREADY_REGISTERED_ERR)
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
                                                .field(Message.Field.Action, "register")
                                                .field(Message.Field.Data, ServerConstants.WRONG_USERNAME_FORMAT_ERR)
                                                .lastMessage()
                                                .build();
                            connectionWriter.println(errMessage.serialize());
                        }

                        if (registrationSuccessful)
                        {
                            Message successMessage = Message.createNew(Message.Type.NotifySuccess)
                                                     .field(Message.Field.Action, "register")
                                                     .field(Message.Field.Data, "")
                                                     .lastMessage()
                                                     .build();

                            connectionWriter.println(successMessage.serialize());
                            Logger.logEvent(Logger.EventType.Info, "Nuovo utente registrato: " + username);

                            // Notifica gli admin panel che un nuovo utente si è registrato
                            sendToAdminPanelsOnly(Message.createNew(Message.Type.UserRegistered)
                                    .field(Message.Field.Username, username)
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
            // Notifica tutti gli utenti che l'utente è stato bannato.
            // La effettuo prima di disconnettere l'utente bannato in modo di evitare che il messaggio di notifica venga aggiunto alla
            // coda dell'utente bannato quando il suo thread di invio è già stato interrotto, ma lo UserConnectionHandler è ancora nella map
            sendToAllClients(Message.createNew(Message.Type.UserBanned)
                    .field(Message.Field.Username, userToBeBanned.getUsername())
                    .build()
            );

            if (bannedUserConnection != null)
            {
                // Invia un nuovo messaggio all'utente bannato con gli stessi campi del messaggio di ban ricevuto dall'admin panel
                // e il terminatore di connessione presente
                Message banAndKickMessage = Message.createNew(Message.Type.Ban)
                                            .field(Message.Field.BannedUser, userToBeBanned.getUsername())
                                            .field(Message.Field.Reason, reason)
                                            .lastMessage()
                                            .build();
                bannedUserConnection.sendMessage(banAndKickMessage);
            }
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
                    .field(Message.Field.Username, bannedUser.getUsername())
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
                            .filter(user -> user.getUsername().equals(message.getField(Message.Field.BannedUser)))
                            .findFirst().orElse(null);

        if (existingUser != null)
        {
            try
            {
                if (message.getType() == Message.Type.Ban)
                    banUser(existingUser, message.getField(Message.Field.Reason));
                else
                    unbanUser(existingUser);
            }
            catch (Exception exc)
            {
                Logger.logEvent(Logger.EventType.Error, "Errore durante il ban/unban dell'utente " +
                                message.getField(Message.Field.BannedUser) + ": " + exc.getMessage());
            }
        }
        else
            Logger.logEvent(Logger.EventType.Error, "Tentato ban/unban di un utente inesistente");
    }

    /**
     * Recapita il messaggio privato al destinatario specificato all'interno del messaggio.
     * L'esito della consegna viene notificato al client che lo ha inviato; nel campo dati del messaggio di notifica viene
     * inserito il nome del destinatario, in modo che il client possa capire a quale chat privata la notifica si riferisce
     * @param messageToDeliver messaggio di tipo PrivateMessage
     */
    public void deliverPrivateMessage(Message messageToDeliver, UserConnectionHandler sender)
    {
        UserConnectionHandler recipientConnection = currentUserClientConnections.getOrDefault(messageToDeliver.getField(Message.Field.Recipient), null);
        if (recipientConnection != null)
        {
            recipientConnection.sendMessage(messageToDeliver);
            Message successMessage = Message.createNew(Message.Type.NotifySuccess)
                                     .field(Message.Field.Action, "privateMsg")
                                     .field(Message.Field.Data, messageToDeliver.getField(Message.Field.Recipient))
                                     .build();
            sender.sendMessage(successMessage);

            Logger.logEvent(Logger.EventType.Info, messageToDeliver.getField(Message.Field.Sender) +
                            " ha inviato un messaggio privato a " + messageToDeliver.getField(Message.Field.Recipient));
        }
        else
        {
            Message errorMessage = Message.createNew(Message.Type.NotifyError)
                                    .field(Message.Field.Action, "privateMsg")
                                    .field(Message.Field.Data, messageToDeliver.getField(Message.Field.Recipient))
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
                    .field(Message.Field.Username, userConnection.getUser().getUsername())
                    .build()
            );
        }

        Logger.logEvent(Logger.EventType.Info, "È stato effettuato il logout dell'utente " + userConnection.getUser().getUsername() +
                        (userConnection.isAdminPanelConnection() ? " dal pannello di amministrazione" : ""));
    }


    /**
     * Carica i dati degli utenti da un file locale chiamato usersdata.txt; se il file non esiste, viene creato
     * Eventuali entry malformate o duplicate vengono ignorate
     * Vedi il metodo User.serialize() per informazioni sul formato di salvataggio
     * @throws Exception le eccezioni legate all'apertura del file devono far saltare l'avvio del server
     */
    private void loadUserData() throws Exception
    {
        Path usersDataFilePath = Paths.get(System.getProperty("user.dir"), "usersdata.txt");
        if (Files.notExists(usersDataFilePath))
        {
            try
            {
                Files.createFile(usersDataFilePath);
                Logger.logEvent(Logger.EventType.Info, "Dati degli utenti non trovati: creato file usersdata.txt vuoto");
            }
            catch (Exception exc)
            {
                Logger.logEvent(Logger.EventType.Error, "Impossibile creare il file per i dati degli utenti: " + exc.getMessage() +
                                "\nIl server non può avviarsi.");
                throw exc;
            }
        }
        else
        {
            try
            {
                int currentLine = 1;
                Scanner scanner = new Scanner(usersDataFilePath);
                while (scanner.hasNextLine())
                {
                    String userAsString = scanner.nextLine();
                    try
                    {
                        User deserializedUser = User.deserialize(userAsString);
                        if (registeredUsers.stream().anyMatch(user -> user.getUsername().equals(deserializedUser.getUsername())))
                            Logger.logEvent(Logger.EventType.Warning, "Utente duplicato " + deserializedUser.getUsername() +
                                            " alla riga " + currentLine + ", l'entry verrà ignorata");
                        else
                            registeredUsers.add(deserializedUser);
                    }
                    catch (Exception exc)
                    {
                        Logger.logEvent(Logger.EventType.Warning, "Dati dell'utente malformati alla riga " + currentLine +
                                        ", l'entry verrà ignorata");
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
}
