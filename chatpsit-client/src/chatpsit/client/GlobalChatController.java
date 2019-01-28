package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;

public class GlobalChatController implements IController
{
    private UserClientModel model;

    public void initialize()
    {
        model = ClientApp.getModel();
    }

    @FXML
    private void quitToLogin()
    {
        // TODO logout
        ClientApp.showStartupStage();
    }

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
