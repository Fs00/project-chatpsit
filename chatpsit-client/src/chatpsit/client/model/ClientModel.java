package chatpsit.client.model;

import chatpsit.common.Message;
import chatpsit.common.ServerConstants;
import chatpsit.common.ServerMode;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientModel implements IModel
{
    private List<IController> attachedControllers;

    private String serverUrl = ServerConstants.LOCAL_SERVER_ADDRESS;
    private Socket clientSocket;
    private BufferedReader connectionReader;
    private PrintWriter connectionWriter;

    private String loggedInUsername;
    private List<String> connectedUsers;
    private Map<String, List<String>> privateChatMessages;

    /**
     * Manda il messaggio specificato al server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    public void sendMessageToServer(Message request) throws IOException
    {
        if (clientSocket == null || clientSocket.isClosed())
            connectToServer();

        connectionWriter.println(request.serialize());

        if (request.getType() == Message.Type.UserLogin)
            loggedInUsername = request.getField("username");

        if (request.getType() == Message.Type.UserLogin ||
            request.getType() == Message.Type.Register ||
            request.getType() == Message.Type.PrivateMessage)
            handleServerResponse(connectionReader.readLine());

        if (request.isLastMessage() && !clientSocket.isClosed())
            clientSocket.close();
    }

    /**
     * Decodifica la risposta ricevuta dal server e aggiorna le variabili o
     * chiude la connessione in seguito ad essa
     * @param responseString risposta ricevuta dal server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    private void handleServerResponse(String responseString) throws IOException
    {
        if (responseString == null)
        {
            clientSocket.close();
            throw new SocketException("Il server ha chiuso inaspettatamente la connessione.");
        }

        Message response = Message.deserialize(responseString);
        attachedControllers.forEach(controller -> controller.notifyMessage(response));

        // TODO aggiornamento variabili

        if (response.isLastMessage())
            clientSocket.close();
    }

    private void connectToServer() throws IOException
    {
        clientSocket = new Socket(serverUrl, ServerConstants.SERVER_PORT);
        connectionReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        connectionWriter = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void startMessageReceivingListener()
    {
        // TODO
    }

    public void stopMessageReceivingListener()
    {
        // TODO
    }

    public void changeServerMode(ServerMode mode)
    {
        if (mode == ServerMode.Local)
            serverUrl = ServerConstants.LOCAL_SERVER_ADDRESS;
        else
            serverUrl = ServerConstants.REMOTE_SERVER_ADDRESS;
    }

    public ClientModel()
    {
        attachedControllers = new ArrayList<>(3);
    }
    public void attachController(IController controller)
    {
        attachedControllers.add(controller);
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
