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
    private TextArea textArea;

    private String user;
    private String lastSentMessage;

    public PrivateChatController()
    {
        super(true);
    }

    @Override
    public void initialize()
    {
        super.initialize();
        // Listener per abilitare bottone Invia messaggio se il testo non è vuoto e non contiene spazi
        textArea.textProperty().addListener(text -> {
            if (!textArea.getText().trim().isEmpty())
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

                break;
            case NotifyError:
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Messaggio non inviato");
                errorAlert.setHeaderText("Impossibile recapitare il messaggio");
                errorAlert.setContentText("Il destinatario non è connesso.");
                errorAlert.show();
                break;
            case UserDisconnected:
                textArea.setText("");
                textArea.setDisable(true);
                textArea.setPromptText("Destinatario non connesso");
                break;
            case UserConnected:
                textArea.setDisable(false);
                textArea.setPromptText("");
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
    public ClientModel getModel()
    {
        return ClientApp.getModel();
    }

    @FXML
    private void sendPrivateMessage()
    {
        try
        {
            getModel().sendMessageToServer(Message.createNew(Message.Type.PrivateMessage)
                    .field(Message.Field.Sender, getModel().getLoggedInUsername())
                    .field(Message.Field.Recipient, user)
                    .field(Message.Field.Data, textArea.getText())
                    .build()
            );
            lastSentMessage = textArea.getText();
            textArea.setText("");
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
