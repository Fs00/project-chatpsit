package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IMainWindowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindowController implements IMainWindowController<AdminPanelModel>
{
    private Map<String, Node> contentNodes = new HashMap<>();
    private List<IController> paneControllers = new ArrayList<>();

    @FXML
    private BorderPane rootPane;
    @FXML
    private ListView<String> sidebarMenu;

    public MainWindowController()
    {
        loadContentNodes();
    }

    public void initialize()
    {
        IMainWindowController.super.initialize();

        sidebarMenu.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)   // evita di mostrare nulla in caso di deselezionamento della voce
                rootPane.setCenter(contentNodes.get(newValue));
        });
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
        catch (Exception exc)
        {
            System.err.println("FATAL: Error when loading main window panes: " + exc.getMessage());
            Platform.exit();
        }
    }

    @FXML
    public ObservableList<String> getSidebarEntries()
    {
        return FXCollections.observableArrayList(contentNodes.keySet());
    }

    @Override
    public void notifyMessage(Message message)
    {
        paneControllers.forEach(controller -> Platform.runLater(() -> controller.notifyMessage(message)));
    }

    @Override
    public Stage getCurrentWindow()
    {
        return (Stage) rootPane.getScene().getWindow();
    }

    @Override
    public void showStartupWindow()
    {
        AdminPanelApp.showLoginWindow();
    }

    @Override
    public AdminPanelModel getModel()
    {
        return AdminPanelApp.getModel();
    }

    /*
       Fake overrides necessari siccome FXMLLoader non vede le default implementation
     */
    @FXML
    @Override
    public void logoutAndCloseWindow()
    {
        IMainWindowController.super.logoutAndCloseWindow();
    }
    @FXML
    @Override
    public void logoutAndQuitToLogin()
    {
        IMainWindowController.super.logoutAndQuitToLogin();
    }
}
