package chatpsit.client;

import chatpsit.client.model.ServerConnection;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class LoginController implements IController
{
    private IModel model;
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

    public LoginController()
    {
        this.model = ClientApp.getModel();
        bindToModel(this.model);
    }

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
        switch (message.getType())
        {
            case NotifySuccess:
                // TODO
                break;
            case NotifyError:
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login fallito");
                alert.setContentText(message.getFields().get("description"));   // TODO cambierà a seconda del messaggio di errore
                alert.showAndWait();
                break;
        }
    }
}
