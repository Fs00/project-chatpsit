package chatpsit.client;

import chatpsit.client.model.ClientModel;
import chatpsit.common.Message;
import chatpsit.common.ServerMode;
import chatpsit.common.gui.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController implements IController
{
    private ClientModel model;
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

    public void initialize()
    {
        this.model = ClientApp.getModel();
        serverChoiceBox.getSelectionModel().selectedItemProperty().addListener(observable -> {
            model.changeServerMode(serverChoiceBox.getSelectionModel().getSelectedItem());
        });
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
        ClientApp.showRegisterScene();
    }

    @FXML
    private void attemptLogin()
    {
        Message loginMessage = Message.createNew(Message.Type.UserLogin)
                                .field("username", fieldUsername.getText().trim())
                                .field("password", fieldPasswd.getText())
                                .build();

        changeControlsDisable(true);
        try {
            model.sendMessageToServer(loginMessage);
        }
        catch (Exception exc)
        {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Errore di connessione");
            errAlert.setHeaderText("Errore inatteso nell'invio del messaggio al server");
            errAlert.setContentText(exc.getMessage());
            errAlert.showAndWait();
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

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case NotifySuccess:
                // TODO
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login riuscito");
                alert.showAndWait();
                break;
            case NotifyError:
                Alert errAlert = new Alert(Alert.AlertType.ERROR);
                errAlert.setTitle("Login fallito");
                errAlert.setContentText(message.getField("description"));
                errAlert.showAndWait();
                break;
        }
    }
}
