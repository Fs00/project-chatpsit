package chatpsit.client;

import chatpsit.common.Message;
import chatpsit.common.gui.BaseChatController;
import chatpsit.common.gui.ClientModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class PrivateChatController extends BaseChatController<ClientModel>
{
    @FXML
    private Button sendButton;
    @FXML
    private TextArea messageTextArea;

    private String user;
    private int unreadMessages = 0;
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

        // Azzera i messaggi non letti quando la finestra viene messa in primo piano.
        // Questo event handler non viene registrato in initialize dato che quando FXMLLoader richiama tale metodo
        // la finestra non è ancora stata creata, mentre questo metodo viene richiamato successivamente
        getStage().focusedProperty().addListener((__, ___, isWindowFocused) -> {
            if (isWindowFocused)
                unreadMessages = 0;
        });
    }

    @Override
    public void notifyMessage(Message message)
    {
        super.notifyMessage(message);
        switch (message.getType())
        {
            case PrivateMessage:
                if (!getStage().isFocused())
                    unreadMessages++;
                break;
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

    @FXML
    private void sendMessageIfEnterPressed(KeyEvent event)
    {
        if (event.getCode() == KeyCode.ENTER)
        {
            if (!sendButton.isDisabled())
                sendPrivateMessage();

            event.consume();
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

    public int getUnreadMessagesCount()
    {
        return unreadMessages;
    }

    public Stage getStage()
    {
        if (user == null)
            throw new UnsupportedOperationException("È necessario prima impostare l'utente della chat privata.");

        return (Stage) sendButton.getScene().getWindow();
    }

    @Override
    public ClientModel getModel()
    {
        return ClientApp.getModel();
    }
}
