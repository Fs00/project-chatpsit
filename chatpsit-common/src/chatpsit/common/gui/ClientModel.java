package chatpsit.common.gui;

import chatpsit.common.Message;
import chatpsit.common.ServerConstants;
import chatpsit.common.ServerMode;
import com.sun.javafx.application.PlatformImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ClientModel implements IModel
{
    private CopyOnWriteArrayList<IController> attachedControllers = new CopyOnWriteArrayList<>();

    protected Socket clientSocket;
    protected BufferedReader connectionReader;
    protected PrintWriter connectionWriter;

    /**
     * Manda il messaggio specificato al server e chiude la connessione se il messaggio inviato è marchiato come conclusivo
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    public void sendMessageToServer(Message request) throws IOException
    {
        if (clientSocket == null || clientSocket.isClosed())
            throw new UnsupportedOperationException("Non è stata stabilita una connessione con il server");

        connectionWriter.println(request.serialize());

        if (request.isLastMessage())
            clientSocket.close();
    }

    /**
     * Decodifica il messaggio ricevuto dal server e chiude la connessione se il server ha segnalato tale intenzione
     * @param receivedString risposta ricevuta dal server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    protected void handleServerMessage(String receivedString) throws IOException
    {
        if (receivedString == null)
        {
            clientSocket.close();
            throw new SocketException("Il server ha chiuso inaspettatamente la connessione.");
        }

        Message receivedMessage = Message.deserialize(receivedString);

        // Se non ci sono controller collegati a cui notificare il messaggio, attendine uno
        while (attachedControllers.isEmpty()) {}

        // Il callback di notifica ai controller viene eseguito sul thread principale di JavaFX.
        // L'esecuzione del callback viene attesa in quanto si potrebbero perdere dei messaggi subito dopo il login;
        // lo scollegamento del controller del login potrebbe avvenire dopo la ricezione di altri messaggi (da verificare effettivamente)
        attachedControllers.forEach(controller -> PlatformImpl.runAndWait(() -> controller.notifyMessage(receivedMessage)));

        if (receivedMessage.isLastMessage() && !clientSocket.isClosed())
            clientSocket.close();
    }

    /**
     * Avvia un thread in background che segnalerà ai controller connessi a questo model l'arrivo di nuovi messaggi dal server.
     * Quando il socket verrà chiuso, il thread si interromperà.
     */
    private void startMessageReceivingListener()
    {
        if (clientSocket.isClosed())
            throw new IllegalArgumentException("Impossibile avviare il listener ricezione messaggi se la connessione è chiusa.");

        new Thread(() -> {

            while (!clientSocket.isClosed())
            {
                try
                {
                    String rawReceivedMessage = connectionReader.readLine();
                    handleServerMessage(rawReceivedMessage);
                }
                catch (Exception exc)
                {
                    System.err.println("Eccezione rilevata nel listener ricezione messaggi: " + exc.getMessage());
                }
            }
            System.out.println("Listener ricezione messaggi fermato.");

        }).start();
    }

    public void connectToServer(ServerMode serverMode) throws IOException
    {
        clientSocket = new Socket(serverMode.getCorrespondingUrl(), ServerConstants.SERVER_PORT);
        connectionReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        connectionWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        startMessageReceivingListener();
    }

    public void attachController(IController controller)
    {
        attachedControllers.addIfAbsent(controller);
    }
    public void detachController(IController controller)
    {
        attachedControllers.remove(controller);
    }
    public void detachControllers()
    {
        attachedControllers.clear();
    }
}
