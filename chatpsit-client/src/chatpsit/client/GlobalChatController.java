package chatpsit.client;

import chatpsit.common.Message;
import chatpsit.common.gui.BaseGlobalChatController;
import chatpsit.common.gui.ClientModel;
import chatpsit.common.gui.IMainWindowController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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

public class GlobalChatController extends BaseGlobalChatController<ClientModel> implements IMainWindowController<ClientModel>
{
    @FXML
    private Button sendButton;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private Button sendPrivateButton;
    @FXML
    private Button reportButton;

    @FXML
    private TableView<String> privateChatsTable;
    @FXML
    private TableColumn<String, String> privateChatColumn;


    /**
     * Contiene i controller relativi alle finestre delle chat private.
     * I controller vengono creati quando l'utente apre la prima volta la finestra di chat privata con un altro utente o
     * quando riceve un messaggio privato da quell'utente per la prima volta (nel secondo caso la finestra rimane
     * nascosta). Vengono rimossi soltanto al logout/chiusura dell'applicazione.
     */
    private Map<String, PrivateChatController> privateChatControllers = new HashMap<>();

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

        // Mostra il numero di messaggi non letti nella colonna delle chat private, qualora ce ne fossero
        privateChatColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue()));
        privateChatColumn.setCellFactory(column -> {
            return new TableCell<String, String>() {
                @Override
                protected void updateItem(String username, boolean empty) {
                    super.updateItem(username, empty);

                    if (username == null || empty) {
                        setText(null);
                        setStyle("");
                    }
                    else
                    {
                        // Event handler per cambiare il colore di sfondo della cella quando si muove il mouse sopra
                        // FIXME: rimuovono il grassetto in quanto sovrascrivono le regole CSS
                        setOnMouseEntered(event -> setStyle("-fx-background-color: #e0e0e0;"));
                        setOnMouseExited(event -> setStyle("-fx-background-color: white;"));

                        int unreadMessagesCount = privateChatControllers.get(username).getUnreadMessagesCount();
                        if (unreadMessagesCount == 0)
                        {
                            setText(username);
                            setStyle("");
                        }
                        else
                        {
                            setText(username + " (" + unreadMessagesCount + ")");
                            setStyle("-fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
    }

    @FXML
    private void sendGlobalMessage()
    {
        try
        {
            getModel().sendMessageToServer(Message.createNew(Message.Type.GlobalMessage)
                    .field(Message.Field.Sender, getModel().getLoggedInUsername())
                    .field(Message.Field.Data, messageTextArea.getText())
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
            for (PrivateChatController controller : privateChatControllers.values())
                controller.getStage().close();
            privateChatControllers.clear();
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
                            .field(Message.Field.Sender, getModel().getLoggedInUsername())
                            .field(Message.Field.ReportedUser, selectedUsername)
                            .field(Message.Field.Reason, reportReason.get())
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

    /**
     * Crea il controller relativo alla finestra di chat privata dell'utente dato e inizializza la finestra senza mostrarla.
     * Aggiunge inoltre il nome utente all'elenco delle chat private.
     * Viene invocato quando viene ricevuto per la prima volta un messaggio di chat privata da un determinato utente o
     * subito prima che venga aperta per la prima volta la finestra di chat privata di un utente.
     */
    private PrivateChatController createPrivateChatWindowAndController(String username)
    {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("views/privateChat.fxml"));
        try
        {
            Stage privateChatStage = new Stage();
            privateChatStage.setTitle("Chat privata con " + username);
            privateChatStage.setResizable(false);
            privateChatStage.setScene(new Scene(loader.load()));

            PrivateChatController privateChatController = loader.getController();
            privateChatController.setUser(username);
            privateChatControllers.put(username, privateChatController);
            privateChatsTable.getItems().add(username);

            // Ogni qualvolta la finestra di chat privata viene messa in primo piano, refresha la lista delle chat
            // private per aggiornare i contatori di messaggi non letti
            privateChatController.getStage().focusedProperty().addListener((__, ___, isFocused) -> {
                if (isFocused)
                    privateChatsTable.refresh();
            });

            return privateChatController;
        }
        catch (Exception exc)
        {
            System.err.println("FATAL: Error when loading private chat window: " + exc.getMessage());
            Platform.exit();
            throw new RuntimeException();
        }
    }

    /**
     * Mostra la finestra di chat privata per l'utente selezionato, creando il rispettivo controller se necessario
     */
    private void showPrivateChatWindow(String selectedUsername)
    {
        if (privateChatControllers.containsKey(selectedUsername))
        {
            Stage privateChatWindow = privateChatControllers.get(selectedUsername).getStage();
            if (privateChatWindow.isShowing())
                privateChatWindow.requestFocus();
            else
                privateChatWindow.show();
        }
        else
        {
            PrivateChatController controller = createPrivateChatWindowAndController(selectedUsername);
            controller.getStage().show();
        }
    }

    @FXML
    private void showPrivateChatFromButton()
    {
        String selectedUsername = tableViewUsers.getSelectionModel().getSelectedItem();
        showPrivateChatWindow(selectedUsername);
    }

    @FXML
    private void showPrivateChatFromList()
    {
        String selectedUsername = privateChatsTable.getSelectionModel().getSelectedItem();
        if (selectedUsername != null)
            showPrivateChatWindow(selectedUsername);
        privateChatsTable.getSelectionModel().clearSelection();
    }

    @Override
    public void notifyMessage(Message message)
    {
        IMainWindowController.super.notifyMessage(message);

        if (message.getType() == Message.Type.Ban)
        {
            Alert banAlert = new Alert(Alert.AlertType.ERROR);
            banAlert.setTitle("Utente bannato");
            banAlert.setHeaderText("Sei stato bannato");
            banAlert.setContentText("Motivo: " + message.getField(Message.Field.Reason));
            banAlert.getButtonTypes().setAll(new ButtonType("Esci dall'applicazione"));
            banAlert.showAndWait();
            Platform.exit();
        }

        super.notifyMessage(message);
        // Notifica la finestra della chat privata corrispondente all'utente relativo al messaggio, se presente
        switch (message.getType())
        {
            case UserConnected:
            case UserDisconnected:
            case UserBanned:
            case UserUnbanned:
                String username = message.getField(Message.Field.Username);
                if (privateChatControllers.containsKey(username))
                    Platform.runLater(() -> privateChatControllers.get(username).notifyMessage(message));
                break;

            case NotifySuccess:
            case NotifyError:
                String recipientUsername = message.getField(Message.Field.Data);
                if (privateChatControllers.containsKey(recipientUsername))
                    Platform.runLater(() -> privateChatControllers.get(recipientUsername).notifyMessage(message));
                break;

            case PrivateMessage:
                String sender = message.getField(Message.Field.Sender);
                // Crea la finestra e il relativo controller per memorizzare il messaggio ricevuto
                // (vedi commento sopra alla map dei controller)
                if (!privateChatControllers.containsKey(sender))
                {
                    PrivateChatController controller = createPrivateChatWindowAndController(sender);
                    Platform.runLater(() -> controller.notifyMessage(message));
                }
                else
                {
                    Platform.runLater(() -> privateChatControllers.get(sender).notifyMessage(message));
                    // Posiziona la chat in cima alla lista
                    privateChatsTable.getItems().remove(message.getField(Message.Field.Sender));
                    privateChatsTable.getItems().add(0, message.getField(Message.Field.Sender));
                }
                break;
        }
    }

    @Override
    public Stage getCurrentWindow()
    {
        return (Stage) chatList.getScene().getWindow();
    }

    @Override
    public void showStartupWindow()
    {
        ClientApp.showStartupWindow();
    }

    @Override
    public ClientModel getModel()
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
}
