package chatpsit.client;

import chatpsit.common.Message;
import chatpsit.common.gui.BaseChatController;
import chatpsit.common.gui.ClientModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class PrivateChatController extends BaseChatController<ClientModel>
{
    @FXML
    private Button sendButton;
    @FXML
    private TextArea messageTextArea;

    private String user;
    private Message lastSentMessage;

    public PrivateChatController()
    {
        super(true);
    }

    @Override
    public void initialize()
    {
        super.initialize();
        // Listener per abilitare bottone Invia messaggio se il testo non è vuoto e non contiene spazi
        messageTextArea.textProperty().addListener(text -> {
            if (!messageTextArea.getText().trim().isEmpty())
                sendButton.setDisable(false);
            else
                sendButton.setDisable(true);
        });
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    @Override
    public void notifyMessage(Message message)
    {
        super.notifyMessage(message);
        switch (message.getType())
        {
            case NotifySuccess:
                chatList.getItems().add(lastSentMessage);
                break;
            case NotifyError:
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Messaggio non inviato");
                errorAlert.setHeaderText("Impossibile recapitare il messaggio");
                errorAlert.setContentText("Il destinatario non è connesso.");
                errorAlert.show();
                break;
            case UserDisconnected:
                messageTextArea.setText("");
                messageTextArea.setDisable(true);
                messageTextArea.setPromptText("Destinatario non connesso");
                break;
            case UserConnected:
                messageTextArea.setDisable(false);
                messageTextArea.setPromptText("");
                break;
        }
    }

    @FXML
    private void checkEmptyMessageText()
    {
        if (!messageTextArea.getText().trim().isEmpty())
            sendButton.setDisable(false);
        else
            sendButton.setDisable(true);
    }

    public Stage getStage()
    {
        return (Stage) sendButton.getScene().getWindow();
    }

    @Override
    public ClientModel getModel()
    {
        return ClientApp.getModel();
    }

    @FXML
    private void sendPrivateMessage()
    {
        try
        {
            Message privateMessage = Message.createNew(Message.Type.PrivateMessage)
                    .field(Message.Field.Sender, getModel().getLoggedInUsername())
                    .field(Message.Field.Recipient, user)
                    .field(Message.Field.Data, messageTextArea.getText())
                    .build();

            lastSentMessage = privateMessage;
            getModel().sendMessageToServer(privateMessage);
            messageTextArea.setText("");
        }
        catch (Exception e)
        {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Errore di connessione");
            errorAlert.setHeaderText("Impossibile mandare il messaggio al server");
            errorAlert.setContentText(e.getMessage());
            errorAlert.show();
        }
    }
}
