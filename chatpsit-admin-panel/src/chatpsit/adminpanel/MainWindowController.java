package chatpsit.adminpanel;

import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindowController implements IController
{
    @FXML
    private BorderPane rootNode;

    @FXML
    public ObservableList<String> getSidebarEntries()
    {
        return FXCollections.observableArrayList("Chat globale",
                "Ban utenti", "Segnalazioni", "Log del server");
    }

    @FXML
    private void quitToLogin()
    {
        // TODO logout
        ((Stage) rootNode.getScene().getWindow()).close();
        AdminPanelApp.showLoginWindow();
    }

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
