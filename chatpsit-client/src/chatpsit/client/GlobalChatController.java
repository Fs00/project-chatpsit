package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Optional;

public class GlobalChatController implements IController
{
    private UserClientModel model;

    @FXML
    private BorderPane rootNode;
    @FXML
    private Button sendButton;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private TableView<String> tableViewUsers;
    @FXML
    private Button sendPrivateButton;
    @FXML
    private Button reportButton;
    @FXML
    private ListView listView;
    @FXML
    private TableColumn<String, String> usersColumn;

    public void initialize()
    {
        model = ClientApp.getModel();
        bindToModel(model);

        sendReadyMessageToServer();

        // FIXME
        usersColumn.setCellValueFactory(item -> new SimpleStringProperty(item.toString()));

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

    /**
     * Segnala al server che è pronto a ricevere messaggi
     * Se l'invio del messaggio fallisce, l'utente è invitato a riprovare o a chiudere l'applicazione
     */
    private void sendReadyMessageToServer()
    {
        boolean messageSent = false;
        while (!messageSent)
        {
            try
            {
                Message readyMessage = Message.createNew(Message.Type.Ready).build();
                model.sendMessageToServer(readyMessage);
                messageSent = true;
            }
            catch (Exception exc)
            {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Errore di connessione");
                errorAlert.setHeaderText("Impossibile mandare il messaggio 'READY' al server");
                errorAlert.setContentText("Riprova o esci dall'applicazione.");

                ButtonType retry = new ButtonType("Riprova", ButtonBar.ButtonData.BACK_PREVIOUS);
                ButtonType quit = new ButtonType("Esci", ButtonBar.ButtonData.FINISH);
                errorAlert.getButtonTypes().setAll(retry, quit);

                Optional<ButtonType> choice = errorAlert.showAndWait();
                if (!choice.isPresent() || choice.get() == quit)
                    Platform.exit();
            }
        }
    }

    boolean sendLogout()
    {
        //TODO chiudere finestre chat private
        boolean logoutSuccessful = false;
        try
        {
            Message logoutMessage = Message.createNew(Message.Type.Logout)
                                    .lastMessage()
                                    .build();
            model.sendMessageToServer(logoutMessage);
            model.detachControllers();
            logoutSuccessful = true;
        }
        catch (Exception exc)
        {
            Alert errorDialog = new Alert(Alert.AlertType.ERROR);
            errorDialog.setTitle("Errore di connessione");
            errorDialog.setHeaderText("Logout fallito");
            errorDialog.setContentText(exc.getMessage());
            errorDialog.show();
        }
        return logoutSuccessful;
    }

    @FXML
    private void logoutAndCloseWindow()
    {
        boolean logoutSuccessful = sendLogout();
        if (logoutSuccessful)
            ((Stage) rootNode.getScene().getWindow()).close();
    }

    @FXML
    private void logoutAndQuitToLogin()
    {
        logoutAndCloseWindow();
        if (!rootNode.getScene().getWindow().isShowing())
            ClientApp.showStartupWindow();
    }

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case GlobalMessage:
                listView.getItems().add(message.getField("sender") + ": " + message.getField("message"));
                break;
            case UserConnected:
                tableViewUsers.getItems().add(message.getField("username"));
            case UserDisconnected:
                tableViewUsers.getItems().remove(message.getField("username"));
                break;
        }
    }

    @FXML
    private void sendGlobalMessage()
    {
        try
        {
            model.sendMessageToServer(Message.createNew(Message.Type.GlobalMessage)
                    .field("sender", model.getLoggedInUsername())
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
            errorAlert.setContentText("Riprova o esci dall'applicazione.");
        }
    }
}
