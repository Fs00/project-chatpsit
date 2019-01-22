package chatpsit.server;

import chatpsit.common.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable
{
    // Il server può essere eseguito in locale o su server remoto
    public enum Mode {
        Remote,
        Local
    }

    private ServerSocket serverSocket;
    private final int SERVER_PORT = 7777;

    private final List<UserConnection> currentUserConnections = new ArrayList<>();
    private final List<User> registeredUsers = new ArrayList<>();

    public Server(Server.Mode mode) throws Exception
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
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String rawMessage = bufferedReader.readLine();
            Message message = Message.deserialize(rawMessage);

            if(message.getType() == Message.Type.Login)
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
     * Invia il messaggio specificato a tutti gli utenti amministratori
     * Viene utilizzato da Logger
     */
    public void sendToAdminsOnly(Message message)
    {
        for (UserConnection connection : currentUserConnections)
        {
            if (connection.getUser().isAdmin())
                connection.sendMessage(message);
        }
    }

    /**
     * Carica i dati degli utenti da un file locale chiamato usersdata.txt
     * Gli utenti nel file sono salvati uno per riga, nel seguente formato:
     *   [!@]username;hashedPassword
     * dove ! indica un utente bannato, mentre @ indica un utente admin
     * @throws Exception le eccezioni lanciate da questo metodo devono far saltare l'avvio del server
     */
    private void loadUserData() throws Exception
    {
        File file = new File(System.getProperty("user.dir"), "usersdata.txt");
        try
        {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine())
            {
                String[] splittedLine = scanner.nextLine().split(";");
                boolean isAdmin = splittedLine[0].startsWith("@");
                boolean isBanned = splittedLine[0].startsWith("!");

                if (isAdmin || isBanned)
                    splittedLine[0] = splittedLine[0].substring(1);

                User user = new User(splittedLine[0], splittedLine[1], isAdmin, isBanned);
                registeredUsers.add(user);
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
