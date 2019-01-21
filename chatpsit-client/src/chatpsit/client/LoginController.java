package chatpsit.client;

import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController implements IController
{
    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private TextField fieldUsername;

    @FXML
    private TextField fieldPasswd;

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
