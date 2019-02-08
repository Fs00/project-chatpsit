package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class PrivateChatController implements IController<UserClientModel>
{
    @FXML
    private Button sendButton;
    @FXML
    private TextArea textArea;

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case PrivateMessage:
                // TODO
                break;
            case NotifySuccess:
                // TODO
                break;
            case NotifyError:
                // TODO
                break;
        }
    }

    @FXML
    private void checkEmptyMessageText()
    {
        if (!textArea.getText().trim().isEmpty())
            sendButton.setDisable(false);
        else
            sendButton.setDisable(true);
    }

    public Stage getStage()
    {
        return (Stage) sendButton.getScene().getWindow();
    }

    @Override
    public UserClientModel getModel()
    {
        return ClientApp.getModel();
    }
}
