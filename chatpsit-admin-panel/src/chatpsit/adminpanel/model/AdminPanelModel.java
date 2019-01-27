package chatpsit.adminpanel.model;

import chatpsit.common.Message;
import chatpsit.common.gui.ClientModel;

import java.io.IOException;
import java.util.List;

public class AdminPanelModel extends ClientModel
{
    private String loggedInUsername;
    private List<String> connectedUsers;

    /**
     * Manda il messaggio specificato al server
     * @throws IOException Eventuali errori di connessione devono essere gestiti
     */
    public void sendMessageToServer(Message request) throws IOException
    {
        super.sendMessageToServer(request);

        if (request.getType() == Message.Type.AdminPanelLogin)
        {
            loggedInUsername = request.getField("username");
            handleServerResponse(connectionReader.readLine());
        }
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
