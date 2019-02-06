package chatpsit.adminpanel.model;

import chatpsit.common.gui.ClientModel;

import java.io.IOException;
import java.util.List;

public class AdminPanelModel extends ClientModel
{
    private List<String> connectedUsers;

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
