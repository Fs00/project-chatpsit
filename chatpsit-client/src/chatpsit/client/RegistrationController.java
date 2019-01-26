package chatpsit.client;

import chatpsit.common.Message;
import chatpsit.common.ServerMode;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController implements IController
{
    private IModel model;
    @FXML
    private Button showLoginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPasswd;
    @FXML
    private ChoiceBox<ServerMode> serverChoiceBox;

    public RegistrationController()
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
    private void backToLogin()
    {
        ClientApp.showLoginScene();
    }

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
