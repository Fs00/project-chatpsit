package chatpsit.client;

import chatpsit.common.gui.ClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistrationController implements IController<ClientModel>
{
    @FXML
    private Button showLoginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPasswd;
    @FXML
    private TextField serverAddressField;

    @FXML
    private void backToLogin()
    {
        getModel().detachController(this);
        ClientApp.setLoginScene((Stage) registerButton.getScene().getWindow());
    }

    @FXML
    private void attemptRegistration()
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

        // Procedi con la registrazione se la connessione è andata a buon fine
        Message registrationMessage = Message.createNew(Message.Type.Register)
                .field(Message.Field.Username, fieldUsername.getText().trim())
                .field(Message.Field.Password, fieldPasswd.getText())
                .build();

        try {
            getModel().sendMessageToServer(registrationMessage);
        }
        catch (Exception exc)
        {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Errore di connessione");
            errAlert.setHeaderText("Errore inatteso nell'invio del messaggio");
            errAlert.setContentText(exc.getMessage());
            errAlert.show();
        }

        changeControlsDisable(false);
        clearTextFields();
    }

    private void clearTextFields()
    {
        fieldUsername.setText("");
        fieldPasswd.setText("");
    }

    private void changeControlsDisable(boolean disable)
    {
        fieldUsername.setDisable(disable);
        fieldPasswd.setDisable(disable);
        showLoginButton.setDisable(disable);
        registerButton.setDisable(disable);
        serverAddressField.setDisable(disable);
    }

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case NotifySuccess:
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Operazione riuscita");
                alert.setHeaderText("Registrazione effettuata");
                alert.setContentText("Effettua l'accesso con il tuo nuovo utente.");
                alert.show();
                break;
            case NotifyError:
                Alert errAlert = new Alert(Alert.AlertType.ERROR);
                errAlert.setHeaderText("Registrazione fallita");
                errAlert.setContentText(message.getField(Message.Field.Data));
                errAlert.show();
                break;
        }
    }

    @Override
    public ClientModel getModel()
    {
        return ClientApp.getModel();
    }
}
