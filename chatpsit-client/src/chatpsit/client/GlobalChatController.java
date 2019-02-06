package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.BaseGlobalChatController;
import chatpsit.common.gui.IMainWindowController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class GlobalChatController extends BaseGlobalChatController<UserClientModel> implements IMainWindowController<UserClientModel>
{
    @FXML
    private Button sendButton;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private Button sendPrivateButton;
    @FXML
    private Button reportButton;

    @Override
    public void initialize()
    {
        IMainWindowController.super.initialize();

        super.initialize();

        // Listener per dis/abilitare bottoni sotto la lista degli utenti connessi se un utente è selezionato
        tableViewUsers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                sendPrivateButton.setDisable(false);
                reportButton.setDisable(false);
            }
            else
             {
                sendPrivateButton.setDisable(true);
                reportButton.setDisable(true);
             }
        });

        // Listener per abilitare bottone Invia messaggio se il testo non è vuoto e non contiene spazi
        messageTextArea.textProperty().addListener(text -> {
            if (!messageTextArea.getText().trim().isEmpty())
                sendButton.setDisable(false);
            else
                sendButton.setDisable(true);
        });
    }

    @FXML
    private void sendGlobalMessage()
    {
        try
        {
            getModel().sendMessageToServer(Message.createNew(Message.Type.GlobalMessage)
                    .field("sender", getModel().getLoggedInUsername())
                    .field("message", messageTextArea.getText())
                    .build()
            );
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
                sendGlobalMessage();

            // Impedisce che si possa andare a capo riga nel testo del messaggio
            // (potenziale falla di sicurezza visto che ogni riga è un messaggio del protocollo
            //  e verrebbe interpretata come tale dal server)
            event.consume();
        }
    }

    @Override
    public boolean sendLogout()
    {
        boolean logoutSuccessful = IMainWindowController.super.sendLogout();
        //TODO chiudere finestre chat private
        return logoutSuccessful;
    }

    @Override
    public void notifyMessage(Message message)
    {
        if (message.getType() == Message.Type.Ban)
        {
            Alert banAlert = new Alert(Alert.AlertType.ERROR);
            banAlert.setTitle("Utente bannato");
            banAlert.setHeaderText("Sei stato bannato");
            banAlert.setContentText("Motivo: " + message.getField("reason"));
            banAlert.getButtonTypes().setAll(new ButtonType("Esci dall'applicazione"));
            banAlert.showAndWait();
            Platform.exit();
        }

        super.notifyMessage(message);
        // TODO eventuali altre azioni
    }

    @Override
    public Stage getCurrentWindow()
    {
        return (Stage) globalChatList.getScene().getWindow();
    }

    @Override
    public void showStartupWindow()
    {
        ClientApp.showStartupWindow();
    }

    @Override
    public UserClientModel getModel()
    {
        return ClientApp.getModel();
    }

    /*
       Fake overrides necessari siccome FXMLLoader non vede le default implementation
     */
    @FXML
    @Override
    public void logoutAndCloseWindow()
    {
        IMainWindowController.super.logoutAndCloseWindow();
    }
    @FXML
    @Override
    public void logoutAndQuitToLogin()
    {
        IMainWindowController.super.logoutAndQuitToLogin();
    }

    @FXML
    public void reportUser()
    {
        //tableViewUsers.getSelectionModel().getSelectedItem();
        ClientApp.showReportWindow();
    }
}
