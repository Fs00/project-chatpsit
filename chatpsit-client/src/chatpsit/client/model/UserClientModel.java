package chatpsit.client.model;

import chatpsit.common.gui.BaseClientModel;

import java.io.IOException;

public class UserClientModel extends BaseClientModel
{
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
