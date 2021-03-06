package chatpsit.adminpanel;

import chatpsit.common.Message;
import chatpsit.common.gui.ClientModel;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements IController<ClientModel>
{
    @FXML
    private Button loginButton;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPasswd;
    @FXML
    private TextField serverAddressField;

    @FXML
    private void attemptLogin()
    {
        changeControlsDisable(true);

        // Tenta connessione con il server
        try {
            getModel().connectToServer(serverAddressField.getText());
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
        Message loginMessage = Message.createNew(Message.Type.AdminPanelLogin)
                                .field(Message.Field.Username, fieldUsername.getText().trim())
                                .field(Message.Field.Password, fieldPasswd.getText())
                                .build();
        try {
            getModel().sendMessageToServer(loginMessage);
        }
        catch (Exception exc)
        {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Errore di connessione");
            errAlert.setHeaderText("Errore inatteso nell'invio del messaggio");
            errAlert.setContentText(exc.getMessage());
            errAlert.show();
        }

        fieldPasswd.setText("");
        changeControlsDisable(false);
    }

    private void changeControlsDisable(boolean disable)
    {
        fieldUsername.setDisable(disable);
        fieldPasswd.setDisable(disable);
        loginButton.setDisable(disable);
        serverAddressField.setDisable(disable);
    }

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case NotifySuccess:
                ((Stage) loginButton.getScene().getWindow()).close();
                getModel().detachController(this);
                AdminPanelApp.showMainWindow();
                break;
            case NotifyError:
                Alert errAlert = new Alert(Alert.AlertType.ERROR);
                errAlert.setHeaderText("Login fallito");
                errAlert.setContentText(message.getField(Message.Field.Data));
                errAlert.show();
                break;
        }
    }

    @Override
    public ClientModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
