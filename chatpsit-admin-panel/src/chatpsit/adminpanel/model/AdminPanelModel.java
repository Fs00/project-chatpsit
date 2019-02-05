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
     * Vedi metodo nella superclasse
     */
    public void sendMessageToServer(Message request) throws Exception
    {
        super.sendMessageToServer(request);

        if (request.getType() == Message.Type.AdminPanelLogin)
            loggedInUsername = request.getField("username");
    }

    /**
     * Vedi metodo nella superclasse
     */
    @Override
    protected void handleServerMessage(String responseString) throws IOException
    {
        super.handleServerMessage(responseString);
        // TODO aggiornamento variabili
    }
}
