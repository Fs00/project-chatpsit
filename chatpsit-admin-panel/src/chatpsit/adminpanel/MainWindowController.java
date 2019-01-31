package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController implements IController
{
    private AdminPanelModel model;

    @FXML
    private BorderPane rootNode;

    public void initialize()
    {
        model = AdminPanelApp.getModel();
    }

    @FXML
    public ObservableList<String> getSidebarEntries()
    {
        return FXCollections.observableArrayList("Chat globale",
                "Ban utenti", "Segnalazioni", "Log del server");
    }

    boolean sendLogout()
    {
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
            AdminPanelApp.showLoginWindow();
    }

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
