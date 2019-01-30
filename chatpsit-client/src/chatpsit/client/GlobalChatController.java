package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GlobalChatController implements IController
{
    private UserClientModel model;

    @FXML
    private BorderPane rootNode;

    public void initialize()
    {
        model = ClientApp.getModel();
    }

    @FXML
    private void quitToLogin()
    {
        // TODO logout
        ((Stage) rootNode.getScene().getWindow()).close();
        ClientApp.showStartupWindow();
    }

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
