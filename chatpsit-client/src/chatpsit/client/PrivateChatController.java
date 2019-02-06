package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class PrivateChatController implements IController<UserClientModel>
{
    @FXML
    private Button sendButton;
    @FXML
    private TextArea textArea;

    @Override
    public void notifyMessage(Message message) {

    }

    @FXML
    private void checkEmptyMessageText()
    {
        if (!textArea.getText().trim().isEmpty())
            sendButton.setDisable(false);
        else
            sendButton.setDisable(true);
    }

    @Override
    public UserClientModel getModel()
    {
        return ClientApp.getModel();
    }
}
