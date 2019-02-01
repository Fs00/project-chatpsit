package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindowController implements IController
{
    private AdminPanelModel model;
    private Map<String, Node> contentNodes = new HashMap<>();
    private List<IController> paneControllers = new ArrayList<>();

    @FXML
    private BorderPane rootPane;
    @FXML
    private ListView sidebarMenu;

    public MainWindowController()
    {
        model = AdminPanelApp.getModel();
        loadContentNodes();
    }

    public void initialize()
    {
        sidebarMenu.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            rootPane.setCenter(contentNodes.get(newValue));
        }));
        // Select first sidebar element
        sidebarMenu.getSelectionModel().select(0);
    }

    private void loadContentNodes()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(AdminPanelApp.class.getResource("views/mainWindowPanes/globalChatPane.fxml"));
            contentNodes.put("Chat globale", loader.load());
            paneControllers.add(loader.getController());

            loader = new FXMLLoader(AdminPanelApp.class.getResource("views/mainWindowPanes/banPane.fxml"));
            contentNodes.put("Ban utenti", loader.load());
            paneControllers.add(loader.getController());

            loader = new FXMLLoader(AdminPanelApp.class.getResource("views/mainWindowPanes/reportPane.fxml"));
            contentNodes.put("Segnalazioni", loader.load());
            paneControllers.add(loader.getController());

            loader = new FXMLLoader(AdminPanelApp.class.getResource("views/mainWindowPanes/serverLogPane.fxml"));
            contentNodes.put("Log del server", loader.load());
            paneControllers.add(loader.getController());
        }
        catch (Exception exc) {}
    }

    @FXML
    public ObservableList<String> getSidebarEntries()
    {
        return FXCollections.observableArrayList(contentNodes.keySet());
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
            ((Stage) rootPane.getScene().getWindow()).close();
    }

    @FXML
    private void logoutAndQuitToLogin()
    {
        logoutAndCloseWindow();
        if (!rootPane.getScene().getWindow().isShowing())
            AdminPanelApp.showLoginWindow();
    }

    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }
}
