package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.List;

public class PrivateChatController implements IController<UserClientModel>
{
    @FXML
    private Button sendButton;
    @FXML
    private TextArea textArea;

    private String user;

    private List<String> messageList;

    public void initialize()
    {

    }

    public void setUser(String user)
    {
        this.user = user;
        this.messageList = getModel().getPrivateMessagesListForUser(user);
    }

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
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Errore di comunicazione");
                errorAlert.setHeaderText("Impossibile mandare il messagio al server");
                errorAlert.setContentText("Il sistema non è riuscito ad inviare il messaggio inserito al server");
                errorAlert.show();
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
