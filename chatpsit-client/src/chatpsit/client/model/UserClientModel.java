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
     * Manda il messaggio specificato al server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    public void sendMessageToServer(Message request) throws IOException
    {
        super.sendMessageToServer(request);

        if (request.getType() == Message.Type.UserLogin)
            loggedInUsername = request.getField("username");

        if (request.getType() == Message.Type.UserLogin ||
            request.getType() == Message.Type.Register ||
            request.getType() == Message.Type.PrivateMessage)
            handleServerResponse(connectionReader.readLine());
    }

    /**
     * Decodifica la risposta ricevuta dal server e aggiorna le variabili o
     * chiude la connessione in seguito ad essa
     * @param responseString risposta ricevuta dal server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    @Override
    protected void handleServerResponse(String responseString) throws IOException
    {
        super.handleServerResponse(responseString);
        // TODO aggiornamento variabili
    }
}
