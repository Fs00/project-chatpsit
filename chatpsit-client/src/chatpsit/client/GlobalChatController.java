package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class GlobalChatController implements IController
{
    private UserClientModel model;

    @FXML
    private BorderPane rootNode;

    @FXML
    private Button sendButton;

    @FXML
    private TextArea textArea;

    @FXML
    private TableView tableViewUsers;

    @FXML
    private Button sendPrivateButton;

    @FXML
    private Button reportButton;

    public void initialize()
    {

        model = ClientApp.getModel();
        tableViewUsers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                sendPrivateButton.setDisable(false);
                reportButton.setDisable(false);
            }
            else
             {
                sendPrivateButton.setDisable(true);
                reportButton.setDisable(true);
             }
        });
    }

    boolean sendLogout()
    {
        //TODO chiudere finestre chat private
        boolean logoutSuccessful = false;
        try
        {
            Message logoutMessage = Message.createNew(Message.Type.Logout)
                    .lastMessage()
                    .build();
            model.sendMessageToServer(logoutMessage);
            logoutSuccessful = true;
        }
        catch (IOException exc)
        {
            Alert errorDialog = new Alert(Alert.AlertType.ERROR);
            errorDialog.setTitle("Errore di connessione");
            errorDialog.setHeaderText("Logout fallito");
            errorDialog.setContentText(exc.getMessage());
            errorDialog.show();
        }
        return logoutSuccessful;
    }

    @FXML
    private void logoutAndCloseWindow()
    {
        boolean logoutSuccessful = sendLogout();
        if (logoutSuccessful)
            ((Stage) rootNode.getScene().getWindow()).close();
    }

    @FXML
    private void logoutAndQuitToLogin()
    {
        logoutAndCloseWindow();
        if (!rootNode.getScene().getWindow().isShowing())
            ClientApp.showStartupWindow();
    }
    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }

    @FXML
    private void checkEmptyMessageText()
    {
        if (!textArea.getText().trim().isEmpty())
            sendButton.setDisable(false);
        else
            sendButton.setDisable(true);
    }
}
