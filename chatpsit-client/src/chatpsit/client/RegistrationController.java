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

public class RegistrationController implements IController
{
    private UserClientModel model;
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
        model.detachController(this);
        ClientApp.setLoginScene((Stage) registerButton.getScene().getWindow());
    }

    @FXML
    private void attemptRegistration()
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

        // Procedi con la registrazione se la connessione è andata a buon fine
        Message registrationMessage = Message.createNew(Message.Type.Register)
                .field("username", fieldUsername.getText().trim())
                .field("password", fieldPasswd.getText())
                .build();

        try {
            model.sendMessageToServer(registrationMessage);
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
        serverChoiceBox.setDisable(disable);
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
                errAlert.setContentText(message.getField("description"));
                errAlert.show();
                break;
        }
    }
}
