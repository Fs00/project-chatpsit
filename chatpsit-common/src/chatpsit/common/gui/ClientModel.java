package chatpsit.common.gui;

import chatpsit.common.Message;
import chatpsit.common.ServerConstants;
import chatpsit.common.ServerMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ClientModel implements IModel
{
    private List<IController> attachedControllers = new CopyOnWriteArrayList<>();

    protected String serverUrl = ServerConstants.LOCAL_SERVER_ADDRESS;
    protected Socket clientSocket;
    protected BufferedReader connectionReader;
    protected PrintWriter connectionWriter;

    /**
     * Manda il messaggio specificato al server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    public void sendMessageToServer(Message request) throws IOException
    {
        if (clientSocket == null || clientSocket.isClosed())
            connectToServer();

        connectionWriter.println(request.serialize());

        if (request.isLastMessage())
            clientSocket.close();
    }

    /**
     * Decodifica la risposta ricevuta dal server ed eventualmente chiude la connessione in seguito ad essa
     * @param responseString risposta ricevuta dal server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    protected void handleServerResponse(String responseString) throws IOException
    {
        if (responseString == null)
        {
            clientSocket.close();
            throw new SocketException("Il server ha chiuso inaspettatamente la connessione.");
        }

        Message response = Message.deserialize(responseString);
        attachedControllers.forEach(controller -> controller.notifyMessage(response));

        if (response.isLastMessage() && !clientSocket.isClosed())
            clientSocket.close();
    }

    public void startMessageReceivingListener()
    {
        // TODO
    }

    public void stopMessageReceivingListener()
    {
        // TODO
    }

    private void connectToServer() throws IOException
    {
        clientSocket = new Socket(serverUrl, ServerConstants.SERVER_PORT);
        connectionReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        connectionWriter = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void changeServerMode(ServerMode mode)
    {
        if (mode == ServerMode.Local)
            serverUrl = ServerConstants.LOCAL_SERVER_ADDRESS;
        else
            serverUrl = ServerConstants.REMOTE_SERVER_ADDRESS;
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
