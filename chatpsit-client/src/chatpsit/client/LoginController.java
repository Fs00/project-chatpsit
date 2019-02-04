package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.ServerMode;
import chatpsit.common.gui.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController implements IController
{
    private UserClientModel model;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPasswd;
    @FXML
    private ChoiceBox<ServerMode> serverChoiceBox;

    public LoginController()
    {
        this.model = ClientApp.getModel();
    }

    @FXML
    public ObservableList<ServerMode> getServerChoices()
    {
        return FXCollections.observableArrayList(ServerMode.Local, ServerMode.Remote);
    }
    @FXML
    public ServerMode getDefaultServerChoice()
    {
        return ServerMode.Local;
    }
    @FXML
    private void showRegistrationScene()
    {
        model.detachController(this);
        ClientApp.setRegisterScene((Stage) loginButton.getScene().getWindow());
    }

    @FXML
    private void attemptLogin()
    {
        changeControlsDisable(true);

        // Tenta connessione con il server
        try {
            model.connectToServer(serverChoiceBox.getSelectionModel().getSelectedItem());
        }
        catch (Exception exc)
        {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Errore di connessione");
            errAlert.setHeaderText("Impossibile connettersi al server");
            errAlert.setContentText(exc.getMessage());
            errAlert.show();

            changeControlsDisable(false);
            return;
        }

        // Procedi con il login se la connessione è andata a buon fine
        Message loginMessage = Message.createNew(Message.Type.UserLogin)
                                .field("username", fieldUsername.getText().trim())
                                .field("password", fieldPasswd.getText())
                                .build();
        try {
            model.sendMessageToServer(loginMessage);
        }
        catch (Exception exc)
        {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Errore di accesso");
            errAlert.setHeaderText("Errore inatteso nell'invio del messaggio di login");
            errAlert.setContentText(exc.getMessage());
            errAlert.show();
        }

        changeControlsDisable(false);
    }

    private void changeControlsDisable(boolean disable)
    {
        fieldUsername.setDisable(disable);
        fieldPasswd.setDisable(disable);
        loginButton.setDisable(disable);
        registerButton.setDisable(disable);
        serverChoiceBox.setDisable(disable);
    }

    /**
     * Gestisce la risposta al messaggio di login
     * @param message risposta ricevuta dal server
     */
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case NotifySuccess:
                ((Stage) loginButton.getScene().getWindow()).close();
                model.detachController(this);
                ClientApp.showGlobalChatWindow();
                break;
            case NotifyError:
                Alert errAlert = new Alert(Alert.AlertType.ERROR);
                errAlert.setHeaderText("Login fallito");
                errAlert.setContentText(message.getField("description"));
                errAlert.show();
                break;
        }
    }
}
