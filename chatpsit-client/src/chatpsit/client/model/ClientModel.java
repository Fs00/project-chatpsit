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

public class ClientModel implements IModel
{
    private List<IController> attachedControllers;

    private String serverUrl = ServerConstants.LOCAL_SERVER_ADDRESS;;
    private Socket clientSocket;
    private BufferedReader connectionReader;
    private PrintWriter connectionWriter;

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

    public void sendMessageToServer(Message request) throws IOException
    {
        if (clientSocket == null || clientSocket.isClosed())
            connectToServer();

        connectionWriter.println(request.serialize());

        if (request.getType() == Message.Type.UserLogin ||
            request.getType() == Message.Type.Register ||
            request.getType() == Message.Type.PrivateMessage)
            handleServerResponse(connectionReader.readLine(), request.getType());

        if (request.getType() == Message.Type.Logout)
            clientSocket.close();
    }

    private void handleServerResponse(String responseString, Message.Type requestType) throws IOException
    {
        if (responseString == null)
        {
            clientSocket.close();
            throw new SocketException("Il server ha chiuso inaspettatamente la connessione.");
        }

        Message response = Message.deserialize(responseString);
        attachedControllers.forEach(controller -> controller.notifyMessage(response));

        switch (requestType)
        {
            case UserLogin:
                if (response.getType() == Message.Type.NotifyError)
                    clientSocket.close();
                break;
            case Register:
                clientSocket.close();
                break;
        }
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

    private void connectToServer() throws IOException
    {
        clientSocket = new Socket(serverUrl, ServerConstants.SERVER_PORT);
        connectionReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        connectionWriter = new PrintWriter(clientSocket.getOutputStream(), true);
    }
}
