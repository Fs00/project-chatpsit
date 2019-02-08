package chatpsit.client.model;

import chatpsit.common.gui.ClientModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserClientModel extends ClientModel
{
    private Map<String, List<String>> privateChatMessages = new HashMap<>();

    public List<String> getPrivateMessagesListForUser(String username)
    {
        if (privateChatMessages.containsKey(username))
            return privateChatMessages.get(username);
        else
        {
            List<String> emptyMessagesList = new ArrayList<>();
            privateChatMessages.put(username, emptyMessagesList);
            return emptyMessagesList;
        }
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
