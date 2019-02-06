package chatpsit.client.model;

import chatpsit.common.Message;
import chatpsit.common.gui.ClientModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UserClientModel extends ClientModel
{
    private Map<String, List<String>> privateChatMessages;

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
