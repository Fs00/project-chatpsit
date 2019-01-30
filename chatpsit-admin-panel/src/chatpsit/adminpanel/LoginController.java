package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
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
    private AdminPanelModel model;
    @FXML
    private Button loginButton;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPasswd;
    @FXML
    private ChoiceBox<ServerMode> serverChoiceBox;

    public void initialize()
    {
        this.model = AdminPanelApp.getModel();
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
    private void attemptLogin()
    {
        Message loginMessage = Message.createNew(Message.Type.AdminPanelLogin)
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
            errAlert.setHeaderText("Errore inatteso nell'invio del messaggio");
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
        serverChoiceBox.setDisable(disable);
    }

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case NotifySuccess:
                ((Stage) loginButton.getScene().getWindow()).close();
                AdminPanelApp.showMainWindow();
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
