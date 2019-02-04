package chatpsit.client.model;

import chatpsit.common.Message;
import chatpsit.common.gui.ClientModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UserClientModel extends ClientModel
{
    private String loggedInUsername;
    private List<String> connectedUsers;
    private Map<String, List<String>> privateChatMessages;

    /**
     * Vedi metodo nella superclasse
     */
    public void sendMessageToServer(Message request) throws IOException
    {
        super.sendMessageToServer(request);

        if (request.getType() == Message.Type.UserLogin)
            loggedInUsername = request.getField("username");
    }

    /**
     * Vedi metodo nella superclasse
     */
    @Override
    protected void handleServerMessage(String receivedString) throws IOException
    {
        super.handleServerMessage(receivedString);
        // TODO aggiornamento variabili
    }
}
