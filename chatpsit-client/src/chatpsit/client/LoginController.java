package chatpsit.client;

import chatpsit.common.Message;
import chatpsit.common.ServerMode;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    private ChoiceBox<ServerMode> serverChoiceBox;

    public LoginController()
    {
        this.model = ClientApp.getModel();
        bindToModel(this.model);
    }

    public ObservableList<ServerMode> getServerChoices()
    {
        return FXCollections.observableArrayList(ServerMode.Local, ServerMode.Remote);
    }

    public ServerMode getDefaultServerChoice()
    {
        return ServerMode.Local;
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
