package chatpsit.client;

import chatpsit.client.model.ServerConnection;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController implements IController
{
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPasswd;
    @FXML
    private ChoiceBox<ServerConnection> serverChoiceBox;

    public ObservableList<ServerConnection> getServerChoices()
    {
        return FXCollections.observableArrayList(ServerConnection.Local, ServerConnection.Remote);
    }

    public ServerConnection getDefaultServerChoice()
    {
        return ServerConnection.Local;
    }

    @FXML
    private void showRegistrationScene()
    {
        ClientApp.showRegisterScene();
    }

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
