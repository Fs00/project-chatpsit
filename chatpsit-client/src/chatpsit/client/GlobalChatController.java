package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.BaseGlobalChatController;
import chatpsit.common.gui.IMainWindowController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private Map<String, PrivateChatController> privateChatWindows = new HashMap<>();

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
        // Chiude le finestre delle chat private
        if (logoutSuccessful)
        {
            for (PrivateChatController controller : privateChatWindows.values())
                controller.getStage().close();
        }
        return logoutSuccessful;
    }

    @FXML
    public void reportUser()
    {
        String selectedUsername = tableViewUsers.getSelectionModel().getSelectedItem();

        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Segnala l'utente " + selectedUsername);
        inputDialog.setGraphic(null);
        inputDialog.setHeaderText("Inserisci il motivo della segnalazione:");

        Optional<String> reportReason = inputDialog.showAndWait();
        if (reportReason.isPresent())
        {
            if (reportReason.get().isEmpty())
            {
                Alert errAlert = new Alert(Alert.AlertType.ERROR);
                errAlert.setHeaderText(null);
                errAlert.setContentText("Non è stata inserita una motivazione.");
                errAlert.show();
            }
            else
            {
                try
                {
                    getModel().sendMessageToServer(Message.createNew(Message.Type.Report)
                            .field("sender", getModel().getLoggedInUsername())
                            .field("reportedUser", selectedUsername)
                            .field("reason", reportReason.get())
                            .build()
                    );
                }
                catch (Exception e)
                {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Errore di connessione");
                    errorAlert.setHeaderText("Impossibile mandare il report al server");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.show();
                }
            }
        }
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
        // Notifica le finestre delle chat private del nuovo messaggio
        privateChatWindows.values().forEach(controller -> Platform.runLater(() -> controller.notifyMessage(message)));
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
    @Override
    public void showInfoDialog()
    {
        IMainWindowController.super.showInfoDialog();
    }

    @FXML
    public void showPrivateChat()
    {
        String selectedUsername = tableViewUsers.getSelectionModel().getSelectedItem();
        if (privateChatWindows.containsKey(selectedUsername))
            privateChatWindows.get(selectedUsername).getStage().requestFocus();
        else
        {
            FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("views/privateChat.fxml"));
            try
            {
                Stage privateChatStage = new Stage();
                privateChatStage.setTitle("Chat privata");
                privateChatStage.setResizable(false);
                privateChatStage.setScene(new Scene(loader.load()));

                privateChatStage.show();
                PrivateChatController privateChatController = loader.getController();
                privateChatController.setUser(selectedUsername);
                privateChatWindows.put(selectedUsername, privateChatController);
            }
            catch (Exception exc)
            {
                System.err.println("FATAL: Error when loading private chat window: " + exc.getMessage());
                Platform.exit();
            }

        }
    }
}
