package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.*;

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
        bindToModel(model);

        sendReadyMessageToServer();

        sidebarMenu.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            rootPane.setCenter(contentNodes.get(newValue));
        }));
        // Select first sidebar element
        sidebarMenu.getSelectionModel().select(0);
    }

    /**
     * Segnala al server che è pronto a ricevere messaggi
     * Se l'invio del messaggio fallisce, l'utente è invitato a riprovare o a chiudere l'applicazione
     */
    private void sendReadyMessageToServer()
    {
        boolean messageSent = false;
        while (!messageSent)
        {
            try
            {
                Message readyMessage = Message.createNew(Message.Type.Ready).build();
                model.sendMessageToServer(readyMessage);
                messageSent = true;
            }
            catch (Exception exc)
            {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Errore di connessione");
                errorAlert.setHeaderText("Impossibile mandare il messaggio 'READY' al server");
                errorAlert.setContentText("Riprova o esci dall'applicazione.");

                ButtonType retry = new ButtonType("Riprova", ButtonBar.ButtonData.BACK_PREVIOUS);
                ButtonType quit = new ButtonType("Esci", ButtonBar.ButtonData.FINISH);
                errorAlert.getButtonTypes().setAll(retry, quit);

                Optional<ButtonType> choice = errorAlert.showAndWait();
                if (!choice.isPresent() || choice.get() == quit)
                    Platform.exit();
            }
        }
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
            System.err.println("FATAL: Missing layout assets for main window panes");
            Platform.exit();
        }
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
            model.detachControllers();
            logoutSuccessful = true;
        }
        catch (Exception exc)
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
    public ObservableList<String> getSidebarEntries()
    {
        return FXCollections.observableArrayList(contentNodes.keySet());
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
        paneControllers.forEach(controller -> Platform.runLater(() -> controller.notifyMessage(message)));
    }
}
